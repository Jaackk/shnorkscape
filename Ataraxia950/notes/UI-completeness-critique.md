<!-- The completeness critic from the 2026-09-07 UI survey: what the seven lenses missed.
     Its findings are folded into UI-PLAN.md; kept here because it is the record of what was
     nearly left out, which is the part a plan cannot show. -->

## Gaps in the seven-lens survey

Every lens looked through the same aperture: interface packets, slots, and the binding table. Three whole subsystems that a player experiences as "the UI" sit outside that aperture and got no coverage at all, and several named areas were touched only on the half that happens to be packet-shaped.

---

### A. Combat HUD: hitsplats, hit bars, head icons — zero lens coverage

**What.** `Native947EntityMasks.java:177` publishes hits with `Collections.<Hitbar>emptyList()` — hit bars are never sent, so nothing in the world has an HP bar. `Native947EntityMasks.java:294-295` counts and drops every NPC hit (NPC mask bit 1 is CANDIDATE in `NPC_INFO_MASKS.md`), so no NPC ever shows damage. `Native947EntityMasks.java:88` documents that the untyped hit form carries damage as a plain byte, so any hit above 255 cannot be represented — a silent correctness cap on a server whose content deals five-figure hits. Head icons (prayer, skull, PvP) are `PLAYER_INFO_MASKS.md:62`, mask `0x1000`, **CANDIDATE**, unimplemented.

**Why it matters.** This is the interface a combat player looks at continuously, and it is the entirety of "target information" and the wilderness/PvP overlay in practice. None of the seven lenses' machinery (enum 7716, the binding table, IF_*) touches it, so it will not be picked up as a by-product of any panel work.

**Investigate.** Promote hitbar layout from `PLAYER_INFO_MASKS.md:183` (already CONFIRMED, just unimplemented); trace the NPC mask bit-1 sink to close the CANDIDATE; determine whether a typed hitmark form exists (a hitmark id table in the 947 cache) so damage stops being byte-capped; resolve mask `0x1000`'s 8-slot blob to head-icon ids.

---

### B. Right-click menus and the CS2 setter layer — the packet census asked the wrong question

**What.** Menu text, option count and cursor are per-component cache fields, not packets: the project's own decoder already recovers `ops`, `opBase`, `optionMask`, `targetVerb`, `onOp`, `opCursor1` (present as keys in `verified/ui/chatbox-verifier-components.json`). I found no op-text or tooltip sender anywhere in the 910 `PacketDispatcher.java` `sendIComponent*` family (`:98` settings, `:177` hide, `:185/:189` transparency, `:201` text, `:431` sprite, `:633` model, `:907` animation, `:2410` colour). In this client era, changing a component's right-click text is a **CS2 operation**, and the server can only reach CS2 through `RUNCLIENTSCRIPT(121)` — which can invoke existing scripts, never author one.

**Why it matters.** The outbound lens concluded `IF_SETEVENTS(35)` unblocks clickability. It unblocks *whether* a component is clickable; it does not decide *what the menu says*. Any Ataraxia-authored panel will show whatever 947's cache put on that component. If no callable script sets ops, server-authored menu text does not exist at any effort level — that is a scope wall, not a backlog item, and nothing in the plan currently tests for it.

Second, unnamed risk: `sendIComponentSettings(interfaceId, componentId, fromSlot, toSlot, settingsHash)` (`PacketDispatcher.java:98`). The outbound lens refuted the *field layout* of IF_SETEVENTS but nobody checked the **bit semantics** of the mask. If 910's option-bit assignment differs from 947's, the demand lens's 490-line `unlockDefaultGameInterface` block is 490 lines of wrong masks, and every panel's clickability must be re-derived per component.

**Investigate.** In the 2,243-entry cs2 registrar table the tooling lens rebuilt, find the handler that writes a component's `ops` array and its `optionMask`, then check whether any *existing* script exposes it with server-supplied arguments. Separately, diff the 910 settings-hash bit layout against the native IF_SETEVENTS consumer's mask reads.

---

### C. Scrolling: the extent, not the position

**What.** The outbound lens found opcode 68 = kind 12, "scroll-shaped" — that sets scroll *position*. The thing that decides whether a list scrolls at all is the container's scroll extent (`scrollW`/`scrollH`, decoded IF3 fields). For any panel the server populates with a variable number of rows — the 131 shops, the generic list interface 275, quest lists, the bank tabs — the client clips to the visible rows unless the extent is grown, and in this era that is again a CS2 setter, not a packet.

**Why it matters.** "The shop opens" and "the shop is usable" differ by exactly this. It is invisible in a screenshot of a short list and fatal for a long one.

**Investigate.** Same registrar table: locate the scroll-size setter opcode and its callable-script reachability. Until then, treat every server-populated list as capped at its cache-default extent and say so in the acceptance checklist.

---

### D. Text entry: three mechanisms, not one

**What.** (1) The chatbox dialogue family — open via `RUNCLIENTSCRIPT` 108/109/110 (`CHATBOX_DIALOGUE.md:224-226`, CONFIRMED), submit via `RESUME_P_COUNTDIALOG(16)` / `RESUME_P_STRINGDIALOG(84)` / `RESUME_P_NAMEDIALOG(119)` (all CONFIRMED in `verifiedClientNames.toml`) — client half solved, server half discarded on the world thread. (2) A **second, different** path: `OPCODE64_STRING_INPUT.md`, opcode 64 via cs2 op 742, keyed to interface `1100:12` — CANDIDATE, unowned, almost certainly the in-panel search boxes (GE, bank search, price checker). (3) A third mechanism the 910 engine expects and which has **no 947 evidence at all**: `sendIComponentInputInteger` / `sendIComponentInputText` (`PacketDispatcher.java:2454, 2460`), in-component editable fields.

**Why it matters.** A plan that closes "text entry" by wiring the chatbox family will still have every search box and every in-panel field dead. Also: I found no evidence anywhere in the tree for an on-screen/soft keyboard path — worth one explicit search and then an explicit Tier-3 declaration rather than silence.

**Investigate.** Identify the owner of opcode 64's screen (interface 1100), and determine whether mechanism (3) has any 947 analogue or must be re-expressed as mechanism (2).

---

### E. Buff and debuff bars — probably the cheapest visible win in the programme, filed as "unclassified"

**What.** The entire buff/debuff surface is driven by exactly two scripts: `BuffDebuffTimersManager.java:39` `sendExecuteScript(4252, mapId, ticks)` and `:41/:62/:78/:101/:108` `sendExecuteScript(10624, mapId, on/off)`. Both exist in the 947 cache (`cache/12/4252.dat`, 4274 bytes; `cache/12/10624.dat`, 97 bytes). `RUNCLIENTSCRIPT(121)` is already CONFIRMED and promoted.

**Why it matters.** No new packet, no new mask, no cache reverse-engineering of a panel — this is argument-signature verification plus one slot binding. The demand lens counted these interfaces in its "unclassified 34" bucket and the sequencing lens filed them under M10-is-hidden. Neither noticed the mechanism is already fully available.

**Investigate.** Decode 4252 and 10624's argument signatures and confirm the 910 `Timer.getMapId()` values are valid 947 buff-bar ids. Related and adjacent: `Native947PacketDispatcher.java:556-560` states only varbit 18797 is verified and "the remaining ~100 game-bar varbits are counted drops" — the game-bar and buff-bar work should be scoped together.

---

### F. XP drop counter — already attempted, already failed, and it uses a different struct param

**What.** `InterfaceManager.java:634` binds it as `setWindowInterfaceByKey(35, 3513, 1215)`. Note **param 3513** — not the 3505 used at `:519-522`, and not among the 3514-3517 the slot lens mapped. The counter also has its own configuration UI: `ButtonHandler.java:1382` → `Skills.java:1172 handleSetupXPCounter(componentId)`. Three dev commands already exist for it (`Commands.java:1027 resetxpcounter`, `:1076 probexpcounter`, `:1133 rebindxpcounter`), which is direct evidence someone hit this and could not resolve it.

**Why it matters.** The slot lens's cache-native slot→interface map rests on params 3514-3517; a live call site using 3513 means the param family is wider than the map assumes, and the map may be incomplete for exactly the slots nobody has tried yet.

**Investigate.** Decode what params 3505 vs 3513 mean in the enum-7716 structs before treating the 3514-3517 map as the slot→interface authority.

---

### G. Legacy vs modern interface mode — a live fork that invalidates the demand denominator

**What.** The 910 engine branches on interface mode and opens **different interface ids** depending on it: `InterfaceManager.java:517, 538, 547, 557, 568, 583` (e.g. 1617 vs 1461, 1503 vs 1460), plus `ButtonHandler.java:1440`. `Commands.java:1112-1126` contains a force-flip-to-modern dev command.

**Why it matters.** The demand lens's headline number — 446 referenced, 342 opened — is a **union across both modes**, and the binding table has no concept of mode at all. Which half is correct depends on a 947 varbit nobody has identified. This silently changes the size of the target and the correctness of every id in it.

**Investigate.** Find the 947 varbit/varc that selects legacy HUD (search the settings interface's cs2 writers). Then declare one mode in scope and put the other in Tier 3 of the acceptance partition, explicitly.

---

### H. World map — a data-serving and inbound-opcode problem, entirely untouched

**What.** The data is present: flat cache index 23 (WORLDMAP, 5 files), 41 (WORLDMAPAREAS, 768), 42 (WORLDMAPLABELS, 140). The 910 side expects `InterfaceManager.WORLD_MAP_COMPONENT_ID = 30` (`:37`, opened at `:1881`) and `PacketRepository.WORLD_MAP_CLICK = 18` (`:113`). Neither is reachable: slot 30 is unbound and the native decoder has no map-click branch.

**Why it matters.** The map is a top-level HUD button a player presses in the first minute. It is also the one surface whose failure mode is a JS5 serving gap rather than a packet gap, so no amount of interface work will reveal it.

**Investigate.** (1) Confirm the running OpenNXT JS5 actually serves indices 23/41/42 to this client. (2) Enumerate the native map-click sender using the same sender-enumeration method that solved opcodes 74 and 64. (3) Establish whether map markers/lodestone state are var-driven (likely) or packet-driven.

---

### I. Keybinds — the 910 side is richer than "no evidence" implies

**What.** `KeyBindActions.java:14-27` defines 14 CTRL combinations bound to gameplay actions; `KeyBindInterface.java:36` names the 10 displayed. More telling: `InterfaceManager.java:1865` `HOT_KEY_BIND_SCRIPT_INDEX_FOR_INTERFACE` has entries for `{1430, 5}` (action bar) and `{1477, 1, 3, 4, 7, 18, 33, 42}` (the HUD root) **commented out** — someone already disabled hotkey script binding for exactly the two surfaces this project cares most about.

**Why it matters.** The inbound lens correctly says there is no 947 key packet. But keybinds in this client era are largely *client-side script hooks* installed on components (`OPCODE64_STRING_INPUT.md` documents script 4369 installed as a key hook on `1100:12` via `op_0375` with signature `iiii`). So the question is not "which packet" but "which script hook, installed on which component, and what does it send" — a different and more tractable investigation than the lens framed.

**Investigate.** Trace the `op_0375` key-hook registration family across the UI script closure and enumerate which client packets those hooks emit.

---

### J. Resize, docking, fullscreen — the re-assert loop

**What.** The layout lens settled preset storage thoroughly, but only *within a fixed window*. Unaddressed: a window resize re-runs the client's own layout engine (script 8411), and the sequencing lens already showed the client's `onLoad` hooks enable events by themselves (`logs/server.out.log:141-142`). Nobody checked whether a resize also re-runs `onLoad` and thereby **discards the server's `IF_SETHIDE` state**.

**Why it matters.** If it does, "send nothing and let the client own layout" (lens 5's strategic recommendation) needs an idempotent re-assert path per panel, and the acceptance checklist's "maximise/restore/maximise" step is not sufficient — it needs a true window resize and a fullscreen toggle.

**Investigate.** One live test: open a panel, hide a sibling, resize the window, re-read visibility. Ten minutes, and it decides whether lens 5's central recommendation survives.

---

### K. Accessibility and UI scaling — absent from the tree and from all seven lenses

**What.** No lens and no file I found addresses UI scale, font size, or colour-blind mode. In this client era these are varc-driven and they **change component geometry**, which collides directly with the layout-preset findings.

**Why it matters.** Not a nice-to-have: if a scaling varc changes the geometry the layout engine computes, then any server-sent geometry (currently live via `Native947StatsUi.java:375-388` and `Native947CacheContent.kt:42-49`) is wrong for every non-default scale, and the "invented skills box" problem is worse than lens 5 stated.

**Investigate.** Scan the settings interfaces' cs2 writers for varcs consumed by script 8411. Confirm none of them invalidate pinned component ids (they should not) — and confirm whether they invalidate the geometry currently being written.

---

### L. Notifications, toasts, and the message-type table

**What.** The outbound lens flagged `MESSAGE_GAME(105)` as unpromoted with types 96/98/99 special-routed. Under-covered: the *full* type→routing table. Ataraxia's own notifications ride chat (`Player.java:5167 sendAfkNotification`). There is no evidence anywhere of a toast/notification-tray surface.

**Investigate.** Enumerate the native MESSAGE_GAME type switch completely (parser already located) and publish a type→routing table before any panel depends on a type. Then declare the toast surface Tier 3 explicitly if the 910 engine has no producer for it.

---

### M. Loot window — a content gap, not a binding gap

**What.** The 910 engine has no dedicated loot-window interface; it has ground items and `LootBeamManager` only. The 947 client's area-loot window has **no 910 producer at all**.

**Why it matters.** It will be discovered late as "why doesn't the loot window work" and misdiagnosed as a binding problem. Declare it Tier 3 up front, or budget it as new 910 content rather than porting.

---

### N. Cross-cutting: the drop census cannot answer "which panel is broken"

**What.** `Native947PacketDispatcher.java:216` buckets drops by *method name*. Live sample: 22 `sendGlobalConfig`, 21 `sendConfig`, 12 `sendIComponentText`, 5 `sendExecuteScript`, 3 `sendIComponentSettings`, 2 `sendInterface`, 2 `sendGlobalString`.

**Why it matters.** The sequencing lens proposes drops as the plan's acceptance instrument. As bucketed, they tell you a method failed, never which panel. Bucketing by `(method, interface id)` is a small change with the largest leverage on the whole programme's measurability.

Also unlisted by the outbound lens's "no evidence" family: `sendIComponentTransparency` (`PacketDispatcher.java:185, 189`) is a sixth interface-mutation family with no opcode candidate named.

---

## The three most likely to be underestimated

**1. The CS2 setter layer (menus, op text, scroll extents) — item B + C.**
The tooling lens bounded this as "name ~200 opcodes, 2-3 weeks." That estimate measures the wrong thing. Naming an opcode does not make it *reachable*: the server can only call scripts that already exist in the cache, so for each setter the real work is proving that some callable script exposes it with server-supplied arguments. If none does, that capability is unavailable at any effort — server-authored right-click text and server-grown scroll extents may simply not exist in this architecture. That possibility is not currently on anyone's risk list, and it silently caps what "fully working" can mean. It is also load-bearing for the majority of panels, not a side quest.

**2. The combat HUD — item A.**
It got zero coverage from seven lenses because it lives outside the interface-packet world entirely, and none of the survey's instruments (slot table, binding table, IF_* promotion, drop counters) will surface it. Today: no hit bars anywhere, no NPC damage at all, damage capped at 255, head icons unverified. It is per-frame, per-entity, high-volume, touches the player-info and NPC-info mask parsers rather than the interface parsers, and needs its own evidence programme with its own vocabulary. Any plan that reports "the UI is done" while an NPC takes no visible damage will be rejected on sight by the owner.

**3. Legacy vs modern interface mode — item G.**
This is underestimated because it is currently invisible: nobody has costed it because nobody counted it. It is a live fork in `InterfaceManager` selecting *different interface ids* for the same function, which means the demand lens's 342-id denominator is a union across two mutually exclusive HUDs. Until the selecting varbit is identified and one mode declared in scope, every coverage percentage in the plan is unfalsifiable, and per-panel work risks binding the ids for the mode the client is not in. The fix is cheap; discovering the fix is needed after fifty panels are bound is not.