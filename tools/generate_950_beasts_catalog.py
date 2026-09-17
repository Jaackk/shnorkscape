"""Read-only native753 catalog verification; --write regenerates the pinned resource."""
import sys,hashlib
from pathlib import Path
sys.dont_write_bytecode=True
ROOT=Path(__file__).resolve().parents[1]
import cache950 as c
bosses=c.enum(9031)
assert sorted(bosses)==list(range(len(bosses)))
out=['# Native753 browsing recipe derived from the paired950 cache.','catalog.version=1','bosses='+','.join(str(bosses[i]) for i in range(len(bosses)))]
keys={(3,753,i) for i in c.archive(3,753)}
keys.update((12,i,0) for i in [794,3371,3,3869,11074,10410,10899,16337,10428,10444,10407,12274,6732,5511])
keys.update((17,i>>8,i&255) for i in [9030,9031,13200])
keys.update((22,i>>5,i&31) for i in list(bosses.values())+[28556,24114,24115,24116,24117,24118,24113])
keys.add((3,1477,896))
for i,g,f in sorted(keys):out.append(f'pin.{i}/{g}/{f}='+hashlib.sha256(c.archive(i,g)[f]).hexdigest())
data=('\n'.join(out)+'\n').replace('\n','\r\n').encode('latin1')
p=ROOT/'Ataraxia950/resources/native950/beasts-950.properties'
if '--write' in sys.argv:
 if p.exists():
  b=ROOT/'implementation-backup/ribbon-navigation'/p.relative_to(ROOT);b.parent.mkdir(parents=True,exist_ok=True)
  if not b.exists():b.write_bytes(p.read_bytes())
 p.write_bytes(data)
else:
 assert p.read_bytes()==data,'The derived boss catalog changed'
print('Verified',len(bosses),'boss rows and',len(keys),'cache pins')
