"""Reproduce950 lodestone UI data using exact cache CS2 switches and enum5726.
Writes derived evidence and runtime pins, never modifies the cache.
"""
import argparse,hashlib,json,sys
from pathlib import Path
sys.dont_write_bytecode=True
import cache950 as cache
import verify_950_ui_scripts as cs2
ROOT=Path(__file__).resolve().parents[1]
MAPPING=json.loads((ROOT/'protocol-analysis/ui-scripts-950-evidence.json').read_text())['opcodeMap947to950']
INVERSE={int(v['opcode950'],16):int(k,16) for k,v in MAPPING.items()}


# name -> (arrival x, arrival y); plane always follows the stone. See the comment at the use site.
ARRIVAL_OVERRIDES = {'City of Um': (1084, 1768)}

def script(sid):return cs2.decode(cache.archive(12,sid)[0],INVERSE)
def switches(tail):
    p=17;tables=[]
    for _ in range(tail[16]):
        n=int.from_bytes(tail[p:p+2],'big');p+=2;table={}
        for _ in range(n):
            key=int.from_bytes(tail[p:p+4],'big',signed=True);target=int.from_bytes(tail[p+4:p+8],'big',signed=True);p+=8
            table[key]=target
        tables.append(table)
    assert p==len(tail)-2
    return tables

def collect():
    ins,tail=script(14999)
    assert ins[49][2:4]==(0x51a,0)
    starts={key:50+offset for key,offset in switches(tail)[0].items()}
    names=cache.enum(5726)
    unlocks,unlocktail=script(13702)
    assert unlocks[1][2:4]==(0x51a,0)
    network={}
    for number,offset in switches(unlocktail)[0].items():
        start=2+offset
        if unlocks[start][2]==0x30:network[unlocks[start][3]>>8]=number
    rows=[]
    for widget,start in sorted(starts.items()):
        component=widget&65535
        if component>35 and component!=41:continue
        end=min([v for v in starts.values() if v>start]+[604])
        body=ins[start:end]
        coordinates=[arg[1] for p,q,op,arg,raw in body if op==0x511 and arg[0]==0 and arg[1] in names]
        assert len(set(coordinates))==1
        coordinate=coordinates[0]
        bits=[arg>>8 for p,q,op,arg,raw in body if op==0x30]
        assert bits
        value=15 if component==8 else 190 if component==9 else 1
        anchor_x,anchor_y,anchor_plane=(coordinate>>14)&16383,coordinate&16383,coordinate>>28
        # The original910 convention lands the player one tile south of the stone, and index5
        # confirms that tile is clear for 28 of the 29. City of Um is walled: group3472 marks
        # the whole ring around1083,1768,1 solid except the east tile, so that row lands east.
        # Keep this table tiny and evidence-backed - do not add a row without checking the map.
        arrival_x,arrival_y=ARRIVAL_OVERRIDES.get(names[coordinate],(anchor_x,anchor_y-1))
        row=dict(component=component,name=names[coordinate],packedAnchor=coordinate,networkId=network[bits[0]],unlockVarbit=bits[0],unlockValue=value,
                 anchor=dict(x=anchor_x,y=anchor_y,plane=anchor_plane),
                 arrival=dict(x=arrival_x,y=arrival_y,plane=anchor_plane),
                 tooltipScript=14999,tooltipStartInstruction=start)
        rows.append(row)
    assert len(rows)==29 and set(r['component'] for r in rows)==set(range(8,36))|{41}
    assert cache.enum(7716)[1007]==21304
    assert cache.params(21304)[3505]==(1477<<16)|735
    assert cache.params(21304)[3503]==(1477<<16)|732
    # Exact950 IF3 headers independently establish both clipping ancestors and
    # the panel's dimensions. CS11145 changes size without moving or rebuilding it.
    from inspect_native947_interface_slots import Reader
    def dimensions(iface,component):
        r=Reader(cache.archive(3,iface)[component])
        assert r.integer(1)==11
        kind=r.integer(1)
        if kind&128:r.string()
        assert kind&127==0
        r.integer(2);r.integer(2);r.integer(2)
        return dict(width=r.integer(2),height=r.integer(2),widthMode=r.integer(1),heightMode=r.integer(1))
    panel=dimensions(1092,1);host=dimensions(1477,735);wrapper=dimensions(1477,732)
    assert panel==dict(width=576,height=360,widthMode=0,heightMode=0)
    assert host==dict(width=512,height=336,widthMode=0,heightMode=0)
    assert wrapper==dict(width=512,height=352,widthMode=0,heightMode=0)
    size_program,size_tail=script(11145)
    assert cs2.normalize(size_program)==[(0x35e,i) for i in range(5)]+[(0x55,0),(0x495,0)]
    assert [int.from_bytes(size_tail[n:n+2],'big') for n in (10,12,14)]==[5,0,0]
    layout=dict(panel=panel,host=host,wrapper=wrapper,sizeScript=11145,restoreHostScript=8389,
                openHost=dict(width=576,height=360),openWrapper=dict(width=576,height=376),
                reason='Preserve16px wrapper padding; default central host clips576x360 lodestone interface.')
    bindings={(3,1092,f) for f in cache.archive(3,1092)}
    bindings.update((3,1477,f) for f in (732,733,734,735,736))
    bindings.add((3,1465,34))
    bindings.update((17,e>>8,e&255) for e in (5726,16805,13483,14305,7716))
    bindings.update((22,s>>5,s&31) for s in (21304,21218,21262))
    script_ids=(6001,6002,6003,6004,14999,13702,11673,11674,11675,11676,5841,1026,1027,1364,8420,8421,8841,1187,10416,11145,8389,8391)
    bindings.update((12,s,0) for s in script_ids)
    extra_bits=(41,23198,36140,28634,28635)
    bindings.update((2,69,b) for b in set(r['unlockVarbit'] for r in rows)|set(extra_bits))
    pins=[dict(index=i,group=g,file=f,sha256=hashlib.sha256(cache.archive(i,g)[f]).hexdigest()) for i,g,f in sorted(bindings)]
    varbits={str(b):cache.archive(2,69)[b].hex() for _,_,b in sorted(x for x in bindings if x[:2]==(2,69))}
    result=dict(sourceCache=str(cache.CACHE),revision=950,interface=1092,componentCount=68,homeInterface=1465,homeComponent=34,host=735,wrapper=732,closeComponent=66,
                coordinates='Actual950 tooltip CS14999 switch keys map interface hashes to enum5726 coordinate keys. Arrivals use original910 HomeTeleport south-of-stone convention; runtime collision validation is required.',
                layout=layout,destinations=rows,hiddenUnsupportedComponents=[36,37,38,39,40],
                controls=dict(quickDefaultVarbit=28634,quickChargesVarbit=28635,previousDestinationVarbit=41,quickToggleComponent=64,chargesComponent=61),
                extraClientPresentationUnlocks=[dict(varbit=23198,value=400,reason="Prifddinas tooltip Plagues End threshold"),dict(varbit=36140,value=100,reason='Menaphos tooltip Jack of Spades threshold'),dict(varp=2102,value=15,reason='Tirannwn tooltip Regicide threshold')],
                scriptIds=list(script_ids),varbitFiles=varbits,bindings=pins)
    return result

if __name__=='__main__':
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--write',action='store_true');args=parser.parse_args()
    result=collect()
    target=ROOT/'protocol-analysis/lodestones-950-evidence.json'
    runtime=ROOT/'Ataraxia950/resources/native950/lodestones-950.json'
    if args.write:
        target.write_text(json.dumps(result,indent=2)+'\n',encoding='utf-8')
        runtime.parent.mkdir(parents=True,exist_ok=True)
        runtime.write_text(json.dumps(result,indent=2)+'\n',encoding='utf-8')
    else:
        assert json.loads(target.read_text(encoding='utf-8'))==result,'950 lodestone evidence has changed'
        assert json.loads(runtime.read_text(encoding='utf-8'))==result,'950 lodestone runtime data differs from cache-derived evidence'
    print('PASS:29 cache-derived ordinarylodestones;',len(result['bindings']),'exactcachebindings')
    for row in result['destinations']:print(row['component'],row['name'],row['networkId'],row['packedAnchor'],row['arrival'])
