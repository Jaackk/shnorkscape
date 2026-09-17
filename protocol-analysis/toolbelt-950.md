# Native950 surface toolbelt — 12 September 2026

Implemented by `Native950Toolbelt` and `Native950ToolbeltUi`. This replaces the unavailable legacy toolbelt adapter for native950 skill checks; it does not weaken the old947 cache guard. Existing files were backed up without overwriting to `implementation-backup/toolbelt-production-ui`.

## What now works

- Every native950 character starts with the same 35 explicit ordinary basic tools, including bronze pickaxe/hatchet, tinderbox, knife, hammer, fishing tools, common moulds, and farming tools. Restoring an older save seeds only this fixed basic list. No Slayer unlock, Invention progression, mattock, quest tool, or upgraded tool is granted incidentally.
- Ordinary inventory `Add to tool belt` actions use the actual current item's option, current skill/level parameters770/771, exact source object/quantity, and ordinary item-state checks. Noted, lent, bound, charged, augmented, modified, unsupported, stale-slot and insufficient-level items cannot be consumed as tools.
- Replacing a removable upgraded tool returns the old one atomically to the backpack. A full backpack can swap because the consumed item frees its slot. Removing a tool requires capacity and controller permission. Free basic bronze tools cannot be extracted into ordinary inventory items. A reentrancy guard prevents a controller callback from recursively removing the previous tool while a swap is in flight.
- Mining and Woodcutting select usable stored tools; hatchet requirements now use current item parameter771. Firemaking uses the automatic tinderbox. Fishing accepts belt tools but still requires and consumes physical bait. Farming actions/harvests use stored tools. Archaeology checks equipped, carried, then stored usable mattocks. Shared production tool checks are integrated by the production agent, preserving physical ingredient consumption.
- Root integration persists sorted exact held IDs through nested skill progress version4. `validateSnapshot` is independent of a live cache, rejects malformed order/duplicates/unsupported identities/conflicting families, and clones input. Maximum77 held families; root's wider wire allocation bound256 is narrowed by validation. `restore` and `snapshot` do not alias caller arrays.

## Current950 cache evidence

The paired cache supplied with this project is the authority for IDs and scripts. `resources/native950/toolbelt-950.json` and `protocol-analysis/toolbelt-cache-950.json` contain77 surface tool rows,167 variants, and516 SHA256 pins covering interface components, item definitions, category/upgrade enums, structs, varbits, scripts, and the equipment entry's DB row.

- Surface enum13730 -> category structs3915,3934,43554,4576,4999,5161,29546 -> parameter6979 category enums13731..13736/12938 -> row structs parameter6980 item. Seven categories have22/8/15/6/13/3/10 rows respectively. Row parameter7765 marks removable families.
- Pickaxe enum2433 has55 entries and uses varbit18521. Hatchet enum6397 has17 entries and uses18522. Mattock enum12936 has14 entries and uses45999 with value=enum key+1. These replace stale910 pickaxe IDs and the old43044 assumption.
- Script7090 tests ownership;14090 chooses the upgraded displayed tool. Machete/enchanted secateurs and fixed tools use their individually verified ownership/upgrade vars. Unmanaged Invention vars30224/30225 and Archaeology quest var46463 are deliberately left to their owning systems.
- Interface1944 has103 current components. Script14097 accepts one integer:0 for the surface belt.14098 builds categories/scrolling;14109/14110 build tool headings and hitboxes. Component7 is the77-row action surface. Component27 receives server descriptions after14099 writes `Loading...`. Component102 closes the native frame. The ordinary central host is1477:735 with wrapper732; no oversized fixed canvas is imposed.
- Equipment footer:8472 maps1462:33;8471 calls16559(1462:35,3); enum5134[3]=5137; row ordinal2 points to DB row1200 (`2/41/1200`) named `Tool belt`.16555/16557 create actor2 with child1 carrying option1 via16563. Current routing accepts1462:35 slot4353 (`0x1101`) option1,item=-1. Native constructor0x1401d7ecd reads actor+0x100/child+0x104 and packs positive actors as `((actor+15)<<8)|child`;0x140269e4c stores this at widget+0x1c. IFbutton0x1401a9675 reads that field and passes it to writer0x1401a9980, which writes it as the final big-endian ushort at0x1401a9aa8. Thus actor2/child1 means4353 on the wire; it is not ordinary slot2. Actual rendered clicks still require a client check. No old1462:40 ID is reused (950 has only components0..36).
- Native category switching and scrollbars are client scripts. The port does not rebuild the interface on each click. It answers tool description and removal events. Special settings parent12 is hidden until those mechanics exist:17 is its hitbox, while sibling19/20 owns the visible gear artwork. Free basic tool descriptions never offer removal or imply that replacing a free bronze tool returns an inventory item.

## Validation performed

Isolated Java8 compilation of changed helper/UI/gathering sources succeeded. `Native950ToolbeltAcceptance <cache-directory>` passed **58 actual-cache checks**: default tools, no unearned tools, Mining/Woodcutting/Farming/Archaeology lookup, strict saved state and cloning, level/stale-slot/charged-instance refusal, full-backpack replacement and removal, exact returned item, controller vetoes, reentrant callback protection, unsupported profile refusal, optional Rock hammer storage, native open/event/detail/remove/close packets, stale/out-of-range UI refusal, and real routed equipment-toolbelt to Hero/Skills replacement that retires the old owner without closing the new guide.

The Firemaking regression was updated to prove that an absent carried tinderbox still lights one log with the basic belt, consumes exactly one log, creates one fire and grants the established XP; existing missing-log/refused-world cases remain unchanged. Root owns full project compilation, the combined test run, deployment, and live verification. Acceptance uses disposable players and no character save files. It is not a rendered-client or actual mouse-input test.

## Deliberate limits

This pass supports the **surface** belt, not a Dungeoneering belt. Optional Slayer-point unlock consumers (Bonecrusher, Seedicide, Charming imp, Herbicide, Gold accumulator), Invention special tools, quest-driven controls, keyring contents, and special item actions are not implemented by this helper. Existing Invention/Archaeology state is not overwritten.

All167 current item identities are recorded, but a pickaxe/hatchet can only be added if the existing native skill adapter has its verified action profile and animation. Newer unsupported Primal/Gilded/+ variants are refused before consumption instead of being stored and silently falling back to a different model/tool. This is an explicit admission limit, not a complete modern pickaxe/hatchet expansion. Mattocks use the already verified Archaeology profiles. Augmented tools are excluded. Unavailable rows remain visible in the native catalog with honest descriptions.

## References

Current-cache scripts/definitions above are the technical source of truth. RuneScape Wiki references were used for basic ownership and ordinary removable-tool behavior:

- [Behind the Scenes — April2012](https://runescape.wiki/w/Update:Behind_the_Scenes_-_April_(2012)): automatic basic toolbelt tools and the separate Dungeoneering belt.
- [Patch Notes — 7January2019](https://runescape.wiki/w/Update:Patch_Notes_(7_January_2019)): ordinary non-augmented pickaxe removal, except the free bronze pickaxe.
- [Returning players guide](https://runescape.wiki/w/Catalyst_League/Returning_players_guide): automatic basic tools, equipment interface access, removable tool upgrades, and augmented tool exclusion.

The fixed free-default IDs are1265,1351,2347,1755,946,590,8794,233,975,2575,2576,2574,1735,10150,47718,307,13431,303,305,309,311,301,11323,1733,1785,1595,11065,1597,1592,5523,5341,5343,952,5325,5329. Their paired-cache row descriptions are automatic/basic entries; CS7090 explicitly treats the bronze baseline and incense burner as available.
