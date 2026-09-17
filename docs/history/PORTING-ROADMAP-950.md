# 950 port: current gaps and next milestones

## September 12: first-pass training coverage for all 29 skills

[SKILLS-FIRST-PASS-950.md](SKILLS-FIRST-PASS-950.md) is the current coverage and playtest reference. The latest batch adds Farming, Construction, Invention, Archaeology, solo Dungeoneering, and basic Ranged, Magic and Necromancy attacks to the previously implemented skills. These are repeatable training routes, not complete retail content or a fully self-sufficient fresh-account progression game.

Next passes should refine the existing routes: client rendering and feedback, natural supplies and world access, ranged projectiles and spell selection, familiars, Farming's other patch families, player-owned houses, fuller dungeon generation, advanced Invention and Archaeology systems, Hunter traps, and remaining production recipes. Existing Smithing forging, Slayer and expanded Agility are already implemented. See [HANDOFF-950.md](HANDOFF-950.md) for the latest build, validation and backups.

## Historical milestone snapshots

## September 12: mining, guide, map areas and seven basic skill ports - CURRENT

Implemented, installed and launched in the isolated950 project. **Full910 skill/content parity is still incomplete.** The original947 and910 projects were not edited. Read [PLAYTEST-950.md](PLAYTEST-950.md) for player commands and supported workflows; [validation](protocol-analysis/validation-housekeeping-skills-2026-09-12.json) records checks and process identities.

Repairs:
- Mining now waits for collision-valid arrival, uses13 correctly bound950 pickaxes/animations, and publishes yellow stamina plus blue ore-progress bars with cancellation removal. Clay depletion/regrowth, full-bag/level/tool checks and last-slot bonus handling are tested. Old steel/mithril/adamant/rune pickaxes need current IDs45467/45494/45521/45548. Generic gem-rock levels/rewards and specialist mining remain later work.
- Skill guides now use native Hero window0, Skills page varbit18995=2, 1477:715→1448:3→1218:0→1217. All29 skills map correctly; client-owned category/sort handlers remain intact. The guide is session-owned and closes on other activities. Removed unsafe global guidemount state; restored the three-argument development command API.
- Map areas are decoded from the actual950 index23/group3 chunk-label RLE data joined to config2/83 identities.869areas,5197squares,332608chunks and all29lodestones are verified. Um814, Wendlewick857, Anachronia762, Karamja674, Ashdale4; ordinary mainland474. Fixed freshUm-login→mainland retaining814; debugarea/sweep state is per player.

New ordinary skill paths:
- Fishing16methods and Cooking45recipes retain the originalAction classes, tables/cadence/burn logic. ActualNet/Bait/Cage/Harpoon menus, collision approach, bait/space/level checks, fire Use/Cook choices, quantities and cancellation work. Cooking keeps original0.4 baseXP multiplier.
- Fletching, gemCrafting, herbcleaning/mixing/grinding share243 recipes derived from original910enums. Atomic inventory exchange, retained tools, level checks, quantities and XP use the existingActionManager/Skills/save infrastructure. Native1188 production choice/count menus use verified replies.
- Prayer21bone/ash offerings reuse originalXPtables with atomic consumption and verifiedanimations/effects.
- Smithing12ordinarybars reuse originallevels/XP and current950cache ingredientstructs. Furnacearrival and repeatedreach checks work. Forging/metalbank are not included.

Validation: **1011passed, 2skipped, zero failures**;17launcher/bridge checks. Actual-cache tests passed originalWorld/UI/Rewards/WoodcuttingFiremaking plus all new skills, native encrypted inputs/output, XP and save capture. All29lodestones passed31fullHomeTeleport journeys including freshUm session return. In-game visual checks are pending the user's response; do not claim pixel-level or manual acceptance yet.

Installed engine SHA256: `f4dcd345e14ccae06a1ffc6488da58104f4cdd0d392f9a5ce4b53e71ec50b773`. ServerPID18092, VulkanclientPID18764 at deployment; logs/PIDrecords remain authoritative. Start flags: `-Ataraxia -Collision -Vulkan -LumbridgeNpcs -DevTools`. Backup includes pre-edit sourceZIP/runtime/bridge and pre-deploy savedcharacters/logs at `implementation-backup/2026-09-12-housekeeping-skills`.

Next: finish manual mining/guide/Um checks, then broaden Crafting and Smithing forging, implement native production/item-on-object support, and port remaining skills through original action/controller infrastructure. Toolbelt, perks/contracts/invention, quest/specialist recipes and full world NPC coverage remain separate work. NaturalNPCs still use the existing Lumbridge scope; `;;npc327` supplies a temporary beginner fishing test spot.

## Earlier milestones (historical snapshots)

## Cache name lists and diagnostic spawns — latest update

The actual950 cache was dumped into [items.txt](dumps/items.txt), [npcs.txt](dumps/npcs.txt) and [objects.txt](dumps/objects.txt): **63,414 / 32,762 / 140,252 definitions**, including unnamed entries, sorted by ID with no decode errors. Item note/template names resolve from the same cache. Run Export-950CacheNames.ps1 to regenerate them; hashes and coverage are recorded in dumps/export-report.json.

Added **;;npc <id>** and **;;obj <id> [type] [rotation]** at the player's exact current tile, retaining the loopback Native950 DevTools gates. Object shape is selected from the cache by default; explicit types0–22 and rotations0–3 are checked. Occupied/removed object slots and large footprints touching walls are refused. NPCs use exact-cache definitions and existing world/region/viewport/combat registries. Repurposed/new IDs have instance-specific visual admission and cache-name Examine; they cannot invoke unrelated910 content. Unsupported combat remains refused.

Spawns are temporary: diagnostic NPCs clear when the last player leaves (128 maximum), objects last until restart or removal by game logic. Existing supported interactions still apply. See [guide](protocol-analysis/diagnostic-spawns-950.md).

Validation: **947 passed, 2 skipped (949 discovered)**; actual-cache spawn checks passed with7 encrypted frames; existing world regression and17 launcher checks passed. Engine SHA-256: 27427e7451bce46c24b2f41fd72c3cc0454da6a835c77519302423f6ce7b84d2. Backup: implementation-backup/2026-09-10-diagnostic-spawns. Server9600 and Vulkan client25800 were started with the usual flags; runtime PID records remain authoritative. Live in-game rendering is pending the user's check. See [validation](protocol-analysis/validation-diagnostic-spawns-2026-09-10.json).

## Item command — previous update

`;;item <id> [quantity]` adds the requested item to the backpack; quantity defaults to1. `::item` also works. Examples: `;;item 1511 10` for ten logs, `;;item 1351` for one bronze hatchet. This uses the existing loopback-only development-command opt-in and authoritative inventory insertion. Invalid IDs/quantities, stack overflow and insufficient capacity grant nothing.

Current950 cache identities can now be carried and banked independently of the partial910 loot identity table, including valid noted items, and restore through a fresh catalog. This does not enable unported item actions or equipment. The existing inventory wire range remains1–65534; charges/custom attributes are outside this command.

Full suite: **932 passed, 2 skipped (934 discovered)**. Actual-cache grants and fresh-catalog restoration passed for coins995, logs1511, tinderbox590, abyssal whip4151 and noted bones527. Engine SHA-256: `0ea101acd8dceb259815a665564ceb243b825f52ecc1e51dd73a78f0351d9079`. See [validation](protocol-analysis/validation-item-command-2026-09-10.json). Backup: `implementation-backup/2026-09-10-item-command`.

## Woodcutting and firemaking — previous milestone

Implemented and deployed in the isolated950 project using original910 Woodcutting, Firemaking, Bonfire, ActionManager, item containers, Skills, World/Region and WorldTasksManager. Ordinary trees through elder and ten hatchets are supported; logs and XP, depletion/regrowth, Light, tinderbox-on-log in both directions, repeated bonfire Use, fire expiry and ashes pickup are connected. `;;nxt skilling` supplies missing bronze hatchet/tinderbox and returns to the Lumbridge banker without changing levels or erasing items. The automated test used tree38787 at3228,3267.

Full suite: **924 passed, 2 skipped (926 discovered)**. Exact installed jar passed **377 engine ticks / 841 encrypted frames**, including tool/capacity/cancellation guards, actual collision routing, native object updates, both item-use directions, bronze hatchet Wield/appearance/Remove and exact skill XP save capture. World and reward regression checks, cache preflight, Kotlin compilation and17 launcher checks passed. **Live skilling rendering is pending the user's check**; server and Vulkan client are running. Prior combat pursuit, loot, floating XP, skill circles and pickup were explicitly confirmed by the user.

Engine SHA-256: `10f9f998126535720efd597db87d0f00da8adb10f8968321b5d6ab4e418d3d0c`. Runtime PID records in logs remain authoritative (launch snapshot: server10744, client22552). Backup: `implementation-backup/2026-09-10-woodcutting-firemaking`, containing the accepted combat jar, bridge classes, saves and prior logs. Launch flags remain `-Ataraxia -Collision -Vulkan -LumbridgeNpcs -DevTools`.

See [skill guide](protocol-analysis/skilling-milestone-950.md) and [validation](protocol-analysis/validation-skilling-2026-09-10.json). Object add/change11, remove26 and item-use69 were derived from the actual950 client; cached placement transforms survive stump/regrowth. Chopping uses the verified cache-linked Lumberjack animation role because old ordinary910 sequence IDs were repurposed. Optional pets, invention, contracts, custom/quest/minigame trees, protean logs, portable fires and bonfire HP bonuses remain separate ports. Native toolbelt defaults remain unverified; backpack tools work.

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


Updated 2026-09-10. This is an isolated port in `950RevTest`; `AstraNXT` remains the 947 reference and `Ataraxia-PS` supplies the original 910 game logic. Reusing that logic does not establish that its interface IDs, entity IDs, animation IDs or packet effects are valid for 950. Full 910 feature parity is not yet achieved.

## What this pass repaired

- **Session stability:** the previous live logs showed session-ending failures when an ordinary action queued a player animation and when Settings encountered inherited cache pins. The relevant mask writers and the Settings/World Map bindings have now been adapted in source. These were actual failures, not merely future protocol gaps.
- **Settings and map:** a paired-cache comparison decoded all 81 selected Settings/World Map scripts. All calling signatures matched; 68 of 76 Settings programs and all five World Map programs matched after opcode normalization. The eight changed Settings programs contain native-client changes such as taller pages, placeholder colour packing and revised quick-option checks. The port retains their 950 implementations. Minimap routing also needed real component changes: Run is component 15 and World Map is component 11. See [UI evidence](protocol-analysis/ui-scripts-950-evidence.md).
- **Life points:** the 950 player refresh now sends varp 13537, using the scale derived from the 950 scripts and native arithmetic handler. The old 947 current-HP varbit was being refused on each login. The 910/947 paths remain unchanged. See [HP derivation](protocol-analysis/hitpoints-950.md).
- **Entity updates:** all ten blocks exposed by the typed player mask API are now encodable, including animation, facing, forced movement, colour, spot animations and hits/bars. NPC animation, face-entity, spot-animation and hit writers were derived and connected where engine state is available. Other raw or underived blocks remain refused. A writer being available does not mean every engine feature supplies it yet. See [player derivation](protocol-analysis/player-masks-950-derived.md) and [NPC derivation](protocol-analysis/npc-masks-950-derived.md).
- **Region music verification:** the test fixture now contains the actual 950 music enums, retaining strict digest checks. All old name/archive mappings were unchanged; six tracks were added. Existing live logs already showed region-track resolution, but this pass does not claim a new audible client check.
- **Legacy identity checks:** the runtime now uses a freshly generated **910-to-950** table rather than the inherited 947 table. Of 8,735 NPC IDs referenced by legacy data, 7,988 classify as unchanged and nine as case-only restyles; the other verdicts remain refused. Banker 494 and Cook 278 still classify as unchanged. NPC 380 demonstrates why regeneration matters: its travel option changed in 950. Classification checks selected definition fields, not complete gameplay compatibility. See [identity report](protocol-analysis/id-validity/report-910-to-950.md).

## Validation status

The final combined run discovered **618 tests: 616 passed, two skipped, zero failed**. The updated engine was deployed, and the Kotlin build and verification completed. The dedicated player/NPC mask suites were included; the outdated mask-fence and HP expectations and the inherited 947 music fixture were corrected without disabling the production verification guards.

Fresh-client checks confirmed entry into World 1, all six Settings tabs rendering, Escape closing and reopening Settings, and the audio music-mute control visibly toggling and being restored. The world map opened at the correct player position. The user confirmed manual dragging works; the earlier automated drag did not reproduce their mouse input. A read-only comparison found every component in interfaces 1421/1422, the root map hosts, and the relevant normalized hook scripts unchanged from 947, so no cache topology change currently explains that observation.

Additional live checks confirmed map close restoring the world, eating shrimp updating chat/inventory without disconnect, subsequent movement, and Run toggling on/off through the corrected component. Subsequent live checks passed Cook dialogue/portraits, repeated banker dialogue and bank Withdraw-X. Measured Run speed, equipment removal/appearance and an audible region-music transition remain separate acceptance items. Check both server log streams for disconnects and unexpected refusals. Byte tests and cache comparisons do not establish these client results.

The earlier `HANDOFF-950.md` statement that only protocol breadth remained was too broad: live cache-pin and animation failures still broke ordinary interactions. Its old green-test count should not be used as evidence for this changed build.

## Current effects/NPC milestone

The first bridge/population slice is implemented: four player graphics slots with 6,962 paired identities, force timing/cancellation and movement locks, standard player/NPC HP bars, and an opt-in region-12850 population (93 admitted rows out of 123). Scope, teardown and re-entry are tested. The bounded basic melee slice is now implemented; broader NPC combat is still pending. See [milestone guide](protocol-analysis/engine-effects-and-lumbridge-950.md).

The Cook/effects milestone suite was **709 discovered, 707 passed, two skipped, zero failed**. Real-cache UI, cache preflight and world acceptance passed. The earlier 618-test result above belongs to the preceding stability milestone. The live force check found a duplicate endpoint movement; the corrected queued-base and per-viewer handling is deployed and the user confirmed movement seems fine after fresh login, with normal walking and banking corroborated in the server log. The Cook shortcut landing passed the user check. Full dialogue then exposed unported combat-instance entry; the latest build now closes it safely and prevents legacy NPC contamination, with automated acceptance passed and the user confirming the complete conversation no longer crashes. Per-check current live status is in `protocol-analysis/validation-effects-2026-09-10.json`.

## Next milestones, in order

1. **Extend the working melee loop.** Basic targeting, timed attacks/retaliation, viewer-dependent damage, health bars and death/respawn now use generic profiles for 1,500 eligible melee NPC definitions. Available animations use verified 950 bindings. Next add combat experience and one verified ground-drop/pickup loop, with item identity, ownership and stack accounting. Add zone/projectile packets from 950 evidence and prove a ranged or magic encounter before enabling broad combat content.
2. **Port content as complete playable slices.** Reuse `NPCHandler`, `ObjectHandler` and 910 action/controller logic while adapting modern interfaces, containers, vars and scripts per feature. Prioritize gathering, production quantity dialogs, shops/currency, equipment effects and item-on-object/NPC flows. A handler already existing in Java does not establish that its output is ported.
3. **Expand account and world systems.** Public/social chat, friends/ignores, broader interface families, ground items/projectiles, quests, travel, instances, minigames and persistence need explicit coverage. Separate verified features, features awaiting live acceptance and legacy-only code.

Nonzero colour tint, custom effect flags and broader bar types remain evidence gaps in the bridge. Expand NPC region scope only after the bounded slice works in the real client. Preserve isolated launch/build paths and strict cache checks.
