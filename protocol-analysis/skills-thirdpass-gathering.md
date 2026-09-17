# Gathering and exploration — third refinement pass (950)

Date: 12 September 2026. Changes are confined to `950RevTest`. Original 910 reference: `C:/Users/developer/Desktop/Ataraxia-PS/content/com/rs/game/player/actions/divination` and the original Fishing/ActionManager infrastructure. No new scheduler, cache replacement, item-ID remapping or excavation save-slot reordering was introduced. Backups preserve project-relative paths under `implementation-backup/skills-thirdpass`.

## Implemented

### Divination: permanent boons through energy Weave

The existing original-910 conversion code already applied a boon flag but the native client had no way to create boons and native skill snapshots did not record the flags. The inventory energy **Weave** action now offers the appropriate currently locked boon choices. Selecting a boon consumes its selected ordinary energy stack, grants the recipe's base XP through the existing XP multiplier, and permanently enables the tier's existing 10% conversion XP/energy benefit. Crafting does not create a useless inventory token: the 950 boon items are recipe definitions with no activation option.

Eleven ordinary boon tiers are supported, flickering through incandescent. Requirements, energy amounts, base XP and accepted alternative energy IDs are decoded from SHA-pinned 950 boon items/structs. There are 23 accepted recipe/energy combinations; the brilliant boon accepts elder energy as well as lustrous/brilliant, exactly as the cache specifies. Elder-energy harvesting and the separate elder boon remain outside the existing twelve ordinary-memory tiers.

A selection keeps the exact original inventory object, quantity, origin and controller. Level/cost checks repeat at completion. Cancelling, walking, combat, replacing the same-ID stack, changed quantities, custom/charged resources, duplicate unlocks and controller vetoes cannot consume energy or grant an unlock/XP. Choices for previously unlocked boons disappear. The original twelve `MemoryInfo` ordinal slots are unchanged; helper snapshots clone arrays and reject unversioned shape changes. Root integration owns the bounded nested skill-save v3 migration and legacy snapshot compatibility.

Mechanics checked against [Divination](https://runescape.wiki/w/Divination), [Divination training](https://runescape.wiki/w/Divination_training), [Bright energy](https://runescape.wiki/w/Bright_energy) and the [Divination product table](https://runescape.wiki/w/Transmute). Direct Wiki access was robots-blocked; indexed Wiki excerpts supplied the mechanics, corroborated by paired-cache recipe metadata. No OSRS mechanics were substituted.

### Archaeology: usable material storage and mixed-source restoration

Restoring any of the three existing verified artefacts now takes materials from the backpack and material store together. Carried materials are consumed first, allowing restoration to free inventory slots. The damaged artefact remains a required backpack input. A single inventory exchange grants the result before the corresponding saved storage debit; failed requirements or an exchange refusal leave stored materials untouched. Every batch rechecks its current recipe/material plan.

The workbench menu now also offers withdrawals for each nonempty supported material type. Withdraw-all returns only what fits and retains the rest in storage; full bags and unknown material IDs cannot lose stored quantities. Missing-material messages show the required amount alongside backpack and stored counts. A retained cancelled restoration action cannot award later.

References: [Archaeology](https://runescape.wiki/w/Archaeology), [Material storage container](https://runescape.wiki/w/Material_storage_container), [Archaeology tutorial transcript](https://runescape.wiki/w/Transcript:Archaeology_tutorial), [Artefacts](https://runescape.wiki/w/Artefacts). The mixed-source transaction is a local usability improvement consistent with using gathered materials to restore; the Wiki transcript specifically establishes direct consumption from storage. No claim is made that the dialogue menu duplicates the retail storage interface.

### Fishing, Runecrafting and Divination: action ownership

Fishing now rechecks collision-aware adjacent reach and the actual native NPC menu on every attempt. It stops when the NPC becomes uninteractable, the controller changes, combat begins, the action is stopped, or the original spot/player moves. It cannot begin during the arrival update. The original sixteen admitted fishing methods, level-sensitive success rolls, tool/bait requirements and atomic bait/catch transactions remain.

Runecrafting altar actions and Divination harvesting/conversion similarly capture their controller and reject combat or cancelled action callbacks. A stopped Divination harvest releases only its own wisp-wandering lease, and later calls cannot award resources. The original thirteen altars, runecrafting multipliers, twelve wisp tiers and conversion formulas remain. References: [Free-to-play Fishing training](https://runescape.wiki/w/Free-to-play_Fishing_training), [Rune essence](https://runescape.wiki/w/Rune_essence), and the Divination sources above. These changes concern server action ownership and target validation; no unsupported retail catch-rate formula was introduced.

## Paired-cache evidence

- Item index 19: boon recipes `29373..29383`; ordinary energies `29313..29324`; elder energy `31312`.
- Boon metadata: `2640/2696 = 26` (one-based Divination skill), `2645` required level, `2665` energy cost, `2697 / 10` base XP, `2675` alternate ingredient struct. Names and ordinary tier levels are checked.
- Struct index 22: `22975..22985`, ingredients at parameters `2655..2664`. Structure `22982` contains `29320,31312,29321` for the brilliant boon.
- Exact SHA-256 bytes are in `Ataraxia950/resources/native950/runecrafting-divination-assets-950.properties`, now supporting struct index 22 / shift 5 alongside existing object/NPC/item/sequence/graphic pins.
- Archaeology reuses existing pinned materials `49444,49445,49460,49514`, damaged artefacts `49741,49921,49923`, restored items `49742,49922,49924`, and workbenches `115421,125133`. Recipe inputs/amounts/levels/XP remain cache-derived; both excavation snapshot slots and all 64 storage slots retain their previous identities.

## Validation

- Isolated Java 8 compilation of the modified skill classes and focused acceptance probes passed without changing the runtime JAR or running processes.
- `Native950RunecraftingDivinationAcceptance`: PASS. All eleven boon recipes and 23 energy alternatives, cost/level/unlock/XP checks, duplicate prevention, cancelled/replaced/modified item refusal, controller veto and exact 10% conversion XP effect. Additional altar/harvest/conversion callback and combat/controller checks pass, alongside all thirteen altars, six actual-map gateways, twelve wisp tiers and three conversion modes.
- `Native950FishingCookingAcceptance`: PASS. Stopped callback, controller replacement and combat checks plus all existing actual-cache tool/bait/level/full-bag/catch/XP/cooking regression checks.
- `Native950InventionArchaeologyAcceptance`: all new mixed-source, full-backpack, storage-withdrawal and cancellation checks passed, as did existing mattock progress checks. Its final old Invention remote-disassembly assertion exposed a separate missing origin guard; root owns that correction and the final combined rerun. Do not mistake the intermediate partial run for a complete pass.
- `Native950InventionRoutingAcceptance` now also exercises normal encrypted backpack operation 1 for Weave and Summon. It validates the exact native choice-script payload, no pre-confirmation consumption, energy removal/XP/unlock, skill snapshots, stale/duplicate selection refusal, and owned familiar creation/pouch removal/logout cleanup. Its combined compiled rerun remains with root.
- New `Native950DivinationBoonsTest` covers independent snapshot/restore copies, preserving existing first/last flags, empty legacy defaults and rejecting truncated/extended unversioned arrays. Root owns combined JUnit run and save round-trip checks.
- Logs: `logs/thirdpass-Native950RunecraftingDivinationAcceptance.log`, `logs/thirdpass-Native950FishingCookingAcceptance.log`, `logs/thirdpass-Native950InventionArchaeologyAcceptance.log`.

These probes use temporary embedded clients with no authentication, listeners or account writes. Client pixels and live animation behavior still require user verification.

## Retained boundaries

Woodcutting, Firemaking, Mining, Thieving, Hunter, Farming and Agility retain the second-pass mechanics described in `skills-secondpass-gathering.md`; no new harvesting methods or unrelated probabilities were added to them. Further work remains for toolbelts, resource spirits, rockertunities, fishing spot schedules, traps/barehanded Hunter, Farming disease/tree patches, additional Archaeology excavation/restoration sites, and modern course rewards. Boons do not add transmutation, portents, divine locations or the full native Weave catalogue. Archaeology still has two excavation sites, three restoration recipes, four material types and the existing generic digging animation.

## Final combined deployment validation

Final combined rerun passed: Native950InventionArchaeologyAcceptance502ticks/603frames, including all new mixed-storage/withdrawal checks and corrected Invention origin guard. FishingCooking and RunecraftingDivination passed. Normal encrypted Weave/Summon/research routing passed within407ticks/700frames. Boon save migration and full1260-pass/2-skip suite passed. Runtime installed; details in skills-thirdpass-validation.json.
