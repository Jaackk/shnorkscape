# Generic NPC combat in revision 950

Implemented and deployed on 2026-09-10. Full tests and real-cache acceptance passed; fresh-client Man/spider acceptance is pending. Current engine SHA-256: `7f008a0ca7d52e1a56ed23d241ecea086bc3c605babb279b9b56785d37499632`.

## What is now generic

The six-ID chicken/goblin allowlist is removed. The same resolver admits any identity-safe, ordinary melee NPC with a real 950 Attack option and usable server stats. It has no per-species ID switch. Targeting, movement, retaliation, damage, naming, corpse visibility and respawn share one world-thread service. Larger NPCs use their entire footprints for approach, contact edges, leash distance and clear spawn/respawn homes; walls are checked in both directions, including internal walls crossed by a large footprint. The HP bar now uses a long intermediate so large health pools do not overflow.

The actual paired-cache audit accepts **1,500 of 2,644 effective authored NPC profiles**, including **489 multi-tile profiles**. This is definition eligibility, not a claim that all are spawned or individually visually tested. Source combatDefs.json has 2,646 rows; two duplicate IDs use the existing parser's last-row-wins behavior. The live spawn scope remains region 12850.

## Data sources

| Value | Source and policy |
| --- | --- |
| Name, size, combat level, Attack option | Current950 NPC definition; no added Attack option for bankers or other noncombat NPCs |
| Attack cadence | Cache param 14 where present (1,481 profiles); authored delay otherwise (19). Present-invalid parameters are refused. Chicken now3 ticks and goblin 4, versus the previous authored 5; ticks remain 600ms. |
| Accuracy / armour | Cache29/2865 converted by the existing RS2 rating adapters. Missing ratings use an explicit neutral zero bonus (160/30 profiles), not a guessed cache value. |
| HP, Attack/Defence levels, exact maxhit | Concrete existing combatDefs.json/npcstats.json rows. No cache parameter is relabeled as an unproved health or level field. |
| Death / respawn timing | Existing server rows. Verified death-animation duration extends corpse visibility, capped at 10 animation ticks to avoid long final-pose holds such as human 836. |
| Attack/block/death animation roles | Existing authored role bindings, checked against a generated portable950 sequence compatibility resource. |

The cache also has damage ratings 641/643/965, but these are not explicit maximum-hit fields. The old server's estimated max-hit formula is not substituted for a concrete authored maximum. Unmapped params 26/50 are left uninterpreted.

The animation survey verified **870 of 882 referenced sequences**:817 identical definitions and 53 matching complete frame/duration bindings. Twelve changed bindings are unavailable. The runtime checks selected 950 bytes and durations; it does not need the old cache. Sequence compatibility does not by itself establish rendered compatibility with a remodeled NPC skeleton.

**990 eligible NPC profiles explicitly have attackAnim=-1**. They can fight using their stats without a swing animation; no replacement animation is invented. The legacy Default combat path also sends the authored value without a generic fallback. Positive unverified attack bindings remain refused. Missing/unsupported optional block/death animations are omitted. Some old NPCs depend on custom combat scripts; those behaviors are not enabled by profile eligibility.

## Coverage gaps

The other 1,144 authored profiles are recorded with reasons in generic-npc-combat-coverage-950.json: 633 non-melee attack styles, 191 without a current Attack option, 121 with invalid core stats, 105 with unsafe legacy identities, 85 missing stat rows, 7 with invalid cache parameters, and 2 with changed positive attack-animation bindings. Ranged, magic, boss scripts, scripted transformations, automatic aggression, XP, ground drops/pickup and broader region population remain separate work.

NPCs with incomplete data stay visible when their spawn is otherwise valid. They do not get invented HP1 combat or crash the region population. Fixing a data row benefits every spawn of that NPC; extending a shared resolver or behavior benefits all matching NPCs. No per-NPC Java unlock is required.

## Verification and use

Full suite: **791 discovered; 789 passed; 2 skipped; 0 failures/errors**. Seven Python animation-generator regressions passed. The packaged real-cache combat probe passed **436 accelerated owner-thread ticks and 1,368 encrypted frames**, including complete chicken, goblin, man, cow and giant-rat lifecycles, retaliation, full-HP respawn/readdition, player recovery and retained equipment, actual approach and blocked-wall isolation. The cow fixture uses an ephemeral Strength 20 test character to survive a deterministic all-hits scenario; no authenticated account is changed. Existing Cook/movement/effects/scoped-world regression, cache preflight, Kotlin rebuild and all 17 launcher checks passed.

Run `Start-950Test.ps1 -Ataraxia -Collision -Vulkan -LumbridgeNpcs -DevTools`. After login, `;;nxt combat` moves next to an available eligible creature; `;;nxt status` includes fighter counts. Try a Man or giant spider in Lumbridge in addition to the already confirmed goblins. Missing swing-animation data is a known per-profile gap. Other controls and the existing player weapon set are unchanged.

Evidence: validation-generic-npc-2026-09-10.json and the logs named there. Before this expansion, the user confirmed basic combat, numeric blue zeros and visible goblin respawn after about 40 seconds. New generic visual results must be recorded separately from automated evidence. The working prior runtime/logs/saves are backed up under implementation-backup/2026-09-10-generic-npc-combat.
