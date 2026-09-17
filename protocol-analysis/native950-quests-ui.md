# Native950 quest catalogue and journal adapter

This pass connects the actual Adventure / Quests list1783 and overview/journal1500. It does not port quest gameplay. Navigation owns the1448 shell and mounts; the quest adapter only owns its per-player selection, history, filters and session bookmark.

## Current cache evidence

- Enum169[1] -> struct508 param61 -> enum2252. Its362 sparse public quest keys map to362 quest definitions in index2/group35. The table has gaps and highest key370; sort order never changes the key transmitted by a row. Cook's Assistant is public key1, definition257. Its950 completion varp is2492, thresholds1/2.
- The supplied cache contains533 total quest definition files, including retired/unlisted rows. Only the362 rows actually used by the native list are exposed. Their cache quest points sum to472.
- All362 definitions consume exactly to their terminator with a strict decoder.950 opcode22 has a24-bit varbit id followed by started/completed int thresholds. Opcode17 uses a big-smart item/sprite id. These details differ from older simple16-bit decoders. Other decoded fields include name, ordering name, membership, difficulty, points, prerequisites, skill requirements and typed parameters.
-1783:6 onload2165 ->2160 creates one text child under1783:18 at its sparse enum key.2162 rearranges those existing children for sorting, sets View Overview/Journal option order according to status, and retains that key. Options1..3 are enabled; item-bearing and unknown/gap-row clicks are rejected.
-1500:337 onload4011 fills the real overview from quest definition varc699; public selection is varp3936. Server-supplied varc string2554 contains requirement text. The adapter derives it from the same current quest data, with character base-level/quest-point checks. Cache4011 supplies story, start point, length, membership, age, item requirements, combat advice and rewards where present.
-1500:369 and381 are client-local expansion controls:4249 installs4250 to reveal required-item/reward text. No invented player varbits are used. Accept Quest is hidden/disabled because browsing is not the NPC-driven acceptance workflow.
- Filter bits315/316/317/318/43789/43788 and native sorting callback1477:896 are scoped to an open quest tab. Sort callbacks require the dropdown to be armed; close invalidates them. Set Active uses3260 as a session-only bookmark and never starts a quest or awards anything.
-1013 SHA256 bindings cover the interface files, list enums, selected structs,362 quest definitions, used parameter definitions,77 scripts and state/filter variable definitions. The startup/open verifier rejects a changed cache.

## Progress and the910 systems

The active `content/com/rs/game/activites/quest/QuestHandler` has only the custom Death's Bounty in its quest table. This is not a Jagex quest and is never aliased to a cache quest. The wired Cook dialogue concerns the old server's custom Evil Chef adventure and ends with the existing unavailable message; it is not Cook's Assistant.

The separate `game/com/rs/game/player/QuestManager` holds three same-named canonical entries. The adapter reads their actual started/completed state without calling init/checkCompleted, both of which can auto-complete quests from player levels. Same-named legacy status maps only to coarse current-cache started/finished thresholds: King's Ransom key126/definition61 uses varbit11298 (5/90); Perils of Ice Mountain key136/definition328 uses11095 (10/150); Nomad's Requiem key162/definition170 uses10095 (1/12). Internal910 stage numbers are never copied into950 vars. No rewards or progress setters are called.

The native950 save schema currently has no quest-progress section. This pass changes no save format and promises no persistence for bookmarks or hypothetical imported legacy quest progress. All canonical quest journals explicitly state that the quest is not available yet; cached prerequisites/rewards are informational. Map hints are explicitly unavailable, while the overview's start-point description remains usable. Back/forward, overview/journal toggling, filters and list sorting are functional.

## Validation

`Native950QuestsAcceptance` passed against the actual950 flat cache and isolated compiled source on2026-09-12. It selects all362 rows through encrypted950 IF_BUTTON frames, checks native output packets, sparse identity/gap rejection, item forgery rejection, journal/overview, history, filters/reverse sorting, scoped dropdown, bookmark without progression, closed/stale input, and actual coarse legacy status conversion. Maxed levels do not auto-complete King's Ransom; in-memory legacy completion displays only the expected cache points and uses90, then clears when source state clears. No account file is written, no listener is opened and no live server/client is launched. Live rendering remains for the integration agent to verify.

Run from a temporary working directory, with console-only Log4j configuration to avoid project log files:

```text
java -Dlog4j.configurationFile=<console-log4j2.xml> -Dataraxia.native.verifyCache=true -Dataraxia950.data=C:\Users\developer\Desktop\950RevTest\Ataraxia950\data -cp <isolated-classes>;Ataraxia950\build\classes\java\main;Ataraxia950\resources;OpenNXT\runtime\lib\* com.rs.game.player.client.Native950QuestsAcceptance C:\Users\developer\Desktop\950RevTest\cache
```

Integration API: `new Native950Quests(player,channel)`, `opened(menu,page)`, `closed(menu,page)`, `handle(InterfaceAction)`; menu3/page2 owns quest browsing; menu0/page1 renders Hero Summary counters and scopes its account/profile buttons. Call `verifyCacheBindings()` in native cache preflight. No shared files were edited by this subtask.


## Hero Summary follow-up

Live testing first found designer placeholders999/999 at1446:28 Completed,33 In progress and38 Not started. Invoking8277 removed those placeholders but exposed five client-default completions unsupported by this native profile. The final adapter therefore uses server-authoritative canonical quest state for both counters and bars. It does not treat cached/default client quest variables as evidence of completed gameplay and does not reset unrelated quest variables that may gate other world features.

The eligible denominator follows the exact cache predicate:8277 calls18798, which returns quest parameter7838. Of362 catalogue rows,332 have that parameter unset/zero and30 are hidden subquest or seasonal entries. A fresh native profile consequently displays0/332 completed,0/332 in progress and332/332 not started. Actual same-named legacy completion/started state changes those counts; maximum skill levels do not. The live native list once reported337 items, a distinct client list display whose difference was not resolved by this task; the332 summary denominator is independently proven from8277's eligibility predicate.

The adapter sets those three native text components and invokes the existing11145 size helper for completed bar1446:49 and cumulative completed/started bar1446:45, using the native16384 proportional size units. It does not invoke8277 as the progress authority. Achievement Progress is outside this change.

Money Pouch at1446:22 displays the actual integer money-pouch balance, formatted with digit grouping. No bank/inventory balance is conflated with it and no money is modified. Actual button hitboxes1446:65 (Change Name) and1446:86 (My Examine) receive scoped, explicit unavailable feedback; their text children68/89 are not the click targets. Closing Hero Summary invalidates those actions. The irrelevant global Cook paragraph and revision/porting language were removed from in-game journals.

The updated paired-cache acceptance passed on2026-09-12. It captures actual outgoing packets and verifies all three exact counter texts, the completed-bar command, formatted pouch balance, encrypted account/profile click feedback, stale-close rejection, and server counts for actual completed/started canonical quests. All362 encrypted quest-list selections and the existing state/requirements/navigation checks continue to pass. This isolated test does not execute client scripts or claim that the final summary fix has been seen in the live client; that visual check belongs to integration.
