# Production acceptance (950 paired cache)

`Native950ProductionAcceptance` runs an isolated native player and an encrypted EmbeddedChannel on the original world thread. It opens no listener and writes no player account. Cache verification stays enabled. The probe verifies all 243 current recipe asset pins, then exercises representative source recipes through the real 950 interaction router.

Validated paths:

- Opcode 69 knife + logs (both endpoint orders) → native 1188 product and quantity options (opcode 101) → original ActionManager ticks → bounded Make 5 with only three logs → 45 arrow shafts. The knife remains in inventory.
- Malformed option components, retired quantity responses, walking cancellation before first production tick, and delayed replies to a closed menu do not award materials or XP.
- Native Logs Craft option (opcode 18) → More choices → fourth recipe → Make 1 wooden stock.
- Feather + shafts and bronze arrowheads + headless arrows retain the old 15-item batch quantities and atomically consume both stacks.
- Sapphire cutting refuses level 1; level 20 succeeds, retains the chisel, and awards Crafting XP.
- Grimy guam Clean → unfinished guam potion → attack potion uses original Herblore XP and resource quantities.
- Chocolate grinding retains pestle and mortar and does not invent XP.
- Native UPDATE_STAT and inventory frames are parsed through the outgoing ISAAC stream, and final skill XP reaches the existing character save snapshot.

Result against final compiled classes: **PASS — 243 pinned recipes, 132 original engine ticks, 282 decoded encrypted 950 frames.** See `logs/production-acceptance-950.log`. The final packaged JAR also passes the same checks; see `logs/production-packaged-acceptance-950.log`. JAR SHA-256: `f4dcd345e14ccae06a1ffc6488da58104f4cdd0d392f9a5ce4b53e71ec50b773`. The same exact package passes Prayer (21 offerings) and Skill Guide (29 skills, 1247 packets) in their packaged acceptance logs.

An initial packaged probe used a fixture name longer than the existing 12-character account limit. Gameplay checks passed; save-snapshot validation correctly rejected that fixture name. The probe source now uses `produce-test`. This was a probe fixture error, not a production skill failure.

Independent review also checked inventory staging: although `ItemsContainer.asItemContainer` copies Item references, its add/remove operations replace each changed reference, so preview and failed exchanges do not mutate original held stacks. Focused JUnit tests additionally cover full-bag replacement, multi-output rollback, duplicate inputs, stack overflow, controller veto, tool removal and empty-output offerings.

Limit: these checks prove cache identity, server state transitions and transport framing. Real-client menu layout, the appearance of each animation, and long-running skilling still need in-game checks. Item/sequence IDs were not inferred from names alone; see the production asset and sequence evidence files.
