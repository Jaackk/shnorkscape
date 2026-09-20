# Combat playability pass

Protected workspace/normal Play baseline: `6115b21`. Workspace is closed.

## Native stance lifecycle

`Native950World.worldTick` processes player movement/entity state before
`Native950MeleeCombat.afterMovement` produces attack animations. Frame completion
then clears animation masks. The inherited `CombatDefinitions.processCombatStance`
required a non-null animation to enter combat stance, so native target acquisition
could never satisfy that requirement at the time of its stance check.

Native combat now uses its existing target/combat-delay ownership to enter stance.
Legacy combat retains its animation prerequisite. This does not force animations,
change attack cadence, or introduce a new BAS mapping: the existing cache-backed
appearance encoder chooses the weapon's combat BAS from the stance flag.

Regression: `Native950MeleeCombatTest` verifies entry without an attack mask and
retention across mask resets with a target. Focused test suite passed. Vulkan
visual confirmation of sustained weapon stance remains required.

## Investigated, not yet repaired

- Magic autoattacks enqueue a legacy Region projectile. The native world/frame
  path does not publish that queue. Damage/impact acceptance alone is not proof
  of projectile rendering. Exact 950 zone projectile encoding and publication
  need separate regression coverage before deployment.
- Offline `Native950NpcCombatCoverage` against the current cache/data accepted
  1,500 of 2,644 authored rows (489 multi-tile). Of 1,144 refusals, 633 are
  non-melee/unported, 191 lack a cache Attack option, 121 have invalid authored
  stats, 105 lack verified legacy identity, 85 lack stats, seven have invalid
  cache parameters, and two have unverified attack animations. Do not bypass
  these checks or make non-combat NPCs attackable.
- Accepted boss definitions are not proof that their complete mechanics, access,
  drops, or native presentation are playable. These require individual tests.

No production restart or client changes accompany this stance checkpoint.
