# Playability checkpoint, 23 September 2026

Status: implemented, automated-tested, deployed. Live behavior verification pending.

## Live input investigation

Deployed the existing `ab1f784` diagnostic engine with SHA-256
`631FFD999D0F5D44C65E88214E22769A4EEA2DC1662FA6B3AA3B639E517EBFFB` before testing.
Jack confirmed both key and mouse activation worked through bar switches in this
session. The intermittent stopped-keybind state has **not** been reproduced or
fixed. Keep the bounded diagnostic enabled to capture its first failure.

Ranged abilities stayed dark while executing. Jack tried multiple normally
equipped ranged weapons, then confirmed no ammunition was equipped. This is a
separate, explained presentation mismatch: infinite ammunition bypassed server
cost checks, but exact950 CS2660 still checks equipment94 slot13 against the
weapon's required ammo category (param21). This is not evidence of dead input.

`;;almighty` and `;;infammo` now project compatible arrows/bolts into the native
client equipment container when the real ammunition is absent or incompatible.
The projection follows weapon changes, including the Dorgeshuun bone-bolt case.
Authoritative equipment, inventory, appearance and saves retain the user's own
items. The supplied stack cannot be removed into inventory. Disabling the mode
restores the real ammo display. Modes remain transient and reset on logout.

## Corrected exact950 Necromancy evidence

The previous handoff's identification of movable1887 as Necromancy was wrong.
1887:6 onLoad calls8422 with category10: Teleport Spells, using magic enum6740.
CS8437 maps categories4/14/15 to Necromancy enum16973.

Native version6 onLoad hooks (all now SHA-pinned) are:

| Book | Powers interface | Movable interface | Workspace slot | Native category |
|---|---:|---:|---:|---:|
| Necromancy |1207|1219|42|4|
| Necromancy Abilities |1211|1220|43|14|
| Necromancy Incantations |1214|1221|44|15|

Their hooks call8422 with `(face:7, face:8, face:11, category)`.
Exact950 CS8423 confirms slot42/43/44, and enum7716 resolves them to
struct48237/48238/48239 with the corresponding names. These panels are now
attached on login without changing saved workspace visibility. Server book
click/drag routing and event masks now use the same faces. Incorrect1215/1887
Necromancy routing was removed.

CS6995 distinguishes native shortcut type17 (enum16973) from type7 (prayer
enum6739). Saved/internal category7 is retained for compatibility, but converted
to17 at the native client wire boundary. This fixes the incorrect icon identity
without rewriting existing saved bars. Live icon/population checks remain needed.

## Other implementation

- Developer `;;npc` placement is a compact row directly ahead of the player,
  using real footprints and minimal spacing. It bypasses ordinary map clipping,
  retains world bounds and actual player/NPC overlap checks, and preserves the
  existing one-life/repeat/clear lifecycle. Training-dummy rules are unchanged.
- Physical bank booths/chests/counters use current-cache name and Bank/Use menu
  semantics. The original remote-bank scope and normal reach checks remain.
  A real booth36786 now exercises deposit/withdraw and out-of-reach refusal.
- Ordinary ladders/stairs initially cover Lumbridge and southwest Varrock,
  requiring actual reciprocal map objects on the destination plane. The explicit
  Lumbridge trapdoor36687 and cellar ladder29355 form a tested round trip.
  Landings use free-floor collision plus reciprocal reach; a wall edge is not
  incorrectly treated as an occupied tile.
- Ordinary door IDs12348/36844/36846/45476 are admitted only as shape0 with Open,
  using the existing door handler. Door admission is checked; the complete
  opening/reset visual loop is not yet accepted. No arbitrary Enter teleports.

## Validation

- Full `Build-Ataraxia950.ps1 -Tasks @('test','jar')`: passed, 1463 tests, zero failures/errors.
- `Native950EquipmentAcceptance`: passed, 764 parsed encrypted950 frames,
  including automatic ammo, weapon switches, toggle refresh, virtual removal
  refusal and real equipment/save preservation.
- `Native950BankAcceptance`: passed, 86 encrypted actions, 68 movement/entity
  ticks, 2009 parsed frames, including physical booth and remote-bank regressions.
- `Native950WorldTraversalAcceptance`: passed, ladder/stair ascent/descent and
  cellar round trip against actual950 map/collision.
- `Native950DiagnosticSpawnsAcceptance`: passed, actual-cache compact group
  placement, registration and object placement regressions. Geometry tests cover
  all eight directions and sizes1/2/5/16/64. Expanded large-NPC/lifecycle acceptance
  remains to be added; existing lifecycle code was not changed.
- Native lobby override compilation and `Verify950Kt`: passed, including new
  book mounts, no forced workspace visibility and existing protocol/ribbon gates.
- Installer check-only and installation: passed after Jack confirmed both clients
  closed. Pre-install backup: `backups/playability-update-20260923-012443-121`.
  Server29784 and Vulkan28620 launched after installation; all four deployed
  hashes match the manifest. Restored-profile world attachment succeeded.
  One profile/workspace file differed from the snapshot after the user logged
  back in; no account or workspace file was modified by this implementation.

## Candidate and remaining work

The versioned manifest `protocol-analysis/playability-candidate-20260923.json`
pins the engine and three bootstrap classes staged under
`dist/playability-20260923`. Engine SHA-256:
`B64686313DF712D716958048A6F16E41D902D21DAAEC8D1C8672826DF0AF9143`.
`Apply Staged Update.cmd` now invokes `Apply-PlayabilityUpdate.ps1`, which checks
all four files, refuses running Java/Vulkan clients, backs up runtime files and
player/workspace directories, verifies copied files and restores runtime on a
copy failure. It does not stop processes itself.

P0 remains unresolved. This checkpoint does not claim retail combat visuals,
complete Necromancy execution, Revolution checkbox repair, more boss mechanics,
expanded NPC combat admission, broad dungeon access or a fresh multiplayer test.
The ranged slot containing struct52796 is Imbue: Shadows and remains unsupported;
do not mistake that explicit refusal for the intermittent keybind failure.

Ammo/bank second candidate, 23 September 2026:
Jack LIVE-CONFIRMED first-candidate ranged lighting with almighty and populated
Necromancy books. P0 intermittent keybind death remains unresolved.
Tier-aware ammo now supplies matching current-cache tier40/70 ammunition and
Primal tier99 arrows/bolts for top-tier weapons; Karil racks and Dorgeshuun bone
bolts retain compatibility. Equivalent-tier owned enchanted ammo is preserved.
Combat damage remains capped by weapon tier; virtual ammo never changes saves.
Rangegear grants Primal arrows and bolts instead of dragon bolts.
Bank capture: augmented Dark Sliver52083 x2 sends cleared actor48447 for one
withdrawal. Preserve stack-mode2 and accept this individual-item prediction,
with quantity-change protection against repeated same-tick clicks. This is not
proof of degradation or ID corruption; no item data has been reset.
AUTOMATED-TESTED:1463 tests, zero failures/errors; actual-cache equipment820
frames; bank94 actions/76 ticks/2177 frames including both augmented copies.
Installer check-only passed. Second candidate is STAGED, NOT YET DEPLOYED or
LIVE-CONFIRMED. Engine SHA256:
584E19B3C87627C7A7FE2315A02A999CE8844EACA8AC40BD7DE27EDB46367329
Previous staged files: backups/pre-edit-20260923-012721-196/staged-playability-before-r2.
The earlier engine SHA in this document describes the currently running first
candidate; the manifest now pins the staged second candidate.
USER GATE: finish and verify ammo/bank fixes, then ask exactly:
Are you ready for me to start with the prompt?
Wait for the answer before reading/starting the new bank-style ;;items prompt.
That work has not started. Confirm both clients closed before deployment.
