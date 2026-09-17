# Native950 Agility expansion — 2026-09-12

The existing Gnome course now shares its native Agility entry points with complete basic Barbarian Outpost and Wilderness courses. No central interaction routing change is needed: `Native950Agility.handles`, `approach` and `start` delegate the additional scenery to `Native950AgilityCourses`.

## Supported routes

| Course | Entrance / required level | Ordered lap |
|---|---|---|
| Barbarian Outpost | 2552,3561,0; Agility30 | Entrance pipe; either rope swing; log; net to plane1; balancing ledge; ordinary ladder down; both crumbling walls from west to east. |
| Wilderness | 2998,3915,0; Agility52 | Entrance gate/ridge; southern pipe; rope swing; stepping stones; log; cliff finish. |

The two Barbarian pipe directions, both rope columns, ladder up/down, both Wilderness pipe mouths and the return gate are bound independently. This is19 exact obstacle directions/variants. Exit passages and the platform descent remain available below the entry level so a lowered skill cannot trap a character.

Each binding checks the actual950 object ID, coordinate, plane, shape, rotation and first option. Entrance tiles are reached through the existing collision-aware route and the animation waits for the final movement frame. A declared clear destination is checked again before traversal. The original Wilderness pipe landing at3004,3949 is inside the current950 pipe footprint; its verified clear landing is3004,3950. The outer Wilderness gate starts at2998,3915 rather than on its blocked2998,3916 object tile.

## Original framework retained

- Uses the original `ActionManager`, Player walking/teleport publication, skill XP pipeline and Agility level message.
- Uses the original `BarbarianOutpostCourse` and `WildernessAgilityCourse` temporary stage attributes and `addLapsRan` counter.
- Keeps base Barbarian rewards80 per obstacle plus300 completion, and Wilderness220 for each of its first four obstacles plus2000 completion. The original Wilderness Agility-gloves multiplier is retained through its existing helper.
- Fixes the original Barbarian first-wall bonus bug: the old handler set stage4 and immediately treated it as a finished lap during the same first-wall click. The port requires both actual walls in order. Repeated, skipped or reversed routes cannot retain lap credit.
- Exact current-cache BAS155/157/295 supply balance, ledge and crawl movement. Climb/jump sequences use their verified original animation IDs and native tile publication; stepping stones publish each hop individually.

Actions refuse remote starts, locks, pending movement/teleports and insufficient levels. They retain the starting controller and cancel on changed scenery/controller or unrelated movement. Interruption restores run/render/lock state and returns an owned interrupted traversal to its clear entrance; unrelated teleport destinations remain intact. No XP or lap is awarded for an interrupted action.

## Evidence and checks

`tools/verify_950_agility_assets.py` regenerated40 pins in `Ataraxia950/resources/native950/agility-assets-950.properties`. Every reused sequence's frame references/durations match the original910 cache, and all three BAS records match exactly. `protocol-analysis/agility-assets-950.json` records the sequence comparisons. This verifies selected-cache assets, not a visual recording of the animations.

`tools/Native950AgilityCourseProbe.java` reads actual950 scene placement, object options and destination collision. Its output is recorded in `logs/agility-course-scenery-probe.log`.

Targeted compilation and isolated `Native950AgilityAcceptance` passed26 encrypted object clicks and702 parsed output frames: original Gnome, complete Barbarian and Wilderness laps, every added variant, native appearance emission, approach routing, level gates, misplaced-object refusal, interrupted traversal recovery and unrelated teleport preservation. All players/worlds are ephemeral; no account save, live server or client was changed. Output: `logs/agility-expansion-acceptance.log`.

Four `Native950AgilityCoursesTest` cases passed for ordered walls, repeated/skipped obstacle refusal, original Wilderness reward totals, one-time lap completion and course-state cleanup.

## Remaining scope

The advanced level90 Barbarian roof route is not included. Custom contract/achievement progress, juju ticket rewards, aura lap bonuses and random-event rewards are not added by this adapter. The existing Gnome adapter has the same focused XP/lap scope. The new routes still need normal live-client visual confirmation after the parent project's deployment.

Source backups are under `implementation-backup/2026-09-12-agility-expansion`.