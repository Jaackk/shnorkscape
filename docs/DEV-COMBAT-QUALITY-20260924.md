# Developer combat quality pass - 24 September 2026

First checkpoint: read-only `;;abilityinfo <slot 1-14>` reports server cooldown/GCD/channel ticks, queue, effective ability, target, runtime refusal, equipment/level/resource gates and Revolution filters. The action bar retains its latest CS6570 arguments per structure. These are transport publication observations, **not Vulkan acknowledgements**. The snapshot does not publish packets or select abilities.

`;;resetcooldowns` is an explicit developer action: clears only the requesting player's ability/GCD timers and queue; refuses during a channel. `;;npcinfo <ID, partial name or audited symbol>` reports real production admission/refusal, authored/cache profile sources, timings, presentation and drop-row presence. NPC browser has an Inspect Combat Profile action. Existing Best loadouts, QA and disengage actions are discoverable under Combat.

Gameval search includes provenance with strict type tokens. Exact command/alias matches rank before incidental description substrings (for example DM versus admission).

Focused combat, console and Gameval tests pass, including snapshot no-publication and second-player cooldown isolation. Native UI remains LIVE TEST PENDING. No deployment or restart.

The previous `dist/combat-live-successor-world-20260924` stage is preserved. Pre-edit source verified on GitHub at eead39b; runtime/saves and installer metadata backed up under backups/pre-edit-20260924-174830-246.

## Boss tooling and combat changes

- `;;bosses [query]` opens the existing NPC browser restricted to the curated boss matrix, searchable by name, ID, symbol, style or support status. Select Inspect Combat Profile for full metadata and verified related assets. No startup script or cache change.
- `;;bossinfo <ID>` reports forms, status, mechanics, gaps and the actual production profile. The nine-form matrix is `Ataraxia950/resources/native950/boss-support-950.json`. KBD and KQ remain COMBAT BLOCKED; generic Mole/Bork profiles remain PARTIAL.
- `;;bossgo <ID>` uses audited destinations for the Kings and normal Graardor, sharing ordinary passage landing/collision validation. This is explicit sandbox access, not retail entry requirements.
- `;;bossfight <ID>` creates a temporary single King or Graardor with his three bodyguards. Every footprint/profile must succeed or the whole creation rolls back. One life, one encounter per owner, exact-actor cleanup via `;;bossclear` or logout. It does not touch saved editor placements.
- The three Kings and normal Graardor reset health/damage credit after returning home and every living player leaving their leash area. Movement or a temporary player lock does not count as leaving. Delayed hits against the resetting boss are retired.
- Living normal-room Graardor restores dead normal bodyguards on a 50-tick pulse. Temporary developer actors, other arenas and one-life NPCs are excluded. This implements the bodyguard recovery described in the [RuneScape bestiary transcript](https://runescape.wiki/w/Transcript:Beasts); it does not add instances or hard mode.
- Provoke now starts retaliation even before the first damaging hit, without damage or loot credit. Behaviour reference: [Provoke](https://runescape.wiki/w/Provoke). No speculative presentation ID was added.
- `;;abilityinfo` also records its snapshot in an enabled Bug Test and reports stored Necromancy resources plus the actual equipment-specific presentation resolution. These are read-only observations. Cooldown reset refuses while a channel or Sunshine/Swiftness field is active because the native field clock shares publication state.

The focused ability-priority audit is `protocol-analysis/ability-priorities-950-20260924.json`. The older gap file predates the verified movement/defensive bindings; its empty arrays must not reopen accepted work. Ghost, Immortality/Divert and Limitless presentation remain evidence/live work. Practical future encounter guide: `docs/CUSTOM-BOSS-TEMPLATE.md`.

Deferred unchanged: physical console startup/layout acceptance, cracker Pull, bar drag-off, equipment binding, Strength Powers visibility, Ghost attacks and scene target priority. No new Slayer content, new arbitrary NPC profiles, new animation IDs or unrelated world systems.

## One live checklist

1. Open ;;dev / ;;bosses; search Dagannoth and Graardor, inspect profile, check status and related asset output. Confirm Heal, existing search and close/keybind behaviour.
2. Try ;;abilityinfo on a cooling/queued slot with Bug Test enabled; check server/native values. Use ;;resetcooldowns after channels/fields finish, then confirm normal queue/Revolution behaviour.
3. In an open area, try ;;bossfight 6260 and ;;bossclear. With Nooby's separate encounter present, clearing/logging out must remove only your test actors.
4. Use ;;bossgo 6260 and ;;bossgo 2882; verify safe arrival, bodyguard recovery during a long normal Graardor fight, and boss reset only after both players leave. Quick Provoke, Death Skulls and movement regression.

All new UI and combat behaviour remains LIVE TEST PENDING until these physical checks. Offline tests do not certify Vulkan appearance.

## Verification

Full regression: **1,577 tests, zero failures/errors, two existing skips**. Additional isolated real-cache acceptance covers exact profile refusals, symbolic lookup, related asset pins, both boss destinations, four-actor creation, duplicate prevention, two-owner clearing, actual combat detach/logout cleanup and late-placement rollback preserving an existing actor. The running PID35088 and jar SHA D1DAAC...96D6C remain unchanged.

Checkpoints: `5c85bbe` diagnostics; the following combat/tooling checkpoint includes the boss matrix, lifecycle fixes and actual-cache acceptance. Stage validation is recorded in protocol-analysis/dev-combat-quality-950-20260924.json.
