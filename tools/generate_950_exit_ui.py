"""Read-only paired950 Exit/Options component and script evidence; writes derived pins/report only."""
import hashlib,importlib.util,json,sys
from pathlib import Path
sys.dont_write_bytecode=True
import cache950 as cache
import verify_950_ui_scripts as cs
ROOT=Path(__file__).resolve().parents[1]
spec=importlib.util.spec_from_file_location("if3",ROOT.parent/"AstraNXT/OpenNXT/data/prot/947/generated/native947-3/verified/ui/chatbox-verifier-if3.py")
if3=importlib.util.module_from_spec(spec);spec.loader.exec_module(if3)
PINS={}
def pin(i,g,f):
    b=cache.archive(i,g)[f];PINS[f"{i}.{g}.{f}"]=hashlib.sha256(b).hexdigest();return b
components={}
for iface,files in ((1433,range(93)),(1477,[8,27,92,99,747,751,805,806,807,808,809])):
    for f in files:
        d=if3.decode(pin(3,iface,f));assert d["_consumed"]==d["_len"],(iface,f)
        components[f"{iface}:{f}"]=d
assert cache.enum(7716)[1004]==21278 and cache.params(21278)[3507]==(1477<<16|99)
pin(17,7716>>8,7716&255);pin(22,21278>>5,21278&31)
mapping=json.loads((ROOT/"protocol-analysis/ui-scripts-950-evidence.json").read_text())["opcodeMap947to950"]
inverse={int(v["opcode950"],16):int(k,16)for k,v in mapping.items()}
scripts={}
for sid in (8177,8178,8179,8180,8181,8182,9922,13831,2935,4143,4166,8411,13835,13836,13994,8420):
    data=pin(12,sid,0)
    ins,tail=cs.decode(data,inverse)
    scripts[str(sid)]={"sha256":hashlib.sha256(data).hexdigest(),"instructions":[f"{i}: {line}"for i,line in enumerate(cs.lines(ins))]}
assert components["1433:72"]["onOp"]==[8181,-2147483645,-2147483644]
assert components["1433:86"]["parent"]==84
report={"revision":950,"entry":{"root":1477,"component":99,"slot":1},"wrapper":805,
        "mount":806,"mountEvidence":"1433 at806 is a selected empty child of native wrapper805; cache scripts explicitly prove805 visibility/input context, but do not name modern1433 vs legacy274 sibling mounts.",
        "controls":{"66":"Hop Worlds","69":"Exit to Lobby","72":"Logout","79":"Close","86":"Confirm Logout","89":"Cancel"},
        "components":components,"scripts":scripts,"pins":PINS}
(ROOT/"protocol-analysis/exit-ui-950-evidence.json").write_text(json.dumps(report,indent=2)+"\n",encoding="utf-8")
out=ROOT/"Ataraxia950/resources/native950/exit-ui-950.properties"
out.write_text("# Paired950 Exit UI component/script SHA256; generated from the cache.\n"+"".join(f"{k}={v}\n"for k,v in PINS.items()),encoding="ascii")
print("PASS",len(components),"fully consumed950 components;",len(PINS),"pins")
