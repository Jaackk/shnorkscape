# Native 950 Combat Alpha Model

Scope: offline audit beginning at ecf19a6, 18 September 2026. This describes the
server implementation, not a certification of retail balance or rendered UI.

**Elveron provides behavioural reference; revision 950 provides protocol/cache authority.**
The paired cache already contains Combat Style Modernisation. Do not restore
876's blanket 50% threshold admission, 15% spend or 8% basic gain.

## Ownership and Request Lifecycle

- `Native950MeleeCombat` owns encounter, queue, GCD, cooldown, scheduled-hit and
  effect state on one world thread. No legacy `PlayerCombat` action is installed.
- Manual input and server Revolution call the same validator/executor. Binding
  persistence and active bar belong to `Native950ActionBar`, not the combat loop.
- One queued structure per player. A new valid request replaces the old one.
  Invalid requests do not spend resources or replace a valid request.
- GCD, active animation and channel ownership can defer execution. Individual
  cooldown can be queued only within three ticks of readiness. Execution checks
  target, distance/collision, style, hands, level, adrenaline and supplies again.
- Self buffs use the same deferred executor without requiring an encounter.
  They require the right weapon style, but do not consume ammunition/runes.
- Surge remains a separate collision-checked utility request. Revolution never
  selects it. No movement or presentation changes were made to Surge.

## Commit and Timing

Supply consumption precedes damage/GCD/cooldown commit. Successful abilities own
a three-tick GCD and their cache-verified individual cooldown. Adrenaline is
charged once at execution, never on a rejected or merely queued request.

Definition tier/cost/gain/hand flags are checked at startup against paired structs:
2799 = tier, 2798 = cost x10, 2800 = gain x10, 2811/2812 = hand requirements.
Enhanced admission equals its cost. Infinite-adrenaline mode bypasses spending.

Hit delays are combat ticks, not a division of legacy animation frame duration.
Paired fields 8884/8885 corroborate interval/repeat counts for Flurry, Assault,
Rapid Fire, Asphyxiate and Concentrated Blast. Snipe commits one hit after its
wind-up. Fury retains an explicitly uncertain older-reference cadence.

The current animation gate still combines `lastAnimationEnd` wall time and a
tick-based channel deadline. Auto attacks and incoming block animations cannot
overwrite a protected ability. This is conservative, not proof of ideal pacing.
Probe clocks explicitly advance the wall-time deadline when simulating ticks.

## Cancellation and Effects

- Scheduled damage is processed only after encounter and teleport validation.
  Channels additionally revalidate range, style, hands and both weapon IDs.
- Stop, teleport, logout and death clear the queue, channels, pending hits and
  bleeds. Explicit walk/cancel also clears them. This deliberately conservative
  Alpha policy still differs from mobile Rapid Fire and persistent retail bleeds.
- Same-target re-click does not reset timers; target replacement stops the old
  request owner. NPC death/removal retires pending work by object identity.
- `Native950CombatBuffs` owns Berserk and Death's Swiftness deadlines, replacement,
  expiry and removal. Duration is checked against struct param3740. Combat stop
  does not erase an active self buff; death/logout/world clear does.
- Berserk: 33 ticks, melee direct damage x1.75, incoming native combat damage
  x1.25, no opening hit, no bleed amplification. Extra Bloodlust mechanics are absent.
- Death's Swiftness: 50 ticks, ranged damage x1.5 within three tiles of the cast
  origin on the same plane, no opening hit. Ground-area DOT/presentation remain absent.
- Bleeds are keyed by caster and ability within the owned encounter. Reapplying
  one replaces that effect only. Damage uses native HP units and snapshots Prayer
  at application; later buff changes do not rewrite its ticks. The existing
  opening-hit plus three-follow-up model remains an acknowledged approximation.
- Overload retains its existing Player/Pots countdown, stat refresh and dose
  owner. Pending startup pulses now stop on death/inactive/disconnected sessions.
  Native death explicitly removes Overload and boosts without healing the corpse.
  Overload duration is not persisted in the native save; see the audit limitations.

## Native Boundary

Native direct combat uses explicit HP/hit-mask updates and native death recovery.
`Native950PlayerHits` handles environmental/potion hits without legacy incoming
hit callbacks, charge metadata, ring destruction/teleport or legacy death actions.
It retains capped damage, healing, hit bars, god mode and native recovery. Do not
route it back through `Entity.removeHitpoints` or legacy `Player.sendDeath`.

Prayer remains the shared authoritative Prayer owner, with 950 identity mapping;
native damage reads its style bonuses/protection. Magic has one explicit persisted
spell selection. Level drain rejects that spell rather than silently choosing another.

## Limits and Validation

Classic damage/accuracy and weapon-speed autos remain. Abilities do not yet share
a full modern damage-potential/affinity model. Ranged spends per execution rather
than per shot; Magic always spends cast runes rather than modern probabilistic
consumption. These are documented work items, not hidden claims of retail fidelity.

`Native950OfflineCombatAcceptance` runs real-cache timing, two-style bleeds,
Revolution, equipment, dummy and cancellation scenarios without saves or sockets.
Unit tests verify boundaries and lifecycle. Existing encrypted-frame, Prayer,
Overload, equipment/resource and JS5 probes complement these tests. None verifies
icons, stance, animation visibility or native settings interaction visually.
