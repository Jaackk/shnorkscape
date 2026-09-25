"""Bounded read-only evidence for DEV-UI-NATIVE-CAPABILITIES-950.md.

Inputs: paired cache and the Java export (see report). Writes derived metadata only.
No legacy IF3 hook decoder: it misreads format 11. Only the common header is decoded.
CS3503's unmapped wire 0x0582 is retained UNKNOWN; its one-byte operand is a
structural hypothesis checked against the exact instruction count/end boundary.
This is not a production opcode-map addition or a Vulkan acceptance test.
"""
import argparse
import hashlib
import json
import struct
from collections import Counter
from pathlib import Path

from build_necromancy_polish_cache import unpack
from library_trace_read import scope, inverse

ROOT = Path(__file__).resolve().parents[1]
SCRIPTS = (31,616,1165,1607,1608,3503,3869,4213,6443,6445,6446,6449,
           6450,6451,6455,7411,8479,8480,10324,10882,11054,11061,11074,
           11081,11613,11618,11619,11620,15000,15001,15002,15005,17958,
           17960,17961,17962,18116,18117,18120,18274,20604,20659)
COMPONENTS = {1448:(0,3,5,7,14),1311:(136,140,143,340,341,343,344,362,371,657),
              1495:(4,14,15,16,17,18,33,34,35,69),549:(16,17,19,20,61),
              753:(27,28,30,31,38,39,40),908:(0,1,2,3,4,5,6,7,8),1092:(1,8,41),
              900:(7,14),1591:(2,)}

def sha(data):
    return hashlib.sha256(data).hexdigest()

def header(data):
    version, kind = data[:2]
    assert version in (4,5,6,9,11,255), version
    start = data.index(0,2)+1 if kind & 128 else 2
    content,x,y,w,h,wm,hm,xm,ym,parent,flags = struct.unpack_from('>HhhHHbbbbHB',data,start)
    return dict(format=version,type=kind&127,contentType=content,x=x,y=y,width=w,height=h,
                widthMode=wm,heightMode=hm,xMode=xm,yMode=ym,parent=parent,flags=flags,
                sha256=sha(data))

def main():
    ap=argparse.ArgumentParser()
    ap.add_argument('--components',type=Path,required=True)
    ap.add_argument('--output',type=Path,required=True)
    args=ap.parse_args()
    report={'scope':'Static exact-950 capability evidence; no Vulkan preview acceptance',
            'sourceBaseline':'f2bf37230a3658c37ff4ebb1ffad4d2c3f949563',
            'namePolicy':'949 Gamevals are leads unless separately verified in the existing runtime lookup',
            'interfaces':{},'components':{},'scripts':{}}
    for group in (1448,1311,1495,549,753,908,1092,1499,1850,1218,656,1384,900,
                  623,475,1462,517,1265,1507,1519,1524,1591,1315,1638,1719,1421,1422,1460):
        files=sorted(args.components.glob(f'3-{group}-*.bin'),key=lambda p:int(p.stem.split('-')[-1]))
        assert files,group
        kinds=Counter(header(f.read_bytes())['type'] for f in files)
        report['interfaces'][str(group)]={'count':len(files),'types':dict(sorted(kinds.items())),
            'payloadsSha256':sha(b''.join(f.read_bytes() for f in files)),
            'staticModelChildren':[int(f.stem.split('-')[-1]) for f in files if header(f.read_bytes())['type']==6]}
    for group,children in COMPONENTS.items():
        for child in children:
            report['components'][f'{group}:{child}']=header((args.components/f'3-{group}-{child}.bin').read_bytes())
    mapping=dict(inverse)
    mapping[0x582]=0x10582
    programs={}
    for sid in SCRIPTS:
        data=unpack((ROOT/f'cache/12/{sid}.dat').read_bytes())
        ins,tail=scope['decode'](data,mapping)
        programs[sid]=[(op,arg) for _,_,op,arg,_ in ins]
        report['scripts'][str(sid)]={'sha256':sha(data),'instructions':len(ins),
            'args':[int.from_bytes(tail[n:n+2],'big') for n in (10,12,14)],
            'calls':sorted(set(a for op,a in programs[sid] if op==0x895)),
            'stateOperands':[{'op':hex(op),'packed':a,'id':(a>>8)&65535,'namespace':a>>24}
                for op,a in sorted(set((op,a) for op,a in programs[sid] if op in (0x1ca,0x195,0x30,0xa2)))],
            'unknownWireOpcodes':sorted(set('0x0582' for op,a in programs[sid] if op==0x10582))}
    # Independent native usages agree on full-NPC and sequence setters and their target.
    assert programs[10882][39:46]==[(0x511,(0,0)),(0x511,(0,32)),(0x511,(0,9590)),
            (0x1ca,1294080),(0x67c,0),(0x511,(0,(549<<16)|19)),(0x2be,0)]
    assert programs[3869][469:478]==[(0x1ca,34702592),(0x511,(0,1347)),(0x540,0),
            (0x511,(0,(753<<16)|40)),(0x2be,0),(0x1ca,34702336),(0x89c,0),
            (0x511,(0,(753<<16)|40)),(0x86a,0)]
    assert programs[11613][43:50]==[(0x35e,1),(0x35e,3),(0x2be,0),
            (0x35e,2),(0x89c,0),(0x35e,3),(0x86a,0)]
    assert all(op==0x495 for op,a in programs[6449]+programs[6451]), 'Do not reuse empty animation helpers'
    assert (0x895,7791) in programs[11074] and (0xa0,0) in programs[11074] and (0x1a2,0) in programs[11074]
    assert (0x83c,0) in programs[10324] and (0x511,(0,-2147483644)) in programs[10324]
    report['assertions']='PASS: booth/Beasts/marketplace model contracts; empty helpers; scrolling; operation sentinel'
    executable=ROOT/'OpenNXT/data/clients/950/win64/original/rs2client.exe'
    report['clientSha256']=sha(executable.read_bytes())
    report['nativeHandlers']={
        'fullNpc':{'normalized':'0x02be','wrapper':'0x1401e34a0','body':'0x1401d2620','modelSourceKind':6},
        'sequence':{'normalized':'0x086a','wrapper':'0x1401e37a0','body':'0x1401d2a70','field':'0x224'},
        'genericModel':{'normalized':'0x0098','wrapper':'0x1401e2d80','body':'0x1401d1df0','modelSourceKind':1},
        'localPlayer':{'normalized':'0x0769','wrapper':'0x1401e36e0','body':'0x1401d2970','modelSourceKind':5}}
    args.output.write_bytes((json.dumps(report,indent=2)+'\n').encode('utf-8'))
    print(f"PASS: {len(report['interfaces'])} targeted interfaces, {len(report['components'])} headers, {len(SCRIPTS)} scripts. Static evidence only.")

if __name__=='__main__':
    main()
