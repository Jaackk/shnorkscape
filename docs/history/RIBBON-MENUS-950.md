# Native 950 ribbon and management menus

This pass uses the paired 950 cache's management catalog and native layouts. Work is contained in 950RevTest; the 910/947 project and 950OpenSource are unchanged.

## Implemented surfaces

- Twelve bottom-ribbon icons: Hero, Customisations, Powers, Adventures, Community, Marketplace, RuneMetrics, Settings, Skills, Backpack, Equipment and Chat.
- One management-window owner handles page selection, close/Escape, displacement and restoration of existing HUD interfaces. The bootstrap now records the actual interfaces opened by the frontend, rather than assuming their parents.
- Hero Skills keeps the existing native skill guide. Loadout reuses the authoritative inventory/equipment transactions, including native item checks; equipment moves to the management page and returns to its HUD parent afterward. Summary uses actual skill, money and server quest state.
- Adventures Quests browses the current cache catalog (362 definitions, including hidden subentries), with native overview, requirements, rewards, journal, sort/filter controls and session bookmarks. Canonical quest gameplay and saved quest progression are separate work; browsing grants no completions or rewards.
- Adventures Beasts has 58 current-cache boss entries and native descriptions. Kill records and encounter actions are unavailable.
- Adventures Minigames has 51 distinct activities, six filters, descriptions/rewards information and session favourites. Participation, travel and Spotlight rewards are unavailable.
- Native Quick Options opens from the minimap exit icon. Settings/Ribbon/Controls route to the existing Settings owner. Full logout uses the verified native packet and the session's existing final checkpoint; return-to-lobby is not implemented.

## Remaining backend work

Native page navigation is not equivalent to implementing every feature shown inside a page. Cosmetics/appearance application, abilities, canonical quests, achievements, social/grouping services, RuneMetrics history, calendar/challenges and saved layout editing retain separate backend work. Marketplace is explicitly unavailable. Change Name and My Examine give clear feedback rather than pretending to apply account changes.

No new save schema, account grants or quest completion changes are part of this pass. Activity favourites and quest bookmarks currently last for the session.

## Verification

The native catalog and component/script bindings are pinned against the local 950 cache. Tests cover real encrypted packet framing, stale clicks, modal ownership, inventory aliases, logout restrictions and disposable-profile final saving. See logs/ribbon-final-*.log and the component-specific reports in protocol-analysis.

Live results and the final installed engine hash will be recorded in HANDOFF-950.md after deployment verification. As of the current verification run: native ribbon navigation, quest sorting, Hero Loadout appearance and HUD equipment restoration are confirmed. Exit input is still under investigation; boss/activity adapters await their combined-build live check.
