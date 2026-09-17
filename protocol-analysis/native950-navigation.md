# Native950 management navigation and ribbon audit

Implemented in the isolated950RevTest project. Shared ribbon configuration and Quest content adapter are owned by the parent task. This note covers navigation and the Skills facade only.

## Findings from the current cache

- Menu enum7699 maps management keys to current root structs. Pages are one-based param3448..3453; child interfaces and exact geometry repeat in five-param groups3456..3480. Native script8283 consumes that page struct plus slot ordinal. This keeps resizing on the same cache layout.
- Shared shell1448 belongs at1477:715, wrapper708. Its five children attach at1448:3/5/7/9/11. Root714 header actors3/7/11/15/19/23 choose pages1..6; root717 actor1 closes. Root8 option1 Escape also closes.
- Ribbon enum13319 fixes actors0..7 to buttonIDs128,129,130,131,132,135,136,137. Enum13321 maps those IDs to menu keys0,1,2,3,4,7,8,9; display order is unrelated to actor identity.
- Current Hero has Summary, Skills, Loadout, Achievements, and cache-hidden Group Ironman. The910 Achievements interface IDs are obsolete:950 uses1850/1855/1895. Powers now includes Necromancy at page4, shifting Defensive and Prayers to5/6.
- Adventures page1 is now Calendar1284. Quests is page2, struct21161: list1783 at1448:3 and detail1500 at1448:5. Older910 menu3 was commented out, so copying that branch would not populate Adventures.
- The existing950 SkillGuide owned the whole shell, rejected every non-Skills Hero tab, and forcibly restored page2. Unlocking masks alone could not resolve navigation.

## Implementation

Native950Navigation now owns one management shell and cache-derived page mounts. Native950SkillGuide remains the existing session facade so all existing walking/NPC/bank/settings/teleport cleanup calls retire any newly exposed menu too. The guide still owns its selected skill and nested1217 row trigger.

Selection is published before the shell and page onLoad events. Page changes remove old children, apply the current cache page structure and restore visible child slots. Repeat tab clicks acknowledge the native Loading state without recreating controls. Skills changes continue to issue only the selection/row scripts rather than recreating Hero.

Menu defaults are Summary, Wardrobe, Melee, Quests, Social, and Metrics; each remembers its last selected page for this session. Cache-hidden pages are not admitted by forged tab clicks. Marketplace produces an explicit local-unavailable message and restores the current local page without mounting a retail store or launching a URL.

Loadout temporarily closes/reparents the existing equipment1462 HUD instance into1448:7, then restores its original cache-derived HUD parent on exit. Other shared page interfaces follow the same ownership rule. The server never mounts duplicate copies of one interface ID. Page handlers can attach via skillGuide.navigation().setPageListener(opened/closed/handle) for real quest or other content state.

No guessed root8 option2..7 management mapping was added. Cache1477:8 invokes8181(widget,operation), whose branches concern Escape/overlay cleanup. Old910 F1 handling reached1431:0. The native ribbon hook chain13843/13845 ->5588 ->20343 ->8287 selects management locally; server notifications retain stable1431 actors.

## Verification

- navigation-950.properties derives60 complete interface-group hashes and87 individual script/enum/struct/root-component/varbit hashes from the paired cache. The resource preserves native page and slot ordinals and is verified once per cache store before opening. tools/generate_950_navigation_catalog.py reads cache only by default; --write regenerates with a non-overwriting backup.
- Existing Native950SkillGuideAcceptance passed all29 skills,29 in-place icon changes and29 external selections (1,346 session packets). Scene ownership and close/reopen behavior remained intact.
- Eight focused Native950SkillGuideTest tests passed in an isolated javac/JUnit run, including the updated Summary tab switch regression.
- Native950NavigationAcceptance passed real encrypted950 input and outgoing-frame decoding for28 visible pages across six menus, repeated tabs, invalid actors/items, remembered selection, Skills no-remount, Loadout HUD restoration, Settings roundtrip and stale close. Initial run:1,650 decoded frames before the five Quick Options entries were added. No account/profile writes, server listener, authentication or live client involved.

## Limits and manual checks

This implements native menu navigation and native client browsing; it does not implement every server action behind those pages. The Quest adapter supplies quest state separately. Abilities/combat modes, cosmetics ownership/application, achievements progression, groups/social services, Calendar events, challenges, minigame queues and RuneMetrics history need their own authoritative content passes. Some of those pages may show native placeholders or empty data until those adapters are added. Hidden account-specific pages stay hidden.

Check Hero Summary -> Skills -> Loadout -> Achievements; change skills in place; close and confirm equipment remains available in the HUD. Open Adventures -> Quests and switch all tabs. Open Powers including Necromancy. Switch directly between Hero/Adventures and Settings. Resize and drag each native management frame, then close with X or Escape. Native rendering and keyboard shortcuts require live visual confirmation; no such confirmation is claimed here.

Source changes and original test files were backed up under implementation-backup/ribbon-navigation. Shared build, deployment and live validation remain with the parent task.

## Quick Options and Loadout follow-up

Quick Options1433:5 onLoad13835 ->13836 builds1433:6/7 using enum13320. Its rowIDs128,129,131,130,132,135 map via13321 to menus0,1,3,2,4,7. These exact six dynamic actor requests are now admitted and event-enabled; the same Marketplace refusal applies. This was verified independently of the older910 swap of Adventure/Powers.

Loadout1474:3 onLoad8677 ->8678 ->8682 selects the same six inventory roles as HUD1473, shifted by three components. Actual item actors are1474:8, backing1474:7, overlay1474:9; script8680 reads container93[actor] then12090 constructs native item options. Item actors0..27 receive Native950InventoryMenu.EVENT_MASK while Loadout is open and are disabled before removal. The parent task normalizes this owned origin into existing item transactions.

1463:30 onLoad8451 installs native updates for container94 and stat vars715/716/711 then8452 ->8454 calculates the equipment panel. Its current component21 is the name text and is filled from the Player. Summary1446:72 runs8271 for the native player/title string; old910 name target1446:81 is now a sprite and is deliberately not written. Summary1446:73 onLoad3850(widget,1446:71) creates a dynamic type6 local-player model under71, adds native model-change/rotation hooks8483/8479/8480, and initializes8484. No guessed head/body packet or replacement UI model is needed.

## Live Loadout follow-up: admission ownership and equipment redraw

The first live Loadout check found the original HUD equipment panel still behind the modal. The frontend opens1462 at the verified enum7716 slot3 host1477:114 (wrapper112), but Native950World previously mirrored only the scene1482 after flushing the login burst. Navigation could not displace a panel absent from the server's ownership map. The parent task replaced that scene-only admission mirror with an ordered replay of actual bootstrap interface packets; Navigation continues using actual recorded ownership rather than guessing a HUD parent.

The previous headless acceptance manually registered1462, masking this admission gap. It now writes actual bootstrap open packets and invokes the same mirrorBootstrapInterfaces admission seam before navigating. It checks the encrypted outgoing old-HUD close precedes the Loadout equipment open, and verifies native refresh ordering on both relocation and restoration.

Cache8468 (equipment onLoad) only installs container94 and native variable update hooks and initial positioning. It does not perform the equipment draw. Script8470 routes changes to8471(root,container), while8471 reads current component dimensions and chooses/rebuilds the native equipment grid or paperdoll. Navigation now calls the verified8471(1462:3,94) after its8283 page geometry and after reopening displaced equipment in its HUD parent. No player model, icon positions or paperdoll mode are invented. Scripts8468/8470/8471/8472 are included in the cache pins.

The parent live check confirmed Summary's native player model and Loadout wear/remove transactions. Duplicate-panel removal and initial equipment redraw require a fresh build/live check after these follow-up changes; this note does not claim that follow-up has passed yet.

The follow-up isolated run compiled the current Native950World admission mirror, Navigation and acceptance together, then passed28 visible pages across six menus with2,151 decoded encrypted frames. This includes actual bootstrap ownership, old-HUD close before Loadout mount, native8471 after8283 geometry, refresh after HUD restoration, all owned Quick Options entries, stale/invalid input, Skills no-remount and Settings handoff. No profile writes or live process changes occurred. Final shared build and visual confirmation remain with the parent task.
