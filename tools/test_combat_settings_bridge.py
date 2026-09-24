import unittest
from build_combat_settings_bridge import *

def notifications(row,value,owner=(365<<16)|19,kind=0):
    ops=prefix();stack=[];actors={};current=None;events=[];locals=[owner,row,value,kind];pc=0
    while pc<len(ops):
        op,arg=ops[pc];pc+=1
        if op==0x511:stack.append(arg[1])
        elif op==0x35e:stack.append(locals[arg])
        elif op==0x713:pc+=arg
        elif op in (0x412,0x647,0x3f2,0xab):
            b=stack.pop();a=stack.pop()
            if {0x412:a!=b,0x647:a==b,0x3f2:a<b,0xab:a>b}[op]:pc+=arg
        elif op==0x5:actors[stack.pop()]={}
        elif op==0x691:
            slot=stack.pop();typ=stack.pop();parent=stack.pop();assert typ==4
            assert slot==len(actors[parent]);actors[parent][slot]=True;current=(parent,slot)
        elif op in (0x8c3,0x828):del stack[-4:]
        elif op==0x83c:assert stack.pop()=='Select';assert stack.pop()==1
        elif op==0x109:
            slot=stack.pop();parent=stack.pop();current=(parent,slot)
            stack.append(int(parent==owner and slot==row or slot in actors.get(parent,{})))
        elif op==0x3e8:assert stack.pop()==1;events.append(current)
        else:raise AssertionError(hex(op))
    assert not stack
    return events

class Bridge(unittest.TestCase):
    def test_every_supported_native_selection_notifies_row_before_value(self):
        for row in ROWS:
            for value in range(19):
                self.assertEqual([((365<<16)|19,row),((365<<16)|20,value)],notifications(row,value))
    def test_other_controls_and_bad_values_do_not_notify(self):
        for row in (10246,10754,10755,10759,10827,-1,65535):self.assertEqual([],notifications(row,2))
        for value in (-1,19,2147483647):self.assertEqual([],notifications(10756,value))
        self.assertEqual([],notifications(10756,1,(324<<16)|30))
        self.assertEqual([],notifications(10756,1,kind=1))
    def test_original_callback_and_switch_offsets_remain_exact(self):
        raw,patched=patch();old,tail=scope['decode'](raw,inverse);new,newtail=scope['decode'](patched,inverse)
        self.assertEqual(tail[4:],newtail[4:])
        self.assertEqual([(x[2],x[3]) for x in old],[(x[2],x[3]) for x in new[len(prefix()):]])

if __name__=='__main__':unittest.main()
