"""Reproduce Gnome, Barbarian and Wilderness course pins from local950/910 caches without altering either cache."""
import sys,hashlib,json
from pathlib import Path
sys.dont_write_bytecode=True
import verify_950_melee_animations as a
ROOT=Path(__file__).resolve().parents[1]
def main():
 old=a.v.Packed(ROOT.parent/'Ataraxia-PS/data/cache');pins=[];rows=[]
 for i in [762,763,844,2338,828,751,497,753,759,4853,10580,741,3378]:
  raw=a.v.cache.archive(20,i>>7)[i&127];before=old.archive(20,i>>7)[i&127]
  equal=a.seq(raw)['opcodes'].get(1)==a.seq(before)['opcodes'].get(1)
  if not equal:raise ValueError('Changed animation frames/durations: '+str(i))
  sha=hashlib.sha256(raw).hexdigest();rows.append(dict(sequence=i,sameFramesAndDurations=equal,duration=a.seq(raw)['durationCycles'],sha256=sha));pins.append('sequence.'+str(i)+'='+sha)
 for i in [155,157,295]:
  raw=a.v.cache.archive(2,32)[i]
  if raw!=old.archive(2,32)[i]:raise ValueError('Changed BAS '+str(i))
  pins.append('bas.'+str(i)+'='+hashlib.sha256(raw).hexdigest())
 for i in [69526,69383,69508,2312,4059,69507,69384,69378,69377,1747,3205,1948,2302,20210,20211,43526,43595,64696,64698,64699,65362,65365,65367,65734]:
  raw=a.v.cache.archive(16,i>>8)[i&255];pins.append('object.'+str(i)+'='+hashlib.sha256(raw).hexdigest())
 (ROOT/'Ataraxia950/resources/native950/agility-assets-950.properties').write_text('\n'.join(pins)+'\n',encoding='utf-8')
 (ROOT/'protocol-analysis/agility-assets-950.json').write_text(json.dumps(rows,indent=2)+'\n',encoding='utf-8')
 print('Verified',len(pins),'Gnome/Barbarian/Wilderness course asset pins')
if __name__=='__main__':main()
