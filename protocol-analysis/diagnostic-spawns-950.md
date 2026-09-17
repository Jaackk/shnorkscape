# 950 cache lists and diagnostic commands

The lists come from the selected local 950 cache, not the old 910 ID tables:

- ../dumps/items.txt — 63,414 item definitions
- ../dumps/npcs.txt — 32,762 NPC definitions
- ../dumps/objects.txt — 140,252 object definitions

Each UTF-8 text file contains ascending IDs and tab-separated names. Unnamed definitions remain listed as <unnamed>. The item list resolves note/lent/bound/shard name templates from this same cache and records variant details. A listed definition can still be a placeholder or conditional form without a standalone world model. See ../dumps/export-report.json for source/output hashes and counts; all strict decoding checks completed with zero errors.

Run ../Export-950CacheNames.ps1 to regenerate the three lists. It only reads the cache and writes the exports.

## In-game commands

- ;;item <id> [quantity] — add items to the backpack; quantity defaults to 1.
- ;;npc <id> — spawn an NPC at the player's exact current tile.
- ;;obj <id> [type] [rotation] — spawn an object at the player's exact current tile.

Examples: ;;npc 12353 (goblin), ;;npc 42 (Bill), ;;obj 38787 (tree), ;;obj 70755 (fire). These use current950 IDs. Walk a tile away after an NPC spawn to see it separately from the player.

Object type defaults to the cache's scenery type10 when available, otherwise its first supported model shape. Explicit type must be0–22 and have a model in that definition; rotation is0–3 and defaults to0. The spawn reply names the ID, chosen shape and rotation. Occupied or removed-original object slots are refused rather than replaced. Large-object footprints must have clear scenery and wall space.

Commands stop retained walking/skilling before placement and refuse active teleports/forced movement. They retain the existing loopback-only Native950 + DevTools restriction. Start-950Test.ps1 -Ataraxia -Collision -Vulkan -LumbridgeNpcs -DevTools enables them.

## Lifetime and supported interactions

NPCs join the existing world roster, region index, per-viewer NPC updates and generic combat registration. They start without wandering. Up to128 diagnostic NPCs can exist per active world; the existing last-player-leaves cleanup removes them. Objects are runtime placements, retained until server restart unless normal game logic removes them. Neither command writes permanent spawn data.

NPC definitions are strictly decoded from this cache. Concrete models are required; variable-form definitions ask for a concrete form ID. Exact-instance cache/ID validation lets diagnostics display new or repurposed950 NPCs without relaxing legacy spawn-data identity checks. Examine uses the current cache name/ID. Existing supported generic combat works when its profile and spawn footprint are valid; unported combat reports unavailable. Noncombat actions for repurposed/new IDs do not enter910 ID-specific handlers. This tool does not port boss AI, shops or quests automatically.

Objects use existing World/Region placement, collision and Native950ObjectsView publication. Ordinary ported interactions, such as chopping supported trees, remain on their existing routes. Spawning the fire model alone does not schedule a firemaking lifetime or award XP.

## Validation

The focused unit tests and Native950DiagnosticSpawnsAcceptance exercise parsing/gates, exact tile placement, strict NPC definitions, world/region/viewport registration, existing goblin combat, encrypted NPC/object frame boundaries, automatic scenery/wall shapes, occupied-slot preservation and cleanup. See validation-diagnostic-spawns-2026-09-10.json for the final build/run result. In-game rendering still requires the user's live check.
