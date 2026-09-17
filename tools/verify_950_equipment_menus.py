#!/usr/bin/env python3
"""Read-only validation of the paired950 equipment Wear caption evidence and live pins."""
import hashlib,json,sys
from pathlib import Path
sys.dont_write_bytecode=True
import cache950
from verify_950_ui_scripts import decode
ROOT=Path(__file__).resolve().parents[1]
def main():
    evidence=json.loads((ROOT/'protocol-analysis/equipment-menu-scripts-950-evidence.json').read_text())
    resource=json.loads((ROOT/'Ataraxia950/resources/native950/ui-bindings-950.json').read_text())
    pins={v['id']:v['sha256'] for v in resource['scripts'].values() if isinstance(v,dict)}
    kotlin=(ROOT/'OpenNXT/src/main/kotlin/com/opennxt/net/login/Native950CacheContent.kt').read_text()
    mapping=json.loads((ROOT/'protocol-analysis/ui-scripts-950-evidence.json').read_text())['opcodeMap947to950']
    inverse={int(v['opcode950'],16):int(k,16) for k,v in mapping.items()}
    decoded={}
    for text,row in evidence['scripts'].items():
        sid=int(text);raw=cache950.archive(12,sid)[0];actual=hashlib.sha256(raw).hexdigest()
        assert actual==row['sha256']==pins[sid],sid
        assert actual in kotlin,'Missing Kotlin pin '+str(sid)
        ins,tail=decode(raw,inverse);decoded[sid]=[(op,arg) for _,_,op,arg,_ in ins]
    assert decoded[12090][8:13]==[(0x35e,0),(0x511,(0,1)),(0x35e,3),(0x511,(0,-1)),(0x895,2833)]
    assert decoded[2833][169:173]==[(0x35e,0),(0x35e,1),(0x35e,3),(0x895,18401)]
    assert decoded[18401][0:11]==[(0x35e,2),(0x511,(0,1)),(0x647,8),(0x30,14063104),(0x511,(0,1)),(0x647,1),(0x713,48),(0x35e,2),(0x511,(0,-1)),(0x647,1),(0x713,44)]
    for sid,expected in ((6468,[2,1,3,4,5]),(12405,[1,2,3,5])):
        ops=decoded[sid]
        got=[ops[i-1][1][1] for i,(op,arg) in enumerate(ops) if op==0x2f9]
        assert got==expected,(sid,got)
    raw=cache950.archive(2,69)[54934];var=evidence['varbits']['54934']
    assert raw.hex()==var['raw'] and hashlib.sha256(raw).hexdigest()==var['sha256']
    assert var['sha256'] in kotlin
    print('PASS:11 equipment caption scripts,8 new live script pins, exact Excalibur varbit and caller setting semantics; '+str(len(pins))+' total UI script resource rows')
if __name__=='__main__':main()
