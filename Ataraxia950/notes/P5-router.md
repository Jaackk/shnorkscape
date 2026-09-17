# P5 - Native947ActionRouter into the 910 handlers (validation record)

Written 2026-09-07 while finishing the interrupted Phase B P5 slice. Everything
below was checked against the files on disk, against the Phase-A baseline mirror
(`scratchpad/baseline-b/Ataraxia947`) and against three real-cache smokes run at
`C:\Users\developer\Desktop\rs3cache\cache`. Paths are relative to `Ataraxia947/`.

---

## 1. Audit: what was on disk against the P5 design

### 1.1 Decoded overloads and legacy delegation (VERIFIED, byte-for-byte)

Diffed each handler against the baseline mirror with line endings normalised
(`diff <(tr -d '\r' base) <(tr -d '\r' current)`):

| File | Normalised changed lines | Verdict |
|---|---|---|
| `core/com/rs/network/packet/impl/ObjectHandler.java` | 39 | `handleOption(Player, InputStream, int)` reads the same wire fields, resolves the same `WorldObject`, then calls the new `dispatch(Player, WorldObject, int, boolean)`. `dispatch` re-applies the four gates the stream entry already applied (`clientHasLoadedMapRegion`, `isDead`, `isLocked`, `EmotesManager.getNextEmoteEnd`) - all four are pure reads, so re-evaluating them is a no-op for the legacy path - then runs the original `stopAll` / `setRun` / `handleRunespanPortal` / option switch unchanged. **One deliberate native-only behaviour change**: `handleOption2`'s `"bank chest"` case now also accepts `objectDef.containsOption(1, "Use")` when `player.isNative947()`. Confirmed necessary: the 947 cache names 79036 `Bank chest` with options `[null, Use, Collect, Load Last Preset from, null]`, so `containsOption(1, "Bank")` is false there and true on the 910 cache. Legacy comparison untouched. |
| `core/com/rs/network/packet/impl/NPCHandler.java` | 86 | Five stream entries now read their fields and delegate to `handleOption1/2/3/4(Player, NPC, boolean)` and `handleExamine(Player, NPC, boolean)`, plus a `dispatch(...)` switch. Each decoded body re-checks `player.isLocked()`; verified against the baseline that all four stream entries already checked it first (examine did not, and the decoded examine body does not add it). Bodies are otherwise identical. |
| `core/com/rs/network/packet/impl/ButtonHandler.java` | 36 | `handleButtons(Player, InputStream)` decodes `interfaceId`/`componentId`/`baseHash`/`componentIdBig` exactly as before and delegates to `handleButtons(..., componentIdBig)`. The `BUTTON_TRACE_USERS` trace moved with the body and still runs before the `1477:13` early return. The pre-prediction `item.getId() != slotId2` check at the backpack branch is **not** relaxed. |
| `core/com/rs/network/packet/impl/InventoryOptionsHandler.java` | 0 | Unchanged. The decoded `handleItemOptionN(Player, int, int, Item)` entries already existed; `handleItemOnItem` was not extracted because no M2b action reaches it. |
| `core/com/rs/network/packet/PacketRepository.java` | 0 | Unchanged. The planned static extracts (walk / attackNpc / playerOption / itemTake / switchComponents) were not made. Only `switchComponents` is in the M2b slice, and the adapter reaches its effect through `Inventory.switchItem` (the same call the inline block makes). See section 5. |
| `content/com/rs/game/player/content/InterfaceManager.java` | 113 | Adds `registerNativeOpen` / `unregisterNativeOpen` (native-only, throw for legacy players), `setNative947Bindings`, and routes the three `& 0xfff` slot lookups through one `getComponentIdByKey`, which keeps the exact legacy mask on the 910 cache and validates the full hash against root 1477 (or the binding table) on the flat 947 cache. Legacy path is the same expression it always was. |

**ObjectHandler line-ending churn**: the file's raw diff is 759 lines but only 39
once CRLF is normalised - the P5 pass rewrote a block of imports and two method
bodies with LF endings. No behaviour change; noted so a future reviewer does not
chase it.

### 1.2 (947 interface, component) -> (910 interface, component) translation (VERIFIED)

`Native947ActionRouter.buildBindings()` builds six translations and applies them
in `Native947Interactions.button()` **before** `ButtonHandler.handleButtons` is
called. Confirmed against the handler source and observed live in the smokes
(the `IF=... COMP=...` lines are `ButtonHandler`'s own trace):

| Native 947 pair | 910 pair | 910 branch reached |
|---|---|---|
| 1473:5 (`backpack.items`) | 1473:7 | `interfaceId == Inventory.INVENTORY_INTERFACE && componentId == 7` -> `InventoryOptionsHandler.handleItemOptionN` |
| 517:201 (`bank.items`) | 517:184 | `Bank.withdrawItem/withdrawDefaultAmount/sendExamineBankItem` |
| 517:15 (`bank.inventory`) | 517:14 | `Bank.depositItem/depositLastAmount/sendExamineInteractionItem`, and packet 7 -> `ButtonHandler.sendWear` |
| 517:39 (`bank.deposit_all`) | 517:37 | `componentId >= 37 && componentId <= 46`, type 0 -> `Bank.depositAllInventory(true)` |
| 517:317 (`bank.close`) | (none, -1) | routed to `Player.closeInterfaces()` instead; the 910 client closes the bank with CLOSE_INTERFACE, not a button |
| 1462:31 (`worn_equipment.items`) | 1462:31 | `Equipment.handleEquipment` |

Option -> packet mapping verified against the handler bodies: backpack/equipment
options 1..7 map to `ACTION_BUTTON1..7_PACKET` one-to-one and native examine (10)
to packet 8; the bank uses the verified amount table instead (1 -> packet 2,
5 -> 3, 10 -> 4, all -> 6), because 910 packet 1 means "the server's default
amount" while native bank option 1 means "one". Both directions of every
translation are covered in `tests/modern947/Native947ActionRouterTest.java`.

### 1.3 Actor-prediction pre-filters (VERIFIED, applied before the 910 handler)

All three live in the router as pure static functions and are called by the
adapter before `router.button(...)`:

* `transferItem` - the cleared backpack actor (-1) is accepted only when the
  requested quantity would exhaust the clicked slot and that slot did not change
  earlier in the same tick; any other claim is passed through unchanged so the
  handler's own exact-id check decides.
* `withdrawalItem` - the native bank script compacts before the sender reads the
  actor, so a full withdrawal claims the NEXT slot's item, or `48447` when the
  cleared slot was the last one. Only the predicted post-action form is accepted.
* `exactClaim` - backpack onOp 1620 and worn-equipment script 8471 keep the
  original actor id, so an exact match is required.
* `reversedDragPair` / `dragSlotsValid` - **added during this validation pass**.
  The rule was inline in `Native947Interactions.drag` and therefore untestable;
  it is now a router function the adapter calls, so the unit test covers it.

Nothing inside the 910 handlers was relaxed to make these work: the router
converts the claim to the real item id, it does not weaken the check.

### 1.4 `Native947Containers` as a read/save model (VERIFIED)

Unchanged from the Phase-A baseline and already correct for P5: its constructor
binds `player.getInventory().items`, `player.getEquipment().getItems()` and
`player.getBank()` - the same objects the 910 `Bank`/`Inventory`/`Equipment` code
mutates - so its snapshots are a view, not a second truth. The M2b flow now uses
only `seedStarterItems`, `restore`, `saveSnapshot`, `claimEquipmentKit`,
`withdrawableAmount` and the three snapshot methods; `deposit`/`withdraw`/
`equip`/`unequip` are no longer reached from the adapter (they remain covered by
`Native947ContainersTest` and by the save/restore path).

### 1.5 Resolver installation (VERIFIED)

`Native947ActionRouter.installBindings` installs `Native947Bindings
.allowListResolver()` into `Native947IdMap` once per table and hands the table to
`InterfaceManager`. Per-kind counters are live: the smokes print
`947 id resolver[ALLOW_LIST]: N rejected, component=..., container=..., interface=...,
script=..., varbit=..., varp=...`. Varcs are rejected unconditionally
(no verified 947 binding) and containers throw inside the writer body so the
facade counts a drop rather than masking `-1` into container key 65535.

---

## 2. What was missing or wrong, and what was fixed

The P5 files had never been compiled-tested or executed. Four defects blocked the
slice; all are fixed and proven by the three smokes.

1. **`Player.getToolBelt()` was null for every native player and NPEs on the very
   first `stopAll()`.** `Player.stopAll -> CombatDefinitions.resetSpells ->
   refreshBonuses -> refreshRunes -> Magic.getTotalAmountOfRune ->
   Inventory.containsItem` dereferences it, and `ObjectHandler.dispatch` calls
   `stopAll()` before anything else. `Player.hydrateForNative947` did try to build
   one but `Toolbelt`'s constructor walks enum 13730 -> struct params 6979/6980,
   which is unverified on the 947 cache, so construction threw and the field was
   left null. Guarding all 40 `getToolBelt()` call sites is not an option.
   **Fix**: `content/com/rs/game/player/content/Toolbelt.java` `addDefaultItems()`
   now returns early (belt stays empty, logged once) when
   `Cache.isFlatReadOnly()`. The legacy cache takes the original path untouched.
   An empty belt is the fail-closed answer: `contains()` says "no tool".
   *Consequence to remember for M8*: on native, `handleItemOption2` on an item
   that IS a toolbelt item would now consume it into the empty belt instead of
   reporting "already in your toolbelt". None of the M2b items (995/1511/315/
   1277/1173/1139) are toolbelt items.

2. **A bootstrap class initialiser left a stray NPC in `World.getNPCs()`, which
   made `World.addNative947Npc` refuse every session that uses the banker view.**
   `Native947Bootstrap.run()` (P4 now calls it inside `Native947World.attach`)
   initialises `ControllerHandler`, which loads
   `game/com/rs/game/player/controllers/WarriorsGuild.java`, whose static
   `CATAPULT_PROJECTILE_BASE = new NPC(1957, ...)` registers itself in the world
   as a side effect of construction. `Native947EquipmentSmoke` and
   `Native947NpcSmoke` both died with
   `IllegalStateException: Native 947 currently requires an otherwise empty NPC world`.
   **Fix**: that one field now passes `toSpawn = !Cache.isFlatReadOnly()`, so the
   legacy server keeps its (pre-existing) phantom NPC and the 947 JVM does not
   register one. The strong single-slot gate in `World.addNative947Npc` is
   deliberately left intact so the next such offender fails loudly.
   **This is a class of defect, not one instance**: any future bootstrap step
   whose class initialiser constructs an `NPC` will trip the same gate. P8/M4
   should add an explicit post-bootstrap assertion that `World.getNPCs()` is
   empty, naming the offender.

3. **Three more managers the M2b 910 paths dereference were absent.** Added to
   `Native947Interactions.ensureLegacyFields` (see section 4 for the handover
   list): `treasureTrails`, `DungeoneeringBinds`, `GemBag`.

4. **The reversed-drag rule was untestable.** Moved from a private branch in
   `Native947Interactions.drag` into `Native947ActionRouter.reversedDragPair` /
   `dragSlotsValid` and covered by the unit test.

Also added: `Native947ActionRouter.legacyDispatchCounters()` and
`Native947InteractionsSmoke.reportRouting(...)`, so each smoke now prints the
910 decoded-dispatch counters, the facade tallies and the router report, and
fails closed on `facadeStrictHits != 0`, `handlerFailures != 0`, an unexpected
unmatched-pair count, or zero router dispatches.

---

## 3. Unmatched-id report (every 910 emitter the M2b slice reaches that has no 947 binding)

These are **counted drops**, never wire writes and never strict throws. Collected
from the three smokes' `[Ataraxia947] UI bindings: <kind> <id> has no 947 binding`
lines. M3/M7 must bind or retire each one.

**varps (`sendConfig`)** - 22:
`111 160 680 711 712 713 714 715 716 717 718 1037 1038 1039 3561 3562 3563 3596 7751 7752 7753 8970`
* `8970`/`8971` come from `Bank.refreshBankSize`; 8971 IS bound (the adapter sends
  the occupied span itself), 8970 is not.
* `711-718`, `3561-3563`, `3596` are the `Equipment`/`CombatDefinitions` bonus
  varps regenerated on every wear/remove (M3, "stats/vitals").
* `1037-1039`, `111`, `160`, `680`, `7751-7753` come from `Bank.openBank`'s
  refresh chain and from `Skills`/settings defaults.

**varbits (`sendConfigByFile`)** - 24:
`22179 22186 22187 26188 39433 45139 45140 45143 45144 45145 45146 45147 45148 45149 45150 45151 45152 45153 45154 45155 45156 45190 45191 45192`
* `45139-45156`, `45190-45192` are the 947 bank varbits `Bank.init`/`refreshTabs`/
  `refreshWithdrawNotes`/`refreshLeavePlaceHolders` write. Backlog M2b already
  requires these to be bound through P2 rather than written twice.
* `22179`, `22186`, `22187`, `26188` are the preset varbits (deferred to M3 with
  `Bank.loadPreset`); `39433` is "close bank presets tab".

**cs2 scripts (`sendExecuteScript` / `sendRunScript`)** - 11:
`1364 1487 5559 5561 6992 7808 8420 8841 8862 8901 9299`
* `8420` is the `Bank.openBank` title/name script the backlog already lists for
  M7 rebinding; `9299` is the bank-close script from
  `InterfaceManager.removeBankInterface`; `5559`/`5561` are the "you cannot carry
  that many" chat-script pair; the rest come from the bank tab/filter refreshes.

**interfaces (`sendIComponentText`, `sendHideIComponent`)** - 2: `187`, `1448`.
* `187` is the legacy world-map/hint text panel driven by a background world task;
  `1448` is the settings sub-interface `Bank.unlockButtons` hides.

**components of a bound interface (517)** - 24:
`517:14 517:27 517:33 517:112 517:151 517:152 517:153 517:184 517:187 517:188
517:199 517:200 517:207 517:245 517:247 517:248 517:249 517:250 517:251 517:258
517:268 517:279 517:286 517:294`
* These are the 910 bank component ids that `Bank.unlockButtons` /
  `closeHiddenTabs` / `refreshTabs` address for **output**. Interface 517 itself
  is bound; its component layout is not, so each `sendIComponentSettings` /
  `sendHideIComponent` / `sendUnlockIComponentOptionSlots` on an unbound
  component is dropped. Note that `517:14` and `517:184` appear here as OUTPUT
  drops even though the router uses them as INPUT translation targets - input
  translation is a router table, output still needs a P2 binding.

**containers (`sendItems`)** - 1: `623` (`Bank.refreshBob`'s beast-of-burden
container; there is no familiar, so it always sends an empty container).

**varcs (`sendGlobalConfig`)** - 24: `779 2911 5886 5887 5888 5889 5890 5891
5892 5893 5894 5895 5896 5897 5898 5899 5900 5901 5902 5903 5904 5905 5906 6709`

**Update, M3:** these are now counted DROPS, not NO-OPs. S2 verified
CLIENT_SETVARC_SMALL 1 / _LARGE 112 and the varc families were promoted to REAL
writers, so the discard moved to the resolver: the binding table declares no varc
at all, and `Native947IdMap.IDENTITY` - the default when no table is installed -
returns -1 for the varc kind specifically, so the same 24 ids are rejected with or
without a table. The list itself is unchanged; only the tier is. What follows
describes the pre-M3 state.

These were counted NO-OPs, not resolver rejections: the varc family had no verified
947 packet (CLIENT_SETVARC_SMALL/LARGE was S2 work), so the facade discarded them at
the NO-OP tier. Until the repair pass below they were discarded *anonymously* -
`noop(String)` took no arguments and logged nothing - which meant M2b acceptance
(c) could not be evaluated for this traffic at all. `Native947PacketDispatcher.noop`
now takes the id, records it in `Counters.noopIds(method)` and logs it once; the
list above is the union of the three smokes' `discarded no-op ids` lines
(`build/foundations-b2-fix-Native947{Interactions,Npc,Equipment}Smoke.log`).

* `6709` is `Bank.openBank` / `refreshBob`'s bank-title varc (backlog M2b names it).
* `779` is `GlobalPlayerUpdater.buildAppearenceData`'s varc; a native player takes
  the P4 appearance branch, so these come from the legacy emitters that still run
  around it (`Equipment` / `CombatDefinitions` refreshes).
* `2911` and the contiguous block `5886..5906` are the `CombatDefinitions` /
  `Equipment` bonus varcs regenerated on every wear/remove - the varc twin of the
  `711-718` / `3561-3563` varps above. M3 ("stats/vitals") binds or retires them.

**varps written by a packet-free path, unbound** - 1: `463` (run button).
`Player.setRun` -> `sendRunButtonConfig()` -> `sendConfig(463, ...)`. The binder no
longer calls it (it uses `Player.setRunHidden`), so nothing on the M2b path emits
463 today; it is listed here because M3 turns the run toggle on and must bind it.

**Router-level unmatched (947 interface, component) pairs**: `517:202` and
`1473:6`, both fabricated by `Native947InteractionsSmoke` on purpose as negative
tests. In `Native947EquipmentSmoke` and `Native947NpcSmoke` the count is 0.

### 3a. M3 integration re-measurement (2026-09-07)

The inventory above is left exactly as P5 recorded it so the measurement stays
reproducible. Re-collected after M3 from `build/m3-Native947{World,Interactions,
Npc,Equipment,Persistence,Bootstrap}Smoke.log`, the list changed by exactly one
entry and gained nothing:

* **Bound by M3**: `463` (run toggle). It now leaves through VARP_SMALL/VARP_LARGE
  and no longer appears in any smoke's `has no 947 binding` output. The
  "varps written by a packet-free path, unbound" bullet above is therefore
  historical; that count is now 0.
* **Unchanged**: the 22 varps, 24 varbits, 11 scripts, 2 interfaces, 24 components
  of 517, 1 container (`623`) and 24 varcs are still counted drops, id for id and
  kind for kind. The varc block moved from the NO-OP tier to the drop tier when
  M3 promoted `sendGlobalConfig` to REAL - the ids are unchanged and still
  itemised, now under `dropped` rather than `noops`.
* M3 also bound vars `1668`, `16736`, `41524`, `19007`, `679` and `462`, which
  never appeared above because the M2b slice this section measured did not emit
  them. The bonus varps `711-718` / `3561-3563` / `3596` and varcs `779` / `2911` /
  `5886..5906` were investigated and **retired, not bound**; the located 947
  readers and the retire reason for each are in `notes/M3-bindings.md` section 3,
  which hands them to the equipment-stats-panel milestone.

---

## 4. Handover to P4: managers `Native947Interactions.ensureLegacyFields` still builds

All are packet-free to construct and are dereferenced by an M2b 910 path.
`hydrateForNative947` should absorb them so the adapter can stop doing it.

| Field / setter | Dereferenced by |
|---|---|
| `player.treasureTrails` | `ObjectHandler.handleOption2` calls `getTreasureTrails().useObject(object)` unconditionally, before the bank-chest branch |
| `player.setDungeoneeringBinds()` | `ButtonHandler.handleButtons` calls `getDungeoneeringBinds().processButtonClick(...)` unconditionally, before any interface branch |
| `player.setGemBag()` | `InventoryOptionsHandler.handleItemOption2` dereferences `getGemBag()` twice before it reaches `ButtonHandler.sendWear` |
| `player.gimBank` | every `ButtonHandler` 517 branch, `Bank` history, `ItemConstants.isBankAble` |
| `player.pouch` | `Bank.withdrawItem` coin routing |
| `player.quests` | `ItemConstants.isBankAble` |
| `player.harmonyPillars`, `player.vineHerbPatches` | the `ObjectHandler` option-2 runnable |
| `player.setChargesManagerNew(...)` | `ButtonHandler.sendWear` |

The `Toolbelt` fix in section 2 means `hydrateForNative947`'s existing
`if (toolBelt == null && Cache.isFlatReadOnly())` branch now succeeds instead of
being swallowed by its own catch. Under JUnit (no cache at all) the branch is
still skipped and `toolBelt` stays null - safe today because no cache-free test
calls `stopAll()`, but it is the same landmine one cache away.

---

## 5. Deliberate deviations from the P5 design, and why

1. **NPC options do not go through `NPCHandler`.** `npcBank`, `npcTalk` and
   `npcCollect` run the controller hook the corresponding `handleOptionN` runs
   (`processNPCClick2/1/3`), face the entities, and then do what the 910 banker's
   *dialogue* would do (`Bank.openBank()`), because the 910 NPC path for a banker
   starts the `BankList`/`Banker` dialogues, which are M7. `npcExamine` runs
   `processNPCExamine` and then sends the verified content name, because
   `NPC.getDefinitions()` throws for a native NPC until P6. Consequence:
   `NPCHandler.DISPATCHES` is 0 in every smoke while
   `ObjectHandler.DISPATCHES` and `ButtonHandler.DISPATCHES` are not. This is
   visible in the evidence block and is expected until M7/P6.
2. **`PacketRepository` static extracts were not made.** Of the five in the
   design, only `switchComponents` is in the M2b slice, and the adapter reaches
   its effect with `player.getInventory().switchItem(from, to)` - the identical
   call the inline block makes. The other four (walk, attackNpc, playerOption,
   itemTake) belong to M4-M6.
3. **`InventoryOptionsHandler.handleItemOnItem` was not extracted.** No M2b
   action reaches it.
4. **Coin-pouch redirect.** `Bank.withdrawItem` routes coins into the 910 money
   pouch, but the verified 947 bank open recipe promises coins to the backpack
   (varbit 45158 = 1). `Native947Interactions.redirectPouchCoins` moves them back
   through `Inventory.addItem` and counts it (`coinPouchRedirects`). The clean
   fix is a native branch in `Bank.withdrawItem`; that is M3 work, when the money
   pouch UI is actually bound.

---

## 6. Evidence

Build: `.\gradlew.bat --no-daemon --console=plain --offline test installDist writeRuntimeClasspath`
-> BUILD SUCCESSFUL, **198 tests, 0 failures, 0 errors** (182 before this pass;
`Native947ActionRouterTest` adds 16).

Real-cache runs against `C:\Users\developer\Desktop\rs3cache\cache` (logs under `build/`):

| Probe / smoke | Result |
|---|---|
| `Native947InteractionsSmoke` | PASS - coins 1000 / logs 5 / shrimps 5 conserved; `ObjectHandler.dispatch=3`, `ButtonHandler.handleButtons=15`; router dispatched 18, rejected 0, handlerFailures 0, unmatchedPairs 2 (both fabricated); `strictHits=0` |
| `Native947EquipmentSmoke` | PASS - Collect / wear x4 / remove / bank deposit / bank withdraw / re-equip, same-JVM and fresh-JVM restore; `ButtonHandler.handleButtons=7`; router dispatched 10, rejected 0, handlerFailures 0, unmatchedPairs 0; `strictHits=0` |
| `Native947NpcSmoke` | PASS - Bank/Talk/Examine, removal/re-add, scene rebuild, reconnect; `ButtonHandler.handleButtons=2`; router dispatched 6 (2 npcBank, 1 npcTalk, 1 npcExamine, 2 button), rejected 0, handlerFailures 0, unmatchedPairs 0; `strictHits=0` |
| `Native947WorldSmoke` | PASS |
| `Native947PersistenceSmoke` | PASS |
| `Native947BootstrapSmoke` | PASS |
| `DefinitionScanProbe` | PASS |
| `VarBitScanProbe` | PASS |

One pre-existing assertion was updated, in `Native947EquipmentSmoke`:
"Walking and wearing in one tick must publish one combined movement/appearance
frame". The 910 `InventoryOptionsHandler` batches switches through
`Player.getSwitchItemCache()` and runs `ButtonHandler.sendWear` from a
`WorldTasksManager` task, so the worn set changes on the following tick, exactly
as it does for a legacy 910 player; the walk step is still applied on the click
tick. The frame therefore splits in two. The replacement asserts the step landed
on the click tick and that the wear published **exactly one** appearance frame on
the next tick, and `assertEquipment` still proves that frame carries the helmet.
Recreating the single combined frame would mean bypassing the 910 handler again,
which is what P5 removes.

---

## 7. Phase B review repair pass (foundations-b2-fix)

Seven review findings were checked against the files on disk; all seven were
real and all seven are fixed. Evidence: `build/foundations-b2-fix-build.log`
(BUILD SUCCESSFUL, 199 tests, 0 failures), `build/foundations-b2-fix-singlejvm-alpha.log`
and `-reverse.log` (OK (199 tests) in one JVM in both orders) and the eight
`build/foundations-b2-fix-<probe>.log` real-cache runs.

1. **`Native947PacketDispatcher.noop` discarded its arguments** (blocker). The
   seven emitters P4 moved from STRICT to NO-OP (`sendGlobalConfig`,
   `sendGlobalConfigSmall`, `sendGlobalConfigLarge`, `sendCSVarInteger`,
   `sendGlobalString`, `sendCSVarString`, `sendSkillLevel`) recorded only a method
   name, so an unverified varc id was swallowed with no record - the opposite of
   "unknown = counted and logged", and it made acceptance (c) unevaluable for that
   traffic. `noop` now has `drop`'s shape (varargs + `logOnce`), the id lands in a
   per-method histogram (`Counters.noopIds` / `noopIdsByMethod`, surfaced on
   `Native947Session.Snapshot` and printed by every smoke), and the resulting varc
   inventory is section 3 above. Pinned by
   `Native947DispatcherTest.countedNoOpsRecordTheIdTheyDiscarded`.
2. **`Player.toolBelt` was a silently-null manager** (blocker; also raised against
   P4). It was built only when `Cache.isFlatReadOnly()`, so a cache-free JVM left
   it null while `Inventory.containsItem` / `containsOneItem` dereference it
   unguarded and `ObjectHandler.dispatch` reaches them through `stopAll()`. The
   belt is now constructed unconditionally in `hydrateForNative947`,
   `Toolbelt.addDefaultItems` skips seeding on "the enum 13730 chain is
   unavailable" (`Toolbelt.defaultToolChainAvailable()`) rather than on
   `isFlatReadOnly` alone, and `requireNative947Manager("toolBelt", toolBelt)` is
   part of the admission gate. Section 4's handover line and section 2 item 1 are
   superseded by this.
3. **An unseeded belt destroyed the item it could not hold** (section 2 item 1
   understated this as a wrong chat message). `addItem` deleted the item from the
   inventory *before* `refresh()`, and `refresh()` NPE'd on
   `newItems[0][0].getId()` for every tool but the slot-0 pickaxe. `addItem` now
   returns false immediately when the belt cannot be refreshed (a belt that holds
   nothing accepts nothing, so the caller falls through to its normal Wear
   handling with the item intact) and the slot-0 varbit write is null-guarded.
4. **`Native947PlayerBinder.restore` was not packet-free.** It called
   `Player.setRun`, which emits varp 463 through the facade at a point in
   admission where the transport is provably not attached (the session
   constructor runs before `addLast(TRANSPORT_NAME, transport)`), so a restored
   "running" profile lost its run state as a counted drop. It now uses
   `Player.setRunHidden`; the javadoc, which named `Entity.setRun`, is corrected.
5. **`GlobalPlayerUpdater.getAppeareanceData(Player viewer)`** (and the matching
   `getMD5AppeareanceDataHash(Player)`) had no native branch, so the first
   per-viewer name P6 serves would encode a native player with 910 slot membership
   and `getRenderEmote()` instead of the 947 BAS. Both now rebuild through
   `buildNative947AppearanceData` and keep the last verified body when it cannot
   be built. Dead on the M2b single-player slice; live from P6.
6. **`reportRouting` printed the 910-dispatch evidence but asserted nothing.**
   `routerDispatches > 0` is satisfied by `npcTalk`/`npcCollect`/`npcExamine`,
   none of which call a 910 handler (section 5 item 1), so a regression to a
   bypass would not have failed any smoke. Each smoke now captures the
   `ObjectHandler`/`NPCHandler`/`ButtonHandler` `DISPATCHES` counters before it
   acts and `reportRouting` requires minimum deltas: interactions object>=3
   button>=15, equipment button>=7, npc button>=2. Observed exactly at those
   minimums in this pass, so acceptance (b) is now measured.
