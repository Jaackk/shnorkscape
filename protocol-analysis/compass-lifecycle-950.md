# Compass lifetime correction, 12 September 2026

The compass already exists in the paired 950 cache: interface 1919 has its 36x36 root, ring sprite and camera-rotated needle (content type 1339). The previously accepted mount remains minimap 1465:12, the 41x41 top-left layer. The cache defaults for that layer, its parent 1465:8, and all three compass components are visible; no cache edit, layout reset or forced positioning is needed.

`Native950Interactions.compass()` opened the compass with `walkable=false`, which encodes subinterface type 0. That puts the compass in the client's modal-close lifetime despite its permanent HUD role. A client-side close-modal pass can therefore remove it while the minimap stays present and the server never explicitly closes the compass slot. The corrected mount uses type 1, consistently with the minimap and other permanent HUD components.

For IF_OPENSUB on 950, body offsets 0..3 contain parent 1465:12, offsets 16..17 contain child 1919, and offset 18 is `128 - type`. The changed byte is 0x80 -> 0x7f. This retains the existing mount and client-owned rotation/click behavior.

The binding table now pins the mount and all three compass components against the actual cache. `compass-lifecycle-950.json` records fully consumed IF3 decodes and hashes. `Native950UiAcceptance` now executes the real login bootstrap, decodes its encrypted outbound frames, asserts permanent compass type 1, and verifies Settings, Escape, map handoff and CLOSE_MODAL routes never close its mount. It fails against the former type-0 bootstrap. These checks do not execute the client renderer or prove visual retention.

Manual confirmation: on a fresh login, check the compass at the minimap top left. Walk; open/close Settings; open/close the world map; press Escape; resize. The compass should stay present and its needle should follow camera rotation.

Before-edit backups: `implementation-backup/2026-09-12-compass-modal-fix`. The server/client restart is owned by the coordinating task; no process was restarted by this subtask.
