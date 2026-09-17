# Skills: second refinement pass (950)

Work completed in the test project on 12 September 2026. Original 910/947 sources and the standalone distribution were not edited. The first-pass training paths remain, with the changes below; this is not a claim that every retail training method is implemented.

## Invention: how to disassemble

Reach base level80 Crafting, Smithing and Divination. The lightbulb/Invention Materials icon then appears below the backpack. Drag an ordinary item onto that icon, choose Disassemble, then choose a quantity. This opens a selection before consuming anything. Clicking the icon also offers stored materials, disassembly and Analyse for eligible carried items. The workbench route remains available; disassembly itself no longer requires a workbench. Walking closes the selection and cancels a running batch.

Ordinary noted stacks use their current950 certificate/base links and consume the noted items. Each action consumes the quantity required by its verified disassembly row; a selection processes at most60 actions. Analyse reports the verified base junk chance, material rolls and possible components. Components cap at100,000 each and junk at2,000,000,000; excess rewards for capped components are discarded, not rerolled. Pre-existing saved balances above a cap are preserved and cannot gain more until reduced.

The 10,844 identity-matched ordinary disassembly rows and eight existing manufacture recipes remain the supported content. Augmented/charged/customised item disassembly, invention equipment XP, gizmos/perks, research-based junk reduction, and the full native materials catalogue window are not implemented by this pass. The pouch currently uses the existing native dialogue menu and chat material summary. The Invention tutorial remains bypassed once the three skill requirements are met.

Mechanics references: [Invention](https://runescape.wiki/w/Invention), [Invention tutorial](https://runescape.wiki/w/Invention_tutorial), [Materials](https://runescape.wiki/w/Materials). The 60-action batch bound is a local processing limit. Wiki facts guide mechanics; interface IDs come from the paired cache and binary.

## What improved

- Farming: compost XP and upgrades while growing; level-sensitive persistent harvest yields; corrected flower/limpwurt yields.
- Archaeology: current-cache mattock requirements, strongest carried usable mattock (equipped priority), precision-driven discovery and a progress gauge.
- Mining: stamina starts at15, capacity follows Mining/Agility, and exhausted mining receives the proper strong reduction.
- Thieving: one successful pickpocket continues automatically until interrupted, caught, or out of inventory capacity.
- Woodcutting and Agility: better collision/reach and safe obstacle cancellation; Barbarian course requires35 Agility.
- Production: menus explain levels, materials, available amounts, tools, XP and batch outputs. Make-all respects available materials; missing tools/materials and full-backpack failures give separate reasons. This benefits existing Herblore, Fletching, Crafting, Construction, Smithing, Cooking and Summoning recipes.
- Crafting: eight ordinary jewellery stringing recipes added (111 total supported recipes).
- Prayer: ordinary bone/ash offerings and recharging at verified surface altars, including Varrock church (3253,3486). Use bones on the altar or choose Offer.
- Magic and Ranged: automatic air-spell tiers and RS3 rune costs; weapon/ammunition damage tier limits.
- Slayer: corrected streak rewards; confirmed paid task cancellation and XP purchases.
- Dungeoneering: a local level20 three-wave frozen challenge through the existing ring/tutor/entrance menu, alongside the ordinary solo run.

The original action scheduler, container transactions, XP progression and save system remain the owners. No new save schema was needed. Existing XP multipliers are retained. Combat remains a basic automatic loop without abilities.

## Detailed audits and practical limits

All skills are accounted for across these reports; some existing paths were reviewed and retained rather than changed:

- [Gathering and exploration](protocol-analysis/skills-secondpass-gathering.md)
- [Production, Construction, Prayer and Summoning](protocol-analysis/skills-secondpass-production.md)
- [Combat, Slayer and Dungeoneering](protocol-analysis/skills-secondpass-combat.md)

## Validation

The combined suite and actual-cache routing checks are recorded in the current handoff. Isolated acceptance players do not write account files or listen for connections. New Invention checks cover noted stacks, stale item instances, cancelled prompts/actions, forged source IDs, protected ingredients, storage caps and locked movement. New production checks cover stringing, full inventory Make-all, routed bone/ash Offer/Use, recharge and removed altars. Runtime visual verification is recorded separately and must not be inferred from headless tests.

Source backups: implementation-backup/skills-secondpass. Build and routing logs: logs/build-ataraxia950.log, logs/secondpass-invention-routing.log, logs/secondpass-production-routing.log.
