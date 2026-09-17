# Native950 Beasts information browser

## Current-cache evidence

Interface753:0 onLoad794 clears list actors and installs a detail watcher for varc4485, then initializes its kill-record views. It does not build the information list. Hook3371 calls script3, which reads varp4517:0 selects Boss Info,1 Boss kills,2 Slayer Creature kills. In Boss Info it iterates enum9031 (58 ordinal entries), using each boss struct name param1348 and native10410/10899 button construction.

The visual list lives at753:4, with eleven drawing children per row. The separate hit layer is753:5; its actor is the enum ordinal0..57. This distinction avoids accepting drawing-child numbers as boss selections. Script3869 reads selected boss struct varc4485 and renders native name, model, recommended level/group size, lodestone, requirements and description;11074 handles the description. Struct6411 holds the description. No boss IDs or text were copied from910.

The current list includes Amascut, the Devourer at ordinal0, struct52079. Details remain the current client/cache presentation. All58 structs, enum9031, relevant component/script bindings and shared button structures are pinned:205 individual hashes. The generator is read-only by default; --write regenerates with a non-overwriting backup.

## Integration and verification

Native950Beasts(Player,Channel) exposes opened(menu,page),closed(menu,page),handle(InterfaceAction) and verifyCacheBindings(). It is a Navigation page adapter for Adventures3, Beasts5 and owns no interface mounts. The parent task integrates it alongside the Quest adapter.

Open selects Boss Info, explicitly renders3371, enables the exact753:5 hit actors and renders the remembered boss through varc4485/3869. Selection validates live page ownership, operation, actor and item fields. Close removes its event permissions and retires any armed category dropdown. Unsupported kill-record categories return an explicit unavailable message and retain Boss Info. Other server-backed boss actions remain unavailable; this change does not start encounters, grant rewards, or claim boss mechanics are ported.

An isolated javac run and Native950BeastsAcceptance passed all58 rows through real encrypted950 input and decoded output:246 frames. It checks list/detail initialization, exact enum selection, invalid ordinals/items, category refusal, stale input after closing, remembered selection and walking cleanup. No live profile or running process was changed. Native visual rendering still requires the parent task's live check.

## Minigames investigation (no change in this pass)

Current1344:20 hooks6743 ->6744 for the activity list. Category varbit20794 selects enum6452 for all activities(0) and favourites(1), then8014/8015/8016/8017 for categories2..5. Enum6452 has51 entries and is one-based, unlike the zero-based Beasts list. Selected activity varp3233 is an enum key, and1344:16 hook6745 ->6746 selects that enum's struct then6747 renders details. With selection below1,6746 invokes6748 to show the native selection prompt. A future adapter should explicitly initialize category/list and set a validated one-based selection; reusing the boss row mapping would be incorrect. Minigame participation, teleport/world switching, subscriptions and active tasks need independent server handling.

Follow-up: the bounded native Minigames browsing adapter was subsequently implemented and tested separately; see native950-minigames.md for its exact mapping, scope and limits.
