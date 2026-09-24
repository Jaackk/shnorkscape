"""Stage a bounded CS5591 dropdown notification bridge, preserving its native UI.

Uses the same IF_FIND + CC_OP(1) contract as exact950 CS10450/10451.
Never writes the live cache. The predecessor reference table is retained.
"""
import hashlib,json,struct,zlib
from pathlib import Path
from library_trace_read import ROOT,read,scope,inverse
from build_library_search_bridge import encode,unpack,container

DEST=ROOT/'dist/predev-cleanup-cache-20260924/cache-v4'
ROWS=[10496]+[10756+i*4+k for i in range(18) for k in range(3)]

def prefix():
    ops=[];labels={};fixups=[]
    def emit(op,arg=0):ops.append((op,arg))
    def push(n):emit(0x511,(0,n))
    def jump(op,label):fixups.append((len(ops),label));emit(op,0)
    emit(0x35e,0);push((365<<16)|19);jump(0x412,'original')
    emit(0x35e,3);push(0);jump(0x412,'original')
    emit(0x35e,2);push(0);jump(0x3f2,'original')
    emit(0x35e,2);push(18);jump(0xab,'original')
    for row in ROWS:
        emit(0x35e,1);push(row);jump(0x647,'notify')
    jump(0x713,'original')
    labels['notify']=len(ops)
    push((365<<16)|20);emit(0x0005) # CC_DELETEALL, same value actor owner as sliders.
    for value in range(19):
        push((365<<16)|20);push(4);push(value);emit(0x691)
        for n in (1,1,0,0):push(n)
        emit(0x8c3)
        for n in (-1,-1,0,0):push(n)
        emit(0x828)
        push(1);emit(0x511,(2,'Select'));emit(0x83c)
    emit(0x35e,0);emit(0x35e,1);emit(0x109);push(1);jump(0x412,'original')
    push(1);emit(0x3e8) # Notify selected row before selected value.
    push((365<<16)|20);emit(0x35e,2);emit(0x109);push(1);jump(0x412,'original')
    push(1);emit(0x3e8)
    labels['original']=len(ops)
    for at,label in fixups:ops[at]=(ops[at][0],labels[label]-at-1)
    return ops

def patch():
    raw=(ROOT/'temp/library-followup-trace/12-5591-0.bin').read_bytes()
    assert hashlib.sha256(raw).hexdigest()=='cc3f4e0a3c9e3a1c278bfc67aa9c893bca2963c4dc09cfd67110de058e0e2cb3','Changed950 dropdown callback'
    ins,tail=read(5591);old=[(op,arg) for _,_,op,arg,_ in ins]
    assert old[:3]==[(0x35e,0),(0x51a,0),(0x713,25)]
    ops=prefix()+old
    result=raw[:raw.index(0)+1]+b''.join(encode(*o) for o in ops)+struct.pack('>I',len(ops))+tail[4:]
    assert [(o,a) for _,_,o,a,_ in scope['decode'](result,inverse)[0]]==ops
    return raw,result

def main():
    raw,patched=patch()
    manifest=json.loads((ROOT/'protocol-analysis/playability-candidate-20260923.json').read_bytes())
    previous=ROOT/'dist'/manifest['candidate']/'cache-v4/255/12.dat'
    ref=bytearray(unpack(previous.read_bytes()));assert ref[0]==7 and ref[5]==13
    pos=6
    def count():
        nonlocal pos
        width=4 if ref[pos]&128 else 2
        value=int.from_bytes(ref[pos:pos+width],'big')&0x7fffffff;pos+=width;return value
    total=count();ids=[];acc=0
    for _ in range(total):acc+=count();ids.append(acc)
    pos+=4*total;crc=pos;pos+=4*total;plain=pos;pos+=4*total;length=pos;pos+=8*total;versions=pos
    i=ids.index(5591);version=int.from_bytes(ref[versions+4*i:versions+4*i+4],'big')+1;packed=container(patched)
    for at,value in [(crc+4*i,zlib.crc32(packed)),(plain+4*i,zlib.crc32(patched)),(length+8*i,len(packed)),(length+8*i+4,len(patched)),(versions+4*i,version)]:ref[at:at+4]=struct.pack('>I',value)
    ref[1:5]=struct.pack('>I',int.from_bytes(ref[1:5],'big')+1)
    for name,data in [('12/5591.dat',packed+struct.pack('>H',version&65535)),('255/12.dat',container(ref))]:
        path=DEST/name;path.parent.mkdir(parents=True,exist_ok=True)
        with path.open('xb') as out:out.write(data)
    report={'script':5591,'original':hashlib.sha256(raw).hexdigest(),'patched':hashlib.sha256(patched).hexdigest(),'rows':ROWS,'notification':'365:19 row then 365:20 value; original callback retained','status':'LIVE TEST PENDING'}
    (ROOT/'protocol-analysis/combat-settings-bridge-950.json').write_bytes((json.dumps(report,indent=2)+'\n').encode())
    print('Staged CS5591 and cumulative reference table; live cache unchanged.')

if __name__=='__main__':main()
