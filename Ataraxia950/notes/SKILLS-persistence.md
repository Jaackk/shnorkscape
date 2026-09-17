# Skills persistence and the wire, at 29 stats (area B)

Scope: the native schema-3 profile (`Native947Save`, `Native947SaveStore`,
`Native947PlayerBinder`), the login stat burst (`Native947Session`,
`Native947StatsUi`) and their tests and smoke. The stat model itself - counts,
caps, curves, the `Skills` class - is area A; everything here consumes it.

Legacy 910 `.p` account saves are out of scope by the owner's decision. Nothing
in this area reads or writes them, and no compatibility shim was added for them.

---

## 1. What changed

### The SKILLS section now carries 29 stats

The section was already self-describing - a leading count byte, then ten bytes
per stat (`short` level, `double` experience) - so widening the model from 27 to
the cache's 29 is **not a schema bump**. The file still says schema 3. Only the
count byte and the payload length move, 271 bytes to 291.

`Native947Save.SKILL_COUNT` is no longer the literal `27`. It reads
`com.rs.game.player.Skills.SKILL_COUNT`, so the profile width cannot drift from
the engine model: if area A ever moves the stat count again, the file follows on
the next rebuild with no edit here.

### An existing 27-stat profile still loads, and is upgraded in place

`Native947SaveStore.decodeSkills` accepts exactly two widths - the current one
and `Native947Save.LEGACY_SKILL_COUNT` (27). Anything else is still a hard
failure, and a 27 count byte on a 29-stat payload fails too (it leaves trailing
bytes, which the section reader already rejects).

An older-width section goes through `Native947Save.Skills.upgraded`, which:

1. copies every stored experience value **verbatim** - experience is the durable
   truth and a cap or curve change cannot invalidate it;
2. **recomputes every level** from that experience with the engine's current
   per-stat curve, via `Native947Save.levelForXp` ->
   `Skills.getLevelForXp(int, double)`. The stored level was derived from the 910
   caps and curves and the 947 rebalance may have moved it, so it is discarded,
   not trusted. This is exactly what the native client does: `UPDATE_STAT.md`
   records that the client's own stat setter recomputes the base level from the
   stat definition's xp table rather than being told a level;
3. gives the two stats the older model had no column for (27 Archaeology,
   28 Necromancy) **level 1 and no experience** - the same initial state the
   client's own stat table holds before any `UPDATE_STAT` arrives.

A boost or drain that happened to be frozen into an old file is dropped by the
recompute. That is the correct outcome: a login restores base levels anyway.

A section already at the current width is **not** recomputed. It can only have
been written by the current model, so its levels - boosts included - are trusted
verbatim, and `everySkillAndEveryVitalRoundTripsThroughSchemaThree` still pins
that exact round trip.

The rewrite happens on the first checkpoint of any kind, because `encode` always
writes the whole file and `Native947Session.checkpoint` dirties IDENTITY on every
login (its `lastLogin` is the session start). So the on-disk section reaches full
width on the first tick that saves at all, not only when a stat changes.

### `MAX_XP` follows the engine, and an over-ceiling value is clamped, not rejected

`Native947Save.MAX_XP` was the literal `2000000000d`. It is now
`Skills.MAXIMUM_EXP`, which area A lowered to **200,000,000** on cache evidence
(the client's stat setter clamps a flag-zero entry at `0x0BEBC200`, and cache
script 8489 compares the stored experience against the literal 200000000).

Because that ceiling **went down**, a profile written before the rebalance could
legitimately hold more than the model now accepts. The store therefore **clamps**
such a value on load and prints one line naming the stat, rather than failing the
load - a hard rejection would lock the owner out of exactly the profile this
upgrade exists for. The `Native947Save.Skills` constructor still rejects anything
above `MAX_XP`, so programmatic misuse fails closed; only the decoder is lenient,
and it is loud about it.

#### The clamp is recoverable: `<hash>.947.pre29.bak`

Clamping on load is only half the story, because `Native947Session.checkpoint`
rewrites the WHOLE file on the first saving tick (IDENTITY is dirty on every
login), so the pre-clamp experience would be gone from disk with nothing to
restore it from. `Native947SaveStore.save` therefore takes **one** copy of the
existing file, to `<hash>.947.pre29.bak`, immediately before its first atomic
replacement over a pre-rebalance profile.

- "Pre-rebalance" means the bytes on disk are something this build cannot
  reproduce: an older-width SKILLS section, or a stored experience the decoder
  had to clamp. `decodeSkills` / `clampExperience` set a thread-local flag, and
  `save` reads it straight after its own protective `load` on the same thread.
  It is a property of the BYTES, not of the decoded profile, which is why it is
  not a field on `Native947Save`.
- The copy is **created, never replaced** (`Files.copy` with no
  `REPLACE_EXISTING`), so the second checkpoint - which by then has an
  already-clamped file to offer - cannot overwrite the good backup.
- A failed copy is reported and the save proceeds. Refusing to write would lock
  the owner out of a profile that loads perfectly well, which is the opposite of
  the point.
- Nothing reads `.pre29.bak` back. It exists so the owner can restore it by hand
  if the 200,000,000 ceiling ever turns out to be wrong. The store addresses
  profiles by SHA-256 of the name, so a stray sibling file is never mistaken for
  one.

Pinned by `Native947SaveStoreTest.aPreRebalanceProfileIsCopiedOnceBeforeTheFirstRewrite`,
which covers both routes in (older width, over-ceiling experience), the
never-replaced rule, and the negative case that an in-model profile leaves
exactly one file behind.

No profile in this workspace triggers it - the live profile `x` peaks at 1155 xp -
so this is a latent path closed cheaply, not an observed loss.

### `MAX_SKILLS_SECTION` was checked, not raised

1 + 29 * 10 = 291 bytes, against a cap of 512. It already fits, so the constant is
unchanged; raising it would alter nothing on disk and only widen what a corrupt
length header can claim. `maxSkillsSectionHoldsTheWholeStatTable` now pins the
arithmetic so a future stat cannot silently overflow it.

### The login burst and `Native947StatsUi` cover 29 rows

Both loops were already written against the engine's own table rather than a
literal, so they followed area A automatically:

- `Skills.initNative947` (the burst `Native947Session.sendNative947LoginState`
  triggers) loops `level.length`;
- `Native947StatsUi.ENGINE_STATE` loops `Skills.SKILL_NAME.length`.

Both are now 29, and 27/28 are ordinary rows in them. The stale comments that
said Ataraxia has no Archaeology or Necromancy and therefore leaves them at the
client's initialiser defaults have been removed. The fail-closed rule is
unchanged: every frame still leaves through the packet facade, so an id the
binding table does not declare is a counted drop and never a wire write.

Live evidence from the persistence smoke: `sendSkillLevel=29` in the facade
counters of the login burst, with `dropped=0` and `strictHits=0`.

---

## 2. Effect on the owner's live profile

The running dev server's profile `x`
(`data/modern947/players/2d711642...4881.947`, schema 3, 27-stat SKILLS section)
was **copied** and loaded read-only with the new code. It loads. Its stored
experience is all zero except Constitution 1155, so no level moved, and 27 and 28
came in at level 1 / 0 xp. The file itself was not touched and the running server
was not disturbed.

Per-stat caps as they now resolve for that profile, straight from
`Skills.getLevelCap` (which area A decoded from the cache stat definitions and
cross-checked against enum 10865):

| id | stat | cap | id | stat | cap | id | stat | cap |
|---|---|---|---|---|---|---|---|---|
| 0 | Attack | 120 | 10 | Fishing | 99 | 20 | Runecrafting | 110 |
| 1 | Defence | 99 | 11 | Firemaking | 110 | 21 | Hunter | 110 |
| 2 | Strength | 120 | 12 | Crafting | 110 | 22 | Construction | 99 |
| 3 | Constitution | 99 | 13 | Smithing | 110 | 23 | Summoning | 99 |
| 4 | Ranged | 120 | 14 | Mining | 110 | 24 | Dungeoneering | 120 |
| 5 | Prayer | 99 | 15 | Herblore | 120 | 25 | Divination | 99 |
| 6 | Magic | 120 | 16 | Agility | 99 | 26 | Invention | 120 |
| 7 | Cooking | 99 | 17 | Thieving | 120 | 27 | Archaeology | 120 |
| 8 | Woodcutting | 110 | 18 | Slayer | 120 | 28 | Necromancy | 120 |
| 9 | Fletching | 110 | 19 | Farming | 120 | | | |

What this means for a profile that DOES carry experience: a stat whose cap rose
(seven to 120, eight to 110) and that was sitting pinned at 99 will come back at
the level its experience actually buys, which may be above 99 - the recompute is
what delivers the rebalance to an existing character. No stat's cap fell, so no
level can be reduced by a cap change. No existing stat's curve changed (only
Invention uses a non-default table, and it already matched), so no level can be
reduced by a curve change either.

---

## 3. Deliberate non-goals

- **Legacy `.p` saves.** Not read, not written, not migrated. If an old `.p`
  save no longer loads, that is accepted scope.
- **The 1155 Constitution seed.** `Native947Save.Skills.fresh()` still mirrors the
  engine constructor's Constitution 10 / 1155 xp. The cache curve puts level 10 at
  1,154, so 1155 is one point of 910 sloppiness, but it still resolves to level 10
  under the recompute and the client would show the same thing. Left alone; it is
  the engine's number to change, not the profile's.
- **Raising `MAX_SKILLS_SECTION`.** See above - it already fits.

---

## 4. Tests and evidence

New or extended, all passing in the 34-class / 341-test run:

- `Native947SaveStoreTest.legacyTwentySevenStatProfileUpgradesInPlaceKeepingEveryExperience`
  builds a genuine 27-stat schema-3 file with an **independent** fixture encoder
  (never the current one), stores a deliberately wrong level 3 in every stat, and
  asserts: it loads; all 27 experience values survive with delta 0.0; the anchors
  come out at the cache curve's own rows (101,333 -> 50, 1,155 -> 10,
  13,034,431 -> 99, 104,273,167 -> 120); every level matches the engine curve;
  27 and 28 are level 1 / 0 xp; a re-save writes a 29-stat count byte at the exact
  file offset; and the reload is identical.
- `Native947SaveStoreTest.maxSkillsSectionHoldsTheWholeStatTable` pins the
  1 + 29 * 10 arithmetic and reads the section length back off a real file.
- `Native947SaveStoreTest.corruptSectionsFailClosedWithoutOverwriting` now walks
  the section offsets from `SKILL_COUNT` and adds three count-byte mutations
  (26, 28, 30) plus the 27-on-a-29-payload case.
- `Native947SaveStoreTest.sectionValuesAreValidatedBeforePersistence` checks both
  array widths, the over-cap experience and both `upgraded` rejections.
- `Native947StatsTest.theLoginBurstCarriesArchaeologyAndNecromancyAtLevelOneWithNoExperience`
  and `.theNewStatsCarryTheirOwnExperienceAndBoostedLevel` read the two new stats
  off the real wire: level 1 / 0 xp for a fresh character, and the boosted current
  level with WHOLE experience once they carry any (see section 5).
- `Native947StatsUiTest.theEngineStateBurstCoversEveryStatTheCacheDefines` counts
  `ENGINE_STATE`'s `UPDATE_STAT` frames and requires all 29 ids including 27 and 28.
- `Native947PersistenceSmoke` sets Archaeology 50 / 101,333 and Necromancy
  99 / 13,034,431 on the world thread and requires them on disk before disconnect,
  in the fresh JVM's read of the file, and on the restored `Player` - plus that the
  restored player's stat arrays are `SKILL_COUNT` long.

Cache evidence for every number quoted here:
`OpenNXT/data/prot/947/generated/native947-3/verified/ui/STAT_DEFINITIONS.md`
(stat definitions, caps, curves, the 200,000,000 ceiling) and
`.../verified/UPDATE_STAT.md` (the client recomputes the base level from
experience).

---

## 5. The wire carries WHOLE experience, not experience x10 (fix, this pass)

`Native947Packets.updateStat` sent `experience * 10`. That was wrong and is now
`out.i32(experience)`.

`UPDATE_STAT.md` concluded "every entry carries flag 1, so the wire experience is
in tenths; a server must send `xp * 10`" from the entry construction at
`0x1402FE438`, which belongs to function `0x1402FE290` - a DIFFERENT stat table.
`STAT_DEFINITIONS.md` section 3b.1 corrected it. This pass settled it by
disassembling the image directly rather than by a live check:

1. **Only two instructions in the whole `.text` write the `+0x7618` field**
   (scanned the section for the `18 76 00 00` displacement: 28 hits, 26 of them
   reads or unaligned false positives). They are `0x1400CCD60`
   (`mov qword [rbx+0x7618], r15`, the null at construction) and `0x1400CDF8E`
   (`mov [rax], rbx` through `lea rax,[rdi+0x7618]`).
2. **The entries `0x1400CDF8E` publishes are built with flag byte 0.** At
   `0x1400CDF25`: `mov byte [rsp+0x48], r15b`. `r15` is the function's zero
   register - `0x1400CDF0A` does `mov edx, r15d` and `edx` is then the loop index
   that starts the `movsxd rax, edx; inc edx` walk at index 0.
3. **The setter branches on that byte.** `0x140369C15` loads `entry+8`;
   `0x140369C21` `je` takes the flag-zero path, which clamps at
   `0x0BEBC200` = 200,000,000 (`0x140369C37`) and scans the definition's xp table
   at `0x140369C49..0x140369C7E` against the **raw** value. Only the flag-nonzero
   path (`0x140369C92`) divides by ten.
4. The packet parser reads the same field: `0x140140483`
   `mov rdi, qword ptr [rcx + 0x7618]`, `rcx` = `[owner+0x198E0]`.

So the client stores what the wire says, unscaled, and derives the level from it
against enum 716 / enum 10699 - whose rows are whole experience (L99 = 13,034,431).
The cache scripts agree: script 8489 compares the raw stored value (`op_0517`,
handler `0x1401789B0`, no division) against the literal `200000000`.

Had `* 10` shipped, every base level the client computed would have been the level
for ten times the real experience, pinning nearly every stat at its cap on sight -
which would have made this whole cap rebalance invisible in the tab.

Changed with it, all premise updates rather than weakenings:

| file | before | after |
|---|---|---|
| `network/.../Native947Packets.java:150` | `out.i32(experience * 10)` | `out.i32(experience)` |
| `tests/modern947/Native947StatWireTest.java` | `07c4e576` / `00002d14` / `3dfd2400` / `77359400` / `0000000a` | `00c6e3bf` / `00000482` / `0632ea00` / `0bebc200` / `00000001` |
| `tests/modern947/Native947DispatcherTest.java:234` | `00002d1e` (11,550 tenths) | `00000483` (1,155) |
| `tests/modern947/Native947StatsTest.java` | `statExperienceTenths()`, `x * 10L` | `statExperience()`, `x` |
| `game/.../Native947PacketDispatcher.java` | doc said tenths / 2e9 flagged clamp | doc says whole / flag-zero clamp |

`Skills.MAXIMUM_EXP = 200000000` and the writer's `0..200000000` range check are
correct either way and did not move.

### Side finding, so the next reader does not re-explore it

Stat-definition flags **bit 0x1 is the members flag**, not a fractional-experience
selector. It is set for exactly the twelve members skills (15, 16, 17, 18, 19, 21,
22, 23, 25, 26, 27, 28) and clear for 0-14, 20 and 24 - Runecrafting 20 and
Dungeoneering 24 on the free side is the tell. `StatDefaults.kt` reads it and never
consumes it. It is therefore not the setter's `entry+8` flag, and there is no
per-stat cache reason for some stats to be in tenths and others whole.
