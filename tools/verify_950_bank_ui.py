"""Read-only evidence for the exact 950 bank interface and locally authored controls.

--write writes a derived audit report; the cache and runtime sources are never edited.
Interface headers and strings are decoded directly. Selected hook records are
verified as serialized argument lists; this is not a complete IF3 hook decoder.
Script opcodes are normalized using the separately audited 947->950 opcode map.
"""
import argparse, hashlib, json, re, sys
from pathlib import Path
sys.dont_write_bytecode = True
import cache950 as cache
from inspect_native947_interface_slots import Reader
from verify_950_ui_scripts import decode, lines
from verify_950_lodestones import switches

ROOT = Path(__file__).resolve().parents[1]
SCRIPT_IDS = (6962,8903,8904,13687,8866,1486,1487,8886,8887,8901,
    13745,13746,8912,8913,8914,8915,8916,13353,13798,9324,9325,
    9329,9330,13890,13903,13905,13909,14337,6793,6794,14351,5175,
    14073,14362,14367,7202,7203,7205,7206,7207,7208,7211,13121,
    10519,9833,2259,16587,8905,6298,6520,8420,8409,13964,9313,13748,9236,2293,2295,8911,14234)
CONTROLS = {
    'depositCarried': dict(component=39, hook=[8912], parent=38),
    'depositWorn': dict(component=42, hook=[8913], parent=41),
    'depositFamiliar': dict(component=45, hook=[8914], parent=44),
    'depositPouch': dict(component=48, hook=[8915], parent=47),
    'autoTabSwitch': dict(component=51, hook=[6298], parent=50),
    'destinationBackpack': dict(component=56, hook=[13745,0], parent=54),
    'destinationWorn': dict(component=60, hook=[13745,2], parent=58),
    'destinationFamiliar': dict(component=64, hook=[13745,1], parent=62),
    'quantity1': dict(component=93, hook=[8903,-2147483645], parent=92, quantityMode=2),
    'quantity5': dict(component=96, hook=[8903,-2147483645], parent=92, quantityMode=3),
    'quantity10': dict(component=99, hook=[8903,-2147483645], parent=92, quantityMode=4),
    'quantityAll': dict(component=103, hook=[8903,-2147483645], parent=102, quantityMode=7),
    'quantityX': dict(component=106, hook=[8903,-2147483645], parent=105, quantityMode=5),
    'changeX': dict(component=114, hook=[8903,-2147483645], parent=111, quantityMode=5),
    'placeholder': dict(component=123, hook=[8886,-2147483645,33882236,33882237,-2147483644], parent=122),
    'notes': dict(component=127, hook=[8866,-2147483645,33882240], parent=126),
    'moreStorage': dict(component=149, hook=[16587], parent=148),
    'bankView': dict(component=152, hook=[13746,0], parent=151),
    'presetView': dict(component=153, hook=[13746,1], parent=151),
    'allItems': dict(component=165, hook=[8905,-2147483644,1], parent=158),
    'search': dict(component=237, hook=[13890], parent=234),
    'cancelSearchOverlay': dict(component=238, hook=[13898], parent=234),
    'cancelSearch': dict(component=239, hook=[13898], parent=234),
}

def sha(raw): return hashlib.sha256(raw).hexdigest()
def header(raw):
    r=Reader(raw); version=r.integer(1); kind=r.integer(1)
    name=r.string() if kind & 128 else ''
    result=dict(format=version,type=kind & 127,name=name)
    for key,width in [('contentType',2),('x',2),('y',2),('width',2),('height',2),
                      ('widthMode',1),('heightMode',1),('xMode',1),('yMode',1),('parent',2),('flags',1)]:
        result[key]=r.integer(width)
    result['hidden']=bool(result['flags'] & 1)
    result['strings']=[v.decode('cp1252') for v in re.findall(rb'[ -~]{4,}',raw)]
    result['sha256']=sha(raw)
    return result

def hook_blob(args):
    out=bytes([len(args)])
    for arg in args:
        out += bytes([0])+arg.to_bytes(4,'big',signed=True) if isinstance(arg,int) else bytes([1])+arg.encode('cp1252')+bytes([0])
    return out

def collect():
    mapping=json.loads((ROOT/'protocol-analysis/ui-scripts-950-evidence.json').read_text())['opcodeMap947to950']
    inverse={int(v['opcode950'],16):int(k,16) for k,v in mapping.items()}
    components=cache.archive(3,517)
    report=dict(revision=950,interface=517,componentCount=len(components),
        method='Read-only IF3 headers, exact serialized hook records, and decoded CS2 programs. Unknown normalized opcodes retain numeric names.',
        controls={},components={str(f):header(raw) for f,raw in sorted(components.items())},scripts={})
    programs={}
    for sid in SCRIPT_IDS:
        raw=cache.archive(12,sid)[0];ins,tail=decode(raw,inverse)
        programs[sid]=[(op,arg) for _,_,op,arg,_ in ins]
        report['scripts'][str(sid)]=dict(sha256=sha(raw),
            arguments=[int.from_bytes(tail[n:n+2],'big') for n in (10,12,14)],
            calls=sorted(set(arg for _,_,op,arg,_ in ins if op==0x895)),
            switches=switches(tail),instructions=['%d %s'%(i,line) for i,line in enumerate(lines(ins))])
    for name,row in CONTROLS.items():
        raw=components[row['component']];needle=hook_blob(row['hook']);offset=raw.find(needle)
        assert offset>=40,(name,'hook record absent')
        assert report['components'][str(row['component'])]['parent']==row['parent'],name
        report['controls'][name]=dict(row,hookByteOffset=offset,
            authoredStrings=report['components'][str(row['component'])]['strings'])
    # Exact hash-to-default-mode switch; the same helper also supports interface1313.
    switch=report['scripts']['6962']['switches'][0]
    for name in ('quantity1','quantity5','quantity10','quantityAll','quantityX','changeX'):
        row=CONTROLS[name];dest=2+switch[(517<<16)|row['component']]
        assert programs[6962][dest:dest+2]==[(0x511,(0,row['quantityMode'])),(0xa2,45189<<8)]
    # Notes are varp160 (not a guessed varbit), and placeholders are varbit45190.
    assert programs[8866][0]==(0x1ca,160<<8)
    assert (0x195,160<<8) in programs[8866]
    assert programs[8886][4:8]==[(0x511,(0,1)),(0x30,45190<<8),(0x730,0),(0xa2,45190<<8)]
    # Search selects the original child index, reads that same source container slot,
    # then moves/hides the actor. It never reindexes matches into a compacted list.
    assert programs[9325][104:107]==[(0x511,(0,(517<<16)|201)),(0x35e,1),(0x109,0)]
    assert programs[9325][114:117]==[(0x511,(0,95)),(0x35e,1),(0x64,0)]
    assert programs[9325][131:138]==[(0x35e,4),(0x35e,5),(0x511,(0,0)),(0x511,(0,0)),(0x828,0),(0x511,(0,0)),(0x4a9,0)]
    assert programs[9325][159:161]==[(0x511,(0,1)),(0x4a9,0)]
    assert programs[14337][0]==(0x1ca,8971<<8)
    # Search has one ordinary key callback per mounted interface tree. Mounting
    # interface517 at both wrapper693 and content host695 dispatches that callback
    # twice into the same shared edit string; fixing text in the callback would
    # mask the duplicate interface lifecycle instead of correcting it.
    assert programs[13903][22:28]==[(0x511,(0,(517<<16)|234)),(0x195,34126592),
        (0x511,(0,8)),(0x195,34126848),(0x511,(0,12)),(0x195,34127104)]
    assert programs[13903][45:53]==[(0x511,(0,(517<<16)|component)) for component in (234,235,236,237,239)]+[(0x511,(0,0)),(0x511,(0,0)),(0x895,9833)]
    assert programs[9833][56:68]==[(0x511,(0,7211)),(0x511,(0,-2147483640)),
        (0x511,(0,-2147483639))]+[(0x35e,i) for i in (0,1,2,3,4,6)]+[(0x511,(2,'iiiiiiii')),(0x35e,1),(0x375,0)]
    assert programs[7211]==[(0x35e,i) for i in range(8)]+[(0x895,13121),(0x495,0)]
    assert programs[13121][0:5]==[(0x1ca,34289920),(0x511,(0,11)),(0x412,1),(0x713,1),(0x495,0)]
    assert programs[13121][158:167]==[(0x1ca,33817856),(0x1ca,34130432),
        (0x511,(0,0)),(0x35e,0),(0x35e,1),(0x1ca,34127104),(0x895,7170),
        (0x195,33817856),(0x195,34130432)]
    report['searchKeyboard']=dict(startScript=13903,binderScript=9833,keyScript=7211,
        editorScript=13121,keyComponent=(517<<16)|235,context=11,inputMode=8,
        activeInputOperand=34126592,inputModeOperand=34126848,maxLengthOperand=34127104,
        inputTextOperand=34130432,cursorOperand=33817856,contextOperand=34289920,
        registrationInstructions=[56,67],editInstructions=[158,166],
        note='One ordinary key hook per mounted interface traversal edits shared text. Exactly one bank mount is required; this cache audit does not simulate native keyboard traversal or claim visual acceptance.')
    # Native embedded amount field accepts the existing count-dialog packet in mode7.
    assert programs[7208][13:20]==[(0x1ca,34126848),(0x511,(0,7)),(0x647,1),(0x713,4),(0x25a,0),(0x895,7209),(0x80c,0)]
    assert programs[2259][0]==(0x1ca,111<<8)
    slot=cache.params(cache.enum(7716)[1017])
    assert slot[3494]==21329 and slot[3503]==(1477<<16)|693 and slot[3505]==(1477<<16)|695
    report['layout']=dict(slotKey=1017,slotStruct=21308,mountSelectionParam3494=slot[3494],wrapper=slot[3503],host=slot[3505],
        wrapperHeader=header(cache.archive(3,1477)[slot[3503]&65535]),
        hostHeader=header(cache.archive(3,1477)[slot[3505]&65535]),
        rootHeader=header(components[0]),
        note='Native wrapper onLoad8409 and bank root onLoad8420 participate in layout. Header sizes alone do not prove visible clipping.')
    report['state']=dict(quantityModeVarbit=45189,quantityModes={'1':2,'5':3,'10':4,'X':5,'all':7},
        customQuantityVarp=111,notesVarp=160,placeholderVarbit=45190,transferPresetVarbit=45191,withdrawDestinationVarbit=45139,
        destinationValues={'backpack':0,'familiar':1,'worn':2},bankCategoryVarbit=45140,
        bankTabVarbit=45141,allItemsValue=1,bankSizeVarp=8971)
    report['varbits']={str(v):dict(raw=cache.archive(2,69)[v].hex(),sha256=sha(cache.archive(2,69)[v])) for v in (45139,45140,45141,45158,45189,45190,45191,45221,45223,45911)}
    report['scopeObservations']=[
        'At audit start bootstrap hid entire quantity panel91 plus destination parents58/62, placeholder122 and notes126.',
        'At audit start only bank items201, inventory15, deposit carried39 and close317 were native bank bindings.',
        'Search237 is cache-local and preserves authoritative bank actor indexes; avoid refreshing/resetting it for its local click.',
        'CustomX114 selects X locally but server must begin the scoped default-amount request before numeric input can submit.',
        'Enabling note mode requires capacity and depleted-actor prediction based on output note ID, while source claims remain the bank item ID.',
        'Enabling worn destination requires atomic bank/equipment exchanges and cache-driven equip checks; merely exposing60 is insufficient.',
        'Placeholders need persisted zero-quantity slot metadata; current compact single-tab item arrays do not represent them.',
        'Presets, familiar storage, custom tabs, bank PIN and auxiliary storage are separate backend features, not unlocked by an event mask.',
        'Dynamic bank actor menus include op5 saved-X,op6 enter-X,op7 All,op8 Placeholder andop10 Examine; masks/routing must match real meanings.',
    ]
    return report

def main():
    p=argparse.ArgumentParser(description=__doc__);p.add_argument('--write',action='store_true');args=p.parse_args()
    report=collect();path=ROOT/'protocol-analysis/bank-ui-cache-950-evidence.json'
    if args.write:path.write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8')
    else:assert json.loads(path.read_text(encoding='utf-8'))==json.loads(json.dumps(report)),'Bank cache evidence changed'
    print('PASS: %d bank components, %d exact control hook records, %d CS2 programs; quantity/note/search-slot/keyboard/input assertions.'%(report['componentCount'],len(CONTROLS),len(SCRIPT_IDS)))
if __name__=='__main__':main()



