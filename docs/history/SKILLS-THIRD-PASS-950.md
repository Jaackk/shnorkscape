# Skills: third refinement pass (950)

Installed in950RevTest on12 September2026. This expands ordinary training and shared mechanics using the RuneScape Wiki, the paired950 cache, and the original910 framework. Skills not listed below retain their second-pass behavior; full retail content is still a longer project.

## What to try

| Skill | Improvements and normal test route |
| --- | --- |
| Divination | Right-click energy and choose **Weave**. Eleven permanent boons use current cache costs/levels and improve the existing conversion rewards by10%. Both accepted energy tiers are supported where specified; duplicate unlocks cannot consume another stack. |
| Archaeology | Restore using materials split between backpack and material storage. The workbench also offers withdrawals, limited by free backpack space. The existing two excavation sites/three artefacts remain. |
| Invention | Choose **Research junk reduction** at an inventor's workbench. Nine upgrades at levels34–105 permanently improve ordinary disassembly. **Analyse** reports your adjusted chance. Study currently grants basic discovery XP; the optimisation puzzle is not included. |
| Summoning | Choose **Summon** on an ordinary pouch:85 current-cache companions supported. Interact with your familiar to check/recall/renew/dismiss it. Obelisks restore points. Following, expiry, reconnect persistence, restricted-area suspension and Bunyip healing are implemented. |
| Herblore | Use a clean guam/marrentill/tarromin/harralander with swamp tar while carrying a pestle. Recipes consume15 tar and make15 ammunition. |
| Fletching | Use a chisel on supported gems or oyster pearls to cut bolt tips. Twelve routes use corrected Fletching XP per cut. |
| Crafting | Combine a charged elemental orb with a battlestaff, or use a glassblowing pipe with molten glass for powerburst/bomb vials. Six additional recipes. |
| Cooking | More dough, pie, pizza and cake preparation; vessels are returned correctly. Use bread dough, uncooked pizza or uncooked cake on a range to bake. Tin/empty-vessel space is checked before consuming ingredients. |
| Construction | Flatpack recipes accept the cache-approved nail grades and switch grades between batches. Workbench tiers enforce their construction limits. |
| Smithing | Twelve ordinary smelting bars now use current cache XP and modern5/4/3-tick speed milestones. The XP correction is intentional; the old values were too high. Existing702 forging recipes remain. |
| Ranged | Ordinary darts, knives, throwing axes and javelins consume the equipped stack. The final throw still awards the selected Ranged/Defence XP and stops when ammunition runs out. |

Fishing, Runecrafting, Divination and Invention actions also received stronger cancellation, controller, combat and reach checks. These use the existing scheduler, inventory transactions and XP pipeline.

## Saved progress and testing

Boons, research and remaining familiar lifetime now survive reconnects. Familiar time pauses offline and in areas where summoning is prohibited. Your existing profile and working runtime were backed up before deployment. Older save versions remain readable; rolling back to the old engine requires the matching old profile backup.

Validation:1,260 unit tests passed,2 existing skips, zero failures/errors. Ten actual-cache gameplay acceptance programs passed, including normal encrypted Weave/Summon/research/cooking inputs,367 familiar lifecycle checks,69 combat checks and the existing bank/melee regressions. The19 bridge checks, lobby regression and mandatory cache preflight passed. A disposable copy of your backed-up character migrated with all existing sections preserved; its original bytes were unchanged.

Server and client were restarted for manual testing. Automated checks do not prove every animation, menu pixel or mouse gesture in the real client.

## Remaining limits

Familiar combat, scroll specials, beast-of-burden storage, most familiar passive effects and the full familiar panel remain unimplemented. Invention augmentation, equipment XP and gizmo perks remain future work. Archaeology has not gained more digsites in this pass. Cake/pizza burn rates are a documented approximation; conditional cooking boosts are not yet mapped. Special thrown weapons, chinchompas and poisoned variants remain unsupported rather than silently losing their special behavior.

## References and details

- [Gathering/cache evidence](protocol-analysis/skills-thirdpass-gathering.md)
- [Production/cache evidence](protocol-analysis/skills-thirdpass-production.md)
- [Combat and familiar evidence](protocol-analysis/skills-thirdpass-combat.md)
- [Invention and save compatibility](protocol-analysis/skills-thirdpass-invention-persistence.md)
- [Exact validation and runtime manifest](protocol-analysis/skills-thirdpass-validation.json)

Wiki mechanics references include [Divination](https://runescape.wiki/w/Divination), [Archaeology](https://runescape.wiki/w/Archaeology), [Invention junk reduction](https://runescape.wiki/w/Junk), [Summoning familiars](https://runescape.wiki/w/Familiars), [Crafting](https://runescape.wiki/w/Crafting), and [Fletching bolt tips](https://runescape.wiki/w/Calculator:Fletching/Gems_to_bolt_tips). Each detailed report distinguishes current-cache facts, Wiki mechanics and retained local approximations.
