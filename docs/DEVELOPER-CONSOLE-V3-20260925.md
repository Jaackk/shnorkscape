# Developer Console V3 polish ? 25 September 2026

## Scope and baseline
V2 is user live accepted. Preserve native previews, rotation, search, editor ownership, Equipment Library, combat and keyboard cleanup. Source backup: 75c9ae0; local snapshot backups/pre-edit-20260925-044304-773. The V3 prompt explicitly authorizes deployment/launch and physical validation. User also agreed to leave Vulkan untouched for testing.

## Implemented, awaiting final physical validation
- Search field updates its existing native component instead of deleting/recreating it. Padded text, contextual placeholder, Search button and Enter share the authoritative query path. Native context11 cleanup is retained.
- Preserve the native list geometry until CS21151 captures scroll. The old initializer resized the list before this capture, invalidating retention on inspector redraws.
- Same-domain filters preserve queries. Major-domain round trips retain their own query/filter/window/selection, without carrying NPC text into Items.
- NPC ranking now orders useful family representatives across results instead of only choosing the best duplicate and then leaving all families alphabetical. Exact names/IDs and All Variants remain available.
- Stronger selected tab/filter text and collection borders. Item row names are bounded, inspector headings explicitly wrap; item IDs move out of Simple-mode metadata.
- Larger local-player preview with native drag and Zoom/Reset; Best Gear uses its empty inspector for the local player. No appearance mutation for preview.
- Combat/Home/Player mode controls display ON/OFF. Teleports now has bounded-result navigation beyond 100 destinations.
- Unrenderable/malformed NPC preview resolves to Preview unavailable rather than breaking the inspector.
- Startup experiment: hide the management window during the existing ready handshake; the native bridge still runs the exact original refresh and acknowledgement, then the completed server page reveals it. No delay. Must pass fresh-open Vulkan before acceptance.

## Physical baseline observed
Existing running Vulkan/Jaxa: clipped Crown of the First Necromancer heading, raw technical NPC names leading the default NPC list, search text flush against its left border, unused title-to-content band. Kalphite King grouped representative rendered at usable scale and retained its native model/animation controls. Native key events work for search; Computer Use Unicode type_text did not enter characters in this client, so physical tests use press_key.

## Bounded research / unresolved
- Complete scenery preview: exact research proves raw-model component, not assembled loc shape/recolour/terrain-contour contract. Do not present one raw mesh as faithful object preview.
- Floating placements: latest available Jaxa Bug Test has no developer-placement diagnostic entries; no concrete failing definition/tile and matching natural placement were established. No global offset applied. Existing shape/rotation retained.
- NPC placement: existing occupancy is exact width-square with zero added clearance; shared World.canMoveNPC rejects any nonzero clipping mask. Changing that requires a concrete failing tile/mask versus legitimate wall/occupancy evidence. No collision relaxation.
- Map teleport: Open World Map remains; clicked packed-coordinate/plane/menu packet conversion is not yet proven. No guessed conversion.
- Teleport graphics: current audited travel path supplies movement and landing validation, no established presentation binding. No speculative effect added.
- Header band: management-frame/content origin is native; no safe relayout/resize contract established in this bounded pass. No negative offset.
- Framing: Beasts params3040/3041 remain authoritative, ordinary footprint fallback remains provisional; no exact arbitrary-model bounds contract. Kalphite King baseline does not justify blanket zoom adjustment.
- Animation Lab and new category/icon breadth deferred behind core polish. No new persistence system.

## Validation
In progress. Do not treat this document as a completion or live-acceptance claim.
