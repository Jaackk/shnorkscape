# Crafting and Summoning production port — 2026-09-12

This batch adds 264 ordinary recipes to the existing 243 production recipes: 103 Crafting recipes and 161 Summoning recipes. The existing original ActionManager, Skills XP pipeline, native inventory exchange, production choice/quantity dialogue and save path remain in use. No changes were made to the original 910 or AstraNXT source trees.

## Crafting scope

| Group | Recipes | Trigger |
| --- | ---: | --- |
| Leather, dragonhide, snakeskin, royal hide, imphide, spider silk, carapace, batwing, cloth | 51 | Craft on material, or needle/thread used on material |
| Wool, flax, sinew, magic roots, yak hair | 5 | Spin at spinning wheel |
| Gold/gem rings, necklaces, amulets, bracelets | 28 | Existing furnace Smelt/Use menu; carry the appropriate mould |
| Unstrung holy symbol, tiara | 2 | Furnace with silver bar and mould |
| Beer glass through empty light orb | 8 | Craft molten glass or use glassblowing pipe on it |
| Unfired pottery | 4 | Form at pottery wheel |
| Firing pottery | 4 | Fire at pottery oven |
| Molten glass | 1 | Furnace with soda ash and a bucket of sand |

The leather and spinning product families come from the original LeatherData and SpinningItem enums. Jewellery and glass/pottery families come from JewellerySmithing and CraftingRs3Dialogue. Current cache product records provide the current prerequisites, quantities and XP. Examples: imphide hood requires level12/two hides/20XP, the old leather coif row actually names a studded coif and is excluded from sewing, and the old light-orb row named the wired product rather than the empty glass stage. The old silver table produced already-blessed symbols; this implementation produces the actual unstrung symbol. Quest-gated silver products and special flasks remain outside this batch.

Needles, moulds and glassblowing pipes are retained. Thread follows the original leather action's first five-item reel followed by its original 2–6-item reel cadence. On a reel's final use, materials and thread are consumed in the same atomic exchange. Full backpacks can replace consumed material in place; missing tools, insufficient level and output overflow cannot consume inputs or award XP.

## Summoning scope

82 ordinary pouch recipes and 79 pouch-to-scroll conversions at a verified full summoning obelisk. Pouches require their actual current empty pouch, charm, spirit shards and secondary material(s). Each pouch conversion creates ten scrolls. This is production only: familiar summoning, familiar combat/AI, special moves, follower UI and beast-of-burden storage still require their own port. Nightmare/nihil and other recipes requiring special systems or nonstandard energy ingredients remain outside this batch.

The product whitelist comes from the original SummoningPouches / SummoningScroll enums, with three corrected identities supplied by the original Summoning.Pouches enum and independently verified in the950 cache: kalphite12063 (old12064 was noted), void spinner12780 (old12781 was noted), graahk12810 (old12710 is a Labrador puppy). Current product records also fix missing empty pouches in the old hydra/yak rows and the old spider-scroll level/XP.

Production-only950 records25590–25600 provide the additional void/titan/cockatrice pouch alternatives. Param2989 directs these records to the real backpack scroll; XP is read from that pinned canonical output. This prevents producing hidden recipe-display items. Granite lobster currently offers the original5kg granite route; smaller granite alternatives are not presented in this pass.

## Cache boundaries and behavior

`crafting-summoning-assets-950.properties` pins 578 individual definitions: 457 named item records,92 station objects,24 recipe structs and5 sequences. Native950Crafting.verifyCacheBindings and Native950Summoning.verifyCacheBindings reject changed/missing definitions or unexpected recipe counts. Tools/Native950CraftingAssetDump.java is an explicit research/binding generator; startup never regenerates pins.

Sequence25594 (sewing),896 (wheel),32626 (furnace),884 (glassblowing),725 (infusion) have the same frame references and total durations as their original910 definitions. Both definitions and hashes were inspected; this does not independently verify rendered mesh/frame dependencies.

Only a matching actual cache operation is accepted: Spin, Form, Fire, furnace Smelt/Use/Craft, obelisk Infuse-pouch. Renew-points is not interpreted as infusion. Every station action checks the current object identity and rotation, collision adjacency, plane and stationary movement state both when starting and before each exchange. The shared router separately waits until the final walking movement has been published. Walking away, replacement/removal, teleport/forced movement, missing ingredients or a controller refusing item additions stop work without later XP/items.

## Verification

`Native950CraftingSummoningAcceptance <cache>` passes with strict cache verification: all264 recipes complete exactly once through the original engine, preserve tools and award native UPDATE_STAT packets. It also checks modern recipe corrections, alternate scroll inputs, missing ingredients, level gates, remote starts, removed stations and movement-away cancellation. Result:2876 original engine ticks,3304 decoded encrypted950 output frames,264 stat updates. Log: `logs/crafting-summoning-acceptance-2026-09-12.log`.

`Native950CraftingActionTest` passes all four focused unit regressions for thread cadence, Make count, lost thread/cancellation and a completely full backpack.

`Native950CraftingRoutingAcceptance <cache>` exercises encrypted item-use, inventory Craft, wheel Spin option2, merged furnace menu, obelisk pouch/scroll quantity dialogs and stale dialogue responses. Combined-router acceptance passed with144 engine ticks and305 decoded encrypted950 frames. Log: `logs/crafting-routing-acceptance-2026-09-12.log`. Real client pixels/animations still need manual playtesting.

## Quick diagnostics

- Sewing: `;;item 1733 1`, `;;item 1734 10`, `;;item 1741 10`; Craft the leather or use the needle on it. Level1 boots/gloves/cowl/chaps/body are available.
- Spinning: wool1737 or flax1779 at spinning wheel2644 (Spin is option2).
- Glass: glassblowing pipe1785 and molten glass1775. Beer glass is level1.
- Furnace: gold bar2357 + ring mould1592 at level5 for gold ring1635; silver bar2355 + holy mould1599 at level16 for unstrung symbol1714.
- Pottery: soft clay1761 at wheel2642, then Fire the unfired pot at oven2643.
- Summoning: pouch12155 + gold charm12158 + wolf bones2859 + seven spirit shards12183 at obelisk67036. Choose Spirit wolf pouch; reopen with that pouch to create Howl scrolls12425. This works at level1.
- Existing `;;obj <id>` can place a diagnostic station; move onto a clear tile beside it before clicking.

`crafting-summoning-recipes-950.tsv`, `crafting-summoning-items-950.tsv` and `crafting-summoning-objects-950.tsv` contain the full verified lists.
