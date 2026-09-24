"""Build narrowly audited companion interaction / Entropic passive cache delta offline."""
import bz2,gzip,hashlib,json,struct,zlib
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]

def unpack(data):
 kind=data[0];length=int.from_bytes(data[1:5],'big')
 if kind==0:return data[5:5+length]
 raw=data[9:9+length]
 result=bz2.decompress(b'BZh1'+raw) if kind==1 else gzip.decompress(raw)
 assert len(result)==int.from_bytes(data[5:9],'big')
 return result

def pack(raw):
 data=gzip.compress(bytes(raw),mtime=0)
 return b'\2'+struct.pack('>II',len(data),len(raw))+data

def build(source,destination):
 report=[]
 for index,archive,changes in [(18,236,{57:'examine',58:'examine',59:'examine'}),(18,243,{38:'examine'}),(19,239,{171:'souls'})]:
  files={int(p.stem.split('-')[2]):p.read_bytes() for p in source.glob(f'{index}-{archive}-*.bin')}
  assert files
  for file,kind in changes.items():
   original=raw=files[file]
   if kind=='examine':
    assert raw[0]==127 and raw[3]==107 and raw[4]==253
    raw=raw[:3]+raw[4:] # Remove only native no-interaction opcode; no Attack option added.
   else:
    assert raw[85]==249 and raw[86]==14 and raw[199]==144
    param=b'\0'+(8928).to_bytes(3,'big')+(48397).to_bytes(4,'big')
    raw=raw[:86]+bytes([15])+raw[87:199]+param+raw[199:]
   files[file]=raw
   report.append(dict(index=index,archive=archive,file=file,reason=kind,before=hashlib.sha256(original).hexdigest(),after=hashlib.sha256(raw).hexdigest()))
  # One JS5 chunk, preserve every file ID, order and untouched file verbatim.
  data=b''.join(files[f] for f in sorted(files));last=0
  for f in sorted(files):
   size=len(files[f]);data+=struct.pack('>i',size-last);last=size
  data+=b'\1'
  refpath=destination/'255'/f'{index}.dat'
  ref=bytearray(unpack((refpath if refpath.exists() else ROOT/'cache'/'255'/f'{index}.dat').read_bytes()))
  assert ref[0]==7 and ref[5] in (12,13)
  pos=6
  def smart():
   nonlocal pos
   size=4 if ref[pos]&128 else 2;v=int.from_bytes(ref[pos:pos+size],'big')&0x7fffffff;pos+=size;return v
  count=smart();ids=[];acc=0
  for _ in range(count):acc+=smart();ids.append(acc)
  if ref[5]&1:pos+=4*count
  crc=pos;pos+=4*count;plain=pos;pos+=4*count;lengths=pos;pos+=8*count;versions=pos
  i=ids.index(archive);version=int.from_bytes(ref[versions+4*i:versions+4*i+4],'big')+1;packed=pack(data)
  for offset,value in [(crc+4*i,zlib.crc32(packed)),(plain+4*i,zlib.crc32(data)),(lengths+8*i,len(packed)),(lengths+8*i+4,len(data)),(versions+4*i,version)]:ref[offset:offset+4]=struct.pack('>I',value)
  ref[1:5]=struct.pack('>I',int.from_bytes(ref[1:5],'big')+1)
  for p,content in [(destination/str(index)/f'{archive}.dat',packed+struct.pack('>H',version&65535)),(refpath,pack(ref))]:
   p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(content)
 return report

if __name__=='__main__':
 import argparse
 parser=argparse.ArgumentParser();parser.add_argument('export');parser.add_argument('destination');args=parser.parse_args()
 destination=Path(args.destination)
 if destination.exists():raise SystemExit('Use a new destination; existing artifacts are not overwritten.')
 print(json.dumps(build(Path(args.export),destination),indent=2))
