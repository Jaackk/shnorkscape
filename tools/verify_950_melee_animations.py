import sys,hashlib,json
from pathlib import Path
sys.dont_write_bytecode=True
import verify_950_hitbars as v
IDS=[422,425,836,5387,5388,5389,6182,6183,6184,37378,37385,18292]
def seq(data):
 r=v.cache.Reader(data);ops={};frames=[];duration=0
 while True:
  op=r.integer(1)
  if op==0:break
  start=r.pos
  if op==1:
   n=r.integer(2);ds=[r.integer(2) for _ in range(n)];ls=[r.integer(2) for _ in range(n)];hs=[r.integer(2) for _ in range(n)];frames=list(zip(hs,ls));duration=sum(ds)
  elif op in (2,6,7,23,24,25):r.pos+=2
  elif op in (5,8,9,10,11,22,27):r.pos+=1
  elif op in (14,15,16,18):pass
  elif op==26:r.integer(2);duration+=r.integer(2)
  elif op==13:
   for _ in range(r.integer(2)):
    n=r.integer(1)
    if n:r.pos+=3+2*(n-1)
  elif op==119:r.pos+=3
  elif op==120:r.pos+=6
  elif op==19:r.pos+=2
  elif op==20:r.pos+=5
  elif op==249:
   for _ in range(r.integer(1)):
    t=r.integer(1);r.integer(3)
    if t:r.string()
    else:r.integer(4)
  else:raise ValueError((op,r.pos))
  ops[op]=data[start:r.pos].hex()
 assert r.pos==len(data)
 return {'opcodes':ops,'frames':frames,'durationCycles':duration}
def main():
 old=v.Packed(v.ROOT.parent/'Ataraxia-PS/data/cache');records=[];pins=[]
 for i in IDS:
  data=v.cache.archive(20,i>>7)[i&127];parsed=seq(data);sha=hashlib.sha256(data).hexdigest();row={'id':i,'950sha256':sha,'950':parsed};pins.append(dict(index=20,group=i>>7,file=i&127,sha256=sha))
  if i>>7 in old.reference(20) and i&127 in old.reference(20)[i>>7]:
   a=old.archive(20,i>>7)[i&127];d=seq(a);row.update({'910sha256':hashlib.sha256(a).hexdigest(),'sameDefinition':a==data,'sameFramesAndDurations':d['opcodes'].get(1)==parsed['opcodes'].get(1),'910':d})
  else:row['910']='absent'
  records.append(row)
 for idx,ids,shift in [(19,[1277,1291,1321],8),(22,[14922,14923,14924],5)]:
  for i in ids:
   data=v.cache.archive(idx,i>>shift)[i&((1<<shift)-1)];pins.append(dict(index=idx,group=i>>shift,file=i&((1<<shift)-1),sha256=hashlib.sha256(data).hexdigest()))
 result={'sequences':records,'pins':pins,'limitation':'NXT cache lacks legacy indexes0/7; matching sequence references do not prove unchanged rendered frame/model dependencies.'}
 if len(sys.argv)>1:Path(sys.argv[1]).write_text(json.dumps(result,indent=2)+'\n',encoding='utf-8')
 for row in records:print(row['id'],row['950']['durationCycles'],row.get('sameDefinition'),row.get('sameFramesAndDurations'))
if __name__=='__main__':main()
