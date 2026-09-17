#!/usr/bin/env python3
"""Read-only paired950 Forge modal/drag verifier; optional --output writes derived evidence only."""
import argparse, hashlib, json, struct, sys
from pathlib import Path
sys.dont_write_bytecode = True
ROOT = Path(__file__).resolve().parents[1]
import cache950 as cache
import verify_950_ui_scripts as cs

SCRIPT_IDS = [1364, 2600, 8421, 3934, 20528, 3927, 8412, 8301, 13509,
              8304, 8387, 8390, 8391, 20543, 8072, 8074, 1186, 8874,
              19986, 7808, 8178, 20393]
# Exact independent cache bytes supporting the relocation; never generated from packet writers.
PINS = {
 "17/30/36":"63a614bce39c6953c96821e6fda3ad88ea42b5d3a6d0b8feb82819b2ce84694a",
 "22/1262/9":"b7a15ba08df53003362a7b64308fc1d26988d69b4f812ea3e139c8d4cdc2e7f3",
 "22/665/24":"f3e54c3a96195bf71eb9918c30c5d9829b3a3309b972e59a614f73847b4a78ff",
 "17/30/40":"daa718c704caba4e5049e17054c34da43dd8cd495ab80844b560a0227e6b0c7a",
 "22/666/18":"48a6d0456845c126db349f10f62c4081860b9f6f8480b866cb8f19291495ea29",
 "3/1477/724":"7203f948165c41f00980b843abfc243237304877a8077d550000d5f9bc2b8141",
 "3/1477/726":"fed3952553972c77c61e7e0229d99be22049b4dced597c43d0122469024f7a0a",
 "3/1477/727":"fed3952553972c77c61e7e0229d99be22049b4dced597c43d0122469024f7a0a",
 "3/1477/732":"5f3f29f418ca18c3ccc5dee67e7a38c667112d64f983541e6bca20b1f861a3ac",
 "3/1477/735":"7aabe17f121ec67146e2139a210ef413950360de2c9e673be8b03dbbdeb69674",
 "3/37/17":"9f265701b51586352b194d1788e8090ba6e1c1748728ecfb1b4fa3a6fe52141e",
 "12/2600/0":"687aad13dac4218e2da15a0e1be62cb8715ee58eaf328c806660f7b4629788c7",
 "12/8421/0":"538f76796f8ad460ef5dc918ed036c69fba0397def38049c87a96c43fb50bf3a",
 "12/3934/0":"d01768ce856cd4c30bf68c458ca9027aef33a5438a41aeff5865fafdb35e994a",
 "12/20528/0":"1eef4aebe5d025547e63139e86cb143a0b12adc857fe593e1314960654ef3927",
 "12/3927/0":"144c9eb7bf7c0c4267d515a32a5b3351fe39f1a1cea667fdd9e3920f2714f5f1",
 "12/8412/0":"241bd1602d238f623b9619080138ab03b6e9f869ed55d209e3720ceac9733e4b",
 "12/8301/0":"669a704dbefca98d40915ea87e33e727e0950fceb2c8d3cb5c8d6bde6546d8b7",
 "12/13509/0":"83d3f1866f34c4b1f2a4b762e901b147cc4385dcc9eec55c3dbcec89a0aa176b",
 "12/8304/0":"3bb411b7f3e3c7017225337635f496dcfb3d6bc63cbc52806c107267183ef066",
 "12/8387/0":"0ebaa4bb11c0563a017e984d7e132e5963b7cebb62ddb8a36c0269a4d717cf8b",
 "12/8390/0":"b56c70a8e5a1690d4120b9fb33eadd24ea09e0414e22617f66d57495f6a73fa1",
 "12/8391/0":"6999f80047152c63db33d8127a2bb68c871d918967f0d7bfed6cfe2b38a46800",
 "12/8074/0":"47ab86de46c862eabb5bc9bc8f3f6f7355354071c10ac06ccdf949abf8fd9c28",
}
def sha(data): return hashlib.sha256(data).hexdigest()
def component(interface, child):
    data = cache.archive(3, interface)[child]
    fmt, kind = data[0], data[1]
    p = 2
    if kind & 128: p = data.index(0, p) + 1
    values = struct.unpack_from(">HhhHHbbbbHB", data, p)
    names = ["contentType", "x", "y", "width", "height", "widthMode",
             "heightMode", "xMode", "yMode", "parent", "settings"]
    result = dict(zip(names, values))
    result.update(format=fmt, type=kind & 127, sha256=sha(data))
    result["hidden"] = bool(result["settings"] & 1)
    result["noClickThrough"] = bool(result["settings"] & 2)
    return result

def main():
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("--output", type=Path)
    args = ap.parse_args()
    assertions = []
    def check(condition, explanation):
        if not condition: raise AssertionError(explanation)
        assertions.append(explanation)
    for key, expected in PINS.items():
        index, group, file = map(int, key.split("/"))
        check(sha(cache.archive(index, group)[file]) == expected, "cache SHA " + key)
    slots = {str(key): {"struct": cache.enum(7716)[key],
                       "params": cache.params(cache.enum(7716)[key])}
             for key in (1006, 1007, 1047)}
    small = slots["1007"]["params"]
    large = slots["1047"]["params"]
    check(small[3503] == (1477<<16|732) and small[3505] == (1477<<16|735),
          "native small central key1007 owns wrapper732 and host735")
    check(slots["1047"]["struct"] == 40393 and large[3503] == (1477<<16|724)
          and large[3505] == (1477<<16|726) and large[3506] == (1477<<16|727),
          "native large central key1047 owns wrapper724, host726, overlay727")
    modal = cache.enum(7720)
    check(modal == {0:21330}, "small central size enum7720 has only preset0")
    check(cache.params(21330)[3638] == 370 and cache.params(21330)[3639] == 256,
          "small central preset0 is370x256, never800x484")
    components = {f"{i}:{c}":component(i,c) for i,c in
                  [(1477,27),(1477,722),(1477,724),(1477,725),(1477,726),
                   (1477,727),(1477,732),(1477,735),(37,17),(37,18),(37,41),(37,42)]}
    w = components["1477:724"]
    check([w[n] for n in ("width","height","widthMode","heightMode","xMode","yMode","parent")]
          == [800,600,0,0,1,1,722], "large wrapper is native800x600 centered")
    h = components["1477:726"]
    check([h[n] for n in ("width","height","widthMode","heightMode","parent")]
          == [0,0,1,1,724], "large host fills wrapper rather than fixed512px width")
    f = components["37:17"]
    check([f[n] for n in ("width","height","xMode","yMode")] == [800,484,1,1],
          "forge content remains fixed800x484 centered within its host")
    mapping = json.loads((ROOT/"protocol-analysis/ui-scripts-950-evidence.json").read_text())["opcodeMap947to950"]
    inverse = {int(v["opcode950"],16):int(k,16) for k,v in mapping.items()}
    scripts, programs = {}, {}
    for sid in SCRIPT_IDS:
        data = cache.archive(12,sid)[0]
        ins,tail = cs.decode(data,inverse)
        programs[sid] = cs.normalize(ins)
        scripts[str(sid)] = {"sha256":sha(data),
          "arguments":[int.from_bytes(tail[n:n+2],"big") for n in (10,12,14)],
          "instructions":[f"{i}: {line}" for i,line in enumerate(cs.lines(ins))]}
    def called(sid,target): return (0x895,target) in programs[sid]
    for caller,callee in ((2600,8421),(8421,3934),(8421,20528),(20528,3927),
                          (3927,8412),(8301,13509),(13509,8390),(8304,8387),
                          (8387,8390),(8390,8391),(1364,8074)):
        check(called(caller,callee),f"native call {caller}->{callee}")
    check(programs[8391][17:21] == [(0x35e,0),(0x511,(0,1007)),
                                   (0x647,1),(0x713,41)],
          "8391 specializes key1007 only, before generic native relayout")
    check(programs[8391][44:46] == [(0x511,(0,512)),(0x511,(0,334))],
          "8391 fallback resets small central to512x334 after drag")
    check((0x511,(0,1047)) in programs[3934] and (0x511,(0,3505)) in programs[3934],
          "frame mount discovery recognizes key1047 without server override")
    check(programs[8074][:3] == [(0x511,(0,40393)),(0x511,(0,3505)),(0x540,0)],
          "1364 large-modal helper resolves40393.host726")
    report = {"revision":950,"scope":"Read-only paired-cache Forge drag layout audit",
      "assertions":assertions,"pins":PINS,"slots":slots,
      "smallModalPresets":modal,"components":components,"scripts":scripts,
      "recommendedMount":{"root":1477,"wrapper":724,"host":726,"key":1047},
      "limits":["Component inspection decodes stable header only; format9/11 listeners are not decoded.",
                "Static cache checks establish layout contract; actual client title dragging requires live validation."]}
    if args.output:
        args.output.parent.mkdir(parents=True,exist_ok=True)
        args.output.write_text(json.dumps(report,indent=2)+"\n",encoding="utf-8")
    print(f"PASS: {len(assertions)} paired950 Forge drag checks; native large host1477:726, wrapper1477:724.")
if __name__ == "__main__": main()
