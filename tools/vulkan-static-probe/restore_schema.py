"""Offline schema/restore planning only. No network, native calls or disk persistence.

The V3 capture is deliberately insufficient input: five copy-helper parents must
be acquired before constructing a complete plan. Do not fill them with guesses.
"""
import hashlib
import json
from pathlib import Path
from unittest.mock import patch
import snapshot_schema as source

SCHEMA_VERSION = 1
V3_HASH = '6f86d06ad857f7d878e0375372b9bdc93e598abd0586f6d4b2f936f34cb3269c'
HELPER_PINS = {
    'cache/12/8703.dat':'20fbc3c6f541d6dc77bbd2338f641415d98f50095c4b2200a3cbb61ef8cf4541',
    'cache/12/8700.dat':'06cbd315d03fa60e4c3b3e1f403827e273c4b8a72070b59daf7b04a0a4dcaad8',
}

def schema():
    raw=(source.HERE/'snapshot-schema.json').read_bytes()
    source.require(hashlib.sha256(raw).hexdigest()==V3_HASH,'V3 schema changed')
    v3=json.loads(raw)
    mapping=json.loads(source.pinned('protocol-analysis/ui-scripts-950-evidence.json'))
    inverse={int(v['opcode950'],16):int(k,16) for k,v in mapping['opcodeMap947to950'].items()}
    # Pinned decoder extension local to offline generation; the V3 schema is untouched.
    with patch.dict(source.PINS,HELPER_PINS):
        ins,switches=source.script(8703,inverse)
        flags,flag_switches=source.script(8700,inverse)
    bits=source.varbits()
    metadata={}
    for slot in (6,7,12,13,8):
        at=7+switches[0][slot]
        source.require(ins[at]==(0x35e,2) and ins[at+1][0]==0xa2,'Copy helper changed')
        bit=(ins[at+1][1]>>8)&65535
        r=source.Reader(bits[bit])
        source.require((r.num(1),r.num(1))==(1,2),'Wrong metadata domain')
        parent=r.smart()
        source.require((r.num(1),r.num(1),r.num(1),r.num(1))==(2,0,4,0),'Wrong metadata mask')
        source.require(r.p==len(r.data),'Trailing metadata')
        at=2+flag_switches[0][slot]
        source.require(flags[at]==(0x35e,1) and flags[at+1][0]==0xa2,'Flag helper changed')
        flag=(flags[at+1][1]>>8)&65535
        r=source.Reader(bits[flag])
        source.require((r.num(1),r.num(1),r.smart(),r.num(1),r.num(1),r.num(1),r.num(1))==(1,2,parent,2,5,5,0),'Flag sibling mismatch')
        metadata[str(slot)]={'id':parent,'varbit':bit,'mask':31,'excludedFlagVarbit':flag,'excludedFlagMask':32}
    ids=sorted(set(v3['ids'])|{v['id'] for v in metadata.values()})
    source.require(len(ids)==912,'Unexpected restore coverage')
    body={'version':SCHEMA_VERSION,'v3SchemaSha256':V3_HASH,'helperPins':HELPER_PINS,
          'ids':ids,'matrixIds':v3['descriptorIds'],'integerMetadataIds':v3['metadataIds'],
          'slotMetadata':metadata,'slots':v3['slots']}
    body['schemaSha256']=hashlib.sha256(json.dumps(body,sort_keys=True,separators=(',',':')).encode()).hexdigest()
    return body

def plan(envelope, account, session, expected_schema):
    """Produce non-executable, validated intentions. Runtime application stays gated."""
    source.require(envelope.get('version')==SCHEMA_VERSION,'Wrong version')
    source.require(envelope.get('account')==account and envelope.get('session')==session,'Account/session mismatch')
    source.require(envelope.get('schemaSha256')==expected_schema['schemaSha256'],'Schema mismatch')
    records=envelope.get('items')
    source.require(type(records) is list and len(records)==912,'Complete 912-ID capture required')
    values={}
    for item in records:
        source.require(set(item)=={'id','found','variantTag','int32'},'Unexpected record shape')
        i=item['id']
        source.require(type(i) is int and i in expected_schema['ids'] and i not in values,'Duplicate/unknown ID')
        source.require(type(item['found']) is bool,'Bad presence tag')
        if item['found']:
            source.require(type(item['variantTag']) is int and item['variantTag']==0 and
                           type(item['int32']) is int and -(2**31)<=item['int32']<2**31,'Not int32')
        else:
            source.require(item['variantTag'] is None and item['int32'] is None,'Absent is not zero')
        values[i]=item['int32'] if item['found'] else None
    selected=values[8372]
    source.require(selected in (6,7,12,13),'No committed Custom selection')
    selected_ids={r[1] for row in expected_schema['slots'][str(selected)].values() for r in row}
    source.require(any(values[i] is not None for i in selected_ids),'Selected preset has no contents')
    source.require(values[expected_schema['slotMetadata'][str(selected)]['id']] is not None,'Selected slot metadata absent')
    # Order: content, masked slot metadata, then tokens. No bootstrap edits or script calls.
    stages=[]
    for slot in (6,7,12,13):
        ids=sorted({r[1] for row in expected_schema['slots'][str(slot)].values() for r in row})
        stages.append({'kind':'custom-matrix','slot':slot,'values':[[i,values[i]] for i in ids if values[i] is not None]})
    stages.append({'kind':'slot-metadata-bits','values':[
        [m['varbit'],values[m['id']]&m['mask']] for m in expected_schema['slotMetadata'].values() if values[m['id']] is not None]})
    stages.append({'kind':'integer-metadata','values':[[i,values[i]] for i in expected_schema['integerMetadataIds'] if values[i] is not None]})
    return {'executable':False,'selectedSlot':selected,'stages':stages,
            'activeMatrixRetainedNotReplayed':True,
            'requiredGates':['verified-native-setter-domain','post-bootstrap-fence',
                             'verified-native-apply-wrapper','post-apply-readback'],
            'absentPolicy':'skip; never zero/delete native state',
            'note':'Caller account/session must come from authenticated binding, not the envelope. No runtime integration exists.'}

if __name__=='__main__':
    s=schema()
    print(json.dumps({k:v for k,v in s.items() if k not in ('slots','matrixIds','ids')},indent=2))
