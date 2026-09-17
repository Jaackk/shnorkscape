#!/usr/bin/env python3
"""Read-only 910/947/950 standard hitbar and ordinary-melee hitmark comparison.
Only --output writes an evidence JSON. Packed cache files are opened strictly rb.
"""
import argparse, hashlib, json, sys
from pathlib import Path
sys.dont_write_bytecode = True
import cache950
import inspect_native947_interface_slots as cache
ROOT = Path(__file__).resolve().parents[1]

class Packed:
    def __init__(self, path): self.path=Path(path); self.refs={}
    def payload(self,index,group):
        with (self.path/f'main_file_cache.idx{index}').open('rb') as f:
            f.seek(group*6); entry=f.read(6)
        assert len(entry)==6
        length=int.from_bytes(entry[:3],'big'); sector=int.from_bytes(entry[3:],'big')
        out=bytearray(); chunk=0
        with (self.path/'main_file_cache.dat2').open('rb') as f:
            while len(out)<length:
                assert sector>0
                f.seek(sector*520); data=f.read(520)
                header=10 if group>65535 else 8; gw=header-6
                assert int.from_bytes(data[:gw],'big')==group
                assert int.from_bytes(data[gw:gw+2],'big')==chunk and data[header-1]==index
                sector=int.from_bytes(data[gw+2:gw+5],'big')
                out.extend(data[header:header+min(520-header,length-len(out))]);chunk+=1
        return cache.decode_container(bytes(out)).payload_bytes
    def reference(self,index):
        if index in self.refs:return self.refs[index]
        r=cache.Reader(self.payload(255,index));fmt=r.integer(1)
        if fmt>=6:r.integer()
        flags=r.integer(1);count=r.smart(fmt);ids=[];total=0
        for _ in range(count):total+=r.smart(fmt);ids.append(total)
        r.pos+=count*(4+(4 if flags&1 else 0)+(4 if flags&8 else 0)+(64 if flags&2 else 0)+(8 if flags&4 else 0)+4)
        counts=[r.smart(fmt) for _ in ids];files={}
        for group,n in zip(ids,counts):
            total=0;fids=[]
            for _ in range(n):total+=r.smart(fmt);fids.append(total)
            files[group]=fids
        assert r.pos==len(r.data)
        self.refs[index]=files;return files
    def archive(self,index,group):
        data=self.payload(index,group);ids=self.reference(index)[group]
        if len(ids)==1:return {ids[0]:data}
        chunks=data[-1];table=cache.Reader(data);table.pos=len(data)-1-chunks*len(ids)*4
        end=table.pos;out={key:bytearray() for key in ids};pos=0
        for _ in range(chunks):
            size=0
            for key in ids:
                size+=table.integer(signed=True);assert size>=0
                out[key].extend(data[pos:pos+size]);pos+=size
        assert pos==end
        return {key:bytes(value) for key,value in out.items()}

def hitmark(data):
    r=cache.Reader(data);d={'duration':70,'replacement':-1,'numerator':1,'denominator':1}
    def nullable(value,limit=65535):return -1 if value==limit else value
    def bigsmart():
        return (r.integer(4)&0x7fffffff) if r.data[r.pos]&128 else nullable(r.integer(2),32767)
    while True:
        op=r.integer(1)
        if op==0:break
        if op in (1,3,4,5,6):d[{1:'font',3:'sprite0',4:'sprite2',5:'sprite1',6:'sprite3'}[op]]=bigsmart()
        elif op==2:d['colour']=r.integer(3)
        elif op==8:
            assert r.integer(1)==0
            d['text']=r.string()
        elif op in (9,12):d['duration' if op==9 else 'replacement']=r.integer(2 if op==9 else 1)
        elif op in (7,10,13,19,20):d[{7:'offsetX',10:'offsetY',13:'textY',19:'numerator',20:'denominator'}[op]]=r.integer(2,signed=True)
        elif op==11:d['fade']=0
        elif op==14:d['fade']=r.integer(2)
        elif op==16:d['offsetPair']=[r.integer(2,signed=True),r.integer(2,signed=True)]
        elif op in (17,18,21,22):
            width=3 if op>=21 else 2
            d['varbit']=nullable(r.integer(width),(1<<(width*8))-1)
            d['varp']=nullable(r.integer(2))
            fallback=nullable(r.integer(2)) if op in (18,22) else -1
            count=r.integer(1)
            d['targets']=[nullable(r.integer(2)) for _ in range(count+1)]+[fallback]
        else:raise ValueError(('hitmark-opcode',op,r.pos))
    assert r.pos==len(data),(r.pos,len(data))
    return d

def describe(data):return {'bytes':len(data),'sha256':hashlib.sha256(data).hexdigest(),'hex':data.hex()}

def main():
    p=argparse.ArgumentParser();p.add_argument('--output',type=Path);args=p.parse_args()
    old=Packed(ROOT.parent/'Ataraxia-PS/data/cache')
    rows={'910':{72:old.archive(2,72),46:old.archive(2,46)}}
    for rev,path in [('947',ROOT.parent/'rs3cache/cache'),('950',ROOT/'cache')]:
        cache.FLAT=path;cache.archive.cache_clear();cache.reference.cache_clear()
        rows[rev]={g:cache.archive(2,g) for g in [72,46]}
    bars={str(i):{rev:describe(groups[72][i]) for rev,groups in rows.items()} for i in [0,3,4]}
    for i,versions in bars.items():assert len({r['sha256'] for r in versions.values()})==1
    ids={133,150,132,141,158}
    for groups in rows.values():
        for i in [133,150]:ids.update(t for t in hitmark(groups[46][i])['targets'] if t>=0)
    marks={}
    for i in sorted(ids):
        marks[str(i)]={rev:dict(describe(groups[46][i]),decoded=hitmark(groups[46][i])) for rev,groups in rows.items()}
    for i in [133,150]:assert marks[str(i)]['947']['decoded']==marks[str(i)]['950']['decoded']
    result={'scope':'Three standard HP bars and ordinary noncritical melee wrappers; no arbitrary hitmark admission.',
        'bars':bars,'hitmarks':marks,
        'nativeEvidence':{'barConfigTypeGlobal':'0x140c70480 = 72','barSink':'0x14037c6f0',
            'hitmarkConfigTypeGlobal':'0x140c70348 = 46','hitmarkSink':'0x14037c0d0',
            'hitmarkDecoder':'0x14033da40','morphResolver':'0x14033e3e0',
            'damageFormatter':'0x14033e1b0: wireDamage * field+b0 / field+b4'}}
    if args.output:args.output.write_text(json.dumps(result,indent=2)+'\n',encoding='utf-8')
    print('PASS: bars0/3/4 identical910/947/950; ordinary wrappers133/150 normalize identically947/950')
    for i in [133,150]:print('HITMARK',i,marks[str(i)]['950']['decoded'])
    for i in [0,14,106,116]:
        d=marks[str(i)]['950']['decoded'];print('DISPLAY_SCALE',i,d['numerator'],d['denominator'])
    print('EVIDENCE',str(args.output) if args.output else '(not written)')

if __name__=='__main__':main()
