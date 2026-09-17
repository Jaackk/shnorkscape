# P6 Area A - generalised PLAYER_INFO

Scope: the multi-player PLAYER_INFO encoder and the per-session view state. NPC_INFO (Area B),
the world-phase tick, N-slot admission and the session/world wiring (Area C) are not here.

Files:

- `network/com/rs/network/protocol/modern947/Native947PlayerInfo.java` - the encoder plus its
  `Actor` snapshot type and `ViewState`.
- `game/com/rs/game/player/client/Native947Viewport.java` - the world-thread-owned per-session
  holder of that state, with the ownership check and the counters.
- `tests/modern947/Native947PlayerInfoTest.java` - 21 cache-free byte tests.

Nothing under `network/.../Native947Packets.java` or `Native947PlayerMasks.java` was modified;
both are byte-identical to the baseline mirror. The encoder composes every mask block through
`Native947PlayerMasks`, so the confirmed-mask allow-list, the header extension markers and the
client consumption order stay in one place.

## Evidence used, field by field

| Wire element | Bits | Evidence |
|---|---:|---|
| Pass order: local skip-clear, local skip-set, external skip-set, external skip-clear | - | `PLAYER_INFO.md` "Movement prefix"; `player-movement-passes.disassembly.txt` |
| Pass alignment (each pass rounds back up to a byte boundary) | - | `PLAYER_INFO.md` "Movement prefix" |
| Skip selector 0/1/2/3 -> 0/5/8/11 count bits, counting subsequent eligible slots | 2 + 0/5/8/11 | `PLAYER_INFO.md` "Movement prefix" |
| Local: pending-mask bit, then 2 movement-type bits | 1 + 2 | `0x14012589F` (1 bit), `0x1401258C5` (2 bits) |
| Local type 0 + mask bit set = mask-only | - | `0x1401258E9` |
| Local type 0 + mask bit clear = remove, then 1 bit, then the external region-update parser | 1 | `0x140125906` destroy, `0x1401259A3` flag, `0x1401259CF` tail-jump to `0x140125FE0` |
| Local type 1 (walk): 3 direction bits then the extra-step flag | 2 + 3 + 1 | `0x1401259E2` selector, `0x1401259E8` direction, `0x1401259F8` flag; table in `player-direction-jumptable.txt` (M4) |
| Local type 2 (run): 4 direction bits | 2 + 4 | `0x140125BA8` selector, `0x140125BB1` direction; table in `MOVEMENT_TABLES.md` section 1 (M4) |
| Local type 3 (teleport) short form: 15 bits `y | x<<5 | plane<<10 | speed<<12` | 1 + 15 | `0x140125D42` selector, `0x140125D57` 15 bits, decode at `0x140125D68..0x140125DA4` |
| Local type 3 long form: 3 speed bits + 30 position bits | 1 + 3 + 30 | `0x140125E89`, `0x140125E9C` |
| External type 0 (add): optional region update, 6 X bits, 6 Y bits, pending-mask bit | 2 + 1 + 6 + 6 + 1 | `0x140126000`, `0x140126018`, `0x14012603A`, `0x140126047`, `0x140126056` |
| Region update type 1: 2 plane bits | 2 + 2 | `0x14012631B` |
| Region update type 2: 5 bits `plane<<3 | opcode`, opcode 0=(-1,-1) .. 7=(+1,+1) | 2 + 5 | `0x140126349`, deltas at `0x14012638E..0x1401263F5` |
| Region update type 3: 20 bits `y | x<<8 | plane<<16 | speed<<18` | 2 + 20 | `0x14012641F`, decode at `0x140126429..0x140126454` |
| Init: 30-bit tile hash then 2046 20-bit records | 30 + 2046x20 | `PLAYER_INFO.md`; `player-init.disassembly.txt` `0x140125179` reads 0x14 bits |
| Init record: bits 0..17 = 910 region hash, bits 18..19 = movement-speed table index | 20 | `0x140125190` (`(v>>18)&3` indexes `0x140C9C888`), `0x140125280` rebuilds the tile from the low 18 bits |
| Two skipped bytes before each pending player's mask block | 16 | `PLAYER_INFO.md`; `0x1401065C4` |
| Mask header markers 0x1 / 0x4000 / 0x40000 | - | `PLAYER_INFO.md` header table; applied by `Native947PlayerMasks.writeHeader` |
| Appearance: mask 0x4, length `(N+128)&255`, body untransformed | - | `PLAYER_INFO.md` "Appearance: mask 0x4"; applied by `Native947PlayerMasks` |

The 3-bit speed field of the long teleport form and the 2-bit field of the init/region-hash
records index the **same** client table: the local parser computes it at `0x140125D68`
(`0x140125D61 + 7 + 0xB76B20 = 0x140C9C888`), the init parser at `0x140125190`
(`0x140125189 + 7 + 0xB776F8 = 0x140C9C888`), and the external type-3 parser at `0x140126454`.
That is why one `Actor.movementType` feeds all three, exactly as the 910 encoder does
(`getMovementType()` for the region-hash form, `hasTeleported() ? 4 : getMovementType()` for the
local form, truncated to two bits where the field is two bits wide).

Empirical anchor for the values: the shipped, client-verified
`Native947Packets.singlePlayerWalkStep` carries movement type **2** in the 15-bit field for a
walk, and the shipped `initialSinglePlayerScene` carries **3** in every empty slot's top two bits
(`0xC0000`). Both are reproduced byte for byte by the new encoder, so
`MOVEMENT_WALK = 2`, `MOVEMENT_RUN = 3`, `MOVEMENT_TELEPORT = 4` and
`EMPTY_SLOT_SPEED = 3` are the constants this port uses.

## What changed against the 910 encoder

1. **Init record 18 -> 20 bits.** `LocalPlayerUpdate.init` writes `writeBits(18, regionHash)`.
   The 947 parser reads 20 and takes the top two as the speed index. A live external slot now
   publishes `regionHash | (movementType & 3) << 18`; an empty slot publishes
   `EMPTY_SLOT_SPEED << 18`, which is what the shipped writer already emitted for every slot.
2. **Local player index is no longer hard-coded.** `initialScene` skips whatever index the world
   assigned. The record count is always 2046, so the stream length is unchanged.
3. **Mask header.** 910 writes `short(0)` then markers 0x40 / 0x1000. The 947 header uses
   0x1 / 0x4000 / 0x40000 and old APPEARANCE=0x1 is now an extension marker. The two zero bytes
   are still written, because the 947 dispatch skips exactly two bytes per pending player.
4. **Block order.** 910 writes blocks in bit order. The 947 parser reads them in a fixed
   consumption order that is not bit order; `Native947PlayerMasks.encode` owns that order.
5. **Appearance transform.** 910 writes `writeByteC(length)` and reversed bytes; 947 writes
   `(N + 128) & 255` and the body in normal order.
6. **Mask allow-list.** Only masks marked CONFIRMED in `PLAYER_INFO_MASKS.md` can be encoded at
   all; a CANDIDATE one throws `UnsupportedOperationException` naming the evidence gap. Today the
   session only feeds appearance, which is the P6 starting allow-list; face angle, face entity,
   animation, force talk, hits and colour are already encodable and can be turned on one at a
   time from the game side without touching this class.
7. **Appearance cache cleared on remove.** 910 keeps `cachedAppearencesHashes[index]` across a
   removal. The 947 client destroys the player-slot object when it parses a remove
   (`0x140125906`), and its appearance cache lives on that object, so this encoder forgets the
   hash on remove and always resends the body on a re-add. Emitting one extra appearance block is
   cheap; a re-add with no appearance would risk an unrendered player.
8. **External region-hash-only updates are still not emitted.** They are legal (external types
   1/2/3 outside an add) and OpenNXT emits them, but 910 does not, and the add path already
   compares the stored hash and writes the delta when it differs. Fewer bytes, one less form to
   get wrong. The skip-run scan therefore breaks only on `needsAdd`, never on a hash mismatch.
9. **`MAX_PLAYER_ADD` is reset once per frame**, not once per external pass as in 910 (which
   resets it at the top of both `processOutsidePlayers` calls and so admits up to 100).

## What is deliberately NOT emitted

- ~~**Walk (type 1) and run (type 2) local forms.**~~ **SUPERSEDED by M4 Area A, 2026-09-07.**
  Both compact forms are now emitted; the 4-bit run table was dumped in
  `verified/MOVEMENT_TABLES.md` section 1 (a literal `cmp`/`jne` chain at
  `0x140125C0C..0x140125CFA`, all 16 arms in the instruction stream) and both tables were
  cross-checked against 910's own `Utils.getPlayerWalkingDirection` /
  `getPlayerRunningDirection`. See `notes/M4-movement.md` for the emit rule, the per-slot
  speed-token model that gates it, and the cases that still fall back to the absolute form.

  The paragraph below is kept because it is still true of the fallback form and explains why
  players walked correctly before the compact forms existed:

  **Do not read "teleport form" as "the character teleports on screen".** The form is only
  structurally a position write; its 3-bit speed field carries `MOVEMENT_WALK` (2), which the
  client's speed table at `0x140C9C888` maps to a walk, so the client interpolates between tiles
  and plays the walk animation from the actor's Base Animation Set. Confirmed in a live two-client
  session on 2026-09-07: both characters walked normally on each other's screen, and the other
  player right-clicked as a named actor with a combat level. Locomotion animation needs no
  animation mask at all - the BAS in the appearance block supplies stand/walk/run, which is also
  why wielding the bronze sword switches the set from 2699 to 2584. What the walk (type 1) and
  run (type 2) forms actually bought is a compact encoding and correct two-tile-per-tick running,
  NOT the appearance of walking.
- **Every CANDIDATE mask** (0x1000 head icons, 0x80000, 0x100, 0x200000/0x400000,
  0x1000000) and the confirmed no-op blocks.
- **Head-icon hashes.** 910 carries a second MD5 cache for icons (mask 0x800 there); the 947
  head-icon block is CANDIDATE, so there is no icon cache here.

## Integration recipe (Area C)

Per world tick, after all movement is processed and before `Entity.resetMasks`:

1. Build one `Native947PlayerInfo.Actor` per live player, indexed into an `Actor[2048]`:
   - `Actor.builder(p.getIndex(), p.getX(), p.getY(), p.getPlane())`
   - `.previous(lastTile.getX(), lastTile.getY(), lastTile.getPlane())` from
     `p.getLastWorldTile()`
   - `.moved(p.hasTeleported() || p.getNextWalkDirection() != -1)` - the 910 condition
   - `.movementType(p.hasTeleported() ? MOVEMENT_TELEPORT : p.getMovementType())`
   - `.visible(p.isRunning() && !p.hasFinished())`
   - `.appearance(body, hash)` from `Appearence.getAppeareanceData()` /
     `getMD5AppeareanceDataHash()` when both are non-null (the native appearance body is
     viewer-independent, which is why one snapshot serves every viewer)
   - `.masks(...)` only when `p.needMasksUpdate()` and at least one CONFIRMED mask applies.
2. Hand the same table to every attached session's `Native947Viewport.frame(world)`.
3. Write PLAYER_INFO before NPC_INFO in each frame (M4 acceptance).
4. Call `viewport.forget(index)` when the world frees a slot, so a later occupant of the same
   index cannot inherit a stale appearance hash or region hash.

`Native947Viewport` must be constructed on the world thread (or given the world thread
explicitly); every mutating call checks. A session's first frame is
`viewport.initialScene(world, npcBits, areaType, hash1, hash2)`, which replaces
`Native947Packets.initialSinglePlayerScene` and returns the same REBUILD_NORMAL packet type.

## Regression anchors held by the tests

- `initialScene` for index 1 with no other players is byte-identical to
  `Native947Packets.initialSinglePlayerScene`, and the stream length is unchanged for indices
  1, 2, 1000 and 2047.
- An unchanged player's frame is `00 7F F4` and equals `Native947Packets.singlePlayerIdle`.
- An appearance-only frame is `C0 7F F4 00 00 04 (N+128) <N>` and equals
  `Native947Packets.singlePlayerAppearance`.
- All eight one-tile steps equal `Native947Packets.singlePlayerWalkStep`, with and without the
  appearance block. (M4 kept this: a session's *first* move after `initialScene` still uses the
  absolute form, because nothing has told the client that slot's movement-speed token yet - see
  `notes/M4-movement.md`. The second consecutive step is the compact walk form.)
- Two sessions add each other with the "region hash unchanged" bit and the right 6/6 in-region
  offsets, and the mask blocks appear in the order the passes registered the pending indices.
- The three region-hash delta selectors, the remove form, the 5/8/11-bit skip boundaries, the
  header extension markers and a refused CANDIDATE mask each have a test.
