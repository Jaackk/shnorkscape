"""Verify paired950 asset identity before admitting ordinary RC/Divination. --write writes evidence/pins only."""
import sys, hashlib, json
from pathlib import Path
sys.dont_write_bytecode=True
import verify_950_melee_animations as v
ROOT=Path(__file__).resolve().parents[1]
ALTARS=[*range(2478,2489),17010,30624]
ITEMS=[1438,1448,1444,1440,1442,1446,5527,5529,5531,5535,5537,5533,13655,1436,7936,554,555,556,557,558,559,560,561,562,563,564,565,9075,*range(29313,29325),*range(29384,29407)]
SEQUENCES=[791,21231,21232,21234,790,21204,21205,21233,21235]
GFX=[186,4235,4236,4239,4240]
def main():
 old=v.v.Packed(ROOT.parent/'Ataraxia-PS/data/cache');pins={};rows=[]
 for kind,index,shift,ids in [('object',16,8,ALTARS+[87306,*range(2452,2458),*range(2465,2471)]),('npc',18,7,range(18150,18196)),('item',19,8,ITEMS),('sequence',20,7,SEQUENCES),('graphic',21,8,GFX)]:
  for id in sorted(set(ids)):
   data=v.v.cache.archive(index,id>>shift)[id&((1<<shift)-1)];sha=hashlib.sha256(data).hexdigest();pins[f'{kind}.{id}']=sha
   row=dict(kind=kind,id=id,sha256=sha)
   if kind in ['sequence','graphic']:
    previous=old.archive(index,id>>shift)[id&((1<<shift)-1)];row['same910Definition']=previous==data
    if kind=='sequence':
     a,b=v.seq(data),v.seq(previous);row['same910FramesAndDurations']=a['opcodes'].get(1)==b['opcodes'].get(1);row['durationCycles']=a['durationCycles']
     assert row['same910FramesAndDurations'],row
    else:assert row['same910Definition'],row
   rows.append(row)
 if '--write' in sys.argv:
  (ROOT/'Ataraxia950/resources/native950/runecrafting-divination-assets-950.properties').write_text('# Paired950 strict per-record SHA256; regenerate only after reviewed identity evidence.\n'+'\n'.join(f'{k}={v}' for k,v in sorted(pins.items()))+'\n',encoding='utf-8')
  (ROOT/'protocol-analysis/runecrafting-divination-assets-950.json').write_text(json.dumps({'assets':rows,'limit':'Matching sequence frame references do not independently prove all rendered modern model dependencies.'},indent=2)+'\n',encoding='utf-8')
 print('PASS:',len(rows),'asset records;',len(SEQUENCES),'sequence frame/duration comparisons;',len(GFX),'identical graphics definitions')
if __name__=='__main__':main()
