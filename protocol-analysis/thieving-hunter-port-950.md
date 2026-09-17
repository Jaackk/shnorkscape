# Ordinary Thieving and butterfly Hunter â€” 950 batch, 2026-09-12

Implemented in the isolated950 project. Original910 and947 projects remain unchanged.

## Supported scope

- **69 exact950 pickpocket NPC definitions** in15 original `PickPocketableNPC` categories: men/women, farmers, male/female H.A.M. members, warriors, rogues, cave goblins, master farmers, guards, Ardougne knights, Menaphite thugs, paladins, gnomes and dwarf traders/heroes. Uses the original `PickPocketAction` with a native per-action journey and the original normal/multiple-loot level thresholds, chance calculation, loot table rows, base XP, two-tick action delay, stun time and stun damage.
- **35 exact950 stall definitions** covering all16 original `Stalls` categories. Uses original level, XP, reward choices/amount ceiling and `1500 * seconds` restoration timing. The old amount roll allowed zero items; the native port guarantees1..ceiling on success. A successful stall temporarily disappears and is restored by the existing `WorldTasksManager`; a competing replacement object prevents its stale restoration.
- **Four butterflies** using the original `FlyingEntityHunter.FlyingEntities`: Ruby harvest5085 (level15, reward10020), Sapphire glacialis5084 (25,10018), Snowy knight5083 (35,10016), Black warlock5082 (45,10014). Original chance formula and ordinary-world XP, equipped regular/magic butterfly net, empty jar, catch/miss, authoritative jar exchange, disappearance and18-second same-species/site respawn. The old impling respawner was intentionally not called because it relocates butterflies to Puro-Puro and invokes legacy-only NPC lifecycle code.
- Regular net10010 and magic net11259 have verified Wield option2; empty jar10012; filled jars have verified Release option1 and return the empty jar without XP. Jar release uses the ordinary release behavior; impling loot tables and the original impling jar-shattering roll are outside this butterfly scope.

## Reused infrastructure and boundaries

`ActionManager` owns timing/cancellation; `Skills.addXp` owns XP/multiplier/UI; `Native950Skilling` owns atomic authoritative inventory changes through existing containers; `WorldTasksManager` owns restoration; existing native NPC/world and save owners carry state. No second scheduler, fake skill level store or client-side rewards.

Target registration/ID/scene/plane, exact cache operation, adjacency and collision route, completed final movement frame, active/alive/unlocked player, no queued teleport/forced movement, unchanged controller and required levels/resources are checked before starting and committing. Walking or another action stops the original action manager. Stalls also check the live world slot and depletion token. Multi-loot inserts all rewards atomically and refuses full bags/overflow/controller veto without partial items or XP. The existing910â†’950 identity audit filters unsafe legacy loot rows instead of assigning repurposed items; the source weights of retained rows are preserved.

Native `Entity.applyHit` is not used: its legacy charge callbacks are absent on native players. Stun damage uses the same direct HP/update/display boundary as native basic combat, with the verified zero-delay melee hit display. **For this batch stuns leave at least1 engine HP**, because a general noncombat death/recovery service is not ported yet. Player animation424 shows the stun; unrelated old graphics/perks/random events/quests/guard AI are not activated by this port. Standalone stall guard aggression, Prifddinas thieving, monkey knife-fighter/phoenix special interactions, implings and placed Hunter traps remain future scope.

## Cache evidence

`thieving-hunter-assets-950.json` and resource `native950/thieving-hunter-assets-950.properties` carry267 exact raw pins:73 NPCs (69 thieving +4 butterflies),35 objects,151 items,8 sequences. The source-cache probe strictly decoded950 menus and rejected transforms and unrelated options. It explicitly excluded repurposed old IDs2268/2269 and2649/2650. Source player sequences24887/5074/5075/5078/881/424/6606 are byte-identical to910;422 retains identical frame/duration data. Appearance/animation frame references have not been visually confirmed in a running client for these new skills.

Read-only cache probe output: `logs/thieving-hunter-cache-probe.log`. Reproducible pin generator: `tools/verify_950_thieving_hunter_assets.py` (reads the paired caches and that strict950 probe output). It writes only950 project resources/evidence.

## Integration APIs

- `Native950Thieving.isPickpocket(NPC, option)` / `startPickpocket(Player, NPC, option)`.
- `Native950Thieving.isStall(WorldObject, option)` / `startStall(Player, WorldObject, option)`.
- `Native950Hunter.isCatchable(NPC, option)` / `start(Player, NPC, option)`.
- `Native950Hunter.itemEntry(id)` admits only verified Wield/Release operations.
- `Native950Hunter.release(Player, slot, itemId, option)`.
- `Native950Thieving.verifyCacheBindings()` or `Native950Hunter.verifyCacheBindings()` verifies the shared raw pins for startup.

Routes must invoke original controller callbacks after stationary arrival. Native Pickpocket slot3 maps to original `processNPCClick2`, just like the already mapped second Fishing operation; Hunter Catch maps to Click1. The shared router retains client visibility/index validation before these APIs.

## Validation

Five focused JUnit regressions passed: original multiple-loot gates, success formula, exact stall options, source cooldown conversion, Hunter net/level chance curve.

`Native950ThievingHunterAcceptance <absolute950-cache-path>` passed in a separate JVM, with no listening socket or save writes. It validates all admitted target counts and actual operation slots, original `PickPocketAction` ownership, success loot+XP, failure/stun/native hit display, full bags/overflow, teleport/removed-target cancellation, stall depletion/restoration and no stale reward, equipped net/jar/level checks, full-backpack1:1 jar exchange, catch/miss, butterfly fresh-entity respawn/release, controller veto and capture of both skills in the normal save snapshot. Log: `logs/thieving-hunter-acceptance-2026-09-12.log`.

The additional `Native950NpcSkillsAcceptance <cache>` passed: five actual encrypted NPC menu paths (Pickpocket, Net, Bait, Harvest, Catch), each walking and running, unpublished-index rejection, final-arrival cancellation, correct910 controller callback/veto, original action ownership, inventory/XP output, scheduled net Wield, butterfly owner-roster/viewport respawn and generation cancellation after world clear. Run recorded311 ticks and1505 parsed encrypted950 frames (`logs/npc-skills-routed-acceptance-2026-09-12.log`). Three additional focused population-scope unit tests cover exact16-type exceptions and invalid regions.

`Native950WorldAcceptance` was updated to keep exact original scoped rows/outcomes while adding independently counted skill source rows, exact original home coordinates,950 pin/menu verification and clear home tiles. It passed with25 populated regions,365 admitted source rows,309 live NPCs and56 explicit refusals (52 original +4 blocked skill tiles), then exact teardown/repopulation, force movement and Cook dialogue regression (`logs/world-skill-population-acceptance-2026-09-12.log`). These remain server/packet acceptance checks, not an on-screen playtest.

## Suggested in-game checks

1. `;;npc 1`, walk off its tile, right-click Pickpocket with a clear backpack. Confirm approach before animation, coins/Thieving XP on success and temporary stun/health loss on failure.
2. `;;obj 635`, walk off its footprint, Steal-from Tea stall. Confirm tea712, XP, depletion then restoration after about10.5seconds (rounded to engine ticks).
3. Hunter level15 or higher: `;;item 10010 1`, `;;item 10012 5`, Wield net, `;;npc 5085`, walk off its tile and Catch. Confirm catch/miss, jar10020, XP and eventual same-site respawn; Release the filled jar.

Existing source modified: only `content/.../thieving/PickPocketAction.java`, backed up under `implementation-backup/2026-09-12-skills-batch/Ataraxia950/content/...`. The rest are new Native950Thieving*/Native950Hunter*/shared asset files, a focused test, an acceptance class and scoped evidence/docs.

## Natural population and world lifecycle follow-up

The original staged9091-row spawn file already contains74 ordinary wisps and92 butterflies, all16 species retaining safe910→950 identity. `-LumbridgeNpcs` formerly admitted only region12850, so none appeared naturally. The existing region spawn pipeline now retains its full-region scope and additionally admits only those16 verified gathering NPC types as their other regions load. Other NPCs outside the configured scope remain excluded. Actual950 collision validated162 usable rows across22 regions:73 wisps and89 butterflies. Four old blocked coordinates are skipped: bright wisp3304,3400; ruby harvest2324,3599 and2323,3600; black warlock2518,2901. No guessed NPC spawn positions were introduced.

Natural entry points verified clear in the current cache: pale wisp18150 at **3125,3215,0** (Divination level1); ruby harvest5085 at **2328,3526,0** (Hunter level15). For a diagnostic Hunter test use `;;nxt level21 15`, then Wield item10010 and carry jar10012; level command is developer-only.

Captured butterflies now leave both `World`/region registries and the `Native950World` owner roster. An owner-scheduled restoration registers a fresh entity at its original home, preserves its original wandering setting, and refuses timers from a cleared world generation. This prevents accumulating dead owner records or resurrecting orphan NPCs after the last logout. Hunter checks owner membership before committing inventory or XP.

Additional production files: `Native950SkillNpcPopulation.java` and the scoped spawn/capture/clear portions of `Native950World.java` (backed up before editing). Additional tests: `Native950SkillNpcPopulationTest.java`, `Native950NpcSkillsAcceptance.java`, and the updated/backed-up `Native950WorldAcceptance.java`.
