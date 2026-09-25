# Developer Console V3 polish - 25 September 2026

## Scope and baseline
V2 is user live accepted. Preserve native previews, rotation, search, editor ownership, Equipment Library, combat and keyboard cleanup. Source backup: 75c9ae0; local snapshot backups/pre-edit-20260925-044304-773. The V3 prompt explicitly authorizes deployment/launch and physical validation. User also agreed to leave Vulkan untouched for testing.

## Implemented
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
- Framing: Beasts params3040/3041 remain authoritative, ordinary footprint fallback is now increasing camera distance (800 for single-tile actors, otherwise 1000 * size capped at 6000), not the previous inverse. No exact arbitrary-model bounds contract. Native Beasts framing remains unchanged; fallback model origin is 65 pixels lower to keep its feet near the inspector baseline.
- Animation Lab and new category/icon breadth deferred behind core polish. No new persistence system.

## Physical validation during this pass
- Normal fresh login/open rendered Home and clickable domains without a diagnostic redraw. Snapshot observations cannot certify absence of a single-frame flash.
- Player is now about 180 pixels tall, centred and fully visible; Zoom + physically enlarges it. Previous camera distance 1500 made it microscopic; current distance 500 with the native player offset is useful. No real appearance changes.
- Search button submitted Vorago; Enter submitted Nex, Kalphite King and Banker. Padded input and contextual placeholders are visible.
- Broad dragon search physically shows Dragon, baby black/blue/red dragons and King Black Dragon before technical helper names; exact-name Dragon (level 0) still leads, so relevance remains imperfect.
- Nex appears first, followed by Angel of Death, ahead of helper names. Banker appears first. Kalphite King is grouped to one normal result with the variants route still available. Vorago is grouped ahead of quoted/helper names.
- Nex and Kalphite King render at useful scale using native Beasts metadata. Kalphite King's verified Attack preview visibly animates.
- NPC subject replacement rendered successive Vorago, Nex and Kalphite King without closing the console. This is evidence of improvement, not proof that every cache model will load.
- First candidate: NPC query survived Items round-trip without leaking into Items; Teleports scrolled to the last saved destination and selection retained scroll. The unfocused cleanup then exposed an old-query flash, covered by a new regression and repaired.
- Home, Player, NPCs, Objects, Items and Teleports were physically opened. Native item icons render. No grants, loadouts, world spawns, placement deletion or teleports were performed in this pass so far.
- Drag cursor appears, but automated short drags did not provide clear rotation evidence. Existing user live acceptance is preserved; new Player rotation is not independently accepted.

## Automated validation
- Full Java suite: 1592 tests, 0 failures, 0 errors, 2 existing skips; test + jar build passed.
- Native script VM: 21 tests passed, including unfocused cleanup not restoring another domain's query, selection/scroll retention, model subject replacement and native input contracts.
- Installer fixtures: 6 tests, 0 failures, 1 inapplicable new-allocation test skipped for the refinement; Update and Play: 6 passed.
- Real 51-file pinned candidate CheckOnly passed; Update and Play successfully preserved local settings, backed up runtime, applied and relaunched Vulkan.
- Final model-origin argument change received script verification and jar compilation; Java gameplay code was not changed by that final layout adjustment.

## Acceptance status and remaining work
This is a deployed V3 polish checkpoint, NOT complete V3 acceptance. Home still lacks the requested visual icon treatment; richer favourites/recent, full domain state including cross-domain scroll restoration, stronger NPC selected-row treatment, full Player sectional redesign and all-page physical action coverage remain partial. Optional object/map/teleport/header contracts above remain unresolved and safe to revisit separately. No speculative world or collision fixes were introduced.

## Final deployed checkpoint
Source 066bbb28d80ab60345221c397a1891985e36dbbd. Candidate developer-console-v3-20260925, 51 pinned files.
Jar SHA256: 1E5556E4992375691AC4025C58BFB6C52BE87FD4E2C53E015CA1058D865FEDF5.
Script reference SHA256: 6D4D86AF13754607EDE54871A6349D3C901DBAB0F99CDF1CB40E369596A15239.
Final rollback: backups/playability-update-20260925-054255-434.
Vorago at the lower fallback origin plus a larger native camera distance was physically unclipped and usable. That measured direction informed the final footprint-distance formula; its final default and the last title-wrap change are not yet re-observed on the final build. Baby black dragon rendered, but small; zoom remains useful. Celestial catalytic wand's large title still clipped at the old threshold, prompting the final earlier wrapping.
