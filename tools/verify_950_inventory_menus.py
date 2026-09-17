#!/usr/bin/env python3
"""Read-only paired950 backpack CS2/native-mask evidence; --output records the report."""
import argparse, hashlib, json, sys
from pathlib import Path
sys.dont_write_bytecode = True
import cache950
from dis950 import Image, EXE_950, fmt
from verify_950_ui_scripts import decode, lines
ROOT = Path(__file__).resolve().parents[1]
SCRIPTS = (8677,8678,8679,8680,8682,12090,2833,2410,1620)
EXPECTED = {
8677:'61ce759acd890ac63405e11de88d80581b6f72e1d384eb900cdd714cf3333eb6',
8678:'727bc18626541d39da8dd739fe87dbb292ca37766782e83e85c7e6ef9b9e384f',
8679:'6519901216960540530774f3d0d27d763fe65608950006786ec7893343d6ca13',
8680:'0b52ef101d691c4e3f2c5e04c454af6e3ceca2bc705c1a3b8760c37b248008d7',
8682:'875561f065b16f74615745963e589278ab007fb94fe1bce4b2e535427b3d0254',
12090:'aae4aa14c3992f92c4dfcc3741171daa5236458f4ca07049ae3db2ba87241d54',
2833:'3277400face2411e85602eececb21520fd33a3003969678356d8cc20eeed36f8',
2410:'7da996882e2804da8b357bf812a52916cdd15af966664058710fa7995f75adc3',
1620:'e695aad1827f75c2a7f4a0a1c634cd1812bebff8b30aedd83b84a554059235e2',
}

def sha(data): return hashlib.sha256(data).hexdigest()

def main():
    parser=argparse.ArgumentParser();parser.add_argument('--output',type=Path);args=parser.parse_args()
    mapping=json.loads((ROOT/'protocol-analysis/ui-scripts-950-evidence.json').read_text())['opcodeMap947to950']
    inverse={int(v['opcode950'],16):int(k,16) for k,v in mapping.items()}
    source=(ROOT/'Ataraxia950/game/com/rs/game/player/client/Native950InventoryMenu.java').read_text()
    report={'clientSha256':sha(Path(EXE_950).read_bytes()),'scripts':{},'native':{},'ordinaryUiToCacheOption':{1:1,2:2,3:3,7:4,8:5}}
    assert report['clientSha256']=='fc7492548627a4068e88a7a45ba4f0be93004adf246bb4b174b24ae588768b36'
    decoded={}
    for sid in SCRIPTS:
        data=cache950.archive(12,sid)[0];assert sha(data)==EXPECTED[sid],sid
        instructions,tail=decode(data,inverse);decoded[sid]=(instructions,tail)
        report['scripts'][sid]={'sha256':sha(data),'calls':sorted(set(arg for _,_,op,arg,_ in instructions if op==0x895)),
            'instructions':['%d %s'%(i,v) for i,v in enumerate(lines(instructions))]}
    ops=[(op,arg) for _,_,op,arg,_ in decoded[2410][0]]
    # Native current backpack uses isInventory=1 branch. Each cache string's local is independent
    # of its UI operation; copying option-1 as cache index breaks both Add-to-toolbelt and Drop.
    for ui,local,at in ((1,0,50),(2,1,69),(3,2,88),(7,3,117),(8,4,136)):
        assert ops[at:at+3]==[(0x511,(0,ui)),(0x25a,local),(0x83c,0)]
    assert ops[109:112]==[(0x511,(0,10)),(0x511,(2,'Examine')),(0x83c,0)]
    ins,tail=decoded[2833];q=17;special=[]
    for n in range(tail[16]):
        count=int.from_bytes(tail[q:q+2],'big');q+=2;rows={}
        base=next(i for i,(_,_,op,a,_) in enumerate(ins) if op==0x51a and a==n)+1
        for _ in range(count):
            key=int.from_bytes(tail[q:q+4],'big',signed=True);off=int.from_bytes(tail[q+4:q+8],'big',signed=True);q+=8
            dest=base+off;rows[key]=[a for _,_,op,a,_ in ins[dest:dest+6] if op==0x895]
        special.append(rows)
    assert len(special[0])==71 and len(special[1])==8
    report['specialItemScripts']=special[0];report['specialCategoryScripts']=special[1]
    report['specialParamScripts']={6799:12405,4840:6468}
    for name,rows in zip(('SPECIAL_IDS','SPECIAL_CATEGORIES'),special):
        expected='{'+','.join(map(str,sorted(rows)))+'}'
        assert name+' = '+expected in source,'menu guard differs from native switch '+name
    im=Image(EXE_950)
    slices={
       'selectionHashSlotItemAndTargetMask':(0x14019f491,0x80),
       'targetCaptionRequiresNonzeroSourceMask':(0x14019f710,0x24),
       'itemTargetNeedsDestinationBit22AndSourceBit32':(0x1401699a6,0x2e),
       'npcTargetRequiresBit2':(0x14016d2c0,0x1a),
       'objectTargetRequiresBit4':(0x14016dee3,0x1a),
       'itemCategoryGetterRegistration':(0x1400610bb,0x19),
       'itemCategoryGetterField':(0x1400a6160,0x4d),
       'itemCategoryDefinition94Field':(0x14036f57c,0x31),
       'objectUseDescriptor':(0x1400e5157,0x14),
       'objectUseWriter90':(0x1400e516b,0x207),
       'npcUseDescriptor':(0x1400e56eb,0x10),
       'npcUseWriter19':(0x1400e56fb,0x105),
    }
    for name,(address,length) in slices.items():
        blob=im.read(address,length)
        report['native'][name]={'address':hex(address),'sha256':sha(blob),'hex':blob.hex(),'instructions':[fmt(v) for v in im.dis(address,length)]}
    assert im.read(0x14019f4e8,6).hex()=='c1e80b83e07f'
    assert im.read(0x14019f728,6).hex()=='f70000f80300'
    assert 'shr      ecx, 0x16' in '\n'.join(report['native']['itemTargetNeedsDestinationBit22AndSourceBit32']['instructions'])
    assert 'word ptr [rax + 0x228]' in '\n'.join(report['native']['itemCategoryGetterField']['instructions'])
    assert 'word ptr [r14 + 0x228], dx' in '\n'.join(report['native']['itemCategoryDefinition94Field']['instructions'])
    report['menuMask']={'previous':'0x24043e','current':'0x6d37fe','operations':'1..10 cache-authored captions',
      'sourceTargets':{'npc':2,'object':4,'componentItem':32},'dragParentDepth':1,'dragTargetBit':21,'itemUseTargetBit':22}
    report['limitations']=['Special menus require separate dispatch; the ordinary guard rejects their IDs/params/categories.',
      'Ground-item/player/world-tile selected target types are not enabled.',
      'Packet derivation and cache menus do not prove any unported gameplay effect.']
    if args.output:args.output.write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8')
    print('PASS:950 inventory source/target masks; nine pinnedCS2 scripts; ordinaryoperations1,2,3,7,8; 71ID+8categoryspecialguards; object90/NPC19 writers')
if __name__=='__main__':main()
