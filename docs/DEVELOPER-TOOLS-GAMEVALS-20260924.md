# Owned object cleanup and Gameval lookup - 24 September 2026

## Changes

- `;;clearobjects [radius]` / `;;clearobjs [radius]`: clear your recorded object placements within a square tile radius on your current plane. Default 16; allowed 0-128; zero means your current tile. Includes your saved placements and writes their deletion atomically before world removal. Objects are selected by placement origin, not footprint overlap. Map objects, other players' placements and NPCs are protected.
- `;;obj` now records new placements in the same owner ledger as the Developer Console. They appear under Spawns, support the existing save/delete controls, and remain temporary until explicitly saved. The existing 200-placement limit also applies.
- Clear Objects is available through the existing searchable command UI with a radius parameter and confirmation. Successful clearing invalidates that owner's undo/redo history and stale selection/placement cursor; it cannot resurrect deleted records.
- `;;gameval [query]` opens a read-only Cache / Gamevals browser. Also accessible from the unselected Tools/actions details panel. Search by partial symbolic name, type words, exact numeric ID, interface:component or packed component hash. Selection shows full identity, source/target revision, evidence and current payload/reference SHA verification. Print Full Identity sends the exact unwrapped name to chat.

## Deliberately bounded Gameval adoption

This implements the first research-tool step from DARKAN-GAMEVAL-INVESTIGATION.md, not a server-wide symbol migration or content port. There are 1,385 entries: original names from OpenRS2 2670 (949), restricted to six identical interface groups and six specifically audited variable files. The variable entries explicitly state the weaker payload-match evidence; equal payloads alone do not establish semantics. Changed frame 1477 and unsupported clientscript names are excluded.

The lookup cannot execute scripts, set variables or place entities. It has no fallback to guessed IDs. Both definition and reference-table hashes must match to display a current verified payload. Native console scripts/startup/input handling remain unchanged.

Generated resource: `Ataraxia950/resources/native950/gameval-lookup-950.tsv`.
Rebuild: `python tools/build_gameval_lookup_950.py --inputs temp/gameval-research --cache cache --output Ataraxia950/resources/native950/gameval-lookup-950.tsv`.
Inputs and provenance are described in the investigation report. Production cache-reader verification remains required after regeneration; do not blindly repin modified caches.

## Verification

AUTOMATED VERIFIED:
- Full suite: 1,563 tests, zero failures/errors, two existing skips.
- Ownership/radius/plane isolation, saved persistence and failed-write no-removal checks.
- Real-cache isolated world: command tracking, scene removal, persistent removal, second owner protection, stale record/replacement identity safety; existing placement/rotation/save/undo/redo paths preserved.
- All 1,385 symbol pins match the installed paired cache, including reference-table hashes.
- Lookup case/partial/type/ID/hash searches, missing-symbol exclusions, duplicate rejection, mismatched payload rejection, native readiness and closed-window lifecycle tests.
- Three generator tests: deterministic subset, changed source rejection, changed target rejection.

- Installer CheckOnly and five disposable installer fixtures PASS. Staged-jar full native cache preflight PASS.

LIVE TEST PENDING: new browser layout/buttons and object removal as seen by Vulkan. Earlier console startup/Heal/NPC placement acceptance is preserved, not re-certified by these tests.

## Operational limits

Objects created by old `;;obj` builds had no ownership record. This command deliberately cannot infer ownership or erase arbitrary scene objects. Existing console placements with ledger records are supported. Saved deletions are persistent; clearing is not an undoable placement operation. Temporary placements are still cleaned up on logout.

No cache edits, character-save edits, server restart or deployment are needed during development. The successor is staged through Apply Staged Update.cmd; stop the server and close clients before applying it yourself.

## One live checklist

1. Place an object with `;;dev` or `;;obj`, then run `;;clearobjects 0` on its origin / `;;clearobjects 16` nearby. It disappears, walking/collision updates, and other owners/map objects remain.
2. Save one placement, clear it, and confirm it does not return on the next normal restart.
3. Open `;;gameval skybox_tab`, select a row, search `623:27` and `11035`, print an identity, and return to Tools. Check the new panel for clipping.
4. Quick regression: fresh `;;dev`, Heal, NPC placement, close console, then ability keybind.

Staged candidate: `dist/developer-tools-gamevals-20260924`, 25 pinned files. Only the engine jar changes relative to the previous successor; cache/patch files are carried forward unchanged. Source checkpoint `f8e4d4f`. Running jar matches the pre-edit backup.
