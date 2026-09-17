# Bounded 950 melee admission for the existing bronze kit

The general identity table correctly reports two renamed items, so basic melee previously rejected the banker equipment slice even though wearing these items is supported. This adapter adds an explicit exception only inside the basic melee loadout. It does not mark other items, all armour, or either global identity row safe.

## Actual paired 950 item definitions

Read raw index 19, group `id >>> 8`, file `id & 255`. The complete decoded records consume every byte through the terminator.

| ID | Raw address | Bytes | Native name | Equipment slot | SHA-256 |
| --- | --- | --- | --- | --- | --- |
| 1173 | 19/4/149 | 320 | Bronze square shield | 5 (shield) | `97af6e94f54e0289f9b4c6ee7d6ad0430ff34025df9215bf87f383fe3da9f184` |
| 1139 | 19/4/115 | 306 | Bronze med helm | 0 (head) | `df1f9d31a32f0dac7ba94adccf8d0e01c9515c1681a351c41877a879562ca3a0` |

1173 has shield parameter 2832 = 1. Neither definition is a melee, ranged or magic weapon (parameters 2825, 2826 and 2827). The helper checks those flags, exact item ID/name and slot in addition to the complete raw SHA. It decodes the raw records directly instead of relying on the global item-definition memoization.

The existing identity classification compares 910 `Bronze sq shield` with 950 `Bronze square shield`, and 910 `Bronze helm` with 950 `Bronze med helm`. These are reviewed exceptions, not a general rule that renamed items retain their function.

## Explicit classic gameplay data

The current `Ataraxia950/data/rs2_combat/items_2009scape.json` has these exact 15-value source rows:

- `Bronze sq shield`: `[0,0,0,-6,-2,5,6,4,0,5,0,0,0,0,0]`.
- `Bronze med helm`: `[0,0,0,-3,-1,3,4,2,-1,3,0,0,0,0,0]`.

`classicName(1173, "Bronze square shield")` selects the first row. There is no `Bronze square shield` row. Helm already uses the classic table name. The alias requires the exact ID and native name. `hasExpectedClassicBonuses` checks every retained `ClassicBonuses` field against these rows; source array index 10 is the unused summoning defence value (zero here). This is intentionally RS2 gameplay policy, not a conversion of modern cache tier stats.

`verifyCache()` is a cache-only preflight and never loads `RS2BonusDatabase` or resolves `DataPaths`. `isVerifiedKitItem(id)` admits only these two IDs after that verification. Combat must still resolve the actual classic row and call `hasExpectedClassicBonuses` separately. Missing, changed, or mis-slotted data remains refused.

## Checks and limits

`Native950MeleeEquipmentTest` supplies both literal raw cache files without a cache or data root. It checks decoded slots/flags, changed bytes, wrong IDs/names/slots, the restricted name alias, and missing/wrong/modified bonus rows. Parent integration runs the shared suite and the actual-cache banker-kit melee acceptance. This local task did not run a build or restart the server.

A definition pin establishes the reviewed metadata, not equality of rendered meshes or textures with revision 910. Existing equipment appearance support and the live kit check provide the separate rendering coverage.
