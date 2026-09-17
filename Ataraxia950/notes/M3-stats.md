# M3 area B - skills, vitals and run energy on the native 947 tick

Written 2026-09-07 alongside the M3 slice. Paths are relative to `Ataraxia947/`.
Everything below was checked against the files on disk and against the
Phase-C baseline mirror
(`scratchpad/baseline-m3/Ataraxia947`). Evidence for every id and byte layout
is `OpenNXT/data/prot/947/generated/native947-3/verified/` - `UPDATE_STAT.md`,
`UPDATE_RUNENERGY.md`, `vars/VARBIT_LARGE.md`, `vars/VARP_SMALL.md`,
`ui/SKILLS_TAB.md` and `ui/ORBS_AND_VARS.md` (plus their `-verification.md`
companions). Every var ID and value transform bound here comes from a CONFIRMED
row. One WIRE FORM does not: VARP_LARGE 111 is used for adrenaline alone and is
not in `verifiedNames.toml` - see section 1 and gap 7.

This area owns the ENGINE path only. The wire tiers (`sendSkillLevel` ->
UPDATE_STAT, `sendConfigByFile` -> VARBIT_LARGE, `sendRunEnergy` ->
UPDATE_RUNENERGY) and the binding table entries are area A; the panel/orb
bootstrap is area C.

---

## 1. What the client actually needs, on login and on change

| Surface | Server value | Packet | Id | Evidence |
|---|---|---|---|---|
| One stat | experience x10, CURRENT level | UPDATE_STAT 66 | stat 0..28 | `UPDATE_STAT.md` read layout + setter `0x140369C10` |
| Life points | `hitpoints * 10` | VARBIT_LARGE 71 | varbit 1668 (varp 659 bits 1..15) | `ORBS_AND_VARS.md` 5, scripts 8122 / 2915 / 16856 |
| Prayer points | `prayerpoints * 10` | VARBIT_LARGE 71 | varbit 16736 (varp 3274 bits 0..14) | `ORBS_AND_VARS.md` 5, script 5256 |
| Summoning points | `summoning level * 100` | VARBIT_LARGE 71 | varbit 41524 (varp 8040 bits 0..14) | `ORBS_AND_VARS.md` 5, scripts 873/874 |
| Adrenaline | `specialAttackPercentage * 10` (0..1000) | VARP_SMALL 10 below 128, else **VARP_LARGE 111 (no verdict)** | varp 679 | `ORBS_AND_VARS.md` 5, script 8125 |
| Auto-retaliate | `autoRetaliate ? 0 : 1` | VARP_SMALL 10 | varp 462 | `ORBS_AND_VARS.md` 5, scripts 8129/8131 (1 = OFF) |
| Run energy | 0..100 percent | UPDATE_RUNENERGY 116 | - | `UPDATE_RUNENERGY.md`, cs2 op 731 |
| Run toggle | 0 walk / 1 run / 3 rest | VARP_SMALL 10 | varp 463 | `ORBS_AND_VARS.md` 4.1, scripts 1315/1316/1741 |
| Virtual levels | 0/1 | VARBIT_LARGE 71 | varbit 19007 (varp 458 bit 30) | `SKILLS_TAB.md` 5.3 and 9.6 |

**Which opcodes are actually confirmed.** `verifiedNames.toml` carries 66
UPDATE_STAT, 10 VARP_SMALL, 71 VARBIT_LARGE, 116 UPDATE_RUNENERGY and 108
UPDATE_RUNWEIGHT. It does NOT carry 50 VARBIT_SMALL or 111 VARP_LARGE:
`PROTOCOL-S-SUMMARY.md` section 3 lists both as "not promoted for lack of a
verdict this round ... They need a verdict before promotion." The facade
therefore sends every varbit on VARBIT_LARGE 71 - three extra bytes, whole range,
verified layout - and never writes opcode 50 at all; and it sends every varp that
fits a signed byte on VARP_SMALL 10. That leaves exactly one M3 value on an
unverified wire form: adrenaline, whose 0..1000 range cannot fit VARP_SMALL. The
writer `Native947Packets.varbitSmall` and its byte test are kept for the day
opcode 50 gets a verdict; nothing calls it.

**Login versus change.** The 947 skills tab (interface 1466) has no `onLoad`
hook at all: `SKILLS_TAB.md` section 2 shows `1466:0`'s builder is installed as
`onVarTransmit`/`onStatTransmit` (script 8488 with 13 component hashes) with an
EMPTY stat filter list, and `UPDATE_STAT.md`'s trigger-dispatcher analysis shows
an unfiltered hook fires for every stat transmit. So the login burst of 27
UPDATE_STAT frames is what draws the panel; nothing else has to be sent to make
it appear. On change, one UPDATE_STAT is enough: scripts 525 and 532 are
per-cell `onStatTransmit` handlers with a one-skill filter, so the client
repaints only the changed cell. The action-bar bars behave the same way, from
their `varpTransmitList` / `statTransmitList` (1430:7 lists stat 3, 1430:14
lists stat 5, 1430:20 lists stat 23).

**Boosted / drained levels.** The client never takes the base level from the
packet: setter `0x140369C10` recomputes it from the experience through the stat
definition's table and stores it at `entry+0x10`, while the packet's level byte
lands at `entry+0x14` (the CURRENT level). Script 8489 colours the cell by
comparing `op_04d8` (current) with `op_014` (base): red below, green above.
`Skills.refresh` therefore calls `sendSkillLevel(skill)`, whose facade
implementation reads `Skills.getLevel` (the boosted/drained short) and
`Skills.getXp`; `Skills.getLevelForXp` is deliberately NOT what goes on the
wire. `Native947StatsTest.updateStatCarriesTheBoostedOrDrainedCurrentLevelNotTheBase`
pins both directions.

**Skills 27 and 28.** Enum 680 has 29 entries (27 Archaeology, 28 Necromancy)
and enum 7674 renders 29 cells, but Ataraxia's `Skills` models 0..26 and has no
level, xp table or content for the other two. They are never sent. Consequence,
recorded rather than faked: the client keeps its stat-table initialiser defaults
for them - `stat-table-initializer.disassembly.txt` builds every entry with
experience 0, base 1, current 1 - so both cells render as **level 1 with 0 xp**,
the total level (script 4708, which sums 29 raw base levels) is **2 higher** than
this server's own `getTotalLevel()`, and the combat level formula 1432 reads
stat 28 as 1. Nothing is transmitted for them, so nothing is wrong on the wire;
the display simply shows two unstarted skills. Fixing this means giving Ataraxia
the two skills, which is content work well outside M3.

---

## 2. Changes, file by file

### `game/com/rs/game/player/Skills.java`

* **`init()`** takes a native branch, `initNative947()`: `ensureXpTrackerInitialised()`
  (packet-free, and `addXp` dereferences the tracker arrays), then one
  `sendSkillLevel` per modelled skill, then `refreshVirtualLeveling()` and
  `refreshSummoningPoints()`. Nothing else. The legacy body is untouched.
  Deliberately not run on native, each with its reason:
  * `sendXPDisplay` / `refreshCounterXp` / `refreshXPDisplay` - varps 91..93 and
    varbits 225+/229+/19964 have no 947 reader, and the XP tracker slot
    (enum 7716 key 1015) is still CANDIDATE (`SKILLS_TAB.md` 6 and 9.7).
  * `refreshXPPopUp` - it calls `setWindowInterfaceByKey(1026, 1213)`, i.e. it
    OPENS an interface in a slot the evidence marks CANDIDATE.
  * `refreshEnabledSkillsTargets` / `refreshUsingLevelTargets` /
    `refreshSkillsTargetsValues` - they resolve ids through
    `RS3ClientScriptMap.getMap(1482)`, a 910 cache map, and write varps
    1115/1117/1118+ that the 947 layout does not read.
  * `refreshXpBonuses` - varps 3304..3327 are the 947 XP-TARGET vars read by
    tooltip script 547 (`SKILLS_TAB.md` 9.6), not bonus xp. Writing bonus values
    there would be a WRONG binding, not a missing one.
  All of these would otherwise have added roughly 60 new unbound ids to the
  counted-drop inventory in `notes/P5-router.md` section 3, which M3 acceptance
  (e) checks must only shrink.

* **`refresh(int)`** keeps `sendSkillLevel` first for every player. On native it
  regenerates the appearance body only for the eight stats the 947 body actually
  encodes (`affectsNative947Appearance`: attack, defence, strength, hitpoints,
  ranged, prayer, magic, summoning - the inputs of
  `getCombatLevel`/`getCombatLevelWithSummoning`, the only skill-derived values
  in `GlobalPlayerUpdater.buildNative947AppearanceData`). That removes 19 of 27
  body rebuilds and MD5 hashes per login with no visible difference. Legacy keeps
  regenerating for every skill.

* **`refreshVirtualLeveling()`** on native writes varbit 19007 straight through
  `sendConfigByFile`. The legacy route (`VarBitManager` -> `VarsManager.updateVarBit`)
  recomputes the WHOLE of varp 458 - which also carries 19009 - and needs a cache
  lookup for the bit range; `VARS.md` puts 19007 in the varbit family (the facade
  sends it on the confirmed VARBIT_LARGE 71) and `SKILLS_TAB.md` 9.6 confirms no
  cs2 script writes it, so the server must.

* **`addXp` / `silentAddXp`**: `player.harmonyPillars` is null-guarded (it is a
  legacy-login field that native admission builds in
  `Native947Interactions.ensureLegacyFields`, so a Player that never opened a
  session had `addXp` NPE on its first line), and the level-up feedback goes
  through a new `announceLevelUp(int)` which keeps the `LevelUp` dialogue for
  legacy players and sends a verified MESSAGE_GAME line for native ones. The
  dialogue drives the 910 chatbox component numbers that `CHATBOX_DIALOGUE.md`
  refutes for this cache; the dialogue family is M7.

### `game/com/rs/game/player/Player.java`

* **`refreshHitPoints()`**: P4's `deferNativeRefresh` stub is gone. Varbit 1668 is
  CONFIRMED for 947 as varp 659 bits 1..15, the client's maximum is Constitution
  CURRENT level x100 and this server's hitpoints are level x10
  (`getMaxHitpoints`), so `hitpoints * 10` is exactly right, and the
  `Short.MAX_VALUE` clamp matches the 15-bit field. Every 910 caller (heal,
  `restoreHitPoints`, `Equipment` bonus changes, `Pots`, the death reset) now
  reaches the client.
* **`hydrateForNative947()` builds `banks`** (the `List<Bank>` side list) and the
  admission gate requires it. Found while wiring `addXp`: `Player.hasItem(Item)`
  iterates `getBanks()` with no null guard and `Skills.handleSkillShards` reaches
  it on roughly half of all experience awards (`Utils.random(100) <= 50`), so the
  first xp award on a native player NPE'd - intermittently, which is worse. The
  legacy `Player(password, mac)` constructor builds the same empty list; the
  primary bank lives in the `bank` field, not in this one, so behaviour is
  identical. These are the only two changes in this file.

### `game/com/rs/game/player/client/Native947Session.java`

* `ready()` calls a new `sendNative947LoginState()` AFTER `interactions.bootstrap()`
  (area C attaches the panels there; the stats have to arrive after the panel is
  attached because the panel is built by the stat transmit). It pushes, from the
  real 910 state: `Skills.init()`, `refreshHitPoints`, `Prayer.refreshPrayerPoints`,
  `CombatDefinitions.refreshSpecialAttackPercentage` and `refreshAutoRelatie`,
  `sendRunEnergy` and `sendRunButtonConfig`.
* `Snapshot` gains `skillXp`, `prayerPoints` and `running` so the smokes can
  assert the restored VITALS without reaching into the Player.
* No per-tick emitter was added: after login the ordinary 910 code already
  refreshes each var when it changes - `World.addRestoreHitPointsTask` and
  `Player.heal` call `refreshHitPoints`, `World.addDrainPrayerTask` calls
  `Prayer.drainPrayer`, `Player.processEntity` calls `restoreRunEnergy`,
  `Entity.processMovement` calls `drainRunEnergy`, `Skills.set` (from
  `World.addRestoreSkillsTask`) calls `refresh`.

### `game/com/rs/game/player/client/Native947Bootstrap.java`

`World.addDrainPrayerTask` and `World.addRestoreSkillsTask` carry
`Requirement.NONE` again and are queued. The recorded skip reason ("requires-M3:
Player.createNative947 owns no Prayer; the task would NPE every tick") was stale:
`Player.hydrateForNative947` builds and wires a `Prayer` and
`requireNative947TickManagers` refuses admission without one. Re-checked before
lifting the skip:

* `Prayer.processPrayerDrain` returns at once while `hasPrayersOn()` is false,
  and its only emitter (`sendConfigByFile(38915, drain)`) is reached only with a
  prayer active - an unbound id there is a counted drop, not a throw.
* `World.addRestoreSkillsTask` calls `Skills.set`, which now emits a verified
  UPDATE_STAT; before M3 that was a counted NO-OP, which is why the second half
  of the skip reason ("the packets they need are unverified") is also gone.
* Both task bodies already catch `Throwable`.

`Requirement.NATIVE_PRAYER` itself is kept (it is the bootstrap unit tests'
never-satisfied fixture and the right gate for a future initialiser that needs a
manager native admission does not build) with a reason that no longer claims
Prayer is missing. `Native947BootstrapSmoke` now requires the two tasks to be OK
and the queued world-task count to be 6 instead of 4;
`Native947BootstrapTest.productionListIsOrderedAndWellFormed`'s two
`NATIVE_PRAYER` assertions moved into the `Requirement.NONE` group. That is an
intentional behaviour change, not a weakened assertion: the same tests still
prove every gated initialiser is gated and every free one is free.

### `game/com/rs/game/player/client/Native947PlayerBinder.java`

Unchanged behaviour; only the comment that promised "the run button is bound and
pushed in M3" is now accurate. `restore()` stays packet-free by contract -
it runs from the `Native947Session` constructor, before the transport joins the
pipeline - and `Native947Session.ready()` is what pushes the restored state.

### Persistence

`Native947Save` schema 3 already round-trips SKILLS (27 levels + 27 xp) and
VITALS (hitpoints, prayer points, run energy, run flag); no format change was
needed. `Native947PersistenceSmoke` now also changes prayer points, run energy
and the run flag on the world thread, asserts the VITALS section goes dirty,
asserts all of it is on disk before disconnect, and in the fresh-JVM child
asserts the restored Player carries the level, the experience and the three
vitals AND that the login burst emitted at least 27 UPDATE_STAT frames.

---

## 3. Ids this area newly puts on the wire for a native player

Every ID below is CONFIRMED and area A binds it in
`resources/native947/ui-bindings-947.json`. Every WIRE FORM below is confirmed
too, with the single exception noted for varp 679 in section 1 and gap 7.

| Kind | Id | Emitter |
|---|---|---|
| varbit | 1668 | `Player.refreshHitPoints` |
| varbit | 16736 | `Prayer.refreshPrayerPoints` |
| varbit | 41524 | `Skills.refreshSummoningPoints` |
| varbit | 19007 | `Skills.refreshVirtualLeveling` |
| varp | 679 | `CombatDefinitions.refreshSpecialAttackPercentage` |
| varp | 462 | `CombatDefinitions.refreshAutoRelatie` |
| varp | 463 | `Player.sendRunButtonConfig` |
| stat | 0..26 | `Skills.refresh` / `Skills.init` |
| - | - | `Player.setRunEnergy` / `restoreRunEnergy` / `drainRunEnergy` (UPDATE_RUNENERGY) |

Ids this area deliberately stopped emitting for a native player (they were never
bound, so the counted-drop inventory only shrinks): varps 91, 92, 93 (XP
counter), 1115, 1117, 1118+ (skill targets), the bonus-xp varps 3304+, varbits
225+, 229+, 228, 19964, and the interface open of 1213 in slot 1026.

---

## 4. Known gaps

1. **Prayer maximum under a boost.** `Prayer.restorePrayer` caps at
   `getLevelForXp(PRAYER) * 10` (base level) while the client's bar maximum is
   `op_04d8` (current level) x10, so a boosted prayer level shows a bar that
   cannot be filled past the base cap. Pre-existing 910 behaviour, unchanged
   here; it becomes visible only once boosts exist (M9).
2. **Prayer varbit 38915** (`processPrayerDrain`'s drain counter) has no 947
   evidence and stays a counted drop.
3. **Run weight** (UPDATE_RUNWEIGHT 108) is verified and has a writer, but
   `PacketDispatcher.refreshWeight` is area A's tier decision; nothing in this
   area calls it.
4. **The XP counter, XP popups and skill targets are not implemented natively**
   at all (section 2). They need the CANDIDATE slot 1015/1026 evidence settled by
   a live test first.
5. **Skills 27/28** are unsent by design; see section 1 for exactly what the
   client shows.
6. **VARP_LARGE 111 has no Protocol S verdict** and adrenaline (varp 679, up to
   1000) is the one M3 value too wide for the confirmed VARP_SMALL 10, so it is
   the only M3 var that leaves on an unverified opcode. Everything else was moved
   onto confirmed forms (section 1). If 111's layout turns out to be wrong, the
   symptom is the adrenaline bar alone; nothing else regresses. A verdict request
   for 111 (and for 50, which is now unused) belongs in the next protocol round.
7. **`Skills.addXp` cannot run in a cache-free JVM**: its multiplier block calls
   `SkillingPets.rollForPetDrop`, whose class initialiser needs
   `Utils.getItemDefinitionsSize()`. `Native947StatsTest` therefore exercises
   `silentAddXp` (the same award and refresh path without the multipliers) and
   the full `addXp` is covered against the real cache in
   `Native947PersistenceSmoke`. A future slice could give `SkillingPets` a
   cache-absent guard; nothing in M3 needed it.

---

## 5. Evidence

| Run | Result |
|---|---|
| `gradlew --offline cleanTest test installDist writeRuntimeClasspath` (`build/m3-areaB-final.log`) | BUILD SUCCESSFUL, 33 classes, **314 tests, 0 failures, 0 errors** |
| `Native947StatsTest` + `Native947PlayerLifecycleTest` + `Native947SaveStoreTest` + `Native947BootstrapTest`, one JVM | OK (56 tests); also OK in both orders when interleaved with `Native947DispatcherTest` / `Native947BindingsTest` (45 tests each way) |
| `Native947WorldSmoke` (`build/m3-world-smoke.log`) | PASS. Login burst `sendSkillLevel=27, sendConfigByFile=4, sendConfig=3, sendRunEnergy=1`, `dropped={}`, **strictHits=0**, **tickFailures=0**, `processEntityRuns=100` over 100 strict ticks |
| `Native947PersistenceSmoke` (`build/m3-persistence-smoke.log`) | PASS. Level, xp, prayer points, run energy and the run flag round-trip; fresh-JVM child reports `restored login burst: UPDATE_STAT frames=27 strictHits=0 tickFailures=0` |
| `Native947BootstrapSmoke` (`build/m3-bootstrap-smoke.log`) | PASS, `ok=27 skipped=2` (was `ok=25 skipped=4`); 6 queued world tasks |
| `Native947InteractionsSmoke`, `Native947EquipmentSmoke`, `Native947NpcSmoke` | PASS (exit 0) |

The union of `has no 947 binding` lines across all five real-cache smokes is a
strict subset of the `notes/P5-router.md` section 3 inventory - same 22 varps and
same 24 varbits, nothing new - so this area added no unbound emitter.
