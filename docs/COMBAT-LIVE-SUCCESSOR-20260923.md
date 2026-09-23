# Combat successor — 23 September 2026

This continues the user's live acceptance of `combat-completion-20260923`.
The running JAR remains SHA256
`a426b834f41384afd6b9ac9e81769e2d1914ccc79eaf8d416264efcbe7de44de`.
No installation, restart, client manipulation or production-character edit was
performed. The pre-edit backup is `backups/pre-edit-20260923-132706-892`.
Staged bundle: `dist/combat-live-successor-20260923`,11 pinned files;
installer check-only PASS. Source checkpoint `f6d5f97` pushed.

Source baseline/rollback checkpoint: `9f778e7`; first successor checkpoint:
`475a15b`. Use the candidate manifest for the final source checkpoint and hashes.

## Authoritative live results

| Area | Installed candidate result | Successor acceptance |
|---|---|---|
| Keybinds after bank/items | LIVE PASS | Preserve context24 close fix |
| Queued marker | LIVE FAIL | Offline transport passes; Vulkan pending |
| Revolution | LIVE PASS/PARTIAL | Preserve; offline regression passes |
| Presentation | LIVE PARTIAL | Broader exact950 bindings; Vulkan pending |
| Necromancy | LIVE PARTIAL, major missing systems | Expanded foundation; incomplete |
| Death Skulls bounce | LIVE FAIL | Timed bounce lifecycle; Vulkan pending |
| Dive chosen tile | LIVE FAIL | Input masks and full server path tested; Vulkan pending |

Do not reopen the speculative P0 investigation without contradictory live
evidence. The normal bank close releases context24 through CS9299/8841 before
closing the subinterface. This pass does not change that lifecycle.

## Dive and queued presentation

The Dive log showed requests travelling down the normal NPC ability path, not a
successful ground-target path. The actual icon/target-parent children
1430:{65,66}+13*slot now receive the ground-target bit17 as well as their ability
events. Previously only the background children had the ground mask. This is a
candidate explanation for the Vulkan cursor failure, not a proven live fix.

`Native950DiveAcceptance` exercises a mounted action-bar source, decoded opcode85,
the real interaction handler, actual950 collision, scheduled force arrival,
authoritative position commit and PLAYER_INFO. It rejects an unbound source and
repeat use during cooldown. Its terminal frame is independently checked to be
stationary for a viewer that already received the force endpoint. It does not
execute Vulkan input/cursor code. Dive remains chosen-tile movement; no forward
dash fallback or guessed animation was added.

Opt-in Bug Test events now identify tile input/source, validation result,
requested/resolved clipping endpoints, force-frame publication, scheduled arrival
and movement commit. These distinguish missing input from a later move failure.

Live logs explicitly rejected sendConfig4164/5861 at the strict binding resolver.
The successor admits their exact varps. The native listener1430:70+13*slot invokes
CS5899 with overlay1430:71+13*slot; its main-bar identity is1003, separate from the
selected saved-bar number. No direct CS5899 call or chat replacement is used.
Publishing clears slot4164, sets identity5861, then publishes the one-based slot.
Transform redraws restamp queue and active cooldowns without remounting the bar.
Execution, cancellation and invalidation clear queued state.

`;;bugtest` then `;;queuehold <slot 1-14>` holds the actual manual queue for12 ticks
(7.2 seconds), subject to normal admission/target/equipment validation. It is a
diagnostic GCD extension, not a separate fake overlay. The real-transport harness
checks publication, persistence, redraw, execution clear and cancel clear.

The native bar selector's observed ops6–10 now map to saved bars1–5; this server
has four saved bars and refuses the unsupported fifth cleanly. Target switching
no longer requires the old NPC to stop retaliating. Each existing NPC retains
its retaliation owner while only the selected target consumes player/Revolution
actions. Controller, clipping and target-validity checks remain.

## Necromancy

The strict binding table also rejected10986/11035. Actual ability execution now
has transport acceptance for Touch of Death -> Necrosis4 -> Finger of Death ->0,
and Soul Sap -> residual soul1 -> Soul Strike ->0. Player isolation is checked.
The existing caps, six-stack Finger consumption/cost reduction, Volley soul
consumption, offhand soul-cap passive and Living Death generation are retained.
Native display and dependent availability still need live verification.

Death Skulls is an independent cast-owned flight chain. Every leg publishes the
exact950 projectile7882 and waits2 ticks, or3 at distance6+, before impact. It can
select another valid nearby NPC, return harmlessly through the caster and re-hit
a prior NPC. It continues after initial-target death when an endpoint remains,
uses line-of-sight/range checks, preserves ownership/attribution and stops on
owner removal/death/teleport or exhaustion/no endpoint. Damage/critical selection
is snapshotted once. Exact CS17730 supplies225–275%, range6 and4 bounces, with2
additional bounces and60% cost for the identified Necromancy Igneous cape passive;
normal cost100%. Tests cover one/multiple targets, death, range, no endpoint,
two owners and detach. Retail immunity/respawn-epoch edge cases remain partial.

Spectral Scythe now has both native recasts48312/48313, separate10/20/30 adrenaline
costs,25-tick transform windows and11051/11054 publication. CS17731 supplies
72–88%,180–220%,225–275%, cone/area target limits,25% soul generation on the first
two stages, and the final missing-HP multiplier. Expiry/cleanup and two-player
isolation are tested. Recast cooldown details still need live review.

Conjures have their own actor manager and never enter generic hostile NPC AI.
Skeleton, Zombie, Ghost and Phantom are distinct exact950 asset instances, with
owner, following, target acquisition, cadence, ectoplasm costs, level limits,
duration/Spirit Pact, command state, expiry and death/logout/teleport/conduit
cleanup. Army spawns transactionally and rolls back partial creation. Native
active varps drive command transforms. Two viewers receive additions/removals
through the actual NPC viewport and transport. Two owners retain separate
Ghost haunt/damage state. Hitmarks use verified950 Necromancy477/478 and conjure480.

Asset confidence is deliberately separate from lifecycle confidence. Phantom
31142 has a public NPC identity. Skeleton30265, Zombie30266 and Ghost30267 are
inferred companion assets from the exact950 adjacent non-attackable definitions,
models and BAS4703/4707/3284, with immutable SHA checks; their semantic/model
mapping still needs visual confirmation. They are not the generic Rasial
skeleton30166/zombie30167. No hostile NPC stats/AI are reused as conjure behavior.

Conjure limitations: actor attack poses/projectiles are unresolved; Zombie's
passive poison and Phantom's Valour-scaled command damage are not implemented.
Lifetime countdown display is not fabricated from unproven timer-var semantics.
Command unlock varbits are sent session-locally at the required level for the
developer combat path; they are not persisted talent unlocks. Full talent,
quest, equipment-passive and cosmetic integration is unfinished. Do not call
this retail-complete Necromancy.

Threads, Darkness, Split Soul, Life Transfer, Bone Shield and Invoke incantations
remain missing executable systems. Exact rune costs8891–8894, native durations
and tooltips were traced; the next pass should share cost/buff/area/death-mark
mechanics rather than add generic damage substitutes. The canonical matrix
retains these as MISSING. NPC/boss expansion was deferred behind these failures.

## Presentation evidence

The old player/NPC graphics gate accepted only proven910=950 identity entries.
The audit found125 graphics referenced by the85 admitted definitions and their
weapon-family sequences;123 were absent from that legacy identity table. A
separate exact950 SHA-pinned table now admits them without claiming legacy-ID
equivalence. Caster and impact output is checked for refusals by the actual mask
adapter. Impact binding can come from typed sequence2933 when the struct omits it.

Volley sequence35469 is corroborated by Undercut's named reference and its
exact950 impact7879 matching the Volley struct; it also binds projectile7880.
Living Death exposes sequence35475 in its alternate2535 field. Both are candidate
presentation bindings pending rendered confirmation. Every audit variant and
asset hash is in `protocol-analysis/combat-presentation-audit-950-20260923.json`.

Twenty admitted definitions lack a bound caster sequence: Berserk, Death's
Swiftness, Surge, Dive, Bladed Dive, Escape, Provoke, Cease, Resonance, Preparation,
Immortality, Natural Instinct, Devotion, Limitless, Eat Food, Divert and the four
conjure commands. Some intentionally have no caster animation; actor/area effects
must be evaluated separately. No separate graphic parameter does not prove an
animation-integrated effect is missing. No arbitrary old animation IDs were added.

## Validation and continuation

Build/test/JAR:1514 tests,0 failures/errors,2 skips. Real-cache combat:1726 checks,
225 activations,144 projectiles. Melee/boss gate:1491 owner-thread ticks and8230
encrypted frames. Bank, LibraryFollowup, CombatStyles, Equipment,
DiagnosticSpawns, ExitUi and World gates pass. Separate Dive boundary gate passes.
Reports are offline evidence, not Vulkan acceptance.

The189-entry inventory contains85 admitted partial definitions. Canonical native
books contain135 entries:80 PARTIAL,55 MISSING,0 COMPLETE. Conditional transforms
remain separately classified. The matrix preserves the user's live failures.

Reproduce with `Build-Ataraxia950.ps1 -Tasks test,jar`. Compile the Java tools in
`tools/combat-pass` against fresh classes; classpath order must be tool classes,
fresh server classes/resources, then `OpenNXT/runtime/lib/*`. Set
`-Dataraxia950.data=C:\Games\950OpenSource\Ataraxia950\data`. Run CombatPassAcceptance
with `cache <output.json>`, DiveAcceptance with `cache`, and AbilityCoverage with
`cache protocol-analysis/ability-coverage-950.json`. Matrix generation accepts
`--live-feedback protocol-analysis/combat-live-followup-20260923.json` and
`--presentation protocol-analysis/combat-presentation-audit-950-20260923.json`.

References reviewed: exact950 scripts17445,17454,17458/59,17727,17730/31,18295,
18658–18674,8247,10903; local Undercut949.1 ability transforms/hitmarks/Rasial
named sequences; older Ataraxia/Darkan/Vernox/Elveron/Matrix candidates.
Public behavior references: https://runescape.wiki/w/Death_Skulls,
https://runescape.wiki/w/Conjuration, https://runescape.wiki/w/Phantom_Guardian,
https://runescape.wiki/w/Spectral_Scythe_(status). Current retail differs from
some950 coefficients; exact950 tooltips take precedence here.
