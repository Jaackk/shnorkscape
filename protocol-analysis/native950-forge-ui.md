# Native 950 Smithing / Smelting interface

The paired cache uses interface37 for modern Smithing and Smelting. Generic Make-X1370/1371 is a separate system. Native950ForgeUi owns the native frame and invokes existing Native950SmithingAction / Native950ProductionAction callbacks after validating the selected cache product and current recipe.

## Cache-derived bindings

- Native large central slot1047 resolves through enum7716 to struct40393: wrapper1477:724 and host1477:726. Frame37 has174 components and a fixed800x484 canvas; the native800x600 large wrapper owns dragging and clipping. Forge does not resize or reset the separate small modal732/735.
- DB table7 defines material/product groups. Root row1489 is Smithing and1482 is Smelting. Mixed string/int tuples must be decoded correctly; the generic Invention DB decoder is not suitable for this table.
- Enum2531 maps bars to Smithing material rows;2530 maps ores/casting materials to Smelting rows. Each DB-row group names a dense item enum. Root groups include Smithing Core metals2532, Masterwork2533 and Misc2534; Smelting Core ores2528 and Casting metals/glass2529.
- Material grids37:52/62/72/82/92; product grids37:103/114/125/136/147. Dynamic clickable child is2*enumOrdinal+1. Both the cache row and claimed item are validated before selecting.
- Initializer2586(rootRow,materialRow), refresh2589(materialRow,baseProduct,upgrade), heading2600(smelting?1:0).
- Varp8331 root row,8332 material row,8333 base item,8336 quantity. Varbit43239 upgrade level.
- Upgrade buttons149/161/159/157/155/153/151 correspond to0/1/2/3/4/5/burial50. Item parameters7806(base),7805(max),7807(next),7804(level),7803(burial) resolve actual output IDs.
- Begin Project37:163; Close37:42; quantity37:35 transmits count-1 through scripts10085/10424/10450. Quantity is bounded by actual backpack ingredients and a10000 batch ceiling.
- Keyboard context85 is explicitly unregistered with8841(85,0) on close. Owned mounts are retired without invoking a generic close callback that could close a newer modal.

Generated evidence: tools/generate_950_forge_ui.py -> resources/native950/forge-ui-950.json. It records36 DB rows,97 enums and382 SHA256 cache pins. Startup preflight verifies those pins before login can open the interface.

## Behavior and boundaries

All702 currently ported smithing recipes and12 smelting recipes have native UI paths. Anvils offer all implemented products even when materials are absent; actual production still checks level, tools, inventory, station reach and action/controller state. The toolbelt hammer satisfies the ordinary hammer requirement. Recipe callbacks and XP rules were retained rather than duplicated in the UI layer.

Visible cache products without a ported callback cannot start. Stale, forged, duplicate-after-close and moved-player requests cannot consume materials or start production. The selected recipe and current material bound are rechecked before starting; existing actions continue to recheck their station and requirements during work.

Metal-bank storage is not implemented. Actions consume backpack materials. Deposit-all and Withdraw-all controls are hidden; the ordinary stored-metal display can remain zero. No mirrored virtual inventory is sent to simulate metal-bank contents, because that could double-count ingredients.

## Automated verification

Native950ForgeUiAcceptance uses the actual cache and encrypted interface actions with disposable state, no login or save file. It verifies every702 Smithing and12 Smelting native path, material/product/upgrade selectors, quantity2, Begin Project, Close, bronze dagger production using only the belt hammer, iron dagger upgrade, bronze smelting, invalid claimed IDs/actors, duplicate clicks, walking away and station removal. Passed952 decoded response frames. This establishes routing/state/packet behavior; final rendered layout and live click behavior require an in-game check.

## Live-client follow-up: initial redraw and station context

The actual client sent a normal interface action for `37:163` Begin Project, confirmed in the live server log. This control is different from generic Make-X `1370:30`, which uses a dialogue continuation. Cache component `37:163` has the normal Select option mask; `37:4` builds its artwork through scripts13968/13982.

The first furnace open showed `OBJECT NAME` until a product was clicked. Switching furnace to anvil also briefly retained the previous material categories. Script2591 returns immediately when root varp8331 is -1; script2588 reads that same root when redrawing. The server now performs one additional next-world-tick refresh of both2586 grids and2589 details after publishing the opening's varps. A generation guard, active channel, current interface ownership, captured tile and current station prevent a deferred callback from redrawing a closed, replaced or remote screen.

The red normal-anvil requirement had a separate cause: missing station varp8334. Current-cache scripts7163 (condition) and7164 (message) read the clicked object's ID from8334 and then object param7803. `open` now takes the actual WorldObject, validates reach, publishes its ID, and clears8334 on close. It does not grant blanket unlock flags. Live diagnostic station IDs were furnace113261 and anvil113258.

The cache generator now also pins2589,10085, the quantity callbacks, actual button builders and both station requirement scripts. The expanded encrypted acceptance passes952 decoded frames, including initial deferred redraw, exact station publication/clear, furnace-to-anvil generation isolation, and close/movement/station-removal cancellation. Final rendering of these follow-up fixes awaits the combined build's live retest.

Remaining scope: metal-bank storage is separate. Existing backend Smithing callbacks do not yet enforce every burial/barbarian/dragonkin-specific station requirement shown by the native cache. Publishing the true station context improves the display; it does not complete those additional gameplay restrictions.

## Drag clipping correction (12 September2026)

The user reproduced the clipped view live: moving the Smithing window reduced its visible region to512x334 and cut off the title, controls and edges. The prior implementation enlarged the small central host1007 temporarily. Its native drag/layout path restores the small-host dimensions, so that enlargement could not survive dragging.

Forge now mounts in the actual large central slot1047. Script2600/8421 discovers that mount through3934 and installs the matching native frame handlers. The native large wrapper and filling host retain their cache dimensions; there are no11145 overrides or8389 small-host reset calls in Forge. Close retires726, hides724 and refreshes1364. Production selection, requirements, quantities and deferred initialization are unchanged.

The expanded encrypted acceptance checks mount/close/registration726, rejects all Forge writes to small components732/735, then opens native Fletching Make-X and crafts a shortbow after a stale Forge close. It also retains all702Smithing/12Smelting recipe paths and production/interruption checks. Targeted source acceptance passed1038frames. Deployment and rendered drag verification are recorded in the latest handoff.
