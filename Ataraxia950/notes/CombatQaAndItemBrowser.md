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
small featured catalogue. Its live revision-950 Search owner is component 41 and
Recent is component 32; their visible text children are not input owners. Search
temporarily closes the grid, opens the verified native text input, and restores
the same browser with every ranked cache-index match split into 40-result pages.
The grid is read-only and uses container 139 only as a client presentation source;
it does not create a server Shop or alter bank/equipment state.

A result click temporarily closes the grid before opening quantity choices for
1, 5, 10, 100 or the verified native count input. This ordering is required: the
shared dialogue host is otherwise hidden behind the large shop surface. Submit,
cancel and grant all restore the same results/page, while Recent records successful
grants. The final grant revalidates the item against the active cache-backed
container catalog and uses `Native950Skilling.giveItem`, so controller rules,
stack overflow and backpack capacity remain authoritative and atomic.

`Native950ContentCommands.itemMatches` is deliberately UI-independent so a future
NPC or object browser can reuse the same indexed-search pattern without sharing
item-grant behavior.
