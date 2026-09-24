# Native Developer Console candidate

`;;dev` and `;;developer` open the same developer-only console. This candidate uses revision-950 management shell **1448**, its native frame/open/close lifecycle, textured Beasts-browser buttons (CS10410/10899) and native text styles (CS2995). It adds six scripts, **21124–21129**, without changing existing scripts or the client executable. The mockup guides the three-column layout; physical Vulkan appearance and input remain **LIVE TEST PENDING**.

## Included

- Categories, global action/description/alias search, paged results, details, typed parameter controls, confirmations and per-account favourites.
- Metadata imported from the existing command directory. Execute returns to the same command dispatcher; no second God/DM/resource implementation. God, Almighty, infinite resources, Revolution, Bug Test and Combat QA show authoritative state.
- Equipment Library and gear/teleport tools through their existing handlers. The library, real bank/presets and workspace are unchanged.
- Cache-derived NPC/object name or exact-ID search: **77,850 concrete definitions**. Details include real footprint, combat level or supported object types. Runtime placement revalidates the actual cache definition.
- Separate native chosen-tile placement, one-use source slots, timeout/cancel, full-footprint checks and compact NPC formations. Object type/rotation controls. Placement never replaces existing scenery.
- **Spawns** lists the account's own editor placements. Inspect, move, duplicate, rotate temporary objects, delete, explicit **Save to World**, and bounded undo/redo of temporary placement batches.
- Temporary placements disappear on logout/cleanup. Explicitly saved placements restore after native world bootstrap and survive logout. Malformed persistence is reported and is not overwritten.

Browse NPCs/objects from the right-hand shortcuts when no action is selected. Search within the chosen browser; **Back to actions** returns to the command directory. Select **Spawns** to manage placed entities.

## Deliberate bounds and remaining acceptance

- Previews are verified metadata/text. No speculative model renderer, ghost actor, green/red footprint overlay or mouse-move spawn loop. The native **Place** targeting verb/source is implemented; cursor visibility and selected-tile packet ordering require Vulkan acceptance.
- Native content area is 742 × 450 with eight results per page, not the mockup's invented full-screen asset layout. No claim of pixel-perfect or live visual success.
- Move/rotate saved records is intentionally disabled: duplicate into a temporary edit, place/rotate it, explicitly save, then explicitly delete the old record. Undo/redo covers temporary placement batches; saving/deleting/moving/rotating clears that history.
- At most 50 NPCs per placement, 200 ledger entries per account, 256 persisted records and 32 undo batches. Existing global diagnostic-NPC limit remains authoritative. Whole formations must remain within 32 tiles of the placement origin. Unsupported/blocked definitions are refused with feedback.
- Low-level raw client-script/interface diagnostics remain documented chat tools instead of unrestricted UI parameter editors. Optional prefabs and click-to-teleport are deferred.
- Christmas-cracker Pull, dragging abilities off unlocked bars, and equipment-driven action-bar switching remain **UNRESOLVED / USER-DEFERRED**. Earlier automated tests do not constitute live passes.

## Persistence and rollback

Account favourites: `server-home/developer-preferences/<account>.txt`. Explicit world placements: `server-home/developer-world-edits-950.tsv`, versioned tab-separated schema `SHNORKSCAPE-WORLD-EDITS-950-1`. Both are atomically replaced, bounded sidecars; neither changes character saves or cache/maps. Edit the world file only while the server is stopped. Saved entries whose locations are blocked remain recorded and are reported rather than overwriting their occupants.

The staged update is cumulative. `Apply Staged Update.cmd --check-only` verifies all 23 hashes and all three launch cache gates without installation. Installation requires clients/server closed. Backup `new-files.json` identifies the six newly appended script files for manual rollback; automatic rollback restores prior files and removes only new files created by that install.

## Exact native input evidence

The native-only selected-component opcode is `0x72c`, registered at `0x1400562be`; handler `0x1401f76f0` resolves hash/slot through `0x140390f60`, cancels the previous selection through `0x14019f620`, then selects through `0x14019f3e0`. Its independent source is dynamic child **1448:11**, with native ground-target mask `64 << 11`. Existing Dive/action-bar sources are not reused. Per-session source slots advance and are consumed once; source hash/item, plane, timeout and distance are checked before mutation.

Native button onOp is normalized `0x6a` / actual `0x83b`, event `0x25`; the callback sends a per-render nonce through the existing STRING_DIALOGUE contract. Target-leave is event `16`, installed by normalized `0x665`; normalized `0x8aa` performs native target cancellation. The exact client executable identity and selection-handler calls are checked by `tools/test_developer_console_950.py`. Script-loop/stack tests and the production JS5 decoder verify the appended archives. These are contract checks, not proof that the physical cursor/button presentation works; that boundary is deliberately left for the live checklist below.

## One live checklist

After applying and launching normally:

1. Open both aliases; inspect native frame/layout, search/categories, parameters and favourites across a relog. Verify God/DM/resource states match chat commands.
2. Open the existing Equipment Library from Items; verify normal banking and keybinds after closing both interfaces.
3. NPC browser: search by name/ID, place one and a small group at chosen tiles, try a large footprint and a blocked tile, then cancel placement with Escape/right-click.
4. Object browser: place a clear-footprint object, change rotation/type, then inspect/move/duplicate/delete it through Spawns. Check undo/redo of a temporary placement.
5. Save one placement explicitly; verify temporary cleanup on logout and saved restoration after your next restart.
6. Check Jaxa/Nooby session/ownership isolation and refusal for an account without developer permission. Finish with a quick keybind/queue/Revolution/Dive check.

Please use Bug Test for any input failure; report the selected action or NPC/object ID and whether the cursor, tile click or resulting world change failed. Do not use old deferred issues as acceptance gates for this console.
