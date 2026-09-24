# Combat and world expansion ? 24 September 2026

**AUTOMATED VERIFIED / LIVE TEST PENDING.** No live deployment, restart or character-save changes. This is a scoped successor, not a claim of retail-complete bossing.

## What changed

- **Dagannoth Kings:** shared counter-style immunity for autos, abilities, queued hits, bleeds, Death Skulls and conjures. Supreme accepts melee, Prime ranged, Rex magic; all reject Necromancy. Poison remains allowed. Prime/Supreme resist stuns; Rex can be stunned. Kings acquire eligible nearby players. Supreme attacks multiple players, with separate rolls, projectiles and captured targets.
- **Boss presentation:** named cast/projectile bindings restored for Prime/Supreme; Prime impact graphic; corrected Kings defence/death sequences. These are exact950 payloads linked to named sequences proven byte-identical to949. Vulkan appearance still needs acceptance.
- **Normal Graardor:** newly admitted with melee and delayed multi-player ranged shockwave, named attack animation/caster effect, stun/poison immunity, existing three style-specific minions acquiring nearby players, existing loot and respawn. Damage follows normal-mode engine-unit limits; queued ranged hits retain their own style rather than inheriting the boss's melee profile. Hard mode is not implemented.
- **Shared NPC damage:** native fractional rounding cannot exceed the authored maximum. A shockwave no longer triggers melee block feedback before impact.
- **World travel:** all 26 reciprocal Slayer Tower stair links exercise actual movement against950 collision. Large 7x7 staircases now search around their decoded footprint rather than only three tiles. Source identity, free-floor and reciprocal-route validation remain required.
- **Combat access:** War's Retreat Reaper portal offers a native choice menu for Graardor's room, Dagannoth lair entrance, Slayer Tower and Taverley Dungeon. Bandos altar Teleport returns to War's Retreat. These are explicit SHNORKSCAPE sandbox routes, not retail Reaper assignments/kill-count or instance systems. Normal War exit option3 reaches Death's Office.
- **Symbols/dev tools:** 53 additional identities: 33 project NPC/object aliases, 14 named sequence bindings and 6 effect aliases derived from exact950 links. Total browser entries: **1,438**. Source provenance is explicit. The new fail-closed execution resolver checks payload and reference-index SHA pins; cache-store replacement invalidates its verified cache. No console startup/layout rewrite.

Engine HP was preserved: stored3,500 represents35,000 displayed life points. Do not multiply it again.

## Audit and limits

| Class | Encounters | Assessment |
|---|---|---|
| Improved / feasible now | Dagannoth Kings | Style rules, aggro, multi-target Supreme and named presentation implemented. Retail target-switch/Provoke rules remain incomplete. |
| Newly admitted / partial | Normal Graardor | Mixed attacks, shockwave, minions, access, loot and individual respawn use shared combat. Hard mode, encounter-wide reset/instance orchestration and exact retail tuning remain future work. |
| Needs generic mechanics | K'ril, KBD, Chaos Elemental | Basic admission of some actors is not a complete encounter; prayer/fire/control mechanics need separate work. |
| Access/instance/phase work | Giant Mole, Kalphite Queen | Burrow/phase/instance lifecycle needed. Do not treat placeholder variants as full bosses. |
| Major future work | Multi-phase modern bosses | No blanket special-style admission. |

NPC admission: **1,881 / 2,644** authored rows, up one (normal Graardor). Remaining763:140 invalid authored stats,7 invalid cache parameters,85 missing stat rows,191 no Attack option,231 unsupported special styles,2 animation gaps,105 unverified legacy identities,2 effect gaps. A read-only scan found **zero exact current-name/size/level/all-parameter stat aliases** for missing rows; no stats were fabricated.

Slayer already has weighted tasks, level gates, credited-owner progress, points, cancellation and XP rewards. Dagannoth family names include all three Kings. This pass improves access to combat/Slayer areas; no new skill implementation is claimed. Special-equipment/finisher tasks remain deferred.

Loot rates are unchanged. The isolated probe verifies11 signature rows against safe950 item metadata: Kings rings/dragon hatchets and Graardor Bandos armour/hilt. Existing damage-credit ownership remains authoritative; these are authored server rates, not newly claimed retail drop rates.

Deferred live issues remain: cracker Pull, ability drag-off, equipment binding, Strength Powers, Ghost presentation and render priority. No speculative native UI fixes.

## Evidence and reproduction

- `Native950CombatWorldAudit.java`: read-only NPC/map/symbol audit; optional second output generates NPC/object aliases.
- `build_boss_bindings.py`: bounded named-source hash pin,949/950 sequence equality, exact950 effect-to-sequence links; emits resources and `boss-bindings-950-20260924.json`. No cache edits.
- `Native950CombatWorldAcceptance.java`: isolated world JVM, production cache/collision, no socket/save. Checks1,438 symbol pins, four boss profiles,26 stair movements, four combat routes, stale callbacks, both return routes and11 loot rows.
- `Native950StatAliasAudit.java`: read-only missing-stat candidate discovery; grants no runtime admission.
- Deterministic tests cover damage-style immunity, poison/stun policy, multi-player/other-plane targeting, delayed shockwave type/presentation, unregister cleanup and maximum-hit rounding.
- Gameplay references: [RS Wiki Dagannoth strategy](https://runescape.wiki/w/RuneScape%3AEvents_Team/Dagannoth_Kings_%2822_April_2018%29), [bosses](https://runescape.wiki/w/Bosses), [General Graardor](https://runescape.wiki/w/General_Graardor). Numeric950 contracts come from cache pins. Normal Graardor shockwave probability also follows the local authored encounter.
- Reference classification: preserve our shared queue/ownership/UI lifecycle; adapt named-symbol resolution from Undercut; local boss code supplies behaviour candidates only. The old one-fifth wrong-style Dagannoth rule and old Graardor projectile1200 were not blindly imported. Public Darkan727 mappings remain unsafe without verification.

## One live checklist

1. At War's Retreat, use Reaper portal: enter each destination; use Bandos altar Teleport to return. Check Death-office exit option.
2. Fight each King with correct/wrong styles; check Prime/Supreme casts, projectiles, deaths, and Supreme attacking Jaxa + Nooby independently.
3. Fight normal Graardor: melee, visible delayed shockwave, active minions, drops and respawn; check both players and leaving/logout.
4. Climb Slayer Tower stairs up/down/top/bottom; confirm floor and clear landing.
5. Briefly check keybinds after bank/items/dev, queue ring, Revolution, Dive, souls/conjures and Death Skulls. Check `;;gameval graardor` and owned `;;clearobjects`.

Apply only through **Apply Staged Update.cmd** after closing both clients and stopping the server. Offline verification does not establish live Vulkan acceptance.
