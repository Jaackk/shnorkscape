# Skills 27/28 and the 947 skill model (Area A)

The skill model is now the 947 cache's, not Ataraxia's 910 one. The stat definitions in
flat cache index 28 group 9 (the `STAT` defaults group `StatDefaults.kt` decodes) are the
single source of truth for how many stats there are, what they are called, what each one
is capped at, and which experience curve each one rides. Everything below is transcribed
from the evidence pass at
`OpenNXT/data/prot/947/generated/native947-3/verified/ui/STAT_DEFINITIONS.md`
(container SHA256 `916be2681091edcb1ff4df43bb804b364d8dd2d10456a846e15e59818e52ca42`,
decoded payload SHA256 `d0b5852c8b53ff9076e70b0d2346cc83d4406bed722bc0af1da5ab561decc418`).

Nothing here comes from RuneScape general knowledge. Every number below can be pointed at
in the cache.

## 1. What changed, in one line each

- `Skills.SKILL_COUNT = 29` is the one constant the whole model reads. It is the count
  byte of opcode 1 in `28/9`, cross-checked against enum 680 (29 names) and enum 10865
  (size 29).
- `ARCHAEOLOGY = 27` and `NECROMANCY = 28` exist, with `SKILL_NAME` entries spelled the
  way enum 680 spells them.
- Every skill's cap comes from `Skills.getLevelCap(int)`, backed by the `LEVEL_CAP` table
  transcribed from the stat definitions. There is no "120 for Dungeoneering and Slayer,
  99 for everything else" rule any more.
- Every skill's curve comes from `Skills.getXPForLevel(int, int)` /
  `Skills.getLevelForXp(int, double)`, which pick the table the stat definition asks for.
- `Skills.MAXIMUM_EXP` is 200,000,000, not 2,000,000,000.
- `getCombatLevel()` / `getCombatLevelWithSummoning()` are cache script 1432, including
  Necromancy.
- The native login burst is 29 UPDATE_STAT frames, so the skills tab no longer draws 27
  and 28 from the client's stat-table initialiser defaults. That closes
  `MIGRATION-BACKLOG.md` section 10.

## 2. The disagreement table - cache vs Ataraxia (this IS the rebalance)

15 existing skills change cap. 0 existing skills change curve (Ataraxia's `expArray`
already equalled enum 716 for levels 1..120 and its `INVENTION_XP_FOR_LEVEL` already
equalled enum 10699). 2 skills are new.

| id | skill | Ataraxia before | cache after | curve | xp at the new cap |
|---:|---|---:|---:|---|---:|
| 0 | Attack | 99 | **120** | default (enum 716) | 104,273,167 |
| 1 | Defence | 99 | 99 | default | 13,034,431 |
| 2 | Strength | 99 | **120** | default | 104,273,167 |
| 3 | Constitution | 99 | 99 | default | 13,034,431 |
| 4 | Ranged | 99 | **120** | default | 104,273,167 |
| 5 | Prayer | 99 | 99 | default | 13,034,431 |
| 6 | Magic | 99 | **120** | default | 104,273,167 |
| 7 | Cooking | 99 | 99 | default | 13,034,431 |
| 8 | Woodcutting | 99 | **110** | default | 38,737,661 |
| 9 | Fletching | 99 | **110** | default | 38,737,661 |
| 10 | Fishing | 99 | 99 | default | 13,034,431 |
| 11 | Firemaking | 99 | **110** | default | 38,737,661 |
| 12 | Crafting | 99 | **110** | default | 38,737,661 |
| 13 | Smithing | 99 | **110** | default | 38,737,661 |
| 14 | Mining | 99 | **110** | default | 38,737,661 |
| 15 | Herblore | 99 | **120** | default | 104,273,167 |
| 16 | Agility | 99 | 99 | default | 13,034,431 |
| 17 | Thieving | 99 | **120** | default | 104,273,167 |
| 18 | Slayer | 120 | 120 | default | 104,273,167 |
| 19 | Farming | 99 | **120** | default | 104,273,167 |
| 20 | Runecrafting | 99 | **110** | default | 38,737,661 |
| 21 | Hunter | 99 | **110** | default | 38,737,661 |
| 22 | Construction | 99 | 99 | default | 13,034,431 |
| 23 | Summoning | 99 | 99 | default | 13,034,431 |
| 24 | Dungeoneering | 120 | 120 | default | 104,273,167 |
| 25 | Divination | 99 | 99 | default | 13,034,431 |
| 26 | Invention | 120 | 120 | **elite** (enum 10699) | 80,618,654 |
| 27 | Archaeology | - | **120** | default | 104,273,167 |
| 28 | Necromancy | - | **120** | default | 104,273,167 |

Only stat 26 sets `flags & 0x4` in its definition, and its table id is 0, which is
byte-identical to enum 10699. Stats 27 and 28 carry no table id and have no enum 10866
entry either, so both resolve to the default series, enum 716, exactly like the other 26.

## 3. Maximum experience: 2,000,000,000 -> 200,000,000

`Skills.MAXIMUM_EXP` was a 910 value. The 947 ceiling is 200,000,000 (`0x0BEBC200`):

- the live stat table the UPDATE_STAT sink writes (`[[owner+0x198E0]+0x7618]`) is built
  with entry flag 0, and the setter `0x140369C10` clamps a flag-zero entry at
  `0x0BEBC200`;
- cache script 8489 instruction 630 compares the stored experience against the literal
  `200000000` to light the "maxed" graphic;
- neither cache curve can express more - elite tops out at 194,927,409 and the default
  curve crosses 200,000,000 between level 126 and 127.

Consequences, handled explicitly:

- **Stored experience above the new ceiling** is clamped to 200,000,000 the first time the
  skill is touched. `Skills.setPlayer` clamps every stat on load, and `addXp`,
  `silentAddXp` and `addSkillXpRefresh` clamp on every award. A profile written under the
  old ceiling therefore loses the excess, which is only reachable at all on a save that
  had more than 200m in a skill.
- **The level is recomputed from the experience**, not trusted, on load - the same rule the
  client uses. `setPlayer` raises a stored level that is below the recomputed base (which
  is what a 99 stored under the old cap looks like for a skill the cache caps at 110/120);
  a level above the base is left alone, because that is a live potion boost.
- **The milestone announcements** were 104m, 250m, 500m, 1000m, 1500m and 2000m. The last
  five are unreachable by construction now and their calls are gone (the `LevelUp.send*`
  methods themselves are untouched, in case another caller wants them). What remains:
  - 104,273,167 still fires `LevelUp.send104m`, but only for a skill the cache caps at 120
    on the default curve - its text says "level 120", which was a lie for the 99- and
    110-capped skills it used to fire for;
  - reaching 200,000,000 sends a plain message naming the real ceiling. There is no world
    announcement for it because the only existing one (`send2000m`) says "2,000,000,000".
- **The total-xp announcement ladder** (`sendTotalXpAnnouncements`) is unchanged and is now
  mostly unreachable: its tiers are 5B, 10B ... 50B, and the maximum total is
  200,000,000 x 29 = 5,800,000,000. Tier 0 (5B) and the final tier (max total) still fire,
  in that order; tiers 1..9 cannot. This is server flavour with no cache basis, so it was
  left alone rather than invented anew.

## 4. Combat level: cache script 1432

```
m = max(base(0 Attack) + base(2 Strength), 2*base(4 Ranged), 2*base(6 Magic), 2*base(28 Necromancy))
a = (m * 13) / 10
combat = (a + base(1 Defence) + base(3 Constitution) + base(5 Prayer)/2
            + (members ? base(23 Summoning)/2 : 1)) / 4
```

Every division truncates where it appears. `getCombatLevelWithSummoning()` is the members
branch (`op_034F() == 1`), `getCombatLevel()` is the non-members branch, which substitutes
a literal `1` for `floor(Summoning/2)` and does not read stat 23 at all.

Four differences from the 910 formula, all because the client says so: Ranged and Magic
weigh **x2**, not x1.5; **Necromancy** is a fourth candidate in the same `max`; the
rounding happens at `/10`, at each `/2` and at `/4` instead of once at the end in floating
point; and the `min(126)` / `min(138)` clamps **do not exist** - the script has no final
clamp and the cache caps allow a maximum of 152.

This is visible, not cosmetic: the 947 client computes and displays its own combat level,
so a server that disagreed would show one number in the appearance block and another in
the tab.

## 5. Hardcoded counts and hardcoded 99s

### Fixed to follow the model

| file | what it was | what it is |
|---|---|---|
| `game/com/rs/game/player/Skills.java` | `new short[27]` / `new double[27]` / `new int[27]` in the constructor and in `setPlayer` | all `SKILL_COUNT`, through one `grow(...)` helper per primitive type |
| `game/com/rs/game/player/Skills.java` | `getLevel` / `getXp` bounds tested `skill > 26` | `skill >= SKILL_COUNT` |
| `game/com/rs/game/player/Skills.java` | `handleSkillShards` compared 26 skills against a literal `99` | compares against `getLevelCap(skill)` |
| `game/com/rs/game/player/Skills.java` | `getSkillName` was a second hand-written name switch (and called stat 3 "Hitpoints") | delegates to `SKILL_NAME`, i.e. to enum 680 |
| `content/com/rs/game/player/content/Commands.java:3180` | `for (int i = 0; i <= 26; i++)` | `i < Skills.SKILL_COUNT` |
| `game/com/rs/game/player/dialogue/impl/godwars2/jmodcommands.java:85` | `for (int i = 0; i <= 26; i++)` | `i < Skills.SKILL_COUNT` |
| `content/.../distinctioncape/DistinctionCape.java` | `isMaxed` was 27 hand-written `>= 99` checks | `skillsBelowCap(player) == 0`, looping all 29 against `Skills.getLevelCap` |
| `content/.../distinctioncape/DistinctionCapeInterface.java` | max-cape rows read `(level/99)` for every skill | read `(level/cap)`, and only the skills still below their cap are listed |
| `game/com/rs/game/player/client/Native947PacketDispatcher.java` | `MAX_MODELLED_SKILL = 26`, so 27 and 28 were counted drops | `Skills.SKILL_COUNT - 1`, so all 29 reach the wire and only 29+ / negative ids drop |
| `game/com/rs/game/player/skills/SkillsListener.java` | `listenForNews` demanded 120 for Dungeoneering, Invention and Slayer and **99 for everything else** - the exact 910 rule this work abolishes | `getLevelForXp(i) < Skills.getLevelCap(i)` over `Skills.SKILL_COUNT`, so "maxed stats" means every stat at its own cache cap |
| `game/com/rs/game/player/skills/SkillsListener.java` | `listenForMusicEffects` indexed a **27-entry** jingle table with the raw stat id, so an Archaeology or Necromancy level-up threw `ArrayIndexOutOfBoundsException` | bounds-guarded: stats outside the 910 table get no jingle (see below) |

### Deliberately left alone, with reasons

- **`core/com/rs/network/packet/impl/InventoryOptionsHandler.java:3947`** - the brief
  listed this as a skill loop. It is not: it is
  `for (int i = 0; i < 27; i++) player.getInventory().getItem(i)`, an INVENTORY scan for
  Silverhawk boots. Touching it would be an unrelated (and possibly real, since the
  inventory has 28 slots) bug fix, so it was left exactly as it was.
- **`InventoryOptionsHandler` lines 832 and 881**, `getLevel(Skills.CRAFTING) >= 99` - a
  content gate on a specific craft, not a "this skill is mastered" test. Crafting's cache
  cap moved to 110, so these now gate at 99 out of 110, which is what they meant.
  Re-pointing them at the cap would silently make the content harder; that is a content
  decision, not a cache one.
- **`SkillsListener.skillLevelUpMusicEffects`** - a 910 asset table of level-up
  music effect ids, one per 910 stat, indexed by raw stat id. There is no cache
  evidence for an Archaeology or Necromancy level-up jingle, so the table is left
  at 27 entries and the lookup is bounds-guarded (`skill < 0 || skill >= length`
  returns): stats 27 and 28 level up silently rather than playing an invented id.
  Same treatment as `SHARDS` and `SKILL_MENU_COMPONENTS` below. This site was
  missed by the earlier sweeps because it is a lombok `val` declaration, so a
  scan for `int[] name = {...}` does not see it; without the guard the widened
  `petrates` loop in `Commands.java:3957` crashes a legacy player on stat 27.
  (Native players never reach it - `Skills.announceLevelUp` short-circuits to a
  game message at `Skills.java:1416` - but the crash was real for legacy ones.)
- **The "maxed stats" world announcement is now unreachable until content awards
  Archaeology or Necromancy experience.** The loop covers all 29 stats against
  their own caps, which is the same rule `DistinctionCape.isMaxed` uses, and
  nothing awards experience in 27/28 yet. That is one rule across the codebase
  rather than two, and it is a content gap, not an engine one. The alternative -
  excluding 27 and 28 from "maxed" - would announce a maxed character who has two
  stats at level 1.
- **The `staticLevel == 99` / `== 120` milestone branches under it are unchanged.**
  They are 910 flavour with no cache basis, and after the rebalance a stat that
  reaches its own cap of 110 announces nothing (it announced at 99 on the way).
  Re-pointing them at the cap is a content decision, so it was not taken here.
- **`SHARDS`, `FIXED_SLOTS`, `SKILL_MENU_COMPONENTS`, `getCounterSkill`,
  `getSkillBonusXpVarId`, `getSkillId`** in `Skills` - these are 910 interface orderings
  and item/varp id tables, not the skill model. There is no cache evidence for an
  Archaeology or Necromancy entry in any of them, so inventing ids would be worse than
  having none. `getCounterSkill` and `getSkillId` return `-1` for 27/28 and every caller
  already handles `-1`; `refreshXpBonuses` now skips a `-1` var id instead of writing it
  (the 947 evidence says varps 3304.. are the XP TARGET vars anyway, `SKILLS_TAB.md` 9.6).
- **`unlockSkills`** sends `sendIComponentSettings(..., 0, SKILL_NAME.length - 1, 30)`,
  which is now `0..28`. This is the 910 path only (interface 1466/320); a wider range on a
  narrower interface is inert.
- **`getVirtualLevel`** still computes its own series up to 120. Every cache cap is <= 120,
  so it is still an upper bound; it was not rewritten because virtual levels above the cap
  are a display feature with no cache binding in this pass.
- **`Skills()` seeds Constitution with `xp[3] = 1155`** where the cache curve puts level 10
  at 1,154. 1155 still resolves to level 10, the evidence calls it cosmetic, and the wire
  fixtures in `Native947DispatcherTest` are transcribed against it. Left alone.

## 6. Persistence

- **Legacy 910 `.p` saves are out of scope, by the owner's decision.** No
  Java-serialization migration was written, there is no `readObject` hook, and `.p`
  compatibility shaped nothing here. `Skills` is still `Serializable` and its
  `serialVersionUID` is unchanged, so an old `.p` still deserialises; `setPlayer` then
  widens its 27-long arrays to 29 as a side effect of the normalisation that was already
  there. If a `.p` save fails to load for some other reason after this change, that is
  accepted and not worth fixing.
- **Native schema-3 profiles must and do still load.** Experience is treated as the durable
  truth: `setPlayer` clamps stored experience to the new maximum and recomputes the base
  level from the new curve rather than trusting a stored level, which is exactly what the
  947 client does on every UPDATE_STAT. The SKILLS section width itself is
  `Native947Save`/`Native947SaveStore` work (Area B); this file only guarantees that a
  `Skills` object built from stored experience lands on the cache's levels.

## 7. Tests

`tests/modern947/Native947SkillModelTest.java` is cache-free and hermetic. Every expected
value is transcribed from the evidence pass into checked-in tables at the top of the file
(`ENUM_680`, `CACHE_CAP`, `USES_ELITE_CURVE`, the enum 716 and enum 10699 key rows and the
200,000,000 ceiling) and is never read back out of the code under test. It asserts:

- the count is 29 and `SKILL_NAME` is enum 680 in order, including 27 and 28;
- every skill's cap equals the stat definition's, and the 9/8/12 split across 99/110/120;
- the default curve is enum 716 and the elite curve is enum 10699, and only stat 26 uses
  the elite one;
- the experience at each skill's own cap;
- each curve round-trips xp -> level -> xp at levels 1, 98, 99, the 99/100 boundary, 100
  and the cap, and that no amount of experience takes a skill past its cap;
- the 15 raised skills reach their new caps and the 9 unchanged ones do not;
- experience above 200,000,000 clamps, with the level following the per-skill cap;
- the total level sums all 29 stats, as script 4708 does;
- combat level reproduces script 1432 on both branches, for an all-level-1 character, a
  **Necromancy-only** character (64 members / 65 free), Magic-only and Ranged-only (proving
  the x2 weight), a Prayer+Summoning case (proving both `/2` truncations) and the
  cache-maximum 152 - reached identically by 120+120 melee and by 120 Necromancy alone.

Two pre-existing tests had their premise updated because this work removes it, not
weakened: `Native947StatsTest` (the login burst is 29 frames, and 27/28 are no longer
"never faked") and `Native947DispatcherTest` (the out-of-range skill ids that must drop are
now 29 and -1, not 27 and 28).

## 8. Integration pass: two more hardcoded counts

Found during integration by re-sweeping the tree for skill-shaped loops bounded by a
literal. Both were missed by the area passes and both are fixed:

| file | what it was | what it is | why it matters |
|---|---|---|---|
| `game/com/rs/game/World.java:556` | `for (int skill = 0; skill < 26; skill++)` in the 100-tick boost/drain restore task | `skill < Skills.SKILL_COUNT`, with `SUMMONING` **and** `INVENTION` skipped explicitly | the literal 26 already excluded Invention; after the widening it also excluded Archaeology and Necromancy, so a boost or drain on 27/28 never decayed. Adding the explicit Invention skip keeps stats 0..26 behaving exactly as before, so only 27 and 28 change. |
| `content/com/rs/game/player/content/Commands.java:3957` | `for (int i = 0; i < 26; i++)` in the `petrates` test command | `i < Skills.SKILL_COUNT` | `TEST_SERVER_MODE` only; it awarded experience to 26 of 29 stats. |

Re-swept and confirmed NOT skill loops (left alone): `ShardsBag:114` and
`GeneralTafaniD:93` iterate the 26-entry `SHARDS` table; `Commands:5681`,
`ClanBank:145`, `DungeonManager:787`, `FistOfGuthix:278`, `GemBag:87`,
`ItemsKeptOnDeath:186`, `Magic:1449/1471/1576`, `Bank:2507`, `ArtefactExchangingD:29`,
`Player:9109` and `InventoryOptionsHandler:3947` are 28-slot inventory scans;
`DuelRules:11` is a rules bitset; `DungeoneeringJournals:62` is a journal page index.

## 9. Fix pass: the wire multiplier

`Native947Packets.updateStat` was sending `experience * 10`. It now sends whole
experience. The full derivation - the `+0x7618` displacement scan, the flag byte
at `0x1400CDF25`, the setter's two branches at `0x140369C21`, and the cache-script
corroboration - is in `notes/SKILLS-persistence.md` section 5, together with the
list of test fixtures whose premise moved with it.

This matters HERE because it is what makes the rebalance visible: under `* 10`
the client would have computed every base level from ten times the real
experience and pinned nearly every stat at the new cap on sight, so none of the
cap changes in section 2 would have shown in the tab.
