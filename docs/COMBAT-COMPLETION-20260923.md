# Combat completion candidate - evidence and continuation

## State and protection

This is a substantial **partial combat implementation**, not a claim of retail
combat completion. Work was offline. No agent deployment, restart, login or
character edit occurred. Apply Staged Update.cmd is the user's installation entry.

Rollback source: `1e22dc112d2e2d1d99130bfab889804bba1fedff`.
Pre-edit GitHub backup succeeded; private snapshot:
`backups/pre-edit-20260923-042146-169`.
First pushed implementation checkpoint: `d646ba3794e5932230be119ce23fad16a2b6321c`.
The final staged manifest is `protocol-analysis/playability-candidate-20260923.json`.

Save audit: seven of eight protected files match the initial snapshot. One
character file was last written at 04:23:17 while the pre-existing live session
was still active; server logs end with character removal. This is consistent with
that session's save, before offline validation, rather than an offline test write.
It was preserved, not rolled back. Current private hashes are recorded in
`backups/pre-edit-20260923-042146-169/combat-pre-stage-save-hashes.json`.
Do not publish character files or private manifests.

## Machine-derived coverage and gates

* 1,500 JUnit tests: zero failures/errors, two skips.
* 74 explicit ability definitions, up from 60. Do not call these COMPLETE.
* 189 ability-shaped cache structs retained, including structs without cooldowns.
  135 are reached by reviewed native books. Of those, 75 are PARTIAL (including
  the separately witnessed Necromancy automatic attack), 60 MISSING, zero COMPLETE.
  54 other structs remain conditional or have unproven player-facing membership;
  they are not silently labelled obsolete. Availability/unlocks remain player-specific.
* Real-cache harness: 723 checks, 203 manual activations, 135 projectile launches,
  64 two-player cycles across all four styles, equipment/bar changes, native
  settings routing, source revocation, target handover, detach and target removal.
  It uses ordinary built-in high-tier equipment without almighty; deterministic
  damage/accuracy and a clear collision seam mean it is not a balance/visual test.
* Seven generic boss encounters pass: Rex2883, Barrelchest5666, Supreme2881,
  Prime2882, K'ril6203, Bork7133, Giant Mole18932. 1,491 owner-thread ticks and
  8,230 encrypted frames cover ordinary resources, retaliation, prayer, death,
  private loot, corpse lifetime and same-index respawn. These are not completed
  retail encounters; no claim of newly implemented burrowing/minions/special phases.
* NPC metadata admission: 1,880/2,644 authored profiles, up from1,867 (+13).
  Zero authored respawn delay now means one-life, not invalid stats. Special-style
  rows232 remain refused; 1,193 admitted profiles lack a verified attack animation.
  Missing presentation is explicitly separate from mechanical admission.
* Bank gate:149 encrypted actions,97 ticks,7,113 frames, including P0 close ordering,
  actual bank handlers, library and sort. Library global search/loadout isolation,
  equipment safety (876 frames), combat resources (69 checks), diagnostic spawning,
  and exit UI/session persistence (63 checks) also pass.

Reports:
- `protocol-analysis/combat-canonical-matrix-950-20260923.json`
- `protocol-analysis/combat-acceptance-950-20260923.json`
- `protocol-analysis/combat-npc-summary-950-20260923.json`
- `protocol-analysis/combat-validation-20260923.json`

The canonical matrix has per-group counts, typed requirements, native enum
membership, effect/channel/resource fields and per-struct offline observations.
No inventory scanner or generic damage test promotes an ability to COMPLETE.

## Implemented shared changes

`Native950MeleeCombat` owns the world-thread scheduler. Manual work executes at
readiness without waiting for the previous animation to finish; manual queue
wins over Revolution. Auto attacks remain animation/channel gated. Secondary
players use the same projectile, cost and impact path. Attacker activity no longer
inherits the retaliation owner's paused state. Provoke, stop, disconnect and NPC
removal maintain independent targets. Pending NPC strikes retain their original
recipient through aggro changes.

Area/stun/healing hooks now run at impact for immediate and delayed attacks.
Area candidates check line of sight, plane, Slayer and Dungeoneering requirements.
They may already be in another player's fight; killing one retires all its target
owners while preserving unrelated fights. Provoke no longer causes incidental damage.
Tendril recoil snapshots base ability damage, and lethal recoil stops channel
construction without overwriting the death animation.

`Native950ActionBar` publishes queued overlay state using exact950 components
1430:70,83,...239 -> CS5899(slot1..14,1003,overlay), varps4164 and5861, and
CS6568/6505. Existing activation/cooldown CS6570 remains. No global bar remount.

Revolution uses the same executor before auto attacks, respects ordered slots,
manual priority and enabled tiers. Native settings row1306/category25 indices6..10
map to range/Basic/Threshold/Enhanced/Ultimate. CS2526 reads range varbit38639 and
inverted disable flags38666/38708/52329/38709 (param7524). Slider CS10451 first
selects its row on365:19; CS10450 sends the zero-based value on365:20. All row
selections revoke unrelated slider authority; invalid/closed/wrong-page values
cannot change the range. Settings and bar bootstrap publish these fields. The
old `actionBar.revolution` integer packs the extra options without increasing the
bounded save-key count; old0/1 values retain nine-slot defaults. Runtime hashes pin
row, structs, scripts and varbits. Physical native slider behaviour is LIVE PENDING.

## Abilities and exact950 evidence

New explicit paths: Soul Strike48299, Volley48301, Blood Siphon48309,
Living Death48324, Provoke14712, Cease45340, Dive47129, Bladed Dive1488,
Galeshot52799, Shadow Tendrils28177, Imbue Shadows52796, Smoke Tendrils28180,
Limitless37203 and Eat Food44225.

Galeshot/Imbue/Tendrils follow tooltip scripts15738/18689/16279/18625 and buff
CS11087. Limitless changes threshold admission to its real cost, not a free cast.
Sailfish42251 now uses exact950 params963=2400 life points and6924=10% overheal,
ordinary food delay, slot/metadata/controller checks and verified eat sequences.
Unsupported legacy controllers refuse Sailfish rather than bypass restrictions.

Necromancy resources are player/session owned in `Native950NecromancyResources`:
varp10986 Necrosis,11035 souls; CS17445 Finger cost; CS17458/18658 Touch;
CS17459 offhand passive48397 cap; CS18660 Sap; CS18662 Volley;
CS18671 Living Death modifiers. Necrosis no longer has a fabricated30-second
expiry; souls expire six seconds outside combat and clamp on offhand changes.
Gameplay references: https://runescape.wiki/w/Necrosis and
https://runescape.wiki/w/Residual_Soul. Some teleport-specific clearing remains unported.

Blood Siphon CS18670: four22..28% pulses up to25 targets within two tiles,70%
healing, then a117..143% primary hit plus accumulated heal value. State is per cast,
shared only by its pending hits; interruption/disconnect discards it. It still
needs visual/timing acceptance and full retail modifiers.

Necromancy automatic attack uses the already bound sequence35449 with exact
caster7853 and impact7854 from params2920/2933. That sequence has no2940 projectile
binding; none is invented. Live hitmarker/style presentation remains PARTIAL.

Dive packet opcode85/13 bytes was independently traced in the original950 binary
SHA256 fc749254...: descriptor0x140e94830, writer0x1400e4e19. Slot BE16, item
middle/high/low24, sourcehash BE32, X BE128, Y LE16. Undercut rev950 codec only
corroborates it. `Native950Interactions` validates mounted source, slot and item;
`Native950Surge` checks level, weapons, range and each collision step/diagonal.
Dive uses the chosen tile and shared cooldown with Bladed Dive. Bladed endpoint
area impact is bounded; no guessed animation/graphic is added.

## P0 and preserved library

FACT: bank onLoad CS13353 acquires keyboard context24. User confirmed
CS8841(24,0) restores a dead key after bank/library close. CS1364/8838 did not;
broad1998 did, but is not the shipped fix. Both close paths retain original
CS9299 before IF_CLOSESUB, releasing context24 and search context11. Repeated
server-side close/input gates pass; physical repeated bank/items/key acceptance
is still required. See `docs/P0-BANK-KEYBOARD-CONTEXT-20260923.md`.

Curated native bank design/order, global item search, isolated developer presets,
compatible ammunition and bank sorting are preserved. No new cache patch was
required for this combat pass. Bork's authored custom drop override is now routed
through private native loot; two unsafe noted-gem rows1622/1624 are excluded by
existing metadata admission rather than guessed or substituted.

## Precise unresolved work / next locations

FACTS, not completed claims:
- Conjures/commands/incantations remain missing. Trace actor ownership, resources,
  offhand admission and exact950 NPC/effect contracts before adding them.
- `Native950MeleeCombat.abilityRefusal`: Necromancy param9107 offhand admission
  still needs its native contract. Living Death param2535 is not silently used as2914.
- Skulls bounces, Scythe recasts, Invoke/Threads/Darkness/Split Soul are unfinished.
  Skulls tooltip CS17730 states225..275% per hit, range6, four bounces (+2 conditional);
  current simplified hit coefficients/bounce behaviour still need replacement.
- `processPendingHits`/`processDamageOverTime`: explicit cancellation/target swaps
  still discard already-launched effects; valid launched-effect persistence needs
  a target-independent lifecycle. Do not make channels persist accidentally.
- General damage, bleed scaling, AoE geometry/modifiers and crit identities remain
  alpha/partial. Necromancy uses the inherited magic hitmark category until an
  exact native semantic mapping is verified; do not invent a wrapper.
- Native cursor cancellation, Dive/Bladed animation/FX and kill cooldown reset
  need further work/live acceptance. No visual claim follows merely from opcode85.
- Boss specials/minions/phase scripts, full loot families and controlled-area entry
  remain partial. Missing910 cache assets prevent safe regeneration of some paired
  animation bindings; do not substitute a nearby numeric animation.
- The old `--boss-stress` fixture is not an acceptance claim: it uses unrealistic
  guaranteed rolls/insufficient supplies. The successful gate is `--bosses`.

HYPOTHESES: visual feedback or slider focus can still expose client-only state
not executed by offline hooks. Use the single live checklist to investigate;
do not reset/remount the bar after every command to hide a focus failure.

## Reproduce and continue

Build: `./Build-Ataraxia950.ps1 -Tasks test,jar`.
Java8 classpath must put the fresh build JAR before `OpenNXT/runtime/lib/*`.
Set `-Dataraxia950.data=C:\Games\950OpenSource\Ataraxia950\data`.
Run `com.rs.game.player.client.Native950MeleeAcceptance cache --bosses`.
Compile/run `tools/combat-pass/Native950CombatPassAcceptance.java` in an isolated
JVM with output JSON; it never loads account files. Run AbilityCoverage and
`tools/combat-pass/build_matrix.py` to regenerate the canonical matrix.
Run BankAcceptance, LibraryFollowupAcceptance, CombatStylesAcceptance,
EquipmentAcceptance, DiagnosticSpawnsAcceptance and ExitUiAcceptance with `cache`.

Live acceptance: `docs/COMBAT-LIVE-CHECKLIST-20260923.md`, one A-H sequence.
No live verification was performed by this pass. The user applies the staged
update and the agent reads diagnostic logs afterward.
