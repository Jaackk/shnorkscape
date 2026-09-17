# Third skill pass: ordinary production, 950

12 September 2026. Changes are confined to `950RevTest/Ataraxia950`. The existing native interaction router, dialogue selection, ActionManager, inventory transactions, Skills XP and save systems remain the owners. The lobby, 910/947 source, standalone package and live processes are untouched by this subtask.

## What changed

| Skill | Implemented refinement | Ordinary entry point |
| --- | --- | --- |
| Herblore | Four herb tars; 15 swamp tar and one clean herb produce 15 ammunition, retaining the pestle | Use the clean herb on swamp tar, select product and quantity |
| Fletching | Twelve gem/pearl-to-bolt-tip routes; current cache requirements and per-cut XP replace the old table's incorrect Crafting-like XP | Use chisel on a cut gem or oyster pearl(s) |
| Crafting | Four elemental battlestaves and the two advanced glass vials | Orb on battlestaff; glassblowing pipe on molten glass |
| Cooking | Seventeen preparation variants for dough, pie shells/fillings, pizza and cake; bread, pizza and cake baking | Use ingredients together; use the raw food on a range or choose Cook at the range |
| Construction | Cache-authorized interchangeable nails; individual workbench tier limits | Construct/Work-at, or use a nail/plank/tool on the workbench |
| Smithing/Smelting | All 12 ordinary bars now read current level/XP from the pinned output record; modern per-bar smelting speed milestones | Smelt at an existing furnace |

There are now **276 generic production rows** (243 existing + 33 new), **117 Crafting rows** (111 + six), three additional range-baking foods, 45 existing flatpacks and 12 ordinary smelting bars. The 702 existing forging rows were reviewed and retained; this pass does not claim a new forging content expansion.

## Current-cache recipe evidence

Every admitted new product, input, tool, ingredient-alternative struct and animation is SHA-256 bound to the paired flat950.1 cache. The additional pins are in the existing production and Crafting/Summoning properties; the six new nail identities are appended to Farming/Construction properties. `skills-thirdpass-production-cache.txt` contains the actual decoded identities and production parameters, and `tools/Native950ProductionThirdPassAssetDump.java` reproduces the inventory/struct/sequence fingerprints.

The product reader now explicitly distinguishes Crafting type11, Herblore9, Fletching19, Cooking16 and Summoning24. It still rejects unimplemented secondary requirements, special item templates and unknown alternatives. Level is param2645, ordinary output count2653, and base XP2697/10. Ingredient records remain2655..2674 plus explicit alternative structs2675..2684. The reader does not assume that every production family scales material quantity or XP by its output count.

| Product | Inputs and tools | Level | Base XP per completed batch |
| --- | --- | --- | --- |
| Guam tar10142 | Swamp tar1939 x15 + clean guam249; pestle233 | 19 | 30 |
| Marrentill tar10143 | 1939 x15 + clean marrentill251; pestle233 | 31 | 42.5 |
| Tarromin tar10144 | 1939 x15 + clean tarromin253; pestle233 | 39 | 55 |
| Harralander tar10145 | 1939 x15 + clean harralander255; pestle233 | 44 | 72.5 |
| Water battlestaff1395 | Battlestaff1391 + water orb571 | 54 | 100 |
| Earth battlestaff1399 | 1391 + earth orb575 | 58 | 112.5 |
| Fire battlestaff1393 | 1391 + fire orb569 | 62 | 125 |
| Air battlestaff1397 | 1391 + air orb573 | 66 | 137.5 |
| Powerburst vial48960 / bomb vial48961 | Molten glass1775 + retained pipe1785 | 54 | 55 each |

These recipes agree with the Wiki's [Herblore production table](https://runescape.wiki/w/Calculator:Herblore/Production_costs) and [Crafting tables](https://runescape.wiki/w/Crafting). The old910 `Herblore.Ingredients`, `BoltTips`, `LeatherData`, `SpinningItem` and inventory battlestaff handler supplied the content families and ordinary interaction structure; current cache records supply present identities and production prerequisites.

Bolt cutting supports opal, jade, oyster pearl, oyster pearls, red topaz, sapphire, emerald, ruby, diamond, dragonstone, onyx and hydrix. The one-gem yields are12 tips except onyx24 and hydrix36; single oyster pearl411 yields6 and oyster pearls413 yields24. Cutting is Fletching XP per gem/pearl item, not per resulting tip: sapphire is4.7, diamond7, hydrix10.6. The 413 variant retains the original family quantity with the pinned current pearl-tip level/XP. This corrects the old table's use of gem-cutting-sized awards. [Gem-to-bolt-tip calculator](https://runescape.wiki/w/Calculator:Fletching/Gems_to_bolt_tips)

## Food preparation and baking

Pot of flour1933 with water in jug1937, bucket1929 or bowl1921 offers bread dough2307, pastry dough1953 or pizza base2283 at levels1,10,35. All three vessels are explicit cache struct14969 alternatives. Each preparation returns the empty flour pot1931 and the exact empty water vessel1935/1925/1923, together with the dough. Space for those returns is checked before any consumption.

Pastry dough + pie dish2313 makes pie shell2315. Shell + redberries1951, apple1955, cooked meat2142 or cooked chicken2140 makes the corresponding raw pie; existing native pie cooking handles those products. Pizza base + tomato1982 makes incomplete pizza2285; adding cheese1985 makes uncooked pizza2287. Cake tin1887 + flour1933 + egg1944 + milk1927 makes uncooked cake1889, returning the empty pot and bucket. Every cache-defined preparation gives1 base Cooking XP. Food preparation combines all required ingredients in one transaction even when the selected pair contains only two of them.

Bread dough, uncooked pizza and uncooked cake now bake on a range into2309/2289/1891. Cake returns its tin on success or burning. A full backpack of raw cakes is refused before any item or XP change because the tin requires another slot. The range-only distinction and returned cake tin follow [Cooking](https://runescape.wiki/w/Cooking_burn). Baking retains the existing local0.4 multiplier: the cache base40/143/180 becomes16/57.2/72 before the normal server XP pipeline.

Baking is a cancellable ActionManager action with current object/reach, fixed origin, movement and capacity checks on each completion. Bread stops burning at37. The cake/pizza burn curve remains an explicitly approximate local policy:50% at the minimum level, decreasing to zero at99. Conditional range bonuses, cooking gauntlets/cape and exact retail cake/pizza burn thresholds are not mapped here. Existing fish/meat/pie Cooking actions remain unchanged.

## Construction and shared material alternatives

`Recipe.withAlternatives` returns a new recipe with defensive copies. The canonical static recipe remains unchanged; each completion resolves the carried material grade afresh. Make-all sums the number of complete batches across allowed grades. Material selection and output commitment happen through the existing atomic exchange, so a previous completion can exhaust bronze nails and the next use rune nails without a separate action implementation.

The flatpack cache's struct33785 authorizes steel1539, bronze4819, iron4820, black4821, mithril4822, adamant4823 and rune4824. The full required nail quantity must be available from one grade for each furniture item; two incomplete grade stacks are not blended into one item. This local bound is shown in material feedback. Grade-dependent nail bending/breakage is deferred. The ordinary choice of any nail material follows the [flatpack calculator](https://runescape.wiki/w/Calculator:Construction/Flatpacks).

Wooden13704, oak13705, steel frame13706, vice13707 and lathe13708 workbenches enforce caps20/40/60/80/99. The current public Furniture workbench139147 admits all45 supported baseline rows. These caps follow [Workbench space](https://runescape.wiki/w/Workbench_space). Player-owned house rooms, placing flatpacks in a house, upgrades to the physical workbench and rebuilding furniture are outside this baseline.

## Smithing audit

Smelting previously retained pre-rework-style910 XP (bronze6.2, rune50, elder rune70). It now uses pinned cache values1,10,26 respectively, with the other nine bars corrected the same way. The existing physical-backpack recipe structs remain authoritative. Core bars take5/4/3 ticks at their documented skill milestones; for example bronze1/2/5 and elder rune90/91/94. Both the initial action delay and subsequent intervals respect these durations. This follows the [current Smithing tables](https://runescape.wiki/w/Smithing) and [smelting XP data](https://runescape.wiki/w/Module:Smelting_urn/Data).

Forging already uses702 pinned current product rows, upgrade inputs, required work, heat/progress gauges, and atomic completion. Those mechanisms were retained. Primal ores/bars and100+ recipes, metal-bank storage, double-bar bonuses, Superheat Form, masterwork, burial sets and exact retail forge reheating remain future work. Silver/gold use the baseline5-tick interval; their special accelerators are not represented.

## Validation and integration

- Isolated Java8 compilation passed for the changed production sources and new helpers.
- JUnit:21 tests passed, including six new cases for grade switching, incomplete alternatives, immutable previews, protected item refusal, overlapping recipe families and full-backpack output failure.
- Initial actual950 encrypted acceptance passed165 ticks /356 decoded frames: tar level gate/Make-all, gem tips, orb assembly, advanced glass, empty vessel returns, bread/cake baking, full-cake inventory refusal, cancellation, alternate-nail Use-on-workbench and tier gate.
- The strengthened `Native950ProductionThirdPassAcceptance` additionally runs all33 new ordinary rows, the encrypted raw-food Use-on-range path, and checks the exact first/subsequent5-tick smelting completions. Run it against the integrated build, with the two shared hooks below; its final result is recorded by the integrating agent.
- `Native950CraftingSummoningAcceptance` now expects117 Crafting rows and recognizes battlestaves as inventory production. Its renewal assertion was coordinated with the parallel Summoning refinement.

Required shared router integration (owned by root): `Native950Cooking.supportsRaw(int)` in item-on-object dispatch, and the overload `Native950Cooking.choice(Player,WorldObject,int)` for the selected raw item. Existing Cookables callers remain valid. No save schema change is required for production.

Acceptance JVM flags: `-Dataraxia.native.verifyCache=true -Dataraxia950.data=<Ataraxia950>/data`; classpath is current compiled classes, current resources and `OpenNXT/runtime/lib/*`; argument is the paired flat950 cache path. These checks use temporary players and no authentication, listeners or account-file writes. Visible menus, animations and feedback still need a live-client playtest; headless protocol acceptance does not assert their pixels.

Originals are preserved under `implementation-backup/skills-thirdpass/production-20260912-120301`, retaining their project-relative paths.

## Final combined deployment validation

Final integrated execution passed all33 new ordinary recipe transactions and actual encrypted Use-on-range routing, with613ticks/956frames. Crafting/Summoning117/161recipe regression passed3044ticks/3486frames; smelting5tick first/repeat timing passed. Complete unit suite1260passed/2skipped. Runtime installed; details in skills-thirdpass-validation.json.
