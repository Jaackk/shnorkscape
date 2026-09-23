"""Execute guarded native bridge programs, and verify ordinary-bank instructions survive."""
import unittest
from build_library_search_bridge import *

def program(sid,patched):
    raw=unpack((DEST/f'12/{sid}.dat').read_bytes()) if patched else (ROOT/f'temp/library-followup-trace/12-{sid}-0.bin').read_bytes()
    return [(op,arg) for _,_,op,arg,_ in scope['decode'](raw,inverse)[0]]

def execute(ops,marker='',query='',previous='',slot=1,stop_original=None):
    stack=[];ints=[-1,0,0];ints[0]=slot if stop_original is not None else -1
    strings=[previous];variables={34130432:query,35272448:previous};events=[];pc=0
    def pop():return stack.pop()
    for _ in range(1000):
        if stop_original is not None and pc==stop_original:return 'original',events
        op,arg=ops[pc];pc+=1
        if op==0x511:stack.append(arg[1])
        elif op==0x35e:stack.append(ints[arg])
        elif op==0x592:ints[arg]=pop()
        elif op==0x25a:stack.append(strings[arg])
        elif op==0x717:strings[arg]=pop()
        elif op==0x1ca:stack.append(variables.get(arg,0))
        elif op==0x195:variables[arg]=pop()
        elif op==0x8b9:assert pop()==(517<<16)|73;stack.append(marker)
        elif op==0x3f:b=pop();a=pop();stack.append(0 if a==b else 1)
        elif op==0x267:parts=stack[-arg:];del stack[-arg:];stack.append(''.join(parts))
        elif op==0x77b:events.append(('send',pop()))
        elif op==0x389:stack.append(100)
        elif op==0x1d:b=pop();a=pop();stack.append(a+b)
        elif op==0x713:pc+=arg
        elif op in (0x647,0x412,0x73d,0xab,0x3f2):
            b=pop();a=pop()
            if {0x647:a==b,0x412:a!=b,0x73d:a<=b,0xab:a>b,0x3f2:a<b}[op]:pc+=arg
        elif op==0x895:
            assert arg==9325;mode=pop();text=pop();events.append(('filter',text,mode))
        elif op==0x56b:return None,events # timer installation; rendering fixture only
        elif op==0x495:return pop() if stack else None,events
        else:raise AssertionError((pc-1,hex(op)))
    raise AssertionError('loop limit')

class Bridge(unittest.TestCase):
    def test_changed_query_reaches_server_but_ordinary_bank_does_not(self):
        for marker in ('Load Preset:',MARKER):
            _,events=execute(program(13905,True),marker,'TeCtOnIc','old')
            self.assertIn(('filter','TeCtOnIc',1),events)
            self.assertEqual([e for e in events if e[0]=='send'],[('send',PREFIX+'q:TeCtOnIc')] if marker==MARKER else [])
        _,events=execute(program(13905,True),MARKER,'same','same');self.assertFalse(events)
        _,events=execute(program(13905,True),MARKER,'','old');self.assertIn(('send',PREFIX+'q:'),events)
    def test_activation_cancellation_and_names_are_library_only(self):
        for sid,event in [(13903,'begin'),(13909,'cancel')]:
            old,new=program(sid,False),program(sid,True);prefix=len(new)-len(old)
            at=28 if sid==13903 else 0
            for marker in ('Load Preset:',MARKER):
                result,events=execute(new[at:at+prefix],marker,stop_original=prefix)
                self.assertEqual(result,'original');self.assertEqual(events,[('send',PREFIX+event)] if marker==MARKER else [])
        for slot,name in enumerate(NAMES,1):
            for sid,expected in [(15897,name),(6963,0)]:
                old,new=program(sid,False),program(sid,True);prefix=len(new)-len(old)
                self.assertEqual(execute(new,MARKER,slot=slot,stop_original=prefix)[0],expected)
                self.assertEqual(execute(new,'Load Preset:',slot=slot,stop_original=prefix)[0],'original')
    def test_original_native_programs_are_preserved_with_relocated_jumps(self):
        jumps={0x713,0x647,0x412,0xab,0x454,0x73d,0x3f2}
        for sid in (13903,13905,13909,15897,6963):
            old,new=program(sid,False),program(sid,True);added=len(new)-len(old);at=10 if sid==13905 else 28 if sid==13903 else 0
            for i,(op,arg) in enumerate(old):
                j=i+(added if i>=at else 0);newop,newarg=new[j];self.assertEqual(op,newop)
                if op in jumps:
                    target=i+1+arg;expected=target+(added if target>at or at==0 and target==0 else 0)
                    self.assertEqual(j+1+newarg,expected)
                else:self.assertEqual(arg,newarg)
    def test_staged_groups_have_valid_crcs_and_pins(self):
        pins=json.loads((ROOT/'protocol-analysis/library-search-bridge-950.json').read_text())
        for key,pin in pins.items():
            sid=int(key.split('/')[1]);raw=unpack((DEST/f'12/{sid}.dat').read_bytes())
            self.assertEqual(hashlib.sha256(raw).hexdigest(),pin['patched'])

if __name__=='__main__':unittest.main()
