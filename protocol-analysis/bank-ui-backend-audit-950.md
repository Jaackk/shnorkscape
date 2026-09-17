# Native950 bank UI backend audit — 2026-09-12

Scope: read the original910 Bank/ButtonHandler, current950 Bank/Containers/Save/Interactions, the950 opening recipe, and latest server logs. No live character or save was changed by this audit. Backend implementation now adds note withdrawal and atomic equipment deposits; root owns UI controls and mappings.

## Existing surface and evidence

The basic bank is functional. The current opening recipe pins default quantity1, the All Items tab and coin-to-backpack mode, binds only item/backpack/deposit-all/close operations, and hides517:91,58,62,122,126. Native950Interactions rejects bank drags and routes only those four bank component categories. Logs show successful950 withdrawals followed by unhandled517:149 clicks; many attempted910 component/script/var writes are refused by the binding adapter. This is a partially exposed banking interface, not a broken item-definition decoder.

Original910 Bank.java still provides deposit-all, amount preferences, notes, tab arrangement and presets. Its search methods are already no-ops, consistent with client-driven filtering. Reuse its user-facing behavior and shared storage, but not its unguarded mutation routines.

## Implementation priorities and dependencies

| Feature | Backend status / dependency | Main correctness requirement |
|---|---|---|
| Quantity1/5/10/All/X and Deposit-X | Native quantity ownership exists for Withdraw-X only. Root must route selected quantities instead of fixed option1=1. | `bankPacketIdFor` only supports1/5/10/MAX; arbitrary selectedX needs a direct validated transfer. Cancel outstanding quantity prompts on mode changes. |
| Notes | Added generic current-cache output ID and capacity APIs, with container source/output overloads. | Preview, controller permission, commit and movement accounting must all use the output note ID; the bank source claim remains the unnoted ID. |
| Deposit equipment | Added staged eligible-subset deposit with whole-stack preservation. | Respect current bankability, removal lock2091, canRemoveEquip, capacity and final identity checks. Root must refresh/observe equipment as well as bank/inventory. |
| Search | Client audit confirms script9325 hides/repositions original517:201 child indexes. No separate server search index map is necessary for this path. | Retain original container95 slot claims; do not replace the actual bank array with filtered results. |
| Single-tab swap/insert | Existing save arrays already preserve row order; a staged reorder can work without a save schema change. | Prove native optimistic drag actor ordering; snapshot both source and target and reject duplicate/stale changes. Insertion target semantics differ from simple swap. |
| Multiple tabs | Original bank stores Item[][] and flattens tabs1..N then0. Current native containers require exactly one compact tab. | Add an explicit flatten/unflatten map, tab partition data and persistence together. Merely calling createTab makes subsequent native validation fail. |
| Placeholders | Not represented by current native containers/save/wire validators. | Current ItemID+amount rows require positive amount; placeholders need explicit bank-only representation and capacity/count rules. Do not relax backpack/equipment zero-amount validation. |
| Presets | Original910 nested BankPreset holds inventory/equipment/BoB arrays and flags; native schema3 does not serialize them. | Add persistent definitions, cache/current requirements validation and one staged bank/backpack/equipment plan. Old loadPreset mutates storage piecemeal and bypasses the new wear requirements/controller transaction. |
| Beast of burden / pouch / bank wear | Shared old methods exist, but950 ownership/storage/UI paths are not fully ported. | Each destination needs its own capacity and controller contract. Do not treat it as an ordinary backpack withdrawal. |

## Exact note fallback

Original910 `Bank.withdrawItem` attempts a certificate conversion when notes mode is active, the source is not already noted, a certificate reference exists and the item has no custom attributes (also an old per-ID exception14876..14892). If it cannot convert, it reports that the item cannot be withdrawn as a note and continues with an ordinary withdrawal and ordinary capacity. It does not reject every unnotable item.

The950 backend preserves that fallback behavior while replacing the old ID exception and unchecked reference with current-cache validation: `source.certId` must resolve to a named stackable noted definition whose `certId` points back to the source. Missing/invalid/unnotable links return the original source ID. Successful fallback sends an explanatory message. Charged/custom/Invention item state remains explicitly unsupported rather than being erased by conversion. Preview is pure and emits no messages.

Public APIs: `Native950Banking.withdrawnItemId(Player,int)` and `withdrawableAmount(Player,Native950Containers,int,int,int)`. The selected Bank.getWithdrawNotes mode must be the same for preview and commit. A controller changing note mode or source storage invalidates the transaction.

## Equipment-deposit behavior

`Native950Banking.depositEquipment(Player,Bank,boolean)` plans all eligible slots, then commits them together. Unbankable, removal-locked, controller-denied and full/overflow slots remain equipped. A worn stack is deposited in full or stays equipped in full. Existing bank stacks can receive gear even when no new bank slots remain. If some slots remain, the player receives a reason. Any callback mutation of captured inventory/equipment/bank state cancels the whole pending plan.

The old910 `depositAllEquipment` must not be exposed directly. It calls `addItems`, interprets a free-bank-slot prefix length as the number of processed equipment slots, and clears those source slots. `addItem` returns void and may silently reject an overflow or controller-specific case. For example, overflowing an existing bank stack can cause the old bulk caller to clear the worn stack even though no bank addition occurred. The new path never uses that prefix-count/clear pattern.

## Index, persistence and duplicate pitfalls

- Current bank state is compact bankTabs[0], max600 unique IDs. `sendBank` publishes a600-slot snapshot and authoritative occupied span varp8971. Full withdrawals clear native actor48447 before the client sends its click, then server compaction may shift later rows. Existing changedBankSlots protection must survive all new modes.
- Note-mode capacity differs from base-item capacity. With one free backpack slot, a50-unit nonstackable base can withdraw50 notes, not1. The native actor prediction must use this same quantity. `quantity()` and normal bank transfer logging must count the output note identity.
- Search is a display projection for the verified path, not a storage reorder. Sorted/filtered views beyond that path still need their own proof.
- Bank preferences (lastX, default quantity, insert, current tab, notes, placeholders) are fields on original Bank, but current native profile persistence serializes only the item arrays for Bank. Transient session behavior should not be described as persistent until wired into the profile.
- Tab and preset UI-only changes will not survive the current save format. Existing strict validation prevents silently saving a multi-tab bank as if it were one tab; bypassing that guard would risk omitting items.
- Root UI should invalidate/cancel the existing server-owned quantity request when changing relevant bank modes. Its current request snapshot includes bankEpoch and bank contents, not the selected notes mode.

## Validation added

Native950BankTransactionsTest covers output-note stack capacity and overflow, exact source bank identity, duplicate exhaustion, staged equipment subset commits, full-bank merging, whole-stack overflow refusal, callback identity replacement and bank mutation. Native950BankCatalogAcceptance now additionally exercises every current-cache reversible note link, note reverse deposits and fallback, output-controller/mode changes, and real equipment-deposit quantities/controller exclusions. These additions are ready for the root build and acceptance run; no new run is claimed here.