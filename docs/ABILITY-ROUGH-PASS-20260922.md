# Ability Rough Pass, 22 September 2026

## Scope and Runtime Boundary

User requested a breadth-first rough pass, prioritising Sunshine, ultimates and
defensives, explicitly skipping complex effects rather than blocking the pass.
No live restart, live JAR replacement or player-save edits are permitted for this
task. The staged candidate also contains the earlier public Chat and Follow fixes.
Workspace/editor/durability, authentication, Item Browser and native manual-press
acknowledgement are unchanged.

Pre-edit remote checkpoint: `6aab69bc7f3f24ddae898d2dd1868cfc1fdba775`.
Local backup: `backups/pre-edit-20260922-180908-831`.

## Implemented Breadth

The explicit executable catalogue grows from 31 to 54 definitions. New entries:

- Sunshine: timed, caster-location-bound +50% magic damage, native cache animation
  lookup, 100% adrenaline cost, 50-tick duration, 100-tick cooldown.
- Anticipation, Freedom: timed damage/stun protection; Freedom clears existing
  stun/freeze and grants temporary freeze immunity. Original immunity is restored.
- Resonance, Divert: consume the next damaging hit, heal/generate adrenaline;
  mutually exclusive with shared cooldown. Divert has bounded diminishing returns.
- Preparation: shortens Resonance/Divert cooldown after hits and publishes the new
  cooldown to the native bar.
- Reflect, Debilitate, Barricade: incoming reduction/reflection, attacker-specific
  reduction, and timed blocking. Barricade duration uses evidenced shield level.
- Rejuvenate, Immortality: healing over time/drained-stat recovery and timed damage
  reduction with one-use lethal-hit revival.
- Revenge, Natural Instinct, Devotion: capped damage stacks, doubled basic adrenaline
  generation, protection-prayer blocking and bounded on-kill extension.
- Chaos Roar: the next targeted melee ability is empowered, including its scheduled
  hits/bleed; expires after the cache-backed duration.
- Pulverise, Bombardment, Corruption Blast/Shot, Rend, Sacrifice: rough direct,
  bounded area, bleed and damage-to-heal effects through existing combat ownership.
- Escape: backwards collision-checked movement through the existing native
  force-movement path. No guessed animation from another revision.

Shield buffs expire on shield removal. Defence/constitution abilities no longer
require an invented weapon style. Native requirements, tier, cost/gain, book key,
cooldown and fixed buff durations are checked against the exact 950 cache.
Defensive tier-3 thresholds retain 50% admission / 15% spending; modern tier-2
damage abilities keep their distinct cache costs (including Corruption's 20%).

## Native Books: Root Routing Correction

Existing code incorrectly classified `1449`, `1882`, `1883` as ranged surfaces.
Exact950 `564 -> 8426 -> 8437` proves:

- `1449:1`: Defence (book12 -> enum6736 -> action-bar type3).
- `1882:1`: Constitution (book13 -> enum6737 -> action-bar type4).
- `1880:1` and `1883:1`: combined defensive books (book5), category0=Defence,
  category1=Constitution. Category children7/8 on component7 select them.
- Category varbits36453/36454: domain0, varp3705, bits24..27 / 28..31.
- `1456:1` is the additional ranged surface, not a defensive book.

Native category writes use slice packets, not whole-varp resets. Existing compact
bar saves already have room for types3/4; no save schema migration is required.
Native hide-unavailable filters44637/27344 are cleared when configuring the bar,
so native entries can be inspected even when unusable. This is not a quest/codex
unlock grant and does not manufacture obsolete/internal entries as player abilities.
Conditional upgrades and situational entries remain owned by native cache rules.

Script564 SHA256: `3aa07cc19f10efaad3a46850806c8eb2c1edb48791c0d036398dff6ffab2d105`.
Scripts8426/8437 were already pinned. Runtime verification additionally checks the
four relevant category/filter varbit domains and bit slices.

References: current exact950 cache; inherited Ataraxia effect/combat infrastructure;
Vernox ActionBar defensive lifetimes as behavioural clues; Undercut AbilityBooks
for the defence/constitution/category architecture. Numeric mappings come from950.

## Deliberately Partial / Deferred

This is playable Alpha coverage, NOT a claim of retail-complete abilities:

- Damage coefficients remain explicit Alpha approximations. No critical-hit or
  per-boss powerful-hit exception overhaul is included.
- Sunshine lacks its separate enemy-area damage-over-time component. Presentation
  uses existing exact-cache resolution and still needs live inspection.
- Pulverise lacks its weakening side effect; Corruption does not yet spread.
- Divert diminishing-return formula is conservative Alpha behaviour; Revenge,
  reflect and revival interactions still need boss-specific live coverage.
- No new weapon specials: weapon-specific dispatch, costs and target effects need
  a separate verified path. Legacy callbacks are not invoked blindly.
- Dive/Bladed Dive native tile targeting, tendrils, Magma Tempest, necromancy,
  Onslaught/Reprisal/Transfigure, group-healing entities and conditional upgrades
  remain deferred. Escape has movement but no newly verified ability animation.
- No claim that all 171 inventory candidates are genuine simultaneously visible
  abilities. The inventory includes aliases, situational and internal entries.

Unsupported abilities still refuse explicitly; visibility is not an execution claim.

## Validation and Manual Install

Run Java regression suite and exact-cache `Native950AbilityCoverage`.
`tools/ability-pass/Native950AbilityPassAcceptance.java` verifies native defensive
book categories, compact saved bindings, a cache-backed spirit shield and executes
all self buffs in isolated ephemeral players. It never reads character profiles.
Unit tests cover Sunshine area/plane/expiry, immunity cleanup, defensive costs,
shield cancellation, capped stacks, revive, next-hit healing/blocking and adrenaline.

Candidate installation is intentionally manual: close clients when ready, Stop.cmd,
then `Apply Staged Update.cmd`, then normal Play.cmd. The installer checks the
candidate hash, refuses a running Java/client process, backs up engine/player and
workspace files, and does not restart anything. The old Chat/Follow installer name
delegates to the cumulative installer so it cannot accidentally install an older JAR.

Live checks required: Sunshine with magic on a dummy; defence/constitution book
drag and keybinds; Resonance/Reflect/Barricade with a real shield; Freedom under a
stun; Immortality against a normal NPC; switch/remove shield; verify Chat/Follow.
No automated result here establishes Vulkan visual correctness.

Completed gates: 1,451 tests, zero failures/errors, two skipped; 54 cache catalogue
definitions verified; 16 self buffs executed by the offline gate. Existing
`Native950MeleeAcceptance --bosses` passed 1,149 owner-thread ticks and 5,538 parsed
encrypted950 frames, including Rex and NPC5666 death/respawn and recovery checks.
The initial boss-probe invocation lacked its data-root property; rerunning with
`-Dataraxia950.data=C:\Games\950OpenSource\Ataraxia950\data` passed.

Staged JAR: `dist/staged-update/ataraxia-950-1.0-UNTRACKED.jar`.
SHA256: `e65069abafe0711feda6b403e9ba8128d9f57af050175840b3715945590327dc`.
Live JAR remains `1fc09938a842432b174fbfea32261bb5f49a856b1f1a294a4631b1b8a6a11f03`.
No server/client restart was performed. No live saves were modified by this task.
Both installer names passed `--check-only`. A full invocation with the live Java
process present correctly refused installation; the deployed JAR hash stayed intact.
