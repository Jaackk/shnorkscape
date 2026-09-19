"""Offline bounded A/B/C comparison. Never interprets an absent entry as zero."""
import hashlib
import json
import sys
from pathlib import Path

HERE = Path(__file__).resolve().parent

def state(item):
    return (item['found'], item['variantTag'], item['int32'])

def load(path, phase, schema, schema_hash):
    data = json.loads(Path(path).read_text())
    if data['status'] != 'snapshot-stable' or data['phase'] != phase or data['nativeWrites'] is not False or data['schemaSha256'] != schema_hash:
        raise ValueError('Refused snapshot or schema/phase mismatch: '+str(path))
    items = {i['id']: i for i in data['items']}
    if len(items) != len(data['items']) or set(items) != set(schema['ids']):
        raise ValueError('Snapshot ID scope mismatch')
    for item in items.values():
        if not item['stable']:
            raise ValueError('Unstable entry')
        if item['found']:
            if item['variantTag'] != 0 or type(item['int32']) is not int or not -(2**31) <= item['int32'] < 2**31:
                raise ValueError('Invalid integer representation')
        elif item['variantTag'] is not None or item['int32'] is not None:
            raise ValueError('Absent entry has a fabricated value')
    return data, items

def field(items, ref):
    bit, parent, lo, hi = ref
    item = items[parent]
    if not item['found']:
        return None
    return ((item['int32'] & 0xffffffff) >> lo) & ((1 << (hi-lo+1))-1)

def matrix_compare(left, right, left_rows, right_rows):
    equal, missing, different = 0, [], []
    for actor, row in left_rows.items():
        for index, ref in enumerate(row):
            saved, restored = field(left,ref),field(right,right_rows[actor][index])
            if saved is None or restored is None:
                missing.append([int(actor),index+2])
            elif saved == restored:
                equal += 1
            else:
                different.append([int(actor),index+2])
    return {'equalFields':equal,'missingFields':missing,'differentFields':different,
            'completeEquality':equal==720,
            'allReadableFieldsEqual':equal>0 and not different}

def compare(a, b, c, schema):
    def changed(x,y,ids):
        return [i for i in ids if state(x[i]) != state(y[i])]
    by_slot = {}
    for slot, rows in schema['slots'].items():
        ids = sorted({ref[1] for row in rows.values() for ref in row})
        by_slot[slot] = {'changedAtoB':changed(a,b,ids), 'changedBtoC':changed(b,c,ids),
                         'presentB':sum(b[i]['found'] for i in ids),'presentC':sum(c[i]['found'] for i in ids)}
    paired_changes = []
    for actor,row in schema['slots']['6'].items():
        for index,ref in enumerate(row):
            active=schema['slots']['8'][actor][index]
            before,after=field(b,ref),field(c,ref)
            if before != after:
                paired_changes.append({'actor':int(actor),'argument':index+2,
                    'customParent':ref[1],'activeParent':active[1],
                    'sameChangeInActive':before==field(b,active) and after==field(c,active)})
    return {'changedIdsAtoB':changed(a,b,schema['ids']), 'changedIdsBtoC':changed(b,c,schema['ids']),
            'slots':by_slot,'metadataChangedAtoB':changed(a,b,schema['metadataIds']),
            'metadataChangedBtoC':changed(b,c,schema['metadataIds']),
            'custom1BvsActiveC':matrix_compare(b,c,schema['slots']['6'],schema['slots']['8']),
            'custom1BvsActiveB':matrix_compare(b,b,schema['slots']['6'],schema['slots']['8']),
            'custom1CvsActiveC':matrix_compare(c,c,schema['slots']['6'],schema['slots']['8']),
            'pairedCustomActiveChanges':paired_changes,
            'custom1RetainedBtoC':not by_slot['6']['changedBtoC'],
            'note':'Missing native entries remain unknown; no default-provider reads. Keep client dimensions unchanged. Native Load may transform fields; mismatches require inspection, not forced equality.'}

if __name__ == '__main__':
    if len(sys.argv) != 4:
        raise SystemExit('Usage: compare_snapshots.py A.json B.json C.json')
    raw = (HERE/'snapshot-schema.json').read_bytes()
    schema = json.loads(raw)
    snapshots = [load(p,phase,schema,hashlib.sha256(raw).hexdigest()) for p,phase in zip(sys.argv[1:], 'ABC')]
    if len({(d['pid'],d['thread'],d['imageSha256']) for d,items in snapshots}) != 1 or not snapshots[0][0]['tick'] < snapshots[1][0]['tick'] < snapshots[2][0]['tick']:
        raise ValueError('Snapshots must be ordered in one client/game-thread session')
    print(json.dumps(compare(*(items for d,items in snapshots),schema),indent=2))
