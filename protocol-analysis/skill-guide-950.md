# Native 950 skill-guide repair (2026-09-12)

The earlier guide borrowed Settings window key9 and Gameplay page21179. Trying Hero key0 without selecting its Skills tab let native layout callbacks select Hero's default Summary tab. Summary struct21143 puts its first panel at x512 with width230, which explains the shifted/clipped skill grid. Hiding the management tab strip and changing root hosts did not correct that state.

The repaired owner selects varbit18994=0 (Hero), varbit18995=2 (Skills), and varc2911=0 before shell initialization. Script441's actual decoded switch reads18995 for Hero and clamps unset0 to page1; the independent evidence probe executes that script and obtains page2 only after18995=2. Enum7699[0]=21142, param3449=21144, and21144 specifies1218 at x0/y0, width742/height450. The native initial layout8288 and subsequent8186 callbacks therefore select the same correct Skills page.

Mount chain:1477:715 ->1448,1448:3 ->1218,1218:0 ->1217. Script5682 gets enum1482's guide skill argument before1217 is attached;1217 onLoad5689 calls5690 to build the cache-owned unlock list. All29 skill cells are independently resolved through enum7674's structs (params3440 and3441), including Archaeology and Necromancy. No raw-skill-ID fallback remains.

Category and sort dropdowns stay client-owned:10438 ->11988 ->11070/11436 stores category/sort, then5691 refreshes. The server must not rerun5682 for those clicks because it resets category selection. Hide Members' onOp14985 changes the preference; the server only requests5691 to refresh results. Other Hero pages are separate ports; their clicks acknowledge back to Skills with an explanatory message.

Opening is verified before another modal closes. Close/X/Escape retires1217,1218 and1448, restores the Options key and preserves scene1482 at1477:30. The session must close this owner before settings, map, banker/conversation, movement or teleport replaces it. Global experimental guide mount controls were retired.

Validation:7 focused JUnit tests passed. Native950SkillGuideAcceptance passes against the actual paired cache: all29 enum/struct mappings, strict interface/script pins,1247 generated session packets,29 open/close cycles without losing the scene. The compact evidence JSON contains actual hashes and argument metadata. Real client drawing, close/Escape, resize and dropdown appearance still need visual confirmation; automated packet checks cannot establish pixels.
