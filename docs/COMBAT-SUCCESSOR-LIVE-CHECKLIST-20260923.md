# Successor live check

Apply `Apply Staged Update.cmd` yourself after closing the clients and stopping
the server. Then launch normally. No agent restart has been performed.

The previous P0 bank/items keybind result is LIVE PASS. Preserve that finding;
only investigate it again if this candidate produces a new failure.

1. Enable `;;bugtest`. Put Dive on a visible bar slot. Activate it and click a
   clear tile a few squares away, including a direction you are not facing.
   Confirm cursor mode, chosen-tile movement and cooldown. Try a wall, excessive
   range and repeat use. Report which boundary fails if any. Do not judge Dive's
   animation complete: its exact presentation binding is still missing.
2. Equip melee gear, select a living durable target, and put a usable basic
   ability in a known slot. Run `;;queuehold <that slot number>`. Watch for the
   native queued overlay for about7.2 seconds, then execution and marker clear.
   Repeat and cancel by walking. The marker must clear. The command's message
   is not acceptance of the marker. Check yellow activation and cooldown too.
3. Wear a Necromancy weapon and conduit. Use Touch of Death, then Finger of
   Death; watch Necrosis. Use Soul Sap repeatedly, then Soul Strike and Volley;
   watch residual souls, consumption and dependent availability. Check Volley
   projectile/impact and Living Death's basic-attack Necrosis generation.
4. Use Death Skulls on one durable NPC, then several. Watch individual travelling
   skull legs, including a harmless caster return and re-hit on a single target.
   Repeat with a target dying during the chain. Check no effect leaks to another
   player's resources/damage ownership. Cape changes affect bounces and cost.
5. With ectoplasm available, conjure Skeleton, Zombie, Ghost and Phantom, or
   Undead Army. Check actual models, following, target attacks, command transforms,
   expiry and conduit-removal cleanup. Attack poses are known incomplete; report
   wrong models separately. A second player should see actors and removals.
6. Use all three Scythe stages; check transforms, costs, nearby targets, souls and
   expiry. Then check Revolution range1–14, tier filters, manual/queued priority,
   weapon swaps and two players attacking the same NPC.
7. Change bar using the native selector. Switch to another NPC while the first
   still retaliates. Briefly check bank/items closing preserves the already
   accepted keybind behavior and that the library still looks unchanged.

Useful markers: `;;bug successor-dive`, `;;bug successor-queue`,
`;;bug successor-necromancy`. Do not use cleanup scripts or remount the bar to
hide a failure. Record expected versus observed behavior; offline PASS is not
rendered acceptance. Missing incantations/actor attack effects are tracked in
`COMBAT-LIVE-SUCCESSOR-20260923.md` and are not claimed complete.
