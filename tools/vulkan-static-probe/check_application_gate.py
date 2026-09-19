"""Pinned disposable fixture validation/readback; no client or server writes."""
import hashlib
import json
import sys
from pathlib import Path
from compare_snapshots import HERE, load, state, matrix_compare
from check_setter_gate import CONTROLS

ROOT = HERE.parents[1]
IMAGE = '3d4e432e8cb81d5b83cd3cb2064669228d24231779ae997fe78335a81e43364d'
SCHEMA = '53c06c70b12ba0f68ff71a3e46db4c46fdc7f3a3792e3ce6c2656066e692119d'
FIXTURE = '958508114f036246a433060ab0b02e6afccbd964e52e567e9efd8dac4efec1e0'
CONTROL_SHA = 'dc61ee972be3a91cdd328aeed7102f3e1fec188d6270c472ac0e815dc0616881'
LOAD_READ_SHA = '84067abe35df0656aac05fa9cc12e719a652f8a1db5c429c55ef2d309adc88d3'

def custom_visibility_constants():
    # Unlike the raw storage schema, native8701 returns literal1 for these fields.
    import snapshot_schema as s
    from unittest.mock import patch
    path='cache/12/8701.dat'
    require(hashlib.sha256((ROOT/path).read_bytes()).hexdigest()==LOAD_READ_SHA,'Changed native Load reader')
    inverse={int(v['opcode950'],16):int(k,16) for k,v in json.loads(s.pinned('protocol-analysis/ui-scripts-950-evidence.json'))['opcodeMap947to950'].items()}
    with patch.dict(s.PINS,{path:LOAD_READ_SHA}): ins,switches=s.script(8701,inverse)
    result=[]
    for actor in (1032,1033,1034,1035):
        for slot in (6,7,12,13):
            start=32+switches[0][slot]
            require(ins[start]==(0x35e,1) and ins[start+1][0]==0x51a,'Native actor dispatch changed')
            at=start+2+switches[ins[start+1][1]][actor]
            require(all(op==0x30 for op,arg in ins[at:at+6]) and ins[at+6]==(0x511,1)
                and ins[at+7][0]==0x30 and ins[at+8]==(0x511,1)
                and ins[at+9:at+18]==[(0x592,i) for i in range(10,1,-1)],'Custom visibility is no longer literal1')
        result.append([actor,8])
    return result

def require(ok, message):
    if not ok:
        raise ValueError(message)

def controls(path, data, bootstrap=False):
    raw = Path(path).with_name(Path(path).stem+'-controls.json').read_bytes()
    c = json.loads(raw)
    require(all(c[k] == data[k] for k in ('pid','thread','phase','nativeWrites')), 'Control session mismatch')
    rows = {i['id']:i for i in c['controls']}
    require(len(rows)==len(c['controls'])==7 and set(rows)==set(CONTROLS)|{3296}, 'Control scope mismatch')
    for i, expected in CONTROLS.items():
        r=rows[i]
        require(r['stable'] and r['found'] and r['variantTag']==0 and type(r['int32']) is int, 'Unstable control')
        if bootstrap:
            require(r['int32']==expected, 'Bootstrap control mismatch')
    return raw, rows

def source():
    raw=(HERE/'snapshot-schema-v4.json').read_bytes()
    require(hashlib.sha256(raw).hexdigest()==SCHEMA, 'Changed schema')
    schema=json.loads(raw)
    path=ROOT/'logs/workspace-static-v4-35192-A.json'
    require(hashlib.sha256(path.read_bytes()).hexdigest()==FIXTURE, 'Changed source fixture')
    d,items=load(path,'A',schema,SCHEMA)
    require(d['version']==4 and d['pid']==35192 and d['imageSha256']==IMAGE, 'Source image/session mismatch')
    raw,rows=controls(path,d,True)
    require(hashlib.sha256(raw).hexdigest()==CONTROL_SHA, 'Changed source controls')
    require(state(rows[3296])==state(items[3296]) and items[3296]['found'], '3296 source mismatch')
    require([items[i]['int32'] for i in (8372,8373,8374)]==[6,6,0], 'Source is not committed Custom1')
    relationship=matrix_compare(items,items,schema['slots']['6'],schema['slots']['8'])
    require(relationship['allReadableFieldsEqual'], 'Source Custom1 does not match active')
    return schema,items,{'fixture':'validated','sourceAccount':'layoutgate1 (user-confirmed)',
        'fixtureSha256':FIXTURE,'schemaSha256':SCHEMA,'entries':len(items),
        'custom1VsActive':relationship,'absentIsNotZero':True,'application':'not yet tested'}

def compare(paths):
    schema,fixture,report=source()
    snapshots=[load(p,phase,schema,SCHEMA) for p,phase in zip(paths,'ABC')]
    require(len(snapshots)==3,'Need A/B/C')
    data=[d for d,items in snapshots]
    require(len({(d['pid'],d['thread'],d['imageSha256']) for d in data})==1 and data[0]['imageSha256']==IMAGE
        and data[0]['pid']!=35192 and data[0]['tick']<data[1]['tick']<data[2]['tick'], 'Wrong target image/session/order')
    for index,(p,d) in enumerate(zip(paths,data)):
        controls(p,d,index==0)
    a,b,c=[items for d,items in snapshots]
    matrix_ids={r[1] for slot in ('6','7','12','13') for row in schema['slots'][slot].values() for r in row}
    integers=matrix_ids | set(schema['integerMetadataIds'])
    metadata={m['id']:m for m in schema['slotMetadata'].values()}
    stage_errors=[]
    unknown_sibling_baselines=[]
    for i in schema['ids']:
        if i in integers and fixture[i]['found']:
            ok=state(b[i])==state(fixture[i])
        elif i in metadata and fixture[i]['found']:
            mask=metadata[i]['mask']
            ok=b[i]['found'] and b[i]['int32']&mask==fixture[i]['int32']&mask
            if a[i]['found']:
                ok=ok and (b[i]['int32']&~mask)==(a[i]['int32']&~mask)
            else:
                unknown_sibling_baselines.append(i)
        else:
            ok=state(b[i])==state(a[i])
        if not ok: stage_errors.append(i)
    custom_changes=[i for i in sorted(matrix_ids) if state(b[i])!=state(c[i])]
    relationship=matrix_compare(fixture,c,schema['slots']['6'],schema['slots']['8'])
    constants=custom_visibility_constants()
    from compare_snapshots import field
    explained=[pair for pair in relationship['differentFields'] if pair in constants
        and field(c,schema['slots']['8'][str(pair[0])][6])==1]
    unexplained=[pair for pair in relationship['differentFields'] if pair not in explained]
    for pair in constants:
        if field(c,schema['slots']['8'][str(pair[0])][6])!=1 and pair not in unexplained:
            unexplained.append(pair)
    report.update({'targetAccount':'must separately confirm layoutgate2','stageMismatchIds':stage_errors,
        'metadataSiblingBaselineAbsent':unknown_sibling_baselines,
        'customMatrixChangedDuringApply':custom_changes,
        'sourceCustom1VsAppliedActive':relationship,
        'native8701LiteralVisibilityFields':explained,'unexplainedApplicationFields':unexplained,
        'structuralApplicationPassed':not stage_errors and not custom_changes and not unexplained and not unknown_sibling_baselines,
        'appliedCustom1VsAppliedActive':matrix_compare(c,c,schema['slots']['6'],schema['slots']['8']),
        'application':'requires visual confirmation and review of any native transformation; never auto-waive mismatches'})
    return report

if __name__=='__main__':
    if len(sys.argv) not in (1,4): raise SystemExit('Usage: check_application_gate.py [target-A target-B target-C]')
    print(json.dumps(source()[2] if len(sys.argv)==1 else compare(sys.argv[1:]),indent=2))
