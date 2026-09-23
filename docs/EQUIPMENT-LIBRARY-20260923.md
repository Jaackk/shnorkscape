# Native developer equipment library

`;;items` now opens the revision950 bank surface as a session-owned, read-only
equipment catalogue. The player receives ordinary inventory items through the
existing capacity/controller checks. No catalogue row is installed in `Bank`,
saved, removed from stock, or shared as mutable state between players.

The user's latest mockup is the visual guide. This candidate uses the real bank
frame, dense grid, scrolling, search field, native tab artwork and quantity menus.
It does not recreate the mockup's larger labelled item cards or move the bank's
search field to a new location. Those are not the authored bank layout.

## Catalogue and ordering

600 catalogue rows representing571 distinct items, including the original40-item testing kit. Some items appear
both in Best and a style/supply category. Generated metadata is curated for
usefulness rather than item count. The current category counts are:

| Category | Rows | Leading equipment / contents |
|---|---:|---|
| Best |40| Existing high-end testing loadouts and shared extras |
| Melee |135| Vestments of havoc, trimmed Masterwork, Leng pair, Ek-ZekKil |
| Ranged |130| Elite Sirenic, Last Guardian, Blightbound pair |
| Magic |125| Elite Tectonic, fractured Staff of Armadyl, Praesul pair |
| Necromancy |54| First Necromancer set, Omni guard, Soulbound lantern |
| Hybrid / Defence |60| Souls/Finality amulets, combat rings/capes, Sliske armour and spirit shields |
| Supplies |44| Food, full-dose potions, runes and Primal ammunition |
| Utility |12| Teleports and useful testing tools |

Cache equipment validation supplies identity, slot, requirements and combat-style
parameters2821/2822/2823,2825/2826/2827 and8898. Tier uses parameter23 with equipment
requirements as fallback. The small editorial layer elevates iconic endgame
families. Groups remain adjacent, armour uses head/body/legs/gloves/boots order,
and matching dual-wield weapons stay together. Exact retail BIS is not claimed.

Notes, bound/lent variants, broken/degraded labels, coloured/dyed duplicates,
cosmetic variants, duplicate doses, batch/minigame consumables, master-cape
clutter and gathering tools are excluded from generated combat lists. The old
explicitly requested dyed/augmented testing gear remains in Best. Equal-name
equipment selects the higher cache tier, not the lower item ID. Smithing upgrade
families retain their strongest version. Groups are never cut midway to fill a
row limit. Lower tiers have reserved space.

Generation scans the cache OFFLINE. Live opening loads the generated manifest,
verifies item hashes, then caches the immutable result. It does not rescan every
cache definition or publish thousands of updates each tick.

## Exact native contract

| Function | Revision950 owner |
|---|---|
| Bank surface / only mount |517 at1477:693; never a second mount at695 |
| Catalogue grid |517:201, client container95; original slot indexes |
| Inventory / worn preview |517:15 /35, containers93 /94; deposits disabled in library |
| Search |517:237, input234/235;13890 ->13903 ->9833/7211;9325 filters original slots |
| Cancel search |238/239 ->13898 ->13909, then9324 redraws the selected tab |
| Tab buttons |517:169 dynamic children2..9;165 is native All Items |
| Tab metadata |45143..45156 counts;45161..45174 ordering;45193..45206 icons;45207..45220 names |
| Selected tab / redraw |45141;8905/8906,5797/5799,9511/9587,9324/9329/9330 |
| Tab artwork |Native enum15585 keys5,1,4,13,33,3,20,29: star/sword/bow/hat/skull/shield/food/scroll |
| Quantities |93/96/99/103/106/114;45189 and111; ordinary item menu13798 |
| Withdraw prediction |6794 ->14362; exhausted or individual actors use48447 |
| Close |517:317 / CLOSE_MODAL; quantity request cancelled, mount and catalogue data retired |
| Title |Exact517:0 onLoad8420 arguments; only title string changed |

The root title call is8420(517:1,517:311,-1,517:317,
"DEVELOPER EQUIPMENT LIBRARY",28241,1017). The native search script can temporarily
use its own Bank-of-Gielinor search caption; cancelling search or selecting a tab
restores the library title. No cache script is patched.

The cache supports14 custom tabs plus All Items. Eight are used. Its authored tab
name enum8657 supplies Combat/Melee/Ranged/Magic/Necromancy/Hybrid/Food/Questing
tooltips; arbitrary persistent labels below icons are not an authored capability.
The mockup's category identity is conveyed with the native bank icons. Tab
creation, deletion, drag reordering, deposits, presets, notes, and alternative
withdraw destinations cannot mutate the library or real bank.

Native search is case-insensitive partial-name search across catalogue rows. It
keeps original source slots, including after filtering. Exact numeric-ID search
is not added to the native name-search script. There is no separate search dialog.

Left click uses the selected native quantity (initially1); explicit1/5/10/saved-X,
custom-X and All use the existing menu. All grants the catalogue default, limited
by capacity: equipment28, ordinary consumables1000, stackables10000. Custom-X can
exceed that display/default quantity up to real inventory/stack capacity. All
successful/refused grants republish catalogue stock without remounting, so native
search and the selected tab survive. Same-tick repeated source actors are refused.

## Isolation and validation

The library never calls `Bank.openBank`, deposit, withdrawal or bank save mutation
APIs. It owns a separate immutable catalogue and per-player quantity/tab state.
Container95 is reused only as a client display namespace. The real-bank observer
explicitly ignores the library mount; close unregisters it before ordinary bank
cleanup, clears temporary client stock, and resets the temporary native tab fields.
Existing developer/LAN permission checks are preserved and rechecked on actions.

Focused tests cover quantities, custom-X above display stock, replenishment,
capacity, augmented actors, stale/foreign slots, cancellation, permission loss,
two-player isolation, bank object identity/preferences and malformed entries.
Actual-cache acceptance checks catalogue identities, complete ordered Torva sets,
adjacent Drygore hands, first-screen armour, resource stacks and clutter exclusions.
Encrypted bank acceptance follows real bank -> `;;items` -> Withdraw5/X -> close
-> real bank withdrawal, and confirms that a world tick never adopts the library
as the real bank. No account file or running game listener is used.

`tools/verify_950_library_search.py` executes the pinned native9325 instructions
with bounded drawing fixtures, proving case-insensitive partial matching and
unchanged source indexes. UI, tab, sprite and item assets are SHA-pinned. Native
keyboard delivery, appearance and responsive resizing remain live visual checks.

Regenerate offline with `Native950EquipmentLibraryAcceptance cache --generate`,
then rebuild. `tools/Native950LibraryIcons.java` exports the bank's own tab artwork
for inspection; it is not replacement UI artwork.

## Delivery

The update is prepared for Jack's self-service **Apply Staged Update.cmd** workflow.
No running server, client, profile, workspace or LAN configuration is replaced.
After applying: check `;;items`, all eight icons/tabs, search for Noxious/Drygore/
Torva, scroll, quantities, closing/reopening, and ordinary bank contents afterward.
Inspect the native title/search caption and short/tall window layouts visually.

Final verification:1472 Java tests, zero failures/errors. Actual-cache catalogue
acceptance passed, including hashes, item identity and grouping; the complete
audit ran in1812ms (including metadata validation and console report), and later
opens reuse the immutable catalogue. Encrypted bank acceptance passed106 input
actions,82 world ticks and2578 output frames. The pinned native search regression
passed. Equipment/ammo acceptance also passed820 frames during this feature pass.

Staged engine SHA256:
`5AAE1032EF34FC0F43F85ECBDB3847ECE29207BE127D2893C7506775DCF0DC80`.
The installer manifest/check-only passed. Old staged files are preserved in
`backups/pre-edit-20260923-014751-634/staged-before-equipment-library`.
The deployed engine remains unchanged at584E19B3C87627C7A7FE2315A02A999CE8844EACA8AC40BD7DE27EDB46367329.
This candidate is AUTOMATED-TESTED and STAGED, not deployed or live visually confirmed.
