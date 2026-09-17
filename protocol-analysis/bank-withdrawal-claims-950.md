# Bank withdrawal item claims, revision 950

The installed bank uses a single **517 subinterface at1477:693**. This attachment is part of the prediction contract: the client's own bank-open predicate tests that exact slot. A partial withdrawal retains the original item actor; an exhausted row becomes actor **48447** in the supported open-bank path. Server reconciliation may compact the owned bank afterwards, but it must not accept an unrelated next-row ID as the selected source.

## Direct cache and native evidence

The selected cache was decoded through the independently established947-to-950 CS2 opcode map. Original exhausted-actor instruction listings and native byte slices remain in [withdrawal evidence](bank-withdrawal-claims-950-evidence.json). The complete mount-dependent reproduction is in [bank-mount evidence](bank-mount-950-evidence.json).

- Script14073 calls6793 for the bank operation, then schedules refresh through9314. Script6793 obtains the clicked actor's item/quantity through14344/14345 and calls withdrawal script6794.
- Script14362 selects **517:201 and the supplied clicked slot** at instructions0–4. Exhaustion on the non-placeholder path passes `(-1,0)` into13796; partial withdrawal passes the original item and remaining quantity.
- Script13796 forwards into13798. Script13798 converts -1 or48447 into actor **48447, quantity0**, removes ordinary actions and leaves Examine. This is UI prediction state, not a new owned item or an item-definition morph.
- The native IF_BUTTON routine retains its supplied actor reference at `0x1401a9416`. After onOp, `0x1401a98a6` reads that retained reference and `0x1401a9905` calls its item getter immediately before sending. Later mutation of that same actor therefore changes the reported item.
- The observed item-display setters share native sink `0x1401d0c40`; its virtual setters change the selected actor's item/quantity. No universal actor deletion/recreation rule follows from that trace.

## Capacity and source validation

Script6794 caps the requested quantity to stock and uses14351 to find the unmovable remainder. If the whole request is blocked, it returns before mutating actors. Otherwise the capacity-limited moved amount reaches14362. Consequently, a partly full backpack authorizes48447 only if the quantity that can actually move exhausts the authoritative source row. A full backpack keeps the current actor claim.

The adapter requires an existing catalog-valid source at the supplied slot, unchanged during the current input batch. It predicts48447 only when movable quantity equals the positive bank quantity; partial or blocked transfers require the current ID. Foreign IDs, next-row guesses, empty slots, invalid sentinels and same-tick shifted slots remain rejected. Current-cache note conversion is resolved before evaluating output stack capacity. The shared bank handler receives the authoritative source ID. Opening Withdraw-X remains an exact-ID request because no items move until its owned quantity reply is validated.

## The mount-dependent compaction branch

Script **10906**, instructions0–5, reads struct21308 parameter **3503** and tests whether subinterface517 exists there. The full current-cache hash is **1477:693**. Parameter3505 resolves **1477:695**; mounting only there makes10906 false despite a visible bank.

When10906 is false,6961 calls14354 ->14358 immediately. Compaction can replace the exhausted clicked actor with the next nonempty actor from14360 before IF_BUTTON reads its retained reference. It also prevents9316 from taking the ordinary bank-redraw branch. This reproduced the user's claims exactly: clicked slot6 held211 but sent next-row215; slot7 held215 but sent24000; slot11 held20000 but sent62789. Refreshing containers or reopening at the same incorrect mount could not repair that branch selection.

The correction keeps **one bank at693**, with matching server contains/get/remove state and one native wire owner. It preserves the earlier fix for duplicate Search input without accepting a different owned item. The actual-cache bounded interpreter executes10906/6961/14354/14358/14360/9316: the sole695 negative case produces all three reported neighbors and skips redraw; sole693 retains48447 and redraw. Retail-capacity lookups and drawing operations are stubbed; the branch and compaction programs are the cached programs.

## Authoritative refresh state

Varp **8971** is the occupied bank span. Varp **8970** is the earliest empty slot used by the compaction scripts. The installed build binds8970 and resets it to **-1** with authoritative compact-bank refresh; the original generateContainer/getNullItemSlot convention and14354 cleanup confirm this no-hole sentinel. The600-slot wire padding is outside the occupied span and does not require8970 to name its first trailing empty entry.

Selected quantity is synchronized through varbit45189, saved-X through varp111, and note mode through varp160. All Items remains45141=1 and ordinary coins go to the backpack through45158=1. Notes and scalar quantity controls are supported; structural placeholders, tabs and presets remain outside this ordinary prediction model. Retail capacity display is a separate documented limitation.

## Installed validation

Engine SHA256 `84970e322f673e889d6a4a2fa1fa5e52eeac7908c5fd1106ed7ea0eab49d3aac` is installed (deployment server PID18944, client PID15024). Recovery copy: `implementation-backup/2026-09-12-bank-slot-lifecycle/before-deploy`.

The unit suite passed **1,149 tests with2 existing skips, zero failures/errors (1,151 discovered)**. Bank acceptance passed **80 encrypted actions,62 ticks,1,377 frames**; general UI passed **37 actions/473 frames**; equipment passed **136 frames**; launcher checks passed **19**. Startup verification passed **138 scripts,24 variables,136 bank bindings**. The actual-cache wrong-mount negative reproduction is part of the evidence. [Final validation record](validation-bank-slot-lifecycle-2026-09-12.json).

The user manually retested single-item withdrawals, redepositing those items and normal Search typing, and reported: "it seems to work properly now". Fresh live logs show successful exhausted-row withdrawals for205,438 and63001 with actor48447, a partial436 withdrawal retaining its current ID, and Deposit inventory control39. The final log audit counted **29 successful transfers and 2 residual stale-claim refusals**. One request claimed item436 at slot14 when the authoritative item was24000 after an earlier211 exhaustion; its changedThisTick flag was false, so the stale claim crossed a refresh boundary. Retrying with the correct actor48447 succeeded. The other request claimed48447 at slot15 immediately after215 at slot14 was exhausted; changedThisTick was true, and the ambiguous shifted-row request was correctly refused. The persistent wrong-mount failure is fixed, but rapid or queued clicks can still reach these protective guards. Eliminating such refusals requires further study of client refresh acknowledgement and stable container views, while preserving item-identity checks. Earlier whole-cache catalog totals are earlier verification, not reruns for this deployment. [Comprehensive slot architecture and remaining regression policy](bank-slot-lifecycle-950.md).