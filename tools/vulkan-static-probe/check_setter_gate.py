"""Verify disposable existing-setter readback. Does not send packets or run clients."""
import hashlib
import json
import sys
from pathlib import Path
from compare_snapshots import HERE,load,state

IMAGE='38bd30b4a7bbc68e00af8de4d892585fea17cf3dfc4cce5ea8ff743e24689f98'
CONTROLS={2852:319951120,2912:32,3721:100992003,4955:16780678,5139:-2146664148,6458:8390656}

def verify(snapshots,controls):
    if len(snapshots)!=3 or len(controls)!=3:
        raise ValueError('Need three snapshots and controls')
    data=[d for d,items in snapshots]
    if [d['phase'] for d in data]!=list('ABC') or len({(d['pid'],d['thread'],d['imageSha256']) for d in data})!=1 or data[0]['imageSha256']!=IMAGE:
        raise ValueError('Wrong image/session/phase')
    if not data[0]['tick']<data[1]['tick']<data[2]['tick']:
        raise ValueError('Wrong capture order')
    for d,c in zip(data,controls):
        if c['pid']!=d['pid'] or c['thread']!=d['thread'] or c['phase']!=d['phase'] or c['nativeWrites'] is not False:
            raise ValueError('Control session mismatch')
        by_id={i['id']:i for i in c['controls']}
        for i,v in CONTROLS.items():
            row=by_id[i]
            if not row['found'] or not row['stable'] or row['variantTag']!=0 or row['int32']!=v:
                raise ValueError('Bootstrap control changed')
    a,b,c=[items for d,items in snapshots]
    if state(a[3296])!=(False,None,None):
        raise ValueError('3296 already present; not a fresh absent-parent gate')
    if state(b[3296])!=(True,0,4097) or state(c[3296])!=(True,0,0):
        raise ValueError('Native setter readback did not match 4097 then stored zero')
    other=sorted(i for i in a if i!=3296 and (state(a[i])!=state(b[i]) or state(b[i])!=state(c[i])))
    if other:
        raise ValueError('Other captured IDs changed; inspect before passing: '+','.join(map(str,other)))
    return {'readbackGate':'passed','id':3296,'observed':['absent','int32:4097','int32:0'],
            'bootstrapControls':'unchanged','otherCapturedIds':'unchanged',
            'scope':'907 workspace IDs plus six controls, not all native state',
            'accountBinding':'must separately confirm disposable account; snapshots do not encode identity',
            'applicationGate':'not performed','durability':'disabled'}

if __name__=='__main__':
    if len(sys.argv)!=4:raise SystemExit('Usage: check_setter_gate.py A.json B.json C.json')
    raw=(HERE/'snapshot-schema.json').read_bytes();schema=json.loads(raw)
    captures=[load(p,phase,schema,hashlib.sha256(raw).hexdigest()) for p,phase in zip(sys.argv[1:],'ABC')]
    controls=[json.loads(Path(p).with_name(Path(p).stem+'-controls.json').read_text()) for p in sys.argv[1:]]
    print(json.dumps(verify(captures,controls),indent=2))
