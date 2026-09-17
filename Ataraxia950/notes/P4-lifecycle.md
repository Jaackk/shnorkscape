# P4 - Player lifecycle: hydrate, native tick, persistence v3

Status: implemented in Phase B, repaired and verified in the Phase B repair pass.
Owner files: `game/com/rs/game/player/Player.java`, `AuraManager.java`,
`InventionManager.java`, `Skills.java`, `GlobalPlayerUpdater.java`,
`client/Native947Session.java`, `Native947World.java`, `Native947Save.java`,
`Native947SaveStore.java`, `Native947PlayerBinder.java`,
`Native947PacketDispatcher.java` (tier policy only),
`Native947WorldSmoke.java`, `Native947PersistenceSmoke.java`,
`tests/modern947/Native947PlayerLifecycleTest.java`,
`Native947WorldLifecycleTest.java`, `Native947SaveStoreTest.java`,
`Native947AppearanceTest.java`.

---

## 1. The hydration contract (fail closed)

**Rule.** For a native 947 player, every manager that `Player.processEntity()`
dereferences without a null guard, plus every manager the M2b router path
(`ObjectHandler` / `NPCHandler` / `ButtonHandler` / `InventoryOptionsHandler`)
dereferences before its own id branches, MUST be non-null once
`Player.hydrateForNative947()` returns. There is no third state: a manager is
either constructed or admission is refused.

Enforcement is a single method, `Player.requireNative947TickManagers()`, run at
the end of `hydrateForNative947()` (which itself runs inside the private
`Player(WorldTile, ClientSession, String)` constructor used by
`Player.createNative947`). It throws

```
Native 947 admission refused for <username>: manager '<field>' could not be
constructed, and Player.processEntity dereferences it every tick. Initialize the
947 cache (Cache.initFlatReadOnly) and run Native947Bootstrap before admitting a
player, then retry the login.
```

`Native947World.attachOnWorld` runs `Player.createNative947` inside its own
`try`, so the throw becomes a refused admission: the session is never created,
the channel is closed and the future completes exceptionally with that message.
Nothing half-hydrated ever reaches the world list or the tick loop.

Fields checked (all of them are asserted non-null by
`Native947PlayerLifecycleTest.hydrationCreatesEveryManagerTheNativeTickDereferences`):

| Group | Fields |
|---|---|
| `processEntity` body, unguarded | `cutscenesManager`, `auraManager`, `actionManager`, `inventionManager`, `prayer`, `dayOfWeekManager`, `buffDebuffTimersManager`, `controlerManager`, `farmingManager`, `playerExamineManager`, `accountPin`, `interfaceManager`, `musicsManager`, `combatDefinitions` |
| reached from `processEntity` | `activityTimersManager` (`CombatDefinitions.processCombatStance`), `visWaxManager` (`DivineObject.resetGatherLimit`), `activePotions` (`PotionTimerInterface` gate) |
| M2b / M3 content paths | `questManager`, `toolBeltNew`, `perkManager`, `deathManager`, `dialogueManager`, `hintIconsManager`, `priceCheckManager`, `varBitManager` |
| containers / appearance / persistence | `inventory`, `equipment`, `bank`, `skills`, `appearence`, `switchItemCache` |
| M2b router prologue | `treasureTrails`, `emotesManager`, `dungeoneeringBinds` (`binds`), `gemBag` |

**`toolBelt` was the one exception, and the review pass closed it.** The original
implementation built it only when `Cache.isFlatReadOnly()` and left it null
otherwise, on the premise that "every native call site goes through
`toolBeltNew`". That premise is false: `Inventory.containsItem` (`Inventory.java:369`)
and `Inventory.containsOneItem` (`:424`) dereference `getToolBelt()` with no guard,
and `ObjectHandler.dispatch` - the router's own entry - calls `player.stopAll()`
first, which reaches them through `CombatDefinitions.resetSpells ->
Magic.getTotalAmountOfRune`. So a cache-free JVM (every JUnit run) admitted a
player whose first routed object click would NPE: exactly the "third state" this
section forbids. The belt is now constructed unconditionally,
`Toolbelt.addDefaultItems` skips the unverified enum 13730 -> 6979/6980 -> 2433
seeding on "that chain is unavailable" rather than on `isFlatReadOnly` alone
(`Toolbelt.defaultToolChainAvailable()`), and `toolBelt` is on the
`requireNative947TickManagers()` list. An unseeded belt is empty and, since the
same repair, refuses `addItem` outright instead of deleting the item from the
inventory and then NPE-ing inside `refresh()`. `Native947PlayerLifecycleTest`
asserts the belt is non-null, empty, and refuses an item.

**Deliberately NOT required, and why**

* The ~200 other reference fields a legacy `Player(password, mac)` +
  `LoginManager.init` login creates (`slayer`, `house`, `pet`, `dominionTower`,
  `squealOfFortune`, `ports`, ...). They are not on the tick path or the M2b
  router path. They become required one milestone at a time, as the content that
  reads them is enabled; each addition is a line in
  `requireNative947TickManagers()` plus a line in `hydrateForNative947()`.

## 2. The order-dependent test failure (root cause and fix)

`Native947PlayerLifecycleTest` passed alone and in alphabetical order but failed
three assertions under Gradle's class order:

* `hydrationCreatesEveryManagerTheNativeTickDereferences` at
  `assertNotNull(player.getInventionManager())`;
* `oneHundredStrictTicksProduceNoFailureAndNoStrictHit` with
  `IllegalStateException: Native 947 tick failure #1 ... NullPointerException`;
* `skillsAndVitalsSettersAreSilent` with `expected:<1> but was:<2>` on
  `player.getNativeDeferredRefreshes()` (NOT on `noops("sendSkillLevel")`,
  which was always 1 - `Skills.refresh` calls `sendSkillLevel` exactly once and
  `refreshXpBonuses(skill)` only writes a varbit).

All three had one cause. `InventionManager()` sized `materials` from
`ClientScriptMap.getMap(10742).getSize()` and then wrote `materials[0]` and
`materials[10]` unconditionally. `ClientScriptMap.getMap` never returns null and
never throws (`RS3ClientScriptMap.getMap` swallows the missing index 17 and
caches an **empty** map), so with no cache the array had length 0 and the
constructor threw `ArrayIndexOutOfBoundsException`. `hydrateForNative947` caught
that `RuntimeException`, counted a deferred refresh and left `inventionManager`
**null**; `Player.processEntity` then dereferenced `inventionManager.process()`
every tick, which the new strict rethrow surfaced as a tick failure.

The order dependence came from the fixture: the test seeded enums
13430/10742/10743 through reflection but skipped an id that was already present,
and *any* earlier test class that calls `Player.createNative947` populates those
three ids with empty maps as a side effect of hydration. Alphabetically
`Native947AppearanceTest` runs first and calls the same seeder before creating a
player, so the fixtures won; under Gradle's order a poisoning class ran first.

**Fix (both halves).**

1. `InventionManager` gained `enumSize(int)` (a null/absent-tolerant size read)
   and `seedStartingMaterial(int,int)` (a bounds-guarded write).
   `AuraManager` gained `aurasEnumSize()` and uses it for all nine of its
   array-size reads. With the enum present both are **exactly** the previous
   expression, so legacy 910 sizing and legacy resize/migration behaviour are
   byte-for-byte unchanged (diffed against the Phase-A baseline mirror; the only
   non-comment changes are the substitutions themselves). Without a cache both
   managers are constructed empty and usable: `AuraManager.process()` and
   `InventionManager.process()` never index those arrays, and
   `InventionManager.init()` (which does index up to 82) is called from
   `LoginManager` only, never natively.
2. `hydrateForNative947` no longer wraps either constructor in `try/catch`.
   They are constructed directly, and `requireNative947TickManagers()` is the
   backstop.

Deferred-refresh accounting is kept for what is *genuinely* deferred, not for
construction failures: `Toolbelt` (enum chain unverified, M8),
`MusicsManager.setPlayer` (947 map squares carry no music list; decision D8) and
`Player.refreshHitPoints` (varbit 1668 unverified). The count is surfaced as
`Native947Session.Snapshot.deferredRefreshes` and printed by both smokes.

## 3. Two further null landmines found by the real-cache smokes

The unit tests only exercise the tick. Running `Native947PersistenceSmoke` and
`Native947EquipmentSmoke` against the real cache with the P5 router in place
surfaced two more nulls on the M2b path, both now hydrated:

* `treasureTrails` - `ObjectHandler.handleOption2` calls
  `player.getTreasureTrails().useObject(object)` before every id branch, so the
  M2b bank chest (79036 option 2) NPE'd. `TreasureTrails` has an implicit
  packet-free, cache-free constructor and `useObject` returns `false`
  immediately when no clue is in progress.
* `gemBag` - `InventoryOptionsHandler.handleItemOption2` (the Wear path for
  1473:5 option 2) calls `player.getGemBag().withdraw(id)` before its id
  branches. `GemBag(Player)` only stores the reference; `withdraw` returns
  `false` for anything but item 18338.
* `binds` (`DungeoneeringBinds`) - `ButtonHandler.handleButtons` calls
  `player.getDungeoneeringBinds().processButtonClick(...)` before the
  517/1473/1462 branches. The constructor only sets `activeLoadouts[0]`;
  `processButtonClick` returns `false` for any interface other than 116.

None of the three touches the cache, the facade or `CoresManager`.

## 4. Test hermeticity

`Native947PlayerLifecycleTest` is now order-independent:

* `@BeforeClass seedCacheFreeDefinitions()` **overwrites** enums
  13430/10742/10743/7716, struct 0, `BodyDefinitions.disabledSlots` and item
  definitions 1139/1277/1173/4151, recording whatever was there before.
* `@AfterClass restoreCacheFreeDefinitions()` puts every one of those JVM
  globals back (removing the key when there was none), so no later class
  inherits the fixtures. `Native947AppearanceTest`, which borrows the same
  seeder for its byte-parity test, now calls the restore in a `finally`.
* `@Before` / `@After` save and restore `Native947PacketDispatcher.strict` and
  call `Native947IdMap.reset()`.
* Per the P0 note the class never calls `CoresManager.drainNativeTick()`.

Verified by running the whole suite in one JVM in **reverse** alphabetical order
(198 tests, 0 failures) and by a deliberately hostile order that starts with five
classes which create native players.

`hydrateForNative947()` is idempotent but never a no-op: on a second call it
re-runs `requireNative947TickManagers()`, which is what
`hydrationRefusesAdmissionInsteadOfLeavingANullManager` uses to prove the gate
fires (it clears `cutscenesManager` and asserts the refusal message names the
field and points at `Cache.initFlatReadOnly`).

## 5. What P4 delivered against the backlog line items

| Backlog item | State |
|---|---|
| (0) construct every tick manager packet-free | done, plus the fail-closed gate above |
| (0b) packet-free appearance serializer | done: `GlobalPlayerUpdater.generateAppearenceData` takes a native branch that builds the 947 body (wear positions, hidden slots, BAS from item param 644, customization mask 0) with no `TaskTab.sendTab`, no `sendGlobalConfig(779,...)` and no `getPrayer()` head icons; an unverifiable body keeps the previous one and increments `getNative947WithheldBodies()` |
| (1) packet-free hydration split | done as `Player.hydrateForNative947()`, called from the native constructor. `LoginManager.init` is deliberately **not** refactored: it interleaves packet emitters with its null-guards, so a shared helper could not be proven sequence-identical for legacy logins by diff. `LoginManager.java` is unchanged from the baseline. |
| (2) `processLogicPackets` no-op, pin marked entered, AFK disabled | done (`logicPackets == null` short-circuit; `accountPin.setPinEntered()`; the AFK-island branch and `sendAfkNotification()` are guarded by `!isNative947()`) |
| (3) native tick in WorldThread order | done in `Native947Session.tick()`; `clientHasLoadedMapRegion` set after each rebuild flush, `hasCompleted` set in `ready()`, `lastPacketReceivedTime` updated from `drainActions` |
| (4) seed the 910 containers from the save and route refreshes through the facade | **partial**: `Native947PlayerBinder` restores SKILLS/VITALS/SETTINGS/APPEARANCE/IDENTITY onto the `Player`, and the P5 router drives the real `Inventory`/`Bank`/`Equipment`. `Native947Containers` is **not** yet a thin view over `Player.getInventory()`; it is still the container of record for the session snapshot, and `Native947Session.currentAppearanceIfChanged()` compares its equipment snapshot to decide when to regenerate the body. Making it a view is open work (see gaps). |
| (5) save schema 3, sectioned, per-section dirty diffing | done: `POSITION, BACKPACK, BANK, EQUIPMENT, KIT, SKILLS, VITALS, SETTINGS, APPEARANCE, IDENTITY` with `changedSections()`; v1 and v2 profiles upgrade in place. COMBAT / NOTES / QUESTS / CONTROLLER from the backlog wording are **not** implemented (not needed before M3/M7). |
| (6) packet-free `realFinish` variant | **not implemented, and not needed today**: the native logout path is `Native947Session.close()` -> `World.removeNative947Player(player)` and never calls `finish()`/`realFinish()`. `realFinish()` would NPE for a native player on `house.finish()` / `coOpSlayer`; any content that starts calling `player.finish()` for a native player must add the guard first. |

`Skills.java` did not get per-call native guards: `sendSkillLevel` is a counted
NO-OP in the facade tier table (UPDATE_STAT is unverified until M3) and
`Skills.refresh` -> `generateAppearenceData` takes the native branch, so
`Skills.init`'s 27 refreshes cannot tear down a strict smoke. Only the two
packet-free persistence setters (`setLevelWithoutRefresh`,
`setXpWithoutRefresh`) and the two copy accessors were added.

## 6. Verification evidence (this pass)

* `gradlew test installDist writeRuntimeClasspath`: BUILD SUCCESSFUL, 198 tests,
  0 failures, 0 errors across 23 classes (182 before this pass; +1 here, the
  rest from the concurrent P5 repair).
* `Native947PlayerLifecycleTest` (7), `Native947WorldLifecycleTest` (5),
  `Native947SaveStoreTest` (20), `Native947AppearanceTest` (7) each green when
  run alone under `JUnitCore`, green in the full suite, and green in reverse
  alphabetical order in one JVM (198 tests).
* `Native947WorldSmoke` against `C:\Users\developer\Desktop\rs3cache\cache`: PASS.
  `Bootstrap ran during first admission: ok=26 failed=0 skipped=3` is printed
  from inside `attachOnWorld`; 48 ticks with `tickFailures=0`, `strictHits=0`,
  one scene rebuild, `deferred=1` (the MusicsManager region-music gap).
* `Native947PersistenceSmoke` against the same cache: PASS, including
  `schema-2 profile upgraded in place to schema 3 with quantities preserved`,
  the restart-child-JVM check, corrupt-save fail-closed and the
  checkpoint-failure disconnect.
* Appearance byte parity: `Native947AppearanceTest`
  `globalPlayerUpdaterNativeBodyIsByteIdenticalToTheTemplate` compares the
  `GlobalPlayerUpdater` native branch against `Native947Appearance.encode` for
  the bare body and for the three bronze items, and additionally against the
  literal verified hex; `Native947PlayerLifecycleTest`
  `nativeAppearanceBodyMatchesTheVerifiedTemplateForBareAndBronzeBodies`
  repeats the comparison and proves the appearance path emits no packets.

## 7. Known gaps / follow-ups

1. `Native947Containers` is still the container of record for the session
   snapshot (backlog item 4). Until it is a view over `Player.getInventory()`,
   two representations of the same items exist and `currentAppearanceIfChanged`
   has to diff the bypass snapshot to know when to regenerate the body.
2. `GlobalPlayerUpdater.buildNative947AppearanceData` withholds the body for a
   display name longer than 12 characters. `Native947WorldSmoke` uses
   `native947-reconnect` (19 chars), so its appearance branch is inert and the
   smoke reports `appearance(updates=0,withheld=1)`. Real 947 names are at most
   12 characters, so this is correct behaviour, but the smoke does not exercise
   the body for that connection.
3. `Native947Bootstrap` still skips `World.addDrainPrayerTask` and
   `World.addRestoreSkillsTask` with the reason
   "`Player.createNative947` owns no Prayer". That reason is now stale - P4
   hydrates `Prayer` - but the skip itself is still correct for M2b because both
   tasks emit unverified stat/prayer packets. The P8 owner should restate the
   reason before M3 enables them.
4. `realFinish()` has no native variant (see the table above).
5. Schema-3 has no COMBAT / NOTES / QUESTS / CONTROLLER section.

## 8. Review repair pass (foundations-b2-fix)

Four P4 findings from the Phase B review were confirmed against the source and
fixed. Build `build/foundations-b2-fix-build.log`: BUILD SUCCESSFUL, 199 tests,
0 failures; one JVM alpha and reverse order both `OK (199 tests)`; all eight
probes/smokes green.

1. **NO-OP tier discarded ids** (blocker). The seven emitters this pillar moved
   from STRICT to NO-OP called `noop(String)`, which recorded a method name and
   logged nothing - so `Bank.openBank`'s varc 6709 and every `Skills.refresh` were
   indistinguishable in the counters from any other id, and the project rule
   "unknown = counted and logged" was not met. `noop` now takes the id/value,
   records a per-method histogram (`Counters.noopIds`, on
   `Native947Session.Snapshot.noopIdsByMethod`) and logs once, exactly like
   `drop`. The resulting varc inventory (`779`, `2911`, `5886..5906`, `6709`) is
   written up in `notes/P5-router.md` section 3, so M2b acceptance (c) is now
   evaluable for that traffic. New test:
   `Native947DispatcherTest.countedNoOpsRecordTheIdTheyDiscarded`.
2. **`toolBelt` third state** - see section 1 above.
3. **`Native947PlayerBinder.restore` emitted a packet.** `player.setRun` ->
   `sendRunButtonConfig()` -> `sendConfig(463, ...)`, called from the
   `Native947Session` constructor, i.e. before `attachOnWorld` adds the transport,
   so a restored "running" profile silently lost its run state as a counted drop.
   Replaced with `Player.setRunHidden` (which delegates to `Entity.setRun` and
   emits nothing); the javadoc that claimed `Entity.setRun` is corrected. varp 463
   is added to the P5 unmatched-id inventory for M3.
4. **`GlobalPlayerUpdater.getAppeareanceData(Player viewer)` had no native
   branch.** Only `generateAppearenceData()` did, so the moment P6 serves a
   per-viewer name a native player would be encoded by `buildAppearenceData`:
   910 slot membership and a `getRenderEmote()` tail instead of the 947 BAS from
   item param 644 - a guessed body on the wire. Both the per-viewer body and its
   MD5 twin now go through `buildNative947AppearanceData` and fall back to the
   last verified body (counted by `getNative947WithheldBodies()`) rather than to
   the 910 serializer. Inert on the M2b single-player slice; load-bearing from P6.

Gap 3 in section 7 (the stale `Native947Bootstrap` skip reason for
`addDrainPrayerTask`/`addRestoreSkillsTask`) is still open and is P8's line.
