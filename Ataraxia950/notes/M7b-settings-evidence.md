# M7b: native settings and bottom ribbon

The subsequent [M7d settings extension](M7d-settings-tabs.md) unlocks Gameplay,
Controls, Ribbon and Accessibility. The unavailable gates described below
record the original Graphics/Audio milestone.

This milestone adds the paired 947 client's Graphics and Audio preferences to
the native management window. Gameplay, Controls, Ribbon editing and
Accessibility remain visibly unavailable. The user requested these settings
and a compact bottom ribbon; this does not promote the deferred server music
streaming packets or port every legacy gameplay setting.

The final coordinated build, installed real-cache smoke and native acceptance
checks have passed. Graphics, readable Audio controls, tested mute and volume
changes, the five-icon ribbon, and Escape open/close behavior are working.
The earlier Audio layout and duplicate Escape problems below are retained as
the evidence for their fixes.

## Sources and reproducibility

The research reads the project's paired flat cache, without importing 910
component numbers. `OpenNXT/tools/inspect_native947_settings.py` uses the existing
flat-cache archive reader and verified IF3/CS2 decoders. Its modes are:

- No arguments: decode the selected interfaces, with full byte-consumption
  assertions, and print the enum 7716 slot structs.
- `--facts`: decode Settings menu enum 7699, its page structs, exact root
  components, and the varbit domain/parent/range.
- `--pins`: emit the SHA-256 assertions used by `Native947Settings.verify()`.
- `--refs <integers>` and `--calls <script IDs>`: locate decoded push-constant
  references or script callers after a raw prefilter.
- Numeric script IDs: retain a CS2 instruction dump and each file's digest.

Research outputs are under `Ataraxia947/build/ribbon-research/`. In particular,
`settings-components.json`, `settings-components.txt`, `settings-facts.txt`,
`settings-tab-routing.txt`, `settings-gates.txt` and
`settings-panel-layout.txt` document the findings. The broad component census
reports an unparsed type-10 component at 365:2; interface 365 is not used or
claimed verified by this implementation.

The earlier `UI-PLAN.md` and `UI-DECISIONS.md` music restrictions concern server
music behavior. The preferences here invoke native client setters. The
historical warning against opening quick-options 1433 at raw host 747 still
applies: that host belongs to dialogue framing. M7b never opens 1433 there.

## Verified entry and response targets

All entries use the existing decoded `InterfaceAction`, opcode 96 for option 1.
The item sentinel must be -1.

| Action | Interface:component | Dynamic slot | Evidence |
| --- | --- | --- | --- |
| Escape / options | 1477:8 | -1 | Root component key binding and onOp 8181; baseline native log |
| Settings ribbon | 1431:0 | 7 | Button 137 is enum 13319 actor 7; 8146 → 5588 |
| Graphics tab | 1477:714 | 7 | 8190/8186/8191/10068, second tab's fourth dynamic child |
| Audio tab | 1477:714 | 15 | Same scripts, fourth tab's fourth dynamic child |
| Management close | 1477:717 | 1 | Struct 21301 param 3507 and script 8181 |

The active native frame accepts only the two supported tab actors. Wrong
options, item/slot sentinels and stale tab or close actions cannot create a
window. Escape toggles the per-session owner. The Settings gear opens or
focuses Graphics; it does not promise a toggle because native hooks can send
CLOSE_MODAL before the gear's entry action.

The gear hook is special: `5588(137,-1)` selects its menu-9 branch and calls
8182, toggling the quick-options wrapper 1477:805. It does not use the generic
20343 management-request path. Full-settings open, repeated gear focus and
close therefore run the verified quick-overlay cleanup 8179. Native QA
confirmed gear delivery, including a second gear click returning to Graphics.

That quick hook exposed an Escape lifecycle gap: 8177 shows 805, then accesses
the absent quick interface 1433:62 before registering context 1 through
8841(1,1). Cleanup 8179 calls 8841(1,0), disabling the Options key. Because
context 1 was never on top of the stack, normal base-key restoration does not
run. The follow-up explicitly runs **8180(1,1)** immediately after each owned
8179 cleanup. Its two-int-argument enable branch calls
`8844(1,13,8,1477:8,-1)`, restoring the existing key and repeat policy.
It does not clear the global modal stack. Scripts 8180 and 8844 are pinned;
their hashes are respectively
`ab016c02ae5f518a233c3ea23796f591d1a6d1ce8345559f2eee6505c3c3a570`
and `ea851b4bd719b94829f5b2b6e4351e878f4a5835cc0637eddf278fd18b58e2cf`.
The 620-test installation confirmed that Escape could open Settings again,
but closing an open page emitted both 1477:717 and 1477:8. This is one key
hook, not proof of two independent Escape bindings: 8181 itself explicitly
emits the management close actor at instructions 107–112; the generic native
operation router subsequently emits the original root-8 action. Its onOp
runs before the server event-mask check, as independently recorded in
`verified/INTERACTIONS.md` and `verified/ui/RIBBON.md`.

The final correction keeps 8180's key hook enabled. While Settings is open or
focused, `IF_SETEVENTS(1477,8,-1,-1,252)` suppresses only the generic operation-1
notification. The pinned cache mask is 254; removing bit 1 preserves all
other native operations. Closing restores the complete mask 254. This lets
8181 send its explicit close actor once and allows a later Escape to open
Settings normally. It does not use a timeout or discard an arbitrary next
server action. A new regression checks both mask states and preserved key
setup. Native acceptance of this final correction passed: Escape opens
Graphics, closes it with only the 717 action, stays closed, and opens it again
on the next key press. Escape also closes Audio without reopening Graphics.

## Management frame and layout

Enum 7716 key 1001 resolves to struct **21301**, Management Windows:

| Purpose | Struct parameter | Component |
| --- | --- | --- |
| Wrapper | 3503 | 1477:708 |
| Frame | 3506 | 1477:712 |
| Tab bar | 3509 | 1477:714 |
| Content attachment | 3505 | 1477:715 |
| Close actor | 3507 | 1477:717 |

Interface **1448** attaches at **1477:715**. Its content attachment hosts are
3, 5, 7, 9 and 11, positively checked by script 12293. The neighboring hidden
components 4, 6, 8, 10 and 12 are slot wrappers, not attachment hosts.
Root 1477:707 listens to client varc **2911** through script 8286: menu 9
refreshes the management frame; -1 closes it.

Menu enum 7699 key 9 resolves to struct **21178**, Settings. Graphics page 2
uses struct **21182**, interface 1426 and desktop geometry 742×404. Script
12343 promotes desktop height 404 to 450. Its verified caller
**8283(tabStruct, slotIndex)** applies the returned x/y/width/height to the
correct 1448 host. M7b calls `8283(21182,0)` for the Graphics content panel.
Script 8288 supplies the Settings title, native tab strip and frame sizing.

Graphics owns these explicit server attachments:

```
1477:715 → 1448
1448:3   → 1426
1426:0   → 742
```

Interface 742 has only seven components. Its onLoad 2594 calls 2595(1), which
selects either **324** or **1513** at 742:0 using a native client predicate.
Both possible child interfaces and both scripts are pinned. The server does
not fabricate preference handlers or register those client-created children
as additional server-owned attachments.

Audio's cache tab struct **21183** pairs music catalogue 187 in slot 0 with
Audio 429 in slot 1. Native QA refuted the initial attempt to use full-width
429 at slot 0: a later **8186 → 8282 → 8283** tab refresh reapplies all five
hosts' selected-tab geometry. Slot 0 consequently became the music column at
x=534, width=209. The observed controls appeared in a narrow right column,
with their fixed-width inner rows overlapping labels.

The follow-up preserves Audio's native composition: **429 at 1448:5**, x=0,
width=539, using `8283(21183,1)`. It also opens an empty **1426 at 1448:3**,
lays out that placeholder with `8283(21183,0)`, and keeps host 3 hidden.
This satisfies script 8784's populated-slot-0 resize check without displaying
the unimplemented music catalogue 187. Graphics owns 742 inside 1426; Audio's
placeholder contains no 742. Audio close removes both attachments.

The server shows the selected content host, shows 1477:708, hides 1448:1's
loading layer, and enables only tab actors 7 and 15 and close actor 1. A
same-tab click still needs a visibility response because native script 10068
first hides the page and displays Loading; it does not recreate the
preference controls.

## Unavailable tab gates

Script 8284's Settings branch reads these cache-defined server-domain varbits.
Script 8186 presents the unavailable state, clears that tab's onOp and option,
and supplies the native text “This tab is currently unavailable.”

| Tab | Varbit | Parent / bits | M7b value |
| --- | --- | --- | --- |
| Gameplay | 19029 | 3711 / 25 | 1 |
| Controls | 19031 | 3711 / 27 | 1 |
| Graphics | 19032 | 3711 / 28 | 0 |
| Audio | 19033 | 3711 / 29 | 0 |
| Ribbon editor | 47565 | 4334 / 25 | 1 |
| Accessibility | 60056 | 12431 / 2 | 1 |

Additional exact fields are menu 18994 (parent 3708, bits 0–8), subpage 19001
(3709, bits 4–7), and management lock 19004 (3710, bit 0). The verifier pins
each definition, including domain 0. Updates change individual ranges and do
not overwrite whole shared parent variables.

## Client-owned audio behavior

Interface 429 onLoad is script **8043**, which initializes volume controls
through 13818 and 13821. Its four channels are Music, Sound Effects, Ambient
Sounds and Voice Over. Script 13819 updates client-domain-2 varbits
38833–38836, whose parent is 6341; 13820 completes the drag and refreshes.

Script 13816 applies native preference IDs 23 (music), 22 (effects),
24 (ambient), and 25 (voice) through 15087 and CS2 opcode 0x6FE. Independent
native analysis found registrar 0x140075AC6 → handler 0x1401B9B50 → local
preference setter 0x1403DA5B0. A following helper can emit opcode 108 window
status telemetry, but that setter does not require a server reply.
Mute uses 9287 → 5523 → 5633/5639, followed by 13818's refresh.

Native QA confirmed that the Music slider position changes and restores, but
clicking the Global mute checkbox did not toggle it. The checkbox graphics
have option 1 and no onOp, while label 429:6 already runs 9287. Their setup
chain 13818 → 1191 → 10002 → 10020 → 2798 styles the checkbox and installs
mouse feedback, but never installs the mute action. The follow-up therefore
accepts only exact visible Audio checkbox actions: component 7 runs 9287;
15/32/49/66 run 5523 with channel 0/1/2/3, then 13818. Each requires option 1,
slot -1 and item -1. Existing label/slider hooks are not repeated by the
server, and stale checkbox actions do nothing. The setter and styling chain
are now pinned, with unit coverage for every channel and invalid context.

The implementation leaves these native preference changes to the client. It
does not write their client-domain variables from server varbit packets.
Audible server-triggered effects, region music and preference persistence
across application restart are not established by this static proof.

## Ownership, cleanup and verification

`Native947Settings` binds to the session's real `InterfaceManager`. The
coordinator verifies before cancelling a bank, map, dialogue or quantity
request. Opening stops the pending route; page changes close only owned child
attachments. Closing retires the quick overlay and management frame, clears
varc 2911, unregisters 1448 and the selected page, and preserves the game
scene at **1477:30**. It is safe to call close repeatedly, including before an
initial open. Shared manager presence checks use this owner rather than a
legacy raw component number.

The integration also corrects initial scene ownership.
`Native947World.bootstrapOpensScene` follows the ordered interface packets
actually flushed during bootstrap. Before session readiness it registers
1482 at 1477:30 only when that attachment is present after those operations.
The new ordered regression covers this distinction; startup bookkeeping no
longer assumes a scene that the bootstrap packet sequence did not open.

`Native947Settings.verify()` pins the complete selected interface groups,
root components, menu/page structs, menu enum, varbit definitions, and the
entry/layout/tab/preference scripts. It refuses an incompatible cache before
opening or writing any packet. The isolated tests inject only this verifier;
production uses the paired cache and its actual SHA-256 bytes.

## Final validation and installation

- `logs/m7b-settings-escape-final-ship.log` passed **621 tests**: **552 engine**
  and **69 frontend**, with zero failures, errors or skipped tests. The helper
  has ten lifecycle regressions, including the duplicate Escape correction.
- `logs/m7b-settings-escape-final-installed-smoke.log` passed against the
  installed distribution, covering the settings, map, dialogue and scene
  transitions with the real paired cache.
- Current server PID **13584** was created at
  **2026-09-08T20:29:30.8187380Z**. The rollback snapshot is
  `backups/install-2026-09-08-152800`, containing **112 JARs**.
- Final native checks passed after manual login: Escape opened Graphics,
  closed it and stayed closed (only the 717 close action was logged), then
  reopened it on the next Escape. Audio remained readable, and Escape closed
  Audio without reopening Graphics.
- The preceding 620-test native session verified Audio at both tested window
  sizes; Global mute retained its checked state through reopening and was
  restored off; Music mute checked and was restored off; the Music slider
  changed, retained its position across resize, and was restored to about
  50%. All four panel toggles and the five-icon ribbon at both tested sizes
  passed. These checks supplement the final Escape retest; they do not claim
  preference persistence across a complete client application restart.
- Final read-only health in
  `logs/ui-debug/result-d24d8374-25b2-4066-93d5-bf84b12f54ad.txt` reported
  active=true, mapOpen=false, zero tick failures, zero strict facade hits and
  zero scheduler failures. Three known music-interface-187 label writes were
  dropped by the deferred legacy facade. The snapshot also counted 42
  unhandled frames, with last opcode 30. The deferred music behavior remains
  outside this milestone; the health result is not a claim that every legacy
  packet family is implemented.

## Earlier validation history

- The initial six isolated helper regressions passed: exact entry targets,
  verification failure without mutation, page ownership and scene preservation,
  same-tab loading acknowledgement, unavailable/stale targets, and shared
  Escape lifecycle and gear entry with Graphics as the initial page.
- The full build in `logs/m7b-settings-scene-ship.log` passed **617 tests**:
  548 engine and 69 frontend, with zero failures, errors or skipped tests.
- The installation saved rollback snapshot
  `backups/install-2026-09-08-150920`, containing **112 JARs**. That installed
  server was PID **18612**, created at **2026-09-08T20:11:47.9610280Z**.
- Installed encrypted real-cache smoke
  `logs/m7b-settings-final-installed-smoke.log` passed entry, tab switching,
  close, stale-action rejection, map transitions, Cook's No branch, and scene
  ownership checks. It created no persistent profiles.
- On this first installation, native Graphics rendered at full size and its
  brightness slider changed the visible world, then restored it. Music slider
  position changes also worked. Audio column clipping, inert mute checkboxes
  and lost Escape behavior prevented full acceptance.
- The next coordinated build passed **620 tests** and its installed smoke
  passed on server PID **18620**. Native Audio rendered a readable 539-pixel
  control panel without clipping. Global mute stayed checked through
  Audio → Graphics → Audio, then was restored off. Escape opened Settings,
  but its duplicate close/open notification prevented final acceptance.
- The final mask correction added the tenth helper regression. Its passing
  build and native Escape retest are recorded above; the 617- and 620-test
  installations remain historical evidence.

The bottom ribbon implementation is owned by the parallel ribbon change:
Settings button 137 is stored as value 138 at custom index 4 (varbit 21792),
followed by terminator 0 at 21793. Native event actor 7 is enabled after the
native ribbon construction script. This note records the settings-side seam;
the root agent owns combined deployment and acceptance evidence.
