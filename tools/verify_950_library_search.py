"""Execute the pinned native9325 search program against catalogue-shaped rows.

Uses the existing decoded cache evidence, not a rewritten search algorithm.
Native950EquipmentLibraryAcceptance verifies its SHA against the live paired cache.
Unrelated drawing helpers are explicit no-op fixtures. No client/cache/save edits.
"""
import ast, json, re
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
EVIDENCE=json.loads((ROOT/'protocol-analysis/bank-ui-cache-950-evidence.json').read_text(encoding='utf-8-sig'))

def search(rows, query):
    program=[]
    for line in EVIDENCE['scripts']['9325']['instructions']:
        match=re.match(r'\d+ ([0-9a-f]+)\s+(?:\w+\s+)?(.+)$',line)
        op=int(match[1],16)
        # Opcode names are formatting only; preserve the decoded operands.
        raw=line.split(maxsplit=2)[2].strip()
        if op==0x511: arg=ast.literal_eval(raw[raw.index('('):])
        else: arg=int(raw.split()[-1])
        program.append((op,arg))
    ints=[0]*16; strings=[query,'']; stack=[]; hidden={}; active=-1; pc=0; gas=100000
    def pop(): return stack.pop()
    def take(n):
        result=stack[-n:] if n else []
        if n: del stack[-n:]
        return result
    while pc<len(program):
        gas-=1;assert gas>0
        op,arg=program[pc];pc+=1
        if op==0x511:stack.append(arg[1])
        elif op==0x35e:stack.append(ints[arg])
        elif op==0x592:ints[arg]=pop()
        elif op==0x25a:stack.append(strings[arg])
        elif op==0x717:strings[arg]=pop()
        elif op==0x410:stack.append(pop().strip())
        elif op==0x611:stack.append(pop().lower())
        elif op==0x25f:stack.append(len(pop()))
        elif op==0x862:
            start=pop();needle=pop();haystack=pop();stack.append(haystack.find(needle,start))
        elif op==0x86b:
            item=pop();stack.append(next((name for id,name in rows if id==item),'null'))
        elif op==0x64:
            slot=pop();container=pop();assert container==95
            stack.append(rows[slot][0] if slot<len(rows) else -1)
        elif op==0x109:
            child=pop();parent=pop();active=child
            stack.append(int(parent==((517<<16)|201) and child<len(rows)))
        elif op==0x4a9:hidden[active]=bool(pop())
        elif op==0x87a:pop();stack.append(440)
        elif op==0x195:pop()
        elif op==0xe4:take(2)
        elif op==0x828:take(4)
        elif op==0x1a2:take(3)
        elif op==0x7c5:take(5)
        elif op==0x713:pc+=arg
        elif op==0x1d:b=pop();a=pop();stack.append(a+b)
        elif op in (0x647,0x412,0x3f2,0x454,0xab,0x73d):
            b=pop();a=pop()
            if {0x647:a==b,0x412:a!=b,0x3f2:a<b,0x454:a>=b,0xab:a>b,0x73d:a<=b}[op]:pc+=arg
        elif op==0x895:
            if arg==14337:stack.append(len(rows))
            elif arg==6431:stack.append(0)
            elif arg in (8808,10239):pass
            elif arg in (9511,13828,9302):take(1)
            elif arg==10186:take(2)
            elif arg==9328:take(3)
            else:raise AssertionError(('unexpected helper',arg))
        elif op==0x495:break
        else:raise AssertionError((pc-1,hex(op),'unmodelled instruction'))
    return [slot for slot in range(len(rows)) if hidden.get(slot) is False]

def main():
    pins=dict(line.split('=',1) for line in (ROOT/'Ataraxia950/resources/native950/equipment-library-ui-950.properties').read_text().splitlines() if line and not line.startswith('#'))
    for script in ('9325','9324','13909'):
        assert EVIDENCE['scripts'][script]['sha256']==pins[f'12/{script}/0']
    # Cache-backed names/IDs from the existing testing catalogue and paired names TSV.
    names={}
    for row in (ROOT/'Ataraxia950/resources/native950/search-items.tsv').read_text(encoding='utf-8-sig').splitlines():
        parts=row.split('\t')
        if len(parts)==3 and parts[0].isdigit() and not parts[2]:names[int(parts[0])]=parts[1]
    ids=[53375,16403,42991,58041,56483,23531]
    rows=[(id,names[id]) for id in ids]
    assert search(rows,'TeCtOnIc')==[2]
    assert search(rows,'primal')==[1,3]
    assert search(rows,'necromancer')==[4]
    assert search(rows,'zzzz absent')==[]
    assert search(rows,'')==[] # Native empty-active-search differs from cancelling search.
    # Cancel hook13898 calls13909, then9324 when mounted at693. The latter
    # reads selected-tab45141 rather than resetting it. The wrapper is pinned
    # independently in equipment-library-ui-950.properties.
    cancel=EVIDENCE['scripts']['13909']['instructions']
    assert any('35265536' in line for line in cancel)
    assert any('11556096' in line for line in EVIDENCE['scripts']['9324']['instructions'])
    print('PASS: native CS9325 case-insensitive partial-name filtering retains original slots; empty/no-match behavior; cancel redraws selected native tab.')
    print('LIMIT: drawing helpers are fixtures; native keyboard and visual layout require live verification.')

if __name__=='__main__':main()
