# 947 to950 packet mapping — bounded pass

All results are static review artifacts. Active protocol maps, binaries and servers were not changed by this mapping pass. No live950 lobby success is claimed.

Extracted all223 server and129 client descriptor lengths. Recovered221 native950 server handler bindings. Registration-order alignment gives208 server opcode candidates.71 of these independently agree with strong normalized handler-shape matches (73 strong shape candidates total). Five client opcodes have unique exact normalized sender-shape candidates.

Native code-address relocations were normalized; direct call targets and RIP-relative data targets were abstracted. Consequently shape agreement does not establish called helper semantics, byte transformations, or packet field order. All payload layouts remain unverified. Equal wire lengths or long matching registration sequences alone do not prove packet meaning.

## Minimum lobby/world candidates

|Name inherited from947|947|950 candidate|Evidence|
|---|---:|---:|---|
|IF_OPENSUB|8|100|Registration-order candidate; payload review required|
|PLAYER_INFO|27|36|Order + strong handler shape|
|REBUILD_NORMAL|90|63|Registration-order candidate; payload review required|
|IF_OPENTOP|94|1|Registration-order candidate; payload review required|
|IF_SETHIDE|103|67|Order suggests67; shape-only ranking misleadingly prefers85|
|RUNCLIENTSCRIPT|121|35|Registration-order candidate; payload review required|
|WORLDLIST_FETCH_REPLY|159|129|Order + strong handler shape|
|SERVER_TICK_END|195|160|Order + strong handler shape|
|NO_TIMEOUT|216|183|Registration-order candidate; payload review required|

## Files

- server-map-review.csv: compact947→950 comparison, with independent agreement and payload-verification columns.
- server-candidates.json: ranked handler-shape candidates and independent registration-order candidates.
- client-candidates.json: exact sender-shape candidates, still review-only.
- 950-server-sizes.toml /950-client-sizes.toml: actual950 descriptor lengths; never combine with inherited947 names in a live server.
- 947-native.json /950-native.json: constructor sites, descriptors, vtable bindings, parser targets and normalized code for follow-up without repeating extraction.
- lobby-parser-comparison.txt: unnormalized disassembly of candidate lobby/world handlers, for the next field-layout review.

## Next smallest implementation step

Validate IF_OPENTOP94→1, IF_OPENSUB8→100 and RUNCLIENTSCRIPT121→35 field order/transforms against the saved native parsers, then update only those950 codecs and test the lobby. Resolve IF_SETHIDE103→67 separately. NO_TIMEOUT216→183 is an order-based candidate; its leaf handler did not produce a fingerprint. Do not install the full208-entry draft automatically.

## Reproduce

Run tools/map_950_protocol.py followed by tools/summarize_950_mapping.py with Python and pefile/capstone installed. The existing AstraNXT .venv was read-only for this pass; no dependencies were installed. The original947 executable is only read as a reference.
