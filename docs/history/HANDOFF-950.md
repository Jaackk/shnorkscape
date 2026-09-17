# Current installed build: native ribbon/menu pass, live verification pending (12 September 2026, 14:42)

Engine SHA256 **9422C0D9FEAFF3343F5092DBAACBA44C10CE43D42614005BB5A66F995703E497**. Server26004/client12512 started; verify logs/server.pid.json and client.pid.json plus exact creation timestamps before stopping. Full-content profile and mandatory cache preflight passed. Manual user sign-in requested for final live checks. Read RIBBON-MENUS-950.md for scope and limits.

Implemented:12-icon ribbon; cache-based management navigation28pages/6menus; nativeHeroSummary/Loadout/Skills, quest catalog/overview/journal/sort/filter/bookmarks,58boss information entries and51activities with6filters/sessionfavourites. Core bootstrap now mirrors all actualIF_OPENTOP/OPENSUB/CLOSESUB mounts. Loadout uses existing systemic item/equipment actions and restores displacedHUD1462 with8471 refresh. Summary counts332 eligiblequests from actualserverstate, not native default flags; MoneyPouch and profile-button feedback corrected. Canonicalquestplay, cosmeticsapplication, socialservices, abilities, achievementsandotherbackends remain separate work.

Exit still requires live verification:1477:99 correctly reaches owner1433. Previous806 mount was visually present but static/dynamic clicks passed through to world; hiding808/809 did not fix it. Current candidate restores those siblings, explicitly shows805/806/1433:0, then runs13831(1) after8177. Diagnosticopen/close ownership logs added. Do not claimExitfixed until a1433button packet and nativefull signout are observed. Strong fallback is provenCentralhost735/wrapper732 (512x336), rather than unproven805child guesses. FullLogout202(0payload, smartopcode0x80ca) writer/flushclose/sessionfinalsave is implemented and tested; return-to-lobby remainshidden/unimplemented. Exitmenu must notcalllegacyrealFinish.

Validation:1277tests,0failures/errors,2skipped; actualcacheacceptances passedNavigation2222encryptedframes,Questsall362definitions,Exit44checks including disposablefinalcheckpoint/removal,Beasts246frames/58rows,Minigames1426frames/125categoryselections. Logs/ribbon-final-*.log. PreviousSkillGuide/Forge and21bridgechecks passed earlier inthismilestone; bridgeunchanged bylatestJavachanges.

Live BEFORE this latestdeploy:12ribbonicons visible;HeroSummary renders,Loadoutmodel/equipmenttransactionsworkandHUDrestores;Questoverview/requirements/journal andProgresssortwork;Community/Powers/RuneMetricssurfacesrender;Wardrobe remainsblank(noadapter). Newboss/minigameadapterandfinalSummarycountersnotyetlivechecked. Exitcandidateisstillpending asabove. Noaccountgrants/saveformat changes.

Backup immediatelybeforecurrentdeploy:implementation-backup/ribbon-navigation/before-menu-adapters-20260912-144236 (engine1A5237...,disconnectedprofilesandlogs). Clientclosednormallyandserverloggedplayerremovedbeforebackup. Olderbackups:before-resume-deploy-20260912-143010 andbefore-deploy-20260912-141354. Only950RevTestchanged. Continuefinaltesting,updatecurrenthash/resultsandreportremainingUIbackendlimits honestly.

---

# Latest: native Smithing/Smelting drag fix installed (12 September2026,13:49)

User confirmed the previous equipment/toolbelt/native production milestone basically works, then demonstrated that dragging Smithing cuts the screen down to512x334. Cache path8304 ->8387 ->8390 ->8391 resets small central slot1007, so the former temporary11145 enlargement was not durable.

Native950ForgeUi now uses actual large central1047 (enum7716 ->struct40393): wrapper1477:724, host1477:726. Native800x600 wrapper and filling host contain the fixed800x484 Forge screen. Existing2600/8421 discovers1047 and installs the correct native drag handlers. Removed every Forge11145/8389 override; retained1364, existing gameplay callbacks and guarded deferred initialization. No recurring resize/recenter workaround or client/cache mutation.

Built and installed engine SHA256 6509088276434cae3e504576c867a05775002d6b4b8babe65828b8fd076749b4. Source/build/runtime agree. Full tests1264passed/2skipped/zero failures;46 paired-cache drag checks;382 enforced Forge pins;1038 encrypted Forge acceptance frames covering all702Smithing/12Smelting paths and small Make-X isolation/stale-close/shortbow completion. Bridge19checks and mandatory preflight passed. Logs:forge-drag-build.log,forge-drag-acceptance.log,forge-drag-bridge.log,cache-preflight.log. See protocol-analysis/forge-drag-validation-950.json and forge-drag-950.md.

Previous client was closed; server confirmed character removal before stop. Backup of prior runtime/bridge/disconnected profiles/logs: C:\Users\developer\Desktop\950RevTest\implementation-backup\forge-window-drag\before-deploy-20260912-134757. New server25868 and Vulkan client10804 launched; fresh process records/creation times are authoritative. User signed in and confirmed: "dragging works now without breaking it". Read-only live capture also showed the complete Smelting screen. The updated server/client remain running. User dragged the pre-fix window, not automated input. Current cache dimensions/position policy supersede the earlier optional19986 centering proposal below.

---

# Current installed build: final live UI corrections deployed (12 September 2026,13:32)

User clarified the crashed application was ChatGPT, not the game. At the requested relaunch both950 processes were absent and their ports were free. No stale PID was terminated. Installed the final built engine A66001AE285D7A51197CFABD834EE1980286E3E758200CF897DAF1F5AD200E82, rebuilt the bridge, verified19checks and mandatory cache preflight, and launched full-content server14032/client22684. Current logs/process records and creation times are authoritative.

Now installed: native Make1370:30 pause mask1/DialogueClick101; accurate basic-tool descriptions and hidden whole Settings icon; generation-guarded deferred forge initialization and actual station8334. Current cache-backed tests passed: Toolbelt58checks, Make102ticks/597frames, Forge952frames/all702Smithing+12Smelting paths, ProductionThirdPass613ticks/1285frames, Invention407ticks/769frames. Full test/jar build previously passed before this deploy. Source and deployed JAR hashes match. See logs/toolbelt-relaunch-*.log.

Backup of previous runtime, bridge, exact disconnected profiles and logs:implementation-backup/toolbelt-production-ui/before-relaunch-20260912-133138. No account edits or grants. User will now test manually; no post-deployment authentication or rendered Make completion is claimed. The optional forge-centering CS19986 adjustment described below remains unapplied. Metal bank and special station/backend restrictions retain documented limits.

---

# Latest status: live UI corrections ready; user stopped Computer Use

User pressed physical Escape during the final pre-restart observation. Computer Use stopped; no further desktop input or process restart was performed. Server10376/client26852 remain on installed engine68399620347bb3f02dc3aa80604203515d4d45401e8ad404f93a027b3c5c2799; verify fresh process records before any stop. Character is beside actual Lumbridge anvil113258 at3228,3254, with one ordinary log still in backpack. No test items were granted or recipes completed.

Live confirmed: toolbelt footer4353 opens1944; tool descriptions and close work. Generic Fletching Make-X renders correctly, but Make/Space were inert. Root cause was overwritten pause mask:1370:30 must have mask1 and submit DialogueClick opcode101, not IF_BUTTON. Corrected source and115pins/real101 encrypted acceptance passed102ticks/597frames. Toolbelt follow-up corrects free-basic descriptions and hides Settings parent1944:12, not just hitbox17;58checks passed.

Actual furnace113261 and anvil113258 both open native37, product selection and Begin Project IF37:163 work (missing-material reply confirmed). First open/switch showed stale or blank grids/details; new source adds a generation/station/position/ownership-guarded next-tick redraw. It now sends actual station ID in varp8334 so current client requirement7164 can inspect objectparam7803.361pins/952encryptedframes passed in isolation. All3 Interactions forgeUi.open calls pass actual WorldObject. Recipe-specific special burial/barbarian/dragonkin station restrictions are not fully enforced by existing backend; this UI pass does not grant flags.

Final shared Build-Ataraxia950.ps1 test/jar/writeRuntimeClasspath completed successfully after the stop; see logs/build-ataraxia950.log. These latest live corrections have NOT been deployed. Additional read-only finding: to recenter the resized forge wrapper, CS19986(hash1477:732,0,0) reapplies centered x/y after the layout scripts; SHA12/19986/0 cb51e2c7cee34484b9aea0b1340bb259ba68192e36ea6d1c96058a9eb554560c. This centering adjustment is not yet applied.

Next: run current Make/Forge/Toolbelt and migrated production fixtures; safely disconnect/save and back up latest runtime+profiles; deploy new engine, rebuild bridge, preflight, restart; manual sign-in and real Make batch/forge initial layout recheck. Do not claim production completion from the preliminary IF-button-only tests. Existing original backup:implementation-backup/toolbelt-production-ui/before-deploy-20260912-125009. Prior final reports below refer to the installed preliminary build.

---

# Latest milestone: equipment movement, toolbelt and native production UI (12 September 2026)

Implemented, tested and installed in950RevTest. Read TOOLBELT-PRODUCTION-950.md for exact in-game checks and limits. Built/deployed engine SHA256:68399620347bb3f02dc3aa80604203515d4d45401e8ad404f93a027b3c5c2799. Server10376 and Vulkan client26852 started; current process records and creation times remain authoritative. Manual sign-in requested; rendered UI verification is pending.

Gear equip/removal no longer cancels walking/running routes or combat targeting. Native surface toolbelt1944 provides35 ordinary free basic tools, supported level-checked upgrades and safe removal, and supplies tools to gathering/production. Equipment1462:35 toolbelt footer uses packed wire slot4353 (actor2/child1), not ordinal2. New modal ownership explicitly retires belt before Hero/Skills; no generic close callback may close a newer owner.

Generic native Make-X1370/1371 uses current pinned product categories, cache aliases, material-aware callback mapping, quantity controls, and the armed root1477:896 category dropdown. Modern Smithing/Smelting uses separate interface37 and DBtable7 with material/product grids, cache upgrade chains, quantity and Begin Project. All702 smithing/12 smelting recipes have real native paths. Existing actions retain inventory/XP/requirements/station checks. Metal-bank storage remains unimplemented: use backpack materials. Unmapped/mixed recipe contexts retain semantic dialogue selection; special belt unlock consumers and tools lacking native action profiles remain unavailable.

Outer character schema4 unchanged; nested SKILL_PROGRESS writes4 and reads1/2/3/4. It appends bounded sorted tool IDs while preserving prior Invention research, Divination boons, familiars and every old section. Existing profiles seed only35 basic tools. A disposable-copy migration of the real x profile passed without changing its original bytes. User authorized persistent skill progress and separately requested the toolbelt. Rollback requires the previous engine and matching saved profiles together.

Final combined suite:1264passed,2skipped,1266discovered,zero failures/errors. Actual-cache acceptances:equipment227frames; toolbelt55checks including exact footer and modal handoff; Make-X102ticks/597frames; forge740frames and all702+12paths; bank80actions/62ticks/1448frames; melee449ticks/1531frames. Provider probes additionally passed production613ticks/1271frames and Invention407ticks/769frames. Mandatory expanded cache preflight and19bridge checks passed. These tests use disposable state and do not establish rendered layouts. Detailed record:protocol-analysis/toolbelt-production-validation-950.json.

Backup of the prior working engine, bridge, disconnected profiles and logs:implementation-backup/toolbelt-production-ui/before-deploy-20260912-125009. Source originals are underimplementation-backup/toolbelt-production-ui. Original910/947 and950OpenSource were not edited. Reports:protocol-analysis/toolbelt-950.md, native950-production-ui.md, native950-forge-ui.md, toolbelt-production-providers-950.md. Full-content startup policy remains unchanged.

---

# Latest milestone: third skill refinement pass (12 September 2026)

Implemented and deployed; main guide:SKILLS-THIRD-PASS-950.md. User confirmed the prior lobby cleanup looks great; it remains installed and its bootstrap regression passes.

New work:11 saved Divination boons/23 energy alternatives, mixed backpack/store Archaeology restoration and withdrawals,9 saved Invention junk-reduction research tiers,85 owned familiar lifecycles and saved remaining time, shared cancellation/reach/controller safeguards,33 generic production recipes,6 Crafting recipes,3 baked foods, interchangeable nails/workbench tiers, modern smelting XP/timing, ordinary thrown weapons with final-shot XP correctness. Familiar combat/scrolls/BoB and most passives, full Invention discovery/augmentation/perks, new Archaeology digsites and exact cake/pizza burn curves remain future work. Read the four detailed skills-thirdpass reports.

User explicitly approved persistent skill progress after automatic review requested it. Outer save schema4 is unchanged; nested SKILL_PROGRESS now writes version3 and reads1/2/3. It appends11 bytes (research byte,12 boon bits, familiar pouch/ticks). Bounds/cloning retained; existing excavation/material/plot identities unchanged. An old engine cannot read the new nested3 section: rollback must pair old engine and saved-profile backup. Normal checkpoint captures familiar state before entity removal; offline/restricted-area lifetimes pause.

Final verification:1,260passed/2skipped/1262discovered/zero failures or errors;10 cache-backed acceptance programs;19 bridge checks; actual lobby bootstrap; mandatory cache preflight; real-profile migration to a disposable copy preserving all existing sections and original bytes. Encrypted Invention/Weave/Summon/research407ticks/700frames, production613ticks/956frames; familiar367checks, combat69checks, bank80actions/1379frames and melee449ticks/1531frames. Exact logs in protocol-analysis/skills-thirdpass-validation.json. No authentication or rendered gameplay claim.

Engine SHA256:e78f5a172c93ba4b871ea6baf6b44c11b089ffc2213c9de98481969005cddda5 (built/deployed identical). ServerPID24172, Vulkan clientPID14368; current logs/process records authoritative. Full-content launch profile retained; no feature switches required. Backups:C:\Users\developer\Desktop\950RevTest\implementation-backup\skills-thirdpass\before-deploy-20260912-121821 (working engine/bridge, disconnected character and logs), plus project-relative original sources under implementation-backup/skills-thirdpass. Original910/947 and950OpenSource not edited.

---

# Latest change: lobby cleanup (12 September 2026)

Installed950-only lobby presentation fix in LobbyPlayer.kt; see protocol-analysis/lobby-cleanup-950.md. Removed old814 account summary from906:37 (the950 tab underlay), which caused the overlapping PLAYER NAME/currency/membership row. Populates native906:118/119 with account name/local status, hides store/promotion roots120/139, mounts world list910 at906:45, and selects World with3059(1). Lists only actual localWorld1 with live player count. Existing authentication/world-entry flow and947 branch unchanged; social/news pages remain outside scope.

Kotlin build,19 bridge checks, actual-cache seven-packet lobby bootstrap and mandatory cache preflight passed. Engine and client binaries unchanged. ServerPID12164 and Vulkan clientPID16152 started; process records authoritative. Client visually confirmed at sign-in screen. User post-login layout/Play Now check pending. Source and disconnected character/log backups are under implementation-backup/lobby-cleanup-950. Do not replace the skill-refinement engine for this UI-only rollback.

---
# Latest milestone: skill refinement pass (12 September 2026)

Implemented and deployed in950RevTest. Main guide: SKILLS-SECOND-PASS-950.md. Three detailed skill audits and the runtime manifest are under protocol-analysis/skills-secondpass-*.md/json. Original AstraNXT, Ataraxia-PS and950OpenSource were not modified by this milestone.

Changes: native Invention pouch click/drop + anywhere/note disassembly, corrected drag bounds, Analyse, capped materials; Farming harvest-life/compost progression; Archaeology mattock precision/gauge; Mining level15 stamina scaling/capacity; repeat pickpocketing; collision-safe woodcutting and Agility cancellation; richer shared recipes and bounded Make-all; eight stringing recipes; normal altar offering/recharge; modern air-spell costs and ammo damage limits; Slayer points/cancel/XP rewards; custom three-wave solo Dungeoneering challenge. Existing XP multipliers and ordinary save schema are preserved. Full live-RuneScape feature parity is not claimed; read the audit limits.

Final tests: 1247 passed, 2 skipped, zero failures/errors. Invention actual-cache encrypted routing:271 ticks/445 frames, explicitly checks emitted VARP5987=128, noted1514 disassembly, stale item identity, forged claims, cancellation and locked obstacle preservation. Production integrated routing:317 ticks/490 frames. Agents additionally passed current-cache gathering, crafting/summoning, combat, Slayer and dungeon acceptances documented in their reports. All19 bridge vectors and full cache preflight passed.

Deployed engine SHA256:46ab8a0309cfb0d61ce217a3546cb59e2ce89fc8e48da8618b48fd9f8c4dda00. Server PID10736 created2026-09-12T16:36:07.1104040Z; confirm fresh metadata before any future stop. All-content launch profile retained. Source + previous saves/runtime/bridge backups: implementation-backup/skills-secondpass (before-deploy for saved runtime). No account was used by acceptance tools; existing player profiles were backed up without editing them.

Invention UI evidence: backpack1473:9 uses enum5134/type2 -> enum5136/row0 -> DBrow1012. Its visibility is varbit30224 (parent5987 bit7), which requires BOTH the varbit and parent varp in ui-bindings-950.json because VarsManager emits whole-varp writes. Native950InventionUi pins the icon root, row, varbit and relevant scripts. Script16557 mode0 selects child1 (background) for16563 operations; child2 is decorative. Native creation0x140269e4c stores packed index at component+0x1c and network sender emits it: row0/child1 => slot1, item-1. Accept ONLY1473:9/slot1 for pouch. Do not revert to guessed slot0 or icon slot2. Inventory drag parent depth3 reaches backpack root (item->1473:5->1473:2->1473:0), mask0x6d37fe. Java/Kotlin bootstrap and both packet-vector tests agree. Pouch drop is not the intra-grid3902 prediction swap; retain the exact original source claim. Drop opens an ordinary menu and quantity confirmation before consumption. Existing banker slot guard and duplicate-Search fixes are unchanged.

Computer Use was stopped by the user's physical Escape key during the pre-deployment inspection. No subsequent desktop input or authentication was performed. Only the server was restarted; client was not relaunched. Final visible pouch/drag check is pending: reconnect, meet base80 Crafting/Smithing/Divination, find the lightbulb below backpack, drag an ordinary item onto it, choose Disassemble then quantity. The headless checks prove emitted state and routed transactions, not pixels/gesture hit-testing. If the icon is absent first confirm the three BASE skill levels. The original public workbench also remains available.

---

# 950 revision port - current handoff

## September 12: all29 skills have first-pass training routes - CURRENT INSTALLED BUILD

User requested a basic working pass for every skill in950RevTest, reusing the original910 infrastructure and refining later. Added eight missing training baselines: Farming, Construction, Dungeoneering, Invention, Archaeology, Ranged, Magic and Necromancy. Original910 lacked Archaeology/Necromancy, so those are explicit new baselines. Full retail content and naturally obtainable supplies are not complete. The current coverage, normal locations, requirements and limits are in [SKILLS-FIRST-PASS-950.md](SKILLS-FIRST-PASS-950.md).

- Farming:36 crops/19 logical patch definitions; rake, compost, plant, original offline growth timings, inspect/harvest/clear. Scene tiles sharing a patch share one saved record. Corrected actual950 object opcodes207/208 for wide varbit/morph references. Bound19 crop varbits and five parent variables; publish initial zero states, preserve unrelated bits and send only changed packed parents. Healthy deterministic growth only; other patch families/disease remain later work.
- Construction:45 actual950 flatpacks from current cache materials/levels/productIDs and original HouseConstants XP. Normal Rimmington bench2941,3229; ordinary approach, tools, menus, quantities, atomic materials/output and cancellation. Full player-owned houses remain later work.
- Invention:10,844 identity-matched original disassembly rows, current component names/indices (Junk82, not old75), eight manufacturing recipes, level80 Crafting/Smithing/Divination gate, saved components. Public Falador bench2967,3407. Augmentation/perks/device effects and discoveries remain separate work.
- Archaeology:Centurion3363,3393 and Venator3368,3393; natural Restore/Store bench3355,3393; mattock49539, level1/5 excavation, three current-cache restorations, saved material storage and partial excavation counters. Actual region loading installs all three new starter objects once with clear footprints. Full digsites/relics/collections/mysteries remain later work.
- Dungeoneering:repeatable solo frozen chambers via ring15707, tutor9712 or original entrance; three guardians, exactly-once XP/tokens, saved tokens/completions, abort/death/logout/crash recovery. All16 room chunks resolve current map area115. Four concurrent reserved rooms; no procedural dungeon, parties or token shop. The original tutor tile is blocked and remains subject to the existing population policy; entrance/ring routes are verified.
- Ranged/Magic/Necromancy:shared original melee scheduler and reward path now supports cache-derived weapon profiles, current animations, range/line-of-sight, real arrow/bolt and rune costs, resource exhaustion and appropriate skill XP. Air Strike is the initial automatic magic spell; basic Death guard auto-attacks train Necromancy. Classic armour defence is preserved when switching styles; newer armour uses the original tier curve. Thrown/powered weapons, selectable spellbooks, rituals/conjures, projectiles, style-specific hit icons and fuller balance remain later work.

Save format is now schema4, adding one bounded SKILL_PROGRESS section to the existing atomic character file; nested section version2 includes Archaeology partial excavation counters. Older schema1/2/3 and nested version1 remain readable. Original schema3 real-profile read-only migration to a temporary schema4 file preserved every section, inventory/bank/equipment slot and all XP/levels; original bytes unchanged. Runtime rollback to the previous binary requires its matching saved-profile backup after a new save has been written. No items or levels were granted manually. Session cleanup runs even if final persistence fails.

Validation: **1222 unit tests passed,2 existing skips,1224 discovered,zero failures/errors;19 bridge checks**. Actual-cache Farming/Construction3958 checks/3002ticks; encrypted Farming325frames includes all19 crop variable slices; Invention/Archaeology434ticks/512frames and normal routing217ticks/337frames; all3 starter placements/idempotence; Dungeoneering normal routing85ticks/187frames with production deaths and exactly-once rewards; combat styles19 cache/profile/cost/XP checks. Existing melee449ticks/1531frames, bank80actions/1377frames and UI37actions/473frames passed. The world acceptance's stale162 gathering count was replaced by an independent37-species audit:179 outside-scope skill spawns, with full scoped327spawns/393rows and exact66refusals. Normal starter-region announcements, teardown/reload, movement, NPC frames, Cook dialogue and scheduler passed. Final production edits passed the whole suite; the subsequent rebuild changed only acceptance fixtures and documentation comments.

Engine SHA256 `142bf9a613595f3b693ec8b097bee9f77c95a416b5a1ddb3131fad92f7fff899` matches built/deployed jars. Kotlin bridge rebuilt after deployment. Mandatory startup preflight passed with48 variable and138 script bindings. ServerPID25220, Vulkan clientPID26688; all local ports bound and950.1 client cache handshake accepted. Process records/logs remain authoritative. New client rendering/gameplay needs manual playtesting; no login, authentication or pixel-level acceptance is claimed. All-content launch policy remains unchanged.

Backups: source at `implementation-backup/skills-firstpass`; previous working engine/bridge and exact pre-deployment profiles at `implementation-backup/skills-firstpass/before-deploy`. Original910/947 and950OpenSource were not edited. [Validation record](protocol-analysis/validation-skills-firstpass-950.json), [Farming/Construction](Ataraxia950/Native950FarmingConstruction-README.md), [Invention/Archaeology](Native950InventionArchaeology.md), [Dungeoneering](Ataraxia950/notes/Native950Dungeoneering.md).


## September12: full content on every launch - CURRENT LAUNCH POLICY

User requested removal of per-feature launch arguments. Both Start-950Test.ps1 and Start-950Server.ps1 now use one full Ataraxia950 profile with no arguments: walking/ribbon/settings/regions/collision, all source NPC regions, local dev commands and world-map support. Backend, isolatedmodern950save path, quoted950data path and mandatory cache preflight/verification are unconditional. Old feature switches are accepted only for shortcut compatibility and cannot restrict gameplay or disable verification. Vulkan default/OpenGL hardware selection remains unchanged. Start-950Test.cmd passes no required feature arguments. Reusing an old restricted running server fails explicitly rather than silently serving partial content.

No engine/cache/player data change or rebuild. Current running server already has all content enabled, so no interruption is required. Previous launchers/handoff: implementation-backup/2026-09-12-full-launch-defaults. See LAUNCH-950.md. This supersedes the diagnostic Lumbridge restriction and feature switches described in earlier entries below.

## September12: full-world NPC population - CURRENT LAUNCH CONFIGURATION

User requested efficient activation of all910NPCspawns and explicitly accepts odd/blocked positions. Read-only multiset audit proves all9,091 original rows already exist in950; the sole additional row is development Turael. Original910 and947 files are byte-identical (SHA2565fc87bdc94a8d385e608cedda22228155b50261a1d26835dabc3adb1aa53d801). Current roster:9,092rows,467regions,2,395NPCIDs. No source data replacement or engine rebuild was necessary.

Removed normal launch's Lumbridge-only restriction: Ataraxia defaults to all source regions (unless Minimal or explicit diagnostic LumbridgeNpcs). Added AllNpcs switch to both PowerShell launchers. Start-950Test.cmd now launches Ataraxia/Collision/Vulkan/AllNpcs/DevTools. NPCs load as their map regions are visited; original blocked positions are accepted by the existing whole-region loader. Current-cache identity/decoder guards remain:7,707compatible rows;1,385rows excluded (missing/renamed/repurposed/unverifiable). This is the full existing source roster, not a claim of all retail-world spawns or all NPC interactions.

Validation: PowerShell syntax checks; actual-cache tools/SpawnPopulation950.java audit; four regions12850/12853/11829/11062 produced94/55/20/19NPCs,188total from233rows,46blocked placements retained,45identity refusals,zero decoding/spawn failures,duplicate region calls prevented. Log:logs/all-world-spawns-probe.log. Whole engine unchanged from skills build522add6a1cb8c2327818faf2de5d4aef3d69736888940ab80694f74d8d11d38e; prior1,185unitpasses remain applicable. Startup cache preflight passed.

Server PID28448/client PID19844; current flags -Ataraxia -Collision -Vulkan -AllNpcs -DevTools. Prior launcher/handoff and latest normal-disconnect saves/logs backed up at implementation-backup/2026-09-12-world-spawns. Original947/910 remain read-only. Source edits for position cleanup belong in Ataraxia950/data/npcs/spawns.json. Client restarted for user to explore; no live traversal claim.

## September 12: Smithing, Slayer, impling Hunter and Agility - CURRENT INSTALLED BUILD

User asked for as many more skills as possible in one pass. Implemented and installed in isolated950, reusing original910 action/data/XP/save infrastructure and actual950 cache semantics. [Playtest guide](SKILL-PORTS-950.md) lists commands, materials and clear limits.

- Smithing:702 current-cache anvil recipes across10 original metal families, including upgrades/ammo/tools/ore boxes; actual current product IDs and requirements, heat/progress gauges5/7. Object Smith and selected bars/upgrade inputs/hammer Use-on-anvil share guarded approach/menu/Make1/5/10/All. Current Bronze dagger1205 takes2bars; Rune platebody is45543. Materials commit only at completion; cancelled progress is discarded and ingredients preserved. Metal bank, rich anvil UI, reheating and persistent unfinished projects remain later work.
- Slayer:8 original masters, weighted assignments restricted to populated verified native combat families, requirements at assignment/attack/reward, damage-owner kill XP, remaining count, completion/streak/points. Six bounded keys in existing SETTINGS preserve progress through saves; no schema expansion. Master menus use actual spaced/hyphen labels. Added Turael8461 at3219,3258,0 near development banker. Special encounters, unlock-dependent families, shop/block/skip systems remain excluded. Master route uses shared published NPC/controller/stationary approach and native1186/1188 chatbox; no new portrait. `;;nxt slayer` moves beside him.
- Hunter:all12 original implings, net/jar/level/catch/miss/XP and100tick owner-managed respawn. Filled jars use actual inventory Loot3; original weighted loot, returned jar/Spirit charm and nonlethal break effect form one atomic exchange. Shared canExchangeSlot/exchangeSlot validates exact source object/ID/quantity across callbacks; Loot and butterfly Release now consume clicked slot, preserving duplicate-slot protection. No ordinary inventory/bank safety was relaxed. Traps, barehanded/Puro-Puro rules and full natural population remain later work.
- Agility:complete basic Barbarian30 and Wilderness52 courses with19 obstacle directions/variants, actual collision endpoints/plane changes and ordered bonuses. Existing Gnome course remains. `;;nxt barbarian` and `;;nxt wilderness` move outside entrances. Advanced/other courses and incidental reward hooks remain later work.

Final validation: **1185 passed, 2 existing skips, 1187 discovered, zero failures/errors; 19 launcher checks**. Packaged actual-cache probes all passed: Smithing702 atomic exchanges/2496ticks/3212encryptedframes/1694gauges; Slayer73ticks/38encryptedframes/2statupdates plus actual save restore and respawn deduplication; Hunter12captures/327loot paths plus exact duplicate-jar Loot/Release; expanded NPC movement636ticks/2631frames; encrypted production and all12Loot3 routes728ticks/1044frames including distant Anvil Smith and bars Use-on-anvil; Agility26clicks/702frames. Existing Bank80actions/1377frames, UI37actions/473frames and Melee449ticks/1531frames passed. Natural candidate population193rows across40regions ->180clear spawns/13blocked skips/28species; the three original Kingly positions remain blocked and skipped. Startup preflight138scripts/24variables plus new skills passed.

Engine SHA256 `522add6a1cb8c2327818faf2de5d4aef3d69736888940ab80694f74d8d11d38e`. Server PID29192, Vulkan client PID28256; PID records and executable/creation identities verified. Normal `-Ataraxia -Collision -Vulkan -LumbridgeNpcs -DevTools` flags, ports8950/43650. Prior runtime/bridge/handoff and latest saves/logs backed up at `implementation-backup/2026-09-12-more-skills/before-deploy`; exact-slot source backup at `implementation-backup/2026-09-12-exact-slot-jars`. Build/deployed hashes match. Original947/910 projects and user items/levels were not manually edited.

**Live state:** existing x profile has restored at3217,3258,0 with bootstrapOk=true, no initial tick/scheduler failures and no withheld appearance. **User confirmed Turael Get task issued an assignment** after `;;nxt slayer` ("yes i got the assignment"). Smithing/Hunter/Agility manual rendering/gameplay tests and Slayer visual XP/relog checks remain pending. No Computer Use was performed. Do not claim complete skill parity or completed live rendering tests. [Validation](protocol-analysis/validation-more-skills-2026-09-12.json), [Smithing](protocol-analysis/smithing-port-950.md), [Slayer](protocol-analysis/slayer-950.md), [Hunter](protocol-analysis/impling-port-950.md), [Agility](protocol-analysis/agility-expansion-950.md).

## September 12: bank slot lifecycle repair - previous accepted build

User reported repeated "The item in that slot has changed" while banking. Read-only save/log comparison established that the received ID was the next authoritative row: slot6 source211 sent215; slot7 source215 sent24000; slot11 source20000 sent62789. All were quantity1, notesfalse, default1, savedX2. The prior duplicate-Search repair was incomplete: it retained one bank517 at1477:695, but actual cache10906 only considers the bank open at struct21308 param3503 =1477:693.

With sole695, cache6961 immediately compacts through14354/14358, mutating the retained clicked actor into the next row before the button sender reads it. Cache9316 also skips its bank redraw/refresh branch when10906 is false. This explains persistent refusals after reopen without server item reordering. A new bounded interpreter runs those actual cache instructions and reproduces all three reported pairs under695;693 retains exhausted48447 and enables refresh. The previous encrypted test incorrectly asserted695 rather than checking the cache's open predicate; do not restore that assumption.

Fixed: exactly one bank mount at693, consistent packet-free native InterfaceManager ownership/queries/remove, matching Java recipe and Kotlin3494-based selection. The duplicate Search fix remains one widget tree. Also bound varp8970 and publish-1 (no holes within occupied span) before8971 and fullcontainer95. Strict source/predicted actor checks remain; no arbitrary next-ID admission. Added detailed rejected-claim logs and exact reported-row/encrypted-publication regressions. No account edits; probes use disposable players, and latest saves/logs were backed up after normal disconnect.

Validation: **1149 passed, 2 existing skips, 1151 discovered, zero failures/errors; 19 launcher checks**. Packaged bank:80 encrypted actions/62 ticks/1377 frames; UI:37 actions/473 frames; equipment:136 frames. Both actual-cache bank audit tools pass;136 dedicated bank pins; startup138 scripts/24 variables. Engine and deployed hashes/process identities verified. No full catalog rerun was needed for this UI lifecycle correction; earlier bulk transfer/catalog results remain historical evidence.

Engine SHA256 `84970e322f673e889d6a4a2fa1fa5e52eeac7908c5fd1106ed7ea0eab49d3aac`. Server PID 18944, Vulkan client PID 15024; normal -Ataraxia -Collision -Vulkan -LumbridgeNpcs -DevTools flags. Backup:`implementation-backup/2026-09-12-bank-slot-lifecycle/before-deploy`. [Deep slot audit](protocol-analysis/bank-slot-lifecycle-950.md), [actual-cache branch evidence](protocol-analysis/bank-mount-950-evidence.json), [validation](protocol-analysis/validation-bank-slot-lifecycle-2026-09-12.json).

**Live acceptance:** user answered "it seems to work properly now" to withdrawing several different single-item stacks (including a grimy herb), depositing them again, and checking Search normal typing. Fresh live log shows successful exhausted-row48447 claims and partial original-ID withdrawals plus Deposit inventory39. Current task used no Computer Use and made no manual account edits. Final log review counted 29 successful item transfers and 2 residual stale-claim refusals: claimed436/current24000 after an earlier row disappeared (changedThisTick=false), then cleared48447 on a shifted slot with changedThisTick=true. Retries succeeded. Do not claim every rapid-click case is solved or zero bank refusals; retain strict guards until a versioned bank-view/history design can disambiguate queued claims.

Remaining structural bank work: tabs/reorder, placeholders, atomic presets, direct bank-to-equipment/familiar/specialized storage, attributed item states, and capacity display700 versus enforced600. The detailed audit explains fixed backpack indices versus compact bank indices, Search original-index mapping, optimistic actor sentinels, same-tick guards, scoped X requests and sequential Deposit inventory behavior.

## September 12: bank controls and first Search fix - SUPERSEDED BY SLOT REPAIR

Restored quantity 1/5/10/All/X, Change-X, saved-X, Deposit-X, cache-driven note withdrawals, bulk equipment deposits, native Search routing, and persisted quantity/note preferences. Native Bank ownership queries now read the active bank instead of the empty legacy multi-bank list. Current-cache metadata drives transfers and requirements; no item-specific exceptions were added.

Live checks passed quantity 5, five-log note withdrawal/deposit, custom X=2, Escape input cancellation, and depositing/withdrawing/re-equipping yellow partyhat1040 and Masterwork staff58486. All test item totals were restored. The user then confirmed Search doubled letters. The cause was two bank517 mounts: legacy MainInterfaceComponents selected wrapper1477:693 (struct21308 param3494 nonzero), while the native recipe mounted again at host1477:695. Both copies ran the same shared-text key callback. InterfaceManager now maintains bank bookkeeping at the current-cache host and leaves all mounting/closing to the native recipe. A fresh client has cleared the old ghost. Also fixed the misleading error from CLOSE_MODAL followed by Close317; repeated notifications now remain silent.

Validation: **1149 passed, 2 existing skips (1151 discovered), zero failures/errors; 18 launcher checks**. Final packaged bank acceptance: 64 encrypted actions, 50 ticks, 1042 frames. It proves one bank mount/close at695, none at693, consistent ownership queries and item conservation. A negative check with the previous InterfaceManager reproduces exactly two mounts. General UI: 37 encrypted actions/473 frames. Earlier core build also passed 51,419 real bank round trips, 9,871 note withdrawal links, equipment136 frames and appearance54 bodies. Cache audit: 341 components/23 hooks/60 scripts; startup: 138 script bindings/23 variables; 126 dedicated bank pins.

Installed engine SHA256 `d30e0f03467f6e3ad32a8506c463dc6e76ed5f7764f92bb29d8367649dda976e`. Server PID 12624, Vulkan client PID 12440; normal -Ataraxia -Collision -Vulkan -LumbridgeNpcs -DevTools flags. Hashes and process identities verified. Original pre-milestone backup: `implementation-backup/2026-09-12-bank-ui/before-deploy`; immediate pre-cleanup engine/bridge/latest saves/logs: `implementation-backup/2026-09-12-bank-ui/before-search-cleanup`. Original947/910 projects and account contents were not manually edited.

**Live acceptance complete:** after the restart, the user answered "it works now" to the combined Search single-letter typing and bank close/reopen check. The reported doubled-character symptom is resolved. Computer Use had been stopped by physical Escape, so the final retest was performed manually by the user. Short-window layout remains unchecked.

**Still unported:** tabs/reordering, placeholders, presets, direct bank-to-worn withdrawals, familiar/pouch/specialized storage, PIN workflow, per-item attributes. Capacity display says700 while actual storage enforces600. These require a bounded bank state/layout extension and atomic preset transactions. [Implementation](protocol-analysis/bank-ui-950.md), [cache audit](protocol-analysis/bank-ui-cache-950.md), [save-model audit](protocol-analysis/bank-persistence-feature-audit-950.md), [validation](protocol-analysis/validation-bank-ui-2026-09-12.json).

## September 12: generic cache equipment and banking - PREVIOUS ACCEPTED BUILD

The user requested systemic fixes, specifically Masterwork staff58486, rather than additional item-specific exceptions. Native Wear/Wield/Equip now resolves every candidate from the selected950 cache: slot, secondary conflicts, actual menu labels, model assets, normal/combat BAS and skill requirements. Native950EquipmentActions stages an atomic inventory/equipment exchange, supports stackable ammo and symmetric two-handed conflicts, checks unboosted XP including virtual120 requirements, and retains controller/stale/space/overflow/lock guards. Direct Remove and displaced gear respect cache2091. Current appearance uses the item's actual cache models/offsets/scale and validated BAS; no per-item whitelist or hash grants equipment capability. Masterwork staff requires Magic99, occupies3+5, and uses current combat-profile BAS2697/2689. Other equipment styles/effects and full modern combat bonuses remain separate ports.

Banking now dispatches into Native950Banking using the same original Player containers but current-cache identity, stackability, note-template links and bank restrictions. Removed the old910 custom item-ID admission rules; normal deposits unnote using cache links, normal withdrawals target the backpack. Existing bank actor prediction/Withdraw-X guards remain. CacheItems now resolves valid lent/bound/shard forms; no template family is blanket-denied. Saved equipment stacks and native u24 item bounds are consistent. Special per-instance charges/attributes/Invention state still refuse explicitly; disabled bank presets/extra tabs/BoB/note-withdraw/wear modes are not claimed ported.

Validation: **1124 passed,2 existing skips (1126 discovered), zero failures/errors;18 launcher checks**. Exhaustive cache audit:63,414 definitions, zero unresolved templates, **16,596 named wearable definitions validated**,59 internal/unnamed wearable rows excluded. Real Bank entrypoints: **51,419 bank round trips**,6,411 authored restrictions respected. Final packaged-jar acceptances: equipment136encryptedframes; live appearance54bodies; bank30actions/35ticks/479frames; melee449ticks/1531frames; rewards117frames; earlier partyhat regression136frames. Startup preflight passed94script bindings and17variables. Tests use ephemeral players; user saves/items/levels and947/910 references were not manually edited.

Current engine SHA256: `de3f96c1e7b1772fe74da6c29cf81cfb29f1e1c2fd494b759acb38c56daebcfd`. Server PID28976, Vulkan client PID12640, usual -Ataraxia -Collision -Vulkan -LumbridgeNpcs -DevTools flags. Built/deployed hashes and PID identities match. Backup: `implementation-backup/2026-09-12-systemic-equipment/before-deploy`. The restored real profile attached after restart with bootstrapOk=true, zero tick failures and no withheld appearance. [Implementation/evidence](protocol-analysis/systemic-equipment-950.md), [metadata](protocol-analysis/equipment-types-950.md), [validation](protocol-analysis/validation-systemic-equipment-2026-09-12.json).

**Live acceptance, September12:** user answered "yes it works" to the requested Masterwork staff Wield/walk/Remove and correct appearance/movement check. This check is complete. Cache Magic99 requirement remains enforced. The user may explicitly choose `;;nxt level 6 99` for local diagnostics; no levels are granted automatically. Existing basic melee and special item effects remain separate from generic equipment capability.

## September 12: single-stack bank withdrawals and partyhats - superseded by generic equipment

User accepted Use, ordinary Drop/pickup and tinderbox-on-logs. Their follow-up reported a green partyhat Wear refusal and single grimy herbs failing to withdraw while larger log stacks worked.

The bank sender reports actor48447 after exhausting its clicked row, including non-final rows. The old filter wrongly required the next row's ID. The corrected filter admits the cleared actor only for a valid, unchanged source whose capacity-limited movable amount exhausts its positive quantity; partial/blocked requests require the real ID. Original bank handlers, capacity checks, duplicate-slot guards and Withdraw-X ownership remain enforced. Four directly used script pins were added. See [native evidence](protocol-analysis/bank-withdrawal-claims-950.md); no universal client compaction/deletion timing is claimed.

Green partyhat1044 had Wear but no server equipment capability. Added a verified cosmetic family for six original partyhats (1038-1048, even IDs), exact current950 definitions, head slot and both gender models. The original Wear/Remove handlers and live GlobalPlayerUpdater now handle them through the catalog; hair/beard remain visible. Only that verified cosmetic family receives neutral melee bonuses. Other equipment still requires existing verification, and notes are not wearable. Startup preflight validates the partyhat assets.

Validation: **1087 passed,2 existing skips (1089 discovered)**, zero failures/errors; **18 launcher checks**. Actual-cache encrypted bank acceptance passed30actions/35ticks/529frames, including reported grimy herb215, first/middle/final exhaustion, partial1/5, All, stock/capacity limits, duplicate single-herb clicks, full inventory and Withdraw-X. Six-partyhat Wear/swap/Remove, full-bag/controller/stale refusals, both-gender live appearance, neutral melee and RAM save restore passed175frames from the installed jar. Existing melee passed449ticks/1531frames; inventory Drop/pickup/XP117frames; preflight passed86script bindings plus partyhat assets. Only acceptance parser code changed after the full unit pass; its installed-jar rerun passed. [Repair notes](protocol-analysis/bank-wear-950.md), [validation](protocol-analysis/validation-bank-wear-2026-09-12.json).

Installed engine SHA256: 5359c8e81edf265eaf602d6f1a9e405c07bab404a48f60de102a1cd67bb912c7. Server PID 7512, Vulkan client PID 22584, normal -Ataraxia -Collision -Vulkan -LumbridgeNpcs -DevTools flags. PID records remain authoritative; process identities and built/deployed jar hashes were verified. Previous engine/bridge, latest saves/logs and handoff are in implementation-backup/2026-09-12-bank-wear/before-deploy. Original947/910 projects and user items/levels/bank contents were not manually changed.

**Live check pending:** sign in, withdraw a single grimy herb from the middle of the bank, and Wear/Remove the green partyhat while confirming its visible model. The user has been asked; server/client are reopened.
## September 12: inventory menus, Use and Drop - accepted previous repair

User accepted the previous compass and Divination repairs. This update fixes inventory interaction routing in the isolated950 project.

The strict950 item decoder already handles the ordinary option opcodes. The adapter discarded most cache labels; those labels now survive catalog resolution, with notes retaining their own menus and equipment eligibility independently verified. The original backpack event mask disabled Use and operation8. Actual950 CS2 maps cache options1/2/3/4/5 to UI operations1/2/3/7/8; ordinary Drop is operation8/opcode66. Both live Kotlin bootstrap and engine bindings now send the native Use source/target mask, and specialized menus are gated before ordinary operation translation.

Ordinary Drop removes the exact clicked slot/whole stack, publishes owner-private floor loot and reuses the existing lifetime/pickup system. Stale claims, controller vetoes, charges/attributes/Invention data and Destroy/Discard cannot silently lose items. Ordinary Eat uses verified original Food rows, native health/animation boundaries and exact-slot portions. Existing item-on-item production/firemaking/crafting remains connected. Newly derived object-target90 supports raw food on cooking stations, logs on fires, and essence on supported altars after collision approach and final movement publication. NPC-target19 returns explicit unsupported workflow feedback. Unported ordinary effects and special menus no longer fall into arbitrary old handlers.

Validation: **1083 passed,2 existing skips (1085 discovered)**, zero failures/errors; **18 launcher checks**, including the actual live backpack bootstrap mask. Paired-cache encrypted checks passed: Drop/owner pickup/coin+note stacks/duplicate-slot guards/Destroy preservation (117frames); existing264production recipes and selected Use cooking/Eat/Bonfire/altar/controller/arrival checks (272ticks/475frames); all five NPC skill routes plus equipment and wisp feedback (410ticks/1858frames); Settings/map/compass/Run (37actions/473frames); cache preflight. Only acceptance fixtures changed after the full unit pass; their affected runs then passed. See [repair scope](protocol-analysis/inventory-actions-950.md), [native menu/packet evidence](protocol-analysis/inventory-menus-950.md), and [validation record](protocol-analysis/validation-inventory-actions-2026-09-12.json).

Installed engine SHA256: 482a87f49d03511c94022b353a9651b03b9b142f43c0e934bf5b65750e94726d. Server PID 21104, Vulkan client PID 27784. Usual flags -Ataraxia -Collision -Vulkan -LumbridgeNpcs -DevTools; PID records are authoritative. Previous engine/bridge, latest saves and logs are backed up under implementation-backup/2026-09-12-inventory-actions/before-deploy. Built/deployed jar hashes match and runtime process identities were checked. Original947/910 projects and character items/levels were not edited.

**Live acceptance, September 12:** user confirmed Use, Drop/pickup and tinderbox-on-logs work. The partyhat and single-stack withdrawal follow-up is handled by the current build above. Specialized tool-belt/books/pouches/containers, special food effects and NPC-item workflows still require separate ports; this is not full910 content parity.
## September 12: wisp refusal feedback and compass lifetime - accepted previous build

The reported Pale-wisp failure was a full backpack: the saved character already held Pale energy and a Pale memory from successful harvesting, but all28 slots were occupied. Divination sent a specific capacity explanation followed by a generic dispatch failure. The generic last message is now removed; the space explanation suggests banking items or converting memories. Level and unreachable-start refusals remain explicit. Harvest capacity, atomic rewards, movement timing and wisp pause behavior stay enforced.

The compass1919 at minimap1465:12 was mounted with modal type0, so native modal cleanup could dismiss it. It now uses permanent HUD type1. Its cache defaults are visible and its existing placement/rotation remain intact. Added exact cache pins for the mount and all compass components.

Validation: **1043 passed,2 existing skips**, zero failures/errors, plus **17 launcher checks**. The new encrypted Harvest regression reproduced the old duplicate message, then passed full-backpack refusal/no reward, free-one-slot resumption, last-slot stop and higher-tier level feedback. All five NPC skill routes/cancellation/controller gates/respawn passed (369ticks/1733encryptedframes on final run). All13 rune altars, six gateways,12Divination tiers and three conversions passed. UI acceptance passed37encryptedactions/473frames including real compass bootstrap/permanent type, Settings, Escape and map/modal routes. Startup cache preflight passed.

Installed engine SHA256: dc2f7f7b1e13d637240072e9723bdb0a969713b87193b23f0ee00a2acf71e5cd. Server PID17468, Vulkan client PID21472, usual flags -Ataraxia -Collision -Vulkan -LumbridgeNpcs -DevTools. PID records remain authoritative. Previous engine/bridge, latest saves and logs are backed up in implementation-backup/2026-09-12-wisp-compass/before-deploy. Compass originals are additionally in implementation-backup/2026-09-12-compass-modal-fix. No player item/level/save edits were made by these repairs; original947/910 projects remain untouched.

**Live acceptance, September 12:** user confirmed the compass works correctly and Divination is working. The previous live check is complete. See [diagnosis](protocol-analysis/wisp-feedback-950.md), [compass evidence](protocol-analysis/compass-lifecycle-950.md), and [validation](protocol-analysis/validation-wisp-compass-2026-09-12.json).


<!-- SKILLS_BATCH_STATUS_START -->
## September 12: broad skill batch - previous installed build

Implemented and installed in the isolated950 project. The original AstraNXT947 and Ataraxia-PS910 sources/cache were used as references and not edited. [Player guide and commands](SKILL-PORTS-950.md) cover each new workflow and its limits.

- Crafting:103 additional leather/cloth, spinning, jewellery, glass and pottery recipes.
- Summoning:82 pouches and79 scroll conversions. Familiar world behavior/UI is a later milestone.
- Runecrafting:13 ordinary altars, six basic ruins/portal roundtrips, original essence/level/XP/multiplier rules.
- Divination:12 tiers, three conversion modes, atomic memory/energy rewards. Natural wisps pause while harvested; multiple players share the pause and the last stop restores wandering.
- Thieving:69 pickpocket NPC IDs and35 stalls, original rewards/levels/XP, failures/stun, depletion/restore. Stun damage is currently nonlethal; guards/special content remain later work.
- Hunter:four butterflies, net/jar/level checks, catches/misses, release and owner-managed respawn. Traps/other methods remain later work.
- Agility:complete beginner Gnome course, actual collision entrances, verified balance/crawl appearance sets, plane changes and ordered lap bonus. Other/advanced courses remain later work.

Shared repairs include atomic multi-output gathering rewards, stationary arrival for all NPC skills, correct legacy controller mapping, verified extra skill item actions, combined Crafting/Smelting furnace choices, safe metadata-only map borders, and butterfly owner-roster cleanup/respawn cancellation. Natural gathering NPCs reuse162 of166 original rows across22 regions (four blocked950 placements skipped); unrelated NPCs retain prior general spawn scope.

Validation: **1043 passed, 2 skipped (1045 discovered), zero failures/errors**, plus17 launcher checks. All264 new production recipes executed against the paired cache; encrypted Crafting/Summoning menus, all13altar/6gateway/12Divination-tier loops, two-harvester movement pause, seven encrypted Agility obstacles, five NPC skill paths walking/running, natural population/lifecycle, original Mining/Woodcutting/Firemaking/Smelting/Fishing/Cooking/Rewards and31Lodestone journeys passed. [Detailed results](protocol-analysis/validation-skills-batch-2026-09-12.json) retain logs and scope; per-skill technical notes are in protocol-analysis.

Installed engine SHA256: `2545a534ba7327c12e42470a793ea03bfcd6bc405006a4afa45b08b576ed2b8a`. Server PID18644, Vulkan client PID21352; PID log records remain authoritative. Start flags: `-Ataraxia -Collision -Vulkan -LumbridgeNpcs -DevTools`. The rebuilt launcher passed17 checks. Startup logs confirm x's existing character restored at5429,2335,0 with healthy bootstrap, no initial tick/scheduler failures and no withheld appearance.

Pre-change sources/docs/engine and pre-deploy character saves/logs/bridge are backed up under `implementation-backup/2026-09-12-skills-batch`. Latest saved characters are in its `before-deploy/players` subtree. No levels or items were granted automatically. `;;nxt agility` moves to the course; `;;nxt level <ported skill ID> <1-99>` is an explicit localhost-only development action that changes saved XP/level.

**Live checks pending:** new skill menu pixels, animations and complete user playtests. Existing UI/mining/lodestone fixes remain user-accepted. Start with the linked guide; do not describe this batch as full910 skill/content parity.
<!-- SKILLS_BATCH_STATUS_END -->

## September 12: guide refresh and lodestone presentation - accepted previous build

User confirmed skill-guide content works and lodestone map areas now load properly. Mining arrival and stamina/progress bars were accepted earlier. This update addresses the remaining Hero refresh, initial locked icons and clipped network frame.

- Guide skill changes preserve the existing Hero/content mounts.1218 icon clicks send5690 only; external1466 selections while open send5682 then5690. No whole-window layout reload on either path.
- Lodestone unlock presentation is verified and sent during login bootstrap so the client's script-visible variables settle before first onLoad. Values/server travel policy unchanged.
- Lodestone wrapper732/host735 now fit the actual576x360 panel, preserving its centered placement and16px wrapper margin. Paired-cache CS11145 sizes both before mount; close restores the original wrapper and native host policy.

Validation: **1013 passed,2 skipped (1015 discovered)**;17 launcher checks. Actual-cache guide1406packets/all29selections; lodestone31journeys/108encryptedactions/872ticks/5415frames, including login unlocks, sized mounting and panel handoffs. See [technical notes](protocol-analysis/ui-polish-950.md) and [validation](protocol-analysis/validation-ui-polish-2026-09-12.json).

Installed engine SHA256:`17b4d68f45f2b56371d25701c02a89c61cc3c166760cc90a205fbe4eade2a038`. Server18264, Vulkanclient29644; PID log records are authoritative. Start flags remain `-Ataraxia -Collision -Vulkan -LumbridgeNpcs -DevTools`. Backup: `implementation-backup/2026-09-12-ui-polish`, including source/runtime/bridge and latest saved characters/logs.

**Live acceptance, September 12:** user confirmed all three presentation fixes: "yeah everything is working well now." Skill switching without Hero reload, initial lodestone unlock display, and panel edges are now accepted alongside guide content, map-area loading, and mining arrival/bars. No visual check remains pending for this repair batch. Next work is the remaining skill ports from the earlier milestone.

## September 12: mining arrival timing correction - accepted

User confirmed yellow stamina and blue ore-progress bars work, but reported swinging **before reaching** the rock. Live clicks targeted copper113146 at3229,3148 (1x1) and113148 at3230,3147 (2x2). The server reached the correct edges. Actual950 animation/client analysis showed that sending the first mining sequence alongside the final movement can block client interpolation; see [audit](protocol-analysis/mining-arrival-950.md).

Fixed `Native950Interactions.processSkillApproach` to retain the pending object interaction while the current frame contains walking/running or teleport movement, even when its server route queue has just emptied. The final movement is published first; the original ActionManager starts the skill on the following stationary frame, matching910 RouteEvent timing. Already adjacent clicks start normally. The shared object approach also covers woodcutting, fire/bonfire and furnace interactions. No cache flags, rock footprints or rewards were changed.

The new actual-cache regression failed before the correction and passed afterward: both rocks with Run off/on, cancellation between arrival and action start, adjacent restart, bars, original mining rewards/depletion/save capture. Full suite: **1011 passed, 2 skipped (1013 discovered)**. Mining298ticks/779frames; wood/fire452ticks/1009frames; smelting89ticks/237frames; launcher17checks passed. Details: [validation](protocol-analysis/validation-mining-arrival-2026-09-12.json).

Installed engine SHA256: `abc4f2dabc96f8b7fae48b9b8056f06358931c5794e7977786681374e01bd08f`. Started server25188 and Vulkanclient20012 with `-Ataraxia -Collision -Vulkan -LumbridgeNpcs -DevTools`; log PID records are authoritative. Backup of previous engine/sources, bridge, latest saves and logs: `implementation-backup/2026-09-12-mining-arrival`.

**Live acceptance, September 12:** after retesting the installed mining arrival fix, the user confirmed: "so much better. it works well." Mining approach timing is now visually accepted; the stamina/progress bars were confirmed earlier. Next live checks are the skill guide and Um/lodestone journeys, followed by the remaining skill-port work.

## September 12: mining, guide, map areas and seven basic skill ports - before arrival correction

Implemented, installed and launched in the isolated950 project. **Full910 skill/content parity is still incomplete.** The original947 and910 projects were not edited. Read [PLAYTEST-950.md](PLAYTEST-950.md) for player commands and supported workflows; [validation](protocol-analysis/validation-housekeeping-skills-2026-09-12.json) records checks and process identities.

Repairs:
- Mining now waits for collision-valid arrival, uses13 correctly bound950 pickaxes/animations, and publishes yellow stamina plus blue ore-progress bars with cancellation removal. Clay depletion/regrowth, full-bag/level/tool checks and last-slot bonus handling are tested. Old steel/mithril/adamant/rune pickaxes need current IDs45467/45494/45521/45548. Generic gem-rock levels/rewards and specialist mining remain later work.
- Skill guides now use native Hero window0, Skills page varbit18995=2, 1477:715â†’1448:3â†’1218:0â†’1217. All29 skills map correctly; client-owned category/sort handlers remain intact. The guide is session-owned and closes on other activities. Removed unsafe global guidemount state; restored the three-argument development command API.
- Map areas are decoded from the actual950 index23/group3 chunk-label RLE data joined to config2/83 identities.869areas,5197squares,332608chunks and all29lodestones are verified. Um814, Wendlewick857, Anachronia762, Karamja674, Ashdale4; ordinary mainland474. Fixed freshUm-loginâ†’mainland retaining814; debugarea/sweep state is per player.

New ordinary skill paths:
- Fishing16methods and Cooking45recipes retain the originalAction classes, tables/cadence/burn logic. ActualNet/Bait/Cage/Harpoon menus, collision approach, bait/space/level checks, fire Use/Cook choices, quantities and cancellation work. Cooking keeps original0.4 baseXP multiplier.
- Fletching, gemCrafting, herbcleaning/mixing/grinding share243 recipes derived from original910enums. Atomic inventory exchange, retained tools, level checks, quantities and XP use the existingActionManager/Skills/save infrastructure. Native1188 production choice/count menus use verified replies.
- Prayer21bone/ash offerings reuse originalXPtables with atomic consumption and verifiedanimations/effects.
- Smithing12ordinarybars reuse originallevels/XP and current950cache ingredientstructs. Furnacearrival and repeatedreach checks work. Forging/metalbank are not included.

Validation: **1011passed, 2skipped, zero failures**;17launcher/bridge checks. Actual-cache tests passed originalWorld/UI/Rewards/WoodcuttingFiremaking plus all new skills, native encrypted inputs/output, XP and save capture. All29lodestones passed31fullHomeTeleport journeys including freshUm session return. In-game visual checks are pending the user's response; do not claim pixel-level or manual acceptance yet.

Installed engine SHA256: `f4dcd345e14ccae06a1ffc6488da58104f4cdd0d392f9a5ce4b53e71ec50b773`. ServerPID18092, VulkanclientPID18764 at deployment; logs/PIDrecords remain authoritative. Start flags: `-Ataraxia -Collision -Vulkan -LumbridgeNpcs -DevTools`. Backup includes pre-edit sourceZIP/runtime/bridge and pre-deploy savedcharacters/logs at `implementation-backup/2026-09-12-housekeeping-skills`.

Next: finish manual mining/guide/Um checks, then broaden Crafting and Smithing forging, implement native production/item-on-object support, and port remaining skills through original action/controller infrastructure. Toolbelt, perks/contracts/invention, quest/specialist recipes and full world NPC coverage remain separate work. NaturalNPCs still use the existing Lumbridge scope; `;;npc327` supplies a temporary beginner fishing test spot.

## Earlier milestones (historical snapshots)

## Cache name lists and diagnostic spawns â€” latest update

The actual950 cache was dumped into [items.txt](dumps/items.txt), [npcs.txt](dumps/npcs.txt) and [objects.txt](dumps/objects.txt): **63,414 / 32,762 / 140,252 definitions**, including unnamed entries, sorted by ID with no decode errors. Item note/template names resolve from the same cache. Run Export-950CacheNames.ps1 to regenerate them; hashes and coverage are recorded in dumps/export-report.json.

Added **;;npc <id>** and **;;obj <id> [type] [rotation]** at the player's exact current tile, retaining the loopback Native950 DevTools gates. Object shape is selected from the cache by default; explicit types0â€“22 and rotations0â€“3 are checked. Occupied/removed object slots and large footprints touching walls are refused. NPCs use exact-cache definitions and existing world/region/viewport/combat registries. Repurposed/new IDs have instance-specific visual admission and cache-name Examine; they cannot invoke unrelated910 content. Unsupported combat remains refused.

Spawns are temporary: diagnostic NPCs clear when the last player leaves (128 maximum), objects last until restart or removal by game logic. Existing supported interactions still apply. See [guide](protocol-analysis/diagnostic-spawns-950.md).

Validation: **947 passed, 2 skipped (949 discovered)**; actual-cache spawn checks passed with7 encrypted frames; existing world regression and17 launcher checks passed. Engine SHA-256: 27427e7451bce46c24b2f41fd72c3cc0454da6a835c77519302423f6ce7b84d2. Backup: implementation-backup/2026-09-10-diagnostic-spawns. Server9600 and Vulkan client25800 were started with the usual flags; runtime PID records remain authoritative. Live in-game rendering is pending the user's check. See [validation](protocol-analysis/validation-diagnostic-spawns-2026-09-10.json).

## Item command â€” previous update

`;;item <id> [quantity]` adds the requested item to the backpack; quantity defaults to1. `::item` also works. Examples: `;;item 1511 10` for ten logs, `;;item 1351` for one bronze hatchet. This uses the existing loopback-only development-command opt-in and authoritative inventory insertion. Invalid IDs/quantities, stack overflow and insufficient capacity grant nothing.

Current950 cache identities can now be carried and banked independently of the partial910 loot identity table, including valid noted items, and restore through a fresh catalog. This does not enable unported item actions or equipment. The existing inventory wire range remains1â€“65534; charges/custom attributes are outside this command.

Full suite: **932 passed, 2 skipped (934 discovered)**. Actual-cache grants and fresh-catalog restoration passed for coins995, logs1511, tinderbox590, abyssal whip4151 and noted bones527. Engine SHA-256: `0ea101acd8dceb259815a665564ceb243b825f52ecc1e51dd73a78f0351d9079`. See [validation](protocol-analysis/validation-item-command-2026-09-10.json). Backup: `implementation-backup/2026-09-10-item-command`.

## Woodcutting and firemaking â€” previous milestone

Implemented and deployed in the isolated950 project using original910 Woodcutting, Firemaking, Bonfire, ActionManager, item containers, Skills, World/Region and WorldTasksManager. Ordinary trees through elder and ten hatchets are supported; logs and XP, depletion/regrowth, Light, tinderbox-on-log in both directions, repeated bonfire Use, fire expiry and ashes pickup are connected. `;;nxt skilling` supplies missing bronze hatchet/tinderbox and returns to the Lumbridge banker without changing levels or erasing items. The automated test used tree38787 at3228,3267.

Full suite: **924 passed, 2 skipped (926 discovered)**. Exact installed jar passed **377 engine ticks / 841 encrypted frames**, including tool/capacity/cancellation guards, actual collision routing, native object updates, both item-use directions, bronze hatchet Wield/appearance/Remove and exact skill XP save capture. World and reward regression checks, cache preflight, Kotlin compilation and17 launcher checks passed. **Live skilling rendering is pending the user's check**; server and Vulkan client are running. Prior combat pursuit, loot, floating XP, skill circles and pickup were explicitly confirmed by the user.

Engine SHA-256: `10f9f998126535720efd597db87d0f00da8adb10f8968321b5d6ab4e418d3d0c`. Runtime PID records in logs remain authoritative (launch snapshot: server10744, client22552). Backup: `implementation-backup/2026-09-10-woodcutting-firemaking`, containing the accepted combat jar, bridge classes, saves and prior logs. Launch flags remain `-Ataraxia -Collision -Vulkan -LumbridgeNpcs -DevTools`.

See [skill guide](protocol-analysis/skilling-milestone-950.md) and [validation](protocol-analysis/validation-skilling-2026-09-10.json). Object add/change11, remove26 and item-use69 were derived from the actual950 client; cached placement transforms survive stump/regrowth. Chopping uses the verified cache-linked Lumberjack animation role because old ordinary910 sequence IDs were repurposed. Optional pets, invention, contracts, custom/quest/minigame trees, protean logs, portable fires and bonfire HP bonuses remain separate ports. Native toolbelt defaults remain unverified; backpack tools work.

User acceptance (2026-09-10): pursuit, NPC loot, floating XP, skill icon progress circles, and ground pickup into inventory all confirmed working. Woodcutting and firemaking are now implemented as described below.

Updated 2026-09-10 after the woodcutting and firemaking milestone. **Full 910 feature parity is not achieved.**
Previous handoffs/builds are retained under `implementation-backup/2026-09-10-stability` and `implementation-backup/2026-09-10-effects`, with the pre-chat-fix runtime/logs in `implementation-backup/2026-09-10-chat` and the pre-force-correction runtime/logs in `implementation-backup/2026-09-10-force-arrival`.

## Combat pursuit, loot and XP - previous

Implemented and deployed using the existing910 framework: player attack cancellation is separate from NPC retaliation; the NPC follows through Entity.calcFollow and returns home when combat ends. Ordinary drops reuse NPCDropTableRolls/NPCDropsDataParser plus World/Region/FloorItem lifetimes. Take uses the existing pathfinder, controller/account checks and real inventory storage. CombatDefinitions.giveXp and shared Skills calculations now award and save combat XP; the actual950 XP popup interface is initialized with its cache-default layout and resize state.

Full suite **866 passed, 2 skipped (868 discovered)**. Final-cache melee check passed449 owner-thread ticks/1,531 encryptedframes, including five NPC fights with original-table owned drops, pursuit, exact-home return, combined level-up appearance/HP, death and respawn. Final packaged rewards check passed78 encryptedframes/5 real pickup movementticks, covering owner/public/latejoin/rebuild, quantity, Take, inventory/removal and native XP updates. World regression, cache preflight, Kotlin build and17 launcher checks passed. The user subsequently confirmed pursuit, loot, floating XP, skill progress circles and ground pickup into inventory.

Current engine SHA-256:`0e1f7183b27bea5f587039742fff59c1ca479315a824f4369600051bb5c90f29`. Backup:`implementation-backup/2026-09-10-combat-rewards` contains the prior runtime jar/bridge, saved characters and logs. Runtime PID records in logs are authoritative (launch server15240/client25720). Launch flags and Lumbridge spawn scope remain the same. The actual950 client uses a256-tile scene; ground coordinates use that native origin even though the existing server interest radius still uses104tiles.

See [milestone guide](protocol-analysis/combat-rewards-950.md), [validation](protocol-analysis/validation-combat-rewards-2026-09-10.json), [original drop reuse](protocol-analysis/npc-drops-reuse-950.md) and [ground protocol evidence](protocol-analysis/ground-items-950.md). New loot can be carried, banked and saved; unported item actions, ranged/magic, abilities, boss scripts, custom death perks and complete910 feature parity remain later work.

## Generic NPC combat - previous

The six-NPC allowlist is replaced by a shared cache/server-data resolver. **1,500 melee NPC definitions qualify automatically, including 489 multi-tile profiles**. Cache metadata supplies identity, size, combat level, Attack option, cadence and available ratings; concrete server rows supply HP, skill levels, exact maximum damage and respawn timing. 990 eligible rows explicitly lack an attack animation and fight with that animation omitted. This is not full NPC behavior parity: ranged/magic, boss scripts and missing/invalid profiles remain separate work.

Full suite **789 passed, 2 skipped (791 discovered)**. Actual-cache fights for chicken, goblin, man, cow and giant rat passed 436 accelerated ticks and 1,368 encrypted frames, including death/respawn; world regression, preflight and17 launcher checks passed. The user subsequently reported combat otherwise working, with missing pursuit when walking away; that is addressed by the latest milestone above. No individual Man/spider rendering claim is inferred. The prior blue-zero and 40-second goblin respawn checks are user-confirmed.

See [generic combat guide](protocol-analysis/generic-npc-combat-950.md), [coverage](protocol-analysis/generic-npc-combat-coverage-950.json) and [validation](protocol-analysis/validation-generic-npc-2026-09-10.json). Previous engine SHA-256:`7f008a0ca7d52e1a56ed23d241ecea086bc3c605babb279b9b56785d37499632`. Backup:`implementation-backup/2026-09-10-generic-npc-combat`. Runtime PID records in logs remain authoritative. Existing launch flags and Lumbridge spawn scope are preserved.

## Basic melee milestone - previous

Basic native 950 melee is implemented, installed and running. The user confirmed automatic attacks, good animations, 100 damage with the red sword icon, changing health bars, NPC death/removal and safe player recovery. The user also confirmed visible goblin respawn after about 40 seconds. The user confirmed the blue numeric zero correction and visible goblin respawn after about 40 seconds. The complete automated suite passed **759 tests (2 skipped; 761 discovered)**. Actual-cache checks passed approach, blocked-wall isolation, typed damage/HP displays, the banker equipment kit, NPC death/respawn and safe player recovery. The prior Cook/movement regression, startup cache preflight, Kotlin build and 17 launcher checks also passed.

Launch with `Start-950Test.ps1 -Ataraxia -Collision -Vulkan -LumbridgeNpcs -DevTools`. In game, `;;nxt combat` moves beside a supported goblin; choose Attack. Unarmed or the banker bronze kit works. At that earlier build, walking cancelled combat and XP/loot were not yet connected. The latest milestone above supersedes those limitations. Death still keeps items and returns to Lumbridge; ranged/magic, abilities and broader combat content remain later work. Do not enable the legacy NPC combat or death callbacks wholesale.

See [combat guide](protocol-analysis/basic-melee-950.md) and [validation record](protocol-analysis/validation-melee-2026-09-10.json). Latest installed engine SHA-256: `5a0a2e49ea547f773344de87dad1565c75464ac10f98796766683157dd9c68c4`. Pre-combat runtime/logs/saves: `implementation-backup/2026-09-10-basic-melee`.

## Scope and runtime

All changes belong to `C:/Users/developer/Desktop/950RevTest`. `AstraNXT` is the working 947 reference and `Ataraxia-PS` is the original 910 reference; both remain read-only. The paired cache is `950RevTest/cache`.

The real 950 Vulkan client connects to OpenNXT for cache, login and lobby. Kotlin `Ataraxia950Handoff` transfers the game socket to the Java `Ataraxia950` engine. The optional bare OpenNXT backend does not exercise this gameplay path.

```powershell
.\Start-950Test.ps1 -Ataraxia -Collision -Vulkan -LumbridgeNpcs -DevTools
```

`-LumbridgeNpcs` enables verified legacy data spawns only in region 12850; `-DevTools` enables loopback-only development commands. Both are opt-in. Type `;;nxt` in ordinary game chat for help, or use `;;nxt banker`, `cook`, `effects`, `clear`, `bar`, `force`, `combat`, `status`.

Read `logs/server.pid.json` and `logs/client.pid.json` and verify process identities before stopping anything. PID values in notes are launch snapshots, not current authority. The pre-combat Cook correction used engine SHA-256 `32cb9aabc9b98bea9dcb8737590986db88c2c577a75fb6b14560d703ef11aa97` (historical launch: server PID12508, Vulkan client PID15780). The generic NPC combat section above and its validation record describe the current build. Cache startup verification remains enforced. On the preceding build, the user authenticated into World 1 and confirmed movement seems fine after testing `;;nxt banker`, `;;nxt force`, subsequent walking and banker interaction. The server log corroborates repeated two-tile moves, ordinary walking, bank reopening and transfers. The user also confirmed the corrected Cook shortcut and greeting. Completing the old dialogue then entered an unported combat instance and caused a disconnect/reconnect failure. The latest build fixes that boundary; the user confirmed the complete dialogue works without crashing after fresh login.

Live banker checks passed on the preceding build: bank opening, deposit five logs, Withdraw-X two, withdraw remaining three, Talk-to portrait/options, Never mind, dialogue reopening and walking cancellation. Original inventory counts were restored. The same session passed the corrected `;;nxt banker` command, Cook greeting/options and both portraits, dialogue reopening after world-map close, overlapping player effects, and a full green HP bar. These visual results are recorded separately from automated packet tests.

That live force test exposed a persistent two-tile display offset. The native client uses its final forced XY as a logical/path base during interpolation, so the old ordinary arrival delta moved it again. The deployed correction aligns the queued final XY, rebases the force mask, and tracks the recipient's base through waypoints and cancellation. The user confirmed the corrected movement after fresh login; this manual result is separate from byte-test evidence. The Cook shortcut also targeted a trapdoor; the deployed destination is the clear adjacent tile3208,3215, with an actual-cache invariant. Its command retest passed by user confirmation.

An earlier Computer Use session was stopped by physical Escape; the current user offered to perform acceptance manually. Native bulk typing had been ignored by this client, while individual key presses worked.

## Current implementation

- Player graphics now reach all four native slots through a running-cache identity gate. The effect survey found 6,962 identical 910/950 definitions, 297 changed and 2,005 new. Clear-all/per-slot semantics, delays and height packing follow the 950 parser. The NPC bridge shares the identity gate. Model rendering is still a separate client check.
- Player forced movement uses the native 20 ms clock and immutable endpoint snapshots. Its first packet queues final XY at WALK speed, preserving rendered position, then rebases the force mask to that queued base. Per-viewer tracking prevents duplicate XY arrivals while retaining plane changes and normal movement for viewers who missed the original mask. Cancellation corrects position before its stationary mask. New NPC additions wait during active force movement; retained updates/removals continue. A newly visible player waits only its original force-mask frame. Server waypoint timing remains authoritative at 600 ms tick resolution. See the movement evidence for the queued-path and rendered-position distinction.
- Ordinary HP bars 0/3/4 are verified against exact cache identities and included in player and NPC updates. Custom/timed bars remain refused. Basic ordinary melee hits now use verified, viewer-aware marks and life-point scaling. Other legacy hit forms remain refused.
- Lumbridge's data population admits 93 of 123 legacy rows. The other 30 are refused by the actual 910-to-950 identity table. The development banker remains separate. Native NPCs have movement/interaction presentation. The generic resolver now admits 1,500 eligible melee definitions with stats, retaliation, death and respawn. Unsupported profiles and drops remain outside combat admission.
- The preceding stability milestone fixed all typed player mask writers, NPC animation/facing/hit writers, Run `1465:15`, map `1465:11`, HP varp 13537 and the strict 950 settings/map/dialogue/quantity cache bindings. It regenerated the actual 910-to-950 entity identity table; the inherited 947 table is no longer used for 950 admission.
- Nonzero legacy tint conversion and custom graphics flags remain explicit refusals. Transparent colour clearing is supported.

Details: [milestone guide](protocol-analysis/engine-effects-and-lumbridge-950.md), [effects](protocol-analysis/player-effects-950.md), [movement](protocol-analysis/player-force-movement-950.md), [bars/hitmarks](protocol-analysis/hitbars-hitmarks-950.md), [roadmap](PORTING-ROADMAP-950.md).

## Live chat correction

The first real `;;nxt banker` attempts exposed an inherited framing error: Actions read a second packet length after transport had already removed the sole native length. This rejected opcode 87 and caused reconnection. Public/private chat (87/72) and text/name replies (17/53) now consume their actual bodies. Native allocator/sender evidence and literal wire fixtures are recorded in [chat framing](protocol-analysis/chat-framing-950-derived.md). Regression tests now include full encrypted input through the real command handler and exact status output. Real-client `;;nxt banker`, `effects`, `bar` and `force` commands now reach their handlers without reconnect. The forced-movement visual issue is separate from input decoding.

## Cook completion crash correction

The complete Yes conversation previously started the unported ImpossibleJad combat instance.
It queued an invalid dynamic-map destination and inserted a legacy boss outside the native
roster; the move phase failed on region0 and that leftover boss blocked subsequent logins.
The 950 Cook now ends safely before progress changes or instance entry. Controller admission
blocks direct/reconnect entry, and legacy NPC construction/registration is guarded before
world mutation. This does not implement the combat miniquest. See
[cause and safeguards](protocol-analysis/cook-instance-admission-950.md).

The existing x save was valid at3208,3215,0; no account edit/reset was needed. Crash logs,
pre-fix runtime and an unchanged save backup are retained under
`implementation-backup/2026-09-10-cook-instance`. The fixed full dialogue passed automated
and actual-cache checks; the user confirmed it works without crashing after manual login on the latest build. The user then authorized the next milestone: basic RS2-style combat without abilities.

## Previous Cook/effects validation

Combined Java suite: **709 discovered; 707 passed; two skipped; zero failures/errors**. The new movement, graphics, bar, scope and local-command tests are included. The skipped tests are not claimed as passes.

Real-cache preflight passed with verification enforced. `Native950UiAcceptance` passed 37 encrypted inbound actions and 430 parsed outbound frames, including settings/map/run regression. `Native950WorldAcceptance` passed scoped spawn accounting (199 rows, 147 admitted, 52 refused), outside-region exclusion, teardown/repopulation, native player/NPC viewport framing, graphics/bar/force bytes, the queued final-position publication, unchanged server origin at publication, and real world-thread arrival without duplicate recipient movement. The final probe log is `logs/world-acceptance-cook-completion.log`; it also passes the actual Cook full Yes conversation, safe ending and reopening without a position/progress/NPC mutation. These probes use temporary state and no authenticated account; they do not establish client rendering.

The preceding real-client checks confirmed World 1 entry, all six Settings tabs rendering, Escape closing/reopening Settings, mute checkbox toggling/restoration, map open/close, eating shrimp without disconnect and Run toggling. **The user confirmed manual world-map dragging works.**

Cook/effects visual checks are recorded in `protocol-analysis/validation-effects-2026-09-10.json`; current melee checks are in `protocol-analysis/validation-melee-2026-09-10.json`. Do not infer visual results from byte tests.

## Build and verification

```powershell
.\Stop-950Test.ps1
.\Build-Ataraxia950.ps1 -Tasks test,jar -Deploy
.\Build-950Lobby.ps1
.\Start-950Test.ps1 -Ataraxia -Collision -Vulkan -LumbridgeNpcs -DevTools
```

The engine builds with bundled/configured JDK 8 and runs beside OpenNXT on JDK 25. Kotlin overrides must be rebuilt after deploying the engine jar. The live classpath is `patches/classes;OpenNXT/runtime/lib/*`.

Read both `logs/server.out.log` (engine/UI routes) and `logs/server.err.log` (transport/SLF4J exceptions). Startup runs `com.rs.tools.modern.Native950CachePreflight` before opening the server. Do not disable cache verification to turn a failing test green.

The isolated world probe runs either from `Ataraxia950` or with the absolute `-Dataraxia950.data=.../Ataraxia950/data` property, with its built/deployed jar and runtime libraries on the classpath, passing the absolute `950RevTest/cache` directory. It starts only an ephemeral daemon world and embedded transport, no listener or account persistence.

## Next milestone

Extend the working basic melee encounter with combat experience and a verified ground-drop/pickup loop, preserving stack counts and ownership. Targeting, basic animations, damage, death and respawn are already implemented for the admitted creatures. Add projectile/zone effects only when their 950 wire layouts are established. Keep the rest of the old content fenced until its full interaction loop has been checked. Broader shops, skilling, quests, social systems and instances remain later work.
