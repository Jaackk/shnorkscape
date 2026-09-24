# Live combat polish - 24 September 2026

Successor: `combat-live-successor-polish-20260924`. Offline work only; no server restart, deployment or production character changes. Preserve the current native bank/library design, LAN and workspace behaviour.

## Accepted live baseline

The latest user report confirms keybind recovery after bank/items, chosen-tile Dive, conjures surviving Surge/Dive, Death Skulls bouncing, residual-soul world appearance, conduit availability, Necrosis generation/display/consumption and native buff panels. Revolution is functioning with remaining polish. These are CONFIRMED LIVE; this pass does not reopen the context-24 investigation.

The `session-20260924-010148-657-jaxa` log contains 265 ability executions (261 Revolution), 375 conjure attacks, 151 Death Skulls flights and 149 impacts. Living Death requests were rejected at 109/105/76/39 remaining cooldown ticks. The captured multi-soul attack was **Volley of Souls 48301**, not Soul Strike 48299. Living Death executed six times with sequence35475 but no separate caster effect; server execution does not refute the user's missing-visual report.

## Implemented - live test pending

- **Persistent manual queue.** Removed the three-tick own-cooldown admission limit. One manual request retains its native ring until execution, replacement or cancellation. Revolution can execute while it waits without replacing that request. Manual input wins when ready; target/equipment and resource/adrenaline checks run again before execution. Existing stop/death/logout/bar invalidation paths remain. No chat substitute for the ring.
- **Necromancy Overload.** Normal boost is base + floor(15% base) +3; supreme is base + floor(16% base) +4. Both feed the existing native stat and damage path; expiry/death restore the base value. The real-cache gate consumes an actual flask dose, checks increased damage, exact UPDATE_STAT output and another player's unchanged level. Level120 becomes141/143 respectively. Other style formulas are unchanged.
- **Soul lifetime.** Owned delayed hits and incoming combat damage now refresh combat activity, in addition to active target selection. In the live log, souls expired at01:10:15 despite Skulls impacts at01:10:12/13/15. The existing six-second out-of-combat expiry remains; world count and native count still clear together. A narrow lifecycle event records expiry versus conduit-cap reduction.
- **Volley timing.** Removed the extra per-soul release delay. All souls launch together and retain projectile travel before impact. Exact CS18662 establishes a non-channelled hit per soul; simultaneous release is an implementation inference addressing the observed serial staggering, not a decoded retail tick constant. Soul Strike retains its main hit, same-impact adjacent damage, soul cost and stun (CS18661); it was not executed in this log.
- **Conjure Examine.** The native Examine packet now returns a description for the four verified companion definitions after published-view, region and controller checks. It does not enable attack interactions. The response is a server description, not claimed as recovered retail examine text.
- **Developer completion.** `;;comp` populates all362 exact-cache quest finished thresholds, permanent CS15411 combat unlock branches and related Greater Sunshine/Death's Swiftness/Spirit Pact values. Empty Undead Army selections receive all four native choices (CS18289, enum17157, vars11499-11502). Existing nonempty selections are retained. Native quest UI refresh no longer overwrites completion with three old quest mappings. Normal accounts are unchanged. This establishes the audited flags; it is not proof that every possible conditional item/quest script has been exercised.
- **Berserk pose.** Named exact950 Chaotic/Azure Berserk structs39860/47230 share player sequence35135. The default missing pose now uses that sequence, with raw asset hashes pinned. This is an evidence-backed shared-pose inference corroborated by Jagex's updated standard Berserk announcement; separate cosmetic effects are not enabled. Physical animation acceptance remains pending.
- **Conjure stacks/command.** Exact CS11077 uses varp10997 for Skeleton Rage and11823 for Phantom Valour. Counts now publish per owner and clear with consumption/dismissal. CS13240 describes Phantom command's45-55% base,20% increase per Valour stack and primary plus up to four enemies within one tile. Those mechanics now share the existing ownership/damage checks; cap, range, plane, clear packets and second-player isolation pass.

## Partial / unresolved

**Living Death is not claimed fixed.** Its exact struct already binds35475. New Bug Test events at the post-movement PLAYER_INFO boundary distinguish an overwritten animation from an emitted one. Candidate graphic7883 shares effect sequence35476 with named Living Death cosmetic models, but that relationship alone does not establish its correct lifecycle; it was not blindly enabled.

**Conjure actor attack/spawn/expiry effects remain unresolved.** Exact companion BAS assets prove idle/follow, not the missing attack/expiry identities. This pass did not substitute Rasial/hostile actors or nearby numeric sequences. Damage, following, commands and Examine do not constitute complete presentation.

The updated85-ability presentation audit has19 missing animation bindings (previously20); every bound graphic passes the identity gate. No ability was promoted to COMPLETE. Incantations, remaining Necromancy mechanics, Revenge/variable Barricade UI details and other matrix gaps remain partial.

## Evidence and verification

Exact cache/script records: `protocol-analysis/combat-progression-950-20260924.json`, `combat-presentation-audit-950-20260924.json`, `combat-presentation-gaps-950-20260924.json`; canonical matrix retains its existing20260923 filename and now carries current live acceptance.

Public corroboration: [Necromancy boost behaviour](https://runescape.wiki/w/Necromancy), [combat boosts](https://runescape.wiki/w/Combat_boost), [Jagex Berserk update,16 January2023](https://secure.runescape.com/m=news/mobile-lobby-updates--raptors-rampage-continues---this-week-in-runescape). These support behaviour/semantic inference; raw950 bindings remain authoritative for IDs.

Full JUnit:1524 tests,0 failures/errors,2 existing skips. Real-cache combat:1938 checks,226 activations,144 projectiles. New polish gate:983 checks. Real scheduled Dive/Surge/Escape, bank, global library/presets, interface-close and combat-style gates pass. Final packaged checks/hashes are recorded in `protocol-analysis/combat-polish-validation-20260924.json`.

Use **Apply Staged Update.cmd** when ready, after closing clients and stopping the server. The agent only runs `--check-only`. One live checklist: `docs/COMBAT-POLISH-LIVE-CHECKLIST-20260924.md`.
