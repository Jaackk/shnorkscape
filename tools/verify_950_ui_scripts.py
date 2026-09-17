#!/usr/bin/env python3
"""Compare 947/950 CS2 instructions without changing either cache or any server source.

Only --output writes derived evidence. The opcode map is inferred from thousands
of independently compiled script pairs, never from packet-writer output. Equal
normalized instructions prove equal script programs under that map; they do not
prove unchanged native opcode implementation or unchanged called data/scripts.
"""
import argparse, collections, difflib, hashlib, json, os, re, sys
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path
sys.dont_write_bytecode = True
ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT.parent/'AstraNXT/OpenNXT/tools'))
import inspect_native947_interface_slots as cache
from native947_paths import cache_path
FOUR = {0x35e,0x592,0x713,0x647,0x412,0xab,0x454,0x73d,0x3f2,0x25a,0x717,0x895,0x267,0x51a,0x56,0x96,0x639,0x1ca,0x195}
THREE = {0x30,0xa2}
NAMES = {0x35e:'iload',0x592:'istore',0x25a:'sload',0x717:'sstore',0x895:'gosub',0x495:'return',0x713:'branch',0x647:'branch_eq',0x412:'branch_ne',0x511:'push',0x51a:'switch',0x1ca:'getvar',0x195:'setvar',0x30:'getvarbit',0xa2:'setvarbit',0x1d:'add',0x51e:'multiply',0x585:'divide',0x59d:'scale',0x1f9:'max',0x4d3:'min',0x4d8:'current_stat'}

def all_scripts(path):
    cache.FLAT=Path(path)
    cache.archive.cache_clear()
    cache.reference.cache_clear()
    ids=list(cache.reference(12))
    print("READ",path,"scripts",len(ids),flush=True)
    with ThreadPoolExecutor(max_workers=12) as pool:
        rows=list(pool.map(lambda sid:(sid,cache.archive(12,sid)[0]),ids))
    print("READ COMPLETE",path,flush=True)
    return dict(rows)

def decode(data, inverse=None):
    end=len(data)-int.from_bytes(data[-2:],'big')-18
    p=data.index(0)+1
    count=int.from_bytes(data[end:end+4],'big')
    ins=[]
    while p<end:
        start=p; op=int.from_bytes(data[p:p+2],'big');p+=2
        old=op if inverse is None else inverse.get(op)
        if old is None: raise ValueError('unmapped opcode %04x at %d'%(op,start))
        if old==0x511:
            kind=data[p];p+=1
            if kind==2:
                q=data.index(0,p);value=data[p:q].decode('cp1252',errors='replace');p=q+1
            elif kind in (0,1):
                width=8 if kind==1 else 4
                value=int.from_bytes(data[p:p+width],'big',signed=True);p+=width
            else: raise ValueError('unsupported push type %d'%kind)
            arg=(kind,value)
        else:
            width=4 if old in FOUR or (inverse is not None and old in THREE) else 3 if old in THREE else 1
            arg=int.from_bytes(data[p:p+width],'big',signed=width==4);p+=width
        ins.append((start,p,old,arg,data[start+2:p]))
    if p!=end or len(ins)!=count: raise ValueError('bad boundary/count %d/%d %d/%d'%(p,end,len(ins),count))
    # Include locals, arguments, switches and switch-target offsets in equality.
    return ins,data[end:]

def normalize(ins): return [(op,arg) for a,b,op,arg,raw in ins]
def lines(ins): return ['%04x %-12s %r'%(op,NAMES.get(op,''),arg) for a,b,op,arg,raw in ins]
def sha(d): return hashlib.sha256(d).hexdigest()

def main():
    a=argparse.ArgumentParser();a.add_argument('--old-cache',default=str(ROOT.parent/'rs3cache/cache'))
    a.add_argument('--new-cache',default=str(ROOT/'cache'));a.add_argument('--output',type=Path)
    a.add_argument('--dump',nargs='*',type=int,default=[]);args=a.parse_args()
    old=all_scripts(args.old_cache);new=all_scripts(args.new_cache)
    old_decoded={};failed_old={};votes=collections.defaultdict(collections.Counter);seeds=[]
    for sid,data in old.items():
        try: old_decoded[sid]=decode(data)
        except (ValueError,IndexError) as ex:failed_old[sid]=str(ex)
    for sid,(ins,tail) in old_decoded.items():
        data=old[sid];other=new.get(sid)
        if other is None or data[:ins[0][0]]!=other[:ins[0][0]]:continue
        cursor=ins[0][0];pairs=[]
        for p,q,op,arg,raw in ins:
            code=int.from_bytes(other[cursor:cursor+2],'big');cursor+=2
            # 950 widens packed varbit id+flag operands from three bytes to four.
            # Comparing their numeric operand, every other literal and the exact
            # full metadata rejects a false alignment instead of accepting drift.
            width=len(raw)+(1 if op in THREE else 0)
            candidate=other[cursor:cursor+width];cursor+=width
            if op in THREE:
                same=len(candidate)==width and int.from_bytes(candidate,'big')==arg
            else:same=candidate==raw
            if not same:break
            pairs.append((op,code))
        else:
            if other[cursor:]!=tail:continue
            seeds.append(sid)
            for op,code in pairs:votes[op][code]+=1
    mapping={};ambiguity={}
    for op,c in votes.items():
        top,n=c.most_common(1)[0]
        if n>=1 and n/sum(c.values())>=0.995: mapping[op]=top
        else: ambiguity[hex(op)]=dict(c)
    # This input placeholder operation occurs only in changed script programs.
    # Native registrations and their callbacks independently establish the pair:
    # 947 0x140048ed8 -> 0x1401df4a0 -> 0x1401cea10;
    # 950 0x1400493bd -> 0x1401e69e0 -> 0x1401d5f30.
    # Both callbacks own cc_if_input_setemptytext, pop string + two ints,
    # copy the placeholder and OR/store its colour. Widget type moved 11 -> 12.
    mapping[0x576]=0x443
    inverse={v:k for k,v in mapping.items()}
    if len(inverse)!=len(mapping):raise RuntimeError('opcode map is not bijective')
    new_decoded={};failed_new={}
    for sid,data in new.items():
        try:new_decoded[sid]=decode(data,inverse)
        except (ValueError,IndexError) as ex:failed_new[sid]=str(ex)
    scripts={};categories={}
    for name in ('Settings','WorldMap'):
        source=(ROOT/f'Ataraxia950/game/com/rs/game/player/client/Native950{name}.java').read_text()
        ids=set(map(int,re.findall(r'pin\(12,\s*(\d+),',source)))
        # Include directly called scripts even when the source forgot to pin them.
        ids.update(map(int,re.findall(r'runClientScript\((\d+)',source)))
        if name=='WorldMap':
            ids.update(int(x) for x in re.findall(r'SCRIPT_(?:INIT|LOAD|CLOSE|OPEN)\s*=\s*(\d+)',source))
        categories[name]=sorted(ids)
        for sid in ids:
            row={'sha947':sha(old[sid]) if sid in old else None,'sha950':sha(new[sid]) if sid in new else None}
            if sid not in old_decoded or sid not in new_decoded:
                row.update(status='undecoded',oldError=failed_old.get(sid),newError=failed_new.get(sid))
            else:
                oi,ot=old_decoded[sid];ni,nt=new_decoded[sid]
                row['arguments947']=[int.from_bytes(ot[n:n+2],'big') for n in (10,12,14)]
                row['arguments950']=[int.from_bytes(nt[n:n+2],'big') for n in (10,12,14)]
                row['locals947']=[int.from_bytes(ot[n:n+2],'big') for n in (4,6,8)]
                row['locals950']=[int.from_bytes(nt[n:n+2],'big') for n in (4,6,8)]
                same=normalize(oi)==normalize(ni) and ot==nt
                row.update(status='recompiled_identical_program' if same else 'changed_program',instructions947=len(oi),instructions950=len(ni),metadataEqual=ot==nt, lowSupportOpcodes=[hex(op) for op in sorted({i[2] for i in ni}) if votes[op][mapping[op]]<3],
                    calls947=sorted(set(arg for p,q,op,arg,raw in oi if op==0x895)),calls950=sorted(set(arg for p,q,op,arg,raw in ni if op==0x895)))
                if not same:row['diff']=list(difflib.unified_diff(lines(oi),lines(ni),fromfile='947',tofile='950',lineterm=''))
            scripts[str(sid)]=row
    for sid in args.dump:
        print('SCRIPT',sid)
        if sid in new_decoded:
            for i,line in enumerate(lines(new_decoded[sid][0])):print('%4d'%i,line)
        else:print('UNDECODED',failed_new.get(sid));print('947 BASELINE');print('\n'.join(lines(old_decoded[sid][0])))
    report={'method':'Paired logical CS2 script operands and full metadata, bijective opcode normalization with per-op support counts and independent native checks for selected rare opcodes. Not proof of native handler/data/callee semantics.',
        'coverage':{'947total':len(old),'950total':len(new),'947decoded':len(old_decoded),'950decoded':len(new_decoded),'exactOperandSeedPairs':len(seeds),'opcodeMappings':len(mapping)},
        'opcodeMap947to950':{hex(k):{'opcode950':hex(v),'support':votes[k][v],'total':sum(votes[k].values())} for k,v in sorted(mapping.items())},
        'nativeOpcodeEvidence':{'0x576->0x443':{'947handler':'0x1401df4a0','947callback':'0x1401cea10','950handler':'0x1401e69e0','950callback':'0x1401d5f30','meaning':'cc_if_input_setemptytext: string + two ints; copy placeholder and OR/store colour; widget type11->12'},'0x339->0x4e5':{'947handler':'0x14017f0a0','950handler':'0x140184370','meaning':'same map coordinate unpack and region bounds operations'},'0x1f7->0x365':{'947handler':'0x14017e710','950handler':'0x1401839e0','meaning':'map state reset with constant1'}}, 'ambiguousOpcodes':ambiguity,'categories':categories,'scripts':scripts}
    print('COVERAGE',report['coverage'])
    for name,ids in categories.items():
        print(name,dict(collections.Counter(scripts[str(sid)]['status'] for sid in ids)))
        for sid in ids:
            row=scripts[str(sid)]
            if row['status']!='recompiled_identical_program':print(sid,row['status'],row.get('newError'))
    if args.output:
        args.output.write_text(json.dumps(report,indent=2)+'\n')
        print('WROTE',args.output)
if __name__=='__main__':main()
