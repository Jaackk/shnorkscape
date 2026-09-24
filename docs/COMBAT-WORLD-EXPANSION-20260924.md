# Combat and world expansion ? 24 September 2026

Status: **AUTOMATED VERIFIED / LIVE TEST PENDING**. No live deployment or restart.

## First checkpoint

The Dagannoth Kings now enforce the combat triangle at the shared final damage boundary, including queued hits, bleeds, Death Skulls and conjure damage. Supreme accepts melee, Prime ranged, Rex magic; all reject Necromancy. Non-style poison is not suppressed. Prime/Supreme resist stuns; Rex remains stunnable. Kings acquire nearby eligible players, and Supreme attacks multiple nearby players with separate rolls and captured targets. The existing scheduler, rewards, death/respawn and damage ownership remain in use.

This is a partial encounter improvement, not a complete retail recreation: ranged/magic projectile presentation, retail target-switch policy and Provoke immunity remain gaps. Health was deliberately preserved: engine HP 3,500 represents 35,000 displayed life points; multiplying the stored health again would be wrong.

Slayer Tower uses the existing reciprocal map stair handler. An actual-cache probe revealed that its 7x7 stairs exceeded the old three-tile landing search. The handler now searches around the decoded footprint while retaining free-floor, current-object and reciprocal-route checks. All 26 directional stair links were exercised with actual movement against the production950 collision data in a separate JVM.

War's Retreat's Reaper portal now offers **SHNORKSCAPE combat travel**: Dagannoth lair entrance, Slayer Tower ground floor, Taverley Dungeon entrance. These are explicit sandbox travel routes, not an implementation of retail Reaper assignments. Each arrives beside an actual symbol-pinned map object. Stale callbacks, blocked arrivals and invalid movement state cannot teleport. Exit option3 now correctly reaches Death's Office.

32 new NPC/object aliases carry decoded950 payload and index-reference SHA pins. They are labelled project-authored, not recovered Jagex names. They appear in the existing `;;gameval`/developer browser. `Native950Symbols.require` adds a fail-closed execution resolver; no mass migration of stable code or console startup changes.

## Bounded boss/admission audit

| Class | Examples | Current assessment |
|---|---|---|
| High value / feasible | Dagannoth Kings | Three existing profiles improved in this checkpoint; accessible through verified combat travel. |
| Needs generic mechanic | Normal Graardor; K'ril | Mixed attacks/AoE, immunities and access gates still need scoped implementation. K'ril's basic admission is not a complete encounter. |
| Access/instance blocked | Giant Mole; Kalphite Queen | Existing actors are insufficient: burrow/phase/instance lifecycle needed. |
| Major future work | Multi-phase modern bosses | Do not admit as oversized ordinary NPCs. |

Current admission baseline: 1,880 of 2,644 authored rows. Refusals: 140 invalid authored stats, 7 invalid cache parameters, 85 missing stat rows, 191 no Attack option, 232 unsupported special styles, 2 attack animation gaps, 105 unverified legacy identities, 2 effect-binding gaps. **No new NPC admission claimed in this checkpoint.**

Slayer already has weighted tasks, level gates, credited-owner progress, points, cancellation and rewards. Dagannoth task names include all three Kings. This pass improves access to Slayer Tower/Taverley; it does not claim a new skill implementation. Special-equipment/finisher tasks remain deliberately excluded.

Existing Kings loot tables contain 31/36/36 rows and Graardor 36. No rates were invented or changed; normal cache validation and credited-owner loot remain authoritative.

## Evidence and reproduction

- `tools/combat-pass/Native950CombatWorldAudit.java`: reproducible read-only NPC/map/symbol audit; optional second output path generates the alias TSV.
- `tools/combat-pass/Native950CombatWorldAcceptance.java`: isolated world JVM, real cache/collision, no listening socket or character saves. Verifies 1,417 pins, three admitted boss profiles, 26 stairs, three portal routes, stale callbacks and Death-office exit.
- Focused deterministic combat tests cover style gate, poison exception, stun policy and two players/other-plane Supreme attacks.
- Gameplay references: [RS Wiki Dagannoth event strategy](https://runescape.wiki/w/RuneScape%3AEvents_Team/Dagannoth_Kings_%2822_April_2018%29), [RS Wiki bosses](https://runescape.wiki/w/Bosses), [General Graardor](https://runescape.wiki/w/General_Graardor). Exact numeric identities come from950 cache pins, not these pages.
- Local old Dagannoth handler only reduced wrong-style damage to one fifth; that older behaviour was not blindly retained. Current shared combat ownership/queue architecture is preserved over legacy per-NPC callbacks.

Deferred live bugs remain deferred: cracker Pull, ability drag-off, equipment binding, Strength Powers, Ghost presentation and render priority. No speculative native UI changes.
