# 950 player update blocks: native derivation

Date: 2026-09-10. Source: the isolated original WIN64 revision 950 executable at
`OpenNXT/data/clients/950/win64/original/rs2client.exe`, SHA-256
`fc7492548627a4068e88a7a45ba4f0be93004adf246bb4b174b24ae588768b36`.

This derivation replaces all six remaining inherited 947 writer bodies in
`Native950PlayerMasks`. The existing four typed blocks (appearance, face angle, overhead text,
local overhead text) remain unchanged. All ten blocks exposed by that typed API are now encodable.
This does **not** claim that every mask accepted by the 950 client is implemented. Arbitrary raw
masks and the legacy raw head-icon API still throw.

## Reproducing the evidence

Use `tools/dis950.py` with a Python environment containing capstone and pefile; it reads the
original executable without patching it. For example:

```
python -B tools/dis950.py range 0x14012c880 0x700
python -B tools/dis950.py range 0x14012d136 0x720
python -B tools/dis950.py range 0x14012dd7e 0x500
python -B tools/dis950.py range 0x14010d850 0x600
python -B tools/dis950.py bytes 0x140b5fea0 0x50
```

The parser starts at `0x14012C900`; mask-byte reader `0x14012C880` uses markers `0x10`,
`0x8000`, `0x40000`. The encoder's supported consumption order is:

1. Spotanims (`0x4000000`)
2. Animation (`0x8`)
3. Face entity (`0x80`)
4. Hits/hitbars (`0x40`)
5. Overhead text (`0x400000`)
6. Appearance (`0x20`)
7. Forced movement (`0x1`)
8. Colour overlay (`0x200000`)
9. Local overhead text (`0x400`)
10. Face angle (`0x2`)

Other client blocks occur between these tests, but their bits are not set by this API.

## Byte-transform helpers

The parser uses a pointer at packet-reader `+0x28` into immutable transform selector bytes.
Those selector bytes are client-side constants, **not bytes transmitted on the wire**. Each
transformed helper advances the selector pointer by one. Plain count/BE-short helpers do not.

- `0x14010D850`: selector 0 reads byte `w`; 1 reads `w-128`; 2 reads `-w`; 3 reads `128-w`.
- `0x14010D970`: selector 0 reads BE u16; 1 LE u16; 2 BE with low byte minus 128;
  3 LE with low byte minus 128.
- `0x14010DD10`: selector 1 reads LE i32. Selector 0 is BE (the rotate/XOR sequence in related
  helpers is a byte swap, not a third wire endianness).
- `0x14010DB50`: selector 1 reads LE u24 (`w0 + (w1<<8) + (w2<<16)`).
- `0x1400B5EB0`: plain byte, no transform-selector advance.
- `0x14010D6F0`: plain BE short, no transform-selector advance.

All byte arithmetic below is modulo 256. `LE+128` means little-endian short whose low wire
byte is increased by 128.

## Animation: bit 0x8

Test at `0x14012CBC3`, reads `0x14012CBCD..0x14012CDDD`, virtual animation sink at
`0x14012CDEF` (`vtable+0x1D8`). Exactly four sequence slots, then a **plain** delay byte.

Each slot is an unchanged BE big-smart: first byte >127 selects BE u32 with bit31 cleared;
otherwise BE u16, where 32767 means -1. The fourth slot ends at `0x14012CDB4`; the delay is
loaded with plain `movzx r8d, byte ptr [r8+r10]` at `0x14012CDDD`. The old +128 delay bias
must be removed.

Fixture, sequence 855, other slots -1, delay 0:
`08 03 57 7f ff 7f ff 7f ff 00`.

## Face entity: bit 0x80

Reads `0x14012CE32..0x14012CE5F`: first wire byte is low, last is high. The high logical byte
selects NPC=1 (`0x14012CFA9`), player=2 (`0x14012CF91`), or clear=255/127
(`0x14012CE7E`). NPC/player sinks call `0x1403C01A0` with the low u16 index and kind.

This is **LE u24**, previously BE. Player index 5: `80 05 00 02`; NPC 1234:
`80 d2 04 01`; clear: `80 ff ff ff`.

## Forced movement: bit 0x1

Reads `0x14012DDA0..0x14012DE78`, sink `0x14031DAE0` called at `0x14012DEDC`.
The six coordinates retain their argument positions: dx1, dy1, dx2, dy2, plane1, plane2.
The six direct reads are respectively `128-w`, plain signed byte, `-w`, `w+128`, `-w`,
`128-w`. Their inverse server bytes are:

| Field | Server wire |
|---|---|
| deltaX1 | 128 - value |
| deltaY1 | value |
| deltaX2 | -value |
| deltaY2 | value + 128 |
| planeDelta1 | -value |
| planeDelta2 | 128 - value |
| arriveTick1 | LE u16 |
| arriveTick2 | BE u16 |
| direction | LE u16 |

The last direct read leaves the selector pointer at `0x140B5FEAE`; bytes `01 00 01`
select LE/BE/LE for the calls at `0x14012DE52`, `0x14012DE60`, `0x14012DE73`.
Direction is masked to 14 bits and converted to an angle before reaching the sink.

Asymmetric fixture (-12,23,-34,45,-1,2; 0x1234,0x5678,0x2345):
`01 8c 17 22 ad 01 7e 34 12 56 78 45 23`.

## Colour overlay: bit 0x200000

Reads `0x14012E0E5..0x14012E185`. Hue is plain; saturation is `128-w`; lightness is plain
(masked to 127); strength is `128-w`. The client forms the HSL index at
`0x14012E1B8..0x14012E1D3` and stores the colour and alpha in actor+0x190..0x19c.
After the four bytes, selector pointer `0x140B5FEB8` contains `03 00`: the start offset is
LE+128 u16, the end offset BE u16. Both offsets are added independently to current tick;
the second is **not** a duration added to the first. Existing API name `duration` is retained
for compatibility, but its Javadoc now identifies the end-offset meaning.

Fixture (hue17,sat5,light99,strength200,start0x1234,end0x4567):
`10 80 20 11 7b 63 b8 b4 12 45 67`.

## Spotanim list: bit 0x4000000

Reads `0x14012C9BB..0x14012CB48`. Plain removal count followed by signed BE short ids;
-1 clears all (calls `0x140320EF0` and `0x140320F40`). The clear-all branch at
`0x14012C9FE` skips the rest of the removal loop and falls through to the addition-count read
at `0x14012CA18`. Therefore clear-all must be the only removal in this API; it may still be
followed by additions. This prevents trailing removal bytes being consumed as addition records.
Plain addition count. For each
addition the transform script at `0x140B5FEE0` is **03 03 01 00 01**:

| Field | Server wire |
|---|---|
| slot | 128 - slot |
| spotanim id (-1 becomes 65535) | LE+128 u16 |
| packed height/flag/delay | LE i32 |
| rotation and flag | plain byte |
| packed x/y offsets and flag | LE u24 |

The script is reset after **each addition** (`0x14012CB26`), not consumed continuously across
records. The existing packed fields are confirmed: high16 height, bit15 flag, low15 delay;
rotation low3 and flag bit7. `0x1400E30D0` decodes offsets as low11 -1023, next11 -1023,
flag bit22. Main sink is `0x140128720`, called at `0x14012CB21`.

Fixture (no removals; slot0,id1234,delay0,height100,rotation0,offsets0/0):
`10 80 04 04 00 01 80 52 04 00 00 64 00 00 ff fb 1f`.

Tests include two additions with asymmetric values and flags, verifying the script resets,
plus -1 id, extreme offsets, signed removal ids, and four-byte header markers.

## Hits and hitbars: bit 0x40

Read range `0x14012D140..0x14012D844`. Ordinary, dual (32767 marker) and untyped (32766 marker)
hit forms retain their smart layout. A smart is one byte below128; otherwise BE u16 with
0x8000 set. Untyped damage uses a plain byte: selector pointer `0x140B5FED5` contains zero,
tested at `0x14012D31D`. The pointer is reset per hit at `0x14012D4B4`.
Hit sink `0x14037C0D0`, called at `0x14012D498`.

Hit count reads `w+128`, so server count+128. Hitbar count at `0x14012D4D6` reads **-w**, so
server sends negated count (the old bias was wrong). Each bar is:

- id smart, cycle smart; cycle32767 removes immediately without further bytes.
- delay smart, first percent **negated byte**.
- second percent only if cycle !=0: **128 minus byte**.
- signed size smart: first byte below128 becomes value-1; otherwise BE u16 +32767,
  interpreted as signed16. Server emits size+1 for -1..126, otherwise size-32767 as u16.
- if size >-1, first quantity **value+128** on wire; same transform for second quantity,
  which appears only if cycle !=0. With zero cycle, the client reuses each first value.

Direct instruction evidence: percent1 `0x14012D5CA`, percent2 `0x14012D5ED`,
size `0x14012D5F7..0x14012D636`, quantities `0x14012D664` and `0x14012D68F`.
Bar sink `0x14037C6F0` called at `0x14012D773`; removal path starts `0x14012D789`.

Fixtures:

- type132 damage25 delay0, no bars: `40 81 80 84 19 00 00`.
- no hits; bar0,cycle1,delay0,percent100/50,no size: `40 80 ff 00 01 00 9c 4e 00`.
- remove bar0: `40 80 ff 00 ff ff`.

## Validation and remaining integration

`Native950PlayerMasksTest` contains 25 tests. The expected byte strings are literals derived
from the readers above, not snapshots captured from the encoder. Coverage includes asymmetric
fields, smart boundaries, missing optional fields, count transforms, multiple list records,
all ten blocks combined in native consumption order, raw-mask rejection, existing appearance
framing, and the real ISAAC transport on an embedded channel.

Focused JDK8 compilation and JUnit execution passed all 24 initial tests; the added clear-all regression is awaiting the full project run. Full-project build/deploy and
live exercise are the root agent's responsibility; this change itself does not restart anything.

The entity bridge currently sources animation, face entity, untyped hits, face angle, and
force talk. It deliberately drops hitbar ids pending 950 cache mapping and does not yet source
player graphics, colour overlays or forced movement. These writer derivations enable those
bridges, but do not by themselves prove the 910 content ids or source-unit conversions.
