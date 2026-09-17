# World-map input comparison: 947 to 950

2026-09-10. Read-only comparison of the paired logical caches at `Desktop/rs3cache/cache` (947) and `950RevTest/cache` (950). No cache, client, server or input behavior was changed during this audit.

## Live observation and limits

The 950 map opened and displayed the player's correct position. An automated `sky.drag` attempt did not visibly pan it in the subsequent snapshot. **Manual drag confirmation is still pending.** Closing the map restored the game world in the live client. The automated observation alone does not establish a client bug: event timing, hit position or runtime input-layer state remain possible explanations.

## Component comparison

- Interface 1421: one component, byte-for-byte identical between caches.
- Interface 1422: all 126 components byte-for-byte identical.
- Root interface 1477: components 0 through 818 are byte-for-byte identical. Changes start with an insertion at 819; the map hosts 27, 28, 30, 31 and 37 are unchanged.
- All 127 components of 1421/1422 and those five root hosts were fully consumed by the IF3 parser. Format 11 uses 25 hook slots and five trigger lists; the parser's original 22-hook/eight-list labels must not be used to interpret trigger placement. Full consumption and raw equality establish that this comparison is not merely a header check.

| Component | Relevant decoded state |
|---|---|
| 1477:27 | Root container, fills parent, not hidden, no click-through blocker |
| 1477:28 | Child of 27; initially hidden; onLoad script 8409 with slot 1000 |
| 1477:30 | Child of 28; proportional dimensions 16384/16384; varp-transmit script 7996, trigger 1113 |
| 1477:31 | Child of 30; fills parent; varp-transmit script 1094, trigger 1114 |
| 1477:37 | Child of 30; fills parent; no cached hooks |
| 1421:0 | Format 4, type 0, content type 1400; y=32, height inset=32, fills available width/height; not hidden; no cached drag/mouse hooks |
| 1422:0 | Format 11 root container; fills parent; onLoad script 1369 with its unchanged component arguments |

The map adapter closes the scene attachment at 1477:30, then opens 1421 on 1477:31 and 1422 on 1477:37. Both map hosts are static children of 30. On close, it runs script 1898, retires both map attachments and restores scene interface 1482 at 30.

The map surface's content type 1400 indicates native map handling rather than a cached onDrag listener. Zero cached drag thresholds or absent cached hooks therefore do not by themselves show that dragging was omitted.

## Script topology and comparison

Interface 1422:0 loads script 1369, which calls 343. Script 343 calls 10420 with 1422:0 and root host 1477:37. Script 10420 installs the wheel callback to 1370 and calls 14185 for additional gesture setup; 14185 checks 14177 and installs callbacks to 14204. Scripts 1370 and 14204 lead into 14205.

The earlier [UI script evidence](ui-scripts-950-evidence.md) established identical normalized programs and full metadata for 1369, 343, 10420, 1898 and 8105. This audit additionally decoded and compared:

| Script | Instructions per revision | Comparison |
|---|---:|---|
| 14185 | 27 | Identical normalized program and metadata |
| 14177 | 12 | Identical normalized program and metadata |
| 1370 | 5 | Identical normalized program and metadata |
| 14204 | 7 | Identical normalized program and metadata |
| 14205 | 326 | Identical normalized program and metadata |
| 8409 | 4 | Identical normalized program and metadata |
| 7996 | 2 | Identical normalized program and metadata |
| 1094 | 2 | Identical normalized program and metadata |

Comparison used the opcode correspondence in `ui-scripts-950-evidence.json` and `tools/verify_950_ui_scripts.py`, checking all normalized instructions/operands and complete script metadata. This establishes unchanged script programs under that correspondence; it does not prove unchanged native opcode implementations or runtime widget state.

## Conclusion and next check

No changed static map topology, cached input flags or compared hook program was found that explains the automated drag result. Preserve the current topology while obtaining a deliberate manual drag result. If manual dragging also fails, inspect runtime overlap/input dispatch and the native content-type-1400 handler before changing host IDs or adding guessed server callbacks.

## User confirmation

The user subsequently confirmed: "map dragging works." The automated drag observation is a tool reproduction limitation, not an established client defect.
