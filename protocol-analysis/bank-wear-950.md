# Bank withdrawals and partyhat equipment, revision 950

The user accepted Use, ordinary Drop/pickup, and tinderbox-on-logs. The follow-up issues were independent of item-option decoding.

## Bank withdrawals

Live input showed every rejected single-item row reporting actor48447, while partial stacks of logs and partyhats reported their real IDs and worked. The previous adapter incorrectly expected the next bank row's ID when exhausting a non-final row.

The corrected filter requires the actual950 cleared-actor value only when the authoritative source is valid, has not changed in this input batch, and the movable amount exhausts its positive quantity. Partial and capacity-blocked requests retain the real ID. The original bank handler receives the authoritative item ID; inventory capacity, exact source validation, stale-slot rejection and quantity prompt ownership remain enforced. Server-side compaction is distinct from the client actor held by the button sender. Native/cache evidence and limits are in bank-withdrawal-claims-950.md.

## Partyhats

Green partyhat1044 already supplied Wear at native operation2, but had no admitted equipment capability. Native950CosmeticEquipment validates the six ordinary partyhat definitions (1038,1040,1042,1044,1046,1048), head slot, ordinary menus, absence of equipment effects/transforms, and male/female models187/363 against the paired950 cache. Catalog resolution now admits that family into existing Wear/Remove handlers, equipment container94 and save validation.

The live GlobalPlayerUpdater already encodes current-cache equipment and does not need the old bronze-only sample appearance template extended. Partyhats keep hair/beard visible. The verified cosmetic family contributes zero melee bonuses; unrelated items still need their existing equipment/bonus verification. The startup cache preflight includes these assets.

## Validation and installation

The bank probe has passed30 encrypted actions,35 engine ticks and529 parsed frames, including reported herb215, first/middle/final single-item withdrawal, partial1/5, All, stock caps, full inventory, limited space, duplicates, and Withdraw-X. Its setup uses the original BodyDefinitions initialization needed by Bank.unlockButtons, as the real appearance bootstrap does.

Installed and running: 1087 tests passed, two existing skips, 18 launcher checks. Six-partyhat equipment acceptance passed175 frames, existing melee449 ticks/1531 frames, inventory117 frames, and startup cache preflight. Exact build/runtime details are in validation-bank-wear-2026-09-12.json and HANDOFF-950.md. Visible user checks remain pending. Tests use ephemeral characters and RAM-only equipment snapshots; no user inventory, bank contents, levels or save data are manually changed. Backups are under implementation-backup/2026-09-12-bank-wear.