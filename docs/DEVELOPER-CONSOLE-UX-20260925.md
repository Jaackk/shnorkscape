# Developer Console UX successor ? 25 September 2026

This pass keeps interface 1448, the proven native-ready handshake, shared command handlers, cached definition index, placement ownership and persistence. It changes the GUI layer rather than combat or the client executable. **Vulkan acceptance is pending.**

## Changes

- The visible frame title now targets dynamic child **14**, matching exact-950 CS8289 instructions 133?164; child 3 was a frame element, not the heading. Title width uses the same native font measurement and frame-height parameters. The existing native tabs are still hidden by the takeover.
- Friendly action labels, direct toggles, single-click inspection, double-click execution for zero-argument actions, and native right-click Execute Now / Favourite / Details. CS10324 establishes CC_SETOP and the event-operation sentinel. Every callback retains its page token. A one-shot continuation accepts a second physical click already queued with the inspected action's old token; navigation, another render, close, permission loss or execution invalidates it.
- Command responses and errors appear in paged native details. A synchronous, owner/channel-scoped output sink wraps the existing authoritative handler and is always removed in `finally`. Typed commands and subsequent combat messages retain normal chat behaviour. Gameval printing is explicitly labelled a raw diagnostic.
- Both the sidebar and top NPCs/World entries open searchable browsers. Name, partial name, exact ID and audited symbol search resolve existing 950 catalogue entries. The original read-only index is retained; a bounded 32-query immutable cache avoids repeatedly filtering/sorting 77,850 entries. Cached results cannot be mutated by callers.
- NPC rows show name, ID, footprint and cache level. The inspector exposes the existing combat admission, style, animations, drops and provenance report inside the console. Amount, repeat policy, chosen-tile Place, collision-checked Spawn Near Me, audited encounter travel and test-encounter creation use existing backend paths.
- Object placement defaults to a valid cached model type. Normal controls are Rotate Left/Right, Save to World and Place. Raw model types are behind Advanced; the exact current definition is checked again before native targeting is armed. After a tile placement, the console returns through its proven readiness handshake and shows the result.
- Spawns adopts command-created and boss-test NPCs into the same identity-based ledger, without duplicating editor entries. It preserves repeat policy and labels boss actors. Rows use names; inspect, move, duplicate, rotation, owner-only delete, save and safe teleport remain contextual. Clear NPCs / Objects / Temporary use the editor's ownership and persistence checks; a different control or intervening redraw invalidates bulk-clear confirmation. Saved records are removed atomically before world deletion. Another player's entities and map scenery are excluded.

## Performance evidence and bounded native research

The supplied live baseline has 100 console operations with a nearby preceding input-ingress event: median 267.5 ms and maximum 572 ms. This is an approximate boundary correlation, not a client-render benchmark; callbacks are handled on the 600 ms world cadence. The pass does not move world mutations onto Netty threads. It caches read-only query results, avoids boundary-page redraws, flushes completed console packets and records render microseconds/actor/text counts in Bug Test. Physical redraw latency still needs Vulkan measurement. Two isolated cache probes measured the first Vorago lookup at 12?36 ms; 1,000 cached lookups took about 2.3 ms total.

**Native scrolling / partial redraw: PARTIAL.** Exact CS11074 uses content/scroll hosts 753:28 and 753:31, content sizing/reset and CS7791. The console's native nine-slice builders currently share visual/actor/text hosts 1448:3/5/7. A safe reusable scrolling composition must keep those layers clipped and scrolled together and preserve source-slot/page-token ownership. That combined contract is not yet proven. Existing working pagination and atomic renders remain; this pass does not claim incremental rendering or smooth scrolling.

**NPC/model/animation preview: UNRESOLVED.** The existing native Beasts integration is driven by boss structs/varc4485 and metadata scripts 3869/11074, not an arbitrary NPC definition setter. The server's `sendNPCOnIComponent` and `sendIComponentModel` are still explicitly unverified no-ops. No verified arbitrary-NPC model/sequence/scale contract for the developer inspector emerged from this bounded investigation. Native metadata reports are used; no fake renderer, hostile NPC stand-in or speculative animation IDs were introduced. A separate exact-client setter investigation can add preview without replacing the browser/backend.

## Verification

Focused input/output, semantic browser and shared-handler tests; native script stack/menu/marker contracts; full Java regressions; actual-cache placement, command-NPC adoption/deletion, repeat policy, persistence and two-player clearing; staged cache preflight and installer fixtures. Full Java suite: 1,583 tests, zero failures/errors, two skips. Seven script VM tests plus the shared readiness-pin test pass. Actual-cache editor and boss-regression probes pass, including four-actor encounter adoption, clearing and immediate recreation. Staged native preflight passes. Installer/hash results are recorded in the handoff after packaging. These checks do not prove the new menus/title/placement return path visually accepted in Vulkan.

## Live checklist

1. Fresh `;;dev`: Developer Console title, layout and native controls.
2. Right-click Heal ? Execute Now; also single/double click and a direct toggle.
3. NPCs ? search Vorago ? select ? Place; inspect combat details inside the console.
4. Search a named object, rotate and place without entering an ID/type.
5. Spawns: delete a placed or command-created NPC/object; check owned clear controls.
6. Search responsiveness and inline results/errors.
7. Close/reopen, then quick action-bar keybind regression.
