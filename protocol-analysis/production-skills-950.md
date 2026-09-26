# Ordinary production skills, 2026-09-12

The new Native950Production adapter consumes original910 Fletchables, GemCutting.Gem, HerbCleaning.Herbs and Herblore.Ingredients/RawIngredient tables. It exposes243 recipes across Fletching, gem Crafting, herb cleaning, ordinary mixing and grinding. Original material quantities, levels and base XP are retained, including15-arrow batches and log-tier shaft counts. Quest overload/multiple-potion, tar, specialist Slayer/dungeoneering and variable-output recipes are excluded rather than treated as ordinary two-item conversions.

Native950ProductionAction runs under the original ActionManager. The shared Recipe stores copies of inputs/outputs/tools, and completion calls Native950Skilling.exchange before Skills.addXp. Native950Containers stages the full exchange, validates the selected catalog and stack limits, and commits only when every input/output fits. Controller vetoes run before commit. Inputs cannot be empty; gathering without bait instead uses the already atomic reward path. No new timer or alternative inventory is introduced.

Native950ProductionMenu is session-owned. It renders up to three products, More choices and Cancel on cache-verified1188, then Make1/5/10/all/Cancel. A one-shot Choice can select a mode such as Cook food/Add logs. Replies must name the actually visible, unconsumed1188 target. Closing any owned dialogue clears the menu. Every repeat rechecks player state, level, tools and inventory. Environment-dependent Cooking/Fishing/furnace actions also recheck the live entity and collision reach.

Item-on-item69 validates both backpack1473:5 endpoints, exact slot IDs, distinct slots, and same-tick changed-slot barriers. Actual950 inventory verbs (Craft/Fletch/String/Tip/Feather/Make/Mix/Grind/Powder/Clean) only expose corresponding supported recipes; metadata alone does not enable unrelated Wear/Drink effects. Validated production options merge with existing mining carry metadata so uncuts retain Craft. Prayer options are merged independently.

## Evidence

- production-assets-950.properties pins396 actual950 item definitions and38 sequences (a removed quest-only recipe leaves a harmless extra asset pin).
- production-items-950.tsv lists actual950 names and menus. All396 IDs were present in the original910 reference dump. Name changes were reviewed: shieldbow to longbow, unf/u to unfinished/unstrung, arrow shaft/headless arrow to shaft/headless shaft, and equivalent material/item wording; no global ID-validity relaxation is applied.
- production-sequences-950.json compares every sequence to910:10 are byte-identical,34 retain identical frames and all38 retain timing. Gem sequences mostly differ only in opcode ordering; hydrix removes a priority opcode. Four fletching sequences use updated frames while retaining the fletching held-item/knife roles and duration. Bow-stringing changes sound fields. These findings establish asset roles, not observed client rendering.
- Native950ProductionTest covers full-bag replacement, failed multi-output rollback, duplicate-input accounting, stack overflow/preview immutability, controller refusal, quantity bounds, tool retention/loss and cancellation.
- Native950ProductionAcceptance exercises encrypted native actions through the production menu, original scheduler, resource use, native UPDATE_STAT and save capture. See production-acceptance-950.md.

The user-facing playtest checklist is retained in the owner-local history. Smelting, Fishing/Cooking and Prayer have independent cache admission, original-data provenance and acceptance documents in this folder.
