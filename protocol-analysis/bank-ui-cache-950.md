# 950 bank interface audit

Read-only cache investigation, 2026-09-12. This records the state found at the start of the banking milestone; runtime changes made afterward should be described in the implementation handoff.

Reproduce with `tools/verify_950_bank_ui.py`. The derived evidence is `protocol-analysis/bank-ui-cache-950-evidence.json`: 341 component headers, 23 exact serialized control hooks, 60 decoded client scripts, and ten control-varbit records. The cache is not modified. This is not a claim of visual acceptance in the running client.

## What was actually missing

The live bootstrap explicitly hid quantity panel 517:91, worn-equipment destination 58, familiar destination 62, placeholder toggle parent 122, and note toggle parent 126. The live router admitted only bank grid 201, backpack grid 15, deposit backpack 39 and close 317. Other native controls could be visible, and some could change client state locally, without a matching server operation.

This is a bank-system integration gap, not missing item-definition options.

## Exact controls in interface 517

| Function | Click component | Native hook | State / implementation requirement |
| --- | ---: | --- | --- |
| Deposit backpack | 39 | 8912 | Existing route; client predicts deposits |
| Deposit worn equipment | 42 | 8913 -> 8916 | Atomic equipment-to-bank transaction; refresh equipment and appearance |
| Deposit familiar | 45 | 8914 | Requires familiar inventory backend |
| Deposit coin pouch | 48 | 8915 | Uses native container 623; current client normally keeps withdrawn coins in backpack |
| Automatic tab switching | 51 | 6298 | Only meaningful with persisted custom bank tabs |
| Withdraw to backpack | 56 | 13745(0) | Destination varbit 45139 = 0 |
| Withdraw to worn equipment | 60 | 13745(2) | Destination 2; must integrate cache-driven equip requirements and atomic swaps |
| Withdraw to familiar | 64 | 13745(1) | Destination 1; requires familiar inventory backend |
| Default quantity 1 | 93 | 8903 -> 6962 | Varbit 45189 = 2 |
| Default quantity 5 | 96 | 8903 -> 6962 | Varbit 45189 = 3 |
| Default quantity 10 | 99 | 8903 -> 6962 | Varbit 45189 = 4 |
| Default quantity All | 103 | 8903 -> 6962 | Varbit 45189 = 7 |
| Default quantity X | 106 | 8903 -> 6962 | Varbit 45189 = 5; amount is varp 111 |
| Change saved X | 114 | 8903 | Selects X locally; server must open a scoped amount request |
| Placeholder toggle / delete all | 123 | 8886 | Option 1 toggles varbit 45190, option 2 runs removal confirmation; needs persisted zero-count slot metadata |
| Item / note toggle | 127 | 8866 | **Varp 160**, not a guessed varbit; 0 = item, 1 = note |
| Group Ironman storage | 131 | Cache-authored control | Separate storage backend |
| Bank PIN | 134 | Cache-authored control | Separate PIN lifecycle; do not fake success |
| Costume room | 140 | Cache-authored control | Separate player-owned-house storage backend |
| Diango | 143 | Cache-authored control | Separate reclaim backend |
| Metal bank | 146 | Cache-authored control | Separate metal storage interface / container integration |
| More storage | 149 | 16587 | Local storage submenu; its entries still need server handlers |
| Transfer | 152 | 13746(0) | Varbit 45191 = 0, mirrored in 45223 |
| Presets | 153 | 13746(1) | Varbit 45191 = 1; selecting a panel does not implement preset storage |
| All items / tab positioning | 165 | 8905(option,1) | All-items selection varbit 45141 = 1; other operations alter layout/tab state |
| Search | 237 | 13890 -> 13903 | Cache-local name filtering and keyboard input |
| Cancel search | 238, 239 | 13898 | Cache-local input cleanup |
| Close | 317 | Native close control | Existing route |

Dynamic item grids are bank 201 / container 95, backpack 15 / container 93, and worn equipment **35 / container 94**. The worn grid is populated by script 13353 -> 9236 with the caption `Deposit`; it is not the normal equipment panel 1462:31. A large bank layout can show it even when the worn-destination toggle is hidden.

## Item menu operation numbering

Client script 13798 authors these ordinary bank row operations:

| Native option | Meaning |
| ---: | --- |
| 1 | Selected default quantity |
| 2 | Withdraw 1 |
| 3 | Withdraw 5 |
| 4 | Withdraw 10 |
| 5 | Withdraw saved X (varp 111) |
| 6 | Enter Withdraw-X |
| 7 | Withdraw all |
| 8 | Withdraw placeholder |
| 9 | Context-sensitive action (including withdraw-one in a special branch) |
| 10 | Examine |

The selected default quantity's duplicate explicit caption is cleared locally. Enabling an event mask alone is unsafe: option 1 must use the same authoritative selected quantity that the client uses for its optimistic item mutation. Old fixed tables map option 1 to one and omit saved-X option 5.

Notes change the output inventory ID and stack capacity, but the clicked bank actor remains the source unnoted item until its quantity is exhausted. Capacity / exhausted-actor validation must use the output note's stackability and stack room. Full-stack prediction still needs the authoritative source slot, requested amount, and changed-slot checks.

## Search is already implemented in the cache

Script 9325 loops from zero to occupied bank span (script 14337 reads varp 8971), selects `517:201 child[originalSlot]`, reads `container95[originalSlot]`, tests the name, and only changes the actor's position and visibility. It does **not** assign compacted search-result indexes. Therefore the existing exact bank-slot ownership checks remain valid while searching.

The native on-inventory/update logic re-applies the filter. A server handler should not reset search on each local click or replace the client filter with a second, independently indexed list. Test filtered withdrawal of first, middle, and final source slots, including full-stack compaction.

## Custom amount input

Component 114 only selects the X mode in its on-operation hook. The server must establish whether the following amount changes the saved default or withdraws from a particular bank slot.

The embedded field is rooted at 517:111; 7202 -> 9833 binds the keyboard. Submission reaches 7211 -> 13121 -> 10519 -> 7208. Numeric mode 7 sends the normal count-dialog submission. The saved value is varp 111, whose transmit callback 2259 updates the displayed amount and item captions. Reuse the existing scoped amount-validation infrastructure; do not accept an arbitrary count without an open-bank request and epoch/source ownership.

## Responsive layout matters

Enum 7716 key 1017 resolves to struct 21308: primary window mount 1477:693 (parameter 3503), plus layout component 1477:695 (parameter 3505). Earlier notes called these wrapper/host; those labels alone must not determine where the bank is mounted. Cache script 10906 explicitly recognizes bank 517 at parameter 3503, so the bank needs one mount at 1477:693. The authored root is 714 x 555; the wrapper's initial header is 600 x 668 and the host is 1024 x 768. These header dimensions alone do not prove clipping: wrapper on-load 8409 and bank on-load 8420 participate in layout, and script 9313 computes the responsive arrangement.

Script 9313 switches between transfer and presets, conditionally re-shows quantity panel 91 and preset panel 116, and displays inventory columns based on available space. A one-time hide of a responsive child can be overwritten by the next local resize or layout refresh. Test at both short and tall window sizes. Disable unsupported mutating operations in routing as well as presentation.

## Boundaries for the current single-tab bank

Quantity selection, saved-X, notes, search, carried-item deposit, and worn-item deposit can use the existing bank container and shared item metadata. Worn-destination withdrawal is a larger atomic bank/equipment exchange, but should reuse the current cache-driven equipment planner rather than a list of wearable item IDs.

Presets, custom bank tabs, placeholders and familiar storage require persistent state that the current compact single-tab item arrays do not contain. They should not be represented as completed merely because native buttons can be displayed. Dragging items also requires a verified drag packet and authoritative reorder transaction; the cache installing drag hooks is not evidence the server supports it.


## Remaining capacity-display mismatch

The authoritative bank currently holds **600 slots**, and the server validates its item actions against that limit. Publishing occupied span in varp 8971 and container 95 with 600 entries is sufficient for the supported item grid and scrolling: script 14337 reads the occupied span, and the layout retains original slot indexes. Native allocation of up to 1820 item actors does not extend the server's accessible slot range.

The displayed maximum at **517:249** is a separate retail calculation. Component 517:244 schedules script 13830, which scans container 95 for used slots and obtains the displayed capacity from scripts 5777/5778. Those scripts begin with 1820 and subtract unavailable retail entitlements (20, 50-slot purchases, 250, and 100). Their normal combined minimum is 700, not this project's 600-slot policy. The free/member presentation can split that total into separate figures; it still does not derive its limit from the transmitted container length.

A one-time write of varp 8968 or 8969 cannot reliably override this display. Script 13830 first calls script 5776, which clears both cached capacity variables to zero, and then recalculates them through 5777/5778. Subsequent inventory or relevant variable updates schedule that calculation again.

This remains a known **display limitation** for the next bank-state/layout milestone. The server continues to enforce 600 slots. A durable correction needs a verified capacity-rendering lifecycle that displays the authoritative bank limit; it should not simulate purchased retail entitlements or repeatedly race the native redraw with text packets.


## Duplicate search characters: bank mounted twice

A subsequent live check found that manually typing one letter into Search displayed two letters. The runtime source audit found two `IF_OPENSUB` routes during a single bank open: `MainInterfaceComponents.BANK` selected wrapper **1477:693** because cache struct 21308 has nonzero parameter **3494 = 21329**, while the native recipe separately opened the same interface 517 at the authored content host **1477:695**. The wrapper and content host are distinct cache components; both must not be used as simultaneous bank content mounts.

The exact cache keyboard path is now asserted by `verify_950_bank_ui.py`:

- Script **13903**, instructions 22â€“27, selects input root 517:234, mode 8 and maximum length 12. Instructions 45â€“52 call **9833** with input widgets 517:234/235/236/237/239.
- Script **9833**, instructions 56â€“67, installs one ordinary key handler, **7211**, on **517:235**, with typed-key and key-code event arguments. Its other nearby handler controls mouse cursor placement; it is not a second ordinary character handler.
- Script **7211** delegates once to **13121**. In Search mode 8, instructions 158â€“166 of 13121 read the shared cursor and input text, call string editor **7170**, and write both back. The serialized shared cursor and text operands are **33817856** and **34130432**.
- The editor checks keyboard context **11** and input-field visibility. Those checks do not distinguish two traversals of the same mounted interface group or deduplicate a key event. The search timer **13905** merely copies this input text into the filter and cannot itself insert a second character.

Two active bank mounts therefore provide two opportunities for the same key hook to edit the shared input text. The first duplicate-mount correction chose 1477:695, which stopped duplicate keyboard traversal but was later shown to be the wrong sole mount. The correct lifecycle is one native bank mount at **1477:693**, matching both server bookkeeping and the native presence predicate 10906. Component 695 remains part of the root layout, but must not independently host another copy of interface 517. No alteration to the cache's text-insertion algorithm is warranted.

Evidence boundaries: the cache assertions prove the slot identities, input binding and shared edit path; the runtime source/frame tests establish the duplicate mount and corrected single-mount packet lifecycle. Actual keyboard delivery is native-client behavior, so a fresh live Search check after deployment is still required before declaring the doubled-character symptom resolved.

Live acceptance after the single-mount deployment: the user answered "it works now" when asked to verify one character per keypress and bank close/reopen. This confirms the reported doubled-character symptom is resolved.


## Withdrawal refusals caused by the wrong sole mount

The later live error, "The item in that slot has changed", was not an item-definition exception. Saved bank rows and logged click actors matched a one-row shift: source 211 was followed by 215, source 215 by 24000, and source 20000 by 62789. Those following IDs, rather than the withdrawn source IDs, appeared in the rejected packets.

`tools/verify_950_bank_mount.py` now provides a bounded interpreter regression against the actual 950 cache instructions. Its derived evidence is `protocol-analysis/bank-mount-950-evidence.json`. The fixture executes the real bank-presence predicate, immediate/deferred compaction and dynamic actor-copy operations. Only unrelated responsive drawing and the below-capacity, single-tab fixture helpers are stubbed. It is stronger than a test that injects a hardcoded expected actor without executing the cache branch that chooses it.

| Cache program | Verified behavior |
| --- | --- |
| 10906, instructions 0–5 | Reads struct 21308 parameter **3503**, then tests whether interface 517 is mounted at that hash, **1477:693**. Parameter 3505/1477:695 does not satisfy the predicate. |
| 6794 -> 14362 -> 13796/13798 | An exhausted ordinary source actor first becomes sentinel **48447**. Partial or capacity-limited withdrawal retains its source actor ID. |
| 6961, instructions 5–14 | If 10906 is false, immediately calls compaction 14354. With the correct mounted bank, it instead records the earliest empty slot in varp 8970 for deferred handling. |
| 14354 -> 14358 -> 14360 | Finds an empty actor, obtains the next nonempty actor and copies its item/count into the empty child. Because this happens before the native sender reads the held actor, mounting only at 695 sends the **next row's ID**. |
| 9316, instructions 36–57 | Gates native bank layout and inventory/grid refresh calls 9313, 13748 and 9324 on the same 10906 predicate. The wrong mount also skips that refresh path. |

The regression reproduces **211 -> 215**, **215 -> 24000**, and **20000 -> 62789** with the incorrect 695 mount. With one 693 mount it keeps 48447 until the deferred refresh, matching the server's strict exhausted-actor validation. This supports correcting the mount rather than broadening accepted item claims, which could authorize a different source row after a stale click.

### Prediction boundaries

Selected quantity remains varbit 45189 (2/3/4/5/7 for 1/5/10/X/All), with saved X in varp 111. Script 6793 maps the selected mode before calling 6794. Withdraw-X option 6 predicts zero movement, retaining the source actor until the server amount request completes.

For backpack withdrawal, script 6794 uses the cert/note output ID when notes are possible, then calls 14351 to count room in the native inventory actor grid. Non-notable items retain their ordinary output and show the cache's cannot-note message. Full inventory returns before source mutation; partial capacity keeps the remaining source actor. Script 14362 updates the predicted bank actor, then destination inventory actors. The search layout preserves original actor indexes; it is not responsible for compacting search matches into alternate bank slots.

### Authoritative compact refresh marker

Varp **8970** represents the first pending empty slot within the occupied bank span. It is not the first unused slot in the padded 600-entry transmitted container. The original `Bank.generateContainer()` sets it through `getNullItemSlot()`, which returns **-1** for a compact occupied span. The native 6961 stores a predicted hole there, while 14354 resets it to -1 after compaction. An authoritative compact bank refresh should therefore publish **8970 = -1** before occupied span 8971 and container 95, so an obsolete client compaction marker does not survive the replacement snapshot.

These cache and source checks reproduce the actor mismatch without modifying the player's save. Final confirmation still requires live withdraw/deposit and Search checks after the corrected single-mount build is deployed.
