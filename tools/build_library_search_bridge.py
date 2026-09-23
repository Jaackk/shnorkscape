"""Stage a narrowly scoped native bank query bridge; never edit the source cache.

The authored bank editor/layout remains intact. Only a library-labelled bank
emits prefixed STRING_DIALOGUE queries; ordinary banking executes the original
instructions. Reference CRCs, lengths and versions describe the new payloads.
"""
import gzip, hashlib, json, struct, zlib
from pathlib import Path
from library_trace_read import ROOT, read, scope, inverse

MARKER='Developer loadout:'
PREFIX='__devlib:'
DEST=ROOT/'dist/library-followup-20260923/cache-v4'
NAMES=['Melee - Best','Ranged - Best','Magic - Best','Necromancy - Best',
       'Melee - T80','Ranged - T80','Magic - T80','Tank / Defence','Boss Testing',
       'Developer Custom 1','Developer Custom 2','Developer Custom 3']
mapping={old:new for new,old in inverse.items()}
def encode(op,arg):
    out=struct.pack('>H',mapping[op])
    if op==0x511:
        kind,value=arg
        return out+bytes([kind])+(value.encode('cp1252')+b'\0' if kind==2 else int(value).to_bytes(8 if kind==1 else 4,'big',signed=True))
    width=4 if op in scope['FOUR'] or op in scope['THREE'] else 1
    return out+int(arg).to_bytes(width,'big',signed=width==4)
def guarded(body):
    # IF_GETTEXT on a real, static label provides a session-local discriminator.
    return [(0x511,(0,(517<<16)|73)),(0x8b9,0),(0x511,(2,MARKER)),(0x3f,0),
            (0x511,(0,0)),(0x647,1),(0x713,len(body))]+body
def notification(message,query=False):
    body=[(0x511,(2,PREFIX+message))]
    if query:body += [(0x1ca,34130432),(0x267,2)]
    return guarded(body+[(0x77b,0)])
def patch(sid):
    raw=(ROOT/f'temp/library-followup-trace/12-{sid}-0.bin').read_bytes()
    instructions,tail=read(sid);ops=[(o,a) for _,_,o,a,_ in instructions]
    # Insert before the normal changed-query filter; every old jump keeps its target.
    at=10 if sid==13905 else 28 if sid==13903 else 0
    if sid==15897:
        body=[]
        for i,name in enumerate(NAMES,1):body += [(0x35e,0),(0x511,(0,i)),(0x412,2),(0x511,(2,name)),(0x495,0)]
        extra=guarded(body+[(0x511,(2,'Unused developer slot')),(0x495,0)])
    elif sid==6963:
        extra=guarded([(0x35e,0),(0x511,(0,1)),(0x3f2,5),(0x35e,0),(0x511,(0,12)),(0xab,2),
                       (0x511,(0,0)),(0x495,0),(0x511,(0,3)),(0x495,0)])
    elif sid==13903:extra=notification('begin')+guarded([(0x511,(0,80)),(0x195,34127104)])
    else:extra=notification('q:',True) if sid==13905 else notification('cancel')
    jumps={0x713,0x647,0x412,0xab,0x454,0x73d,0x3f2}
    updated=[]
    for i,(op,arg) in enumerate(ops):
        if i==at:updated.extend(extra)
        if op in jumps:
            dest=i+1+arg
            new_i=i+(len(extra) if i>=at else 0)
            # A branch into the changed-query block must execute its inserted notification.
            new_dest=dest+(len(extra) if dest>at or at==0 and dest==0 else 0)
            arg=new_dest-new_i-1
        updated.append((op,arg))
    # These three programs have no switch tables. Relocating an unaudited switch is forbidden.
    assert at==0 or tail[16]==0,(sid,'unexpected switches')
    newtail=struct.pack('>I',len(updated))+tail[4:]
    result=raw[:raw.index(0)+1]+b''.join(encode(*op) for op in updated)+newtail
    decoded,_=scope['decode'](result,inverse)
    assert [(o,a) for _,_,o,a,_ in decoded]==updated
    return result
def unpack(raw):
    length=int.from_bytes(raw[1:5],'big')
    assert raw[0] in (0,2)
    return raw[5:5+length] if raw[0]==0 else gzip.decompress(raw[9:9+length])
def container(raw):
    compressed=gzip.compress(raw,mtime=0)
    return b'\x02'+struct.pack('>II',len(compressed),len(raw))+compressed
def main():
    ref=bytearray(unpack((ROOT/'cache/255/12.dat').read_bytes()))
    assert ref[0]==7 and ref[5]==13,'Unexpected reference layout'
    pos=6
    def count():
        nonlocal pos
        width=4 if ref[pos]&128 else 2
        value=int.from_bytes(ref[pos:pos+width],'big')&0x7fffffff;pos+=width;return value
    total=count();ids=[];acc=0
    for _ in range(total):acc+=count();ids.append(acc)
    pos+=4*total # names
    crc_at=pos;pos+=4*total
    plain_crc_at=pos;pos+=4*total
    lengths_at=pos;pos+=8*total
    versions_at=pos
    pins={}
    for sid in (13903,13905,13909,15897,6963):
        raw=patch(sid);packed=container(raw);i=ids.index(sid)
        version=int.from_bytes(ref[versions_at+4*i:versions_at+4*i+4],'big')+1
        for at,value in [(crc_at+4*i,zlib.crc32(packed)),(plain_crc_at+4*i,zlib.crc32(raw)),
                         (lengths_at+8*i,len(packed)),(lengths_at+8*i+4,len(raw)),(versions_at+4*i,version)]:
            ref[at:at+4]=struct.pack('>I',value)
        path=DEST/f'12/{sid}.dat';path.parent.mkdir(parents=True,exist_ok=True)
        with path.open('xb') as out:out.write(packed+struct.pack('>H',version&65535))
        pins[f'12/{sid}/0']={'original':hashlib.sha256((ROOT/f'temp/library-followup-trace/12-{sid}-0.bin').read_bytes()).hexdigest(), 'patched':hashlib.sha256(raw).hexdigest()}
    ref[1:5]=struct.pack('>I',int.from_bytes(ref[1:5],'big')+1)
    path=DEST/'255/12.dat';path.parent.mkdir(parents=True,exist_ok=True)
    with path.open('xb') as out:out.write(container(ref))
    (ROOT/'protocol-analysis/library-search-bridge-950.json').write_text(json.dumps(pins,indent=2)+'\n')
    print('Staged five guarded native bank scripts and their reference table; source cache unchanged.')
if __name__=='__main__':main()
