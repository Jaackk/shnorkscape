# Runecrafting and Divination batch port — 2026-09-12

Implemented within 950RevTest only. Original action files and Region.java are backed up under `implementation-backup/2026-09-12-skills-batch` before modification. No server/client process or player-save writes were made by the skill worker; combined build/deploy remains the root task's responsibility.

## Runecrafting

Ordinary one-click crafting consumes all eligible backpack essence atomically, then awards original per-essence XP through Skills and produces runes using the original RuneCrafting multiplier thresholds and intermediate bonus-roll policy. ActionManager owns the activity; pending movement/teleport, stale or removed altars, missing levels/essence, controller denial, full backpack/overflow cannot grant free XP or destroy ingredients. Six basic runes accept rune/pure essence; later runes require pure essence.

| Rune | Altar ID | Level (original source) | Base XP/essence |
|---|---:|---:|---:|
| Air | 2478 | 1 | 5 |
| Mind | 2479 | 1 | 5.5 |
| Water | 2480 | 5 | 6 |
| Earth | 2481 | 9 | 6.5 |
| Fire | 2482 | 14 | 7 |
| Body | 2483 | 20 | 7.5 |
| Cosmic | 2484 | 27 | 8 |
| Chaos | 2487 | 35 | 8.5 |
| Astral | 17010 | 40 | 8.7 |
| Nature | 2486 | 44 | 9 |
| Law | 2485 | 54 | 9.5 |
| Death | 2488 | 65 | 10 |
| Blood | 30624 | 77 | 10.5 |

The 950 cache menu is **Use (1)** / **Craft runes (3)**, with Imbue weapons and Configure occupying unrelated slots. Both admitted crafting options invoke the one-click ordinary craft. Soul altar charging, combination runes, abyss, pouches, tiara making, imbued weapons, outfit bonuses and original side-content achievement/contract hooks are outside this batch.

### Six ordinary entrances/exits

The six basic ruins retain Enter in slot1 and require the original matching carried talisman or equipped matching/omni tiara. Talismans are not consumed. Exits need no item. Exact ID, type, rotation and coordinate guard avoids applying altar travel to unrelated Portal objects or diagnostic clones elsewhere. Current-world identity, stationary arrival, controller object-teleport permission, destination collision and known map-area membership are checked before travel.

| Altar | Ruins location / ID | Inside arrival | Exit portal location / ID | Outside arrival | Talisman |
|---|---|---|---|---|---:|
| Air | 3126,3404 / 2452 | 2841,4829 | 2841,4828 / 2465 | 3128,3403 | 1438 |
| Mind | 2981,3513 / 2453 | 2792,4827 | 2793,4827 / 2466 | 2983,3512 | 1448 |
| Water | 3182,3157 / 2454 | 3482,4838 | 3495,4832 / 2467 | 3181,3158 | 1444 |
| Earth | 3305,3473 / 2455 | 2655,4830 | 2655,4829 / 2468 | 3307,3476 | 1440 |
| Fire | 3312,3254 / 2456 | 2574,4848 | 2576,4846 / 2469 | 3311,3256 | 1442 |
| Body | 3052,3444 / 2457 | 2522,4833 | 2521,4833 / 2470 | 3051,3445 | 1446 |

All plane0. Actual950 locations/terrain verified. Old Body arrival2522,4825 is blocked, so it was corrected. Water ruins have moved relative to the original exit target; its exit was corrected to an adjacent current tile. Altar map area IDs are Air521, Mind482, Water672, Earth445, Fire407, Body367; mainland474. These are handled by the existing generated chunk-area map.

Metadata-only map squares border these isolated altar scenes: index5/group9771 near Air has only file5, no terrain3 or locations0. Region's native loader now leaves both walking/projectile masks fully blocked on all planes for reference-table absence of both world files and lets the scene load. Terrain-only squares remain valid. Locations without terrain, declared-but-unavailable files and corrupt reads still fail; exceptions are not swallowed.

Diagnostics: `;;item 1436 28` or `;;item 7936 28`; `;;item 1438 1` for the Air ruins. `;;obj 2478` creates an Air altar at the character for tests; walk clear of its3x3 footprint before clicking Use/Craft runes. Higher altar IDs above can be tested similarly; normal gateways are only the six exact basic entrances/exits.

## Divination

The original DivinationHarvest and DivinationConvert actions remain in ActionManager, with native journeys providing current-cache admission and inventory transactions. The original WispInfo/MemoryInfo tables, memory/enrichment chances, energy tiers, XP values, conversion rate table and cadence are reused.

Twelve tiers: Pale(1), Flickering(10), Bright(20), Glowing(30), Sparkling(40), Gleaming(50), Vibrant(60), Lustrous(70), Brilliant(80), Radiant(85), Luminous(90), Incandescent(95). Normal/enriched wisps and springs18150–18195 are admitted by verified record and Harvest option1. Energy29313–29324, normal memories29384–29395, enriched memories29396–29406 retain matching950 identities. Each harvest awards energy and optional memory in one atomic grant; controller rejection, capacity or integer overflow cannot award only one output or XP.

Energy rift87306 now has **Convert memories (1), Configure (2), Empower (3), Check power (4), Progress (5)**. The910 mode mappings on options2/3/4 no longer match. Convert memories uses the player's existing default (normally XP); Configure shows three immediate native dialogue choices:

- Convert to energy: original rate table, level scaling, rounding, enriched1.5x; base conversionXP1.
- Convert to experience: ordinary memory XP, enriched2x.
- Use energy for more experience: normal5/enriched10 energy,1.25x XP when paid; fallback to ordinaryXP when energy is missing. Original enriched-first ordering remains.

Conversion choice starts the selected loop for the held memories; it does not send the unverified910 preference varbit40524 or persist a new preference. Missing native legacy boon arrays are safely treated as no boon; existing populated boon data follows the original1.1x rule.

Wisps/springs remain in their current admitted form. A reference-counted harvesting pause temporarily stops natural wisp wandering, clears queued NPC steps, and restores the original radius only after the last harvester stops. Failed starts acquire no pause; cancellation, logout force-stop and removed targets release their ownership. Later radius changes from other NPC systems are preserved; transformation, spring lifetime/depletion, vacuum, skillchompas, outfits/perks, chronicle/clue spawns, boons creation and transmutation are outside this batch. Native NPC's legacy transformation API is explicitly forbidden, so this port does not call it or silently repurpose old masks.

Diagnostics: `;;npc 18150`, walk beside the Pale wisp, Harvest; `;;obj 87306`, walk clear of the rift's2x2 footprint, Convert memories or Configure. Other ordinary wisp IDs are18151,18153,18155,18157,18159,18161,18163,18165,18167,18169,18171. `;;item 29384 1` gives a Pale memory; `;;item 29313 10` gives Pale energy for conversion checks.

## Validation and integration

`Native950Runecrafting.handles(object,option)` covers both altar crafting and exact gateway Enter; `start(player,object)` dispatches appropriately. `Native950Divination.isHarvestable/startHarvest`, `isRift/startConvert`, `conversionChoices` are integrated by the root task. Both expose `itemEntry`, `itemEntries` and `verifyCacheBindings`. `conversionChoices` uses RunnableChoice, so no irrelevant quantity prompt follows the mode choice.

`tools/verify_950_runecrafting_divination_assets.py --write` regenerates149 per-recordSHA256 pins plus JSON evidence. Nine sequences have identical910/950 frame/duration references; five effects have identical full definitions. Matching sequence references alone do not independently prove unchanged rendering models.

Focused tests: `com.rs.game.player.client.Native950RunecraftingDivinationTest` (4) and `com.rs.game.Native950RunecraftingBlankRegionTest` (5). All9 passed in the isolated source-only compiler run.

Actual-cache entrypoint: `com.rs.game.player.client.Native950RunecraftingDivinationAcceptance <950-cache-path>` with the usual native world/data JVM flags. It checks13altars, all6 real-cache gateway roundtrips,12harvest tiers, all3 conversion modes, enriched cost, no-energy fallback, original action ownership, XP/items, current target/movement gates, fullbag/overflow/controller multi-output rollback, and native mask/transport acceptance. Final packaged build/live visual verification remains for the root task/user; no visual result is claimed here.


### Natural-wisp movement regression

Actual ordinary wisp definitions have movementCapabilities3 and normal source rows default to canMove=true, producing radius5 wandering. A shared pause now keeps the target still while harvested. `Native950DivinationHarvestPauseTest` covers two owners, queued paths, final/idempotent release, later external radius changes and finished-NPC cleanup. `Native950RunecraftingDivinationAcceptance <cache> --harvest-pause` exercises two actual-cache players through native NPC movement ticks, cancellation and target removal.
