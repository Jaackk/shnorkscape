# Native 950 Dungeoneering baseline

Players can complete repeatable solo dungeons through ordinary Daemonheim entrances or a ring of kinship. The Dungeoneering tutor gives a free ring when the player has room and does not already carry or wear one. The ring's existing **Teleport to Daemonheim** and **Open party interface** actions lead to a native choice dialogue. Bring equipment and food; this first pass keeps the player's normal inventory and equipment.

Choose **Start a solo dungeon / travel to Daemonheim**, defeat all three Dungeon rats, and use **Climb-up** on the dungeon exit. Each completed run awards 150 base Dungeoneering XP through `Skills.addXp` (the configured skill rate applies), 15 tokens and one completion. Killing the guardians alone does not award completion. Repeating an exit or death notification cannot award twice. The ring dialogue shows the saved token and completion counters.

Leaving through the exit before finishing, using the ring to leave, dying, disconnecting or teleporting away aborts the run without a completion reward. Guardians and room ownership are retired. A logout checkpoint returns the player outside; recovery after an interrupted save returns an abandoned chamber occupant to Daemonheim. Ordinary native combat death recovery retains items. Completed XP, tokens and completion counts are saved through the shared schema-4 skill progress section; an unfinished run is deliberately not reconstructed or rewarded after restart.

## Source and scope

The port uses the original Ataraxia `DungeonConstants.START_ROOMS` frozen start templates at chunk coordinates `(14,624)`, `(14,626)`, `(14,630)` and `(14,632)`, and original authored Dungeon rat combat/stat data. The available original source directory was `C:\Users\developer\Desktop\Ataraxia-PS`; the requested `Ataraxia-PS910` directory was absent. The port admits NPCs through the native 950 constructor, world registry and shared combat service; it does not start the original procedural dungeon/controller machinery.

There are four reserved chambers, one player per chamber. Guardian ownership prevents another player from taking the combat objective. This is a small solo combat floor, with three fixed objectives and a fixed reward. Procedural layouts, multiplayer parties, floor selection, prestige, dungeon-only equipment, binding and the token reward shop remain future work. Tokens are earned, saved and displayed; this module does not yet spend them.

## Paired-cache checks

`Native950DungeoneeringAssets` pins the selected 950 bytes for ring `15707`, tutor `9712`, Dungeon rat `88`, entrances `48496`, exits `51156`, and the terrain/location groups used by the four chambers and Daemonheim arrival area. It verifies these before gameplay admission. Runtime room checks also require a clear entry, clear size-2 guardian footprints and the real exit object. The two normal entrance locations are `(3445,3722,0)` and `(3454,3722,0)`. The ring arrives at `(3448,3699,0)` and exiting returns to `(3452,3718,0)`.

The client map-area membership is also verified: all 16 chunks across these four rooms resolve to area `115`, in map squares `9985` and `10113`. The native session's existing `areaTypeFor` lookup supplies `115` to the scene rebuild, instead of falling back to the mainland area. Both acceptance probes verify the complete current `23/3` membership and `2/83` area identity source groups before asserting these room chunks.

## Integration and validation

The shared native interaction dispatcher owns normal packet, menu, visibility and approach validation, then calls this module's object/item/tutor methods. Native combat calls `attackRefusal`, `onNpcDeath` and `onPlayerDeath`; session lifecycle calls `tick` and `onLogout`. Per-player state uses temporary attributes; persisted counters use `tokens`, `completed`, `interrupted` and `restoreProgress`.

Validation against `C:\Users\developer\Desktop\950RevTest\cache`:

- Eight JUnit checks passed for distinct objective credit, replay rejection, exact object locations/options, ring menu scope, interrupted hydration/logout and counter boundaries.
- `Native950DungeoneeringAcceptance` passed actual-cache asset/room admission, owned native guardians, exactly-once completion, repeat entry and death/logout cleanup in a disposable process.
- `Native950DungeoneeringRoutingAcceptance` passed encrypted ring travel/menu, distant actual entrance approach and dialogue selection, three real shared melee damage/death callbacks, actual exit reward with a native Dungeoneering stat frame, stale exit rejection and repeat entry followed by ring abort. It decoded 187 encrypted output frames over 85 accelerated ticks. The production death callback is asserted before the test submits a replay.

Both acceptance classes take the selected flat-cache directory as their single argument and run from the Ataraxia950 directory. They create temporary in-process players, open no listener and touch no account saves. The routing fixture controls combat randomness and uses a high-level dummy player; live client rendering, authentication and real-time play still need a client check.
