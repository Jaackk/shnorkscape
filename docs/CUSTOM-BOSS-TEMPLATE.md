# Small custom boss implementation checklist

No custom encounter is invented here. Use normal Graardor and the Kings as working examples, not as a promise that every mechanic already exists.

1. **Identity/model:** choose a real950 NPC; pin its definition and reference hash through Native950Symbols. Record an explicit project alias if no original Gameval is available. Add authored stats and require Native950NpcCombatCatalog admission; never borrow default stats.
2. **Arena/access:** choose actual loaded collision. Add an audited Native950CombatTravel destination if appropriate; shared landing validates floor and reach. Public rooms and private instances need distinct ownership policies.
3. **Attacks:** define style, max hit and cadence in engine units. Use the shared delayed NpcStrike target/style capture and projectile timing. Verify sequences/effects against950, including impacts. Do not broadcast one player's hit roll to everybody.
4. **Phases/telegraphs:** these have no general encounter framework yet. For a real proposed mechanic, define an explicit state and tick deadline, telegraph before damage, record its affected tiles, and invalidate it on death/reset. Do not implement a hypothetical phase DSL first.
5. **Minions:** use explicit roles and lifecycle association. Current normal Graardor restoration is bounded to its public room; the temporary encounter command creates its group atomically and rolls back if any footprint fails. New bosses should use an explicit encounter identity if groups can coexist in one arena.
6. **Drops:** preserve damage-credit ownership and Native950NpcDrops item validation. Specify authored probabilities separately from gameplay claims. Reset must clear old damage credit.
7. **Reset/multiplayer:** retain progress while any living participant remains, including during movement/locks. Invalidate delayed hits and minions; don't heal other concurrent encounters. Tests must cover logout, deaths, second-player continuation, blocked respawn and two simultaneous groups.
8. **Developer test:** add a truthful boss-support-950.json record. Enable ;;bossfight only after safe actor/profile/placement contracts exist. Keep temporary encounters owner-scoped, one-life and automatically cleaned on logout; persistent world placement is a separate explicit operation.

Required acceptance: offline lifecycle/ownership and real-cache collision checks, then physical attacks, telegraphs, impacts, death, reset and two-player testing. Generic damage alone never earns PLAYABLE/complete status.
