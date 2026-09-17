# Basic melee milestone â€” revision 950

Status: implemented, automated checks passed, engine installed, server and client running. The user confirmed automatic combat, correct attack animations, damage numbers, health bars, NPC death/removal and player recovery. The user confirmed the blue numeric zero correction and visible goblin respawn after about 40 seconds.

The subsequent [generic NPC expansion](generic-npc-combat-950.md) supersedes this milestone's six-NPC admission and authored attack cadence. This document retains the original basic-loop evidence.

## Scope

The native world owns a single-target, passive PvE melee loop. Attack starts collision-aware approach, stops the prior player action, and runs ordinary timed attacks without abilities. Explicit walking or another interaction cancels both participants. Repeated target clicks preserve cooldowns. Players cannot steal an engaged creature, and encounters are constrained to twelve tiles around the NPC home.

Admitted creatures are Chicken 41/1017 and Goblin 12353/12354/12355/12357. Each needs an exact 950 cache definition, an unchanged identity, an authored combat/stat row and verified animation definitions. Blocked legacy spawn rows remain visual and cannot enter combat. The usual Lumbridge launch currently loads region 12850 goblins; chicken rows in the neighbouring region require that region's population scope.

Supported main hands are unarmed, bronze sword 1277, bronze longsword 1291 and bronze scimitar 1321. Worn items need a verified identity and a concrete classic bonus entry; renamed kit items 1173/1139 have explicit cache pins, metadata checks and reviewed classic bonus mappings; unported weapons and off-hand weapons receive an explanatory refusal. The ordinary banker equipment kit is usable. Actual 950 sword/longsword/scimitar animation maps replace the old 910 mappings.

This slice implements attacks, retaliation, health, misses, death and respawn. Experience, ground loot/pickup, combat style selection, aggression/chasing after cancellation, ranged/magic, abilities, PvP, prayers and special attacks remain subsequent work. No legacy instance/death/perk callbacks are enabled by this service.

## Rules and integration

- World phases: inputs, combat approach, all actor movement, melee damage, immutable viewer frames, mask reset. Combat state belongs exclusively to the native world thread.
- Reuses the source's pure RS2 accuracy/max-hit formula and concrete classic item bonuses. Bonuses resolve through DataPaths for both staged and classic launch layouts.
- Player cadence is four 600ms ticks for unarmed/sword/scimitar, five for longsword. NPC cadence is the authored five ticks. This is explicit RS2 gameplay policy, not a claim about modern ability timings.
- Damage rolls are integer old-school hit points multiplied by ten engine HP. NPC maximum rolls use the whole old-school units within their authored engine maximum. NPC profile HP is 30 for chickens and 50 for goblins. Player native life points and ordinary displayed damage both use engine units multiplied by ten.
- Ordinary damage and blue numeric-zero marks are typed and viewer-aware, with exact cache pins for zero definitions458/464 and their six blue sprite records. Critical, special, soaked and unverified hit forms remain refused.
- Damage is capped at remaining HP and commits before display. The NPC corpse stays visible for its verified death sequence, then leaves the viewport; it respawns at its original clear tile after the authored 60 hidden ticks. The world task remains the sole regeneration owner.
- Player death locks briefly, preserves inventory/equipment, then returns to clear Lumbridge tile 3217,3258 with full HP. A saved HP0 login resumes recovery. A blocked return waits for collision; it cannot break other encounters.
- Per-encounter failures close only the affected connection and release ownership. Disconnect, NPC removal and world teardown cancel outstanding combat.

## Validation and manual check

Focused tests cover attack routing, cadence/misses, ownership, collision gates, gear changes, lethal clamping, corpse visibility/respawn, retained inventory, saved death recovery and failure isolation. Native950MeleeAcceptance uses the real paired cache/collision and encrypted 950 frames in an isolated JVM without login/accounts. Its accelerated ticks establish state and packet composition, not live rendering or real-time cadence.

After deployment: sign in, enter World 1, use `;;nxt combat`, then right-click a goblin and choose Attack. Check automatic punches/swings and retaliation, changing health bars and damage numbers, walking cancellation, death animation/removal and respawn. Empty hands work; a bronze sword and shield/helmet can be collected from the development banker. `;;nxt status` reports combat counters. A player defeat keeps items and returns to Lumbridge.

See melee-rendering-950.md for cache pins and literal hit evidence. Current build/runtime evidence is in validation-melee-2026-09-10.json.

## Accepted build evidence

Full suite: **761 discovered, 759 passed, 2 skipped, zero failures/errors**. Real-cache combat acceptance passed 168 accelerated owner-thread ticks and 536 encrypted frames, including approach, wall isolation, both NPC lifecycles and safe player recovery. The prior movement/effects/Cook regression, expanded cache preflight, Kotlin build and all 17 launcher checks passed.

The copied original classic table is included under Ataraxia950/data/rs2_combat; see PORTING-NOTES.md for source/hash. See melee-equipment-kit-950.md for the two renamed starter-armour mappings.

Installed engine SHA-256: `5a0a2e49ea547f773344de87dad1565c75464ac10f98796766683157dd9c68c4`. Backup: implementation-backup/2026-09-10-basic-melee. The user confirmed the core loop works in the live client, with correct animations, 100 damage/red sword, changing health bars, NPC disappearance on death, and player return to the banker. The live server log also records the goblin respawn after 63 ticks; the user subsequently confirmed visible reappearance after about 40 seconds. Explicit walking cancellation still needs a separate user check. The display-only correction now selects existing950 blue literal0 types458/464 instead of Dodged; cache verification and player/NPC wire fixtures passed, and the user confirmed the blue zeros render correctly. The pre-correction live logs/runtime/saves are preserved under implementation-backup/2026-09-10-melee-blue-zero.
