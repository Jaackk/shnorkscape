# Offline EOC Quality Audit

Baseline: deployed ecf19a6. Date: 18 September 2026. Runtime deliberately not
restarted or deployed. Jaxa's save is not an input to the test fixtures.

## Evidence and Matrix

- `combat-audit-ecf19a6.json`: preserved original 31-definition inventory.
- `combat-audit-offline-20260918.json`: complete post-repair matrix, including
  requirements, resource rules, cooldown/GCD, hits/cadence, channels, effect and
  queue/cancellation policy, Revolution eligibility, raw cache params and hashes.
- The matrix includes every sequence mapped by each weapon-family enum, raw
  presence, SHA-256, decode result and sequence graphic/projectile parameters.
  All 114 ability/sequence records exist and decode; 82 report zero legacy-frame
  duration. These are not 82 missing animations. Visible rendering remains untested.
- Berserk, Death's Swiftness and Surge have no mapped param2915 sequence. No
  unrelated 876 sequences were assigned. Param4030 is not assumed to be a sequence.
- Prior live evidence: `combat-review-20260918-145650.md`, its completed Bug Test
  session, and history through ecf19a6. No new live session exists for these repairs.
- Behavioural comparison: local Elveron876 `ActionBar`, `PlayerCombatNew`,
  `EffectsManager`, `Combat`, and `Player` lifecycle. Old protocol IDs are not used.

The paired structs match the modernised resource contract, rather than old
Elveron thresholds. Jagex's [Part 1](https://secure.runescape.com/m=news/patch-notes-part-1---combat-style-modernisation)
corroborates modern adrenaline, Berserk, ranged channels and Deadshot changes;
[Part 2](https://secure.runescape.com/m=news/patch-notes-part-2---combat-style-modernisation)
corroborates Concentrated Blast and base Omnipower. Cache identity/requirements
remain authoritative where older descriptions disagree. Damage coefficients are
explicit Alpha approximations, not values supposedly decoded from tooltips.

## Per-Ability Findings

All rows remain partial Combat Alpha implementations. B/E/U = basic/enhanced/ultimate;
the JSON matrix has the numerical fields and all presentation evidence per row.

| Ability | Type | Corrected this pass | Remaining meaningful limitation |
|---|---|---|---|
| Barge | B | Gain 9 | No engagement dash or conditional follow-up mechanics |
| Adaptive Strike | B | Gain 12 | Does not adapt hit count/cleave to weapon configuration |
| Backhand | B | Gain 9 | Stun exists; charge, immunity and knockback systems incomplete |
| Flurry | E | Cost 25, 8 spaced hits, matching offhand | Single target; no full secondary effects |
| Overpower | U | Cost 60 | Alpha coefficient; no Bloodlust/cape interaction |
| Meteor Strike | U | Cost 60 | Single target; critical-adrenaline buff absent |
| Dismember | E | Free base cast; independent scaled bleed | No recast tiers/heal; approximate DOT model |
| Punish | B | Gain 9 | Below-half modifier exists; overall formula approximate |
| Fury | B | Gain 9; stronger channel cancellation | Retained old 0/2/5 cadence conflicts with modern non-channel metadata; needs further semantic evidence |
| Assault | E | Cost 25; channel weapon validation | Four hits retained; coefficient/Bloodlust incomplete |
| Hurricane | E | Cost 25 | Two hits retained; AoE/cooldown-on-target-count absent |
| Berserk | U | Targetless, no hit, 33 ticks, scoped modifiers | No verified presentation or Bloodlust/Overpower interactions |
| Piercing Shot | B | Gain 9; two scheduled hits preserving Alpha total coefficient | Snipe cooldown reduction absent |
| Binding Shot | B | Gain 9 | Stun approximates bind; charge/immunity systems incomplete |
| Snipe | E | Free base cast; delayed hit and channel ownership | No Piercing Shot cooldown reduction |
| Ricochet | B | Gain 9 | Secondary/ricochet hits absent |
| Snap Shot | E | Cost 25 | Two hits and zero individual cooldown retained; damage approximate |
| Rapid Fire | E | Cost 25; 8 one-tick hits | Explicit walking still cancels channel; per-shot ammunition incomplete |
| Deadshot | U | Cost 60; 4 direct scheduled hits, not DOT | No cape upgrade; fixed reference-average coefficient is an approximation |
| Death's Swiftness | U | Targetless 50-tick anchored ranged buff, no hit | Ground-area DOT and verified presentation absent |
| Impact | B | Gain 9 | Stun works; charge/immunity systems incomplete |
| Chain | B | Gain 9 | No secondary-target copying |
| Combust | B | Gain 9; independent scaled bleed; dummies supported | Retail ten-hit burn not yet modelled; exact local cadence still needs evidence |
| Dragon Breath | B | Gain 9 | AoE/burn synergy absent |
| Asphyxiate | E | Cost 25; retains four spaced hits | Root/bind and fuller damage model incomplete |
| Wild Magic | E | Cost 25 | Two scheduled hits retained; damage approximate |
| Tsunami | U | Cost 100 retained | AoE/critical-adrenaline buff absent |
| Omnipower | U | Cost 60; base single hit instead of unconditional cape upgrade | Cape variant absent; fixed reference-average coefficient is approximate |
| Sonic Wave | B | Gain 9 | FLOW tag has no modern cost-reduction effect |
| Concentrated Blast | B | Gain 9; 3 one-tick hits | Critical buff absent; stale duration metadata must not override cadence |
| Surge | Utility | Tier 7; no adrenaline gain | Collision unchanged; presentation still unverified |

## Subsystem Comparison

| Subsystem | Assessment and disposition |
|---|---|
| World/combat loop | Native single-thread owner is safer than legacy scheduled actions; preserved. |
| Target ownership | Native explicit fighter identity avoids unsafe legacy target panel; preserved. |
| Auto attacks | Still classic weapon-speed attacks, not modern 3-tick basic-attack abilities. Architectural follow-up required. |
| Request/queue | Native bounded replacement and execution revalidation are stronger than unbounded deferred callbacks; preserved and extended tests. |
| GCD/individual cooldown | Shared manual/Revo owner, correct cache resource admission; retained. |
| Adrenaline | Old Elveron admission is wrong for this cache; now checked against exact per-ability fields. |
| Hit formula | Classic tier/bonus approximation; abilities always damage while autos roll accuracy. Elveron's shared EOC hit calculation is more mature. No speculative full balance rewrite. |
| Scheduled hits | Native tick queue is better than animation-duration-derived scheduling. Counts/cadence and weapon interruption repaired. |
| Channels | Explicit cadence fixed; movement policy remains conservative. Fury is a known metadata conflict, not certified. |
| DOTs | Elveron's typed effects are more mature. Fixed overwrite, HP units, buff contamination, dummy omission and cancellation, retaining documented approximate cadence. |
| Buffs | Native typed tick owner now covers two self buffs. No wholesale legacy effect-framework transplant. |
| Debuffs/defensives | Native stun only. No general immunity, cleanse, critical buffs, reflection, shield or defensive-ability catalog. |
| Movement/teleport | Validation now precedes damage. Pending work is cancelled; mobile Rapid Fire remains future work. |
| Target death/removal | Object identity and native encounter stop clear pending work. No old target-panel packets. |
| Death | Native recovery now also owns environmental lethal hits; clears Prayer, buffs and Overload. Items retained. |
| Logout | Native combat maps/toggles are cleared. Save/checkpoint order and schema unchanged. |
| Weapon switch | Channels snapshot both weapon IDs; execution revalidates style/hands. Already-launched non-channel hits retain captured loadout. |
| Prayer | Existing 60-identity cache contract retained; damage/accuracy/protection and points owner tested. Soul Split/leech/deflect retaliation not implemented in native hits. |
| Magic | Explicit spell is no longer silently downgraded by level drain. Rune checks and selection shared by abilities/autos. |
| Ranged | Admission/ammo family/depletion preserved. One ammo debit per ability, not per shot, remains incomplete. |
| Melee | Matching offhand enforced. The starter/classic loadout branch still rejects offhands; general 950 profiles support them. |
| Overload | Existing dose/boost/refresh owner retained; startup pulses and death cleanup fixed. Six-minute redose refusal remains. |
| Revolution | Existing ordered active-bar selection preserved, shared executor tested; self buffs eligible, Surge excluded. No native checkbox work. |
| Animation ownership | Explicit channel deadline plus existing presentation clock retained. Zero frame duration no longer controls channel hits; full animation pacing still needs live review. |
| Graphics/projectiles | Cache family mappings retained centrally; spell cast/projectile/impact mappings exist. Ability projectile emission/ranged autos remain incomplete. |
| Performance | Removed synchronous per-hit stdout. No new mounts/config loops/polling. Immutable definitions/profile caches retained. No FPS claim without live test. |

## Concrete Native/Legacy Boundary Findings

1. `Entity.processHit` previously avoided degradation but still called legacy
   `removeHitpoints`, allowing life-saving jewellery, legacy teleport and death.
   Native hits now bypass the entire callback chain through `Native950PlayerHits`.
2. Overload startup tasks retained player references after death/session loss.
   Native guards stop pending pulses before graphics/hits, without touching legacy clients.
3. The ordinary Player tick returns while dead before reaching Overload's death
   branch. Native death now clears its timer and boosts directly, without corpse healing.
4. DOT and pending damage ran ahead of final post-movement teleport validation.
   Validation now happens first, tested with a teleport between the world phases.

## Remaining High-Priority Architecture Work

- Replace the classic auto/damage foundation coherently with modern basic attacks,
  damage potential/affinity and shared accuracy. Do not change isolated coefficients
  and describe that as a retail combat engine.
- Expand effects only with explicit semantics: Fury, Piercing Shot's cooldown reduction, Sonic Wave,
  modern bleed timings, mobile channels, resource-per-shot rules and AoE ownership.
- Overload duration is not saved, but boosted skill levels are. Relog restores
  residual boosts without the timer. No save migration was attempted in this pass.
- Native environmental hit isolation intentionally does not emulate legacy
  ring-of-life, phoenix necklace, reflection or leech mechanics. These require
  explicit native implementations, not accidental callback reuse.
- Existing Prayer/status code still attempts rejected legacy presentation calls.
  No scripts were whitelisted or guessed. Native status visibility is unverified.

## Validation and Safety

Pre-edit snapshot: `backups/pre-edit-20260918-204839-223`; ecf19a6 was clean and
pushed before work. Repairs are separate pushed checkpoints. No runtime assets,
security controls, client executable, UI protocol, commands or character files changed.

Validation covers the full engine unit suite; real-cache ability definitions and
weapon-family assets; deterministic dummy timing/queue/resources/Revolution;
Prayer identity/state/points; Overload pulses/gear safety; 69 style/resource checks;
449 native owner-thread combat ticks with 1531 parsed encrypted frames; protocol
vectors and 5145 JS5/HTTP payload checks. See the final turn report for final counts
and staged artifact hash. None of these is a rendered-client test.

For the eventual live test after deployment: record a short `;;bugtest` session
with a basic/enhanced/ultimate rotation, Snipe/Rapid Fire/Concentrated Blast, a
weapon-switch interruption, targetless buffs, two bleeds and Overload followed by
ordinary combat. Mark animation visibility/pacing separately from server timing.
