# Stage B Phase 2 — native interface component decoder (2026-09-26)

Status: **implemented, full test suite green, NOT committed** (awaiting owner approval).
Base: Shnorkscape `1d8ca70`. Artaven source (READ-ONLY):
`C:\Games\UpdatedAuthorFiles\950OpenSource-2026-09-25\950OpenSource`.

## 1. What was wrong

`Ataraxia950/game/com/rs/cache/loaders/IComponentDefinitions.java` decoded formats -1/3/4/5 exactly
but not the modern formats 6/9/11. Sweep of every index-3 record of the local 950 cache
(104,285 records), strict `InputStream`, swallowed exceptions rethrown in a probe copy:

| decoder | exact | threw | trailing bytes |
|---|---|---|---|
| Shnorkscape before | 89,861 | 14,415 (13.8%) | 9 |
| Artaven / Shnorkscape after | 104,285 | 0 | 0 |

All failures were format 6/9/11. On records that did not throw, 31,972 still decoded with wrong fields
(misaligned stream): garbage `opBase`, missing `onLoadHook` on 7,747 components, lost
varp/inv/stat triggers, bogus `graphicId`, wrong `activeProperties.settings`. 42 of its 24,711 hooks
named non-existent scripts. The failure was invisible: `decode()` swallowed exceptions, `create()`
returned `null` for bad hooks, and `getInterface()` refused any non-`-1` format with `"if1"`.

Root causes (all confirmed): 24-bit option-mask read for format ≥ 6; missing modern type-block tail
bytes; missing aspect-mode-4 header bytes; missing 3/4 extra hooks before the trigger lists;
no support for component types 10/11/12/13/15/16.

## 2. Exact-950 evidence

Client `OpenNXT/data/clients/950/win64/original/rs2client.exe`
(SHA-256 `fc7492548627a4068e88a7a45ba4f0be93004adf246bb4b174b24ae588768b36`), disassembled with
`tools/dis950.py`:

- `0x14034037c` — aspect width/height type 4 reads two extra u16 (the 4-byte `aspectRatioExtension`).
- `0x14034044f` — format ≥ 0 no-click-through byte (Shnorkscape was already right).
- `0x1403404ce` — virtual per-type block read.
- `0x1403404d1` — format ≥ 6 reads a 4-byte int (low u16 kept); `0x140340540` — format ≥ 9 one more byte.
- `0x14034057f` — option mask is **4 bytes when format ≥ 6**, `0x1403405be` — 3 bytes otherwise.

Cache evidence (new `Native950ComponentDecoderCacheTest`, SHA-256 pinned fixtures):

- all 104,285 records consume exactly; 46,272 hooks (incl. 169 extra hooks) all name existing
  index-12 scripts;
- native widget counts: type 11 = 54, 12 = 16, 13 = 8, 15 = 57, 16 = 8;
- 10 components set option-mask bits above bit 23 (e.g. `1438:18`, `590:7` = `0x01000000`);
- `1311:362` (Customisations preview drag layer) now yields `onLoadHook = [4213, …]`, matching the
  CS4213 already recorded in `docs/DEV-UI-NATIVE-CAPABILITIES-950.md`; option mask `0xC0000`.
  **Correction:** an earlier Shnorkscape research reading of this component's option mask as 0 used
  the same wrong 24-bit layout and is superseded.
- `753:40` previously threw; now `onMouseRepeatHook[0] = 1165`.
- Developer Console shell components `1448:8`/`1448:24` are format 4 and decode identically before
  and after.

## 3. Classification of Artaven's differences

| change | class | adopted |
|---|---|---|
| 32-bit option mask for format ≥ 6 | SAFE TO ADOPT (disassembly + sweep) | yes |
| aspect-mode-4 4-byte extension | SAFE TO ADOPT (disassembly + sweep) | yes |
| extra hooks (3 in format 6, 4 in 9/11) before trigger lists | SAFE TO ADOPT (all 169 name real scripts; exact consumption) | yes |
| trailing-byte check, `decodeIncomplete`, `decodeFailureReason`, counters | SAFE TO ADOPT (turns silent corruption into explicit state) | yes |
| `create()` / trigger-list errors propagate instead of returning `null` | SAFE TO ADOPT | yes |
| `getInterface()`: no `"if1"` refusal, lazy allocation, bounds check, per-component catch, publish after build | SAFE TO ADOPT (zero runtime callers) | yes |
| remove per-component `"Name:"` info log | SAFE TO ADOPT | yes |
| `modernFormatExtension` lengths (format 6: 12/8/4; 9/11: 9/5) | USEFUL BUT NEEDS VALIDATION — boundaries exact on every record, meaning opaque (unparsed tail of the client's per-type virtual read) | yes, kept opaque |
| type 10 38-byte prefix; types 11/12/13/15/16 blocks, label font/flag/text, type-16 counted arrays | USEFUL BUT NEEDS VALIDATION — boundaries exact; only label text/font and arrays are named | yes, kept opaque |
| comments pointing at Artaven-only `tools/verify_950_components.py`, `reports/r3-decoder3-…`, `ORBS_AND_VARS` | IRRELEVANT here (artefacts absent in Shnorkscape) | rewritten to Shnorkscape evidence |
| `Native950ComponentDecoderAcceptance` main + `resources/native950/decoder-950.properties` | USEFUL — ported as a JUnit test (pins inlined) instead of a main | ported |

Nothing CONFLICTS with Shnorkscape: no gameplay or server code calls the decoder.

## 4. Consumers

- `Ataraxia950/game/com/rs/tools/InterfaceFullDumper.java` — now uses a strict stream and reports
  `decode incomplete: <reason>` instead of counting a partial record as decoded; stale `"if1"`
  comment replaced.
- `Ataraxia950/game/com/rs/tools/Native950ChromeProbe.java`, `tools/Native950AbilityProbe.java`,
  `tools/ability-pass/InspectBooks.java` — unchanged; reflection filters on `Object[]` so the new
  `Object[][] additionalHooks` is ignored. Their output now has correct hooks for format 6/9/11.
- `tools/research_native_ui_950.py` parses only common headers; unaffected.
- Server runtime / live client: no use; no deploy needed.

## 5. Tests

- `tests/modern947/IComponentNativeWidgetDecodeTest.java` — Artaven's cacheless test, unchanged (7 tests).
- `tests/modern947/Native950ComponentDecoderCacheTest.java` — new, 5 tests over the real cache;
  skipped when the untracked `../cache` is absent.
- Full `Build-Ataraxia950.ps1 -Tasks test`: 185 classes, 1,611 tests, 0 failures, 2 pre-existing skips.

Not started: opcode 124 / component-value actions (Phase 3+).
