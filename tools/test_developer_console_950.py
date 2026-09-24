"""Offline native-script contracts and selected-component VM stack regression."""
import hashlib, struct, unittest
from build_developer_console_950 import programs, BASE, NATIVE_SELECT
from library_trace_read import scope, inverse, ROOT
from dis950 import Image, EXE_950
from pathlib import Path

class DeveloperScripts(unittest.TestCase):
    def test_exact_native_selection_handler(self):
        im=Image(EXE_950)
        self.assertEqual('fc7492548627a4068e88a7a45ba4f0be93004adf246bb4b174b24ae588768b36',hashlib.sha256(Path(EXE_950).read_bytes()).hexdigest())
        handler=im.dis(0x1401f76f0,150)
        calls=[i.op_str for i in handler if i.mnemonic=='call']
        for address in ('0x140390f60','0x14019f620','0x14019f3e0'):self.assertIn(address,calls)

    def test_target_source_loop_hook_and_stack(self):
        raw=programs()[BASE+4];ops,tail=scope['decode'](raw,dict(inverse)|{0x72c:NATIVE_SELECT})
        self.assertEqual((2,2,0,1,2,0),struct.unpack('>6H',tail[4:16]))
        for slot in (1,7,4095):
            ints=[];strings=[];local=[slot,0];slocal=['Place NPC','__devcancel:'+str(slot)];created=[];pc=0;steps=0;hook=None;selected=None
            while pc<len(ops):
                steps+=1;self.assertLess(steps,100000);_,_,op,arg,_=ops[pc];pc+=1
                if op==0x511:(strings if arg[0]==2 else ints).append(arg[1])
                elif op==0x35e:ints.append(local[arg])
                elif op==0x25a:strings.append(slocal[arg])
                elif op==0x592:local[arg]=ints.pop()
                elif op==0x5:self.assertEqual((1448<<16)|11,ints.pop())
                elif op==0x647:
                    b,a=ints.pop(),ints.pop()
                    if a==b:pc+=arg
                elif op==0x713:pc+=arg
                elif op==0x1d:b,a=ints.pop(),ints.pop();ints.append(a+b)
                elif op==0x691:
                    child,kind,parent=ints.pop(),ints.pop(),ints.pop();self.assertEqual(len(created),child);self.assertEqual((1448<<16)|11,parent);created.append(kind)
                elif op==0x4a9:self.assertEqual(1,ints.pop())
                elif op==0x776:self.assertEqual('Place NPC',strings.pop())
                elif op==0x665:
                    self.assertEqual('s',strings.pop());hook=(ints.pop(),strings.pop())
                elif op==NATIVE_SELECT:selected=(ints.pop(),ints.pop())
                elif op==0x495:break
                else:self.fail('Unexpected opcode '+hex(op))
            self.assertEqual([3]*slot+[4],created);self.assertEqual((BASE+3,'__devcancel:'+str(slot)),hook)
            self.assertEqual((slot,(1448<<16)|11),selected);self.assertEqual([],ints);self.assertEqual([],strings)

    def test_render_helpers_argument_and_return_contracts(self):
        for sid,expected in ((BASE,(0,0)),(BASE+1,(6,2)),(BASE+2,(6,1)),(BASE+3,(0,1)),(BASE+5,(0,0))):
            ops,tail=scope['decode'](programs()[sid],inverse)
            self.assertEqual(expected,struct.unpack('>2H',tail[10:14]));self.assertEqual(0x495,ops[-1][2])
        ops,_=scope['decode'](programs()[BASE+1],inverse)
        self.assertTrue(any(op==0x895 and arg==10410 for _,_,op,arg,_ in ops))
        self.assertEqual(0x6a,ops[-2][2])

    def test_native_ready_bridge_preserves_original_refresh_and_scopes_notification(self):
        from build_developer_console_950 import unpack
        native,_=scope['decode'](unpack((ROOT/'cache/12/8286.dat').read_bytes()),inverse)
        patched,_=scope['decode'](programs()[8286],inverse)
        self.assertEqual([(o,a) for _,_,o,a,_ in native[:-1]],[(o,a) for _,_,o,a,_ in patched[:len(native)-1]])
        # Execute appended guard with a normal management host, dev marker, and
        # cleared close marker. Only the developer session may acknowledge.
        for marker,wanted in (('',[]),('CUSTOMISATIONS',[]),('SHNORKSCAPE Developer Console',['__devready'])):
            ints=[];strings=[];sent=[];pc=len(native)-1
            while pc<len(patched):
                _,_,op,arg,_=patched[pc];pc+=1
                if op==0x511:(strings if arg[0]==2 else ints).append(arg[1])
                elif op==0x8b9:self.assertEqual(1477<<16|713,ints.pop());strings.append(marker)
                elif op==0x3f:b,a=strings.pop(),strings.pop();ints.append(0 if a==b else 1)
                elif op==0x412:
                    b,a=ints.pop(),ints.pop()
                    if a!=b:pc+=arg
                elif op==0x77b:sent.append(strings.pop())
                elif op==0x495:break
                else:self.fail(hex(op))
            self.assertEqual(wanted,sent);self.assertEqual([],ints);self.assertEqual([],strings)

if __name__=='__main__':unittest.main()
