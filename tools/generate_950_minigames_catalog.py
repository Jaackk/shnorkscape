"""Verify paired950 activity browser data; --write regenerates the derived resource."""
import sys,hashlib,json
from pathlib import Path
sys.dont_write_bytecode=True
ROOT=Path(__file__).resolve().parents[1]
import cache950 as c
from verify_950_ui_scripts import decode
from verify_950_lodestones import switches
m=json.loads((ROOT/'protocol-analysis/ui-scripts-950-evidence.json').read_text())['opcodeMap947to950']
inv={int(v['opcode950'],16):int(k,16) for k,v in m.items()}
ins,tail=decode(c.archive(12,9178)[0],inv)
fav={key:ins[2+offset][3]>>8 for key,offset in switches(tail)[0].items() if ins[2+offset][2]==0x30}
out=['# Current1344 activity enums and native favourite bits.','catalog.version=1']
structs=set();bits={20794};enums=[6452,6452,8014,8015,8016,8017]
for category,eid in enumerate(enums):
 data=c.enum(eid);assert sorted(data)==list(range(1,len(data)+1));out.append(f'category.{category}='+','.join(str(data[i]) for i in range(1,len(data)+1)));structs.update(data.values())
for sid in sorted(structs):
 activity=c.params(sid)[1268];bit=fav.get(activity,-1);assert bit>=0
 out.append(f'favorite.{sid}={bit}');bits.add(bit)
keys={(3,1344,i) for i in c.archive(3,1344)}
keys.update((12,i,0) for i in [6743,6744,6745,6746,6747,6748,9174,9175,9177,9178,9179,9138,9139,9132,9180,9181,9182,18246,18243,18244,10428,10444,3224])
keys.update((17,i>>8,i&255) for i in set(enums+[6156,10016]))
keys.update((22,i>>5,i&31) for i in structs);keys.update((2,69,b) for b in bits);keys.add((3,1477,896))
for i,g,f in sorted(keys):out.append(f'pin.{i}/{g}/{f}='+hashlib.sha256(c.archive(i,g)[f]).hexdigest())
data=('\n'.join(out)+'\n').replace('\n','\r\n').encode('latin1');p=ROOT/'Ataraxia950/resources/native950/minigames-950.properties'
if '--write' in sys.argv:
 if p.exists():
  b=ROOT/'implementation-backup/ribbon-navigation'/p.relative_to(ROOT);b.parent.mkdir(parents=True,exist_ok=True)
  if not b.exists():b.write_bytes(p.read_bytes())
 p.write_bytes(data)
else:assert p.read_bytes()==data,'Derived activity catalog changed'
print('Verified',len(structs),'activities,6 filters,',len(keys),'pins')
