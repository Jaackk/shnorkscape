# P6 area C - N-slot admission, the world-phase tick, and retiring the single-entity path

Scope: how a native 947 connection gets a player index, how the world ticks N characters and N
NPCs, and what replaced the seven literal single-entity writers. The bit-level encoders are area A
(`Native947PlayerInfo`, `notes/P6-playerinfo.md`) and area B (`Native947NpcInfo`).

## 1. Admission: reserve before the login response

`GameLoginResponse` carries the player index, and `Ataraxia947Handoff` writes it while the Player
object does not exist yet - the world thread only creates the Player inside `attach`. The index
therefore has to be allocated first, by a call that touches no world state:

```
Ataraxia947Handoff.complete(channel)
  index = Native947World.reserve(username)      // 0 = world full -> close, no response written
  scene = SceneConfig(x, y, plane, index, ...)
  write GameLoginResponse(playerIndex = index)
  world.attach(..., scene, ...)                 // claims the reservation for the session
```

`Native947World` keeps a `Slot[2048]` table guarded by one monitor, because `reserve` runs on a
Netty login thread while `claim`/`free` run on the world thread. A slot is:

| state | `slots[i]` | `slots[i].session` | freed by |
|---|---|---|---|
| free | null | - | - |
| reserved | Slot(username) | null | `release(i)` |
| attached | Slot(username) | the session | the session closing |

`release(i)` deliberately only clears an *unclaimed* slot, so a login failure path can call it
unconditionally without ever taking a slot away from a live session.

### Every branch releases exactly once

A leaked reservation is permanent capacity loss, so the handoff was written so that exactly one
release runs on every path that does not end in an attached session:

| branch | release |
|---|---|
| `reserve` returns 0 | nothing to release; the channel is closed |
| exception building the scene / pipeline | outer `catch` |
| `GameLoginResponse` write failed, or the channel died while it was in flight | the write listener (Netty always completes a write promise) |
| `attach` future completed exceptionally, or the channel died during admission | the `whenComplete` callback |
| `attach` succeeded | none - the session owns the slot, and `closeOnWorld` frees it |

There is deliberately **no** `channel.closeFuture()` release in the handoff. It would race: the
world tick can notice the dead channel and free the slot before the close listener runs, and by
then a later login may already have reserved the same index. Losing a slot on a torn-down channel
is impossible without it, because the two listeners above always fire.

`attach` itself adopts the reservation when the scene's index is already reserved for that
username, and takes a fresh one when the caller never reserved (the smokes and tests that call
`attach` directly). Either way the slot belongs to that username before `attachOnWorld` runs, so
its failure path is unambiguous.

### Index authority

`EntityList` remains the single index authority for both entity kinds, and the index it holds is
exactly what the wire reports.

* Players: the index is decided by `reserve`, before the Player exists, so admission uses the new
  `EntityList.addAt(entity, index)` instead of `add`, which assigns `lowestFreeIndex + 1` at insert
  time. `World.addNative947Player(player, index)` refuses an occupied slot, an index outside
  `PLAYER_INFO`'s 1..2047 and a world that holds any non-native player.
* NPCs: created on the world thread, so ordinary `EntityList.add` assigns the index.
  `World.addNative947Npc` refuses a non-native NPC, an already-registered or finished one, a world
  holding any legacy NPC, and an index that would not fit `NPC_INFO`'s 16-bit field (65535 is its
  addition terminator, so 65534 is the ceiling).

`EntityList` reuses the lowest free index for both, and a login can take a freed index in the same
inter-tick gap the previous session's close ran in - long before any viewer has written the
removal record. That reuse is visible on the wire and both entity kinds handle it the same way,
with an identity serial rather than with timing:

* NPCs: `Native947NpcViewport` gives each visible NPC a per-viewer identity serial, and a changed
  identity at a known index becomes a removal plus a fresh addition on a later frame.
* Players: `Native947EntityFrames` gives each character object a never-reused serial, carried on
  every `Actor` snapshot as `Actor.identity`. `Native947PlayerInfo.processLocal` writes a removal
  (`1 0 00 0`: needs-update, no mask, type 0 destroy at `0x140125906`, region record unchanged)
  the moment the identity at a slot this viewer holds differs from the cached one, and `rotate()`
  only moves the slot onto the external list at the end of that frame, so the new occupant is
  added by the next one. Without it the new character would be encoded as the previous one
  continuing to move: no remove, no add, and the client would keep the departed entity's slot
  object forever.

The deferred `forget` (see 3) drops only the caches that died with the client's slot object - the
per-viewer appearance MD5 - and is **skipped entirely for an index that already has a new
occupant**, because the encoder is reporting that occupant as a removal plus an add and dropping
the viewer's local entry underneath it would make the next frame add the slot a second time. The
per-slot region hash is never dropped: the client's local-remove path at
`0x140125906..0x14012596A` clears only the entity handle at `[record+0x30]`/`[record+0x38]` and
leaves the plane and region bytes at `[record+0x00]`/`[record+0x04]`/`[record+0x08]` intact, so the
next add at that index must write its delta against the value the removal last published.

### One account, one slot

`reserve(username)` returns 0 when the world already holds a reservation or an attached session
for that account (compared through `Native947Save.canonicalUsername`, so "Bob" and "bob" are one
account), and `claim`/`attach` refuse a self-taken slot on the same test. `Ataraxia947Handoff`
asks `Native947World.isOnline` in `authorize` and answers `GenericResponse.LOGGED_IN`. Two slots
for one account is not untidy, it is item duplication: each session loads the same profile by
username and checkpoints it independently, so two whole-profile writers interleave over one save
file, and `World.playerMap` holds one entry per username which the first departure removes out
from under the survivor.

## 2. World-phase tick

`Native947Session.tick()` used to move and encode one session in one call. That is correct for one
character and wrong for two: a viewer encoded before another character has moved describes a world
that never existed, and because `NPC_INFO` offsets are read against the local actor's
post-movement tile (verified/NPC_INFO.md, "Positioning uses the local actor's most recent path
position, so the server sends its player movement update first"), such a frame is wrong on the
wire, not merely stale.

`Native947World.worldTick()` now runs the WorldThread order across all sessions:

```
World.currentTime++
pumpSchedulers()                      // WORLD_CYCLE, tick wheel, WorldTasksManager - unchanged, still first
close sessions whose channel died
phase 1a  session.tickInput()         // drain verified client actions, for every session
phase 1b  session.tickMove()          // processEntity + processEntityUpdate, for every session
          npc.processNative947Movement()   // then every world-owned native NPC
phase 2   frames.beginFrames(characters)   // ONE immutable Actor[2048] snapshot for the tick
          session.tickFrame(...)      // checkpoint, scene rebuild, then PLAYER_INFO + NPC_INFO
phase 3   session.tickEnd()           // Player.resetMasks, keepalive, tickEnd packet, flush
          npc.resetMasks()
```

Kept from the single-session tick: `pumpSchedulers()` first; movement processed exactly once,
inside `processEntityUpdate`; the sectioned checkpoint per session before that session's output
(a failed atomic save still closes the session before this tick's frame is flushed); and the
`tickEnd` packet plus the flush owned by the session, not the world.

A failure isolates to its own connection - `phase()` catches, closes that channel and frees that
slot, and the remaining viewers still complete the phase. A half-built frame for one viewer must
not cost the others theirs.

`Native947MultiplayerSmoke` checks the ordering from observed frames rather than from the shape of
the code: a recording encoder captures what every viewer was shown each tick, and every viewer in a
tick must have been shown the same position for every character. Under the old interleaved tick the
first viewer would see the second at its pre-move tile while the second sees itself post-move, so
the assertion actually distinguishes the designs; the probe also requires that characters really
moved during the observation, otherwise the agreement would be vacuous (last run: 95 two-viewer
ticks, 93 of them after somebody moved).

## 3. One live encoder

`Native947Frames` is the seam. Exactly one implementation is installed:
`Native947EntityFrames`, which owns a `Native947Viewport` per session over
`Native947PlayerInfo` and drives the session's `Native947NpcViewport` over `Native947NpcInfo`.
`Native947World.capacity()` is that encoder's `playerCapacity()`, so there is no separate
multiplayer gate that could drift from what the wire supports.

The seven single-entity writers in `Native947Packets` -
`initialSinglePlayerScene`, `singlePlayerAppearance`, `singlePlayerIdle`, `singlePlayerWalkStep`
(both overloads), `staticNpcAdd`, `singleNpcRetain`, `singleNpcRemove` - are `@Deprecated` and no
longer reachable from the world. They stay in the file as the literal wire fixtures the
verification tests pin, because those bytes are the evidence the generalised encoders are checked
against.

Byte-level consequences of the swap, all confirmed by re-running the six existing real-cache
smokes:

* The login `REBUILD_NORMAL` from `Native947Viewport.initialScene` is byte-identical to
  `initialSinglePlayerScene(1, ...)` for a single character in an empty world - `Native947WorldSmoke`
  still compares the frame byte for byte against the old fixture, cipher included.
* The login burst still carries exactly one `PLAYER_INFO` before its `SERVER_TICK_END`: the
  encoder's first frame, which carries the local appearance block the initial bit stream does not.
* `NPC_INFO` unchanged and empty bodies are still `01 00` and `00`. Two forms changed, both within
  what NPC_INFO.md permits: an addition-only body no longer carries the 65535 terminator (8 bytes
  instead of 10 - the terminator is only needed when a mask section would otherwise leave 16 or
  more bits in front of the addition reader), and an NPC leaving view is normally reported through
  the retained list's removal selector rather than a zero retained count.
  `Native947NpcSmoke` now parses the generalised body instead of matching the three literal
  payloads.

### Freed-slot caches

`Native947PlayerInfo.ViewState.forget(index)` drops a slot's cached appearance MD5 and region hash
so a later occupant of a reused index cannot inherit them. It cannot run at the moment the session
closes: the freed index is still in every other viewer's local list, and the removal record is
written from the actor cached there. `Native947EntityFrames` therefore defers it by one tick -
`release` queues the index, the next tick's frames emit the removal (which already nulls that
slot's actor and appearance hash), and the tick after that forgets the rest.

## 4. NPCs: world-owned, and able to move

The banker used to be created, registered and removed by each session's `Native947NpcView`. With
two sessions that would have meant two bankers at two indices. It is now created once by
`Native947World` on the first admission that supplies banker content, lives in the world's roster,
and is removed when the last character leaves. `Native947World.spawnNativeNpc(id, tile, size,
wanderRadius)` adds further world-owned native NPCs on the world thread.

`Native947NpcView` shrank to the session-facing adapter: the banker reference the P5 interaction
whitelist knows by name, the session's `Native947NpcViewport`, and `canInteract`, which answers
from the list the client was last *sent* (it runs in the input phase, so that list is the previous
tick's - exactly what the player was looking at when the click was made).

`NPC.createNative947` still refuses every legacy entry point. Movement was lifted narrowly rather
than by weakening those gates:

* `enableNative947Movement()` opts an NPC into `Entity.processMovement` and nothing else. It loads
  the NPC's map region set, because `Entity.needMapUpdate()` dereferences
  `lastLoadedMapRegionTile`, which the native constructor deliberately leaves unset for a
  stationary NPC.
* `setNative947Wander(radius)` adds the random-walk block of `NPC.processNPC` with combat,
  aggression, freeze, force-walk and map-area handling removed - none of which a native NPC has.
* `processNative947Movement()` is the world-phase move; a stationary NPC (the default) does nothing
  at all, so the verified static-banker frames are unchanged.
* Still `UnsupportedOperationException`: `getDefinitions`, `getCombatDefinitions`, `processEntity`,
  `processNPC`, `setNPC` and the legacy spawn/respawn path.

## 5. Known gaps for M4

* **Visibility policy.** `Native947PlayerInfo.DISTANCE_ONLY` is installed (same plane, Chebyshev
  24). The 910 test also requires `getMapRegionsIds().contains(getRegionId())`; that half cannot be
  expressed through the `Visibility` interface, which sees only `Actor` snapshots. Inside a
  104-tile scene the radius is the binding constraint, so the looser policy adds no player the
  client has nowhere to draw, but M4's region-aware scene work should install the full test.
* **No non-appearance masks.** No `MaskSource` is installed for players or NPCs, so only the
  confirmed appearance block (mask 0x4) is ever emitted. The mask allow-list opens one block at a
  time, per the P6 backlog row.
* **NPCs are not spawned from data.** `spawnNativeNpc` is a world-thread entry point, not a
  `spawns.json` loader; staging `data/npcs` and driving `Region.loadNPCSpawns` is M4.
* **World roster lifetime.** The native NPC roster is cleared when the last character leaves, which
  keeps a development world clean but is not how a real world behaves. M4 should own NPC lifetime
  separately from player presence.

## 6. What this phase verified

* 385 JUnit tests in 36 classes, 0 failures.
* `Native947WorldSmoke`, `Native947InteractionsSmoke`, `Native947NpcSmoke`,
  `Native947EquipmentSmoke`, `Native947PersistenceSmoke`, `Native947BootstrapSmoke` -
  all green against the real cache with `strictHits = 0` and `tickFailures = 0`.
* `Native947MultiplayerSmoke` - two sessions on separate channels: distinct indices, each viewer
  adds the other, one walks and stays in the other's viewport, three NPCs published to both
  viewers with two of them wandering, every viewer shown the same post-movement world in every
  tick, a disconnect frees the slot and the character, and a third session takes exactly that
  freed index. Every outgoing byte decodes with the real ISAAC stream, container totals conserved,
  `strictHits = 0`, `tickFailures = 0`, `unhandledFrames = 0`, `processEntityRuns == ticks`.
* OpenNXT `compileKotlin compileTestKotlin test` under Java 25, after the handoff change.
