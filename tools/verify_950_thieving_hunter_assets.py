"""Paired-cache pins for ordinary Thieving / butterfly Hunter. Inputs opened read-only."""
import sys,re,json,hashlib
from pathlib import Path
sys.dont_write_bytecode=True
import verify_950_melee_animations as m
ROOT=Path(__file__).resolve().parents[1]
def main():
 old=m.v.Packed(ROOT.parent/'Ataraxia-PS/data/cache')
 log=(ROOT/'logs/thieving-hunter-cache-probe.log').read_text(encoding='utf8')
 src=(ROOT/'Ataraxia950/content/com/rs/game/player/actions/thieving/def/PickPocketableNPC.java').read_text()
 npc_types={int(n):kind for kind,nums in re.findall(r'^\s*(\w+)\(new short\[\]\{([^}]*)\}',src,re.M) for n in re.findall(r'\d+',nums)}
 allowed={'MAN':['Man','Woman'],'FARMER':['Farmer'],'FEMALE_HAM':['H.A.M. Member'],'MALE_HAM':['H.A.M. Member'],'WARRIOR':['Warrior woman','Al Kharid warrior'],'ROGUE':['Rogue'],'CAVE_GOBLIN':['Cave goblin'],'MASTER_FARMER':['Master Farmer'],'GUARD':['Guard'],'ARDOUGNE_KNIGHT':['Knight of Ardougne'],'MENAPHITE_THUG':['Menaphite Thug'],'PALADIN':['Paladin'],'GNOME':['Gnome','Gnome woman'],'HERO':['Hero'],'DWARF_TRADER':['Trader']}
 npcs=[];objects=[];extra={}
 names={"vegetable stalls":"VEGETABAL","veg stall":"VEGETABAL","baker's stall":"CAKE","bakery stall":"CAKE","crafting stall":"CRAFTING","food stall":"MONKEY_FOOD","general stall":"MONKEY_GENERAL","tea stall":"TEA_STALL","silk stall":"SILK_STALL","wine stall":"WINE_STALL","seed stall":"SEED_STALL","fur stall":"FUR_STALL","fish stall":"FISH_STALL","crossbow stall":"CROSSBOW_STALL","silver stall":"SILVER_STALL","spice stall":"SPICE_STALL","magic stall":"MAGIC_STALL","scimitar stall":"SCIMITAR_STALL"}
 for line in log.splitlines():
  cols=line.split('|')
  if len(cols)<4 or cols[-1]!='transforms=null':continue
  kind,sid=cols[0].split(' ');i=int(sid);name=cols[1]
  if kind=='npc' and i in npc_types and name in allowed.get(npc_types[i],[]) and 'Pickpocket' in cols[2]:
   npcs.append(i);extra['pickpocket.'+str(i)]=npc_types[i];extra['npcName.'+str(i)]=name
  elif kind=='npc' and i in [5082,5083,5084,5085] and 'Catch' in cols[2]:
   npcs.append(i);extra['npcName.'+str(i)]=name
  elif kind=='object' and ('Steal-from' in cols[2] or 'Steal from' in cols[2]):
   typ=names.get(name.lower()) or ('WINE_STALL' if i==14011 else None)
   if typ:objects.append(i);extra['stall.'+str(i)]=typ;extra['objectName.'+str(i)]=name
 items=set(int(i) for i in re.findall(r'new Item\((\d+)',src))
 stalls=(ROOT/'Ataraxia950/content/com/rs/game/player/actions/thieving/def/Stalls.java').read_text()
 for line in re.findall(r'new int\[\]\s*\{([^}]+)\}',stalls):items.update(map(int,re.findall(r'\d+',line)))
 items.update([10010,11259,10012,10014,10016,10018,10020])
 records=[];props=['format=1','revision=950']
 for kind,idx,shift,ids in [('npc',18,7,npcs),('object',16,8,objects),('item',19,8,sorted(items)),('sequence',20,7,[24887,5074,5075,5078,881,422,424,6606])]:
  for i in sorted(ids):
   b=m.v.cache.archive(idx,i>>shift)[i&((1<<shift)-1)];sha=hashlib.sha256(b).hexdigest();props.append(f'{kind}.{i}={sha}')
   a=old.archive(idx,i>>shift).get(i&((1<<shift)-1)) if i>>shift in old.reference(idx) else None
   row={'kind':kind,'id':i,'index':idx,'group':i>>shift,'file':i&((1<<shift)-1),'sha256':sha,'same910File':a==b}
   if kind=='sequence':
    d=m.seq(b);row['durationCycles']=d['durationCycles'];row['same910Frames']=a is not None and m.seq(a)['opcodes'].get(1)==d['opcodes'].get(1)
    assert row['same910Frames'],row
   records.append(row)
 props.extend(f'{k}={v}' for k,v in sorted(extra.items()))
 (ROOT/'Ataraxia950/resources/native950/thieving-hunter-assets-950.properties').write_text('\n'.join(props)+'\n',encoding='ascii')
 result={'scope':'Original ordinary pickpocket and stall tables; four butterflies; repurposed NPC IDs, transform roots and unrelated world options excluded. Pinning appearance definitions does not visually verify rendered models.','pins':records,'bindings':extra}
 (ROOT/'protocol-analysis/thieving-hunter-assets-950.json').write_text(json.dumps(result,indent=2)+'\n',encoding='utf8')
 print('Pinned',len(npcs)-4,'pickpocket NPCs,',len(objects),'stall IDs,',len(items),'items, four butterflies and eight sequences')
if __name__=='__main__':main()
