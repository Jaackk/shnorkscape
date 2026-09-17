# Native950 Slayer baseline

2026-09-12. The reused SlayerTaskData, SlayerMasterData and TaskSet source files are byte-identical to the original910 files. This baseline reuses that task data and its completion rules through the native950 world, combat and save adapters. It does not invoke the old NPC death callback or old Slayer manager.

## Playable flow

Turael is added beside the development banker at3219,3258,0. The current-cache Talk to (or Talk-to) option opens a native dialogue with assignment and progress choices; Get task, Get-task or Assignment requests an assignment directly. The route, published-NPC identity and controller checks remain in Native950Interactions. Dialogue replies require the same live master within3 tiles on the same plane. Existing native chatbox message/options interfaces1186/1188 are used; this feature adds no portrait, sequence or interface binding.

The service reads SlayerTaskData and SlayerMasterData rather than duplicating their weights, ranges, aliases or points. Turael's ordinary novice tasks retain the original15–50 count range. An eligible task must have a positive master weight, satisfy base Slayer and current combat-level requirements, and have at least one already registered native combat NPC in the populated world. A matching name on an arbitrary NPC with no accepted native combat profile is not enough. Existing tasks cannot be rerolled by repeatedly clicking a master or visiting a different master.

Task families whose required unlocks, finishing items, special defences or encounter mechanics are not ported are excluded. Other masters retain their original level requirements, and Morvran additionally requires Prifddinas access. Current NPC population and native melee profile admission still limit the available assignments. The task pool grows as those existing systems admit more creatures; this baseline does not enable every monster or region.

## Kill and reward ownership

Native950MeleeCombat's existing committed death path calls Native950Slayer.onDeath once for the existing damage-credit winner, after the ordinary native loot reward. Per-hit combat XP is unchanged. Slayer requires a current registered NPC identity, an accepted matching combat profile, dead state and native death presentation. Matching uses the original exact family aliases, case-insensitively, against the profile's current-cache name. Substring matches and mutable display-name substitutions are not used.

The original Slayer.checkTask reward is integer engine maximum HP divided by10. That base amount goes through Skills.addXp, preserving account rates, XP drops, skill levels and the saved skill arrays. A kill advances only the credited player's active matching task. The existing melee death transition owns duplicate prevention: the helper is not a public combat event replay interface. Display, corpse removal and respawn must not call it again.

Completion increments completed tasks. As in910, Turael grants no points and does not increment the streak; other masters increment the streak and use their original ordinary, tenth-task and fiftieth-task rewards. Arithmetic saturates at Integer.MAX_VALUE. Cooperative credit, bonus multipliers, Slayer shops, blocks/skips, helmets and reward spending remain unported and cannot mutate this new state through old handlers.

Native950Slayer.attackRefusal provides a read-only combat level gate. It combines the original Combat.getSlayerLevelForNPC exceptions with the maximum exact-family Slayer requirement, preventing a broad family such as Aviansie from lowering Spiritual mage requirements. It must be called by native combat before target mutation. Assignment eligibility also filters population witnesses by that required level. This gate does not introduce old combat callbacks or claim to implement special monster mechanics.

## Persistence and migration

Player owns Native950Slayer.State and includes its values in the existing settings snapshot/restore path. Six bounded integer keys fit the existing schema3 settings section:

| Key | Meaning |
| --- | --- |
| slayerTask | Stable positive Java hash of the enum name, or0 for no task |
| slayerRemaining | Remaining count, bounded by that task/master's original maximum |
| slayerMaster | Original master NPC identity, or0 before any assignment |
| slayerCompleted | Nonnegative completed count |
| slayerStreak | Nonnegative streak, no greater than completed |
| slayerPoints | Nonnegative unspent points |

Task codes are independent of enum declaration order; collisions and zero codes fail during initialization. An invalid task/master/count tuple clears the active task together while independently bounding counters. Absent keys give old profiles an empty Slayer state. Restoration emits no packets and does not change items. XP continues to persist in the existing SKILLS section, so progress and Slayer XP participate in the same native snapshot. There is no new save schema or migration of old serialized910 Slayer accounts in this change.

## Verification

Native950SlayerTest covers original weighted selection/count boundaries, requirements, unavailable populations, reroll refusal, exact aliases, kill XP units, completion bonuses, integer bounds, invalid save tuples, stable task codes and a PlayerBinder/temporary SaveStore roundtrip. Its persistence check also verifies that task progress changes settings without changing inventory or bank arrays.

Native950SlayerAcceptance is an isolated actual-cache world probe. It verifies master identity/menu metadata and Turael's floor tile; opens the native conversation; assigns from a real registered goblin profile; kills and respawns that NPC through the production native melee reward path; verifies no reward repeats during corpse/respawn ticks; completes the task; and restores task progress and Slayer XP from a temporary profile. It attaches the real native transport, decrypts its output and verifies the authoritative Slayer UPDATE_STAT experience values. Client input routing and rendering remain outside this probe.

The targeted actual-cache probe passed: all8 original master IDs verify, the Turael tile is clear, and73 combat ticks produced38 encrypted native frames including2 authoritative Slayer stat updates. It verifies one decrement per kill, no repeated reward during corpse/respawn ticks, and saved progress/XP restoration. The menu audit found that most950 masters use spaces in Talk to/Get task; the exact spaced and hyphenated labels are now both covered by a regression test while transforms remain refused. The combined packaged build will rerun the probe. No live account or running client/server is changed by these tests. Manual assignment, XP presentation and progress after relog still require the real client.

## Integration API

- Player.getNative950Slayer(): lazy State; snapshot calls writeSettings(map), restoration calls restore(map).
- Native950Slayer.masterCandidate(id)/verifiedMaster(id,definition): master population metadata; caller retains ordinary identity, model and collision gates.
- Native950Slayer.handles(npc,option)/interact(player,npc,option): existing routed NPC skill path.
- Native950Slayer.attackRefusal(player,npc): read-only native attack admission.
- Native950Slayer.onDeath(player,npc): one committed native credited death.