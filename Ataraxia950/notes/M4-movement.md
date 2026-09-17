# M4 Area A - the compact local walk and run movement forms

Scope: the two compact local movement forms in `PLAYER_INFO` (selector 1 = walk, selector 2 =
run) and the rule that decides which form a move is written with. NPC movement (Area B), NPC
spawns and the id validity table (Area C) and the mask sources (Area D) are not here.

Files:

- `network/com/rs/network/protocol/modern947/Native947PlayerInfo.java` - the two direction
  tables, `movementForm`, the per-slot speed-token model, and the three new emit paths.
- `game/com/rs/game/player/client/Native947Viewport.java` - one new read-only accessor,
  `speedToken(index)`.
- `tests/modern947/Native947PlayerInfoTest.java` - 12 new byte tests (37 total in the class).
- `game/com/rs/game/player/client/Native947EquipmentSmoke.java` - **not owned by this area**;
  its `PLAYER_INFO` decoder hard-coded the absolute form and had to learn the two new ones. See
  "One file outside this area" at the end.

## What was added

| Form | Selector | Payload | Emitted when |
|---|---:|---|---|
| Walk | 1 | 3 direction bits, then the extra-step flag (always 0) | net displacement is Chebyshev 1, speed index is `MOVEMENT_WALK`, plane unchanged, token agrees |
| Run | 2 | 4 direction bits | net displacement is Chebyshev 2 and in the run table, speed index is `MOVEMENT_RUN`, plane unchanged, token agrees |
| Absolute | 3 | 15-bit short form or 3+30-bit long form | everything else, exactly as before |

Nothing that used to be written with the absolute form and cannot be written compactly changed
by a single bit. The four movement passes, the skip runs, the add/remove forms, the region-hash
deltas, the mask blocks and the initial scene are untouched.

## Evidence, field by field

| Wire element | Bits | Evidence |
|---|---:|---|
| Form selector (the 2 movement-type bits) | 2 | `0x1401258C5`; 1 at `0x1401259E2`, 2 at `0x140125BA8`, 3 at `0x140125D42` |
| Walk direction | 3 | `0x1401259E8` (`lea edx,[rax+2]`, `eax == 1`) |
| Walk extra-step flag | 1 | `0x1401259F8` (`edx = 1`), `sete [rbp+0x50]` at `0x140125A0A` |
| Walk direction table | - | `verified/player-direction-jumptable.txt`, jump targets `0x140125B25..0x140125B5F` |
| Run direction | 4 | `0x140125BB1` (`lea edx,[rax+2]`, `eax == 2`) |
| Run direction table | - | `verified/MOVEMENT_TABLES.md` section 1: a literal `cmp`/`jne` chain at `0x140125C0C..0x140125CFA`, all 16 arms in the instruction stream |
| Speed table | - | `verified/MOVEMENT_TABLES.md` section 3: `0x140C9C888`, five int32 entries, `0x140C9C89C` is an unrelated `OggS` datum |
| Speed token is sticky | - | walk reads it at `0x140125B96`, run at `0x140125D30`; neither writes it |
| Absolute short form writes the token | - | `0x140125E6D` (`mov [rax+0x28], rdi`), or `0x140125E43` (`&table[0]`) on the teleport branch at `0x140125E41` |
| Absolute long form writes the token | - | `0x140125F90`, on the branch the teleport index never reaches (`0x140125F59` jumps to the `vtable+0x160` reposition at `0x140125F75`) |
| Init record writes the token | - | `0x140125190` builds the index, `0x140125220` stores the pointer |
| Region-hash type 3 writes the token | - | `0x140126454..0x14012647D` |

### The two tables, and their second source

The walk table is the perimeter of the 3x3 square in row-major order and the run table is the
perimeter of the 5x5 square in the same order, one radius out:

```
walk (3 bits)        run (4 bits)
  0  1  2              0  1  2  3  4      dy = -2
  3     4              5        6         dy = -1
  5  6  7              7        8         dy =  0
                       9       10         dy = +1
                      11 12 13 14 15      dy = +2
```

Both tables are checked against a **second, independent source** in
`theWalkAndRunTablesAgreeWithTheNineTenDirectionHelpers`: 910's own
`Utils.getPlayerWalkingDirection` (Utils.java:1160-1185) and `Utils.getPlayerRunningDirection`
(Utils.java:1104-1152) return exactly the same number for exactly the same delta, cell for cell,
across the whole 5x5 neighbourhood. Two sources that were never derived from each other agreeing
on 24 rows is the strongest check available without a live client.

**The run table has no interior.** Every one of its 16 entries is a Chebyshev-distance-2 move, so
a two-tile run that turns a corner has no run encoding at all, and neither does a running player
who only advanced one tile. Those fall back to the absolute form.

## Which form a move gets, and why it is the 910 decision

The brief asked for the 910 decision, not a new one. The 910 encoder's walk/run branch is
present but commented out at `LocalPlayerUpdate.java:284-300`:

```
int dx = DIRECTION_DELTA_X[p.getNextWalkDirection()];
int dy = DIRECTION_DELTA_Y[p.getNextWalkDirection()];
if (p.getNextRunDirection() != -1) {
    dx += DIRECTION_DELTA_X[p.getNextRunDirection()];
    dy += DIRECTION_DELTA_Y[p.getNextRunDirection()];
    opcode = Utils.getPlayerRunningDirection(dx, dy);
} else {
    opcode = Utils.getPlayerWalkingDirection(dx, dy);
}
...
stream.writeBits(2, p.getNextRunDirection() != -1 ? 2 : 1);
stream.writeBits(p.getNextRunDirection() != -1 ? 4 : 3, opcode);
if (p.getNextRunDirection() == -1) stream.writeBits(1, 0);
```

This encoder takes the same decision from the same numbers, without needing the two direction
fields on the `Actor` snapshot:

1. **The summed delta *is* the net displacement.** `Entity.processMovement` snapshots
   `lastWorldTile` before the step loop (Entity.java:1469) and each step applies the same
   `DIRECTION_DELTA` pair through `moveLocation` (Entity.java:1530), so
   `(x - lastX, y - lastY)` is exactly the `(dx, dy)` 910 computed.
2. **`getMovementType()` is the same discriminator.** `Player.getMovementType()`
   (Player.java:3201) returns the run index exactly when a run step was taken
   (`getNextRunDirection() != -1`) or another is queued, and the walk index otherwise. So
   "a one-tile step is a walk, a two-tile step is a run" comes out of the real Player, and the
   `Actor` snapshot `Native947EntityFrames` already builds needs no new field.
3. **Ataraxia never produces a corner-turning two-tile run.** `Entity.processMovement`
   (Entity.java:1533-1538) previews the second run step and breaks out of the loop when
   `Utils.getPlayerRunningDirection` of the combined delta is -1. Every two-step move the
   encoder can see is therefore in the run table, which is why the walk form's extra-step
   sub-step is never needed.

Four things force the absolute form, and each is a fact about the parser rather than a policy:

- **A teleport.** `MOVEMENT_TELEPORT` is speed index 4, which the client address-compares at
  `0x140125E26` / `0x140125F59` and answers with an instant reposition through `vtable+0x160`.
  Only the absolute forms carry a speed field, so only they can say it.
- **A plane change.** Neither compact form touches the record's plane - the walk form re-writes
  the current one at `0x140125B6D`, the run form at `0x140125D16` - so a plane offset has no
  compact encoding.
- **A displacement with no table entry.** Anything that is not Chebyshev 1 for a walk or a real
  run-table row for a run, including a stationary actor whose `moved` flag is set. Fail closed:
  fall back rather than guess.
- **A speed token that does not already agree.** See below.

## The speed token, and why it gates the compact forms

This is the one non-obvious consequence of the evidence, and getting it wrong would render
players at the wrong speed rather than desynchronise the frame.

The client keeps a per-slot movement-mode token at `slot+0x28`: a pointer into the five-entry
table at `0x140C9C888`. It is **sticky**. The walk form and the run form only read it
(`0x140125B96`, `0x140125D30`) and hand it to the position-queue push `0x140319130`; neither
writes it. Only these write it:

| Writer | Value |
|---|---|
| Init record (`0x140125220`) | `(record>>18) & 3` |
| Absolute short form (`0x140125E6D`) | the 3-bit speed field, or `&table[0]` at `0x140125E43` when that field is the teleport index |
| Absolute long form (`0x140125F90`) | the 3-bit speed field - but the teleport index never reaches this store |
| External region-hash type 3 (`0x14012647D`) | `(v>>18) & 3` |
| Walk form **with** the extra-step bit (`0x140125A7F`) | hard-coded `&table[3]` |

So a compact form renders at whatever speed the slot was last told. `ViewState.speedTokens[]`
mirrors that model exactly, and `movementForm` refuses a compact form unless the token already
equals the speed the move needs. When it does not, the absolute form carries the move *and*
refreshes the token, so at most one frame per speed change pays for the wider form - the
mechanism is self-healing rather than a permanent fallback.

Two consequences worth knowing:

- **A session's own first move is always the absolute form.** `initialScene` writes the local
  player 30 tile-hash bits and no speed field, and where the client's own slot token comes from
  was not disassembled, so it starts `SPEED_TOKEN_UNKNOWN`. This is also why every pre-existing
  byte anchor in the test class - all of which are first frames after `initialScene` - still
  passes unchanged, including
  `oneWalkStepReproducesTheShippedSinglePlayerWalkStepBytes`.
- **An external slot's token is known from the scene.** The 20-bit init record's top two bits
  are that index, so an external player's first local move may go straight to a compact form
  (`anExternalSlotsSpeedTokenComesFromItsInitRecord`).
- **An EMPTY slot's token is known too, and it happens to be the run index.** `initialScene`
  writes `EMPTY_SLOT_SPEED` (3, the value the shipped client-verified scene has always
  carried) into every unoccupied slot's record, and the same init store gives the client
  `&table[3]`. Since `MOVEMENT_RUN` is also 3, a stranger who walks into view already running
  can use the compact run form on its very first step. The M4 fix pass audited that path
  rather than assuming it (`aStrangerWhoArrivesRunningKeepsTheEmptySlotsOwnSpeedToken`):
  an empty slot's cached region hash is 0, so an add always emits a region-hash update and
  for any real distance that is the type-3 form, which **sends** the speed index
  (`0x14012647D`); and where no update is emitted the token is still the client's, because
  none of the three add-path helpers writes the slot record's `+0x28` - `0x14012ADE0`'s only
  `[reg+0x28]` store is inside the `0x38`-stride loop it initialises at `actor+0x10D0`,
  `0x1401305F0` is a byte-stream reader (argument `+0x68` is a buffer base, `+0x70` a cursor)
  whose `+0x28` store is a DWORD while the slot record's `+0x28` is the QWORD pointer
  `0x140125220` writes, and `0x14012F0A0` touches no `+0x28` at all.

The token is kept, not cleared, by `ViewState.forget` and by a removal, for the same reason the
region record is: it lives on the slot object `[r14+0x10][index]`, not on the entity
`[slot+0x38]` that `0x140125906` destroys.

## The extra-step flag: emitted, never set

The flag is part of the form and is always written, always clear. Setting it does two things
(`MOVEMENT_TABLES.md` section 2): it queues a **second** position - `base + cardinal` from a
further 2 bits at `0x140125A6C`, then `base + direction` from the 3 bits already read, both
applied to the *unmodified* base - and it forces the slot's speed token to table entry 3 at
`0x140125A7F`.

The evidence agent labelled the *purpose* of that form (a corner-turning two-tile run) as
interpretation. It is not needed either way: Ataraxia's own movement loop refuses to produce the
case (Entity.java:1533-1538), so the sub-step is never emitted and nothing rests on the
interpretation.

## Also tightened

`Actor.Builder.movementType` now refuses anything above `MAX_SPEED_INDEX` (4). The 3-bit speed
fields can express 5, 6 and 7, but the table at `0x140C9C888` is exactly five int32 entries and
`0x140C9C89C` begins an unrelated `OggS` datum, so the client would form a pointer into foreign
memory. Nothing in the tree passed such a value; the guard is there so nothing ever can.

## Regression anchors held by the tests

Every pre-existing anchor is unchanged: the shipped initial scene, `00 7F F4`,
`C0 7F F4 00 00 04 ...`, all eight `singlePlayerWalkStep` bodies with and without appearance, the
long teleport form, the two-session add, the three region-hash selectors, the remove form, the
5/8/11-bit skip boundaries, the header extension markers and the refused CANDIDATE mask.

New:

- both tables agree cell for cell with the 910 helpers, and the run table has no interior;
- the first local move keeps the absolute form because the token is unknown, and the token is
  known afterwards;
- all 8 walk directions write selector 1, the right 3 bits and a clear flag;
- all 16 run directions write selector 2 and the right 4 bits;
- a two-tile move picks run and a one-tile move picks walk, with one absolute frame in between
  when the speed changes;
- a running player who advanced one tile, and a three-tile step, fall back to the absolute form;
- a plane change and a teleport never use a compact form, and the short-form teleport resets the
  token to 0;
- running with an appearance change still emits one combined frame;
- an external slot's token comes from its init record;
- a speed index outside the client's table is refused.

## One file outside this area

`Native947EquipmentSmoke.playerOutput` decoded `PLAYER_INFO` on the assumption that a local move
is always the absolute form: it computed the mask offset as `moved ? 5 : 3` and read a 15-bit
relative position out of the first three bytes. With the compact forms that offset is wrong, so
the decoder now reads the 2-bit form selector, derives the local pass width from it (1 byte for
forms 0/1/2, 3 for the short absolute form, 5 for the long one), and decodes the walk and run
directions by inverting `Native947PlayerInfo.walkDirection` / `runDirection` so the tables stay
in one place. Every existing assertion is kept at its original strength, including
`(relative>>>12) == MOVEMENT_WALK`; two new ones were added (the extra-step flag must be clear,
and a direction must resolve in the verified table), and failure messages now carry the frame
hex.

That smoke currently fails **after** this decoder, on
`"Player update must contain only the appearance mask"`, because the new
`Native947EntityMasks.playerSource` (a concurrent M4 area, not in the baseline mirror) now emits
the face-angle mask `0x80`. The frame it rejects is `c0 7f f4 00 00 80 00 00` - a mask-only
frame this decoder parsed correctly. That assertion belongs to whoever installed the mask source.
