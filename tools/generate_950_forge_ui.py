"""Export the paired950 native Smithing/Smelting interface and DB-backed product trees."""
import hashlib,json
from pathlib import Path
import cache950 as cache
from inspect_native947_interface_slots import Reader
ROOT=Path(__file__).resolve().parents[1]
def row(id):
 r=Reader(cache.archive(2,41)[id]);out=[]
 while True:
  op=r.integer(1)
  if op==0:break
  if op==4:
   assert r.integer(1)==112,(id,"Unexpected DBtable")
  elif op==3:
   columns=r.integer(1)
   while True:
    column=r.integer(1)
    if column==255:break
    types=[r.integer(1) for _ in range(r.integer(1))];count=r.integer(1)
    assert count<128 and all(t in(0,1,26,36,74)for t in types),(id,types,count)
    values=[[r.string()if t==36 else r.integer(4)for t in types]for _ in range(count)]
    if column==0:
     assert types==[36,26],(id,types)
     out=[{'label':v[0],'enum':v[1]}for v in values]
  else:raise AssertionError((id,op))
 assert len(out)<=5
 return out
def main():
 maps={str(e):cache.enum(e)for e in(2530,2531)}
 ids={1482,1489}|set(maps['2530'].values())|set(maps['2531'].values())
 rows={str(i):row(i)for i in sorted(ids)}
 enums=set((2530,2531,15093))|{v['enum']for r in rows.values()for v in r}
 lookup={str(e):cache.enum(e)for e in sorted(enums)}
 for e,items in lookup.items():
  if int(e)not in(2530,2531,15093):assert set(items)==set(range(len(items))),e
 pins={}
 def pin(i,g,f):pins[f'{i}.{g}.{f}']=hashlib.sha256(cache.archive(i,g)[f]).hexdigest()
 for f in cache.archive(3,37):pin(3,37,f)
 # Slot1047 is the native large central window. Its frame/drag handlers must
 # remain paired with the wrapper and host; resizing slot1007 cannot persist.
 assert cache.enum(7716)[1047]==40393
 slot=cache.params(40393)
 assert slot[3503]==(1477<<16)|724 and slot[3505]==(1477<<16)|726
 for f in(27,722,724,725,726,727):pin(3,1477,f)
 for struct in(40393,21262):pin(22,struct>>5,struct&31)
 pin(17,7716>>8,7716&255)
 for i in ids:pin(2,41,i)
 for e in enums:pin(17,e>>8,e&255)
 # Entry scripts, material/detail helpers, real button builders, station requirement
 # checks, and quantity slider callbacks used by the current interface37 flow.
 scripts=(29,31,1187,2542,2543,2549,2551,2584,2585,2586,2588,2589,2590,
          2591,2597,2598,2599,2600,2603,2605,2606,2608,2609,2610,2611,
          6233,7108,7126,7127,7129,7163,7164,8389,8421,8841,9670,
          10085,10087,10424,10450,10451,11145,1364,13968,13982,13984,
          13985,13986,13987,13995,13999,18374)
 scripts += (3934,20528,3927,8412,8301,13509,8304,8387,8390,8391,20543,8074)
 for script in scripts:pin(12,script,0)
 for bit in(43235,43239):pin(2,69,bit)
 data={'revision':950,'rows':rows,'enums':lookup,'pins':pins}
 target=ROOT/'Ataraxia950/resources/native950/forge-ui-950.json';target.write_text(json.dumps(data,indent=2)+'\n')
 print('Exported',len(rows),'DBrows,',len(enums),'enums and',len(pins),'pins')
if __name__=='__main__':main()
