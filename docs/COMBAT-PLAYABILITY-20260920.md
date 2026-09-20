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

## Initial findings and remaining gaps

- Magic autoattacks enqueue a legacy Region projectile. The native world/frame
  path does not publish that queue. Damage/impact acceptance alone is not proof
  of projectile rendering. Exact 950 zone projectile encoding and publication
  are addressed by the tested candidate below; rendering still needs live verification.
- Offline `Native950NpcCombatCoverage` against the current cache/data accepted
  1,500 of 2,644 authored rows (489 multi-tile). Of 1,144 refusals, 633 are
  non-melee/unported, 191 lack a cache Attack option, 121 have invalid authored
  stats, 105 lack verified legacy identity, 85 lack stats, seven have invalid
  cache parameters, and two have unverified attack animations. Do not bypass
  these checks or make non-combat NPCs attackable.
- Accepted boss definitions are not proof that their complete mechanics, access,
  drops, or native presentation are playable. These require individual tests.

No production restart or client changes accompany this stance checkpoint.

## Projectile and spell candidate

The exact original 950 WIN64 client pinned by `Native950Protocol` has SHA-256
`fc7492548627a4068e88a7a45ba4f0be93004adf246bb4b174b24ae588768b36`.
Parser `0x140114a40..0x140114d31` consumes opcode 154, 21 bytes:
packed half-tile origin, flags, signed X/Y half-tile deltas, BE u24 source,
BE u24 target, BE u16 graphic, byte start/end height, BE u16 start/end cycles,
byte trajectory angle, BE u16 launch distance. Flags zero heights scale by 16
fine units; launch distance scales by four. This is not the old speculative
use of opcode 154 as a permanent-variable message.

Native entity resolver `0x140133fb0..0x1401340a8` dispatches the third byte:
1 to Client+0x19930's NPC manager, 2 to Client+0x19950's player array, with
the low 16 bits as index. Other kinds resolve empty. Project Undercut's
`Rev950ServerCodecsZone.kt`, `Projectile.kt`, and `Entity.kt` provide the
reference architecture; the native parser/resolver independently establish
the encoding (not arbitrary test fixture entity numbers).

Simple `World.sendProjectileNew` events now enter a bounded world-thread batch,
not the undrained legacy Region queue. The batch freezes positions/indices,
publishes after entity frames to ready, active sessions in the matching scene,
then clears only after all viewers. Special attachment/height-adjusted variants
remain on their existing unsupported path, as do object-centred projectiles whose
rotated footprints need separate treatment. No invented attachment encoding.

Casting-sequence resolution is independent of optional effect parameters.
Missing typed spell parameters no longer default to animation zero. Missing
projectile/impact metadata no longer erases a successfully decoded cast animation.

Exact current cache standard air spell witnesses (one-hand/two-hand sequence,
projectile, impact):

| Spell key | Struct | Sequences | Projectile | Impact |
|---|---|---|---|---|
| 14 | 14738 | 37251 / 37250 | 8936 | 8937 |
| 23 | 14748 | 37253 / 37252 | 8938 | 8939 |
| 37 | 14761 | 37255 / 37254 | 8940 | 8941 |
| 58 | 14779 | 37257 / 37256 | 8942 | 8943 |
| 73 | 14793 | 37259 / 37258 | 8944 | 8945 |

Unlike Elveron876's spell-struct graphic parameters, these exact950 projectile
and impact parameters live on the selected sequence. Do not copy the older
ownership blindly. Ranged ammunition has valid projectile parameters too, but
ranged autoattacks do not yet emit them. Delayed final-ammunition hits need an
explicit cancellation/exhaustion policy before extending that path.

Validation: 1,428 Java tests, zero failures, two existing skips; JAR build passed.
Real-cache melee acceptance: 449 ticks / 1,531 encrypted frames, including five
ordinary NPC death/drop/respawn loops, player recovery, pursuit and collision.
Combat-style acceptance: 69 checks. None of these prove Vulkan rendering.

## Complete cache-shaped ability inventory

`Native950AbilityCoverage <cache> <report.json>` generates
`protocol-analysis/ability-coverage-950.json` without world/save access.
It scans every index-22 structure with typed name/key/tier/cooldown parameters,
then records all typed struct-enum memberships and evidence hashes.
Modern enum opcode102 STRUCT type73 and legacy opcode2 character J=74 are
distinguished. Every implemented ability must retain its known native book/key
membership or generation fails.

Result: 171 ability-shaped structures, 31 partial implementations, 140 missing.
This deliberately includes alternate/obsolete/unmounted definitions: it is not
a claim that the native UI exposes 171 distinct usable abilities. No ability is
labelled fully working merely because an execution branch exists.

## Boss stress gate and multiplayer regression boundary

`Native950MeleeAcceptance cache --bosses` adds optional Dagannoth Rex (2883)
and Barrelchest (5666) profile lifecycle witnesses at isolated clear footprints.
This mode equips an ephemeral level-99 unarmoured player with weapon45445 and
retains the ordinary probe's guaranteed-accuracy, maximum-damage rolls for BOTH
sides. It does not alter boss definitions or any saved character.

The first run FAILED on Rex: the player died before completing the boss loop.
Barrelchest was consequently not reached. This adversarial fixture is not proof
that ordinary player-geared/food-supported boss combat is impossible, but it
does not meet the boss acceptance criterion. No damage values or assertions
were weakened to produce a pass. The five ordinary NPC witnesses passed first.
Boss-area access, full mechanics, and a realistic equipped encounter remain open.

Existing green regressions already cover manual queue/GCD revalidation, channel
interruption, Revolution order/availability, single-combat ownership, disconnect
cleanup, and original damage-ledger drop attribution. They do not establish live
native settings behavior or shared multi-combat boss support. No new ability,
Revolution, prayer or ranged hit-timing implementation is claimed by this pass.

## Candidate safety

Combat changes are staged in source/build output only. The normal Play server
and open client were not restarted; the live runtime remains the prior protected
build. No workspace/client/launcher or saved-player files were edited.
Before the final diagnostic edit, saves, workspace sidecars, runtime JAR and
overrides were snapshotted under `backups/pre-edit-20260920-030022-394`.
Vulkan stance/projectile verification is pending a controlled candidate launch.
