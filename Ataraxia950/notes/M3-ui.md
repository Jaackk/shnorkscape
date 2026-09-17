# M3, area C: the login UI bootstrap (skills tab, action bar, minimap orbs)

Owner file: `game/com/rs/game/player/client/Native947StatsUi.java`.
Test: `tests/modern947/Native947StatsUiTest.java`.
Call site: `Native947Interactions.bootstrap()`, which `Native947Session.ready()`
runs once, after the game transport is in the channel pipeline.

Everything below is either a CONFIRMED row of
`OpenNXT/data/prot/947/generated/native947-3/verified/ui/` or is called out
explicitly as a choice this milestone made. No id is hard-coded in Java: the
class reads slots, interfaces and cs2 scripts out of the validated binding table
(`resources/native947/ui-bindings-947.json` through
`com.rs.game.player.client.ui.Native947Bindings`) and every packet leaves through
`Native947PacketDispatcher`, so the allow-list resolver still gets the last word
on each individual id.

## 1. What the bootstrap emits, in order

| # | Packet | Source of the id | Why |
|---|---|---|---|
| 1 | `IF_OPENSUB` slot `skills`.attach, interface `skills` | enum 7716 key 0 -> struct 21293 param 3505 = 1477:300 | attach the stats panel |
| 2 | `IF_SETHIDE` slot `skills`.wrapper, hidden=false | struct 21293 param 3503 = 1477:298 | the Kotlin handoff hides every root wrapper it does not retain |
| 3* | `RUNCLIENTSCRIPT` `component_size`(224, 360, 0, 0, wrapper) | script 11145 | an unconfigured native layout slot has zero size |
| 4* | `RUNCLIENTSCRIPT` `component_position`(456, 80, 2, 2, wrapper) | script 13268 | place it next to, not on top of, the equipment panel |
| 5* | `RUNCLIENTSCRIPT` `component_show`(wrapper) | script 2330 | `0x00e4` hide=false |
| 6* | `RUNCLIENTSCRIPT` `layout_save_working`(slot key 0) | script 8707 | survive a window resize |
| 7* | `RUNCLIENTSCRIPT` `layout_save_preset`(slot key 0, 8) | script 8708 | active preset is client variable 4108 = 8 |
| 8 | `IF_OPENSUB` slot `action_bar`.attach, interface `action_bar` | key 1003 -> struct 21277 param 3505 = 1477:70 | attach 1430 |
| 9 | `IF_SETHIDE` slot `action_bar`.wrapper, hidden=false | struct 21277 param 3503 = 1477:67 | show the HP/prayer/summoning/adrenaline bars |
| 10 | `IF_SETHIDE` slot `minimap`.wrapper, hidden=false | key 1004 -> struct 21278 param 3503 = 1477:92 | show the run orb (1465:14) and the energy bar (1465:17) |

**Rows 3-7 are marked `*` because they are OFF by default.** An ordinary login
sends rows 1, 2, 8, 9, 10 and nothing else. Two separate things about the layout
recipe are unproven: whether slot 0 needs it at all (`SKILLS_TAB.md` section 8
says the sequence "was not re-tested for slot 0"), and the geometry it would
carry, which is not evidence - see section 4. Script 8708 writes the box into the
client's persistent layout preset 8, so a guessed geometry would be committed to
saved client-side state on the very first login, before anyone has seen the panel
render. The recipe is therefore behind a flag:

```
-Dataraxia947.skillsLayout=true
-Dataraxia947.skillsLayout.width=224   .height=360
-Dataraxia947.skillsLayout.x=456       .y=80
-Dataraxia947.skillsLayout.xMode=2     .yMode=2
```

(or `Native947StatsUi.setSkillsLayoutEnabled(true)` in a probe). One live session
can iterate the geometry without a rebuild and, once the panel renders where
intended, the defaults change and the flag flips on by default.

Then, and only then, the stat and vital burst. That step is **not** emitted by
this class: `Native947Session.ready()` calls its own
`sendNative947LoginState()` on the line after `interactions.bootstrap()`, so the
panels are already attached when `UPDATE_STAT` arrives. `Native947StatsUi`'s
default `StateEmitter` is therefore `NONE`; the full burst still exists as
`Native947StatsUi.ENGINE_STATE` for a caller that wants this class to own the
ordering, and the unit test injects a marker emitter to assert that the state
frame is strictly last.

### Why the ordering is load-bearing

`SKILLS_TAB.md` section 2: neither `1466:0` nor `320:2` has an onLoad hook. The
panel is built by `8488`/`8489` from the component's **onStatTransmit** hook, and
that hook has no stat filter list, so any `UPDATE_STAT` rebuilds the whole tab.
A stat burst that arrives before `IF_OPENSUB` is simply lost: there is no
component to receive it, and nothing re-runs the builder afterwards. The action
bar is different - `1430:0` has onLoad `8109(1003)` - but its four bars still
redraw from their own varp/stat transmit lists, so the same order is correct for
them too.

### Why the action bar gets no geometry scripts

`ORBS_AND_VARS.md` section 5: `1430:0` onLoad is `8109(1003)` -> `8110(slot)`,
which runs the layout helpers `8111, 13765, 8115, 8114, 8112, 8116` itself. The
skills panel has no onLoad at all, which is exactly why it, and not the action
bar, is the one that may need the explicit `11145/13268/2330` + `8707/8708`
recipe `EQUIPMENT_CONTENT.md` proved necessary for slot 3. "May": that it is
also necessary for slot 0 is the CANDIDATE half, which is why the recipe is
opt-in rather than sent on every login.

### Fail-closed behaviour

A slot, interface or script name the table does not declare is skipped, counted
(`Native947StatsUi.skippedBindings()`) and logged once as
`[Ataraxia947] M3 HUD bootstrap skipped <what>: <reason>`. An id the table does
declare but the resolver rejects is a counted drop inside the facade
(`Dropped sendInterface(...): writer rejected interface N: id is not bound in the
947 table`). Neither path can put a guessed id on the wire.

## 2. Binding-table names this class consumes

Area A owns the table. `Native947StatsUi` looks these up by name (first match
wins; the aliases exist only so a differently but equally validated entry is
found instead of being silently skipped):

| Kind | Preferred name | Aliases accepted |
|---|---|---|
| slot | `skills` | `skills_tab`, `stats` |
| slot | `action_bar` | `actionbar`, `main_action_bar` |
| slot | `minimap` | - |
| interface | `skills` | `skills_tab`, `stats` |
| interface | `action_bar` | `actionbar`, `main_action_bar` |
| script | `component_size` (11145) | - |
| script | `component_position` (13268) | - |
| script | `component_show` (2330) | - |
| script | `layout_save_working` (8707) | - |
| script | `layout_save_preset` (8708) | - |

The five scripts and the `minimap` slot were already in the shipped table (the
equipment recipe uses them). The `skills` and `action_bar` slots and interfaces
are Area A's M3 additions and are present under the preferred names above, so no
alias is in use today. Before they landed, the bootstrap logged two
`skipped slot` lines and emitted only the minimap wrapper unhide; that is the
behaviour to expect on any table that drops them again.

## 3. The 1466 versus 320 decision (CANDIDATE, needs one live login)

`SKILLS_TAB.md` section 2 and verifier note 9.2: interfaces **1466** (15 files)
and **320** (18 files) both exist and both carry the same builder hook `8488`
with the same varp transmit list `[458, 458, 4737, 4737, 5863]`. Two panel
families coexist behind a client-side mode value (script 1472 picks the
struct-declared family when `13749() == 1`, the script-2141 family otherwise).

- 1466 is CONFIRMED as the cache binding of **slot key 0** through script 2141
  case 0, and it is the interface the 910 server used
  (`InterfaceManager.setWindowInterfaceByKey(0, 1466)`).
- 320 is CONFIRMED as the panel declared by struct 21293 params 3514..3517, used
  by the newer frame scripts 8410/8411.
- "Use 1466 rather than 320" stays CANDIDATE. The verified backpack path already
  prefers the 2141-table interface (1473) over the struct-declared one (1474),
  which is why 1466 is the default here.

**The switch.** The panel is named, not numbered:
`Native947StatsUi.skillsPanelBinding()` returns a binding-table entry name,
default `"skills"`. Override it with the JVM property

```
-Dataraxia947.skillsPanel=skills_alt
```

(or `Native947StatsUi.setSkillsPanelBinding("skills_alt")` in a probe). The named
entry must exist in the table. If it does not, the class SKIPS the skills panel
(`M3 HUD bootstrap skipped interface <name>: the configured skills panel is not
declared in the 947 table; not falling back to the default`) and opens nothing in
that slot. It deliberately does not fall back to the default: quietly reopening
1466 would make an operator who launched with `skills_alt` read 1466's behaviour
as 320's result, and this switch exists for a one-attempt comparison. The
interface id actually opened is logged on every bootstrap
(`M3 skills slot opens interface 1466 (binding 'skills')`), so the live session
records what it tested. Settling this live therefore needs Area A to add a second
interface entry, e.g.

```json
"skills_alt": {"id": 320, "minComponents": 18,
               "components": {"root": 0, "frame": 1, "builder": 2, "spare": 17}}
```

which is a straight transcription of struct 21293 params 3514..3517
(320:0, 320:1, 320:2, 320:17).

**What to look for in the live client, in one attempt.** Log in with the default
(1466) and check, in this order:

1. **Nothing at all in the stats slot** (the panel area is blank or the old
   backpack shows through). The panel did not attach: check the session log for a
   `skipped slot skills` line (table) or a `Dropped sendInterface(...)` line
   (resolver). This is not the 1466/320 question.
2. **A visible but empty frame** - borders, the bottom bar, no 29 skill cells.
   The panel attached but the builder never ran. Expected cause: the stat burst
   did not arrive (check `sendSkillLevel` in the session's `sentByMethod`; if it
   is in `noopsByMethod` instead, Area A's `UPDATE_STAT` promotion is missing).
   Still not the 1466/320 question.
3. **Cells render but are the wrong size / clipped / off-screen, or the panel
   vanishes after a window resize.** That is the geometry in packets 3-7, not the
   interface choice. Relaunch with `-Dataraxia947.skillsLayout=true` and adjust
   the `skillsLayout.*` properties (see section 4); no rebuild is needed.
4. **The panel renders, the 29 cells are laid out, but the numbers are blank or
   the bottom bar shows no total/combat level.** *This* is the 1466-versus-320
   symptom: the client is in the mode where `13749() == 1` and the frame scripts
   built the struct-declared panel (320) while the server attached 1466, so the
   cells 8489 created belong to a component tree the mode does not draw into.
   Fix: add the `skills_alt` entry above and relaunch with
   `-Dataraxia947.skillsPanel=skills_alt`. If the numbers appear, 320 is the
   right panel for this client mode and the table default should change.
5. **Both panels render at once** (two stacked stat lists). The server attached
   one while the client's own frame script attached the other. Record which one
   carries live numbers - that one is the answer - and leave the other unbound.

Record the outcome in `MIGRATION-BACKLOG.md` section 8b so the CANDIDATE row can
be promoted or retired in one pass.

## 4. Geometry: the one thing here that is a choice, not evidence

`SKILLS_TAB.md` section 1 and section 8: the skills wrapper `1477:298` is a
hidden 224x288 layer at 0,0 with xMode/yMode 2 under `1477:60` - byte for byte
the same shape as the backpack wrapper `1477:101` and the equipment wrapper
`1477:112`. `EQUIPMENT_CONTENT.md` proved (with a live probe) that such a slot is
restored to hidden/zero-size on the next window resize unless the server both
sets the geometry and saves it through 8707/8708. Section 8 of `SKILLS_TAB.md`
says the same sequence "is expected to be required" for slot 0 but was not
re-tested.

The numbers this milestone would send, and does not send by default:

```
size     224 x 360, modes 0, 0     (the EQUIPMENT panel's live-verified box, not the skills wrapper's)
position 456, 80,   modes 2, 2     (bottom/right anchored, one panel width left of equipment)
preset   8                          (client variable 4108, EQUIPMENT_CONTENT.md)
```

None of that is evidence for slot 0. 224x360 is the box the EQUIPMENT slot passed
a live maximise/restore/maximise check with; the cache geometry recorded for
`1477:298` itself is 224x288 at 0,0. The position is arithmetic: equipment sits at
`232, 80` with the same anchors, so 232 + 224 = 456 puts the stats panel
immediately to its left instead of on top of it. Nothing in the verified set fixes
an x/y for slot 0, and script 8708 would persist whatever is sent into layout
preset 8 - which is why the whole recipe is behind
`-Dataraxia947.skillsLayout=true` and each number behind its own
`-Dataraxia947.skillsLayout.<name>` property. Symptoms of a wrong choice, once the
flag is on, and the fix:

- panel drawn over the equipment panel -> the two x values collided; raise
  `SKILLS_X`;
- panel half off the left edge on a small window -> `SKILLS_X` too large for the
  mode-2 anchor; lower it or switch the x mode;
- panel is present but 0x0 or disappears after a resize -> the `8707`/`8708`
  pair did not run; check for a `skipped script layout_save_working` line.

The constants live together at the top of `Native947StatsUi`, are the only
numbers in the file that are not table lookups, and each reads its own system
property so a live session can iterate without a rebuild.
`Native947StatsUiTest` asserts the exact argument list of all five scripts in
natural (script-parameter) order - the writer sends the values reversed, so a
swapped pair, the attach hash where the wrapper hash belongs, or a
`(preset, slot)` argument inversion would otherwise pass unnoticed.

## 5. Deliberately not done here

- **XP tracker (enum 7716 key 1015) and XP popups (key 1026).** Both are
  CANDIDATE: the struct wrappers are known but the interface opened in the slot
  is not. `Skills.init()`'s `refreshXPPopUp()` would bind 1213 into key 1026 and
  unhide its wrapper; that is why the M3 UI bootstrap does not call `Skills.init`
  itself and leaves the stat burst to the session.
- **The run-click gate, varc 1413.** `ORBS_AND_VARS.md` 4.1: no cs2 script in the
  cache writes it, and it only suppresses the client's optimistic flip. The orb's
  visual state follows varp 463 through 1316 -> varc 119 -> 1741 either way, so
  nothing is sent for it. If the live orb refuses to flip on click while the
  server-sent varp 463 still repaints it, that is the missing gate and a single
  `CLIENT_SETVARC_SMALL 1413 = 1` at login is the experiment.
- **`IF_SETEVENTS` on the skills cells.** The 910 server sent
  `IF_SETEVENTS 1466:7, 0..27, 30`. The 947 tab has 29 rows and the event mask is
  server policy, not cache evidence, so no event mask is enabled: the skill cells
  are display-only until M11 needs their menus.
- **Registering the handoff's own slots** (game view, minimap panel, backpack,
  equipment, chat) with `InterfaceManager.registerNativeOpen`. Only the two
  interfaces this class opens are registered, because only those two were opened
  by Java.

## 6. Verification performed

- `javac` (Java 8, ISO-8859-1) of `Native947StatsUi`, `Native947Interactions` and
  the new test against the project runtime classpath.
- `modern947.Native947StatsUiTest`: 9 tests, all green - the exact ordered frame
  sequence with the layout flag on (opcode, interface id, component hash, cs2
  script id AND every argument value in natural order per packet), the default
  login sequence with the flag off (no `RUNCLIENTSCRIPT` at all), idempotence, the
  default emitter emitting no state, an undeclared slot being skipped rather than
  guessed, a null table emitting nothing, a resolver-rejected interface becoming a
  counted drop that also leaves `InterfaceManager` unregistered, a DECLARED
  alternative panel name opening that interface, and an undeclared configured name
  being skipped instead of falling back to the default.
- `Native947InteractionsSmoke` against the real 947 cache, with Area A's table in
  place: `PASS`, and the attach-time snapshot reads

  ```
  strictHits=0, facade(sent=40{sendConfig=3, sendConfigByFile=4,
    sendHideIComponent=3, sendInterface=2,
    sendRunEnergy=1, sendSkillLevel=27}, noops=1, dropped=0{})
  ```

  i.e. exactly the five default bootstrap packets of section 1 (2 `IF_OPENSUB`,
  3 `IF_SETHIDE`) followed by the session's state burst, with zero drops, zero
  strict hits and **no** `M3 HUD bootstrap skipped` line anywhere in the log. The
  log also carries `M3 skills slot opens interface 1466 (binding 'skills')`. An
  earlier packaging of this area also sent `sendExecuteScript=5`; those are the
  layout scripts, now opt-in, and the same smoke re-run with
  `-Dataraxia947.skillsLayout=true` reproduces the 45-packet form.
- `Native947WorldSmoke` against the real cache: `PASS` on every stage,
  `tickFailures=0`, `strictHits=0`, `dropped=0`, `processEntityRuns=100` over 100
  strict ticks. That smoke attaches with a null `Native947Content`, so it has no
  `Native947Interactions` and therefore no HUD bootstrap; it is the regression
  guard, and `Native947InteractionsSmoke` is where the new packets are observed.

### Note for the integrator

An earlier run of `Native947InteractionsSmoke` (before this file settled) showed
the state burst being emitted twice - once by `Native947StatsUi`'s own emitter
and once by `Native947Session.sendNative947LoginState()`. That is why the default
emitter here is `NONE`. If a later change moves the burst back into this class,
delete `sendNative947LoginState()` at the same time; the two must never both run.
