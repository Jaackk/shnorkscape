"""Read-only actual-950 bank mount/compaction branch regression.

Executes a bounded subset of decoded cache CS2, not a replacement client. The
single-tab fixture stubs only unrelated retail capacity and responsive drawing;
10906,6961,14354,14358,14360,14293 and9316 execute their real instructions.
--write updates derived evidence only. No cache or runtime file is modified.
"""
import argparse, hashlib, json, sys
from pathlib import Path
sys.dont_write_bytecode=True
import cache950 as cache
from verify_950_ui_scripts import decode, lines

ROOT=Path(__file__).resolve().parents[1]
SCRIPTS=(10906,6961,14354,14358,14360,14293,14337,14344,9316)
EMPTY=48447

class Programs:
    def __init__(self):
        mapping=json.loads((ROOT/'protocol-analysis/ui-scripts-950-evidence.json').read_text())['opcodeMap947to950']
        self.inverse={int(v['opcode950'],16):int(k,16) for k,v in mapping.items()}
        self.loaded={}
    def get(self,sid):
        if sid not in self.loaded:
            raw=cache.archive(12,sid)[0]
            ins,tail=decode(raw,self.inverse)
            self.loaded[sid]=(raw,ins,int.from_bytes(tail[10:12],'big'))
        return self.loaded[sid]

class Vm:
    """Strict interpreter for the audited integer/control/component subset."""
    def __init__(self,programs,mounts,rows):
        self.programs=programs;self.mounts=mounts;self.rows=list(rows)+[(EMPTY,0)]*(1820-len(rows))
        self.vars={8970<<8:-1,8971<<8:len(rows),35264768:1,35265024:0}
        self.active={0:-1,1:-1};self.calls=[];self.gas=250000
    def call(self,sid,args=()):
        self.calls.append(sid)
        # This compact single-tab fixture is below every retail capacity limit.
        if sid==3999:return [700]
        if sid==5787:return [0,0] # Empty additional tabs.
        if sid==5788:return [0,self.vars[8971<<8]]
        if sid in (9313,13889,13748,10186,9324,15439):return [] # Draw/refresh trace only.
        raw,ins,argc=self.programs.get(sid)
        assert len(args)==argc,(sid,args,argc)
        locals_=list(args)+[0]*32;stack=[];pc=0
        def pop():
            assert stack,(sid,pc,'stack underflow')
            return stack.pop()
        def take(n):
            result=stack[-n:] if n else []
            if n:del stack[-n:]
            return result
        while pc<len(ins):
            self.gas-=1;assert self.gas>0,'CS2 fixture exceeded instruction bound'
            _,_,op,arg,_=ins[pc];pc+=1
            if op==0x511:stack.append(arg[1])
            elif op==0x35e:stack.append(locals_[arg])
            elif op==0x592:locals_[arg]=pop()
            elif op==0x1ca:stack.append(self.vars.get(arg,0))
            elif op==0x195:self.vars[arg]=pop()
            elif op==0x30:stack.append(0)
            elif op==0x389:stack.append(100)
            elif op==0x540:
                param=pop();struct=pop();stack.append(cache.params(struct).get(param,0))
            elif op==0x444:
                interface=pop();parent=pop();stack.append(int(self.mounts.get(parent)==interface))
            elif op==0x713:pc+=arg
            elif op in (0x647,0x412,0x3f2,0x454,0xab,0x73d):
                b=pop();a=pop()
                result={0x647:a==b,0x412:a!=b,0x3f2:a<b,0x454:a>=b,0xab:a>b,0x73d:a<=b}[op]
                if result:pc+=arg
            elif op in (0x1d,0x730,0x4d3,0x1f9):
                b=pop();a=pop()
                stack.append({0x1d:lambda:a+b,0x730:lambda:a-b,0x4d3:lambda:min(a,b),0x1f9:lambda:max(a,b)}[op]())
            elif op==0x895:
                _,_,count=self.programs.get(arg)
                stack.extend(self.call(arg,take(count)))
            elif op==0x109:
                child=pop();parent=pop()
                exists=parent==(517<<16)|201 and 0<=child<len(self.rows)
                if exists:self.active[arg]=child
                stack.append(int(exists))
            elif op==0x36f:stack.append(self.rows[self.active[arg]][0])
            elif op==0x53d:stack.append(self.rows[self.active[arg]][1])
            elif op in (0x550,0x7d0):
                count=pop();item=pop();self.rows[self.active[arg]]=(item,count)
            elif op==0x56b:
                parent=pop();signature=pop();handler=pop();assert signature=='',signature
            elif op==0x7ca:pop()
            elif op==0x495:return stack
            else:raise AssertionError((sid,pc-1,hex(op),'unsupported instruction'))
        raise AssertionError((sid,'missing return'))

def collect():
    programs=Programs();slot_id=cache.enum(7716)[1017];slot=cache.params(slot_id)
    window=slot[3503];layout=slot[3505]
    assert slot_id==21308 and window==(1477<<16)|693 and layout==(1477<<16)|695
    p=programs.get(10906)[1]
    assert [(op,arg) for _,_,op,arg,_ in p[:6]]==[(0x511,(0,21308)),(0x511,(0,3503)),(0x540,0),(0x511,(0,517)),(0x444,0),(0x495,0)]
    branches={}
    for label,mount in [('correctWindow693',window),('incorrectLayout695',layout)]:
        rows=[(211,1),(215,1),(24000,1)]
        vm=Vm(programs,{mount:517},rows)
        mounted=vm.call(10906)[0]
        # Real6794->14362 has just exhausted the selected ordinary actor.
        # The program under test determines whether that actor survives until
        # native IF_BUTTON reads it, or is replaced by the next bank row.
        vm.rows[0]=(EMPTY,0)
        vm.call(6961,[0,211,0])
        compacted=14354 in vm.calls
        actor=vm.rows[0][0]
        expected=mount==window
        assert mounted==int(expected)
        assert compacted==(not expected)
        assert actor==(EMPTY if expected else 215),(label,actor)
        assert vm.vars[8970<<8]==(0 if expected else -1)
        refresh=Vm(programs,{mount:517},rows);refresh.call(9316)
        draws=[sid for sid in refresh.calls if sid in (9313,13889,13748,10186,9324,15439)]
        assert bool(draws)==expected,(label,draws)
        branches[label]=dict(parent=mount,bankMountRecognized=bool(mounted),
            immediateCompaction=compacted,clickedActorAfterEmptying211=actor,
            firstPendingEmptySlot=vm.vars[8970<<8],refreshCalls=draws,
            compactionCalls=vm.calls)
    # The three reported failures all carried the following source row's ID.
    cases=[]
    for source,following in [(211,215),(215,24000),(20000,62789)]:
        vm=Vm(programs,{layout:517},[(source,1),(following,1)])
        vm.rows[0]=(EMPTY,0);vm.call(6961,[0,source,0])
        assert vm.rows[0][0]==following
        cases.append(dict(source=source,nextRow=following,incorrectMountActor=vm.rows[0][0]))
    evidence={}
    for sid in SCRIPTS:
        raw,ins,argc=programs.get(sid)
        evidence[str(sid)]=dict(sha256=hashlib.sha256(raw).hexdigest(),intArguments=argc,
            instructions=['%d %s'%(i,line) for i,line in enumerate(lines(ins))])
    return dict(revision=950,slotStruct=slot_id,windowMountParam=3503,
        windowMount=window,layoutComponentParam=3505,layoutComponent=layout,
        scripts=evidence,branches=branches,reportedNextRowCases=cases,
        scope='Bounded real-cache branch/component interpreter. Retail capacity and unrelated drawing helpers are explicit fixtures; actual native input scheduling/visuals still require live acceptance.')



def main():
    p=argparse.ArgumentParser(description=__doc__);p.add_argument('--write',action='store_true');args=p.parse_args()
    result=collect();path=ROOT/'protocol-analysis/bank-mount-950-evidence.json'
    if args.write:path.write_text(json.dumps(result,indent=2)+'\n',encoding='utf-8')
    else:assert json.loads(path.read_text())==result,'Bank mount cache evidence changed'
    print('PASS: actual950 CS10906 recognizes sole693; sole695 reproduces next-row actor claims215/24000/62789; CS6961 deferred/immediate compaction and CS9316 refresh gating verified.')
if __name__=='__main__':main()

