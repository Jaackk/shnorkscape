# Native950 Minigames and D&D information browser

## Native cache contract

Adventures menu3 page4 contains interface1344. Its list hook6743 invokes6744, while6745 invokes6746 and then6747 for a selected activity or6748 for the empty selection prompt. The adapter invokes those existing native scripts after the page mounts.

Category varbit20794 is enum6156:0 All,1 Favourites,2 D&Ds,3 Minigames All,4 Combat,5 Non-combat. Categories0/1 use enum6452 with51 entries,2 uses8014 with28,3 uses8015 with23,4 uses8016 with15,5 uses8017 with8. Every activity enum is one-based. Struct param1266 gives its name,3631 its long description,1292 its reward summary and other1273.. fields provide hints/requirements/location. Native6747 renders those fields directly rather than copying them into custom widgets.

The actual clickable layer is1344:23;6744 creates its actor using the original enum ordinal. Client click script18246 converts actor to selected key varp3233=actor+1. Filtering favourites hides unwanted actors without compacting their ordinals. Passing the visible row position as a key would select the wrong activity. The server validates the current category, bounds, item and operation fields and refuses a non-favourite row while the Favourites filter is active.

The native filter control is1344:31, created by6744 through10428 using enum6156. It opens the shared dropdown at1477:896. The server accepts those six choices only while this page owns an armed dropdown and retires the dropdown on close.

The detail star1344:34 reads an activity's param1268 through9178 to choose its favourite varbit. The generated mapping covers all51 current activities, and those exact bits are verified. A favourite change updates its bit, rebuilds the native list/details and displays a session-only confirmation. No save-format extension is included. The currently selected struct is retained across filters when present; removing a selected activity from the Favourites view returns to the native empty prompt.

## Implemented scope

Native950Minigames(Player,Channel) exposes opened(menu,page),closed(menu,page),handle(InterfaceAction) and verifyCacheBindings(). Navigation owns all mounts. The parent integrates this adapter alongside Quests and Beasts in its page listener.

This is information browsing, filtering and session favourites. Minigame participation, active task tracking, world switching, travel, subscriptions, cooldowns and reward grants are not implemented by this adapter. Their server actions give an explicit unavailable response. The native Spotlight template is changed to report that no local Spotlight is active, and the unsupported500% bonus template is cleared. Cached reward descriptions describe the activity; browsing does not grant those rewards.

The catalog contains51 distinct activity structs,6 filters and262 cache pins. tools/generate_950_minigames_catalog.py verifies without writing by default. --write regenerates the resource with a non-overwriting backup.

## Verification and live follow-up

Isolated javac compilation and Native950MinigamesAcceptance passed125 row selections across five populated categories, plus favourite filtering with noncompacted ordinals:1,426 decoded encrypted frames. Checks include native list/details initialization, one-based selection keys, malformed rows/items, unarmed and stale dropdown clicks, favourite addition/removal, empty selection, remembered selection, walking and close cleanup. Unsupported action tests verify no movement, pending teleport or skill update is produced. No account/profile writes, shared build, running process or runtime JAR was changed.

Live verification remains with the parent task: open Adventures -> Minigames, click activities in each filter, add an activity as a favourite, filter to Favourites and remove it, then close/reopen and switch to other management pages. Confirm the native list/details and dropdown render correctly, including resize/drag. Client-rendered availability, cooldown displays and service state may need future authoritative adapters even though the cached activity descriptions can now be browsed.
