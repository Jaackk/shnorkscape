# Native950 anvil Smithing

The previous native adapter handled furnace ore-to-bar smelting only. This addition enables **702 current-cache anvil recipes across the original ten metal families**: bronze, iron, steel, mithril, adamant, rune, orikalkum, necronium, bane and elder rune. It includes ordinary weapons/armour, offhands, tools, upgrade chains, ore boxes, nails, arrowheads, unfinished bolts and crossbow limbs where the paired cache records qualify.

## Original framework and current data

Family identity comes from the original `actions/smithing/defs/ForgingBar.java`. The original `Smithing.java` supplies the hammer-work delay and heat/progress formula: initial heat1000 adjusted by overlevel/upgrade, progress28 plus level/tier contribution, high/medium/low/cold multipliers2/1/.66/.5, and heat loss per strike. Original `ActionManager` schedules work; original `Skills.addXp` emits the already ported XP updates/popups.

Current950 item records supply the actual product identity and requirements. Admission requires production type2640=14, category2641=62 and station requirement2646=187, no unsupported extra prerequisite2642/2698, ordinary item templates, a hammer2347 requirement, a supported metal-bar alternative, and a backpack-feasible ingredient count. Direct ingredients2655..2664 and quantities2665..2674 are combined with requirement structs2675..2684, choosing their explicitly identified physical bar2656/7763 and amount2666. This preserves upgrade base products instead of treating an upgrade as bars alone.

Work required is current param7801, level2645, batch quantity2653 and XP2697/10. For example Bronze dagger1205 needs **2bars/30XP**, Bronze arrowheads39 produce **75/15XP**, Rune platebody is current item**45543** with5runebars, and Elder rune longsword+1 **45554** requires base45549 plus2elder runebars. Copying older IDs or old quantities would yield incorrect products.

Every admitted product/material/struct/station/sequence/gauge has a SHA256 pin in `resources/native950/smithing-assets-950.properties` (**865records**). Runtime verification re-derives every roster recipe from the selected cache and compares ingredients/level/work/quantity/XP. `tools/verify_950_smithing_assets.py` also proves animation22143 retains the original910 frame/duration references. The current animation explicitly holds hammer2347; crystal-hammer animation30203 is not used for ordinary hammers.

## Lifecycle and transactions

The native production-selection menu is reused. Only a real, currently present, cache-pinned **Anvil** with the authored **Smith** operation is accepted. Decorative anvils are rejected. Selection and every action step validate collision adjacency, object identity/rotation, movement state, skill, tool and ingredients. Movement, teleport, force movement, station removal/replacement or tool/material loss stops the action before rewards.

One finished project commits materials and output through the existing atomic `Native950Skilling.exchange`, then awards XP. Materials stay in the backpack during progress. Cancellation therefore loses progress but cannot delete ingredients or grant a free product. Requested Make1/5/10/All is bounded, and the hammer is retained. Full backpacks work when the consumed materials free the output slots.

`Native950SmithingGauge` carries exact player-only heat5/progress7 updates and removals. Root integration in `Native950Hitbars` admits only this immutable class, like the mining gauges. Current gauge definitions and sprite records are pinned. Real client rendering remains a manual check.

## Integration hooks

- Startup/preflight: `Native950Smithing.verifyCacheBindings()`.
- World-object admission: `isStation(object)` and `accepts(object,option)`; after the shared approach finishes, open `productionMenu.openChoices("Smithing", Native950Smithing.choices(player,object))`.
- The choices call `startStation(player,object,recipe,quantity)` and capture/revalidate the station.
- Item catalog: `Native950Smithing.itemEntry(id)` admits the paired products/materials/tools; equipment routing stays in the shared cache-driven equipment system.
- Hitbar serialization: exact `Native950SmithingGauge` -> player-only native update/remove using type/percentage; no NPC gauge.

## Verification and remaining scope

`Native950SmithingActionTest` covers eight transaction/cancellation/tool/level/environment/gauge cases. `Native950SmithingAcceptance` verifies all702 atomic exchanges against the real cache, then exercises original engine action loops across all ten metal families plus an upgrade and current rune platebody, native XP output, gauge admission, real anvil collision/options and interruption guards. These are isolated test players; no account save, listening server or authentication is involved.

This is a playable backpack-based forging baseline. The rich anvil interface, metal-bank storage, persistent unfinished items/reheating, burial/masterwork/special quest-gated recipes, augmented/crystal-hammer perks, portable stations and cosmetic overrides remain separate features. Those features are not implied by exposing the native recipe selector. The current batch deliberately refuses unsupported prerequisite families instead of bypassing their requirements.
