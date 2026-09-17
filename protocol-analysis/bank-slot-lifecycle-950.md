# Bank and backpack slot lifecycle — revision 950

2026-09-12. Read-only architecture audit following repeated live withdrawals rejected as “The item in that slot has changed.” Source review is combined below with the main task's read-only saved-state inspection and the cache agent's decoded script evidence. This document changes no items or account files. The corrected build is installed, automated validation is complete, and the user manually confirmed that withdrawals, deposits and Search now work properly.

## Actual cause: the visible bank occupied the wrong native slot

The earlier duplicate-input fix removed the second bank widget tree but kept the single bank at **1477:695**. That stopped doubled Search keystrokes while leaving a separate semantic error: the paired cache considers bank 517 open when it is attached at **1477:693**.

Cache script **10906**, instructions 0–5, resolves struct 21308 parameter **3503**, then checks for subinterface **517** there. This resolves to 1477:693. It does not test parameter3505/host695. The bank is a case where choosing the generic content-host parameter is not sufficient to reproduce the client's own open-state rule.

That predicate controls both optimistic item mutation and refresh:

- **6961**, instructions 5–14: when 10906 is false, immediately invokes compaction through **14354 -> 14358**. The true branch instead records the earliest empty slot in varp8970.
- **14358**, instructions 18–39: finds an exhausted actor48447, obtains the next nonempty actor through14360, then writes it into the clicked child. The subsequently transmitted button can therefore claim the *next row's item*.
- **9316**, instructions 36–57: the refresh path to9313/13748/9324 is gated by10906 being true. A correctly encoded server container refresh cannot repair the intended live bank presentation while that predicate is false.

The main task matched the live reports to the saved authoritative bank:

| Clicked slot | Authoritative item there | Next authoritative row | Reported actor |
| ---: | ---: | ---: | ---: |
| 6 | 211 | slot7 = 215 | 215 |
| 7 | 215 | slot8 = 24000 | 24000 |
| 11 | 20000 | slot12 = 62789 | 62789 |

The sequence follows a successful deposit of63709 but repeats after multiple bank close/reopen cycles. Those observations match the wrong-open-predicate compaction branch. They do not require a server-side reorder or lost item.

**Installed correction:** exactly one bank is mounted at the cache-tested 1477:693 slot. Server bookkeeping, contains/get/close operations and the native packet recipe use that same attachment. Next-row item claims remain rejected; accepting them would allow a stale action to target a different owned item. The client was restarted to clear leftover widget trees.

The [cache audit](bank-ui-cache-950.md), [script evidence](bank-ui-cache-950-evidence.json), and [mount-branch reproduction](bank-mount-950-evidence.json) establish this distinction. The bounded interpreter executes the actual cached 10906/6961/14354/14358/14360/9316 programs, stubbing retail-capacity queries and drawing operations only. Sole693 preserves the exhausted48447 actor and takes the redraw path; sole695 reproduces all three reported next-row actors and skips redraw. Earlier documentation identifying695 as the correct bank attachment is superseded.

## Installed build and validation

Engine SHA256: `84970e322f673e889d6a4a2fa1fa5e52eeac7908c5fd1106ed7ea0eab49d3aac`. At deployment, server PID18944 and client PID15024. Recovery copy: `implementation-backup/2026-09-12-bank-slot-lifecycle/before-deploy`. [Final validation record](validation-bank-slot-lifecycle-2026-09-12.json).

- Unit suite: **1,151 discovered; 1,149 passed; 2 existing skips; zero failures/errors**.
- Bank acceptance: **80 encrypted actions, 62 ticks, 1,377 parsed frames**.
- General UI acceptance: **37 actions, 473 frames**; equipment acceptance: **136 frames**.
- Launcher: **19 checks**. Startup verification: **138 scripts, 24 variables, 136 bank bindings**.
- Actual-cache negative reproduction: sole695 produces the three reported neighboring actors; sole693 preserves the intended exhaustion and redraw branches.

These checks use isolated fixtures. Earlier whole-cache catalog/appearance results remain earlier verification, not fresh runs of this deployment. The user manually retested single-item withdrawals, redepositing those items and normal Search typing, and reported: "it seems to work properly now". Fresh live logs show successful exhausted-row withdrawals for205,438 and63001 with actor48447, a partial436 withdrawal retaining its current ID, and Deposit inventory control39. The final log audit counted **29 successful transfers and 2 residual stale-claim refusals**. One request claimed item436 at slot14 when the authoritative item was24000 after an earlier211 exhaustion; its changedThisTick flag was false, so the stale claim crossed a refresh boundary. Retrying with the correct actor48447 succeeded. The other request claimed48447 at slot15 immediately after215 at slot14 was exhausted; changedThisTick was true, and the ambiguous shifted-row request was correctly refused. The persistent wrong-mount failure is fixed, but rapid or queued clicks can still reach these protective guards. Eliminating such refusals requires further study of client refresh acknowledgement and stable container views, while preserving item-identity checks.

## Four representations that must agree

| Representation | Owner and shape | Meaning of slot |
| --- | --- | --- |
| Owned backpack/equipment/bank | Native950Containers over the real Player objects | Backpack0..27, equipment0..18, one compact bank array |
| Original Bank projection | generateContainer / lastContainerCopy | Flattened legacy tabs; with one tab it is exactly bankTabs[0] order |
| Published native containers | container93/94/95, ID/quantity entries | Sequential authoritative indices; bank publication pads to600 entries |
| Client item actors | Dynamic children under517:201,517:15,517:35 | Item reference may be changed by an on-operation script before the button packet is sent |

The wire actor is not always the original clicked item. Validation must model the **specific verified client mutation** while retaining the original authoritative source slot. A visually adjacent result or matching item elsewhere is not permission to search the bank for a replacement target.

### Projection and compaction

`Bank.generateContainer()` flattens tabs1..N followed by tab0, with `getRealSlot()` applying the inverse traversal. The native save/container invariant permits exactly one tab, so both methods map index i to bankTabs[0][i]. There is no sorting by name, ID or inventory position.

`Native950Containers.deposit()` merges an existing bank ID in place or appends a new ID. It does not move earlier rows. `withdraw()` immediately removes an exhausted bank entry and shifts following entries left inside the same staged commit; partial withdrawals replace the existing stack without shifting indices.

`Bank.shiftItems()` only runs when it finds null entries. Normal native bank transactions already leave a compact array, so this call returns without reordering it. Original910 withdrawals could leave null slots until later compaction; the native path has deliberately stricter compact storage. This difference must be reflected in prediction/refresh tests rather than hidden by loosening slot checks.

`lastContainerCopy` is the original Bank's cached projection, not the native adapter's authoritative snapshot. `refreshItems()` regenerates that projection before choosing a full or sparse update. Native transactions create new Item objects, so this legacy reference-comparison delta can include more unchanged-value rows than necessary, but their order still matches the current one-tab bank.

## Refresh sequence and state synchronization

After a successful live transfer, the shared bank adapter refreshes the affected ordinary containers. `Native950Interactions.refreshBank()` then calls compactBank, sendInventory and sendBank. The final bank publication resets earliest empty slot **varp8970 to -1**, publishes occupied span **varp8971**, and sends a600-entry container95 snapshot. Both the legacy projection and the final native snapshot describe the committed bank in the same order. Both use the native u24 item-ID format.

Multiple refresh packets in one tick are redundant work, but are not evidence that a different bank order was published. The final full snapshot also corrects client optimistic mutations after a refused action. Its usefulness depends on the client's bank-open predicate and inventory-transmit scripts taking the intended branch—the defect above prevented that.

The original bank also publishes earliest empty slot **varp8970**. The faulty build attempted8970=-1 after deposit but dropped it as unbound. The installed build binds that variable and resets it to **-1** on authoritative bank publication. This is the original generateContainer/getNullItemSlot no-hole sentinel for the compact occupied span; trailing empty entries in the600-slot wire padding are not holes inside that span. Cache14354 also resets8970 after compaction. This repairs a separate incomplete client-state binding; the incorrect10906 result and next-row actor evidence establish the reported refusal cause.

Quantity and note mode are separately synchronized on bank open, supported controls and quantity completion/cancellation. Stored raw quantity mode11 means X; the native varbit encoding for X is5, while Last-X is varp111. Note mode is varp160. Capacity prediction must resolve the actual output note identity before deciding whether the source row will be exhausted. UI mode, authoritative selected mode and prediction must stay aligned.

## Transfer and guard behavior

### Withdrawal

1. Require an open, owned bank in reach and an eligible player; determine explicit/default/Last-X amount.
2. Read the current authoritative bank slot and output item identity, including current-cache note links.
3. Compute the capacity-limited movable amount from the actual destination inventory and stack room.
4. Compare the received actor with the verified prediction for this slot. In the correct open-bank path, a partial or capacity-limited row keeps its current ID; an exhausted row becomes actor48447.
5. Refuse any source slot whose identity already changed this input tick. Then call the bank transfer with the authoritative item ID, never a guessed next row.
6. Revalidate after controller callbacks; stage and commit source/destination quantities together; publish authoritative containers.

Full inventory and unsupported item states need accurate user feedback, but may not bypass quantity/identity checks. Current outer routing can report a broad destination error for a backend refusal; new diagnostics should record the precise rejection branch without using a misleading slot error for every condition.

### Deposit and multiple copies

A direct quantity deposit validates the clicked backpack slot first. It consumes that slot, then other slots with the same item ID until the requested amount or bank stack capacity is reached. Notes resolve to their verified underlying bank identity. Backpack holes remain holes; its slots are not compacted. The bank merges or appends once and the staged transaction conserves total units.

The live **Deposit inventory** button uses original `Bank.depositAllInventory()`, iterating current slots and invoking the native deposit adapter for each eligible item, then refreshing. Each individual deposit is staged and validated. The entire button is currently a sequence of transfers, not one all-or-nothing batch; later refusal does not roll back earlier valid deposits. The separate `Native950Containers.depositAll()` helper should not substitute for that live call path in acceptance evidence.

Equipment deposit has a different contract: it stages the eligible worn subset and commits that subset once, while refused slots remain worn. The shared current-cache equipment restrictions still apply.

### Quantity prompts

Withdraw-X, Deposit-X and Change-X pin bank instance, epoch and container snapshot. Closing/replacing the bank or changing the snapshot invalidates a pending transfer. Replies are signed64-bit values bounded to1..Integer.MAX_VALUE before use. Ownership is retired before applying the result; duplicate replies cannot repeat the same request. There is no request ID in the count packet, so that protection must not be described as a general client acknowledgement protocol.

## Search and timing

Search script9325 iterates original occupied bank indices, reads container95[originalSlot] and moves/hides the corresponding child. Search does not assign compacted result indices. Filtered item clicks therefore still use original slots. Server refreshes must allow the native filter to reapply instead of replacing it with a second independently indexed list.

At the start of `Native950Session.tickInput()`, beginTick clears changed-slot sets, then the server drains that tick's queued actions. Committed refreshes mark changed source identities; a second action in that same batch cannot reinterpret a cleared actor as the new occupant of a shifted slot. Ordinary updates are flushed with tickEnd after movement/checkpoint/frame work. This is bounded protection against ambiguous queued actions, not proof that the client has acknowledged every intermediate snapshot. Tests must include bursts and tick boundaries explicitly.

The repeated live failures across close/reopen cycles cannot be explained merely by this same-tick guard: changed sets are cleared, and the bank is refreshed and reopened, while the incorrect native predicate remains false.

## Previous verification gaps and regression policy

The former encrypted acceptance manually supplied actor48447 for single-item exhaustion and asserted server results. It tested the intended actor policy but did not execute the client's open-state predicate. The first Search fix further asserted one mount at695, thereby pinning an incorrect assumption. Cache shape/struct metadata, a successful IF_OPENSUB parse, and a screenshot of a visible bank do not prove that bank scripts consider it open.

The installed mount/branch and encrypted acceptance checks now connect the previously separated layers. Retain and broaden the following regression policy as bank features evolve:

- Run the actual10906 predicate against the proposed mount map; prove one693 mount and no competing695 bank, including shared close/reopen and trailing Close reports.
- Derive exhaustion and refresh branches from cached scripts under that mount state. Include the observed211->215,215->24000 and20000->62789 row relationships as regression witnesses; the wrong-mount branch must reproduce the reported next-row claims and the corrected branch must not require accepting them.
- Exercise first/middle/final exhaustion, partial/default/Last-X/explicit quantities, noted output, full inventory and capacity-limited All; verify exact server totals and final client projection after every action.
- Cover new-stack deposit followed by withdrawal, existing-stack merge, multiple identical backpack copies, mixed note/underlying deposits, and the live Deposit inventory sequence with restrictions/capacity.
- Click filtered first/middle/final source indices and repeat after compaction. Verify original-index mapping survives refresh.
- Test same-tick duplicate cleared actors, multi-action batches and next-tick valid actions, plus X reply invalidation, callbacks that mutate a source, and close/reopen preference restoration.
- Log, for rejected claims: mount/presentation state, operation, clicked slot, received actor, current ID/quantity, output ID, requested/movable amount, expected actor and changed-slot flag. Item totals and account state must remain unchanged on rejection.

Structural tab/placeholder/preset work remains governed by the [persistence audit](bank-persistence-feature-audit-950.md). This correction should preserve strict source-slot ownership and current character data while repairing the client state that generated the wrong claim.