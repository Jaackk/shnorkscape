# Native950 exit menu

The minimap frame's Exit action is1477:99, dynamic slot1, operation1. It comes from enum7716[1004] -> struct21278 param3507. It is not an action on the minimap content interface1465.

Native950ExitUi owns the paired1433 Options Menu. Bootstrap removes the old1433 mount at1477:751, which is in the hidden Dialogue Box layer. Opening uses an empty child806 under the dedicated full-screen quick-options wrapper805. Scripts8177/8179 explicitly show/hide805 and own the native input context. The cache does not identify which of equivalent children806/807 originally hosts modern1433 rather than legacy274;806 is an intentional clean mount under the proven wrapper and requires rendered-client validation.

The complete104 inspected components decode to their exact byte boundaries.122 cache files are pinned in native950/exit-ui-950.properties. The read-only generator is tools/generate_950_exit_ui.py and its output is protocol-analysis/exit-ui-950-evidence.json.

Current950 menu buttons from script2935:

| Component | Native purpose | Implemented behavior |
| --- | --- | --- |
|1433:15|Settings|Returned to the existing Settings owner|
|1433:22|Edit Layout Mode|Explains that saved layout editing is not available|
|1433:35|Ribbon Setup|Returned to the existing Settings owner|
|1433:36|View Controls|Returned to the existing Settings owner|
|1433:43|Report Issue|Gives local-server issue-reporting guidance|
|1433:66|Hop Worlds|Explains that World1 is the only local world|
|1433:69|Exit to Lobby|Hidden through parent67; stale clicks cannot silently log out|
|1433:72|Logout|Opens the native confirmation overlay62|
|1433:79|Close|Closes this owner's menu|
|1433:86|Confirm Logout|Requires an armed confirmation and passes all native logout restrictions|
|1433:89|Cancel|Retires the pending confirmation|

Ordinary dialogue interaction is separate and remains mounted on750. The helper's close is idempotent: once no longer open, it does not run8179 again or disturb a newly opened management window. Root8/Escape closes an owned Exit menu before Settings handles the same entry.

Accepted logout rejects dead/inactive/finished characters, action locks, pending teleports, forced movement, recent incoming or outgoing combat, emotes, and controller canLogout vetoes. Rejected logout leaves the character active and preserves walking/actions. Accepted logout stops the player's action and route, retires the menu, marks the player inactive to fence already queued same-tick actions, and invokes the native dispatcher. It does not call legacy Player.realFinish/forceLogout or save a second independent character state. Native950Session.close remains the sole final checkpoint and removal owner.

Root integration must also check inactive/closed-channel state inside Native950Session's queued-action drain, including WalkRequest. Other modal openers close this owner before opening their surface; the1433 logout/confirm handlers must be dispatched before such generic replacement cleanup.

The original dispatcher used channel closure without a logout packet. The independently reviewed zero-payload202 parser is documented in logout-202-950.md; root owns its writer/flush-completion integration. Full logout is distinct from return-to-lobby and from quitting the client process. The latter two are not implemented by this helper.

Native950ExitUiAcceptance passed62 checks, including cache-pinned editor mount/wrapper ownership, absence of manual3477 writes, native-close reconciliation, and a real native session's same-tick final backpack mutation saved to a newly created temporary profile followed by player removal and slot release. No real user's profile was opened by the probe.

The native1475 saved-layout editor is mounted at the cache-stable host1477:692. Its onLoad8748 calls2462 and therefore owns entry into global Edit Mode; the server must not manually write varc3477. Save & Exit1475:43 is routed through native wrapper8743. Revision950 script8754 has two integer arguments but never reads its first argument: it reads the selected save target from client varc139445, so the wrapper's required callback argument is a neutral ignored placeholder rather than a server-selected layout. X1475:20 is routed through native wrapper8746, which owns immediate discard versus native confirmation. Both wrappers reach8754, whose teardown includes2464,8751(0),6593(0) and8164. The server retains mount ownership until native CLOSE_MODAL reports that teardown completed, then only reconciles its mount registry; it never manually clears3477. Escape is deliberately not intercepted because a revision950 mapping from the server-visible root action to8746 has not been established.

The first live806 mount rendered1433 but all tested static/dynamic clicks reached the game world. Hiding root808/809 as an isolation experiment then produced an invisible menu after fresh login; that experiment is reverted. The current candidate explicitly shows805,806 and1433:0 after8177, and invokes13831(1) directly. Live validation is still required. The acceptance checks packet ordering and rejects reintroducing sibling hides; owner open/close diagnostics identify server-side closure separately from client rendering.

Cache dependency evidence:8177 instructions31-33 show1477:805,34-36 hide1433:62,37-39 hide274:192,40-44 call6739(9)/8841(1,1),45-46 call13831(1). Interface274 has218 components and192 exists. An interrupted legacy-interface initialization is a hypothesis, not a demonstrated cause.13831 modern instructions39-41 set0 through normalized947 opcode08be (95001cc) on1477:808. Native callback1401d0320 sets widget+0x41 bit4 for0; pointer walker1401a454a skips its input-queue clearing whenbit4 is set.806 and807 have identical raw cache bytes. Neither cache nor native literal references establish that changing between them fixes this issue.

Resume handoff after the installed9422C0 candidate: live control was stopped with physical Escape. No new source or process change was made during the final audit. The next smallest experiment is changing only Exit openSub from walkable=true(type1) to false(type0), retaining host806 and the explicit visibility/direct13831 recipe. This is a hypothesis requiring live verification, not a proven universal rule. Verified current examples: Native950ToolbeltUi opens1944 at735 withfalse; Native950ProductionInterface opens top-level1370 at735 withfalse and child1371 withtrue; Native950ForgeUi opens37 at726 withtrue; Native950Settings opens1448 at715 withtrue. Earlier claims that Forge usedfalse were incorrect. The packet writer encodes body18 as128 minus the boolean value.

If the one-flag experiment fails, a bounded fallback is native Central slot1007: paired950 enum7716[1007]=struct21304, param3503=1477:732,3505=1477:735,3518=frame21262. Its default512x336 host accommodates1433:0's fixed470x234 centered root without any manual resize. Open exclusively after existing modal cancellation, use openSub(1477,735,1433,false), register ownership at735, show732, initialize1433 via13831(1), set current control masks, and run1364. Keep805 hidden with8179 rather than opening it via8177. Do not change808/809 visibility. On close, guard current parent ownership, close735, unregister1433, hide732, run1364 and8179, restore8180(1,1)/root8 mask254. Leave the native1433 frame's key-1 drag setup untouched; do not install custom drag hooks or call11145/8389 sizing. This fallback is prepared only, not implemented or validated.

Native1433 control hooks still refer to805:8178 closes it; ordinary8181 can call8182, which toggles805;8146 delegates5588/20343;Logout72 specifically calls4143 confirmation. A Central-host implementation must retire805 with8179 when consuming owned clicks and duringclose, so these local hooks cannot leave an empty full-screen overlay. All management/settings handoffs must close the Exit owner before opening their ownhost. Central migration needs new cache pins for21304/21262, relevant root732/735 frame components and1364 dependency proof, plus packet ownership/regression and live background-click,Settings,Logout/Cancel/Confirm,Escape andclose-reopen checks.
