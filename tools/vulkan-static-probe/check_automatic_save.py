"""Validate the disposable NEW Save -> stored record -> fresh-process readback gate."""
import hashlib
import json
import struct
import sys
from pathlib import Path
from check_application_gate import ROOT, SCHEMA, controls, require
from compare_snapshots import HERE, load, state, matrix_compare

IMAGE = '19323515092bbccd0090033badbc56177d0ca599216e4d702be27432cf693a13'
OLD_IMAGE = '3d4e432e8cb81d5b83cd3cb2064669228d24231779ae997fe78335a81e43364d'


def record(path, schema):
    raw = Path(path).read_bytes()
    require(32 <= len(raw) <= 16384, 'Sidecar size')
    body = raw[:-32]
    require(hashlib.sha256(body).digest() == raw[-32:], 'Sidecar checksum')
    at = 0

    def take(fmt):
        nonlocal at
        size = struct.calcsize(fmt)
        result = struct.unpack_from(fmt, body, at)
        at += size
        return result[0] if len(result) == 1 else result

    def utf():
        nonlocal at
        size = take('>H')
        result = body[at:at+size].decode('ascii')
        require(len(result) == size, 'Truncated sidecar string')
        at += size
        return result

    require(take('>III') == (0x57533935, 1, 950), 'Sidecar version')
    require(utf() == SCHEMA, 'Sidecar schema')
    image = utf()
    require(image in (IMAGE, OLD_IMAGE) and utf() == 'layoutgate2', 'Sidecar image/account')
    revision = take('>q')
    require(revision > 0 and take('>H') == 912, 'Sidecar revision/count')
    result = {}
    for expected in schema['ids']:
        identity, tag = take('>HB')
        require(identity == expected and tag in (0, 1), 'Sidecar IDs/types')
        result[identity] = {'found': bool(tag), 'variantTag': 0 if tag else None,
                            'int32': take('>i') if tag else None}
    require(at == len(body), 'Trailing sidecar bytes')
    return revision, result, image


def compare(saved_path, restarted_path):
    raw = (HERE/'snapshot-schema-v4.json').read_bytes()
    require(hashlib.sha256(raw).hexdigest() == SCHEMA, 'Changed V4 schema')
    schema = json.loads(raw)
    path = ROOT/'workspace-state950'/(hashlib.sha256(b'layoutgate2').hexdigest()+'.workspace950')
    revision, stored, image = record(path, schema)
    old_revision, previous, _ = record(path.with_name(path.name+'.previous'), schema)
    require(image == IMAGE and revision == old_revision+1, 'No new automatic V5 generation')
    captures = [load(p, 'A', schema, SCHEMA) for p in (saved_path, restarted_path)]
    require(captures[0][0]['pid'] != captures[1][0]['pid'], 'Need separate processes')
    for p, (d, _) in zip((saved_path, restarted_path), captures):
        require(d['version'] == 5 and d['imageSha256'] == IMAGE, 'Wrong capture image')
        controls(p, d)
    saved, restarted = [items for _, items in captures]
    custom = {ref[1] for slot in ('6', '7', '12', '13')
              for row in schema['slots'][slot].values() for ref in row}
    mismatch = lambda left, right, ids: [i for i in sorted(ids) if state(left[i]) != state(right[i])]
    saved_mismatch = mismatch(stored, saved, custom)
    restart_mismatch = mismatch(stored, restarted, custom)
    new_ids = mismatch(stored, previous, custom)
    metadata_errors = []
    for items in (saved, restarted):
        ids = mismatch(stored, items, schema['integerMetadataIds'])
        for m in schema['slotMetadata'].values():
            i, mask = m['id'], m['mask']
            if stored[i]['found'] != items[i]['found'] or (stored[i]['found'] and
                    stored[i]['int32'] & mask != items[i]['int32'] & mask):
                ids.append(i)
        metadata_errors.append(sorted(ids))
    return {'revision': revision, 'changedCustomIdsSincePrevious': new_ids,
            'postSaveCustomMismatchIds': saved_mismatch, 'restartCustomMismatchIds': restart_mismatch,
            'metadataMismatchIds': metadata_errors,
            'rawActiveSaveVsRestart': matrix_compare(saved, restarted, schema['slots']['8'], schema['slots']['8']),
            'structuralDurableCustomsPassed': bool(new_ids) and not saved_mismatch and not restart_mismatch and not any(metadata_errors),
            'note': 'Requires separate user confirmation of the NEW visible layout. Raw active differences are reported, not waived.'}


if __name__ == '__main__':
    if len(sys.argv) != 3:
        raise SystemExit('Usage: check_automatic_save.py post-save-A.json fresh-process-A.json')
    report = compare(*sys.argv[1:])
    print(json.dumps(report, indent=2))
    if not report['structuralDurableCustomsPassed']:
        raise SystemExit(1)
