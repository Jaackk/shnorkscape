# NPC mask derivation from the native 950 client

2026-09-10. Source: `OpenNXT/data/clients/950/win64/original/rs2client.exe`, SHA-256
`fc7492548627a4068e88a7a45ba4f0be93004adf246bb4b174b24ae588768b36`.
Every address in this note is **950**, not an overlapping address from 947.

## Correction to the previous handoff

The previous claim that NPC animation bit 3 and hit bit 5 are absent is false.
`tools/dis950.py range 0x14012324b 0xc1a` reveals both tail blocks. The mask is reloaded
into **rsi**, so tests use `sil`, not the earlier `r12b`. Animation's smart readers
are **inlined**: counting calls to the shared smart reader cannot establish absence.
Do not treat a bounded `func` traversal or a scan for one mask register as exhaustive.

## Newly implemented blocks

### Animation: bit 3

- Test: `0x1401232c1 test sil,8`.
- Four independent smart2or4null readers: `0x1401232cb..0x1401234c6`.
- Each: leading byte <= 0x7f selects a BE u16 with 0x7fff -> -1. Otherwise read a
  BE u32 and clear bit 31. No selector-table transform affects these IDs.
- Delay: plain byte at `0x1401234e7`, selector zero at `0x140b55069`.
- Sink: actor virtual slot `+0x1d8`, `0x14012351b`, taking the array of four IDs and
  delay. This is the intentional animation sink, not the unrelated model override bit 20.
- Fixture (855,-1,-1,-1,0): `00 00 08 03 57 7f ff 7f ff 7f ff 00`.
  Previous 947 baseline: `00 00 80 03 57 7f ff 7f ff 7f ff 80`.

### Face entity: bit 1

- Test: `0x14012118f test r12b,2`.
- Reader `0x1401211a4..0x1401211ce`: three bytes `w0,w1,w2` become
  `w1<<16 | w0<<8 | w2`. Writer order is **middle, high, low**.
- High byte selects kind: 1 NPC, 2 player, 0xff/0x7f clear; the low u16 is the index.
- Target reference at actor `+0x1a8`, sink `0x1403c01a0`.
- Fixture NPC 0x1234: `00 00 02 12 01 34`; player 0xabcd: `00 00 02 ab 02 cd`.
- Existing tile facing remains bit 7, BE x and LE y, each encoded as 2*tile+1.

### Spotanim list: bit 24

- Test `0x1401227b3 bt r12,0x18`, block ends `0x140122d42`.
- Removal count is a plain u8, followed by signed BE shorts. -1 clears all.
  A clear-all jumps straight to the addition count without consuming remaining removal
  entries; the API requires -1 to be the sole removal. Removal IDs match **spot definition ids**, compared with spot object `+0x84`.
  They are not slots. Per-slot removal uses an addition whose definition ID is -1.
- Addition count uses selector 1 (`value+128`), table `0x140b603c8`.
- Per-addition selectors at `0x140b603c9`: `00 02 03 03 03`, reset for each addition:

| Field | Encoding | Reader |
|---|---|---|
| slot | plain u8 | 0x140122a30..0x140122a98 |
| definition id | ushort128, -1 -> 0xffff | 0x140122aa1 |
| packed height/delay | bytes `[v>>16,v>>24,v,v>>8]` | 0x140122abf -> 0x14010dd10 selector 3 |
| rotation | 128-value byte, low 3 bits retained | 0x140122af8..0x140122b6b |
| offsets | middle/high/low medium | 0x140122b95..0x140122c29 |

Packed value = height<<16 | delay. The client retains low 15 bits as delay and
arithmetic-shifts height into scene units. The API accepts height 0..32767 so this
remains nonnegative. Offsets = (x+1023) | ((y+1023)<<11), x/y in -1023..1024.
Unknown flag bits (packed bit 15, rotation bit 7, offset bit 22) remain zero.
Sink `0x1401284b0` receives slot, actor, effect placement and definition; its clear
branch (`0x1401286e7..0x1401286f5`) clears that slot if the definition is -1.

### Ordinary hits and hitbars: bit 5

- Test `0x140123836 test sil,0x20`.
- Hit count is a **negated** byte, `0x140123859`.
- Unsigned smart = one byte for 0..127, otherwise BE u16 minus 0x8000.
- Simple: smart type, smart damage, smart delay.
- Dual: smart 32767, smart type, smart damage, smart secondary type, smart secondary
  damage, smart delay.
- Untyped: smart 32766, **negated damage byte**, smart delay. Selector 2 at
  `0x140b603c1`; it is restored for every hit. Important: this differs from player hits.
- Hits sink `0x14037c0d0`, called at `0x140123af7`.
- Hitbar count is a plain byte, `0x140123b3d`.
- Bar: smart id, smart cycle. Cycle 32767 means remove, with no more fields.
- Otherwise: smart delay; first fill byte +128; second fill byte negated only if
  cycle != 0; signed-null smart size (-1..126 as value+1, otherwise BE value+32769).
- If size >= 0: first quantity plain, second quantity 128-minus only if cycle != 0.
- Cycle-zero aliases both second values to the first; the writer rejects different
  supplied second values rather than pretending they are transmitted.
- Bar update/remove sinks `0x14037c6f0` / `0x14037c670` at `0x140123d54` / `0x140123d83`.
- Fixture untyped damage 200/delay 1: `00 00 20 ff ff fe 38 01 00`.

## Complete consumption order of the ten implemented blocks

SAY -> COLOUR_TINT -> TRANSFORM -> FORCE_MOVEMENT -> NAME -> FACE_ENTITY ->
FACE_COORD -> SPOTANIMS -> ANIMATION -> HITS. This order is positional and must not
be replaced by mask-bit order or setter order. Header extension markers are still
4 / 13 / 23 / 27, with two skipped bytes before each NPC's header.

## Engine integration and remaining fences

`Native950EntityMasks.npcMasks` now forwards queued animations, explicit face targets,
ordinary damage and all four graphics slots. The graphics bridge reads explicit
Graphics getters, and passes -1 IDs as per-slot clears. Unknown graphics force/custom
flags and out-of-range fields are counted and refused. Damage retains the existing
engine conversion (divide by 10) and uses the untyped form; values over 255 are still
refused instead of truncating. This avoids inventing mappings for legacy hitmark IDs.

Hitbar wire encoding is now proven and byte-tested, but the engine bridge continues
to refuse legacy hitbar IDs until their 950 cache identity is established. Extended
hit bit 33, model overrides, and other blocks remain fenced. None of this proves
that every 910 animation/graphics definition id denotes the same content in 950.
These protocol and bridge tests do not substitute for a live combat/effect check.

Validation: dedicated literal fixtures in `Native950NpcMasksTest` and real engine
state-to-wire fixtures in `Native950NpcMaskBridgeTest`. No fixture is produced by
calling the writer to obtain the expected bytes.

Focused validation: 23 tests passed (18 mask fixtures + 5 engine bridge tests), 0 skipped.
