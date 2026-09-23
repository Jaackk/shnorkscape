# Developer Equipment Library: global search and combat loadouts

The original native-bank library is live-confirmed by Jack. This follow-up keeps
its interface components, artwork, 600 curated rows / 571 distinct items and
catalogue ordering unchanged. This update is staged for self-service installation;
its new search/preset behaviour still needs a native-client visual check.

## Global native search

The separate offline-generated search index contains 39,212 cache-backed item
definitions. Membership does not depend on the curated tabs. It includes ordinary
resources, tools, cosmetics and other valid items omitted from the curated view.
Definitions must decode successfully and have usable names, stack metadata and,
for equipment, supported slots/models. Null/placeholder/broken definitions and
note/lent/bound/shard templates are excluded. Matched definitions are SHA-checked
against the actual cache before being offered.

The existing bank search input handles case-insensitive partial names and exact
numeric IDs (also `id:1511`). It accepts up to 80 characters in library mode.
There is no dialogue substitute or larger visible catalogue. Cancellation restores
the selected curated tab. The library still owns an independent container 95;
searching and withdrawing catalogue stock never accesses the real bank.

The first 100 matches are displayed, with a refinement message for broader
queries. The authored bank supports 1,820 source slots. Existing 600 curated slots
retain their identities; additional results receive permanent slots for that open
session. Slots are never reassigned between queries, and only current visible
results can be withdrawn. After 1,220 distinct additional results have been
allocated, close/reopen the library to reset the slot budget. This prevents stale
clicks from granting a different item. Exact-ID search accesses duplicate-name
items without depending on their position among broad matches.

## Developer Combat Loadouts

Native Presets opens the authored preset controls and inventory/equipment previews.
The nine built-ins use the strongest usable cache-tier weapon choice requested by
Jack, rather than assuming normal endgame families are always strongest.

| Loadout | Armour | Weapons |
| --- | --- | --- |
| Melee — Best | Vestments of havoc, Gloves of passage | Primal longsword / off-hand, T99 |
| Ranged — Best | Elite Dracolich | Masterwork bow, T100; Primal arrows |
| Magic — Best | Tumeken | Celestial catalytic wand / orb, T99 |
| Necromancy — Best | Visage of the First Necromancer and First Necromancer robes | Entropic guard / lantern, T99 |
| Melee — T80 | Torva | Chaotic rapier / off-hand, T80 |
| Ranged — T80 | Pernix | Chaotic crossbow / off-hand, T80; royal bolts |
| Magic — T80 | Virtus | Virtus wand / book, T80 |
| Tank / Defence | Achto Teralith | Primal longsword and divine spirit shield |
| Boss Testing | First Necromancer setup | Entropic pair; additional antifire/antipoison |

All applicable wearable combat slots are filled: head, cape, neck, body, legs,
gloves, boots, main hand, off-hand when compatible, ring, ammunition/resource slot
and pocket. A two-handed weapon correctly leaves off-hand empty. Aura activation,
wings and reserved body slots are not invented or overridden. Accessories include
Igneous Kal-Zuk, Am-hej or Essence of Finality, appropriate style rings, resource
pouches/nexus or spike harness. Non-necromancy setups include Erethdor's grimoire
and torn pages; Necromancy keeps the Underworld Grimoire used in the player's
existing setup. This does not implement new book activation or combat effects.

Item IDs, requirements, style, weapon tier, ammunition family, conflicts and models
are validated against the actual cache. For example, the Primal crossbow's
requirement is 99 but its combat tier is 85, and Tumeken's Light is a melee weapon;
neither is selected based on name or required level alone. "Best" describes the
chosen developer setup, not a claim about every encounter's optimal DPS.

Jaxa's bank was inspected read-only for familiar equipment and supply choices.
No account contents are included in source control. Supplies fill 28 slots with
large rune/ectoplasm stacks where appropriate, overloads, restores, prayer renewal,
Saradomin brews, adrenaline and sailfish. Resources stay in inventory; existing
runepouch/nexus stores are never rewritten.

Load banks every displaced inventory/equipment item before granting generated
stock. Preparation validates all three containers, capacity, integer overflow,
item state, wear requirements and current activity. Commit rechecks original item
identities and quantities. Full bank, unsupported charged/custom item state,
controller activity or a stale transaction refuses without deleting property.
Real bank presets and bank quantity/note preferences are never overwritten.

Three Developer Custom slots use the native Overwrite control. They copy ordinary
current equipment/inventory into the library owner's separate session storage.
They survive closing/reopening the library but expire on logout. Charged,
customised or invention-bearing item state cannot be cloned. Built-ins cannot be
overwritten. Partial-load/BoB/name-edit operations are disabled in this mode.

## Exact native integration

The UI remains interface 517. Presets tab 153 calls script 13746. Native clickable
preset rows are dynamic children 1–12 of component **267**, not background 265.
Component 84 loads, 303 overwrites custom slots, 86 returns to the bank view.
Scripts 13943/13945/13946 rebuild the existing previews and list. Script 15906's
switch table maps slots 1–10 to paired containers 706/707 through 724/725 and
slots 11–12 to 896/897 and 898/899. These bindings and 30 related scripts are pinned.

Five script extensions are narrowly guarded by static label 517:73 reading
`Developer loadout:`. The label returns to `Load Preset:` before closing.

- 13903: native search begins, and library input length increases.
- 13905: changed native input sends a prefixed string query to the server.
- 13909: search cancellation notification.
- 15897: developer preset labels for slots 1–12.
- 6963: developer preset availability for those slots only.

Ordinary banking follows the original instructions. Original instruction operands,
relative branches and switch targets are preserved and tested. Only exact base or
approved patched hashes pass the UI/cache gates; unknown or mixed bridge revisions
are rejected. No interface/sprite/item cache group is modified.

## Verification and installation

Automated coverage includes global non-curated search, partial/case-insensitive and
ID matching, stale source slots, search cancellation, two-player isolation,
transaction capacity/overflow/stale-state refusal, property conservation,
native preset selection/Load/Overwrite handlers, duplicate-click prevention,
real-preset identity, activity/level refusal and ordinary banking after closing.
Actual-cache acceptance applies all nine loadouts to ephemeral in-memory players;
it does not log in, listen or load/save an account.

Four Python bridge tests execute the guarded programs and verify preservation of
ordinary-bank instructions. The five staged scripts were also read by the real
cache reader, converted through PrepareClientCache (45 indexes / 28,579 groups),
and independently decompressed from the generated native SQLite cache with matching
payload hashes. This is not a substitute for the final visual check in Vulkan.

The staged candidate is `dist/library-followup-20260923`; only `cache-v4` is current.
It includes the engine, three unchanged bootstrap classes, five script groups and
index 12's reference table. The apply manifest pins all ten files and original
cache hashes. The installer requires stopped clients/server, backs up runtime,
cache and account/workspace files, and rolls back file replacement on failure.
Play.cmd then refreshes the native script cache automatically.

Patched reference SHA256:
`21AAE886E340146ED851949C0F900FAE44208BE899E305E7331C12F1D6F44E89`.

After applying, check a non-curated item (`logs` / `1511`), cancel back to a combat
tab, preview and load a built-in, save/load a custom slot, and return to the normal
bank. Check native labels, previews and item appearance in the live client.

Final results: 1,481 Java tests, zero failures/errors; all nine real-cache loadouts
and native handler acceptance passed; encrypted bank regression passed 106 actions,
82 ticks and 2,619 frames; equipment/ammunition regression passed 820 frames; full
cache preflight passed against the staged cache. Installer check-only verified all
ten pinned files. The live engine and cache reference hashes remain unchanged.

Staged engine SHA256:
`46AC9972B84CBE778E68FA58BE09B52241EEE270BE8C6C5191C24CC7773A01F3`.
