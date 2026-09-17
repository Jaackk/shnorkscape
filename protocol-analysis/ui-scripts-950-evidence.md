# Settings and World Map: 947 to 950 script evidence

Investigated on 2026-09-10. `tools/verify_950_ui_scripts.py` reads both caches and
writes only a requested evidence report. The corresponding JSON is
`protocol-analysis/ui-scripts-950-evidence.json`.

## Result

All 76 selected Settings scripts and all five pinned World Map scripts decode with
matching argument signatures across the two revisions. Of these, 68 Settings
programs and all five World Map programs are instruction-for-instruction equivalent
after opcode normalization. Eight Settings programs changed as described below.

The selected set includes every literal script pin and every direct literal script
call in `Native950Settings.java` and `Native950WorldMap.java`. This is not a full
transitive dependency proof. Native opcode internals, scripts called indirectly,
and cache-defined data can still change even when a caller's program is identical.
Live rendering and input checks remain necessary.

## Method and coverage

The reader uses the established 947 operand widths and independently checks the
instruction count, exact code boundary and full metadata/switch-table bytes. It
pairs scripts by cache ID, compares every literal operand, and derives opcode
correspondences only when the entire operand stream and metadata agree. One
structural difference matters: packed varbit ID/flag operands widen from three
bytes in 947 to four bytes in 950. Their numeric values are compared, not their
raw widths. No existing packet encoder supplies the expected results.

The run found 19,398 matching operand/metadata seed pairs. It derived 1,163
one-to-one opcode correspondences, including one manually corroborated native
operation. Each correspondence's support count is retained in JSON. Low-support
operations used by the selected scripts are explicitly listed per script.

Coverage is 20,477 of 20,577 scripts on 947 and 20,918 of 21,098 on 950. Unsupported
scripts remain refused; the tool does not assign an arbitrary one-byte width to
unknown 950 operations. The comparison includes branch offsets, all local and
argument counts, and complete switch tables. No selected script changes its integer,
string or long argument count.

## Actual changes

| Script | Change | Consequence for this adapter |
|---|---|---|
| 2595 | One hook target changes from component 742:63 to 742:68. | Use the native 950 graphics script; do not replay 947 instructions or copy its component constants. |
| 2923 | The Search placeholder colour changes from RGB `0x009a9a9a` plus `0` to RGBA `0x9a9a9a00` plus `255`. | Native 950 script already owns the conversion; its server call signature is unchanged. |
| 8181 | Quick-options dispatch gains a call to 16266, moves an existing check using 14726(13), replaces a 8854 check with 8847(18), and adds another var-dependent branch. | Preserve the native callback rather than reconstructing the menu on the server. These branches need live Escape/gear checks; they are not merely recompilation. |
| 8186 | Two arguments to 10495 change from 25/26 to 28/29. | The native menu builder supplies its updated indexes; no changed server call parameters. |
| 8283 | Graphics height changes from 404 to 450. | Matches the page-struct height changes found by the parent investigation. |
| 8284 | A visibility/availability branch adds a 3825 check and another varbit read, ID 61483. | Retain cache-owned selection logic. The adapter must not claim every modern feature is implemented because its page renders. |
| 8288 | Graphics height changes from 404 to 450. | Let the native 950 layout script size the page. |
| 12343 | Removes older conditional corrections from heights 404/202 to 450/225. | Matches the new native dimensions; no changed server call parameters. |

The JSON contains complete normalized diffs for all eight, so branch changes are
not hidden behind a hash refresh.

## Rare-operation checks against both native executables

The 947 and 950 executable paths are the defaults in `tools/dis950.py`.

- `947 0x339 -> 950 0x4e5`: registration binds the handlers at
  `947 0x14017f0a0` and `950 0x140184370`. Both pop two integers, unpack map
  coordinates with shifts/masks 14 and 0x3fff, and walk the same region-bounds
  structures. This corroborates the low-support pair used by script 343.
- `947 0x1f7 -> 950 0x365`: handlers at `947 0x14017e710` and
  `950 0x1401839e0` take the corresponding map state and invoke its reset with
  constant 1. This corroborates the map-close operation in script 1898.
- `947 0x576 -> 950 0x443`: registration at `947 0x140048ed8` versus
  `950 0x1400493bd` binds wrapper/callback pairs
  `947 0x1401df4a0 -> 0x1401cea10` and
  `950 0x1401e69e0 -> 0x1401d5f30`. Both own the operation named
  `cc_if_input_setemptytext`, consume a string and two integers, copy the
  placeholder string, and OR/store its colour. The input widget type discriminator
  moves from 11 to 12. That confirms the operation identity even though all observed
  uses changed colour literals and could not seed an unchanged-program match.

These checks establish selected corresponding roles; they do not treat a universal
structure displacement change as proof of semantics.

## New World Map script hashes

All five have equivalent normalized programs:

| Script | Logical file SHA-256 in 950 |
|---|---|
| 343 | 0721f17e4a3befcf6a466c6012f8be56362c6bc638feb464d767076e70f003ce |
| 1369 | e455b63442c739606d9d301e2f6713785e74ffa073de864a586c54049236af94 |
| 1898 | 34ee0c94d21c345357d6c6db9a1d2ee571a07bc50089ab3988dab35e502713aa |
| 8105 | f1cf5757548ce7a7c19baeda48088f28f3b5fcd3d1eae3933f9acfa00de8fd0b |
| 10420 | 11bf251ddafddb3ffa2eca9f8cf339d6462d76a3e2a22d68f0dfe41d7d5ece3b |

The Settings hashes are all retained in JSON alongside their comparison status.
Re-pin the actual 950 files while retaining enforcement, then verify page navigation,
Audio controls, Escape, map dragging and close in the native client. Do not replace
native 950 layout programs with inferred 947 equivalents.

## Reproduction

From `950RevTest`, with the project's documented Python runtime:

```
python -B tools/verify_950_ui_scripts.py --output protocol-analysis/ui-scripts-950-evidence.json
```

`--dump 2923 2915` prints selected decoded 950 programs using normalized 947 opcode
names. Neither cache nor either client executable is written.
