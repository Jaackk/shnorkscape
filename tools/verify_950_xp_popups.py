"""Read-only verification of the paired 950 XP popup binding and exact script program."""
from pathlib import Path
import hashlib, json, re, sys
sys.dont_write_bytecode = True
import cache950 as cache
import verify_950_ui_scripts as cs2
ROOT = Path(__file__).resolve().parents[1]
evidence = json.loads((ROOT / "protocol-analysis/xp-popups-950-evidence.json").read_text())
source = (ROOT / "Ataraxia950/game/com/rs/game/player/client/Native950XpDrops.java").read_text()
expected = [(row["index"], row["group"], row["file"], row["sha256"]) for row in evidence["bindings"]]
actual = [(int(i), int(g), int(f), digest) for i,g,f,digest in re.findall(r'new Pin\((\d+), (\d+), (\d+), "([a-f0-9]+)"\)', source)]
assert actual == expected
for i,g,f,digest in expected:
    assert hashlib.sha256(cache.archive(i,g)[f]).hexdigest() == digest, (i,g,f)
assert cache.enum(7716)[1026] == 30143
slot = cache.params(30143)
assert slot[3505] == (1477 << 16) | 668
assert slot[3503] == (1477 << 16) | 666
assert cache.archive(2,69)[228].hex() == "0100005e020f0f00"
assert len(cache.archive(3,1213)) == 97
mapping = json.loads((ROOT / "protocol-analysis/ui-scripts-950-evidence.json").read_text())["opcodeMap947to950"]
inverse = {int(row["opcode950"],16): int(op,16) for op,row in mapping.items()}
script = lambda sid: cs2.decode(cache.archive(12,sid)[0],inverse)[0]
assert cs2.normalize(script(5658)) == [(0x895,5659),(0x495,0)]
assert cs2.normalize(script(5661)) == [(0x35e,0),(0x511,(0,1)),(0x895,5662),(0x495,0)]
assert cs2.normalize(script(5662))[0] == (0x30,228 << 8)
assert (0x511,(0,(1477 << 16) | 637)) in cs2.normalize(script(5662))
assert (0x511,(2," xp ")) in cs2.normalize(script(5662))
wrapper=cache.archive(3,1477)[666]
assert [int.from_bytes(wrapper[n:n+2],"big",signed=n in (4,6)) for n in (4,6,8,10)] == [374,46,173,114]
assert list(wrapper[12:16]) == [0,0,0,0]
for sid in (11145,13268,2330,8707,8708):
    assert any(i==12 and g==sid and f==0 for i,g,f,_ in expected)
print("PASS: 134 paired950 XP popup pins, stat-delta renderer and exact cache-default resize layout")
