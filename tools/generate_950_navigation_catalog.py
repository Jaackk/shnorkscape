import sys,hashlib
from pathlib import Path
sys.dont_write_bytecode=True
root=Path(__file__).resolve().parents[1];sys.path.insert(0,str(root/'tools'));import cache950 as c
out=['# Derived from paired950 enum7699 and its page structs; preserve native slot ordinals.','catalog.version=1','menus=0,1,2,3,4,8']
pins={};faces={1448,1218,1217,1466};bits={18994,18995,29607,18997,18998,18999,30609};scripts={441,8179,8180,8287,8288,8186,8187,8190,8193,8282,8283,8290,13415,12343,8677,8678,8680,8682,12090,9230,13835,13836,13843,13845,13846,13851,8468,8470,8471,8472}
def pin(i,g,f):pins[f'{i}/{g}/{f}']=hashlib.sha256(c.archive(i,g)[f]).hexdigest()
pin(17,13320>>8,13320&255);
pin(17,7699>>8,7699&255);pin(17,13319>>8,13319&255);pin(17,13321>>8,13321&255)
for menu in [0,1,2,3,4,8]:
 sid=c.enum(7699)[menu];m=c.params(sid);pin(22,sid>>5,sid&31);pages=[]
 out.extend([f'menu.{menu}.struct={sid}',f'menu.{menu}.title={m[3493]}'])
 for page in range(1,7):
  pid=m.get(3447+page)
  if not pid:continue
  p=c.params(pid);pin(22,pid>>5,pid&31);ids=[p.get(3456+5*s,-1) for s in range(5)]
  if ids[0]<0:continue
  pages.append(str(page));prefix=f'page.{menu}.{page}.'
  out.extend([prefix+f'struct={pid}',prefix+'title='+p[3455],prefix+'hidden='+str(p.get(5327,0)),prefix+'faces='+','.join(map(str,ids))])
  faces.update(f for f in ids if f>=0)
 out.append(f'menu.{menu}.pages='+','.join(pages))
for cid in [8,708,714,715,717]:pin(3,1477,cid)
for cid in [5,6,7]:pin(3,1433,cid)
for sid in scripts:pin(12,sid,0)
for bit in bits:pin(2,69,bit)
for face in sorted(faces):
 files=c.archive(3,face);out.append(f'face.{face}='+str(len(files))+','+hashlib.sha256(b''.join(files[f] for f in sorted(files))).hexdigest())
for key,value in sorted(pins.items()):out.append('pin.'+key+'='+value)
path=root/'Ataraxia950/resources/native950/navigation-950.properties'
if '--write' not in sys.argv:
 print('Verified derived catalog:',len(faces),'interface groups,',len(pins),'individual pins. Pass --write to replace the derived resource.');raise SystemExit(0)
path.parent.mkdir(parents=True,exist_ok=True)
if path.exists():
 backup=root/'implementation-backup/ribbon-navigation'/path.relative_to(root);backup.parent.mkdir(parents=True,exist_ok=True)
 if not backup.exists():backup.write_bytes(path.read_bytes())
path.write_bytes(('\n'.join(out)+'\n').replace('\n','\r\n').encode('latin1'))
print('Wrote',path,'faces',len(faces),'pins',len(pins))
