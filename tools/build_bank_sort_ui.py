"""Stage a minimal exact950 bank plus-button tooltip/operation patch; never modify live cache."""
import hashlib,json,struct,zlib
from pathlib import Path
from library_trace_read import ROOT,read,scope,inverse
from build_library_search_bridge import encode,unpack,container
DEST=ROOT/'dist/bank-sort-20260923/cache-v4'
def main():
    raw=(ROOT/'temp/library-followup-trace/12-13830-0.bin').read_bytes()
    ins,tail=read(13830);ops=[(o,a) for _,_,o,a,_ in ins]
    assert ops[255]==(0x25a,0) and ops[261]==(0x495,0) and tail[16]==0
    # Preserve the capacity display. Its plus now always has a sort operation and native hover.
    ops[255]=(0x511,(2,'Click to sort this tab'))
    ops[-1:]=[(0x511,(0,0)),(0x511,(0,(517<<16)|250)),(0xe4,0),
              (0x511,(0,1)),(0x511,(0,(517<<16)|253)),(0xe4,0),
              (0x511,(0,1)),(0x511,(2,'Click to sort this tab')),(0x511,(0,(517<<16)|250)),(0x113,0),(0x495,0)]
    patched=raw[:raw.index(0)+1]+b''.join(encode(*o) for o in ops)+struct.pack('>I',len(ops))+tail[4:]
    decoded,_=scope['decode'](patched,inverse)
    assert [(o,a) for _,_,o,a,_ in decoded]==ops
    assert ops[:255]==[(o,a) for _,_,o,a,_ in ins][:255]
    ref=bytearray(unpack((ROOT/'cache/255/12.dat').read_bytes()))
    assert ref[0]==7 and ref[5]==13
    pos=6
    def count():
        nonlocal pos
        width=4 if ref[pos]&128 else 2
        value=int.from_bytes(ref[pos:pos+width],'big')&0x7fffffff;pos+=width;return value
    total=count();ids=[];acc=0
    for _ in range(total):acc+=count();ids.append(acc)
    pos+=4*total;crc_at=pos;pos+=4*total;plain_at=pos;pos+=4*total;length_at=pos;pos+=8*total;version_at=pos
    i=ids.index(13830);version=int.from_bytes(ref[version_at+4*i:version_at+4*i+4],'big')+1
    packed=container(patched)
    for at,value in [(crc_at+4*i,zlib.crc32(packed)),(plain_at+4*i,zlib.crc32(patched)),(length_at+8*i,len(packed)),(length_at+8*i+4,len(patched)),(version_at+4*i,version)]:ref[at:at+4]=struct.pack('>I',value)
    ref[1:5]=struct.pack('>I',int.from_bytes(ref[1:5],'big')+1)
    for name,data in [('12/13830.dat',packed+struct.pack('>H',version&65535)),('255/12.dat',container(ref))]:
        p=DEST/name;p.parent.mkdir(parents=True,exist_ok=True)
        with p.open('xb') as out:out.write(data)
    report={'script':13830,'component':'517:250','original':hashlib.sha256(raw).hexdigest(),'patched':hashlib.sha256(patched).hexdigest(),'reference':hashlib.sha256((DEST/'255/12.dat').read_bytes()).hexdigest().upper()}
    (ROOT/'protocol-analysis/bank-sort-ui-950.json').write_text(json.dumps(report,indent=2)+'\n')
    print(json.dumps(report))
if __name__=='__main__':main()
