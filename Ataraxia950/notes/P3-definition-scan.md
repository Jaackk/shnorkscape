# P3: definition loaders validated against the 947 cache

Written September 6, 2026 for pillar P3 of `MIGRATION-BACKLOG.md`. Everything below was produced by the checked-in Java probe `game/com/rs/tools/modern/DefinitionScanProbe.java` running against `C:\Users\developer\Desktop\rs3cache\cache` (read-only flat store, `Cache.initFlatReadOnly`). It replaces the out-of-tree Python figures quoted in the backlog. Run it with:

```
java -Xmx2g -cp "<out>;<runtime classpath>" com.rs.tools.modern.DefinitionScanProbe C:\Users\developer\Desktop\rs3cache\cache
```

The probe decodes every file of a family through the strict decoder (`com.rs.network.io.InputStream(data, true)`: EOF throws instead of returning zeroes), rejects unknown opcodes, and rejects trailing bytes after the terminator. A file only counts as `ok` when its opcode stream terminates exactly at the last byte. Exit code is 1 unless items ok >= total-5 and objects ok == total.

## Final scan (run 3)

| family | index | total | ok | failed | unknown opcodes (files) | other failures |
|---|---|---|---|---|---|---|
| items | 19 | 60,617 | **60,616** | 1 | 167 = 1 (item 29492, Bond) | none |
| NPCs | 18 | 32,687 | **32,183** | 504 | 184 = 191, 185 = 28, 186 = 196, 253 = 89 | none |
| animations | 20 | 37,853 | **26,098** | 11,755 | 119 = 6,788, 120 = 1,617, 25 = 2,785, 27 = 557, 112 = 8 | none |
| render anims (BAS) | 2/32 | 4,907 | **4,907** | 0 | none | none |
| objects | 16 | 137,079 | **137,079** | 0 | none | none |

Gate: `GATE items 60616/60617 objects 137079/137079 => PASS`. Wall time for the whole scan is under one second per family (items 264 ms, NPCs 134 ms, animations 199 ms, BAS 9 ms, objects 160 ms).

Comparison with the backlog's Python figures: items match (16,211 as-is, 60,616 after the four opcodes); NPCs match exactly (32,183, same four opcodes, same counts); objects match; animations improved from 23,491 to 26,098 and BAS from 4,511 to 4,907 because two 910-table widths turned out to be wrong for 947 (below). The Python scan's BAS candidates 10/131/133/136 (string), 20/135/137 (u8), 128 (u24) were artefacts and were NOT added.

## Anchor values (flat path through the production loaders)

| id | name | inventory options | equipSlot | equipType | stackable | value (op 181) | op 69 |
|---|---|---|---|---|---|---|---|
| 995 | Coins | Add to pouch, Add X to pouch, -, -, drop | -1 | -1 | 1 | (absent, value stays 1) | 0 |
| 1511 | Logs | Craft, Light, -, -, drop | -1 | -1 | 0 | 4 | 25000 |
| 315 | Shrimps | Eat, -, -, -, drop | -1 | -1 | 0 | 5 | 10000 |
| 1277 | Bronze sword | -, Wield, -, -, drop | 3 | -1 | 0 | 168 | 0 |
| 1173 | Bronze square shield | -, Wield, -, -, drop | 5 | -1 | 0 | 168 | 0 |
| 1139 | Bronze med helm | -, Wear, -, -, drop | 0 | 8 | 0 | 168 | 0 |
| 4151 | Abyssal whip | -, Wield, -, -, drop | 3 | -1 | 0 | 120001 | 10 |
| 11694 | Armadyl godsword | -, Wield, Remove-hilt, -, drop | 3 | 5 | 0 | 1250000 | 10 |

NPCs: 494 `Banker` options `[Bank, -, Talk to, Collect, Load Last Preset from]` combat 0 size 1 renderEmote 3180; 0 `Hans` `[Talk to, -, Buy veteran capes, -, -]` renderEmote 1426; 1 `Man` `[Talk to, Attack, Pickpocket, -, -]` combat 4 renderEmote 4. All eight items and three NPCs decoded with `decodeFailure == null`; the failure registries were empty after the anchor reads.

Names, slots and Wear/Wield options agree with the `Native947CacheContent.kt` pins for 995/1511/315/1277/1173/1139. One difference for the integrator: the pins spell the default fifth option `Drop`; Ataraxia's `ItemDefinitions.setDefaultOptions()` uses lowercase `drop` (910 behaviour, untouched). Anything comparing option text case-sensitively should normalise.

## Item opcodes confirmed (index 19)

Raw sequences dumped by the probe (`=== RAW ITEM FILES ===`):

* 995 Coins (128 bytes): `1@0 7@3 4@6 6@9 5@12 100..108 35@60 36@74 11@90 2@91 249@98 144@124` -> no 178/181/69/3 at all; opcode 12 (32-bit value) is also absent, as it is in every 947 item.
* 1277 Bronze sword (273 bytes): `... 36@42 181@49 178@58 65@59 2@60 97@74 121@77 249@80 144@266 151@269`. Bytes at 49: `b5 00000000000000a8` = 181 + int64 168, immediately followed by `b2` (178, no payload) and `41` (65). Any other width for 181 or a payload for 178 would put the name opcode `02` off by one and fail the trailing check.
* 1733 Needle: `... 65@46 69@47 2@52 ...`, bytes `45 00001388` = 69 + int32 5000, then `02 Needle`.
* 51196 Prime Gaming knowledge bomb: `... 2@51 3@80 249@159 144@193`, opcode 3 = NUL-terminated string `Grants +50% XP for 1 hour! Cannot be used while Double XP effects are active.`
* 29492 Bond: `181@0 (int64 15,000,000) ... 168@52 165@53 65@54 69@55 167@60` -> the single failure. After `a7` (167) the bytes are `02 "Bond" 8b 7335 f9 ... 90 002d 00`, i.e. a payload-less 167 would decode exactly (139 = bind id 29493, params, 144, terminator). One sample is not confirmation; 167 stays unknown and the item is served as `Undecodable Item (29492)` with `getDecodeFailure(29492)` set.

| opcode | width | files | meaning | field |
|---|---|---|---|---|
| 181 | int64 | 29,610 | **Item value.** Replaces the 32-bit opcode 12 (never present in 947). Whip 120,001, AGS 1,250,000, Bond 15,000,000, cannonball 5 all match RS3 values. | `value64`; `value` receives the int-clamped copy |
| 3 | string | 130 | **Examine/description text** (all carriers are ids >= 51196). | `description` |
| 69 | int32 | 4,648 | Not established. Values look like Grand Exchange buy limits (Logs 25,000, Shrimps 10,000, Needle 5,000, Whip 10, AGS 10, Bond 150); treat as a hypothesis. | `unknownInt69` |
| 178 | flag | 36,133 | Not established (present on most items, absent on coins). | `flag178` |
| 167 | ? | 1 | Unknown; probably a flag (one sample). | fails closed |

## NPC opcodes (index 18)

The strict loader rejects 184/185/186/253 as instructed; no widths were applied. Evidence gathered by a scratch width tester (splice the opcode plus W bytes out of each failing file, re-decode, require exact consumption; counts are consistent/inconsistent/blocked-by-another-unknown):

* 184 (191 files): u8 = 191/0/0, every other fixed width inconsistent. Example NPC 233 tail `b8 05 00`. Strong candidate: u8.
* 253 (89 files): u8 = 87/0/2, u32 = 77/0/12 (ambiguous between the two, both leave the file consistent in most cases).
* 185 (28 files): no payload = 26/0/2 (flag candidate).
* 186 (196 files): no single fixed width or string is consistent; compound payload. Example NPC 50 (King Black Dragon) bytes after `ba`: `00 29 de ...`.

Also worth knowing: the 910 `NPCDefinitions(int id)` constructor never stores `id` (private field, `getId()` returns 0 on the legacy path). The 947 paths (`decodeStrict947`, the flat branch of `getNPCDefinitions`, failed definitions) set it; the legacy path was left as it is to keep 910 behaviour byte-identical (`hasAttackOption()` compares `id == 14899`).

## Animation opcodes (index 20)

Change applied (flat path only): opcodes 6 and 7 (right/left hand item) are a plain u16 in 947 with 0xFFFF meaning none. The 910 table reads a bigSmart, so every 0xFFFF became a 4-byte read; the probe showed 4,796 failures whose trace had a 6/7 followed by a high byte, plus a spurious "opcode 255" bucket of 2,148 files (`06 ffff 07 ffff` misread) and 217 trailing/114 truncated/30 NPE failures. With u16 all of those vanish, the unknown histogram collapses to the five opcodes below and no file fails for any other reason. Example: anim 37 `06 ffff 07 092b 0f 01 000b ...` decodes to 11 frames with 11 durations, 11 low halves and 11 high halves (`36ac ...`), terminating exactly.

Not applied (as instructed, failure registry + clean abort): 119, 120, 25, 27 and the newly visible 112. Width-tester evidence for the follow-up (M8/M9):

* 119 (6,788 files): u24 = 1,531/0/5,257, i.e. never inconsistent; the 5,257 remaining files also contain 120. Tail of anim 63: `77 000096 77 000496 78 000000f0010e 78 000400f0010e 00` reads as two 119 entries of 3 bytes and two 120 entries of 6 bytes.
* 120 (1,617 files): 6 bytes = 1,617/0/0.
* 25 (2,785 files): 7 bytes = 1,197/0/1,588 but the tail `19 00ae 1a 000000b3 00` also reads as 25 = u16 followed by an opcode 26 = u32; ambiguous.
* 27 (557 files): nothing consistent on its own (co-occurs with 24/25); tail `1b fe 19 0a97 1a 000000 1e 00`.
* 112 (8 files): not examined.

The strict path also rejects opcodes 19/20 when no opcode-13 sound table precedes them (the 910 code would NPE); this did not occur in the final run.

## BAS / render animations (index 2, archive 32)

Change applied (flat path only): opcode 52 entries are `(bigSmart animation, 2 bytes)` in 947, not `(bigSmart, u8 duration)`. Evidence: all 399 files carrying opcode 52 decode to exact consumption with the 2-byte field and 278 of them failed with the 1-byte read; the second byte is 0 in all 856 entries (so `loopAnimDurations` keeps the first byte, `loopAnimDurationsLow` keeps the second). The stray second/first bytes of the misread entries are exactly the values the assessment took for new opcodes: durations 10 (0x0a) and 20 (0x14) followed by `00` looked like "opcode 10/20 = string/u8", and 0x80/0x83/0x85/0x87/0x88/0x89 became "128/131/133/135/136/137". After the fix none of those bytes are ever reached as opcodes, so none were added; the assessment's requirement of 20 confirming samples was not met by any of them (they occurred in 9-19 files before the fix and in 0 after).

## Behaviour of the loaders now

* `ItemDefinitions`: when `Cache.isFlatReadOnly()`, `loadItemDefinitions` decodes through the strict stream, handles 178/181/69/3, throws on unknown opcodes and trailing bytes, and on failure resets every decoded field, names the item `Undecodable Item (id)`, sets `decodeFailure`, leaves `loaded == false` and records the id in `ItemDefinitions.getDecodeFailures()` / `getDecodeFailure(id)`. The 910 path (filestore stream, unknown opcodes ignored) is unchanged; the opcode table is shared through a private reader interface so both paths run the same code. `decodeStrict947(id, bytes, trace)` is the raw entry point used by the probe and tests.
* `NPCDefinitions`, `AnimationDefinitions`, `RenderAnimDefinitions`: same pattern (`decodeStrict947`, `decodeFailure`, `getDecodeFailures()`); a failed 947 definition is a fresh defaults-only object (name `Undecodable NPC (id)` for NPCs) so no misaligned field survives. `AnimationDefinitions.getAnimationDefinitions` returns that object instead of `null` on the flat path (legacy still returns `null` on error).
* Missing files (id gaps) are not decode failures and are not registered; they keep their 910 fallbacks (`Missing Item (id)`, `Missing NPC (id)`, default animation).

## Tests

`tests/cache/DefinitionDecoderTest.java` (9 tests, no cache): item blob with 178/181/69/3 decodes to the expected fields with the exact `opcode@offset` trace; int64 clamp; unknown opcode 167 rejected; trailing byte rejected; truncated int64 rejected; the legacy path tolerates the same blob as before (name read, everything after the first 947 opcode ignored, no exception); NPC known opcodes decode and 184/185/186/253 plus trailing bytes are rejected; animation u16 hand items decode and 119/25/255/120/27 are rejected; BAS opcode 52 two-byte entries decode and 10/20/128/131/133/135/136/137/200 are rejected.
