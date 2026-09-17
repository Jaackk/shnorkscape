# Current-cache equipment, appearance and backpack Wear routing

This milestone replaces equipment capability based on old item names or small ID lists with the current950 definitions and assets. The shared native transaction resolves slots, conflicts, quantities and skill requirements through `Native950EquipmentTypes`. This note describes the appearance and menu work; metadata/requirement audit logs sit beside it.

## Appearance and movement

`GlobalPlayerUpdater.buildNative950AppearanceData` resolves every equipped item through the strict current-cache equipment type. It checks the actual occupied slot, gender asset validity and both hidden/displaced slots before serializing. Full helmets hide hair/beard where their own cache definition says so; bodies can hide arms; a two-handed weapon hides the offhand. A conflicting staff plus shield cannot become a malformed client body: the previous valid body is retained and the failure counter/reason records the withheld update.

The existing verified950 body format is unchanged: unsigned LEB128 slots, item base0x800, kit base2, sixteen transmitted slots from the nineteen-slot body mapping, no synthetic customization, and the existing PLAYER_INFO byte transformation. The current cache's model index47 is used. Every declared nonnegative worn model must exist. A -1 model is a valid absent contribution, including gender-specific cosmetics and nonvisual ring/ammo/pocket slots. No male model is substituted for a missing female model.

`Native950EquipmentAnimations` resolves normal/combat movement sets from item params2954/2955 or the selected raw current-cache combat profile (item3000, otherwise686), with param644 compatibility where no modern stance was supplied. Absent/default-1 movement uses2699 normal or2688 combat. A present missing or malformed profile is rejected. Only hands need weapon profiles; unrelated capes, armour, ammo and pocket items do not depend on them.

Every resulting BAS (index2/archive32) and every sequence it explicitly declares (index20) is strictly decoded and must exist. The legacy BAS decoder has zero-valued defaults for some omitted sequence fields; the validator uses its opcode trace so omitted fields do not invent sequence0 dependencies. Explicitly declared sequence0 is still checked. Existing verified agility movement overrides remain in place.

Example: Masterwork staff58486 has slot3, hide5, male/female models135664 and135981, Magic99, and item686=14937. It has no param644. Current struct14937 provides2954=2697 and2955=2689, so the staff now uses its staff movement sets rather than unarmed fallback. These are cache lookups, not a per-staff admission table.

`Native950EquipmentAppearanceAcceptance` was run by the parent against the actual cache and parsed54 real serializer outputs. It covered male/female staff, full helmet, platebody, legs, offhand shield, arrows, ring, book, gender-specific robes/cape, wings and pocket gear, normal/combat movement, a combined outfit and rejected staff/shield conflict. This validates serialization and asset dependencies; rendered movement quality is checked with the client. `Native950EquipmentAnimationsTest` covers malformed/missing BAS and sequence dependencies, including explicitly declared versus absent sequence0.

## Equipment-only backpack captions

The cache owns menu captions. The server must translate the selected UI operation back to the caption's cache option before deciding whether it is Wear/Wield/Equip. `equipmentCacheOption(Player,itemId,uiOperation)` does that, validates the equipment type, and returns0 for every other action. It is deliberately separate from the ordinary-menu gate used by Drop and other gameplay operations.

The pinned call chain is12090 ->2833 -> specialized builder or2410. Ordinary2410 positions are UI1,2,3,7,8 for cache options1..5. Script2833 selects its hardcoded native menu cases first, then param6799, param4840, category, and ordinary fallback. The Java hardcoded branches mirror this native caption selection only; they do not grant equipment capability.

| Selected script | Equipment caption handling |
|---|---|
|2410|Ordinary1,2,3,7,8 mapping|
|6468|First two cache captions swapped; common cacheoption2 Wear is UI1|
|18401|Excalibur first two captions swap only when its native setting is enabled|
|7031|Both quest branches preserve cache2/3/5; only actual Wear labels accepted|
|2383,1520|Both quest/event branches preserve cache1/2/5; conditional captions are not inferred|
|12405|Ordinary except cacheoption4 is absent|
|13505|Cache1 and5 preserved; other dynamic skill captions are not inferred|
|16473|Backpack argument-1 retains ordinary captions; native argument1 would suppress all|

Backpack12090 instructions8..12 pass `(item,1,slot,-1)` to2833. The last argument reaches18401, whose instructions0..10 swap if argument1 or `(varbit54934==1 and argument==-1)`. In this backpack path, the swap therefore depends only on varbit54934. Its actual definition is domain0, varp2180 bit4. The server reads the authoritative per-player variable; it never treats both Activate and Wield as equipment.

The raw-cache audit found all special wearable families in6468,18401,7031,2383,1520; common examples are slayer helmets/rings, clan capes, explorer rings, runic staffs, Excalibur, blisterwood stakes, old necklace and tainted shard. Unknown specialized menu branches remain rejected rather than guessing a caption. Notes/shards cannot gain Wear from an inherited label because equipment type admission rejects them.

Eight specialized script hashes and the Excalibur varbit hash are verified by `Native950CacheContent.verifyEquipmentBindings`. The script resource now has94 actual rows. Complete normalized instructions, hashes and footer/switch bytes are recorded in `equipment-menu-scripts-950-evidence.json`. Run `tools/verify_950_equipment_menus.py` for a read-only paired-cache check.

## Combat boundary

Equipping arbitrary valid gear does not imply that its combat style, abilities, special effects, set bonuses or degradations have been ported. The native RS2 melee loop still validates its supported weapon and classic bonus contract. Its old six-partyhat ID exemption has been replaced by a generic current-cache validated equipment check with the complete already-reviewed neutral parameter schema: exactly2195=7,624=1,537=4191. Actual equipment slot must match too. Unknown parameters are not silently treated as zero combat stats. This narrow neutral schema is not a claim that all current-cache combat metadata has been implemented.

## Additional read-only Remove finding

Current script8471 instructions1557..1565 emits ordinary operation1 Remove only when itemparam2091==0. A nonzero value suppresses that caption. Some special slots inspect custom Remove labels separately. The older source also checked1430, but that gate was not found in the reviewed950 equipment initialization scripts and should not be assumed without evidence.

## Transactions and validation (completed September12)

`Native950EquipmentActions` replaces the native call through the old item-specific Wear/Remove handlers. It validates the exact current source, activity/lock/teleport state, cache Wear option, cache models, and six typed skill requirement pairs. Requirements compare stored XP against each skill's verified curve, so temporary boosts do not bypass them and virtual120 cape requirements work above the displayed skill cap. Controller equip/remove/inventory gates remain; player state and XP are checked again after callbacks.

`Native950Containers.EquipmentChange` stages both containers before committing. It handles symmetric secondary-slot conflicts, whole ammunition stacks, same-item merges, integer overflow, full-bag two-handed exchanges, clicked-slot replacement, and exact identity/quantity revalidation. A refused exchange leaves the original item slots intact. Native equipment observation publishes both ID and quantity changes. Equipment stack quantities survive RAM save/restore and the existing on-disk schema; a catalog slot/stackability check still validates restored equipment. Ordinary removal respects current param2091's removal lock. It does not invoke the old item-specific charge/Invention/bonus managers.

`Native950Banking` uses the real original Player Bank/Inventory storage through staged native transfers. Deposits resolve authored current note-to-base links and respect exact current bank restrictions (59==1 or1047==1); withdrawals target the backpack, including coins. No old custom910 ID range or per-item withdrawal exception grants/denies admission. Exact actor/slot prediction, quantity-X ownership, controller gates, source revalidation, stack overflow and full capacity remain enforced. CacheItems resolves valid note/lent/bound/shard templates instead of rejecting all of those families. Native item identity bounds consistently match u24 id+1; raw cache stackability replaces the old ID0 exception.

Final validation:1124 unit tests passed,2 existing skips, zero failures/errors;18 launcher checks and94-script startup preflight passed. The exhaustive cache audit decoded all63,414 definitions without unresolved templates and admitted16,596 named wearable definitions;59 unnamed/internal wearable placeholders were excluded. Real Bank entrypoints passed51,419 deposit/save/withdraw round trips and respected6,411 cache-authored restrictions. Separate encrypted equipment136frames, bank30actions/35ticks/479frames, appearance54bodies, melee449ticks/1531frames, rewards117frames and previous-partyhat136frames checks passed.

The staff acceptance proves Magic99 rejection (including a boosted current level), controller XP/activity changes, successful two-handed equip, full-bag refusal, both displacement directions, stale packet refusal, body serialization, ammunition merges/removal and saved quantities. It uses current Rune armour45543/45541: older1127/1079 are correctly Convert-only in this cache and are not wearable.

Explicit developer diagnostics: `;;nxt level <skill ID> <level>` now supports all29 skills within their normal level caps, only on the existing authorized local development path. `;;nxt level 6 99` lets the user deliberately set Magic99 for the staff test. Login never grants or alters levels automatically.

Full validation details: [validation-systemic-equipment-2026-09-12.json](validation-systemic-equipment-2026-09-12.json). Final probe logs use `logs/systemic-*-final.log`. The installed runtime, bridge, latest saves/logs and prior handoff were backed up before deployment. Live acceptance completed: the user confirmed the requested Masterwork staff Wield/walk/Remove and correct appearance/movement check ("yes it works").
