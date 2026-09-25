"""Offline native-script contracts and selected-component VM stack regression."""
import hashlib, struct, unittest
from build_developer_console_950 import programs, BASE, NATIVE_SELECT, MARKER_COMPONENT
from library_trace_read import scope, inverse, ROOT
from dis950 import Image, EXE_950
from pathlib import Path

class DeveloperScripts(unittest.TestCase):
    def test_menu_notifications_keep_epoch_and_operation(self):
        ops,_=scope['decode'](programs()[BASE+3],inverse)
        for choice,suffix in ((0,''),(1,''),(2,':2'),(3,':3'),(4,':4')):
            ints=[];strings=[];pc=0;sent=[]
            while pc<len(ops):
                _,_,op,arg,_=ops[pc];pc+=1
                if op==0x511:(strings if arg[0]==2 else ints).append(arg[1])
                elif op==0x35e:ints.append(choice)
                elif op==0x25a:strings.append('__devop:123:17')
                elif op==0x412:
                    b,a=ints.pop(),ints.pop()
                    if a!=b:pc+=arg
                elif op==0x267:joined=''.join(strings[-arg:]);del strings[-arg:];strings.append(joined)
                elif op==0xe4:self.assertEqual((1,1477<<16|708),tuple(ints[-2:]));del ints[-2:]
                elif op==0x77b:sent.append(strings.pop())
                elif op==0x895:self.assertEqual(21140,arg)
                elif op==0x495:break
                else:self.fail('Unexpected opcode '+hex(op))
            self.assertEqual(['__devop:123:17'+suffix],sent);self.assertEqual([],ints);self.assertEqual([],strings)

    def test_visible_title_matches_native_frame_child(self):
        native,_=scope['decode']((ROOT/'temp/library-followup-trace/12-8289-0.bin').read_bytes(),inverse)
        self.assertEqual((0,14),native[134][3]);self.assertEqual(0x109,native[135][2])
        authored,_=scope['decode'](programs()[BASE],inverse)
        self.assertTrue(any(authored[i-1][3]==(0,14) for i,entry in enumerate(authored) if entry[2]==0x109))

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
                    self.assertEqual('is',strings.pop());self.assertEqual(0,ints.pop());hook=(ints.pop(),strings.pop())
                elif op==NATIVE_SELECT:selected=(ints.pop(),ints.pop())
                elif op==0x495:break
                else:self.fail('Unexpected opcode '+hex(op))
            self.assertEqual([3]*slot+[4],created);self.assertEqual((BASE+3,'__devcancel:'+str(slot)),hook)
            self.assertEqual((slot,(1448<<16)|11),selected);self.assertEqual([],ints);self.assertEqual([],strings)

    def test_render_helpers_argument_and_return_contracts(self):
        for sid,expected in ((BASE,(0,0)),(BASE+1,(7,2)),(BASE+2,(6,1)),(BASE+3,(1,1)),(BASE+5,(0,0)),(BASE+6,(0,1))):
            ops,tail=scope['decode'](programs()[sid],inverse)
            self.assertEqual(expected,struct.unpack('>2H',tail[10:14]));self.assertEqual(0x495,ops[-1][2])
        ops,_=scope['decode'](programs()[BASE+1],inverse)
        self.assertTrue(any(op==0x895 and arg==10410 for _,_,op,arg,_ in ops))
        self.assertEqual(0x6a,ops[-2][2])

    def test_native_ready_bridge_preserves_original_refresh_and_scopes_notification(self):
        from build_developer_console_950 import unpack
        native,_=scope['decode'](unpack((ROOT/'cache/12/8286.dat').read_bytes()),inverse)
        patched,_=scope['decode'](programs()[8286],inverse)
        self.assertEqual([(o,a) for _,_,o,a,_ in native[:19]],[(o,a) for _,_,o,a,_ in patched[:19]])
        # Execute appended guard with a normal management host, dev marker, and
        # cleared close marker. Only the developer session may acknowledge.
        for marker,wanted in (('',[]),('CUSTOMISATIONS',[]),('SHNORKSCAPE Developer Console',['__devready'])):
            ints=[];strings=[];sent=[];pc=19
            while pc<len(patched):
                _,_,op,arg,_=patched[pc];pc+=1
                if op==0x511:(strings if arg[0]==2 else ints).append(arg[1])
                elif op==0x5a2:self.assertEqual(MARKER_COMPONENT,ints.pop());ints.append(1)
                elif op==0x8b9:self.assertEqual(MARKER_COMPONENT,ints.pop());strings.append(marker)
                elif op==0x3f:b,a=strings.pop(),strings.pop();ints.append(0 if a==b else 1)
                elif op==0x412:
                    b,a=ints.pop(),ints.pop()
                    if a!=b:pc+=arg
                elif op==0xe4:self.assertEqual((1,1477<<16|708),tuple(ints[-2:]));del ints[-2:]
                elif op==0x77b:sent.append(strings.pop())
                elif op==0x495:break
                else:self.fail(hex(op))
            self.assertEqual(wanted,sent);self.assertEqual([],ints);self.assertEqual([],strings)

    def test_marker_requires_native_text_and_synchronous_setter(self):
        # Raw definition type is byte1. Container713 cannot retain readable text.
        self.assertEqual(0,(ROOT/'temp/dev-console/faces/1477-713.bin').read_bytes()[1])
        self.assertEqual(4,(ROOT/'temp/dev-console/faces/1448-14.bin').read_bytes()[1])
        setter,_=scope['decode'](programs()[BASE+6],inverse)
        bridge,_=scope['decode'](programs()[8286],inverse)
        for mounted in (False,True):
            for value in ('SHNORKSCAPE Developer Console',''):
                ints=[];strings=[];text='Loading...';sent=[]
                for ops,start in ((setter,0),(bridge,19)):
                    pc=start
                    while pc<len(ops):
                        _,_,op,arg,_=ops[pc];pc+=1
                        if op==0x511:(strings if arg[0]==2 else ints).append(arg[1])
                        elif op==0x5a2:self.assertEqual(MARKER_COMPONENT,ints.pop());ints.append(int(mounted))
                        elif op==0x412:
                            b,a=ints.pop(),ints.pop()
                            if a!=b:pc+=arg
                        elif op==0x25a:strings.append(value)
                        elif op==0x1f1:self.assertTrue(mounted);text=strings.pop()
                        elif op==0x8b9:self.assertTrue(mounted);self.assertEqual(MARKER_COMPONENT,ints.pop());strings.append(text)
                        elif op==0x3f:b,a=strings.pop(),strings.pop();ints.append(int(a!=b))
                        elif op==0xe4:self.assertEqual((1,1477<<16|708),tuple(ints[-2:]));del ints[-2:]
                        elif op==0x77b:sent.append(strings.pop())
                        elif op==0x495:break
                        else:self.fail(hex(op))
                    self.assertEqual([],ints);self.assertEqual([],strings)
                self.assertEqual(['__devready'] if mounted and value else [],sent)

if __name__=='__main__':unittest.main()
