# M4 area C - entity masks from real state, and clickable NPCs

Two things P6 recorded as deferred:

- *"Only the appearance mask is emitted. The encoders are mask-capable and tested for animation,
  face angle, face entity, force talk, hits and colour, but nothing installs a mask source yet."*
- *"Clicked-NPC resolution does not exist: the interaction path is fail-closed on the banker's
  index, so published wanderers are inert targets."*

Both are closed here. Nothing in `Native947PlayerMasks` or `Native947NpcMasks` changed: those two
classes still own every width, every transform and the client's consumption order, and they still
refuse every CANDIDATE block. What this milestone added is the *source* - one class that reads the
same 910 `Entity` fields `LocalPlayerUpdate` / `LocalNPCUpdate` read and hands them to those
builders - and a generalised NPC click.

| File | Role |
|---|---|
| `game/com/rs/game/player/client/Native947EntityMasks.java` | NEW. The only place that turns 910 entity state into 947 mask builders. |
| `game/com/rs/game/player/client/Native947EntityFrames.java` | Installs the player mask source on every `Actor` snapshot. |
| `game/com/rs/game/player/client/Native947NpcViewport.java` | Default `MaskSource` is now `ENTITY_STATE` instead of silence. |
| `game/com/rs/game/player/client/Native947Interactions.java` | Generalised NPC click; the banker branch is untouched. |
| `game/com/rs/game/player/client/Native947ActionRouter.java` | NEW `npcOption` / `legacyNpcOption`: the generic dispatch into `NPCHandler.dispatch`. |
| `tests/modern947/Native947MaskSourceTest.java` | NEW. 21 cache-free tests, real `Player` / real `NPC` in, verified bytes out. |
| `tests/modern947/Native947ActionRouterTest.java` | Extended with the clicked-NPC cover. |

## 1. When the masks are read

On the world thread, inside the frame phase - after every entity has moved (phase 1b) and before
`resetMasks` (phase 3):

- **Players.** `Native947EntityFrames.beginFrames` builds one immutable `Actor` per character per
  tick and attaches `Native947EntityMasks.playerSource(character)`. The source is therefore
  **viewer independent by construction**: every viewer in a tick is shown the same block. That is
  also why the 910 `added` clauses (`p.getNextFaceEntity() != -2 || added && ...`,
  `added || p.getNextFaceWorldTile() != null`) are deliberately not ported - they are per-viewer
  state that an `Actor` shared across viewers cannot express, and this port never sends a mask
  merely because some viewer is adding the player.
- **NPCs.** `Native947NpcViewport.describe` asks the installed `MaskSource` per viewer, also inside
  the frame phase. The default source ignores the viewer, so the answer is the same for everyone.

`Native947PlayerInfo` requires that a declared mask source always produce at least one confirmed
mask, so the "is there anything to send" decision is made once in `playerSource` and the returned
source rebuilds the same content for each viewer that needs it.

## 2. Player masks: what is sourced

| 947 mask | 910 source | Notes |
|---|---|---|
| animation `0x40` | `Entity.getNextAnimation()` | First four of `Animation.getIds()` plus the speed byte, exactly as `LocalPlayerUpdate.applyAnimationMask`. Ids below -1 normalise to "no sequence"; the speed is clamped to 0..255 where 910 truncates. |
| face entity `0x20` | `Entity.getNextFaceEntity()` | 910 stores one int: -2 nothing, -1 clear, otherwise `getClientIndex()`, which offsets a **player** index by 32768 and leaves an NPC index raw. The 947 block names the kind (1 = NPC, 2 = player, 0xFF = clear), so the source strips that offset again. |
| face angle `0x80` | `Entity.getNextFaceWorldTile()` -> `Entity.getDirection()` | Emitted under the 910 rule for its mask `0x10`: a face rectangle is set and no walk step, run step, force movement or face-entity request is already turning the model. |
| force talk `0x10000` | `Entity.getNextForceTalk()` | Flags byte 0 - overhead only. Flag bit 0 would also echo the text into the chatbox as message type 2, which is M5's public-chat path, not this one. |
| hits `0x8` | `Entity.getNextHits()` | Untyped form only, see below. |

**Locomotion needs no animation mask.** The Base Animation Set inside the appearance block supplies
stand, walk and run (which is why wielding the bronze sword switches the set from 2699 to 2584).
The animation mask is for deliberate animations, which is exactly what `getNextAnimation()` holds.

### Hits: the untyped form, and why

`PLAYER_INFO_MASKS.md` mask `0x8` is CONFIRMED and offers three forms. Two of them carry a hitmark
**type**; `HitLook.getMark()` is a 910 hitmark id table and the 947 ids are not established, so a
typed hit would put an id on the wire that this port cannot point at. Every hit is therefore
published through the client's own untyped form - marker `0x7FFE`, type -1, damage as a plain byte -
which carries no hitmark id at all. That is the same marker 910 already writes for a hit whose type
the viewer must not see (`LocalPlayerUpdate.applyHitsMask`, `writeSmart(32766)` then a plain byte),
so the two protocols agree on the form as well as on the field widths.

The one transform carried over from 910 without independent 947 evidence is the **damage scale**:
910 stores damage in tenths of a life point and divides by ten on the wire, and the client draws
whatever number it is handed. A hit whose displayed damage does not fit the untyped form's plain
byte is **dropped and counted**, never truncated into a number a player would read as real.
Combat is M9; that is the milestone that should confirm the scale and the hitmark table against a
live client.

### Player content deliberately left unsourced

- **Hit bars.** The 947 bar id is resolved through a client cache loader (`0x14039EF60`); the 910
  `HitBar.getType()` ids are a different table. Counted in `refusals()` and dropped.
- **Colour overlay `0x800000`.** The block is CONFIRMED, but 910's `Colour.getColours()` is a
  32-bit hash whose split into the 947 hue / saturation / lightness / strength fields is not
  established.
- **Spotanim list `0x4000000`.** Also CONFIRMED, but `Graphics.getSettingsHash()` is a 910 slot
  layout, not the 947 spotanim record.
- **Force movement `0x10`.** No M4 content produces one.
- Every CANDIDATE mask, as before: `Native947PlayerMasks.Builder.candidateMask` throws, and nothing
  here calls it.

## 3. NPC masks: what is sourced

| 947 mask | 910 source | Notes |
|---|---|---|
| animation bit 7 (`0x80`) | `NPC.getNextAnimation()` | Four `smart2or4null` ids then `delay + 128`. |
| face coordinate bit 3 (`0x8`) | `NPC.getNextFaceWorldTile()` | The server sends `2*tile + 1`. Emitted under the 910 condition (`LocalNPCUpdate.java:227` and `:305`): a rectangle is set **and no walk or run step is being taken**. The guard is not cosmetic - `Entity.resetMasks` (`Entity.java:1732`) clears `nextFaceWorldTile` only on a tick with no walk step, so a rectangle set before a walk survives the whole walk and would otherwise be re-sent, stale, on every tick of it. |
| transform bit 5 (`0x20`) | `NPC.getNextTransformation()` | Skipped on an addition, whose record already names the type. The id is a **raw 910 npc id** out of 910 content, so it goes through `Native947IdValidity` exactly like a spawn: only `same` may name a 947 definition, anything else is a counted refusal. `Native947NpcViewport.describe` applies the same gate to `npc.getId()`, because a transform mutates it and the addition record would otherwise carry it. |
| say bit 0 (`0x1`) | `NPC.getNextForceTalk()` | NUL-terminated CP1252. |

**There is no NPC face-entity block.** NPC mask bit 15 is CANDIDATE in `NPC_INFO_MASKS.md`, so it is
not encodable at all. It does not need to be: 910's `Entity.faceEntity(target)` sets the face
**rectangle**, not the face-entity field, so a banker turning to the player it is serving already
arrives as the CONFIRMED face-coordinate block. `Native947NpcSmoke` now asserts exactly that -
banking publishes a retained mask-only entry whose block header is `0x08`.

**NPC hits are refused.** `Native947NpcMasks.Update.hits()` throws: bit 1 has a confirmed sink but a
CANDIDATE read layout. A queued hit on an NPC is counted in `refusals()` and dropped. NPC name and
force movement are CONFIRMED but have no per-tick 910 source to read.

## 4. Text safety

Both mask encoders throw on an embedded NUL or a character that is not representable in CP1252 -
correctly, because the client's reader is `strlen` based and a bad byte would desynchronise every
following entity in the frame. Throwing during encoding would kill the frame for every viewer, so
`Native947EntityMasks.isClientText` checks first and turns an unrepresentable force talk into a
counted refusal instead.

## 5. Clicked NPCs

`Native947Interactions.npc` used to refuse every published NPC that was not the banker. It now:

1. asks `Native947NpcView.canInteract(player, index)`, which answers from the list this viewer's
   client was **last sent** - never the world roster - so a client cannot name an NPC it was never
   shown, and which already applies the visibility, range and plane gates;
2. resolves that index through `World.getNPCs()`, rejecting an NPC that has since left;
3. branches: the banker keeps its own path (its Talk and Collect answers are native content, and
   its Bank option performs what the 910 banker dialogue's Bank option does), and everything else
   goes to `Native947ActionRouter.npcOption`.

`npcOption` maps the native option through `legacyNpcOption` and dispatches into the decoded
overload `NPCHandler.dispatch` that P5 already uses:

| native option | 910 |
|---|---|
| 1..4 | `NPCHandler` options 1..4 |
| 5 | **no 910 branch** - refused, never folded onto another option |
| 6 | `NPCHandler.EXAMINE_OPTION` |

The 947 NPC opcode table has six entries (`Native947Actions.NPC_OPCODES = {88, 115, 33, 60, 123,
104}`), so option 6 is examine; the 910 `NPCHandler` implements 1..4 plus examine and has nothing
for a fifth option.

Gates kept, in order: the adapter's `playerMayAct`, the published-list membership, range and plane,
then the pre-filter `npcOption` repeats from the handler itself (locked player, absent /
uninteractable / dead / finished NPC, NPC outside the viewer's loaded regions). The pre-filter turns
a refusal into a **counted rejection with a reason** instead of a handler that silently returns.
`stopAll(false)` is deliberately **not** repeated - the decoded overload performs it itself, and
running it twice would drop the player's state before the handler had looked at it.

After the dispatch the pending `RouteEvent` is processed once, exactly as the object path does: the
910 handler installs the walk, a player already in reach completes on this tick, and one that is not
is finished later by `Native947Interactions.afterMovement`.

The client's run modifier is deliberately **not** forwarded as `forceRun`. The native walk path
already refuses to let a modifier flag change movement speed; an NPC click must not be the way
around that.

A handler exception stays a counted `handlerFailures` rejection. That is the path a **native 947**
NPC takes today: it has no 910 definitions, so the deeper branches of the handler throw
`UnsupportedOperationException` rather than answering. Nothing reaches the wire either way, and an
NPC spawned from `spawns.json` with a real 947 definition is unaffected.

## 6. Evidence

- Build: 38 test classes, **447 tests, 0 failures** (was 36 / 391); 21 of the new ones are `Native947MaskSourceTest`, 7 are the clicked-NPC block added to `Native947ActionRouterTest`. `build/m4c-build7.log`.
- All seven real-cache smokes green with `strictHits = 0` and `tickFailures = 0`:
  `build/m4c-Native947{World,Interactions,Npc,Equipment,Persistence,Bootstrap,Multiplayer}Smoke.log`.
- `Native947NpcSmoke` now decodes the retained mask-only form and the mask section and asserts the
  banker's face-coordinate block: `maskOnly=2 faceCoord=2`. Its router totals are unchanged -
  `npcBank=2 npcExamine=1 npcTalk=1`, `handlerFailures=0`, `rejected=0` - which is the banker
  regression anchor holding.
- `Native947MaskSourceTest` drives a real `Player` through `Native947EntityFrames` and compares the
  whole PLAYER_INFO body against the literals in `PLAYER_INFO_MASKS.md`. An animation of 855 in slot
  0 reproduces the document's own "Full frame in the initial state":
  `c0 7f f4 00 00 40 03 57 7f ff 7f ff 7f ff 80`.

## 7. Two smokes outside this area were touched, minimally

Both were asserting the *absence* of the masks this milestone adds, so they had to move with it.

- `Native947NpcSmoke.npcOutput` accepted only the removal retained selector and demanded
  `mask bit == 0` on every addition. It now decodes all four retained selectors, counts pending
  masks, requires the 65535 terminator rule, and fully decodes the mask section (2 skipped bytes +
  a `0x08` header + a 4-byte face-coordinate block per pending NPC), ending exactly on the last
  byte. It still refuses a walk or run selector from the static banker.
- `Native947EquipmentSmoke.playerOutput` required the mask header byte to be literally `0x04`. It
  now parses the 1..4 byte header, refuses any bit outside the set this port can source, finds the
  appearance body after the header (appearance is the first block this port ever emits - the only
  block ahead of it in the consumption order is force movement, which has no source), and keeps the
  old exact-length assertion for an appearance-only update.

## 8. Open items this area did not close

- **Hit damage scale and hitmark ids** (see 2). M9.
- **Colour overlay and spotanim list** need their own 910 -> 947 field derivations before a source
  can exist.
- **Login-burst duplication.** `Native947EntityFrames.admit` builds its own snapshot for the login
  `PLAYER_INFO`, and masks are not cleared until world phase 3, so a mask set before `attach`
  completes would be emitted in the login burst and again on the first world tick. No M4 content
  sets one that early, but a future login-time animation would need the burst to consume the masks
  or the tick to skip them.
- **NPC name and force-movement blocks** are CONFIRMED and encodable but have no per-tick 910
  source; whoever adds one should add it here rather than in the encoder.
