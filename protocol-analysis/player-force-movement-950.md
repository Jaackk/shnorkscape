# 950 player forced movement: engine adapter and timing

Date: 2026-09-10. Binary source: `OpenNXT/data/clients/950/win64/original/rs2client.exe`,
SHA-256 `fc7492548627a4068e88a7a45ba4f0be93004adf246bb4b174b24ae588768b36`.
Source files in AstraNXT were not modified. All changes belong to the isolated 950RevTest copy.

## Native field and clock proof

Reproduce with the read-only `tools/dis950.py`:

```
python -B tools/dis950.py range 0x14012DD96 0x150
python -B tools/dis950.py range 0x14031DAE0 0x1D1
python -B tools/dis950.py range 0x1403208E0 0x440
python -B tools/dis950.py range 0x140143660 0x98
python -B tools/dis950.py range 0x14063C33C 0x38
python -B tools/dis950.py range 0x14063C533 0x38
python -B tools/dis950.py range 0x14063D1DA 0x105
python -B tools/dis950.py range 0x14063D3A8 0x32
python -B tools/dis950.py range 0x14063D59E 0x38
```

The force-movement block is player mask bit 0. Its argument order is dx1, dy1, dx2, dy2,
planeDelta1, planeDelta2, firstArrival, secondArrival, direction. At `0x14012DE52/60/73`
the three shorts use LE / BE / LE; direction is masked to 14 bits at `0x14012DE7C`.
The signed coordinate transforms are documented in `player-masks-950-derived.md` and
are already implemented by `Native950PlayerMasks.ForceMovement`.

The parser calls `0x14031DAE0` at `0x14012DEDC`. That sink shifts each x/y delta left nine
bits (`0x14031DB42..4B`) and adds it to the current actor/path position. Both endpoint
planes add their deltas to actor `+0x50` (`0x14031DB94..B7`). It stores the two endpoint
vectors at `+0xDAC` and `+0xDBC`. It independently adds the supplied current clock to
both received arrivals (`0x14031DB02..35`) and stores the resulting deadlines at
`+0xDCC` and `+0xDD0`; the second field is not a duration added to the first.

The current clock reaches the player parser as EDX: `0x1401436CD` reads it from
client/game-shell `+0x528`, and `0x14012C907` saves it at the stack location later loaded
by `0x14012DE99`. The logic-clock period is **20 milliseconds**:

- Constructor `0x14063C33C` initializes `+0x520` to 20, `+0x524` to 50, and clock
  `+0x528` to 1. Constructor `0x14063C533..546` also restores period20/frequency50.
- `0x14063D1DA..214` converts the performance counter to nanoseconds.
- `0x14063D2A1..2CB` converts elapsed nanoseconds to milliseconds (multiply-high by
  `0x431BDE82D7B634DB`, then shift18, the division by 1,000,000), subtracts the epoch,
  and divides by the period at `+0x520` to calculate due logic cycles.
- Each executed logic step increments clock `+0x528` at `0x14063D3C9`, or the other
  loop's `0x14063D5C8`. Render iteration increments a different field, `+0x530`.

Therefore a received arrival offset30 means 600 ms on this paired client, independently
of rendering FPS. A precise offset17 means 340 ms.

## Legacy state and corrections

`game/com/rs/game/ForceMovement.java` stores two destination tiles and two delays.
`NewForceMovement` overrides only the direction accessor; its name does not mean precise
timing. `setForceMovementPrecise(true)` is the explicit unit switch.

`LocalPlayerUpdate.applyForceMovementMask` multiplies normal delays by30; its precise
variant leaves them unchanged. Both use arrival offsets from now and add one client cycle
when the two arrivals are equal. `Entity.setNextForceMovement` also schedules two absolute
deadlines, despite the constructor comment describing the second as a stage duration.
However, legacy `getFirstTickTime/getSecondTickTime` multiply those client-cycle counts by
**16**, scheduling a nominal one-game-tick arrival at480 ms. The native 950 branch uses
20 ms, while the 910 and 947 branches are unchanged.

The paired client's interpolation is in `0x1403208E0`:

- Before the first deadline, move towards endpoint1 (`0x1403209C4..`).
- At/after that deadline, endpoint2 uses `(now-first)/(second-first)`
  (`0x140320B65`, `0x140320BA2`, `0x140320BE6..BF1`).
- Finishing writes endpoint2 into the actor target and clears both deadlines
  (`0x140320CB8..CDC`).

The adapter uses these semantics, with explicit normalization choices:

1. Two supplied tiles preserve both absolute arrival offsets. An equal pair gets one native
   cycle added to the second arrival, as in the old writer. A second deadline before the
   first, or equal arrivals at65535, is refused instead of inventing a reversed path.
2. A missing second tile means one intended leg. It becomes original position at cycle0
   followed by the requested first target at its arrival time. The legacy placeholder
   second-position origin with deadline0 is not forwarded. This preserves the intended
   destination and leaves the native final target correct. An immediate one-leg request
   uses a minimum one-cycle interval (20 ms).
3. Endpoints are copied when queued. Many real callers pass their mutable Player as the
   first tile; later changes to that object must not change the already-scheduled request.
4. The engine initially describes mask deltas against the authoritative frame tile. The
   final viewport writer rebases XY to the queued final endpoint while retaining the current
   authoritative plane, as detailed in the live correction below. Destination planes use
   the derived signed plane deltas. Native signed
   byte ranges, world tile bounds, 14-bit direction, and u16 arrivals all validate before
   scheduling. Long arithmetic prevents overflow when scaling an untrusted legacy delay.

## World scheduling and integration

`Native950ForceMovement.Plan` is the single source for the mask and authoritative deadlines.
`Entity.setNextForceMovement` selects it only for exact `ClientProfile.NATIVE_950` players;
`Player.isNative950()` is intentionally profile-agnostic in this repository and is not a
sufficient gate. NPC scheduling is outside this player milestone.

The native world already owns a 600 ms `Native950TickScheduler`. The helper schedules two
ordered one-shot arrivals on it, with the proper 20 ms-derived deadlines. The wheel rounds
up to a minimum one game tick. If both arrivals fall in the same game tick (Surge 0/600 ms,
or precise340/580 ms), both execute at that tick and the final endpoint wins before world
movement/frame processing. The old repeating poll applied only the first on that tick and
waited another600 ms before applying the already-due second; this branch removes that bug.

Sub-tick authoritative position changes remain bounded by the world's600 ms resolution:
for example a620 ms completion commits on the1200 ms world tick. The client interpolates
with the original20 ms resolution. This is an explicit existing scheduler limitation, not
a claim of a new20 ms server loop. The branch never reintroduces off-thread schedulers.

An explicit null request or newer movement increments a generation, preventing older queued
arrivals from overwriting it. Finished entities also reject queued arrivals. `resetMasks`
clears only the per-frame plan; it does not cancel the continuing scheduled movement.
Queued walk steps are cleared when accepting the force movement. Other content's explicit
teleports and later input policies remain under their existing controllers.

Parent integration into `Native950EntityMasks.playerMasks`:

```
Native950ForceMovement.Plan move = character.getNextNative950ForceMovement();
if (move != null) {
    // Handle a rebased out-of-range mask as a counted refusal, rather than truncation.
    builder.forceMovement(move.mask(character));
}
```

Rejected input plans increment `Native950ForceMovement.refusals()`. Neither legacy time
accessor is changed, so source profiles retain their prior semantics. A missing native
scheduler fails clearly rather than falling through to the legacy thread pool.

## Validation

`modern947.Native950ForceMovementTest` contains23 focused tests, written without running an
encoder to obtain expected bytes. They cover source shapes from ActionBar Surge (0/1 ticks,
ten tiles), Wilderness ditch (1/2 ticks), GodWars ice door (single destination,2 ticks), and
Runespan NewForceMovement (1/35 ticks), plus precise time units, all eight compass directions,
plane deltas, mutable-tile snapshots, frame rebasing, equal deadlines, immediate one-leg
requests, cancellation, overflow, and last representable u16 times.

Wire assertions are literal950 blocks, including asymmetric coordinates, distinct deadlines
and distinct byte orders. Scheduling assertions drive a private real Native950TickScheduler
without sleeping, opening sockets, or loading/saving players. Entity lifecycle cases temporarily replace and restore the private CoresManager scheduler reference; no global service or world thread is started.
The integrated suite and real-cache acceptance passed; their current totals and deployment
are recorded in `validation-effects-2026-09-10.json`. These tests do not claim live
client animation quality or content reachability for currently unported abilities.


## Cancellation and movement ownership follow-up

Independent review found that merely cancelling server callbacks leaves the existing client
interpolation running. Local short/long teleport readers at 0x1401265F7..12660B and
0x14012670E..126725 invoke actor vtable+0x160. The player constructor installs vtable
0x140B60608, whose +0x160/+0x168 entries are 0x14012FBF0/0x140321AF0. Those path/animation
updates (including 0x140321F50 and 0x14031D9C0) do not write force deadlines DCC/DD0.
Force processing at 0x1403208E0 still uses them; normal completion at 0x140320CDC writes -1.

External nonnull Entity.setNextWorldTile now invalidates queued server endpoints and emits a
stationary force mask at the new authoritative tile: both relative endpoints zero, arrivals
0/1 native cycle. Explicit clear and invalid superseding requests do the same at the pending
or current authoritative tile. The cancellation schedules no server arrival; it replaces the
client interval and completes within20ms. A second teleport before mask emission relocates
the cancellation snapshot again. Internal forced-arrival setter bypasses external cancellation
so stage1 cannot invalidate stage2. Valid replacement requests overwrite the cancellation
snapshot with their own plan.

isNative950ForceMovementActive() is persistent engine state, separate from the one-tick
snapshot. It survives resetMasks, remains true after intermediate arrivals, ends after the
terminal callback, and clears on cancellation/supersession. The session owner uses this gate
alongside the existing content lock to refuse walking during an active plan. Added entity
regressions cover both stages, mask reset,35-tick pending teleport cancellation, repeated
teleports, explicit clear, invalid replacement, supersession, and single-leg completion.
WorldTile.getPlane publicly clamps high constructor planes to3; tests now validate that
existing normalization instead of incorrectly expecting the adapter to observe raw plane4.
Negative planes, which remain observable, are refused.


## Live correction: predicted native position throughout interpolation

The first live two-tile test started at (3207,3214). The server ended at (3207,3216)
while subsequent client positions retained an extra two tiles north. Normal walking
resumed, so this was a position-base mismatch, not an enduring input lock. The first
hypothesis focused on terminal arrival; the complete function shows that endpoint2
becomes the native logical XY base throughout interpolation, not only on completion.

Independent native evidence:

- Player vtable `0x140B60608 + 0xB0` contains `0x140321900`; the getter returns
  actor `+0x270`, the logical position ordinary movement decoding uses.
- Before the first arrival, `0x140320ACD/0x140320AD5` already copy endpoint2 into
  `+0x270..+0x278`. During the second leg, `0x140320BF9/0x140320C01` do the same.
  Terminal completion repeats the copy at `0x140320CCE/0x140320CD6` and clears
  the interpolation deadlines at `0x140320CDC`.
- Render interpolation is separate (`+0x2A8..+0x2B8`). A screenshot may show a
  position between endpoints even while normal protocol deltas are based on endpoint2.
- Small relative movement reads the logical getter at `0x140126569`, then adds
  tile deltas shifted by nine at `0x1401265AC..0x1401265CD`. Sending a second
  two-tile delta therefore creates exactly the observed persistent offset.
- The force routine does not write actor plane `+0x50`. Its downstream position
  update `0x140322130` reads that plane at `0x140322154`; plane remains a separate
  authoritative field. Suppression must retain any plane change as a zero-XY prefix.
- NPC additions likewise use the local actor's logical getter when its path queue
  is empty (`0x140120358..0x1401203A8`). Their offsets need the viewer's predicted
  native base, even while the server player is still at an earlier scheduled waypoint.

The queue is a second source of the native position base, so tracking `+0x270` alone
is insufficient. The movement reader first uses the final queued record when its count
is nonzero (`0x140126544..0x140126599`), falling back to the logical getter only when
empty. NPC additions make the same choice. Actor construction at `0x14031CE7B` calls
`0x140324CD0`, installing queue vtable `0x140B78BB8`. Its `+0x18` method is
`0x1403C0C60`: the force setter calls it at `0x14031DB74`; it retains the last queued
record, and `0x1403C1D50(queue+8)` writes queue count **one**, not zero, at
`0x1403C1D70`. Ordinary queue processing is skipped while forced at
`0x14031FA40..0x14031FA5B`.

An ordinary instant reposition does not establish an empty-queue invariant either.
The teleport branch invokes queue `+0x30` (`0x1403C0BD0`), clearing count at
`0x1403C0BE2`, but subsequently calls `0x14031D9C0` at `0x140321C55`. Its queue
`+0x08` method (`0x1403C0D60`) adds a record even for zero displacement when count is
zero (`0x1403C0F29`, record at `0x1403C1036`, increment at `0x1403C1084`). Therefore
teleporting to the origin before a force mask would still leave an origin queue base.

The correction preserves authoritative server waypoint timing while explicitly aligning
both native bases. On publication of a real, nonzero-generation force block, PLAYER_INFO
first sends an absolute-form **ordinary queued movement** to the plan's final XY, with
the current authoritative plane and movement token `MOVEMENT_WALK` (2). The native
ordinary branch at `0x14012661A..0x140126631` inserts this queue record through
`0x14031D9C0`; it does not instantaneously reposition the rendered character. The force
mask follows, with its XY offsets rebased from the authoritative frame tile to that same
final XY. The native setter therefore reconstructs the intended first and final world
endpoints from the retained final queue record, and interpolation later writes that same
final XY to `+0x270`.

The ordinary queued token matters for two-leg content. Before the first arrival the
native routine reads the current physical render position (`0x1403209EA..0x140320A2B`)
and approaches endpoint1. An instant teleport to endpoint2 before the mask would change
that starting render position. Queued publication preserves it, while normal force
processing owns the render trajectory. The force mask's timing, directions, plane deltas,
and simultaneous animation/effect blocks are unchanged. `Update.rebaseForceMovement`
returns an immutable copy; one viewer cannot mutate another viewer's source mask. The
adapter validates both the original and final-relative signed-byte ranges before it
schedules any server arrival; endpoints each encodable from the origin may still have
an unencodable 254-tile span from the final point, which is refused.

Plan through EntityFrames into Actor carries a per-entity generation and immutable final
XY. Each viewer records the endpoint only when the actual force block is emitted. Every
genuine scheduled force waypoint carries that generation. Matching viewers do not receive
the waypoint's XY delta again; plane changes still travel. Their actor and region records
continue to advance even when movement bytes are suppressed. A newly visible remote player
is deferred only on its original force-mask frame, avoiding a new actor with no rendered
origin for this sequence. On the following frame it can be admitted at its authoritative
tile; because that viewer never received the plan, subsequent waypoints use ordinary
movement. Viewport metadata is bounded and cleared on removal, identity changes and reset.

Ordinary later movement and external teleports compute XY deltas from the viewer's known
native base. Publication of a replacement force uses the old base for its queued-final
prefix and then installs the replacement's final base. Stationary cancellation instead
repositions to the authoritative frame tile and clears the force generation. Suppression
is never allowed to discard this replacement/cancellation prefix. Same-frame old arrival
and new plan keep the old arrival tag long enough to calculate the preceding base.

Cancellation needs both a reposition and a stationary mask. With no queue, the mask sink
`0x14031DB5D..0x14031DB69` reads physical render position, so a zero-offset stationary mask
alone would freeze at an unknown interpolated point. Active cancellation queues a normal
teleport to the authoritative tile before its stationary mask. An explicit external tile
replacement (including null) clears old arrival metadata. A null force clear after a
completed tile is already queued preserves that tile and its generation: it does not
replace the pending authoritative move.

NPC viewport additions and re-additions are conservatively deferred while the viewer's
force is active, including the original publication frame. Existing NPCs still receive
updates and removals. This avoids relying on unproven within-batch interpolation timing.
After terminal arrival or cancellation, additions resume from the authoritative tile;
the queued-final publication ensures the retained queue base agrees at completion. Tests
cover mask reset, terminal offsets, scene/plane changes, cancellation and reused NPC slots.

Engine tests cover both scheduled stages, publication/reset, cancelled and superseded
generations, explicit tile replacement, completed-arrival/new-plan ordering and final-base
range admission. Dedicated protocol tests cover queued final publication, immutable mask
rebasing, seen/unseen generations, plane-only movement, continued walking,
cancellation/replacement, late-player admission and viewport lifecycle. The real-world
acceptance probe exercises scheduling through EntityFrames and transport, asserting that
publication changes the encoded native base while authoritative Player remains at origin.
These tests establish server behavior and native field correspondence. After fresh world
admission on the corrected build, the user reported that movement seems fine. The server
log corroborated repeated two-tile forces followed by ordinary walking and banking. This
is user acceptance of the short local command, not a rendering claim for every legacy
ability or multi-viewer scenario. A pre-existing live offset requires fresh world admission
before testing; another relative teleport alone need not repair it.
