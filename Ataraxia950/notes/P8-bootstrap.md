# P8: 947 bootstrap, data staging, external-integration stubs

Written 2026-09-06 for the AstraNXT migration (see `../../MIGRATION-BACKLOG.md`, pillar P8);
Phase B (P8b, 2026-09-07) added the configurable data root, jar-safe class scanning, the public
`World` core-task wrappers, the launch-script flags and unit coverage. Section 7 lists the Phase B delta.
Java 8 only; nothing in OpenNXT changed; the bootstrap is NOT wired into the login handoff yet (P4 does that).

## 1. What was built

| Item | File | Notes |
|---|---|---|
| Ordered, idempotent 947 init list | `game/com/rs/game/player/client/Native947Bootstrap.java` | 29 entries, one per initialiser, each in its own try/catch; returns a `Report` (name, elapsed ms, ok/failed/skipped + reason, resolved data root + how it was resolved). Single-shot: a second `run()` returns the first report. Requires `Cache.isFlatReadOnly()`. Phase B: a public `Session` core over an injectable `Initialiser` list (tests), `DataPaths` for the root, direct calls to the public `World.queue*Task` wrappers instead of reflection. |
| Data root authority | `game/com/rs/utils/DataPaths.java` (Phase B) | `root()` = installed root, else `-Dataraxia947.data` (must be a directory), else `<cwd>/data` when it holds `npcs/spawns.json`, else `IllegalStateException` naming both candidates and the property. `path/file/resolve(relative)`; `install/reset` override; `resolveRoot(property, cwd)` pure resolver for tests. |
| Real-cache smoke | `game/com/rs/game/player/client/Native947BootstrapSmoke.java` | `main(<flat cache> [data root])`; prints the report, asserts no failures, non-empty NPC spawns / shops / item examines, idempotence, `report.dataRoot == DataPaths.root()`; `System.exit(1)` on any failure, including an unresolved root (printed, no stack trace). Runs from ANY working directory. |
| Unit coverage | `tests/modern947/Native947BootstrapTest.java` (Phase B) | 17 cache-free tests: `DataPaths` resolution order and failure messages, `install/reset`, `Session` accounting over fake entries (ok/failed/skipped, missing data file, unusable root, requirement gating with and without the P0 wheel, single-shot, immutable report), production list shape (29 unique entries, order, gates, relative data files). |
| SQL stub | `game/com/rs/utils/mysql/QueryExecutor.java` | `submit()` counts and drops when `!Settings.SQL_ENABLED` (logged on the 1st and every 1000th drop); `getDroppedCount()`, `getQueuedCount()` added. Queue behaviour with SQL enabled is unchanged. |
| Mail stub | `api/com/rs/external/api/mailing/MailingAPI.java` | Mailer built lazily on first send; credentials resolved through `DataPaths.path("misc/mailingCredentials.json")`; when the root is unresolved or the file is absent every send logs `Mail not sent (no mailer)` and returns. Public API unchanged. |
| Discord removal | `content/.../Commands.java`, `content/.../hcim_news/HcimNewsManager.java`, `api/com/rs/external/api/discord/**` (deleted, 33 files), `build.gradle`, `data/libs/JDA-3.8.3_464.jar` (deleted) | `Commands.java`: removed the `net.dv8tion` import and the `doCommandOnPlayer(TextChannel, ...)` overload; all 560 in-game command cases untouched. `HcimNewsManager.sendDeathInstant` logs the death instead of building a JDA embed; its three persistent files now resolve through `DataPaths.path("hcim/...")`. |
| Bot gate | `game/com/rs/ServerLauncher.java` | `populateStartupBots()` only runs when `Settings.BOT_ENABLED`. Legacy launcher otherwise untouched. |
| Jar-safe class discovery | `game/com/rs/utils/Utils.java` (Phase B) | `Utils.getClasses(pkg)`: directory entries are walked exactly as before; when a classpath entry for the package is not a `file:` URL (a jar) the package is scanned with FastClasspathScanner 3.0.3 (`strictWhitelist`, context class loader), results de-duplicated by name. New helper `Utils.scanPackageClassNames(pkg, loader)`. |
| Combat scripts fail closed | `npc/com/rs/game/npc/combat/CombatScriptsHandler.java` (Phase B) | `init()` logs `Loaded N NPC combat script keys from the class path.` and throws `IllegalStateException("Loaded 0 NPC combat scripts ...")` when nothing registered; `getScriptKeyCount()` added (the bootstrap no longer reads the private map reflectively). |
| Public core tasks | `game/com/rs/game/World.java` (Phase B) | `World.initCoreTasks()` queues the six `WorldTasksManager` core tasks (prayer drain, HP/skill/special restore, owned objects, shop restock) and nothing else; six public one-line wrappers `queueDrainPrayerTask / queueRestoreHitPointsTask / queueRestoreSkillsTask / queueRestoreSpecialAttackTask / queueOwnedObjectsTask / queueRestoreShopItemsTask`. `addRefreshTargetBuffsTask` was already public. `init()` and the private methods are unchanged. |
| Launch script | `../Start-Server.ps1` (Phase B) | For `-Backend ataraxia947` only: refuses to start unless `Ataraxia947\data\npcs\spawns.json` is staged, passes `"-Dataraxia947.data=<abs Ataraxia947\data>" -Dataraxia947.strict=false` before `-cp`, records `DataPath` in `logs/server.pid.json`. The opennxt backend line is byte-identical to before. |
| Data staging | `data/items/*.json`, `data/npcs/*.json`, `data/map/*.json`, `data/musics/hints.json`, `data/perkGenerationData.json` | Copied verbatim from `C:\Users\developer\Desktop\Ataraxia-PS\data` (see section 4). |

## 2. Bootstrap entry list (order, requirement, data file)

Order follows `ServerLauncher.init()` (`ServerLauncher.java:152-361`) with one deliberate change:
the NPC definition tables are loaded BEFORE `NPCSpawnsDataParser.init()`. The launcher loads
spawns first, so its 9,091 NPC templates are built with weakness 0 and the default combat
definition; live spawns re-read both at spawn time, so nothing visible changes, but the 947
path does not repeat the quirk.

| # | Entry | Reads | Result on the real cache |
|---|---|---|---|
| 1 | `WorldAreaTypeDefinitions.init` | cache idx 2/83 + 23/3 | ok, mapped=861 lookUp=5102 |
| 2 | `TTAllRewards.init` | cache (item enum) | ok, allTotal=855.0 |
| 3 | `CustomObjectSpawnsDataParser.init` | `map/customObjectSpawns.json` | ok, regions=0 (file is an empty array; see 5) |
| 4 | `NPCWeaknessesDataParser.init` | `npcs/weaknesses.json` | ok, 2328 |
| 5 | `NPCCombatDefinitionsDataParser.init` | `npcs/combatDefs.json` | ok, 2644 |
| 6 | `NPCStatsDataParser.init` | `npcs/npcstats.json` | ok, 6706 |
| 7 | `NPCDropsDataParser.init` | `npcs/drops.json` | ok, 1089 |
| 8 | `NPCExaminesDataParser.init` | `npcs/examines.json` | ok, 5591 |
| 9 | `NPCSpawnsDataParser.init` | `npcs/spawns.json` | ok, 467 regions / 9091 templates |
| 10 | `ItemExaminesDataParser.init` | `items/itemExamines.json` | ok, 17918 |
| 11 | `ItemWeightsDataParser.init` | `items/itemWeights.json` | ok, 17122 |
| 12 | `ItemDisassembleDataParser.init` | `items/itemsDisassembleData.json` | ok, 14350 |
| 13 | `PerkGenerationDataParser.init` | `perkGenerationData.json` | ok |
| 14 | `TreasureHunterRewardParser.init` | `items/treasurehunterrewards.json` | ok, 617 |
| 15 | `MusicHintsDataParser.init` | `musics/hints.json` | ok, 794 |
| 16 | `CombatScriptsHandler.init` | classpath scan (`Utils.getClasses`, directories AND jars) | ok, 372 script keys (363 impl + 6 RotS + 2 AoD classes) |
| 17 | `ControllerHandler.init` | - | ok, 75 |
| 18 | `CutscenesHandler.init` | - | ok, 10 |
| 19 | `ShopsDataParser.init` | `items/shops.json` + cache item prices | ok, 131 shops |
| 20 | `FishingSpotsHandler.init` | - | ok, 11 |
| 21 | `Scanner.scan` | FastClasspathScanner 3.0.3 | ok, 506 dialogues, 145 commands |
| 22 | `World.addDrainPrayerTask` (`World.queueDrainPrayerTask`) | `WorldTasksManager.schedule` | **skipped: requires-M3** - runs `player.getPrayer().processPrayerDrain()` for every world player every tick (`World.java:458-476` pre-Phase-B numbering); `Player.createNative947` never assigns `prayer`, so once queued it NPEs every 600 ms |
| 23 | `World.addRestoreHitPointsTask` (`queueRestoreHitPointsTask`) | `WorldTasksManager.schedule` | ok, queued (ticks every 10 cycles) |
| 24 | `World.addRestoreSkillsTask` (`queueRestoreSkillsTask`) | `WorldTasksManager.schedule` | **skipped: requires-M3** - calls `player.getPrayer().usingPrayer(...)`, same null Prayer |
| 25 | `World.addRestoreSpecialAttackTask` (`queueRestoreSpecialAttackTask`) | `WorldTasksManager.schedule` | ok, queued |
| 26 | `World.addOwnedObjectsTask` (`queueOwnedObjectsTask`) | `WorldTasksManager.schedule` | ok, queued |
| 27 | `World.addRestoreShopItemsTask` (`queueRestoreShopItemsTask`) | `WorldTasksManager.schedule` | ok, queued |
| 28 | `World.addRefreshTargetBuffsTask` | `CoresManager.getServiceProvider()` | **skipped: requires-M2b** - with the P0 wheel present it would call `TaskTab.sendTab` for every active player every 600 ms, i.e. three `sendIComponentText` writes to interfaces 635/930 through the facade that no M2b binding has verified |
| 29 | `ActionBar.addActionBarTask` | `CoresManager.getServiceProvider()` | skipped: requires-P0 in the bootstrap smoke (no `Native947World`, so no wheel); in the 947 JVM the P0 wheel exists (`Native947World.bindTickWheel`) and this entry runs (it only increments `BAR_CYCLE`) |

Entry names keep the legacy `World.add*Task` form so old and new reports stay comparable; the
bodies call the public `World.queue*Task` wrappers (no reflection left in the bootstrap except the
read-only `tableSize` peeks at private parser maps).

**Timing:** `Native947World.pumpSchedulers()` calls `CoresManager.drainNativeTick()` and
`WorldTasksManager.processTasks()` every 600 ms whether or not a session is attached, so every task
the bootstrap queues starts ticking the moment `Native947Bootstrap.run()` is wired into the 947 JVM.
That is why the two prayer tasks and the task-tab refresh are SKIPPED (each skip reason starts with
`requires-<milestone>`) rather than queued to fail every tick; the smoke asserts those three are
skipped and exactly 4 world tasks are queued.

Every entry that loads a table fails closed with `Loaded 0 <what>` when the table is empty after
`init()`. Entries with a data file fail (not skip) when the data root is unusable or the file is missing.

Dropped from `World.init()` (activity initialisers gated per milestone or external): `addSummoningEffectTask`,
`addRotaionChangeTask` (Vorago/Araxxor instances), `LivingRockCavern`, `addArtisansWorkShopProcessTask`,
`WarriorsGuild`, `DemonFlashMobs`, `ShootingStar`, `WildyWyrm`, `SoulWarsManager`, `SnowballLobby`,
`addTriviaBotTask`, `PuroPuro`, `SerenityPostsHandler`.

Dropped from `ServerLauncher.init()` (full list in the class javadoc): AccountPin flag, AutoBackup,
SQLThread, `Cache.init` (flat cache instead), ItemsEquipIds, Huffman, NXTClientsManager, ChargesDatabase,
DisplayNames, BodyDefinitions, CosmeticsManager, IPBanL/IPMute, TelosEnrageRanks, LoggingSqlManager,
GrandExchange, WorldRepository, ClansManager, TriviaBot, AgilityManager, FriendChatsManager,
`CoresManager.init`, EliteDungeon, WellOfGoodWill, ActivitiesScheduler, MapBuilder, NetworkBootstrapper,
LendingManager, saving/clean-memory/recalc-prices tasks, BossInstanceHandler, AraxxorManager, StarterMap,
GetRichestBanksSql, SiphonActionNodes, `ObjectSpawns.addCustomSpawns`, FactionManager, Lottery, GIM,
EvilTreeHandler, skilling contracts, FlowerGirlD, SeasonalEventManager, JadinkoManager, populateStartupBots.

## 3. Data root and working directory (Phase B)

* `com.rs.utils.DataPaths.root()` resolves, in order: an installed override (`DataPaths.install(Path)`,
  set by `Native947Bootstrap.run(Path)` so the parsers read the tree the bootstrap validated), then
  `-Dataraxia947.data` (made absolute; a value that is not a directory fails closed and the fallback is
  NOT consulted), then `<cwd>/data` when it contains `npcs/spawns.json` (the legacy launcher path: same
  file the old `"data/..."` literals reached), else `IllegalStateException` naming both candidates and
  the property to set.
* All 15 parsers under `game/com/rs/utils/data/parsers/**` keep their constants (`DEFINITIONS_FILE_PATH`,
  `SPAWNS_FILE_PATH`, `SHOPS_FILE_PATH`, `NPCStatsDataParser.path`, `PerkGenerationDataParser.DEFINITIONS_FILE_PATH`)
  but the values are now RELATIVE to the root (`items/shops.json`, ...) and every `new JsonParser(...)`
  call site passes `DataPaths.resolve(constant)`. The two public constants changed meaning accordingly;
  the only outside mention was a commented-out line in `game/com/rs/tools/TestJS.java`.
  `HcimNewsManager` (`hcim/deaths.json`, `hcim/news.json`, `hcim/date.json`) and `MailingAPI`
  (`misc/mailingCredentials.json`) resolve the same way; `MailingAPI` swallows an unresolved root as
  "mail is disabled", `HcimNewsManager` (a class-load singleton) does not, so content that touches it in
  a JVM with no data root gets an `ExceptionInInitializerError` - fail closed, by design.
* `Native947Bootstrap` no longer compares the root with `<cwd>/data`; `Report.dataRootUsable` now means
  "directory holding `npcs/spawns.json`", `Report.dataRootSource` records how the root was chosen
  (`argument: ...`, `-Dataraxia947.data=...`, `working directory: ...`, `installed: ...`, `unresolved: ...`),
  and `Report.workingDirectoryData` is kept for the log only. `run()` with an unresolvable root does not
  throw: the cache-only entries run and every data-backed entry is FAILED with the resolution message.
* The 947 JVM therefore starts with the working directory `OpenNXT/` and `-Dataraxia947.data=<abs Ataraxia947\data>`
  (`Start-Server.ps1` adds it for the ataraxia947 backend). Nothing reads relative to the working directory any more.

## 4. Staged data (bytes, sha256), copied from `C:\Users\developer\Desktop\Ataraxia-PS\data`

```
1266077  24e5e5d06903f20450b80dbadfdc53fdfc46f3d69d14b0cc058ab3e564f5d25d  items/itemBonuses.json
2126206  f7e4840fa20493d949491b8ad6c1bc77cb8d484a7a7c1214c51eadd43b4b38b2  items/itemExamines.json
1214194  6f7398148d2c2cfce960f7a6a91dbce94c65a8c234810b744ea6a0a0cf0818b2  items/itemWeights.json
10901908 1733e35083cddac1ab34fc514a44433e1fd7ee96edc4a59194eb009ddf0f31a4  items/itemsDisassembleData.json
160106   7bb4b4e2f8eff095796222c5fabea9e2616924109aaccc3286c62d71df98198d  items/shops.json
92757    dd22fac24a3ad76564032e8ab0ab3785f39f08b57a1798e8c9781f6afcebd361  items/treasurehunterrewards.json
16       955fbdc8a971881db73ae325330cbb54fe1036ebcd5f1fbb803ab294306b9627  map/customObjectSpawns.json
123819   692b67e8e50cb6befcf25e0c5bb037036e89359b560cdb6d667c2299bbd7922a  map/customObjectSpawns2.json
60655    1f68f835053496847eced2d9b08bd10b3350072a09935a419c243eaea03bbec1  musics/hints.json
151216   3c148793cc50dfd4386b85d813a0590054840fa17f74e5e174e48cf31529e171  npcs/bonuses.json
1041165  76240dffe66dd89a03d7ef2de729f4939611f59e5bf1d699f1f3338ed67f65b0  npcs/combatDefs.json
2754090  91257d31e0a1fe78951739958aafb8951c5732da5201648fff627ddc04c46c7f  npcs/drops.json
554803   ae51494c6ee392eb42d20c8b865bd221931fdb61b6feb6838f64a160399059aa  npcs/examines.json
1842675  0c4b0016eced1afff3f30c6066a471df5ec367548e1266ae0f5ca78f800694a1  npcs/npcstats.json
981051   5fc87bdc94a8d385e608cedda22228155b50261a1d26835dabc3adb1aa53d801  npcs/spawns.json
129449   f5b0ac71de6e67cc4c01b853bf753d9643108f74245a26ef835eeb3d55037a25  npcs/weaknesses.json
102019   193bce78edbcd3ee7f1a9c8bf5fb843ead41a194c830afd659aa46bce05ca967  perkGenerationData.json
```

`items/itemBonuses.json` and `npcs/bonuses.json` are staged because the P8 table lists them, but no
initialiser in the bootstrap reads them. `map/customObjectSpawns2.json` is staged as part of `map/*.json`;
the parser only reads `customObjectSpawns.json`, which is an empty array in the original server too, so the
947 world has zero custom JSON object spawns exactly like 910 (the ~150 hard-coded custom objects live in
`ObjectSpawns.addCustomSpawns`, which is not in the bootstrap).

## 5. Smoke report (real cache, Java 8, Phase B)

Run from a FOREIGN working directory (`C:\Users\developer\Desktop\AstraNXT`) with the property, scratch classes
first so they shadow `build/classes/java/main`:

```
java -Xmx2g -Dataraxia947.data=C:\Users\developer\Desktop\AstraNXT\Ataraxia947\data -cp <out-b-p8b>;<runtime-classpath> com.rs.game.player.client.Native947BootstrapSmoke C:\Users\developer\Desktop\rs3cache\cache
```

```
[Ataraxia947] bootstrap smoke: cache=C:\Users\developer\Desktop\rs3cache\cache dataRoot=C:\Users\developer\Desktop\AstraNXT\Ataraxia947\data workingDirectory=C:\Users\developer\Desktop\AstraNXT
Native947Bootstrap report
  data root: C:\Users\developer\Desktop\AstraNXT\Ataraxia947\data [argument: C:\Users\developer\Desktop\AstraNXT\Ataraxia947\data]
  working directory data: C:\Users\developer\Desktop\AstraNXT\data
  [ok]    91 ms  WorldAreaTypeDefinitions.init - mapped=861 lookUp=5102
  [ok]    11 ms  TTAllRewards.init - allTotal=855.0
  [ok]    37 ms  CustomObjectSpawnsDataParser.init (map/customObjectSpawns.json) - regions=0
  [ok]    13 ms  NPCWeaknessesDataParser.init (npcs/weaknesses.json) - loaded=2328
  [ok]    17 ms  NPCCombatDefinitionsDataParser.init (npcs/combatDefs.json) - loaded=2644
  [ok]    17 ms  NPCStatsDataParser.init (npcs/npcstats.json) - loaded=6706
  [ok]   158 ms  NPCDropsDataParser.init (npcs/drops.json) - loaded=1089
  [ok]     7 ms  NPCExaminesDataParser.init (npcs/examines.json) - loaded=5591
  [ok]   360 ms  NPCSpawnsDataParser.init (npcs/spawns.json) - regions=467
  [ok]    16 ms  ItemExaminesDataParser.init (items/itemExamines.json) - loaded=17918
  [ok]    24 ms  ItemWeightsDataParser.init (items/itemWeights.json) - loaded=17122
  [ok]    46 ms  ItemDisassembleDataParser.init (items/itemsDisassembleData.json) - loaded=14350
  [ok]     6 ms  PerkGenerationDataParser.init (perkGenerationData.json)
  [ok]     2 ms  TreasureHunterRewardParser.init (items/treasurehunterrewards.json) - loaded=617
  [ok]     2 ms  MusicHintsDataParser.init (musics/hints.json) - loaded=794
  [ok]   367 ms  CombatScriptsHandler.init - scripts=372
  [ok]   678 ms  ControllerHandler.init - controllers=75
  [ok]     8 ms  CutscenesHandler.init - cutscenes=10
  [ok]   154 ms  ShopsDataParser.init (items/shops.json) - shops=131
  [ok]     0 ms  FishingSpotsHandler.init - spots=11
  [ok]  2406 ms  Scanner.scan - dialogues=506 commands=145
  [skipped]     0 ms  World.addDrainPrayerTask - requires-M3: Player.createNative947 owns no Prayer; the task would NPE every tick
  [ok]     1 ms  World.addRestoreHitPointsTask - worldTasks=1
  [skipped]     0 ms  World.addRestoreSkillsTask - requires-M3: Player.createNative947 owns no Prayer; the task would NPE every tick
  [ok]     0 ms  World.addRestoreSpecialAttackTask - worldTasks=2
  [ok]     0 ms  World.addOwnedObjectsTask - worldTasks=3
  [ok]     0 ms  World.addRestoreShopItemsTask - worldTasks=4
  [skipped]     0 ms  World.addRefreshTargetBuffsTask - requires-M2b: TaskTab.sendTab would write unverified interfaces 635/930 through the facade every tick
  [skipped]     0 ms  ActionBar.addActionBarTask - requires-P0: CoresManager.getServiceProvider() is null in the 947 JVM
  summary: ok=25 failed=0 skipped=4 total=29 in 4434 ms
[Ataraxia947] data root: C:\Users\developer\Desktop\AstraNXT\Ataraxia947\data [argument: ...] DataPaths.root()=C:\Users\developer\Desktop\AstraNXT\Ataraxia947\data
[Ataraxia947] tables: npcSpawns=9091 shops=131 itemExamines=17918
[Ataraxia947] bootstrap smoke passed: ok=25 skipped(requires-*)=4
```

Also verified: the same command from `cwd = Ataraxia947` WITHOUT the property (working-directory
fallback) passes with identical counts; from the foreign directory WITHOUT the property it exits 1 with
`No data root: -Dataraxia947.data is not set and C:\Users\developer\Desktop\AstraNXT\data does not contain
npcs/spawns.json; start the JVM with -Dataraxia947.data=<absolute path ...>`.

Packaged-jar probe (Java 8): all of `build/classes/java/main` plus the Phase B classes were jarred and
`Utils.getClasses` / `CombatScriptsHandler.init` were run with the jar as the ONLY class directory:
`getClasses impl=363 rots=6 aod=2`, `Loaded 372 NPC combat script keys` - identical to the directory walk.
Before Phase B the same probe returned 0 classes from the jar.

Observed while loading: the NPC definition decoder prints `Unrecognized .npc code:186` for a number of
947 NPCs while the 9,091 templates are constructed (P3 territory; the templates still build and no entry failed).

## 6. Known limits and hand-offs

* `World.addDrainPrayerTask` / `addRestoreSkillsTask` stay skipped (`Requirement.NATIVE_PRAYER`) until
  `Player.createNative947` (P4 hydration) owns a `Prayer`; `World.addRefreshTargetBuffsTask` stays skipped
  (`Requirement.NATIVE_TASK_TAB`) until M2b verifies the task-tab interfaces. Flip the requirement in
  `Native947Bootstrap.satisfied()` when the dependency lands; `Native947World.pumpSchedulers()` will run
  the task on the very next tick, so do not queue them "dormant". `World.initCoreTasks()` queues all six
  at once and is meant for the day both gates are open.
* `-Dataraxia947.strict=false` is passed by `Start-Server.ps1` but nothing in the Java tree reads it yet:
  `Native947PacketDispatcher.strict` is a `public static volatile boolean` defaulting to true with
  `setStrict(boolean)`. The P4 wiring (or P1's owner) should read the property once at world construction
  (`Boolean.parseBoolean(System.getProperty("ataraxia947.strict", "true"))`) so the live server runs the
  lenient count-and-drop tier while JUnit keeps strict.
* `Pool.getConnection` (not owned here) NPEs on `pools.get(auth)` when no `Pool` was constructed; none of
  the bootstrap entries reach it, but the direct callers listed in the backlog (polls, gim, bank_highscores,
  hcim_news `GetTopHcimSql`, `LoggingSqlManager`) should be wrapped or excluded before any of them is
  scheduled from the 947 JVM.
* FastClasspathScanner 3.0.3 is now on two paths (`Scanner.scan` and the `Utils.getClasses` jar fallback);
  the Java 25 run of the bootstrap smoke from the packaged OpenNXT distribution is still outstanding (the
  dev server must not be touched in this phase). If FCS fails on JDK 25, both entries fail closed and the
  report names them.
* The bootstrap is not called from `Native947World`/`Ataraxia947Handoff` yet; wiring it in is a P4 step
  and must happen after `Cache.initFlatReadOnly` and before the first `Player.createNative947`.
  Recommended call: `Native947Bootstrap.run()` (property/cwd resolution) and refuse admission when
  `!report.allOk()` or `!report.dataRootUsable`.

## 7. Phase B delta (P8b), 2026-09-07

Files: NEW `game/com/rs/utils/DataPaths.java`; the 15 parsers under `game/com/rs/utils/data/parsers/**`
(path constants relative + `DataPaths.resolve` at the `JsonParser` call sites, nothing else touched);
`content/.../hcim_news/HcimNewsManager.java` (three `DataPaths.path` calls); `api/.../mailing/MailingAPI.java`
(credentials via `DataPaths.path`, unresolved root = mail disabled); `Native947Bootstrap.java` (public
`Requirement`/`Step`/`Initialiser`/`Session`, `initialisers()` public, `Report.dataRootSource`, cwd check gone,
World wrappers instead of reflection, `CombatScriptsHandler.getScriptKeyCount()`); `Native947BootstrapSmoke.java`
(any cwd, root consistency assertion, clean fail-closed exit on an unresolved root); `World.java`
(`initCoreTasks` + six `queue*Task` wrappers, additive); `Utils.java` (`getClasses` jar fallback +
`scanPackageClassNames`); `CombatScriptsHandler.java` (count log, throw on zero, `getScriptKeyCount`);
`../Start-Server.ps1` (ataraxia947 backend flags + staged-data check + `DataPath` record); NEW
`tests/modern947/Native947BootstrapTest.java`.

Signatures kept stable for the parallel P4 wiring: `Native947Bootstrap.run()`, `run(Path)`, `lastReport()`,
`resolveDataRoot()`, `DATA_ROOT_PROPERTY`, `Report` fields (`dataRoot`, `workingDirectoryData`,
`dataRootUsable`, `entries`, `totalMillis`, `count/failures/skipped/allOk/entry/format`), `Entry`, `Status`.
Additive: `Report.dataRootSource`, `Session`, public `Initialiser`/`Step`/`Requirement`, `initialisers()`.

Verification (all without Gradle, scratch javac of the owned files only): `Native947BootstrapTest` 17/17;
the full `tests/modern947` suite with the new classes shadowing `build/classes` = 150 runnable tests green
(`Native947ProtocolTest` is a main-style probe with no JUnit methods, unchanged from the baseline);
the two positive smoke runs and the negative run in section 5; the jar-only probe in section 5;
`Start-Server.ps1` parses (`[scriptblock]::Create` and `Parser::ParseFile`, 0 errors) and was not executed.
