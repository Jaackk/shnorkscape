# Developer Console: live input diagnosis and successor

## Physical Vulkan evidence — confirmed live

The first console open was broken: native Customisations tabs/title remained,
details were hidden, and clicking visible command buttons produced no console
operation. Right-click exposed only Cancel. This was a shared client lifecycle
fault, not 64 independent command failures.

A shared-state change (`;;god`) caused the existing console tick to redraw after
native startup. Controls then worked. The diagnostic toggle was restored to OFF.
Session `session-20260924-065938-283-jaxa` provides the following evidence:

- 07:12:38: physically clicked Favourites; interface1448:5 and nonce callback arrived.
- 07:13:19: physically selected Heal and clicked Execute. The authoritative `heal`
  command ran, with the visible restored-resources result and console completion.
- 07:14:38: native NPC search accepted `1` and returned Man, ID1. Selection opened
  its amount, respawn and placement controls.
- 07:15:18: Place entered native targeting. Clicking an open tile emitted the
  interface-on-tile action, source1448:11/slot1, coordinates3317,10142. Man appeared
  on that tile beside the player. The temporary placement is owned by Jaxa and
  follows normal temporary cleanup; it was not saved to the persistent world.

These are physical tests of the installed backend after a diagnostic redraw.
They do NOT establish acceptance of the successor's startup or appearance.

## Successor changes — automated verified / live test pending

- Opening no longer publishes actors before the native management refresh.
  Native CS8286 finishes its original varc2911 refresh, then acknowledges only a
  Developer Console session marked on the root host's inert text property.
  The server renders on that acknowledgement. Ordinary management screens retain
  the original instructions and send no developer notification.
- State polling cannot render before readiness. Repeated acknowledgements,
  closed-console callbacks and stale operation epochs cannot invoke actions.
  The marker is cleared on console close and before world placement.
- The real title uses the same struct21301/3506, dynamic-child3 contract as
  CS8289. The misplaced label on host713 is removed; native tabs are hidden.
- Ten top navigation buttons, category navigation, six descriptive command rows,
  separated native panes, larger native heading typography, dedicated details,
  parameter/usage controls and Execute/Place. The introductory text no longer
  overlaps browser controls. NPC and World tabs enter their browsers directly;
  Items opens the existing Equipment Library through its shared handler.
- Heal and common commands have specific descriptions rather than the generic
  description of an entire command-directory group.
- Bug Test records native readiness and operation epoch/actor boundaries.

Native actors, textured buttons, fonts, frame and targeting are reused. The action
registry, 77,850-definition index, permissions, ownership, persistence and undo/redo
remain intact. No combat, bank, action-bar or player-save implementation changed.

## Verification

- Full engine regression: 1,555 tests; zero failures/errors; two existing skips.
- Four Python native-script tests, including ordinary/developer/cleared readiness
  markers and native target selection, passed.
- Production JS5 reader accepted all seven replacement payloads, versions, CRCs,
  lengths and file identities; unrelated references are unchanged.
- Successor packaging and installer verification are recorded in the validation JSON.

## Remaining visual acceptance and limits

The running server and client cache were not replaced. The successor therefore
needs one restart via Apply Staged Update.cmd before its new startup and layout can
be physically tested. Do not describe the mockup as achieved based on these tests.
The current native800x600 shell is retained; the revised composition remains
smaller than the mockup. Model/ghost previews, icon-rich navigation and continuous
list scrolling are not yet implemented; browser previews currently show verified
metadata, and lists use Previous/Next. Do not substitute speculative models or
claim these features are complete.

The user's deferred failures remain: Christmas cracker Pull; unlocked ability
drag-off; equipment bindings selecting a bar.

## One live checklist

1. Fresh login: open `;;dev`; verify Developer Console title, no Customisations
   tabs, readable panes, and immediately working categories/search.
2. Select Heal → Execute; search/select one NPC → Place → click an open tile.
3. Close/reopen twice; open Items/real bank afterward and verify ability keybinds.
4. With Nooby, verify independent selection/placement and unchanged ownership.

No extra `;;god` or redraw command should be needed. If startup fails, Bug Test's
`awaiting-native-ready` / `native-ready` pair identifies the exact boundary.
