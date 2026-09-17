# Mining repair — 12 September 2026

The original Mining action, ActionManager timing, Skills XP, inventory and WorldTasks object lifecycle remain in use.

Concrete fixes:
- Old steel/mithril/adamant/rune item IDs 1269/1273/1271/1275 describe obsolete non-wieldable objects in this cache. The current IDs are 45467/45494/45521/45548. Their paired mining sequences name these current IDs in the held-tool fields.
- Necronium and elder rune animations previously showed upgraded tools rather than the selected base tool; exact held-item bindings now select 32602 and 32610. Dragon/crystal use the current mining sequence family 32588/32614.
- Initial animation and every continued action check actual scenery, plane, stopped walking, adjacency and original collision-aware ObjectStrategy reach. Direct calls cannot start mining from a distance.
- Mining now queues immutable yellow stamina and blue ore progress gauges through original Entity hitbar state and the existing 950 mask writer. Cache bar7 and bar49 plus their actual sprite files are pinned; this is a server-owned mining display, not an assertion of retail semantic IDs. Stop/cancel sends explicit removal of both gauges. Arbitrary custom/adrenaline/timer bars remain refused and standard HP snapshots are unchanged.
- A bonus double ore no longer drops the entire base yield when one inventory slot remains; depleted-rock identity is validated before starting the action.

Verified in an isolated JVM, without saved accounts/listeners: 13 current pickaxes and sequence held-item identities; actual east-Varrock copper113027; current west-Varrock iron113039 level refusal; current clay113033 near3140,3316; encrypted Mine/Wield; automatic collision approach; no-tool/full-bag/distant-action checks; cancellation and gauge removal; original continuous ore/XP loop; XP update and save capture; paired depleted-rock update and timed regrowth. Run com.rs.game.player.client.Native950MiningAcceptance with the cache directory. 191 ticks/476 encrypted frames in recorded run; timing can vary because original mining damage is randomized. Nineteen focused unit tests pass, including independent literal bar wire bytes and existing HP-bar regression.

Coverage remains ordinary named ore/clay/sandstone/granite/gem rocks as admitted by Native950Mining. Core post-rework ores do not deplete, following the original910 source. The existing generic gem-rock level/drop table, augmented/invention tools, toolbelt, stone spirits, geodes and special/quest deposits are not a complete retail mining implementation. User-visible rendering and character-to-rock alignment still need a live client check after the combined build.
