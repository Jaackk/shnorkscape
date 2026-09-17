"""Read-only paired950 anvil asset verification; runtime acceptance also re-derives every recipe."""
import sys,json,hashlib
from pathlib import Path
sys.dont_write_bytecode=True
import cache950 as cache
from verify_950_hitbars import Packed
from verify_950_melee_animations import seq
ROOT=Path(__file__).resolve().parents[1]
def main():
 path=ROOT/'Ataraxia950/resources/native950/smithing-assets-950.properties';count=0
 for line in path.read_text(encoding='utf-8').splitlines():
  if not line or line.startswith('#'):continue
  key,expected=line.split('=',1);kind,id=key.split('.');id=int(id)
  index={'item':19,'struct':22,'object':16,'sequence':20,'hitbar':2,'sprite':8}[kind]
  shift=5 if kind=='struct' else 7 if kind=='sequence' else 8
  group,file=(72,id)if kind=='hitbar'else(id,0)if kind=='sprite'else(id>>shift,id&((1<<shift)-1))
  raw=cache.archive(index,group)[file];assert hashlib.sha256(raw).hexdigest()==expected,key;count+=1
 rows=json.loads((ROOT/'Ataraxia950/resources/native950/smithing-recipes-950.json').read_text())['rows']
 assert len(rows)==702 and len({r['bar']for r in rows})==10
 old=Packed(ROOT.parent/'Ataraxia-PS/data/cache');current=cache.archive(20,22143>>7)[22143&127];previous=old.archive(20,22143>>7)[22143&127]
 assert seq(current)['opcodes'][1]==seq(previous)['opcodes'][1],'Smithing animation frame/duration identity changed'
 print('PASS:',count,'paired950 assets;',len(rows),'anvil recipes across10original metal families; animation22143 matches910 frames/durations. Runtime Native950SmithingAcceptance validates decoded recipes and actions.')
if __name__=='__main__':main()
