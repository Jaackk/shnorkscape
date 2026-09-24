"""Author server-driven pages in the existing 950 management shell.

Uses CS10410/10899 buttons, CS2995 text and the normal management lifecycle.
Replaces the installed helpers and adds a scoped acknowledgement to native8286.
Writes only a staged cache delta; never the client executable or live cache.
"""
import hashlib, json, struct, zlib
from pathlib import Path
from build_library_search_bridge import ROOT, encode, unpack, container
from library_trace_read import scope, inverse

# Native-only opcode: independently derived from registration1400562be and
# handler1401f76f0 (pop hash/slot, resolve component, cancel prior, select target).
NATIVE_SELECT=0x10000
def instruction(op,arg):
    return struct.pack('>HB',0x72c,arg) if op==NATIVE_SELECT else encode(op,arg)

MARKER_COMPONENT = 1448 << 16 | 14  # Native type4 text, under the hidden loading panel.
BASE = 21124  # first unused ID in the paired 950 cache; checked below
def push(value): return (0x511, (2 if isinstance(value,str) else 0,value))
def call(sid): return (0x895,sid)
def ints(*values): return [push(v) for v in values]
def script(ops, ni=0, ns=0):
    ops=ops+[(0x495,0)]
    tail=struct.pack('>I6H',len(ops),ni,ns,0,ni,ns,0)+b'\0\0\1'
    raw=b'\0'+b''.join(instruction(*op) for op in ops)+tail
    decoded,_=scope['decode'](raw,dict(inverse,**{})|{0x72c:NATIVE_SELECT})
    assert [(o,a) for _,_,o,a,_ in decoded]==ops
    return raw
def programs():
    init=[]
    for c in (3,5,7,9,11):
        h=1448<<16|c
        init+=ints(h)+[(0x5,0)]+ints(0,0,0,0,h)+[(0x7c5,0)]+ints(742,450,0,0,h)+[(0x55,0)]
        init+=ints(0 if c in (3,5,7) else 1,h)+[(0xe4,0)]
    # CS8289 resolves the native title host from struct21301/3506 and uses
    # dynamic child14 (child3 is a frame component). 713 is NOT the title: putting text there clips under tabs.
    init+=ints(1,1477<<16|714)+[(0xe4,0)]
    init+=ints(1477<<16|713)+[(0x5,0)]
    title=[push('DEVELOPER CONSOLE'),(0x1f1,0)]
    # Match CS8289's text measurement and frame height so the longer title fits.
    title+=ints(21301,0)+[call(8418)]+ints(3557)+[(0x540,0),push('DEVELOPER CONSOLE')]+ints(60)+[(0x23a,0),(0x1f9,0)]
    title+=ints(21301,0)+[call(8418)]+ints(3548)+[(0x540,0)]+ints(0,0)+[(0x8c3,0)]
    init+=ints(21301,3506)+[(0x540,0)]+ints(14)+[(0x109,0)]+ints(1)+[(0x412,len(title))]+title
    # Native type3 outline components, using the same size/position/colour
    # setters as CS6204. Keep the native textured frame behind all three panes.
    for child,(x,y,w,h) in enumerate(((0,73,162,313),(169,73,314,305),(491,38,247,378))):
        init+=ints(1448<<16|3,3,child)+[(0x691,0)]+ints(w,h,0,0)+[(0x8c3,0)]+ints(x,y,0,0)+[(0x828,0)]+ints(0x6d5b38)+[(0x1a5,0)]
    # Button arguments: actor, x,y,width,height,selected,text. Native nine-slice
    # visuals live in host3; operation actors in host5, as in the Beasts browser.
    button=ints(1448<<16|3,1448<<16|5,28556)
    button += [(0x35e,n) for n in (1,2,3,4,0,5)]+[(0x25a,0),call(10410),(0x592,0)]
    # Same CC_SETONOP contract as native CS10324; callback carries a per-render
    # capability, so delayed input can never execute a new row at an old slot.
    # Native CS10324 uses CC_SETOP083c and event_op=-2147483644.
    button += [(0x35e,6)]+ints(1)+[(0x412,12)]
    for number,label in ((1,'Open Details'),(2,'Execute Now'),(3,'Add/Remove Favourite'),(4,'Open Details')):
        button += ints(number)+[push(label),(0x83c,0)]
    button += ints(BASE+3,-2147483644)+[(0x25a,1),push('is'),(0x6a,0)]
    notify=[]
    for operation in (2,3,4):
        notify += [(0x35e,0)]+ints(operation)+[(0x412,5),(0x25a,0),push(':'+str(operation)),(0x267,2),(0x77b,0),(0x495,0)]
    notify += [(0x25a,0),(0x77b,0)]
    # CS10410 returns the next actor index; the caller has its own actor allocation.
    text=ints(1448<<16|7)+[(0x35e,0),(0x35e,1),(0x35e,2)]+ints(0,0)
    text += [(0x35e,3),(0x35e,4)]+ints(0,0)+[(0x35e,5),(0x25a,0),call(2995)]
    # Headings reuse CS8289's native serif font; body text retains CS2995 style.
    text += [(0x35e,5)]+ints(17514)+[(0x412,4)]+ints(60)+[(0x742,0)]+ints(0xffd479)+[(0x1a5,0)]
    # A distinct source slot per placement rejects late opcode85 packets from old
    # selections. CC_CREATE requires contiguous preceding children; they are hidden.
    arm=ints(1448<<16|11)+[(0x5,0)]+ints(0)+[(0x592,1)]
    start=len(arm);arm += [(0x35e,1),(0x35e,0),(0x647,0)];branch=len(arm)-1
    arm+=ints(1448<<16|11,3)+[(0x35e,1),(0x691,0)]+ints(1)+[(0x4a9,0),(0x35e,1)]+ints(1)+[(0x1d,0),(0x592,1)]
    arm += [(0x713,start-len(arm)-1)];arm[branch]=(0x647,len(arm)-branch-1)
    arm+=ints(1448<<16|11,4)+[(0x35e,0),(0x691,0),(0x25a,0),(0x776,0)]
    # Native selection enters event15, leaves event16. The normalized665 handler
    # at1401fa2b0 installs event16 through1401f84d0, matching14019f66d.
    arm+=ints(BASE+3,0)+[(0x25a,1),push('is'),(0x665,0)]
    arm+=ints(1448<<16|11)+[(0x35e,0),(NATIVE_SELECT,0)]
    # Two int locals, only one argument (the target actor index).
    armraw=bytearray(script(arm,2,2));end=len(armraw)-19;armraw[end+10:end+12]=struct.pack('>H',1)
    return {BASE:script(init),BASE+1:script(button,7,2),BASE+2:script(text,6,1),BASE+3:script(notify,1,1),BASE+4:bytes(armraw),BASE+5:script([(0x8aa,0)]),BASE+6:script(ints(MARKER_COMPONENT)+[(0x5a2,0)]+ints(1)+[(0x412,2),(0x25a,0),(0x1f1,0)],0,1),8286:ready_bridge()}

def ready_bridge():
    """Acknowledge the REAL varc2911 management refresh, after its native work.

    The marker must be text-capable: root1477:713 is a container and its
    native IF_GETTEXT is empty. CS21130 sets hidden text1448:14 synchronously
    before varc2911 changes; close clears it before unmounting the shell.
    """
    raw=unpack((ROOT/'cache/12/8286.dat').read_bytes())
    ops,tail=scope['decode'](raw,inverse)
    old=[(o,a) for _,_,o,a,_ in ops[:19]]+[(0x495,0)]
    original=b'\0'+b''.join(instruction(*op) for op in old)+struct.pack('>I',20)+tail[4:]
    assert hashlib.sha256(original).hexdigest()=='5bd6296bb761633d83eb47a86757815ee88d0369ab844439a11f13792f24d5f7', 'Unexpected native8286 body'
    # IF_FIND protects ordinary management when the developer shell is absent.
    extra=ints(MARKER_COMPONENT)+[(0x5a2,0)]+ints(1)+[(0x412,8)]
    extra+=ints(MARKER_COMPONENT)+[(0x8b9,0),push('SHNORKSCAPE Developer Console'),(0x3f,0)]+ints(0)+[(0x412,2),push('__devready'),(0x77b,0)]
    updated=old[:-1]+extra+[old[-1]]
    return raw[:raw.index(0)+1]+b''.join(instruction(*op) for op in updated)+struct.pack('>I',len(updated))+tail[4:]

def smart(v): return struct.pack('>I',v|0x80000000) if v>=32768 else struct.pack('>H',v)
def append_reference(raw, additions):
    """Replace audited scripts while retaining all unrelated group/file metadata."""
    assert raw[0]==7 and raw[5]==13
    p=6
    def count():
        nonlocal p
        n=4 if raw[p]&128 else 2;v=int.from_bytes(raw[p:p+n],'big')&0x7fffffff;p+=n;return v
    total=count();ids=[];last=0
    for _ in range(total):last+=count();ids.append(last)
    assert all(sid in ids or sid==BASE+6 for sid in additions), 'Only the audited marker helper may be appended'
    arrays=[]
    for width in (4,4,4,8,4):arrays.append([raw[p+i*width:p+(i+1)*width] for i in range(total)]);p+=total*width
    counts=[count() for _ in ids];files=[]
    for n in counts:
        start=p
        for _ in range(n):count()
        files.append(raw[start:p])
    names=[]
    for n in counts:names.append(raw[p:p+4*n]);p+=4*n
    assert p==len(raw),(p,len(raw))
    for sid,(packed,payload) in sorted(additions.items()):
        if sid not in ids:
            assert sid>ids[-1]
            ids.append(sid)
            for arr in arrays:arr.append(b'\0'*(8 if arr is arrays[3] else 4))
            counts.append(1);files.append(smart(0));names.append(b'\0'*4)
        i=ids.index(sid)
        version=int.from_bytes(arrays[4][i],'big')+1
        for arr,value in zip(arrays[1:],(struct.pack('>I',zlib.crc32(packed)),struct.pack('>I',zlib.crc32(payload)),struct.pack('>II',len(packed),len(payload)),struct.pack('>I',version))):arr[i]=value
    out=raw[:1]+struct.pack('>I',int.from_bytes(raw[1:5],'big')+1)+raw[5:6]+smart(len(ids));last=0
    for sid in ids:out+=smart(sid-last);last=sid
    return out+b''.join(b''.join(a) for a in arrays)+b''.join(smart(n) for n in counts)+b''.join(files)+b''.join(names)

def main():
    dest=ROOT/'dist/developer-console-cache-ux-20260925/cache-v4';dest.mkdir(parents=True,exist_ok=True)
    additions={};pins={}
    for sid,payload in programs().items():
        packed=container(payload);additions[sid]=(packed,payload)
        source=ROOT/f'cache/12/{sid}.dat'
        before=source.read_bytes() if source.exists() else b'\0\0'
        version=(int.from_bytes(before[-2:],'big')+1)&65535
        path=dest/f'12/{sid}.dat';path.parent.mkdir(exist_ok=True);path.write_bytes(packed+struct.pack('>H',version))
        pins[str(sid)]=hashlib.sha256(payload).hexdigest()
    ref=append_reference(unpack((ROOT/'cache/255/12.dat').read_bytes()),additions)
    (dest/'255').mkdir(exist_ok=True);(dest/'255/12.dat').write_bytes(container(ref))
    (ROOT/'protocol-analysis/developer-console-scripts-950.json').write_bytes((json.dumps({'scripts':pins,'nativeButton':10410,'nativeText':2995,'shell':1448,'status':'AUTOMATED VERIFIED; Vulkan visual acceptance pending'},indent=2)+'\n').encode())
    for sid in (10410,10899,2995,10644,10324,8289,8418):
        pins[str(sid)]=hashlib.sha256((ROOT/f'temp/library-followup-trace/12-{sid}-0.bin').read_bytes()).hexdigest()
    (ROOT/'Ataraxia950/resources/native950/developer-console-950.properties').write_bytes((''.join(f'{sid}={value}\n' for sid,value in sorted(pins.items()))).encode())
    print('Staged seven developer helpers and scoped native-ready bridge; live cache untouched.')
if __name__=='__main__':main()

