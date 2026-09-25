"""Stack/ownership contracts for the authored 950 composition, not a renderer."""
import struct, unittest
from build_developer_console_950 import programs
from library_trace_read import scope,inverse

class VM:
    def __init__(self):
        self.scripts=programs();self.components={};self.active=None;self.vars={34289920:0,34130432:'',33817856:0};self.sent=[];self.native=[]
    def run(self,sid,args=(),strings=()):
        ops,tail=scope['decode'](self.scripts[sid],inverse)
        ni,ns,_,ai,as_,_=struct.unpack('>6H',tail[4:16])
        assert len(args)==ai and len(strings)==as_,(sid,ai,as_,args,strings)
        loc=list(args)+[0]*(ni-ai);sloc=list(strings)+['']*(ns-as_);iv=[];sv=[];pc=0;steps=0
        def pop(n):
            assert len(iv)>=n,(sid,pc,'int underflow',n,iv)
            if not n:return []
            result=iv[-n:];del iv[-n:];return result
        while pc<len(ops):
            steps+=1;assert steps<20000
            _,_,op,arg,_=ops[pc];pc+=1
            if op==0x511:(sv if arg[0]==2 else iv).append(arg[1])
            elif op==0x35e:iv.append(loc[arg])
            elif op==0x25a:sv.append(sloc[arg])
            elif op==0x592:loc[arg]=pop(1)[0]
            elif op==0x1ca:(sv if arg==34130432 else iv).append(self.vars.get(arg,0))
            elif op==0x195:self.vars[arg]=sv.pop() if arg==34130432 else pop(1)[0]
            elif op in (0x647,0x412):
                a,b=pop(2)
                if (a==b)==(op==0x647):pc+=arg
            elif op==0x713:pc+=arg
            elif op in (0x1d,0x51e,0x1f9,0x4d3):
                a,b=pop(2);iv.append(a+b if op==0x1d else a*b if op==0x51e else max(a,b) if op==0x1f9 else min(a,b))
            elif op==0x3f:b,a=sv.pop(),sv.pop();iv.append(int(a!=b))
            elif op==0x25f:iv.append(len(sv.pop()))
            elif op==0x267:s=''.join(sv[-arg:]);del sv[-arg:];sv.append(s)
            elif op==0x5:
                parent=pop(1)[0];self.components={k:v for k,v in self.components.items() if k[0]!=parent}
            elif op==0x691:
                parent,kind,child=pop(3)
                assert child==0 or (parent,child-1) in self.components,(parent,child)
                self.active=(parent,child);self.components[self.active]={'kind':kind}
            elif op==0x109:
                key=tuple(pop(2));iv.append(int(key in self.components));self.active=key
            elif op in (0x55,0x7c5):pop(5)
            elif op in (0x8c3,0x828):pop(4)
            elif op==0xa0:pop(3)
            elif op==0x1a2:
                x,y,parent=pop(3);self.vars['scroll:'+str(parent)]=y
            elif op==0xd1:iv.append(self.vars.get('scroll:'+str(pop(1)[0]),0))
            elif op in (0xe4,0x353,0x413):pop(2)
            elif op==0x8a2:pop(1)
            elif op==0x1a5:self.components[self.active]['colour']=pop(1)[0]
            elif op==0x1fc:self.components[self.active]['npc']=pop(1)[0]
            elif op==0x550:self.components[self.active]['item']=pop(2)
            elif op==0x79b:self.components[self.active]['player']=True
            elif op in (0x763,0x25d,0x836,0x804,0x73b,0x526):iv.append(self.components[self.active]['view'][(0x763,0x25d,0x836,0x804,0x73b,0x526).index(op)])
            elif op==0x67f:self.components[self.active]['seq']=pop(1)[0]
            elif op==0x112:self.components[self.active]['view']=pop(6)
            elif op==0x1f1:self.components[self.active]['text']=sv.pop()
            elif op==0x83c:
                number=pop(1)[0];self.components[self.active].setdefault('menu',{})[number]=sv.pop()
            elif op in (0x6a,0x375,0x815,0x732):
                target=pop(1)[0] if op!=0x6a else self.active
                sig=sv.pop();assert all(c in 'is' for c in sig)
                values=[]
                for kind in reversed(sig):values.append(pop(1)[0] if kind=='i' else sv.pop())
                callback=pop(1)[0];self.native.append(('hook',target,callback,list(reversed(values))))
            elif op==0x77b:self.sent.append(sv.pop())
            elif op==0x895:
                if arg in self.scripts:
                    _,t=scope['decode'](self.scripts[arg],inverse);a,b=struct.unpack('>2H',t[10:14]);vs=sv[-b:] if b else []
                    if b:del sv[-b:]
                    self.run(arg,pop(a),vs)
                elif arg==2995:
                    a=pop(11);parent,child=a[:2];assert child==0 or (parent,child-1) in self.components
                    self.active=(parent,child);self.components[self.active]={'kind':4,'text':sv.pop()}
                elif arg in (7791,8841):
                    a=pop(2);self.native.append((arg,a))
                    if arg==8841:self.vars[34289920]=11 if a[1] else 0
                elif arg==7170:
                    cursor,mode,key,char,limit=pop(5);s=sv.pop();assert mode==8 and limit==80
                    if key==85:s=s[:max(0,cursor-1)]+s[cursor:];cursor=max(0,cursor-1)
                    elif char and len(s)<limit:s=s[:cursor]+chr(char)+s[cursor:];cursor+=1
                    sv.append(s);iv.append(cursor)
                elif arg==1553:
                    key,cursor=pop(2);s=sv.pop();iv.append(max(0,cursor-1) if key==96 else min(len(s),cursor+1))
                else:raise AssertionError(('unknown call',arg))
            elif op==0x495:break
            else:raise AssertionError((sid,pc,hex(op)))
        assert not iv and not sv,(sid,'unbalanced',iv,sv)

class Primitives(unittest.TestCase):
    def test_search_update_retains_component_and_button_submits_typed_buffer(self):
        vm=VM();vm.run(21137,strings=['Man','__devsearch:4:','Search NPCs...'])
        field=vm.components[(1448<<16|4,0)]
        vm.run(21137,strings=['Man','__devsearch:5:','Search NPCs...'])
        self.assertIs(field,vm.components[(1448<<16|4,0)])
        vm.run(21138,strings=['__devsearch:5:','Man'])
        vm.vars[34130432]='Banker'
        vm.run(21153,strings=['__devsearch:5:','Man'])
        self.assertEqual(['__devsearch:5:Banker'],vm.sent)
        self.assertEqual(0,vm.vars[34289920])
        vm.run(21153,strings=['__devsearch:6:','Banker'])
        self.assertEqual('__devsearch:6:Banker',vm.sent[-1])

    def test_selected_border_does_not_change_item_or_row_operation(self):
        vm=VM();vm.run(21144,[0,20135],['Torva','__devop:3:2000'])
        vm.run(21154,[0,3])
        self.assertEqual(0xffd479,vm.components[(1448<<16|9,0)]['colour'])
        self.assertEqual([20135,1],vm.components[(1448<<16|9,1)]['item'])
        self.assertEqual({1:'Select'},vm.components[(1448<<16|9,2)]['menu'])

    def test_player_preview_and_rotation_share_the_same_bounds(self):
        vm=VM();vm.run(21155,[0,180,80,300,280,1500]);vm.run(21156,[0,180,80,300,280])
        self.assertTrue(vm.components[(1448<<16|7,0)]['player'])
        self.assertEqual(1500,vm.components[(1448<<16|7,0)]['view'][-1])
        self.assertEqual([],vm.sent)

    def test_collection_inspector_rebuild_keeps_scroll_but_new_query_resets(self):
        vm=VM();key='scroll:'+str(1448<<16|9);vm.vars[key]=400
        vm.run(21151,[100,0]);self.assertEqual(400,vm.vars[key])
        vm.run(21151,[100,1]);self.assertEqual(0,vm.vars[key])
    def test_item_rows_use_real_item_icons_and_select_only_contract(self):
        vm=VM();vm.run(21131,[100])
        for row in range(100):vm.run(21144,[row,20135+row],['Torva','__devop:3:'+str(2000+row)])
        self.assertEqual(300,len(vm.components))
        for row in range(100):
            self.assertEqual([20135+row,1],vm.components[(1448<<16|9,row*3+1)]['item'])
            self.assertEqual({1:'Select'},vm.components[(1448<<16|9,row*3+2)]['menu'])
        self.assertEqual([],vm.sent)
        vm.run(21146,[0,20135,578,103,64,64]);self.assertEqual([20135,1],vm.components[(1448<<16|7,0)]['item'])

    def test_zoom_preserves_drag_angles_clamps_and_resets_locally(self):
        vm=VM();vm.run(21135,[0,6260,1,1000,20]);key=(1448<<16|7,0)
        vm.components[key]['view']=[12,20,30,420,50,1000]
        vm.run(21147,[0,150,1000]);self.assertEqual([12,20,30,420,50,1150],vm.components[key]['view'])
        vm.run(21147,[0,-9999,1000]);self.assertEqual(50,vm.components[key]['view'][-1])
        vm.run(21147,[0,9999,1000]);self.assertEqual(6000,vm.components[key]['view'][-1])
        vm.run(21147,[0,0,1000]);self.assertEqual([12,20,30,420,50,1000],vm.components[key]['view']);self.assertEqual([],vm.sent)

    def test_generic_rows_do_not_expose_npc_actions_and_player_model_is_local(self):
        vm=VM();vm.run(21150,[0],['Destination','__devop:2:2000'])
        self.assertEqual({1:'Select'},vm.components[(1448<<16|9,1)]['menu'])
        vm.run(21149,[0]);self.assertTrue(vm.components[(1448<<16|7,0)]['player']);self.assertEqual([],vm.sent)

    def test_bounded_rows_share_parent_and_selection_does_not_rebuild(self):
        vm=VM();vm.run(21131,[100])
        for row in range(100):vm.run(21132,[row,100],[f'NPC {row}','__devop:4:'+str(row)])
        vm.run(21133);before=set(vm.components)
        vm.run(21134,[1,99,100],['__devop:4:99'])
        self.assertEqual(before,set(vm.components));self.assertEqual(['__devop:4:99'],vm.sent)
        self.assertEqual(200,len(vm.components));self.assertEqual(0xffd479,vm.components[(1448<<16|9,198)]['colour'])
        self.assertEqual(0x554d3b,vm.components[(1448<<16|9,0)]['colour'])
        self.assertEqual(['Select','Spawn near me','Place in world','Inspect'],list(vm.components[(1448<<16|9,1)]['menu'].values()))

    def test_model_sequence_changes_preserve_camera_and_never_spawn_world_entity(self):
        vm=VM();vm.run(21135,[0,6260,1,1000,-50]);view=vm.components[(1448<<16|7,0)]['view']
        vm.run(21136,[0]);vm.run(21141,[0,2]);model=vm.components[(1448<<16|7,0)]
        self.assertEqual(6260,model['npc']);self.assertEqual(2,model['seq']);self.assertEqual(view,model['view']);self.assertEqual([],vm.sent)
        vm.run(21135,[0,1,3,800,0]);self.assertEqual(1,model['npc']);self.assertEqual(3,model['seq'])

    def test_unverified_animation_has_no_operation_and_verified_preview_is_local(self):
        vm=VM();key=(1448<<16|5,0);vm.components[key]={'kind':4}
        vm.run(21143,[0,7,-1]);self.assertEqual('',vm.components[key]['menu'][1]);self.assertEqual(-1,vm.native[-1][2])
        vm.run(21143,[0,7,17389]);self.assertEqual('Preview',vm.components[key]['menu'][1])
        self.assertEqual(('hook',key,21141,[7,17389]),vm.native[-1]);self.assertEqual([],vm.sent)

    def test_search_edits_locally_submits_once_and_releases_keyboard(self):
        vm=VM();vm.run(21137,strings=['','__devsearch:4:','Search NPCs...']);vm.run(21138,strings=['__devsearch:4:',''])
        for ch in 'Man':vm.run(21139,[0,ord(ch)],['__devsearch:4:',''])
        self.assertEqual([],vm.sent);self.assertEqual('Man',vm.vars[34130432])
        vm.run(21139,[84,0],['__devsearch:4:','']);self.assertEqual(['__devsearch:4:Man'],vm.sent);self.assertEqual(0,vm.vars[34289920])
        vm.run(21139,[0,ord('x')],['__devsearch:4:','']);self.assertEqual('Man',vm.vars[34130432])

    def test_cancel_restores_query_and_does_not_submit_or_hold_focus(self):
        vm=VM();vm.run(21137,strings=['Man','__devsearch:4:','Search NPCs...']);vm.run(21138,strings=['__devsearch:4:','Man'])
        vm.run(21139,[85,0],['__devsearch:4:','Man']);self.assertEqual('Ma',vm.vars[34130432])
        vm.run(21139,[13,0],['__devsearch:4:','Man']);self.assertEqual('Man',vm.vars[34130432]);self.assertEqual([],vm.sent);self.assertEqual(0,vm.vars[34289920])

if __name__=='__main__':unittest.main()
