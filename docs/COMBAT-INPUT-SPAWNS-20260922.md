# Combat input, diagnostic spawns, and hitmarks (2026-09-22)

## Implemented in the staged candidate

- A ready manual ability executes on its input turn, including while an earlier
  animation is still finishing. GCD, its own cooldown, and active channels still
  enforce bounded queuing and execution-time validation. Queue chat spam is
  suppressed; the native input path and cooldown script 6570 remain intact.
- A secondary attacker on the same NPC retains a usable ability target, pending
  hits and damage-over-time, and the same Revolution selection path as the first
  attacker. The NPC still has one retaliation target.
- `;;npc <id> [amount]` places up to 50 one-life diagnostic NPCs on separated,
  collision-valid tiles ahead of the player. `;;npcrepeat <id> [amount]` and
  `;;npc repeat <id> [amount]` retain combat respawn. `;;npc nex` lists concrete
  cache NPC IDs to choose. All commands are in `;;commands`.
- `;;bank` marks only a successfully opened command bank as remote, so its
  native deposit/withdraw actions do not require a nearby chest. Closing the
  bank retires the remote scope.
- Cache-pinned revision-950 melee, ranged and magic hitmark wrappers now carry
  their own style to the viewer-aware NPC/player mask. Their critical wrappers
  are pinned too. Combat Alpha uses a bounded 10% player critical chance and a
  25% damage increase; this is not claimed as retail 950 balance.

## Verification

The full Java test suite and exact-cache melee, bank and diagnostic-spawn
acceptance programs pass. The staged installer `--check-only` verifies the
candidate hash without installing or restarting anything. These checks do not
prove Vulkan visuals; the current server remains on its older process until
Jack installs the candidate and restarts at a convenient time.

## Still open

- Native yellow press feedback and visible queue state require a live client
  check. A native queue-indicator mapping for exact 950 has not been established;
  no guessed varp or overlay was sent. Script 6570 still sends cooldown timers.
- Necromancy icons in bar shortcuts and the empty movable Necromancy window
  remain unresolved. The six currently supported Necromancy abilities and
  Powers drag path were not expanded or remapped in this candidate.
- Necromancy still uses the existing magic-style hitmark. A distinct 950
  Necromancy hitmark definition has not been pinned, so no numeric type was
  invented.
