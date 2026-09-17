# P6 area B: the multi-NPC NPC_INFO encoder

Replaces the three literal single-entity writers (`Native947Packets.staticNpcAdd`,
`singleNpcRetain`, `singleNpcRemove`) and the single-banker `Native947NpcView`
with an N-NPC encoder plus a per-session viewport.

| File | Layer | Owns |
|---|---|---|
| `network/com/rs/network/protocol/modern947/Native947NpcInfo.java` | protocol | one viewer's retained list and the whole bitstream |
| `game/com/rs/game/player/client/Native947NpcViewport.java` | game | turning world `NPC`s into encoder records, per-viewer identities |
| `tests/modern947/Native947NpcInfoTest.java` | test | 19 literal-byte fixtures, cache free |

Every width and form below is quoted from
`OpenNXT/data/prot/947/generated/native947-3/verified/NPC_INFO.md` and
`NPC_INFO_MASKS.md`. Nothing comes from the 910 encoder or from RS knowledge.

## Body order

1. unsigned 8-bit retained count (`0x14011F404`)
2. one entry per retained NPC, in the client's list order: a changed bit and,
   when set, a 2-bit selector - `00` mask only, `01` walk (3-bit direction, then
   the pending-mask bit), `10` run or single step (a two-step flag, then one or
   two 3-bit directions, then the shared pending-mask bit), `11` remove
3. addition records `index16 / dy[npcBits] / plane2 / dx[npcBits] / type16 /
   teleport1 / facing3 / mask1` (`0x14011F860`)
4. the 16-bit 65535 terminator, **only when a mask section follows**
5. byte alignment (`0x14011F0D7`)
6. one mask block per pending NPC, each preceded by the two bytes the client
   skips at `0x14011F11B`, in retained-list order then addition order

## Three decisions that are not in the 910 encoder

### The terminator is conditional

The addition reader reads another 16-bit index only while at least 16 bits remain
in the whole buffer (`0x14011F88B`). After byte alignment at most seven padding
bits remain, so a body with no mask section needs no terminator: that is exactly
why NPC_INFO.md can publish `01 00` and `00` as complete bodies. The shipped
`staticNpcAdd` writes one because it sized its buffer to include it; the general
encoder writes it if and only if a mask block follows, which is the first moment
16 or more bits can sit in front of the reader. `Native947NpcInfoTest.
firstAdditionMatchesTheEvidenceRecordWithoutAnUnneededTerminator` asserts that the
56 record bits are byte identical to the published
`00 00 01 FA 02 01 EE 87 FF F8` fixture and that only the terminator differs.

### Removals compact the client's list

`0x14011F44D` resets the client's retained count to zero and each kept entry is
re-appended at `0x14011F52E` / `0x14011F56C` / `0x14011F5A8` / `0x14011F61B`;
a selector-11 entry is pushed to the removal queue `mgr+0xB0A8`
(`0x14011F68C`) and is **not** re-appended, and additions append after the
survivors (`0x14011FB7B..0x14011FB95`). So removing the middle of a list compacts
it and preserves the survivors' relative order. `Native947NpcInfo.localIndices()`
mirrors that list exactly; if the two ever diverge the retained count is wrong and
every following entry in the frame desynchronises.

A retained count larger than the client's previous one is invalid, and a smaller
one queues every old trailing entry for removal
(`0x14011F410..0x14011F445`). The encoder only ever sends its own list size, and
that size only grows through additions the same packet just made, so both rules
hold by construction.

### No retained teleport form, so a jump is remove-then-defer

Section 1 of NPC_INFO_MASKS.md: the only retained forms are mask-only, one step,
one or two steps, and remove. An NPC that teleports, changes plane, leaves the
view distance, or is a different NPC reusing a known world index is therefore
removed with selector 11 - and its index is quarantined for that one frame, so
the re-addition lands on the next tick. Mixing a queued removal with a same-index
addition inside one packet is not something the evidence blesses, and the drain
order of `mgr+0xB0A8` versus the addition loop was not established, so the
encoder fails closed.

The one exception the evidence does bless is the scene rebuild: NPC_INFO.md
states that a zero-retained-count add is safe after either kind of rebuild, so
`rebuilt = true` clears the view and republishes every visible NPC in the same
frame, with the shrinking count doing the removals.

## Index authority and reuse

NPCs are world owned and the encoder reports whatever index the world gave them,
unchanged - `EntityList.add` hands out 1-based indices and reuses the lowest free
one. Each record therefore carries an `identity` alongside the index; the encoder
stores the identity it saw when it added the NPC and treats a changed identity at
a known index as a removal. `Native947NpcViewport` mints identities from a
per-viewport `IdentityHashMap<NPC, Long>` that is pruned to the current candidate
set each frame. Index 65535 is the addition terminator, so it is rejected as a
world index.

## Direction table

The NPC table (`0x14011F6D0`, `0x14011F750..0x14011F816`) is clockwise from
north: `0:(0,+1) 1:(+1,+1) 2:(+1,0) 3:(+1,-1) 4:(0,-1) 5:(-1,-1) 6:(-1,0)
7:(-1,+1)`. It is **not** the player jump table `0x140125FB8`. The encoder never
shares one table: the viewport derives every selector from the tile delta through
`Native947NpcMasks.direction(dx, dy)`, so a wrong 910 direction index throws here
instead of misplacing the NPC on the client. The eight walk bodies are asserted
literally as `01 A0 / A4 / A8 / AC / B0 / B4 / B8 / BC`, of which NPC_INFO_MASKS.md
publishes north (`01 A0`) and east (`01 A8`).

## Offsets

Addition offsets are signed inside the scene's `npcBits` (sign extension at
`0x14011FC95..0x14011FCD2`, width from NPC manager `+0xC0E8`, assigned by
`REBUILD_NORMAL` at `0x1400F7461`), and they are added to a base the client
derives from the local player (`0x14011FEF5` / `0x14011FF01`).

**That base is the local actor's LAST QUEUED PATH STEP, not simply its current
tile.** `0x14011FE69` loads the local actor's path queue and
`0x14011FE70`/`0x14011FE74` branch on its length: with at least one queued step
the base is read from that step (`[rdx-0x14]` / `[rdx-0xC]`,
`0x14011FE9B..0x14011FEB9`); only an empty queue falls through to the virtual
position call at `0x14011FE76..0x14011FE95`. This is what NPC_INFO.md means by
"the local actor's most recent path position".

The encoder passes the viewer's post-movement tile, and the two values coincide
**only** because `Native947PlayerInfo` writes every local move with the verified
teleport form, which leaves the client's path queue empty. The moment PLAYER_INFO
emits the local walk (type 1) or run (type 2) forms, the NPC_INFO addition base
must move to the queued destination tile in the same change, or every addition in
that frame lands on the wrong tile. The P4/M4 backlog rows for the local walk/run
forms carry the same cross-reference.

Two independent gates apply:

- the view gate: same plane and `|dx|, |dy| <= viewDistance` (default 15, the
  distance the single-banker view used);
- the width gate: `-(1 << (npcBits-1)) <= offset < (1 << (npcBits-1))`.

An NPC inside the view but outside the width is a counted `widthDrops()`, never a
truncated record. `offsetsAtTheSignedLimitsOfNpcBitsEncodeAndBeyondThemAreDropped`
pins width 7 at `dx = 63, dy = -64` and width 5 at `dx = -16, dy = 15`.

## Masks fail closed

Blocks are composed only through `Native947NpcMasks.Update`, which has builders
for the seven CONFIRMED bits (7 animation, 12 force movement, 28 colour tint,
3 face coordinate, 5 transform, 0 say, 18 name) and refuses bit 1 (hits and hit
bars, layout still CANDIDATE) and has no builder at all for the REFUTED bit 25
graphics block. `NpcState.Builder` exposes exactly one `update` method and it
takes an `Update`: there is no raw-bytes entry point, which is asserted by
reflection in `candidateMasksCannotReachTheEncoder`. `Native947NpcViewport`
sends no mask unless a `MaskSource` is installed, and the default source returns
null - the same silence the single-banker view kept.

The 3-bit `facing` field of the addition record is written because it is part of
the record, but only its width is verified (`0x14011FC2A`), not its mapping, so
the viewport always writes 0. Probing it is an M4 work item.

## What area C still has to wire

- `Native947Session` should hold a `Native947NpcViewport` per session instead of
  a `Native947NpcView`, call `synchronize(player, channel, scene.npcBits, rebuilt)`
  after `PLAYER_INFO` for the same tick (the offsets need the post-movement tile),
  and put the viewport's `State` into `Snapshot`.
- `Native947Interactions` reaches into `Native947NpcView.npc` for the banker; it
  needs a world lookup by index plus `Native947NpcViewport` visibility instead.
- `Native947Packets.staticNpcAdd` / `singleNpcRetain` / `singleNpcRemove` become
  dead once the viewport is wired. `Native947NpcInfoTest` deliberately asserts the
  new encoder reproduces `singleNpcRetain` (`01 00`), `singleNpcRemove` (`00`) and
  the `staticNpcAdd` record bits, so those fixtures survive the deletion.
- The viewport needs the world to allow more than one NPC and to keep NPC region
  indexes current (`World.addNative947Npc`, `Region.addNPCIndex`), and
  `Native947NpcViewport.nearbyNpcs` reads `Region.getNPCsIndexes()`, so region
  registration has to happen before the first frame.
