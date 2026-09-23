# Combat completion pass — continuation and evidence

Work is offline. Baseline rollback: `1e22dc112d2e2d1d99130bfab889804bba1fedff`.
Pre-edit backup: `backups/pre-edit-20260923-042146-169`. No production restart,
save edits, cache overrides or installation have been performed by this pass.

## First tested checkpoint

1,491 JUnit tests: zero failures/errors, two skips. Exact read-only cache admission
passes for 68 catalog definitions (baseline 60). This is implementation coverage,
not 68 complete abilities. The 171-structure inventory includes alternates and
unreached structures and must not be used as a canonical completion denominator.

* Shared-target attacker eligibility no longer inherits the retaliation owner's
  paused state. Removing an NPC clears every attacker's target and pending work.
  Retaliation handover restores the selected replacement's active state.
* Secondary players use the same auto/projectile/resource path as the primary.
  NPC pending projectiles retain their original recipient through aggro changes.
* Revolution uses the same immediate executor before auto attacks; finishing a
  visual animation does not impose an extra ability delay. Manual queue wins.
* Native queued overlay: exact component1430:70,83,...239 hooks call
  CS5899(slot1..14,1003,overlay). It reads varps4164 (slot) and5861 (bar), then
  invokes CS6568/6505. Queue replacement, execution and cancellation publish
  those values. Activation/cooldown retain CS6570. No action-bar remount workaround.
* Necrosis varp10986 and residual souls11035 are session/player-owned. Touch,
  Finger, Sap, Strike and Volley generate/consume resources at execution, never
  queue admission. Finger cost follows CS17445; soul cap follows CS17459 and
  offhand passive param8928=48397. Living Death follows CS18671 resource/cooldown
  modifiers. Blood Siphon has channel/healing foundation. All remain PARTIAL.
* Provoke and Cease are routed through shared ownership/cancellation semantics.
* Native ground-target opcode85 length13 was independently traced in the exact
  original950 binary (SHA256 fc749254...; existing binary witness). Writer
  0x1400e4e19 uses slot BE16, item middle/high/low24, source BE32, X BE128,
  Y LE16. Undercut rev950 codec corroborates this; it is not the identity source.
  Dive/Bladed Dive use the native CS6995 target verb and targeted packet, validate
  mounted source/slot/item, loaded map, level, weapons, cooldown and clipping.
  Collision-tested chosen-tile movement replaces no forward-only approximation.

## Precise remaining work

Impact hooks need consolidation: projectile secondary AoE/stun/healing currently
have incomplete timing. Provoke must be verified to cause no incidental damage.
Blood Siphon final burst/AoE, Skulls bounces, Scythe recasts, conjures/commands,
Necromancy offhand admission and exact resource expiry remain unfinished.
Living Death uses param2535 rather than2914; its sequence needs binding evidence.
Dive/Bladed Dive animation/graphics and physical native cursor/cancel need live
verification. Bladed Dive kill cooldown reset is not implemented.
Revolution's configured native range remains to be traced (currently nine slots).
Broad canonical expansion, real-equipment prolonged harness, boss/NPC expansion,
machine-readable lifecycle matrix and final staged installer remain to be done.
No new visual lifecycle is claimed live-tested by this checkpoint.

## Relevant files and reproducible checks

`Native950MeleeCombat` owns scheduler/ownership; `Native950AbilityCatalog` owns
definitions and cache verification; `Native950NecromancyResources` owns stacks;
`Native950ActionBar` owns native queue state; `Native950Surge` owns movement;
`Native950Actions` decodes opcode85; `Native950Interactions` validates source.
`tools/combat-pass/Native950CombatCacheProbe.java` exports read-only cache witnesses.

Run `./Build-Ataraxia950.ps1 -Tasks test,jar`.
Run Java8 with fresh build JAR first and `OpenNXT/runtime/lib/*` second:
`com.rs.game.player.client.Native950AbilityCoverage cache <report.json>`.
Use `-Dataraxia950.data=C:\Games\950OpenSource\Ataraxia950\data`.
Never allow the deployed old JAR to precede the candidate in offline classpaths.

The P0 authored bank cleanup remains unchanged: CS9299 before unmount releases
context24 and native search context11. User confirmed CS8841(24,0) recovery;
automatic repeated bank/library close still requires final live acceptance.
