# Combat candidate: one live acceptance sequence

Install only through **Apply Staged Update.cmd**, after closing Jaxa and Nooby's
clients and running Stop.cmd. Start Play.cmd normally afterward. This candidate
has not been installed or live-verified by the agent. Keep `;;bugtest` enabled;
the agent should inspect the logs, not ask the player to decode them.

Use ordinary best-style loadouts without `;;almighty` for the combat portions.
Use a durable training target and safe diagnostic space for movement.

A. **Input lifecycle:** activate one known working key, open/close `;;bank`, press
   that same key, then open/close `;;items` and press it again. Repeat three times.
   Compare a mouse click on the same slot. Both should activate without a reset command.
B. **Melee and Revolution:** manually activate a basic, queue a second during
   global cooldown, and watch activation flash, queued marker and cooldown separately.
   Enable Revolution from the native combat settings. Try range 1, then 14, and
   toggle Basic/Enhanced/Threshold/Ultimate controls. Selection should respect
   the chosen slots and tiers; a manual queued ability takes priority. Change an
   unrelated settings slider, close/reopen settings, and verify the range is unchanged.
C. **Ranged:** use a compatible weapon and ordinary matching ammunition. Compare
   an auto attack, Piercing Shot, Galeshot and Shadow Tendrils. Watch facing,
   projectile/impact order, cooldown and recoil; switch weapons during a channel.
D. **Magic:** compare auto casting with Dragon Breath, Asphyxiate and Smoke
   Tendrils. Watch cast/effect/impact order and recoil. Stop or change target during
   a channel; cancelled future channel hits must not fire.
E. **Necromancy:** compare a basic auto attack, Touch, Finger, Sap, Strike, Volley,
   Blood Siphon and Living Death. Watch the correct book/icons, Necrosis/soul
   counts, Finger cost, Siphon healing and final hit. Check a second player's
   resource display stays independent. Necromancy presentation remains partial.
F. **Movement:** Surge and Escape in open space and near scenery. Dive to a
   chosen tile, cancel the targeting cursor, then try beyond range and across a
   blocked diagonal. Movement must use the selected legal destination, never
   teleport through scenery or silently become a forward dash.
G. **Two players:** Jaxa and Nooby attack the same target with different styles.
   Stop/rejoin one player, change bars/weapons, kill the target, then disconnect
   one player during combat. The remaining player must retain control; resources,
   pending hits, damage credit and loot must not transfer to the wrong character.
H. **Encounters:** in clear diagnostic space, try the three Dagannoth Kings,
   Barrelchest, K'ril, Bork and Giant Mole. Observe retaliation, prayer/food use,
   death, owned loot, corpse removal and repeat spawning. These are tested native
   generic encounters, **not completed retail boss mechanics**; Bork's minions,
   K'ril's special attacks and Mole's burrowing remain unported.

Stop and report the first mismatch with its command/ability and whether mouse,
keyboard, animation, effect, hit or resource state failed. No save editing is
needed for this checklist. Original bank/library appearance and ordering are preserved.
