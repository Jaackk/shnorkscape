# Wisp feedback and compass repair - 12 September 2026

## Observed failure

The real saved backpack had all28 slots occupied, including Pale energy29313x1 and Pale memory29384x1. Existing Divination XP and those outputs establish that harvesting had already succeeded once. A subsequent Harvest click failed the existing atomic capacity guard for energy plus one additional nonstackable memory.

Both messages were unfiltered type0 native game messages. Divination sent the specific backpack explanation, then Native950Interactions appended "You cannot start that skill action right now." The generic final line obscured the useful explanation. This was not an unsupported Pale wisp or animation failure.

## Correction

Divination now owns its routed refusal feedback. The dispatcher does not append a generic failure. Capacity feedback explains how to make room (bank items or convert memories), low-level feedback remains specific, and a start outside the stationary/reachable environment explains that the player must move beside the wisp. Inventory capacity, levels, atomic rewards, movement timing and the natural-wisp harvesting pause remain enforced. No player items, levels or save contents are edited by the repair.

The companion compass correction mounts1919 at1465:12 with permanent HUD type1 instead of modal type0, keeping it outside generic client modal cleanup. See compass-lifecycle-950.md and its cache evidence. Placement and the client's camera rotation remain unchanged.

## Regression

Native950NpcSkillsAcceptance adds an encrypted level1 Harvest click with28 occupied slots, existing Pale energy and an existing memory. The new assertion failed against the old behavior with both the capacity message and generic message decoded. It requires exactly one unfiltered capacity explanation, no item/XP change on refusal, successful harvesting after freeing a memory slot, and one clear capacity explanation when that slot fills again. A higher-tier wisp must retain its specific level10 refusal.

Native950UiAcceptance now runs the real compass bootstrap and verifies the permanent type from encrypted IF_OPENSUB output while exercising Settings, Escape and world-map modal handling. These tests do not render the client. Chronological deployment records remain owner-local.

Backups: implementation-backup/2026-09-12-wisp-compass; compass originals separately in implementation-backup/2026-09-12-compass-modal-fix.
