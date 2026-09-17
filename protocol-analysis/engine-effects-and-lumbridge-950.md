# 950 player effects and bounded NPC milestone

Updated 2026-09-10. All work is contained in `950RevTest`; the 947 and original 910 projects are read-only references.

## Implemented

- Player graphics now reach the 950 spot-animation list through all four legacy slots. Per-slot clearing and clear-all preserve the native distinction between slot IDs and definition IDs. The NPC bridge uses the same identity gate.
- The dedicated index-21 survey found 6,962 identical 910/950 effect definitions, 297 changed definitions and 2,005 new definitions. Each accepted effect is checked against the selected running cache. Identical effect records do not prove identical models: visual checks remain necessary. See [effect derivation](player-effects-950.md).
- Forced movement uses an immutable snapshot and the native 20 ms client clock: a normal game tick is 30 cycles / 600 ms. One-leg and two-stage requests share their wire deadlines and server scheduling. The server still applies authoritative arrivals on its 600 ms world ticks; precise requests can therefore round up to the next server tick.
- Forced movement remains active after its one-frame mask is reset. Walk and object/NPC approach requests respect that state and player locks. Explicit cancellation, supersession and unrelated teleports cancel old scheduled arrivals. A stationary 0/1-cycle mask also replaces the client's old interpolation. See [force derivation](player-force-movement-950.md).
- The force publication queues final XY at native WALK speed before emitting an immutably rebased force mask. This aligns the client's retained path point and logical position while preserving its rendered starting position. Per-viewer tracking handles later waypoints, plane changes, cancellation and replacement. New NPC additions wait during the active movement; a newly visible player waits on the original force-mask frame. The actual-cache probe confirms server position stays at the origin until its scheduled arrival and that arrival sends no duplicate recipient movement.
- Standard health bars (IDs 0, 3 and 4) have identical definitions in all three retained caches. They are verified before startup and before first live use, and work for both player and NPC updates. Custom/timed bars remain refused. See [bar derivation](hitbars-hitmarks-950.md).
- `-LumbridgeNpcs` enables identity-checked legacy spawn rows only in region 12850. The existing development banker remains available. The underlying region scope supports explicit lists for reproducible testing. Region teardown/re-entry must repopulate exactly once; unsafe identities are counted and omitted.
- `-DevTools` enables a small `;;nxt` command namespace only on a loopback connection using client profile 950. Existing account rights and legacy commands are unchanged. Public chat opcode 87 and the semicolon prefix were checked against the 950 client.

## Launch and live checks

```powershell
.\Start-950Test.ps1 -Ataraxia -Collision -Vulkan -LumbridgeNpcs -DevTools
```

After entering World 1, type these in ordinary game chat:

| Command | Action |
| --- | --- |
| `;;nxt banker` | Move beside the development banker at Lumbridge. |
| `;;nxt cook` | Move beside the data-spawned Cook. |
| `;;nxt effects` | Queue four verified effects: 94, 184, 436 and 1576. |
| `;;nxt clear` | Clear all four graphics slots. |
| `;;nxt bar` | Show the character's current health bar without changing HP. |
| `;;nxt force` | Move two collision-checked cardinal tiles over 1.2 seconds. |
| `;;nxt status` | Print current tile and update/refusal counts. |

The movement demonstration briefly locks manual movement to keep its path stable. The loopback tools are disabled unless their launch switch is supplied.

## Validation

Combined tests passed: **709 discovered, 707 passed, two skipped, zero failures/errors**.

Real-cache startup preflight passed with verification enforced. The existing UI regression passed 37 encrypted input actions and 430 parsed output frames. The new world probe passed with 199 scoped rows: 147 admitted, 52 refused (19 missing, six renamed, 26 repurposed, one unverifiable), no decode/spawn failures. Its Lumbridge subset admitted 93 of 123 rows. Cleanup/repopulation, outside-region exclusion, real player/NPC viewport framing, effect 94, half/full health bars and forced arrival all passed.

The first world-probe attempt used a name longer than the client's 12-character limit; shortening the temporary test name fixed the fixture without changing gameplay or weakening the appearance guard.

Real-client banker checks passed: deposit five logs, Withdraw-X two, withdraw remaining three, Talk-to portrait/options, dialogue reopening, and walking cancellation. Visible Lumbridge NPC movement was observed. Chat testing exposed a duplicated-length decoder error; public/private chat and text/name prompts were corrected with native sender evidence and full command-route regressions. The corrected banker command, Cook dialogue and both portraits, map-close/dialogue reopening, overlapping player effects and full overhead HP bar passed in the real client. Forced movement exposed a duplicate endpoint update; the deployed correction passed the packet/world checks, and the user confirmed movement seems fine after fresh login. Server logs corroborate subsequent walking and banking. The user confirmed the Cook shortcut; completing the whole dialogue exposed unported combat-instance entry. The latest build supplies a safe ending and pre-mutation guards, and the actual-cache full-conversation/reopen probe passes. The user confirmed the complete conversation works without crashing. See `validation-effects-2026-09-10.json` for the current per-check status.

The real-cache `Native950WorldAcceptance` main checks a temporary scoped NPC population, exclusion of a loaded outside region, identity verdict accounting, cleanup/repopulation, the NPC viewport, actual 950 encrypted output framing, graphics/HP-bar/force mask ordering, and world-thread scheduled arrival. It creates no network listener and loads/saves no account. Run it from `Ataraxia950` so the staged data root resolves correctly.

## Remaining work

Nonzero colour tint conversion, custom effect flags, broader bar classes and viewer/style-aware typed hits still require separate evidence. Full NPC combat, death/respawn, drops, projectiles and broad content parity are not enabled by this milestone. Visible NPCs use the bounded native movement lifecycle; their appearance does not establish complete AI or every interaction.

The user's manual world-map dragging check is confirmed and retained from the preceding stability milestone.
