# M7d: native settings tabs

Gameplay, Controls, Ribbon and Accessibility are present in the paired 947 cache.
The earlier Graphics/Audio milestone deliberately disabled them. This extension
opens their native pages through the existing settings owner.

## Cache bindings

| Page | Tab number / actor at 1477:714 | Disable varbit | Struct | Interface at 1448:3 |
| --- | --- | --- | --- | --- |
| Gameplay | 1 / 3 | 19029 | 21179 | 365 |
| Controls | 3 / 11 | 19031 | 21181 | 1444 |
| Ribbon | 5 / 19 | 47565 | 44487 | 567 |
| Accessibility | 6 / 23 | 60056 | 52418 | 365 |

The disable varbits now receive zero. Page selection uses server varbit 19001;
the existing 8283(struct, 0) script applies each page's native geometry. Exact
interface bytes, structs and initialization scripts are fingerprinted by the
settings verifier before any open can replace a dialogue, bank or map.

Gameplay and Accessibility share interface 365. Its onLoad 2923 selects the
catalogue through 20382. Accessibility requires page 6 and a visible management
wrapper (1477:708); the wrapper is shown before attaching the page. Script 20387
then initializes unset Accessibility parent/leaf selectors before 365's onLoad
captures them. Without this ordering, the first Accessibility display has the
right navigation but stale Gameplay Combat Mode contents. Existing nonzero
Accessibility selections are preserved. Switching
between these tabs closes and recreates 365 with the new page selected first.
Repeating the same tab restores visibility without recreating its controls.
Category navigation and search are supplied by the client.

Controls contains the native key-binding editor. Ribbon contains native
Add/Remove, drag, Default and Clear behavior. Existing login initialization still
seeds the compact ribbon, so this change does not establish ribbon persistence
across login or implement every destination that can be added to the ribbon.
The custom-ribbon checkbox already installs its own click hook through
13849 → 10416 → 10419 → 14457 → 14458 → 10422 → 2755. It must not be toggled
again by a server handler. Login now initializes both its toggle source (client
bit 21816) and displayed mode (42113) to one, fixing their initial mismatch.
The modern login profile also marks normal-world onboarding complete with
server bits 39917=98, 49044=100 and 60098=1 before mounting panels. Native
15532(0) otherwise blocks the custom checkbox with a tutorial warning. These
three definitions and the guard scripts are pinned; varp 12314 (active League)
is not changed. The historical login baseline remains unchanged.

Gameplay and Accessibility also expose choices whose effects belong to the
server. Unlocking their page does not port combat, action bars or other missing
gameplay systems. Local preferences and server-dependent choices must be
distinguished when checking behavior.

Four Gameplay checkbox actors explicitly enter the native waiting state for a
combat-mode response: 365:19 slots 10240, 10241, 10242 and 15872. The owner
accepts only these exact operation-1/item-minus-one requests while Gameplay is
visible, refreshes the native controls with 2929, and explains that the mode
is not available on the local server. It does not write combat preferences.
Local controls, Accessibility, stale events and malformed requests are excluded.
The owner grants operation 1 only to 365:19 ranges 10240..10242 and 15872..15872
while Gameplay is open, and clears both overrides before leaving that page.
This is necessary because the native click hook runs before the separate server
event-mask check; the cache's zero mask would otherwise prevent the reply.
The positive list is derived from the paired DB rows and settings structs;
93 ordered cache bindings protect it. See
`OpenNXT/data/prot/947/generated/native947-3/verified/ui/PENDING_SETTINGS.md`.
This recovery is limited to those checkboxes; it does not implement arbitrary
dropdown or slider requests.

## Validation

The existing settings regressions now cover all six tabs, invalid and stale tab
actors, same-tab loading acknowledgement, Gameplay/Accessibility reinitialization,
Audio placeholder cleanup, close and scene preservation. The encrypted cache
smoke checks the page selection and attachment packets, not just interface IDs.

The integrated build in `logs/m7d-settings-tabs-native-fixes-ship.log` passes **654 tests**:
579 engine and 75 frontend, with zero failures, errors or skipped tests.
The installed server is PID **6616**, created **2026-09-08T21:46:09.8533110Z**.
Rollback snapshot `backups/install-2026-09-08-164414` contains 112 JARs.
The first installed smoke passed every settings/map/dialogue assertion but its
final cleanup assertion raced `closeOnWorld`: the world frees the session slot
before clearing its NPCs. The smoke now observes final entity removal on the
world thread after the close command. This changes test synchronization, not
server cleanup behavior. The first result is retained in
`logs/m7d-settings-tabs-installed-smoke.log`.

The corrected installed smoke passed in
`logs/m7d-settings-tabs-final-installed-smoke.log`. A final pass after granting
the exact pending-checkbox event ranges is recorded in
`logs/m7d-settings-tabs-events-installed-smoke.log`. It verifies all six pages,
repeat clicks, stale events, shared-interface recreation, event grants/removal,
the pending combat reply, Audio cleanup, map and Cook transitions, scene
preservation and complete disposable-entity removal. No profiles are written.

The final installed distribution passes the same smoke in
`logs/m7d-settings-tabs-native-fixes-verified-smoke.log`. Its first invocation
from the restricted shell failed before admission because the class scanner
could not resolve the workspace path (`AccessDeniedException`); that attempt
is retained in `logs/m7d-settings-tabs-native-fixes-installed-smoke.log`.
Running the unchanged installed smoke with ordinary project access passed.

The first native pass (client PID 27540) confirmed populated Gameplay and Controls
pages, key-binding capture with Zoom In restored to Page Up, a working Revolution
rejection response without a stuck overlay, and High Contrast Mode switching on
and back off. Ribbon hit a tutorial-state guard; first Accessibility entry had
stale Combat Mode contents until category navigation. The updated build includes
the onboarding and selector-order corrections described above.

The final native pass (client PID 27624) verified:

- Fresh-session Accessibility opens directly on High Contrast Mode. Text remains
  selected across Text → Gameplay → Accessibility and an Escape close/reopen.
  High Contrast Mode was left off and its category restored afterward.
- Gameplay has its own Combat Mode contents when returning from Accessibility.
- Controls accepts Zoom In = Home; Page Up was then restored.
- Ribbon custom mode switches the live HUD to the default list and back without
  a tutorial warning. Adding/removing Quests updates the HUD; the original five
  shortcuts and custom mode were restored. Drag and labels have the limitations
  recorded below.
- Escape closes and reopens settings while the world remains rendered. The
  client was left logged in with Settings closed.

The read-only server health snapshot at tick 606 shows zero tick failures,
scheduler failures, strict facade hits and dropped facade calls. Its result is
`logs/ui-debug/result-09a2cd19-2280-4029-88b9-93135887d88a.txt`. Unsupported input
frames remain counted by the wider port; this is not a claim of zero unhandled
inputs or complete gameplay support.

Automated and sampled native checks do not establish that every individual
setting works or port its underlying gameplay system.

## Follow-up: ribbon drag initialization and stale labels

The final native check confirmed that Use custom ribbon switches the bottom HUD
to the default list and back without the tutorial error. Adding Quests produced
six icons; clicking it again removed it and restored the original five. Its
preview context menu still said “Add Quests” while it was selected. A drag attempt
from Quests to Skills resulted in the click/remove behavior, so **reordering is
not accepted as working**. These results establish basic Add/Remove and custom
mode behavior, not full ribbon editing or every added destination.

Read-only cache inspection found a possible first-render cause. In 13845,
instructions 339–371 install drag handler 2395 only when the icon child already
exists; instructions 387–397 create that child afterward. The background receives
Add/Remove handler 2396 independently. Script 13862 then refreshes visibility and
positions through 13848/13847, without rebuilding those operation labels. This
explains how a stale “Add” label can still perform Remove; it does not prove that
the observed drag failure was caused by initialization rather than input timing.

No missing server event mask was found: the pinned static drop zones 567:7 and
567:16 already carry mask 2097152. Handler 2395 accepts 567:7 for insertion into
44-pixel cells and 567:16 for removal, then calls 2393 locally. Builder 13845
sets the drag parent to 567:7, native desktop drag parameters to 5/5 (20/5 in its
mobile branch), and drag rendering mode 2. No new server drag handler is proposed.

**Unimplemented, unverified initialization proposal:** after attaching 567 at
1448:3, before the existing page layout/visibility acknowledgement, run these six
native calls in order:

1. `13839(37158926, 37158927)` — 567:14/15, first pass.
2. `13839(37158926, 37158927)` — 567:14/15, second pass.
3. `13838(37158940, 37158942)` — 567:28/30, first pass.
4. `13838(37158940, 37158942)` — 567:28/30, second pass.
5. `13837(37158935, 37158936)` — 567:23/24, first pass.
6. `13837(37158935, 37158936)` — 567:23/24, second pass.

The first pass creates any missing actors; the second can install their drag
hooks. The builders create the entire enum actor set and later hide unused
entries, so adding another visible icon should retain those hooks. One builder
pass on a repeated Ribbon-tab selection would refresh labels, but would not
keep labels fresh immediately after every subsequent client-owned edit.
Automatic onLoad ordering and actual drag behavior still require native checking
before implementing or claiming this repair. No production changes were made for
this proposal.

Paired-cache SHA-256 pins for the proposed builders and relevant handlers:

| Script | SHA-256 |
| --- | --- |
| 13839 | `88c2651f7251cc0c83a184d8827b4a8718ef67350d615d1f39c859bfc6ab22c6` |
| 13838 | `6d47cc19e527034950a873e12057e9a3c2ff9e998a1a9b92e0ef5f26509078df` |
| 13837 | `755ce69b77bbaf64819943b8f7ced56ffdfa2be8a2794377f0e8283893a131f9` |
| 13845 | `350dbbeb142a9d59102197b2abf141a6a8cc9efa473062b800eced21c2c68326` |
| 2395 | `a6d58f2730bbce6944524258ab0bc07e4699cb475951c606dcd239ec061c0594` |
| 2396 | `3a9bb6be08a8498de2a6dbe139478a9ed32fa3f214ad0fcff7b6ded0953f4fba` |
| 13862 | `0bc3db1abd8e5bd1ba8ac92b18a1559442c25bdf507f7587b8961ff97d40919c` |
