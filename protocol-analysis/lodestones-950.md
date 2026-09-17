# Native950 lodestone network

Click Home Teleport beside the minimap to open the client's lodestone network. Select a destination to begin ordinary Home Teleport. Walking before departure cancels the action. Right-click Home Teleport and choose the previous destination to repeat the latest selected trip in the current session.

All29 ordinary destinations are available for local exploration, including the26 legacy destinations plus Fort Forinthry, City of Um and Wendlewick. Unlock values affect the client presentation; they do not complete server quests. Seasonal event and JMod-only destinations remain hidden. Quick Teleport charges are not implemented; a quick destination choice uses ordinary Home Teleport.

## Source and integration

The current950 cache supplies the destination widget IDs, coordinates, names, network IDs and unlock variables through CS14999, CS13702 and enum5726. Lunar Isle's coordinate differs from the old910 constant. Actual interface1092 is mounted at1477:735 with wrapper732; the minimap entry is1465:34. Exact bindings and reproducible extraction are in lodestones-950-evidence.json and ../tools/verify_950_lodestones.py.

Native950Lodestones owns the modal, keyboard context, selection and previous-destination state. Native950Interactions closes it when gameplay actions, Settings, the world map, bank, conversation or logout replaces it. Shared Player.closeInterfaces also closes it through InterfaceManager. The game scene remains at1477:30 throughout ordinary network use.

## Teleport action

The original910 HomeTeleport and ActionManager retain the25-stage timeline. Native950LodestoneTeleport validates and owns the native movement/effects around that timeline. Departure/arrival sequences16385/16386, landing16393 and graphics3017/3018 match the paired910 and950 cache definitions byte for byte; see lodestone-teleport-assets-950.json.

Destination landing uses the original south-of-lodestone convention and checks the actual950 collision before starting and committing the move. Controller restrictions and the existing post-combat cooldown still apply. Walking, death, logout, forced movement or another teleport cancels safely; cleanup does not clear another operation's pending tile or lock. Arrival uses the existing session region rebuild, entity publication and music update path. The adapter does not initialize unported legacy Wilderness, clan-war or dungeon controllers.

Existing Lumbridge-only NPC spawn scope remains in effect; this feature provides travel, not population/content parity for every destination.

## Verification

The new UI and teleport assets are included in Native950CachePreflight. Focused tests cover selection, invalid/closed actions, lifecycle, the original action timeline, controller/collision checks and cancellation. The real-cache acceptance uses an ephemeral native session without account saves or listening sockets. Final outcomes are recorded in validation-lodestones-2026-09-10.json; native rendering requires an in-game check.
