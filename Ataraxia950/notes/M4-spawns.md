# M4 area B: the world's NPCs, behind a 910 -> 947 id validity table

Written September 7, 2026. Covers the three things this area owns: the validity table the
backlog's P3 specified and nobody built, the lazy per-region spawner that drives
`Native947World.spawnNativeNpc` from `data/npcs/spawns.json`, and letting the spawned NPCs
wander.

| File | Owns |
|---|---|
| `game/com/rs/tools/modern/IdValidityProbe.java` | reads both caches, classifies every id, emits the table and the report |
| `resources/native947/id-validity-947.json` | the checked-in table (294 KB) |
| `game/com/rs/game/player/client/Native947IdValidity.java` | loads it; answers `isSafe(kind, id)` and `reason(kind, id)` |
| `game/com/rs/utils/data/parsers/npcs/NPCSpawnsDataParser.java` | the same spawn rows as plain data for the native world |
| `game/com/rs/game/Region.java` | one hook: a region whose collision finished loading announces itself |
| `game/com/rs/game/player/client/Native947World.java` | drains those announcements, filters, spawns, counts |
| `tests/modern947/Native947IdValidityTest.java` | 16 cache-free tests over a fixture, plus one that parses the shipped table |
| `game/com/rs/game/player/client/Native947NpcSmoke.java` | a real-cache spawn phase, and a `--spawns` mode that runs only it |

---

## 1. The id drift problem, measured

Ataraxia's JSON data is keyed by **910** definition ids. The client and every definition
served to it come from the **947** cache. The two numbering spaces overlap without agreeing,
and the dangerous class is not the ids that break - it is the ids that load perfectly and are
the wrong thing.

The measurement is now reproducible instead of quoted. `IdValidityProbe` reads the 910 cache
Ataraxia's data was authored against (`C:\Users\developer\Desktop\Ataraxia-PS\data\cache`, which
is byte-identical in its npc and item indexes to `C:\Users\developer\Desktop\910cache`) and the
947 flat cache (`C:\Users\developer\Desktop\rs3cache\cache`), and classifies every id in both.
`Cache.STORE` is one static store and the definition loaders size their tables from whichever
cache initialised them, so the two sides are dumped by separate JVMs and joined by a third,
cache-free run:

```
IdValidityProbe dump-legacy C:\Users\developer\Desktop\Ataraxia-PS\data\cache <data root> legacy.json
IdValidityProbe dump-modern C:\Users\developer\Desktop\rs3cache\cache        <data root> modern.json
IdValidityProbe classify legacy.json modern.json <data root> \
        resources/native947/id-validity-947.json build/id-validity-947-report.md
```

The two intermediate dumps are retained at `build/id-validity/legacy-910-dump.json` and
`modern-947-dump.json`, so the classify step can be re-run without either cache, and the
human report at `build/id-validity-947-report.md`. Re-running `classify` reproduces
`resources/native947/id-validity-947.json` byte for byte
(sha256 `245a4f0d6b9c91ef6751684590a022a4315c582c5e807637a039602c429f6265`).

Neither cache is written. The modern one is opened through `Cache.initFlatReadOnly`, which is
read-only by construction. The legacy `Store(String)` constructor has no read-only mode and
opens its `dat2`/`idx` handles `"rw"`, so the guarantee there is behavioural rather than
structural: the probe only ever calls `Index.getFile` and the definition getters, and touches
no method that writes. Point it at a copy if that is not good enough for you.

### Verdicts, and which of them is safe

| verdict | meaning | safe |
|---|---|---|
| `same` | the 947 definition exists and every compared field agrees | **yes** |
| `restyled` | every compared field agrees and the names are equal ignoring case, e.g. "Hill Giant" -> "Hill giant" | **yes**, by owner decision 2026-09-07 |
| `renamed` | exists, and reads as the same thing under a genuinely different string | no |
| `repurposed` | exists, and is something else - or kept the name and lost a field that matters | no |
| `missing` | no usable 947 definition (absent, nameless, or rejected by the strict decoder) | no |
| `unverifiable` | no 910 definition to compare against, so nothing can be concluded | no |

Compared fields, because **name equality alone is not sufficient**:

* every kind: the name - exactly for `same`, or ignoring case for `restyled`. **Case is the
  only difference the table forgives**, and it forgives it only after every field check below
  has run: a case-only rename whose size or option array also moved is still `repurposed`.
  Nothing else is folded - a moved word break, a dropped hyphen or a trailing space is a
  different string (see section 4 for what that still costs);
* items: `equipSlot` and `equipType` as well - 1079 "Rune platelegs" and 1333 "Rune scimitar"
  keep their names in 947 and lose their slot (`7 -> -1`, `3 -> -1`), which is exactly the
  Mining-and-Smithing "Convert" placeholder case the brief names;
* NPCs: the size as well - a footprint change moves the entity's collision and its wire
  footprint. Six spawn-referenced ids fail only on this, e.g. 6210 and 22487 "Hellhound"
  `2 -> 1`.

**Option parity** (added by the M4 fix pass; the backlog's P3 item (5) asked for it and the
first cut of this table did not do it). An option *index* is what a click is dispatched by:
`Native947ActionRouter.legacyNpcOption` maps native option n straight onto 910
`NPCHandler` option n. So a name-equal id whose option array moved is exactly as dangerous as
one that lost its equipment slot - the menu the client renders and the branch the 910 handler
runs would mean different things.

* NPCs: the **four forwarded** option slots are compared position-wise, normalised the same
  way names are (so "Talk-to" and "Talk to" are the same option, and an emptied or newly
  filled slot is a difference). 275 whole-index ids and 55 spawn ids move from `same` to
  `repurposed` on this rule alone, worth 72 spawn rows. Worked examples: 2333 "Vasquen"
  swaps `Talk-to` and `Pay`; 3021 "Tool leprechaun" turns slot 4 from `Teleport` into
  `Note produce`; 1087 "Penda" loses `Attack` from slot 2; 456 "Father Aereck" gains
  `View Gravestones` in a slot 910 leaves empty.
* The **fifth** NPC slot is deliberately not compared: `legacyNpcOption` refuses native option
  5 outright, so a difference there cannot reach a 910 branch at all. NPC 494 Banker is that
  case (947 gained `Load Last Preset from` where 910 is empty) and stays `same`, which is what
  keeps the banker - the anchor every smoke spawns - usable.
* Items: both option arrays (inventory and ground), all five slots, same normalisation. 339
  referenced item ids move to `repurposed`; item 6 "Cannon base" loses `Set-up`, 35
  "Excalibur" swaps `Wear`/`Activate`, 995 "Coins" replaces both of its first two.
* Objects are not compared on options: nothing reads an object verdict yet, and the objects
  the client renders come from the 947 map itself, which is self-consistent.

The `renamed` / `repurposed` split is an **advisory** string-similarity rule for the human
report. Both are refused, so a wrong answer there costs readability and nothing else; the
loader rejects any table that declares more than `"same"` as safe.

### Whole-cache figures

| kind | scope | total | same | renamed | repurposed | missing | unverifiable |
|---|---|---|---|---|---|---|---|
| npc | whole index | 28,660 | 22,120 | 49 | 649 | 923 | 4,919 |
| npc | referenced by Ataraxia data | 8,735 | 7,857 | 30 | 192 | 650 | 6 |
| item | whole index | 57,015 | 35,767 | 3,061 | 4,674 | 1,728 | 11,785 |
| item | referenced by Ataraxia data | 17,218 | 14,134 | 1,459 | 925 | 687 | 13 |
| object | referenced by Ataraxia data | 203 | 146 | 17 | 25 | 15 | 0 |

23,272 legacy npc definitions and 27,737 modern ones decoded (ids whose definition has no
name at all are not counted as definitions - the 947 cache carries thousands of nameless
placeholder slots, and a nameless definition is no evidence about an id). `unverifiable` for
NPCs is overwhelmingly new 947 content that never existed in 910.

Coverage of the checked-in table: **npc rows cover the whole of both indexes**, so
`isSafe(NPC, id)` answers for any id a tool might spawn. Item and object rows cover only the
ids Ataraxia's data files reference; an unreferenced item id answers `UNCLASSIFIED`, which is
refused - the correct answer for a caller that should be reading the cache directly.

### Per data file

| data file | kind | ids | same | renamed | repurposed | missing | unverifiable |
|---|---|---|---|---|---|---|---|
| `npcs/spawns.json` | npc | **2,394** | **1,814** | 21 | 89 | 464 | 6 |
| `npcs/combatDefs.json` | npc | 2,644 | 2,459 | 15 | 35 | 135 | 0 |
| `npcs/npcstats.json` | npc | 6,706 | 6,423 | 16 | 111 | 156 | 0 |
| `npcs/bonuses.json` | npc | 467 | 386 | 2 | 6 | 73 | 0 |
| `npcs/weaknesses.json` | npc | 2,328 | 2,268 | 12 | 12 | 36 | 0 |
| `npcs/drops.json` | npc | 1,089 | 999 | 8 | 17 | 65 | 0 |
| `npcs/drops.json` | item | 1,708 | 1,572 | 55 | 79 | 2 | 0 |
| `items/itemBonuses.json` | item | 5,391 | 4,715 | 399 | 235 | 29 | 13 |
| `items/itemsDisassembleData.json` | item | 14,350 | 11,659 | 1,326 | 704 | 661 | 0 |
| `items/shops.json` | item | 1,084 | 892 | 26 | 157 | 9 | 0 |
| `map/customObjectSpawns2.json` | object | 203 | 146 | 17 | 25 | 15 | 0 |

**Difference from the brief's figures.** The brief quoted 1,860 stable / 70 repurposed / 464
missing for spawns.json. The 464 with no 947 definition matches exactly; the stable count is
1,814 here and the rest splits into 21 renamed + 89 repurposed + 6 unverifiable. It is LOWER
than the brief's stable figure because the verdict compares more than the name (see "Option
parity" below): 55 spawn ids keep their name and size and moved a forwarded menu option. The brief also
names NPC 8969 as "Junior Cadet Mina" repurposed to "Adamant dragon" - in the 910 cache this
server actually ships with, 8969 is **already** "Adamant dragon", so it classifies `same`.
NPC 42 Sheep -> Bill and items 1079 / 1333 all reproduce exactly.

---

## 2. Spawning: the headline answer

**How many of the 2,394 spawn ids are usable: 1,818 (76.0%).** Per spawn *row*, which is what
decides how populated the world looks:

| | rows | share |
|---|---|---|
| spawned - `same` | 7,567 | 83.2% |
| spawned - `restyled` (case-only rename) | 52 | 0.6% |
| **spawned, total** | **7,619** | **83.8%** |
| refused - no 947 definition (`missing`) | 1,253 | 13.8% |
| refused - repurposed | 168 | 1.8% |
| refused - renamed | 35 | 0.4% |
| refused - unverifiable | 16 | 0.2% |
| **total rows in `spawns.json`** | **9,091** | |

The 52 `restyled` rows are 117 "Hill Giant" (35), 1265 "Rock Crab" (14), 4690 "Hill Giant" (2)
and 1326 "Bear Cub" (1). Nine NPC ids classify `restyled` in all; a tenth case-only rename,
1327 "Bear Cub", is **still refused**, because its size went 1 -> 2 and the footprint check
runs for a restyled id exactly as for an identical one. That single id is the reason the fold
was applied to the name only after the field checks, and not to the name by itself.

The `missing` bucket is dominated by two ids: 5077 (311 rows) and 5078 (98 rows) - 409 rows
between them, a third of the whole bucket. Neither has a named definition in *either* cache,
so they were already broken on 910 and no 947 regression is involved; the next largest are
152 (20 rows) and 8841 "Cavefish shoal" (14 rows), which did exist in 910 and do not in 947.

### Most notable repurposed ids, by how many spawns they cost

| id | rows | 910 -> 947 |
|---|---|---|
| 191 | 21 | "Tribesman" -> "Wandering warrior" |
| 22487 | 6 | "Hellhound" kept its name, size 2 -> 1 |
| 3342 | 6 | "Baby mole" -> "Ghotistix" |
| 6210 | 5 | "Hellhound" kept its name, size 2 -> 1 |
| 12362 | 5 | "Cow" -> "Odd Jackrabbit" |
| 3341 | 5 | "Baby mole" -> "Sydekix" |
| 756 | 4 | "Uzi's Perk Shop" -> "Dr Harlow" |
| 931 | 4 | "Jungle savage" -> "Nipper demon" |
| 3343 | 4 | "Baby mole" -> "Nhitpyx" |
| 1767 | 3 | "Cow" -> "Sneaky Spy" |
| 7891 | 3 | "Melee dummy" -> "Siege engine" |
| 12363 / 12365 | 3 each | "Cow" -> "Loitering Hare" / "Count Rivance" |
| 5146 | 2 | "Li'l lamb" -> "Stealthy Spy" |
| 17495 | 1 | "Lumbridge Thieves' Guild fighter" -> "Wizards' Tower mage" |
| 12319 / 12320 | 1 each | "Prestigious Paul" / "GIM Guide" -> "Black Knight" |
| 42 | 0 in spawns | "Sheep" -> "Bill" (the brief's example; not itself a spawn row) |

**Owner decision, 2026-09-07: case is folded.** The owner was asked about the 89 refused
`renamed` rows and answered "deal with the names as you see is best fit". Case-only renames
are now the `restyled` verdict and are safe to spawn; 52 rows came back. The rule is
`equalsIgnoreCase`, applied **after** every field check, so it admits a difference in letter
case and nothing else:

* recovered: 117 "Hill Giant" -> "Hill giant" (35 rows), 1265 "Rock Crab" -> "Rock crab" (14),
  4690 "Hill Giant" (2), 1326 "Bear Cub" (1). Nine ids, 52 rows.
* **caught by keeping the field checks**: 1327 "Bear Cub" -> "Bear cub" with size 1 -> 2. A
  fold on names alone would have spawned it a tile too small. It stays `repurposed`.
* the loader was changed as well, not just the table: `FORMAT` went 1 -> 2 and `safeVerdicts`
  is pinned to exactly `["same","restyled"]` in that order, so neither a pre-fold table nor a
  regenerated one that widened the policy on its own can be loaded.

**Still refused, and left that way.** These read as the same creature to a human, and a human
is not evidence:

* 2310 "Cow calf" -> "Calf", 6112 "Ducklings" -> "Duckling", 1631/1632 "Rockslug" -> "Rock
  slug" are almost certainly the same creature; 1328/1329 "Unicorn Foal" -> "Foal" loses the
  colour and is genuinely ambiguous. 35 rows between all of them.
* the four "Hellhound" rows whose size moved 2 -> 1 are a real footprint change, not a naming
  one, and no name rule reaches them.

### How the spawner runs

1. `Region.loadModernMap` announces a region **once**, and only after stage 2 - a spawn placed
   against stage-1 masks would be collision-tested against blocked tiles. The announcement is
   not the spawn: the listener records the region id and returns, because map loading is
   re-entrant (creating an NPC loads its own region) and happens under the region's monitor.
2. `Native947World.drainSpawnRegions()` runs as **phase 0 of the world tick**, before any
   movement and before any frame is built, so a viewer never sees an NPC appear mid-step.
3. Every row goes through `Native947IdValidity` first. Only `same` and `restyled` are
   spawned - `Verdict.isSafe()` is the single question, and no caller tests for `SAME`
   by name. Everything
   else is counted by verdict, its distinct id recorded, and named once in the log:
   `[Ataraxia947] spawn skipped: npc 12365 repurposed in 947: "Cow" -> "Count Rivance"`.
   A skipped spawn is a logged, counted decision; it is never a silent omission.
4. Size comes from the 947 `NPCDefinitions.size`, never from the 910 data. A definition the
   strict decoder rejected, or one with an out-of-range size, is counted `undecodable` and
   skipped.
5. `Native947World.spawnCounters()` hands out an immutable snapshot: regions, rows, spawned,
   refused-by-verdict, refusedIds, undecodable, blockedTiles, failures.

**Off by default.** `-Dataraxia947.npcSpawns=true` (constant `Native947World.SPAWNS_PROPERTY`)
or `Native947World.setDataSpawnsEnabled(true)`. The default is off because the seven
real-cache smokes reason about the exact set of NPCs in an otherwise empty world, and a
populated Lumbridge changes every one of those counts. **The live server needs that flag added
to `Start-Server.ps1`** - that file is outside this area and was not touched.

**Blocked tiles are spawned anyway, and counted.** 48 of the 148 NPCs in the Lumbridge probe
stand on tiles whose collision mask is not clear - behind counters, on top of scenery. The 910
loader never checked either, and refusing them would silently depopulate a third of the map,
so they are spawned and the count is reported. `spawnNativeNpc`, the explicit single-NPC entry
point, still refuses a blocked tile; only the data path is permissive.

---

## 3. Wandering

`NPC.processNative947Movement` (P6) already had the random-walk block with combat, aggression,
freeze, force-walk and map-area handling removed. This area only decides **who gets it**, from
data rather than from taste:

* the spawn row's own `canMove` flag - 35 of the 9,091 rows say no;
* the 947 definition's `movementCapabilities` (opcode 119), whose `NORMAL_WALK` bit (0x2) is
  the same bit `NPC.processNPC` tests before its own random walk (`NPC.java:2253`);
* radius 5, which is the span the legacy random walk uses
  (`Math.random() * 10.0 - 5.0`, `NPC.java:2262-2273`) - `Native947World.DATA_WANDER_RADIUS`.

134 of the 148 Lumbridge NPCs are wanderers. Movement runs in phase 1b of the world tick,
before any frame is built, exactly as P6 arranged it.

One deliberate difference from legacy: the native step generator always checks collision
(`addWalkSteps(..., true)`), where `processNPC` passes `(walkType & FLY_WALK) == 0`. A
flying NPC therefore walks around obstacles instead of over them. That is the conservative
direction and it needs no new evidence; a `FLY_WALK` path can be lifted later.

Combat, respawn-on-death and every legacy world hook stay refused: `getDefinitions`,
`getCombatDefinitions`, `processEntity`, `processNPC`, `setNPC` and the spawn/respawn path all
still throw for a native NPC.

---

## 4. What was verified

* Build **39 classes / 464 tests / 0 failures**, of which 17 are `Native947IdValidityTest`
  and 12 the new `Native947IdValidityRuleTest`. (At the time this area landed it was 38/447
  with 15; the case-fold decision on 2026-09-07 added the rest.)
* `Native947IdValidityTest`, cache free on a fixture: a `same` id passes; `repurposed`,
  `missing`, `renamed`, `unverifiable` and never-classified ids are all refused with a reason;
  a name-equal-but-slot-changed item is refused and its reason names the slot change; range
  endpoints are inclusive and the gaps between ranges are not safe; and six malformed table
  shapes (wrong revision, wrong format, a widened safe policy, a missing kind, an id
  classified twice, and backwards/overlapping/out-of-order/garbage ranges) each reject the
  **whole** table rather than answering "safe" for something nobody classified. One test parses
  the shipped `resources/native947/id-validity-947.json` and asserts that NPC 42 and items
  1079 and 1333 really are refused by the file we ship, and that NPC 117 and 1265 - the two
  case-only renames worth 49 spawn rows - really are accepted by it.
* `Native947IdValidityRuleTest`, cache free on hand-written definition pairs: the
  classification rule itself, added with the case-fold decision. Every admitted case change is
  paired with the same change plus a moved field, which must still be refused: name-only is
  `restyled`; name-plus-size, name-plus-dispatched-option and name-plus-equipment-slot are all
  `repurposed`. It also pins the boundary - a moved word break, a dropped hyphen or a trailing
  space is a different string and stays refused - and that the trusted-verdict list the probe
  stamps into the table is the same list, in the same order, that the loader will accept.
* `Native947NpcSmoke --spawns` against the real cache (`build/m4b-spawns-6.log`):

  ```
  region 12850: 89 of 123 spawn rows became NPCs
  region 12851: 50 of 69
  region 12594:  7 of 7
  region 13362:  2 of 2
  spawns[regions=4,rows=201,spawned=148,refused=53{missing=27,renamed=6,repurposed=19,
         unverifiable=1},undecodable=0,blockedTiles=48,failures=0]
  PASS: 148 NPCs spawned across 4 regions, 53 rows refused, 23 of 134 wanderers moved
        within 5 tiles of home
  ```

  The expectation for all of that is computed from the data file plus the checked-in table
  *before* spawning is enabled, so the spawner's own tallies are never the thing being
  compared against themselves. It also asserts that no NPC the table refuses is standing in
  the world, that every spawned NPC is in its region's npc index (what
  `Native947NpcViewport.nearbyNpcs` reads - a spawned NPC missing from it would be invisible
  however correct the encoder), and that no wanderer left its radius.
* Real-cache smokes re-run green: `Native947BootstrapSmoke`, `Native947WorldSmoke`,
  `Native947InteractionsSmoke`, `Native947PersistenceSmoke`, `Native947MultiplayerSmoke`.

### Two smokes were red, and are green now

**Resolved 2026-09-07.** Both were in the player-mask / movement-form work that landed in the
same tree, and both were fixed by its owner. The whole set now passes:
`Native947BootstrapSmoke`, `Native947WorldSmoke`, `Native947InteractionsSmoke`,
`Native947PersistenceSmoke`, `Native947EquipmentSmoke` and the **full** `Native947NpcSmoke`,
whose spawn phase is therefore reachable again without `--spawns`:

```
PASS: 156 NPCs spawned from spawns.json across 8 regions, 60 rows refused
      {missing=28, renamed=6, repurposed=25, unverifiable=1},
      19 of 144 wanderers moved within 5 tiles of home
```

The original diagnosis is kept below because it is what made the two failures safe to leave
alone at the time.

#### The original diagnosis

Both failures are in the player-mask / movement-form work that landed in the same tree
(`Native947EntityMasks.java`, `Native947EntityFrames.java`, `Native947NpcViewport.java`,
`Native947PlayerInfo.java` - none of them this area's files, all of them changed against the
baseline mirror):

* `Native947EquipmentSmoke`: `Player update must contain only the appearance mask: c0 7f f4 00`.
  A player mask source is now installed, so the frame carries more than the appearance block.
* `Native947NpcSmoke` (full run): `The static banker only ever uses the removal retained form`.
  `Native947NpcViewport` now defaults to `ENTITY_STATE` instead of the silent mask source, so
  the banker's retained entry can be a mask-only form, which that smoke's banker-specific
  NPC_INFO parser rejects.

Neither can be caused by spawning, which is off by default and touches no PLAYER_INFO bit.
The second one does mean **the spawn phase at the end of the full `Native947NpcSmoke` run is
currently unreachable**, which is why it also exists as `--spawns`, a mode that loads the
Lumbridge squares itself and runs only that phase. Once the mask owner updates those two
parsers, the full run reaches it again with no further change here.

---

## 5. Known gaps

* ~~**Facing is still written as 0 on the wire.**~~ **Done, 2026-09-07.**
  `Native947NpcViewport.facing` now writes `((direction >> 11) - 4) & 7`, the transform
  `verified/MOVEMENT_TABLES.md` section 4 proves round-trips exactly (the four the server
  subtracts and the 180 degrees the client's decode adds cancel). The mask is explicit because
  the subtraction goes negative for the southern half of the compass and 910 relied on
  `writeBits` truncating it. Before this every NPC was added with 0, which is not an
  "unspecified" sentinel - a three-bit field has none - but literally north.
  **Looked at the same day; consistent, but NOT confirmed.** NPC 278 "Cook" was read as facing
  north-west, which is what the server emits for it - but so is what a client would render from
  its own copy of the definition, so the observation cannot tell the two apart. The discriminating
  test is the banker (NPC 494 at 3217,3257), which keeps direction 0 and is sent as field 4,
  south, while its definition says north-west; see `verified/MOVEMENT_TABLES.md` section 5.
  Note what decides the cook's value: a
  spawn row with no `direction` does **not** mean direction 0. `NPCSpawnsDataParser` falls back
  to `NPC.getRespawnDirection()`, which reads the definition, and `NPCDefinitions` decodes with
  the defaults `contrast = 32` and `respawnDirection = 7` (`NPCDefinitions.java:115,120`). So the
  overwhelming majority of spawned NPCs get `(4 + 7) << 11 = 22528`, normalised to 6144, and go
  on the wire as field 7, north-west - which is also what the 910 server did, since this is its
  own code path. Only the 228 rows that name a compass direction in the file differ, and the one
  NPC created programmatically rather than from data, the smoke's banker, keeps direction 0 and
  therefore field 4. See `verified/MOVEMENT_TABLES.md` section 5 for the full record, including
  how a wrong expected value nearly condemned a correct table.
* **Clicked-NPC resolution.** Written before area C landed; `Native947Interactions.npc` now
  resolves any published index and hands it to `Native947ActionRouter.npcOption`, which is
  precisely why option parity (above) became a safety requirement of this table rather than a
  nicety.
* **Item and object rows cover only referenced ids.** No parser consults the table for items
  or objects yet; the backlog's P3 item (5) - "every JSON parser skips non-same rows" - is
  done for spawns only. `itemsDisassembleData.json` alone carries 704 repurposed and 661
  missing item ids waiting for the same treatment.
* **The roster is still cleared when the last character leaves** (P6's "world roster
  lifetime" gap). Two things are forgotten with it so the next login re-spawns: this class's
  `spawnedRegions` set, and - since the M4 fix pass - the per-`Region` announcement, through
  `Region.rearmNative947Spawns()`. Both are needed: `World.regions` is a static map that is
  never cleared, so before the re-arm existed the once-per-`Region` flag survived the logout
  and no later login could ever repopulate the world. `Native947NpcSmoke`'s spawn phase now
  empties the roster, re-touches the same regions and requires the identical population back.
  A real world would still not empty itself.
* ~~**`Start-Server.ps1` does not pass `-Dataraxia947.npcSpawns=true`.**~~ **Done,
  2026-09-07**, with a comment saying what turns it off again.
* Region announcement is once per `Region` object **per spawn generation**. `unloadMap()`
  resets the load stage but not the announcement; that is safe only because a region that
  still holds NPC indices refuses to unload at all, and because emptying the roster bumps the
  generation. If regions ever unload their entities without going through
  `clearNativeNpcs()`, the generation has to be bumped there too.
