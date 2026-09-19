# Combat QA and Item Browser

## Combat QA

`;;combatqa` starts an observational session under `logs/combatqa/session-*`.
Starting, stopping and resetting the recorder do not change inventory, equipment,
bank, stats, position, action bars, Prayer, spell or adrenaline. `reset` clears
only recorder-side correlation/anomaly state and captures a new baseline.

Existing combat, action-bar, Prayer, Magic, potion, interface and status-timer
events feed the recorder. Meaningful transitions schedule bounded asynchronous
window captures; ability execution uses a start/250 ms/500 ms/1 s storyboard and
adds a channel-end frame when applicable. Logging, delayed scheduling and window
capture use separate bounded workers. A full worker drops evidence and records the
drop rather than blocking the world thread. No continuous polling or video is used.

`;;combatqa stop` writes `session-index.json`, changes `session-state.txt` from
`ACTIVE` to `UNREVIEWED`, and points `logs/combatqa/latest-completed.txt` at the
finished session. `timeline.jsonl` is the full correlated record and
`evidence-lifecycle.tsv` tracks each PNG.

Manual `;;bug` markers are copied into an active Combat QA session and their PNGs
are marked `UNRESOLVED_EVIDENCE`. Automatic captures become `UNREVIEWED`.

After reviewing a session, create `review-report.json` in that session and append
new lifecycle rows for individual screenshots. The newest row for a filename wins.
Only `REDUNDANT`, `STALE` or `SUPERSEDED` files in a non-active session with that
review report are eligible for `;;combatqa cleanup`. `ACTIVE`, `UNREVIEWED`,
`UNRESOLVED_EVIDENCE` and `BASELINE` are never deleted by the command.

## Item Browser

`;;items` immediately opens interface 1265's native item-container grid with a
small cache-backed Combat Alpha Testing Kit. Its live revision-950 Search owner
is component 41 and Recent is component 32; their visible text children are not
input owners. Search temporarily closes the grid, opens the verified native text
input, and restores the browser with up to the 40 best ranked cache-index matches.

Components 46 and 47 are the cache-authored list/grid display-mode controls, not
pagination. Live revision-950 testing established that this fixed-size surface is
visually safe at 40 entries. Larger dynamic row counts make item models overlap,
so broad searches retain their ranked 40 best matches and ask for a narrower query.
The browser does not invoke script 150: that script owns grid geometry together
with option strings, so using it only to extend the context menu also changes the
layout. The native cache-authored Info/Buy-1 wording is retained rather than
regressing the clean grid. Left-click/Buy-1 both remain safe Give-1 paths.

Each render creates one immutable snapshot and publishes that exact sequence to
container 139. Clicks resolve only against the snapshot: slot is authoritative
when the client omits an item ID, while a unique visible client item claim can
recover a stale slot. A claim outside the snapshot is rejected and remounted.
Every view change and grant remounts the V1 surface so the client's stock hooks
cannot retain the previous view while displaying new icons.

Left-click gives one immediately. The server retains bounded quantity handlers for
verified input actions, but does not override the native grid's context menu or
geometry. Grants revalidate the item against the cache-backed
container catalog and use `Native950Skilling.giveItem`, preserving controller,
stack-overflow and backpack-capacity rules. Bug Test and Combat QA record the
view, query, complete displayed ID order, incoming slot/item/option, resolved ID
and grant result for future live correlation.

`Native950ContentCommands.itemMatches` is deliberately UI-independent so a future
NPC or object browser can reuse the same indexed-search pattern without sharing
item-grant behavior.
