# 950 player appearance — verified wire format

All addresses are VAs in `OpenNXT/data/clients/950/win64/original/rs2client.exe` (image base
0x140000000). Reproduce any line with
`<AstraNXT .venv python> tools/dis950.py range|func|bytes <VA> ...`, `--rev 947` for the reference
client. Nothing in AstraNXT was modified; its binary and venv were read only.

## How the crash was located

The client posts its own fault report to `/nxtclienterror.ws`; the server persists it under
`OpenNXT/data/debug/clienterror/`. The base64 `data=` field is
`<message>\0 <count> <pad> <count x 8-byte big-endian RVA>`. The world-entry report decodes to
`Main: access violation reading (0000000000000218)` with the stack

    0x14001b750 0x140131d53 0x140131529 0x14012dd07 0x1401436d8 0x1400ea2a2 ...

i.e. packet dispatch -> PLAYER_INFO(36) `0x1401436d8` -> appearance decode `0x14012dd07`
-> `0x140131529` -> fault at `0x140131d53`.

## The faulting instruction

    140131cd6  sub    r9d, edx              ; itemId = varint - 0x800
    140131d31  call   qword ptr [rax+0x38]  ; definition lookup
    140131d39  lea    rsi, [rip+0xd9efe0]   ; static fallback, reached on two id-independent paths
    140131d4f  mov    rax, qword ptr [rsi+8]
    140131d53  movzx  ecx, byte ptr [rax+0x218]   ; FAULT, rax == 0

There is no null check on any path into `0x140131d53`. **Any wearpos slot value >= 0x800 kills the
client here, whether or not the item id is valid.**

## Two 947 -> 950 changes, both required

### 1. The appearance body is biased by 0x80 on the wire

    14012dc3b  lea   rax, [rip+0xa32287]   ; -> .rdata 0x140B5FEC9, holds 0x02
    14012dc46  mov   [rbp-0x68], rax       ; transform-script cursor
    14012dc5a  movzx eax, byte [r8+rbx]    ; length byte, read inline
    14012dc65  add   al, 0x80              ; server writes (len+128) & 0xFF  -- unchanged from 947
    14012dc81  call  0x14010ddf0           ; body copy, variant from the script byte

`0x14010ddf0` selects: 0 = plain memcpy (`0x14010df09`), 1 = reversed, 2 = forward with +0x80 per
byte (`0x14010de7c`, the add is at `0x14010dea4`), 3 = reversed with +0x80.

950 installs **0x02**; 947 installs **0x00** at `0x140B91BAD` (`0x14012c3ae lea rax,[rip+0xa657f8]`).
So the 950 server must send every body byte as `b xor 0x80`, forward order. 947 sends it verbatim.

The appearance mask bit also moved: 947 `0x14012c3a2 test r10b, 4`, 950 `0x14012dc31 test r15b, 0x20`.

### 2. Each wearpos slot became an unsigned LEB128 varint

    140131c74  movzx edx, byte [r10+rax]
    140131c7b  and   eax, 0x7f
    140131c7e  shl   rax, cl          ; low 7-bit group first
    140131c81  add   ecx, 7
    140131c84  or    r9, rax
    140131c87  cmp   rdx, 0x7f
    140131c8b  ja    0x140131c60      ; continue while byte > 0x7F

Constructor `0x140131a70` installs the classification constants:

| Field | 950 | 947 (`0x140130e8a`, `0x140130e90`) | Meaning |
|---|---|---|---|
| `[obj+0x00]` | 0 | kit base 0x100 | 950: empty-slot sentinel |
| `[obj+0x04]` | 1 | item base 0x800 | 950: npc-morph escape, wearpos index 0 only |
| `[obj+0x08]` | 2 | -- | 950 kit base: `kitId = varint - 2` |
| `[obj+0x0c]` | 0x800 | -- | 950 item base: `itemId = varint - 0x800` |

947 reads one byte (0 = empty) or a big-endian unsigned short, kit base 0x100, item base 0x800,
morph sentinel 0xFFFF (`0x140131027`-`0x14013108c`, `0x140131130`).

**The item base 0x800 is NOT new to 950** — both clients use it. What changed is the framing, the
kit base and the morph sentinel. (The 947 server's disabled item branch wrote `16385 + id`, which
was wrong for 947 too.)

## Why the old server crashed, deterministically

Equipment starts at body offset 2. The old server's first five equipment bytes were
`00 00 00 00 01` (four empty slots, then the high byte of the `0x0103` short). The client adds
0x80 to each: `80 80 80 80 81` — every one has the continuation bit set, so the varint keeps
consuming. It only terminates on the `0x8B` of the trailing `short 2699` (`0x8B xor 0x80 = 0x0B`).
The resulting value is astronomically larger than 0x800, so the item branch is forced and
`0x140131d53` faults. Independent of name, colours or idkit values.

## Unchanged, verified

- Frame `C0 7F F4 00 00 20 <len+128>` is correct. Mask reader `0x14012c880` gates its continuation
  bytes on `0x10` / bit15 / bit18, so a lone `0x20` is a valid single-byte mask.
- Tail after the slot list is byte-identical to 947: big-endian u16 per-slot bitmask, 10 colours
  (`0x1401320a5`, count 0xa), 10 further bytes (`0x1401321a2`, count 0xa), signed big-endian i16
  (`0x140132241`-`0x140132272`), NUL-terminated name (`0x140131550`-`0x140131572`), combat byte
  (`0x14013165b`), two level bytes (`0x1401316af`, `0x1401316bb`), trailer flag (`0x1401316d7`).
- Wearpos table `cache/28/6.dat` decodes to
  `[0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,1,0]` — 16 transmitted slots. The server's `!= 0` skip and the
  client's `== 1` skip (`0x140131c45`) select the same set for this cache.

## Verified wire vectors (player "x", default kit)

Body, 48 bytes:

    00 00 | 00 00 00 00 14 00 1C 28 05 24 2C 10 00 00 00 00 | 00 00
    03 10 10 00 00 00 00 00 00 00 | 00 x10 | 0A 8B | 78 00 | 03 00 FF 00

On the wire (xor 0x80 applied to the body only):

    c07ff4000020b0 80808080808094809ca885a4ac9080808080808083909080808080
    808080808080808080808080808a0bf88083807f80

Decoding that with the client's own rules yields render 0; slots kit 18/26/38/3/34/42/14 in
wearpos 4/6/7/8/9/10/11 and the rest empty; bitmask 0; colours [3,16,16,0...]; short 2699;
name "x"; combat 3; levels (0,-1); trailer 0 — consuming exactly 48 of 48 bytes.

## Confirmed live

2026-09-08: with both changes applied the 950 client rendered the world on the first attempt. No
`nxtclienterror` report, and the channel stayed open past `native950-world-root`. The loading
overlay cleared with the interface stage left enabled, so the `[ifmgr+0x189]` concern below did not
materialise in practice.

## Still unresolved

- Whether a top-level interface blocks the loading overlay. The gate at `0x14019779d` short
  circuits to the clear at `0x14019783c` when `[ifmgr+0x160] == 0` (no top interface), but with one
  open it also tests `[ifmgr+0x189]` at `0x1401977e8`, and the only observed set of that byte is a
  constructor (`0x14019c8a1`), with clears at `0x14019d6b5`/`0x14019d6f2`. Strong hypothesis, not
  proven — test by bootstrapping the world with no interfaces first.
- REBUILD_NORMAL chunkX/chunkY may be swapped; invisible at the 3222,3222 spawn.
- The two bytes skipped per updated player at `0x1401436c4` have no located producer.
