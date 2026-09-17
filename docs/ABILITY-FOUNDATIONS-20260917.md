# Revision-950 ability and interface foundations

## Delivery status

Partial delivery. The requested three fully implemented melee abilities and
three fully implemented magic abilities are NOT complete. Seven cache-backed
definitions are not seven executable abilities. No in-game client interaction
was performed for this change. No renderer, client executable, cache contents,
save schema or character data was edited.

Pre-edit GitHub backup: d8da4405a10d791bb36ca8a17ab3f5cf03245a2a.
Local saves/runtime backup: backups/pre-edit-20260917-022042-952.
Deployment snapshot: backups/ability-foundations-deploy-20260917-025333.
Engine and Kotlin overrides rebuilt successfully; server restarted as PID27512.
Jaxa's save SHA256 before/after deployment was unchanged:
B9EE2C9434EB55B02714EB72A8F38C6D77B5C30CB67C1D50E47F17AE5E155C97.

## 1. Drag failure

The source event mask enabled a one-parent drag depth but omitted bit23, which
bypasses the parent clipping restriction. This matches the reported icon being
confined to the ability-book box. The paired950 executable independently
confirms this flag; it is not inferred solely from an older client.

Executable: OpenNXT/data/clients/950/win64/original/rs2client.exe.
SHA256: fc7492548627a4068e88a7a45ba4f0be93004adf246bb4b174b24ae588768b36.
At VA0x1401ab907 it shifts the event mask by0x17 and tests bit0. The set branch
bypasses the parent-depth path; at0x1401ab935 the unset branch shifts by0x12,
masks with7 and follows parent components. tools/dis950.py can reproduce the
disassembly around0x1401ab8f4 using Capstone/pefile.

## 2. Drag changes

Native950ActionBar adds bit23 to book/bar ability events while retaining only
the existing operation/target permissions and destination bit21 on bars.
The book range now includes the cache's last key264. Open-interface validation
and decoded packet12 handling remain intact. Shortcut encoding stays
type<<17 | id<<4, verified by varbits1747/1748 and script11797. Main interface
1430 and Powers mirror1436 remain distinct. Fourteen saved settings retain the
same schema; no migration or profile rewrite is needed.

## 3. Drag verification

Automated checks cover the mask, Powers assignment, closed-source rejection and
an actual disk save/load into a fresh player using a disposable temp profile.
Mouse dragging, clipping, cursor placement and visible slot updates still need
manual verification in the real client, including rearrangement and relog.

## 4. Melee

Backhand14682, Punish14700 and Fury14701 have verified catalog definitions.
Only Backhand executes; its animation selection is corrected, but its existing
damage remains the earlier non-retail prototype. Punish and Fury intentionally
return unsupported instead of pretending that arbitrary damage implements them.

## 5. Magic

Impact14727, Combust14729 and Sonic Wave19342 have verified catalog definitions.
Only Impact executes the existing limited damage prototype. Combust's burn and
Sonic Wave's Flow effect are not implemented. Binding Shot remains supported
from the earlier prototype but does not count toward the three magic abilities.

## 6. Surge

Surge14726 is targetless. It checks Agility5, life/action state, active forced
movement and a clear path of up to10 tiles in the facing direction. Each edge
uses the existing collision checks; unloaded regions and active controllers
are refused. Fully blocked use does not consume its34-tick cooldown. It uses
the existing native force-movement adapter, not a teleport or new tick loop.

Current limited policy: no adrenaline change and independent of the damage
global cooldown. Charges, perks, combat-retention nuances and full retail GCD
parity are not verified/implemented. No animation or graphics are emitted:
legacy candidate18358 exists in950, but existence is not proof of its binding.
Visual movement/timing still requires an in-game check.

## 7. Animation and effect evidence

The old Backhand14212/Binding Shot14244/Impact14234 values were icon sprites,
not animation IDs. Script8426 uses struct parameter2802 as a sprite.
Actual animation selection uses struct parameter2915's enum keyed by equipped
weapon parameter686. Unarmed Backhand resolves to18154, verified by the real-cache
execution check. Weapon variants come from enum6692, not one hardcoded sequence.

Other inspected bindings: Punish enum6727, Fury6724, Binding Shot6714,
Impact10089, Combust10085 and Sonic Wave7122. Sequences are strictly decoded;
start graphics use sequence parameter2920 when present. Projectile/impact timing
and all effect semantics are not thereby proven. Cache assets are SHA256 pinned.

Tooltip script3111 dispatches to18633/18647/18648/18607/18610/18623/18621:

| Ability | Level | Cooldown ticks | Current950 tooltip evidence |
| --- | --- | --- | --- |
| Backhand | Attack31 | 25 | 95-105%, 3-second stun |
| Punish | Attack60 | 40 | 110-130%, 2.5x below50% target HP |
| Fury | Attack21 | 25 | 110-130%, next-melee critical buff |
| Impact | Magic31 | 25 | 65-75%, 3-second stun |
| Combust | Magic38 | 30 | 27-33% per burn hit, every3 ticks, 10 hits |
| Sonic Wave | Magic6 | 25 | 90-110%, Flow buff46308 for15 ticks |
| Surge | Agility5 | 34 | Up to10 tiles |

These percentages require a correct ability-damage base. The current engine's
Rs2CombatFormula/weapon profiles are not a verified950 EOC damage base. Copying
tooltip percentages onto that base would not satisfy the requested accuracy.
The exact Flow modifier, full critical model, damage base and effect scheduling
remain follow-up work. Existing basics still use20-100% of the native max hit,
nine adrenaline and a three-tick GCD; they are not newly certified retail rules.

## 8. Revolution

Added a bounded, ordered selector reading the current14-slot bar, excluding
utility/unknown definitions and delegating eligibility to the same combat gate
as manual execution. It respects that gate's target/style/resources/cooldowns.
Cancelled combat now clears queued abilities, and stopped/stunned attackers
cannot execute them. No random timer, background worker or extra per-tick scan.

Automatic activation is NOT enabled. Cache varbits21682 and21684 were located,
but the complete950 settings protocol/active-bar switching is not proven.
Settings integration and actual tick activation must follow verified execution
of the broader ability set, not precede it.

## 9. Logout

Existing logs prove the server issued close immediately after open. They did
not record which caller caused that close, so the exact live trigger is still
unconfirmed. The popup had been opened as a walkable type1 interface. Changed
it to modal type0 and fenced walk/world interaction input while the exit menu
is open, preventing click-through movement from dismissing it. Explicit close,
cancel, confirmation and legitimate CLOSE_MODAL reconciliation are retained.
Close logs now include the caller for a reproducible next test.

This is a candidate fix, NOT an in-game-confirmed resolution. No arbitrary
timeout ignores close packets, and no renderer or broad interface refresh was
changed. Automated modal/confirmation/lifecycle/final-save checks pass.

## 10. Reference implementations

Documents/RSPS/NocturneServer/Elveron876 has the useful EOC ActionBar and
PlayerCombatNew references: ordered selection, shared requirements, cooldown
groups and weapon-family animation enums. Its custom damage multipliers and
fast timer were not imported. The MATRIX/Matrix ActionBar is a different custom
eight-slot interface and not evidence of retail950 behaviour.

Darkan's IFDragOntoIFHandler validates open source/destination components;
ObjectHandler checks definitions/controllers and dispatches content plugins.
Its client IFEvents documents bit23, independently checked against950 above.
Darkan's custom AbilityManager is not a retail Revolution implementation.

## 11. Revision differences

950 shortcut types occupy seven bits; old four-bit packing is wrong. HUD books
and Powers books have different components. Melee uses enum10147; current
Fury/Combust rules differ from876. Old Slice/Wrack names/behaviour cannot simply
be reused. A legacy animation existing in the cache is not a verified binding.

## 12. Objects

Native950Interactions.object explicitly accepts ported skill handlers or the
validated bank route, rejecting other object actions. Unlike Darkan's generic
plugin dispatch, most legacy handlers are not connected to the native950 route.
Cache scenery/options do not provide server behaviour. This is a structural
content-adapter gap, not evidence of a rendering/performance problem. No blanket
dispatch into unverified legacy controllers/instances was added.

## 13. Changed files

Under Ataraxia950/game/com/rs/game/player/client:
- Native950ActionBar.java: mask, book range and selector bridge.
- Native950AbilityCatalog.java: seven definitions and animation resolution.
- Native950AbilityAssets.java: catalog verification.
- Native950Surge.java: targetless collision-checked movement.
- Native950Revolution.java: bounded ordered selection only.
- Native950MeleeCombat.java: animation, Surge entry, queue/state gates.
- Native950ExitUi.java: modal flag and close-call diagnostics.
- Native950Interactions.java and Native950Session.java: exit input fence.
- Native950ExitUiAcceptance.java: modal packet expectation.

Other files:
- Ataraxia950/resources/native950/ability-hub-assets.properties: verified pins.
- Ataraxia950/tests/modern947/Native950AbilityFoundationTest.java: five tests.
- Ataraxia950/tests/modern947/Native950MeleeCombatTest.java: no invented animation.
- tools/Native950AbilityProbe.java: definitions/scripts/sequence research and pins.
- tools/Native950AbilityAcceptance.java: real animation and disk-save restore.
- docs/WARS-AND-NATIVE-ABILITIES.md and this report.

## 14. Verification and manual checklist

Native950 regression: 1,284 tests, zero failures/errors, two skipped.
Real-cache ability acceptance passed, including isolated action-bar disk restore
and Backhand animation18154. Exit acceptance passed44 lifecycle/final-checkpoint
checks. Its first invocation lacked the data-root JVM property; rerunning with
-Dataraxia950.data=C:\Games\950OpenSource\Ataraxia950\data passed.

Manual next steps:
1. Drag Surge and Backhand from Powers beyond the book and onto the main bar.
2. Rearrange, log out/in and verify saved slots, without running testbar first.
3. Use Backhand on a training dummy with melee equipment; inspect animation.
4. Surge in open space, toward walls and diagonal corners; test blocked reuse.
5. Open logout and wait, then cancel; repeat and confirm. Verify bank/settings
   still work. If it disappears, capture the new close-caller log.
6. Do not expect Punish/Fury/Combust/Sonic Wave or automatic Revolution to run.

Remaining risk: the session world-input fence has code review and regression
coverage around its UI state but no dedicated queued-walk integration scenario.
Live graphics, drag gestures, logout appearance and FPS are not tested by these
headless checks. The requested full seven-ability combat milestone remains open.
