# Ataraxia engine -> 950 port plan

Produced 2026-09-09 by a 15-agent derivation (7 dimensions, each adversarially verified, plus a
synthesis that re-derived nine load-bearing claims itself and changed five of them). Everything
here is analysis: no server source, client binary or cache was modified producing it.

## Why this plan exists

The 950 test currently runs the small `opennxt` backend, into which walking, the ribbon, the
Settings window, scene re-centring and a clip map were each reimplemented by hand. That was the
wrong long-run shape. `OpenNXT/runtime/lib/ataraxia-947-1.0-UNTRACKED.jar` already contains the
full Ataraxia engine - 6400 classes, 2820 source files - and it is protocol-agnostic: only 63 of
those files reference the wire protocol at all, one in `content/` and one in `npc/`.

Verified against the original 910 server at `C:\Users\developer\Desktop\Ataraxia-PS`:

| tree | 910 | Ataraxia947 | missing from 947 |
|---|---:|---:|---:|
| content | 1024 | 1024 | 0 (1016 byte-identical) |
| npc | 621 | 621 | 0 (619 byte-identical) |
| game | 1044 | 1097 | 0 (947 adds 53 adapter classes) |
| api | 38 | 5 | 33, all Discord bot commands |

So the 910 content is already in the engine. What is partial is the `Native947*` adapter that
surfaces it to a modern client - and that adapter work is identical whether the target is 947 or
950. Doing it once, against 950, avoids doing it twice.

## Build notes for whoever executes this

- The engine is a **JDK 8** Gradle project (`Ataraxia947/build.gradle`, sourceSets core/content/
  game/network/npc/api). `Build.ps1` supports `-Offline` and uses `GRADLE_USER_HOME` =
  `AstraNXT/.gradle-ataraxia` (126 MB, already populated).
- AstraNXT is read-only. The port must copy the engine source into this project (27 MB, 2820 java
  files) and build it here, with its own copy of the gradle home - Gradle writes locks and caches
  into GRADLE_USER_HOME, so it cannot be shared read-only.

---

# ATARAXIA → 950 PORT PLAN (synthesis of 7 analyses + 14 passes, with my own re-derivations)

## PART 0 — ADJUDICATIONS I MADE MYSELF (spot-checks; these override the inputs where they conflict)

I re-derived nine load-bearing claims directly from `rs2client.exe`. Five of them changed the answer.

**A1. `UPDATE_INV_FULL(9)` / `UPDATE_INV_PARTIAL(50)` item field widened u16 → u24 BE. — packets-wire is RIGHT; protocol-opcodes analyst AND its verifier are BOTH WRONG.**
This is the most consequential disagreement in the whole input set: dimension 1 lists both packets under "unchanged — instruction-identical, needs no body change"; dimension 2 says the item field grew a byte. I disassembled both.
- 950 `0x1400fd060` entry loop: `add qword [rbx+0x18], 3` (0x1400fd120), then `[rdx+r11-3]<<8 + [rdx+r11-2]`, `<<8`, `+ [rdx+r11-1]` (0x1400fd131–0x1400fd152) = **big-endian u24**; `lea r8d,[r10-1]` (0x1400fd1b8) confirms the +1 bias survives; amount byte and the 0xFF→BE i32 escape unchanged.
- 947 `0x1400fd7b0`: `lea r9,[rax+2]` / `movzx edx, word [r10+rax]` / `rol dx,8` (0x1400fd878–0x1400fd88d) = **u16**.
Dimension 1's "identical for 30 instructions" was a prologue-only comparison. Shipping u16 desynchronises every entry after the first in the frame. **Both inventory writers change.**

**A2. `CLIENT_SETVARCSTR_LARGE(81)` id is little-endian with NO +128. — verifier RIGHT, analyst WRONG.**
950 op81 `0x140141743`: `movzx r8d,[r9-1]; movzx eax,[r9-2]; shl r8d,8; add r8d,eax` → the *later* byte is the high byte, no bias anywhere. 947 op67 `0x1401409b3`: `movzx eax,[r9-1]; add eax,-0x80; movzx eax,[r9-2]; shl eax,8` → BE with low byte +128. Not instruction-identical. Server writes `string, (byte)id, (byte)(id>>>8)`.

**A3. The `size()` column does NOT port verbatim.** 950 descriptor table: `30 = -1`, `81 = -2`; 947: `15 = -2`, `67 = -1`. The two SETVARCSTR rows swap frame width. `Packet.frame()` branches on `size()` to emit a u8 vs u16 length prefix, so copying the column mis-frames the stream. packets-wire analyst's "every descriptor size is unchanged" is refuted by its own verifier and by the tables.

**A4. `UPDATE_STAT(92)` field reversal is real.** `0x1401412c1` byte0 `neg al` → ebp, which scales the 24-byte stat entry (`lea rcx,[rbp*2]; add rcx,rbp; lea rbx,[rcx*8]`, 0x140141306) ⇒ **skill**; `0x1401412cf` byte1 `neg al` → r14d stored at entry+0x14 (0x14014132e) ⇒ **level**; `lea rax,[r8+4]` / `bswap edx` (0x1401412e4–0x1401412f2) ⇒ **experience BE at b2..b5**. 947 sent experience first. Confirmed.

**A5. `VARBIT_SMALL(28)` field order reversed.** 950 `0x1401421fd`: value byte at cursor+0 with `add al,0x80`; id at +1/+2 as `(b2<<8)+b1` (0x14014220f/0x140142215/0x14014223c). 947 `0x1401410e0`: id first at +0/+1, value last. Confirmed — and I confirmed the same run of instructions carries the **varbit provider gate** `cmp dword [rax+0x3c], 4; je` at 0x140142226 that silently no-ops the write on the wrong provider kind.

**A6. `REBUILD_NORMAL(63)`'s parser is `0x1400f6e10`, not `0x1400f6750`.** player-info analyst cited the wrong function (it is opcode 5's, also size -2, also prefix-consuming). At 0x1400f6e10 I read: gate `cmp byte [rax+0x49],0` on `[[client]+0x198b8]` (0x1400f6e3e) → `call 0x140125570` (0x1400f6e4b) → header from 0x1400f6e50: b0..b1 `b0<<8 | (b1-0x80)` ushort128; b2 plain (later hard-checked ==5); b3 `add al,0x80` ubyte128; cursor jumps +4 while only `word [r9+rcx+2]` (b6..b7) is read ⇒ **b4/b5 skipped, b6..b7 plain BE**. The brief's 18-byte header is exactly right; only the citation was wrong.

**A7. NPC_INFO addition-record reorder confirmed.** Bit-reader (`0x1400fef30`) widths in `0x14011fd20`, in execution order after index16: `[r14+0xc0e8]`, `1`, `3`, `0x10`, `2`, `1`, `[r14+0xc0e8]` (0x1401200aa → 0x14012017d) ⇒ **index16, dy[npcBits], immediate1, facing3, type16, plane2, mask1, dx[npcBits]**. 947 was index16, dy, plane2, dx, type16, imm1, facing3, mask1. Same total width — silently garbles rather than desyncs.

**A8. GPI local-movement bit widths unchanged.** `0x140126030`: 1, 2, then 1 / 3,1 / 2 at 0x14012604f, 0x140126075, 0x14012615b, 0x14012619b, 0x1401261ab, 0x140126236 — matches the 947 shape. The player-info dimension's central thesis (GPI bitstream identical, only the *masks* and *opcode* moved) holds.

**A9. Every (opcode, size) pair in the plan below re-checked against `950-server-sizes.toml` / `950-client-sizes.toml`.** All 35 server rows and all 36 client rows agree. No exceptions.

**Other adjudications, decided on evidence quality without re-deriving:**
- **Var family (8 packets):** dimension-1's verifier and dimension-2's analyst derived these *independently* and agree field-for-field on all eight. Two independent derivations + my own VARBIT_SMALL check ⇒ **VERIFIED**.
- **`+0x40` struct shift is not corroboration.** Dimension-1's verifier is right: the shift is global and uniform, so it cannot discriminate between candidates. Every opcode claim below rests on a *discriminator* (model-kind 3 vs 5, property index 7 vs 0x15, `mov` vs `movsxd`, the music-stream tail, the descriptor table), never on +0x40.
- **`IF_SETPLAYERHEAD=38` (not 101)** and **`IF_SETHIDE=67` (not 85)**: analyst-over-score, upheld by both verifiers with independent discriminators. Note op101 also reads its hash as intv1 vs op38's plain BE — a second discriminator.
- **`IF_SETCOLOUR(83)` vs `IF_SETSCROLLPOS(16)`:** structurally degenerate (both size 6, both intv1 + 2-byte trailer; only the +128 bias on 83's trailer differs). Neither is verified. 83 is *likely* (shape matches the known IF_SETCOLOUR field list); 16 is a **GUESS**. Neither ships before Stage 4.
- **`Viewport.kt` "hazard":** packets-wire's claim that the 950RevTest project unconditionally prepends 5.1 KB is **wrong** (handoff verifier is right: `Viewport.createPacket()` is header-only; the prefix is written once in `bootstrapNativeWorld`). Do not "fix" it.
- **Interface renumbering: TWO shifts, not one.** 1465 is +1 for ids ≥ 9 (run orb 14→15). **1430 is +2 for ids ≥ 2** — missed by the handoff analyst, found by its verifier, and it invalidates six `ui-bindings` action-bar component ids *and* their digests. Also: 1477 gained an id at 819 (everything ≥819 shifts +1), 1475 shrank 60→59 and is **not** a clean shift, and the world-map pin digest must be **regenerated** (`3/1465/11` = `7c8b227a…`, not the 947 `94bb6e9e…`).

---

## PART 1 — THE LEDGER: VERIFIED vs GUESS

### VERIFIED — safe to send (opcode + size + full body layout, all with instruction-level evidence)

**Outbound.** Opcode(size) — body change:

| Packet | 947→950 | Body |
|---|---|---|
| IF_OPENTOP | 94→**1**(19) | CHANGED: b0=id, b1=id>>>8 (plain LE), b2..b18=0 |
| VARP_LARGE | 111→**4**(6) | CHANGED: b0=(id+128), b1=id>>>8, b2=v>>>8, b3=v, b4=v>>>24, b5=v>>>16 |
| UPDATE_RUNWEIGHT | 108→**7**(2) | same (BE i16) |
| UPDATE_INV_FULL | 69→**9**(-2) | **CHANGED: item field is BE u24** (A1) |
| UPDATE_RUNENERGY | 116→**21**(1) | same |
| RESET_CLIENT_VARCACHE | 48→**23**(0) | same |
| IF_SETEVENTS | 35→**24**(12) | CHANGED: mask intv1, toSlot BE u16, fromSlot ushort128, parent intle; 0xFFFF→-1 |
| VARBIT_SMALL | 50→**28**(3) | CHANGED: b0=value-128, b1=id, b2=id>>>8 (A5) |
| CLIENT_SETVARCSTR (byte frame) | 15→**30**(-1) | id-first: b0=id>>>8, b1=id+128, then NUL string |
| UPDATE_REBOOT_TIMER | 52→**31**(2) | same |
| MESSAGE_GAME | 105→**33**(-1) | same |
| RUNCLIENTSCRIPT | 121→**35**(-2) | same (see GUESS note) |
| PLAYER_INFO | 27→**36**(-2) | GPI bitstream same; masks + appearance changed |
| IF_SETPLAYERHEAD | 81→**38**(4) | CHANGED: plain BE u32 hash |
| CLIENT_SETVARCBIT_SMALL | 115→**48**(3) | CHANGED: b0=128-v, b1=id>>>8, b2=id |
| UPDATE_INV_PARTIAL | 5→**50**(-2) | **CHANGED: item field is BE u24** (A1) |
| REBUILD_NORMAL | 90→**63**(-2) | CHANGED: b0..1 chunkY ushort128, b2=5 plain, b3 npcBits ubyte128, b4/5=0, b6..7 chunkX BE, then areaType/hash1/hash2 (A6) |
| IF_SETHIDE | 103→**67**(5) | CHANGED: b0..3 hash intv1, b4 = flag plain (no +128) |
| IF_CLOSESUB | 33→**69**(4) | CHANGED: hash intv2 (b0=h>>>16, b1=h>>>24, b2=h, b3=h>>>8) |
| VARP_SMALL | 10→**79**(3) | CHANGED: b0=value sbyte plain, b1=id>>>8, b2=id+128 |
| NPC_INFO | 12→**80**(-2) | retained/mask sections same; **addition record reordered** (A7) |
| CLIENT_SETVARCSTR (short frame) | 67→**81**(-2) | string-first, then id **plain LE** (A2) |
| VARBIT_LARGE | 71→**82**(6) | CHANGED: b0..3 value BE, b4=id, b5=id>>>8 |
| CLIENT_SETVARCBIT_LARGE | 55→**87**(6) | CHANGED: b0..3 value LE, b4=id>>>8, b5=id+128 |
| UPDATE_STAT | 66→**92**(6) | CHANGED: b0=-skill, b1=-level, b2..5 exp BE (A4) |
| IF_OPENSUB | 8→**100**(23) | CHANGED: b0..3 parent BE u32, b4..15=0, b16=id+128, b17=id>>>8, b18=128-walkable, b19..22=0 |
| MUSIC | 87→**107**(5) | CHANGED: b0..3 id plain BE, b4=128-volume |
| IF_SETTEXT | 2→**115**(-2) | same |
| CLIENT_SETVARC_LARGE | 112→**119**(6) | CHANGED: b0=id>>>8, b1=id+128, b2=v>>>16, b3=v>>>24, b4=v, b5=v>>>8 |
| CLIENT_SETVARC_SMALL | 1→**126**(3) | CHANGED: b0=id, b1=id>>>8, b2=128-value |
| WORLDLIST_FETCH_REPLY | 159→**129**(-2) | same |
| SERVER_TICK_END | 195→**160**(0) | empty |
| NO_TIMEOUT | 216→**183**(0) | empty |

**Inbound.** Framing: single-byte ISAAC-offset opcode (**delete the `>=128` two-byte-extension branch** — the client never emits it, in either revision, and 950 opcode 128 is live), then fixed / u8 / u16-BE length counting only the bytes after itself.
- walk **88**(5): b0..1 y BE plain, b2 = mod+128, b3 = (x&255)+128, b4 = x>>8 — note the modifier sign *flipped* vs 947.
- minimap walk **78**(18), keepalive **104**(0), WORLDLIST_FETCH **108**(4), map-build report **98**(4).
- IF_BUTTON 1..10 = **18,122,89,100,81,126,49,66,31,59** (all 9): b0..2 item BE u24, b3..6 hash [h>>>16, h>>>24, h, h>>>8], b7..8 slot BE u16.
- Object 1..6 = **34,48,24,41,73,79** (9): b0=(y&255)+128, b1=y>>8, b2..5 id BE u32, b6=x>>8, b7=(x&255)+128, b8=-modifier. Target variant **90**(18).
- NPC 1..6 = **60,92,27,107,13,36** (3): b0 = modifier **plain 0/1**, b1=index>>8, b2=(index&255)+128. Target variant **19**(12).
- Player 1..10 = **20,46,71,39,37,94,51,63,117,58** (3): b0=index&255, b1=index>>8, b2=mod+128.
- Ground item 1..6 = **127,103,22,56,52,113** (8): b0=128-(mod|flag2), b1..2 x BE, b3..4 y BE, b5..7 item BE u24. Target variant **112**(17).
- Drag **12**(18), dialogue click **101**(6), count dialog **120**(8), pause **11**(0), close modal **5**(0), public chat **87**(-1), private chat **72**(-2), music ended **110**(4), window report **9**(6). Count-int **114**(4), **10**(2), **32**(2) — *trap: 947 op 32 → 950 op 114, while 950 op 32 is a different packet.*

**Player masks (PLAYER_INFO).** Bits: SAY 0→**6**, TRANSFORM 5→**2**, ANIMATION 7→**3**, FACE_COORD 3→**7**, FORCE_MOVEMENT 12→**14**, NAME **18**, COLOUR_TINT **28** *(NPC masks)*. Player masks: APPEARANCE 0x4→**0x20**, ANIMATION 0x40→**0x8**, FACE_ENTITY 0x20→**0x80**, HITS 0x8→**0x40**, FORCE_TALK 0x10000→**0x400000**, FORCE_TALK_LOCAL 0x8000→**0x400**, FACE_ANGLE 0x80→**0x2**, FORCE_MOVEMENT 0x10→**0x1**, COLOUR_OVERLAY 0x800000→**0x200000**, SPOTANIM **0x4000000** (unmoved). Header markers 0x1/0x4000/0x40000 → **0x10/0x8000/0x40000**. Emit order (positional — the client parses in this order): spotanims, animation, faceEntity, hits, forceTalk, appearance, forceMovement, colourOverlay, forceTalkLocal, faceAngle. Seven of ten blocks changed byte transforms (forceTalk / forceTalkLocal / faceAngle are unchanged).
**Appearance body:** wearpos slots become unsigned LEB128 (kit base **2**, item base 0x800, 0 = empty, **1 = npc-morph escape** — replaces the 947 0xFFFF sentinel), and every body byte is written as `b ^ 0x80`, under mask bit **0x20** with the length byte still `len+128`.

### GUESS — must not be presented as ready to send
1. **`IF_SETSCROLLPOS(16, 6)`** — rests on a cs2 name-string reference; shape-degenerate against 83. Do not send.
2. **`IF_SETCOLOUR(83, 6)`** — likely (intv1 + ushort128 matches the known field list) but the sink is untraced.
3. **`RUNCLIENTSCRIPT(35)` "unchanged"** — inherited from the brief; the 950 parser's stack frame grew 0x40 bytes vs 947, which is exactly where an argument-count change would show. Fine for short arg lists; walk the loop before sending a long one.
4. **Second HITS block at bit 0x2000000 / player, and NPC bit 33** — real, structurally identical, *different* selector tables. Never emit; keep the refusal on **both**.
5. **`MESSAGE_GAME` / `MUSIC` / `NPC_INFO` / inventory opcode *names*** — the opcode↔parser bindings are certain; the symbolic names are meaning-derived conventions, not recovered symbols.
6. **`NO_TIMEOUT=183`** — chosen by descriptor size from a 3-way shared-stub tie (88/-1, 172/-2, 183/0). Correct by elimination, unverified against the timeout counter.
7. **`ClientProfile` UPDATE number for 950** — not derivable; leave unknown, never guess 1.
8. **`Skills.SKILL_COUNT == 29` on 950** — unsettled. The 950 stat-defaults table is the same 785 bytes but a different digest.
9. **Every masked block still marked CANDIDATE in 947** (head icons, typed record maps, body-part transforms, NPC hits) — bits remapped, semantics still untraced.
10. **23 inbound opcodes with no derived 950 equivalent** in the *current* Actions file's vocabulary — resolved above for objects/npcs/players/ground items/drag/dialogue, but the ~13 dummy/discard blocks and the camera/focus reports (950 ops 97, 128, 75) are unnamed.
11. **147 of 230 cache SHA pins** and **all 83 script pins in `ui-bindings`** — stale, and "stale" ≠ "rehash": equal-length-different-bytes is the signature of a recompile with renumbered operands.

---

## PART 2 — STRUCTURE DECISION

**Parallel `modern950` package + one `ClientProfile` constant.** Not a build-conditional layer, not an interface abstraction.
- The engine already has exactly one polymorphic seam for outbound traffic (`Native947PacketDispatcher extends PacketDispatcher`, installed per-Player at `Player.java:1836`). Adding a sibling costs nothing.
- The 93–96 `isNative947()` call sites across ~27 files all mean *"native client, skip the 910 path"*. Widen the predicate to `isNativeModern()` (`profile != LEGACY_910`) and **none of them needs an edit**.
- A build-conditional layer would put a branch inside the GPI/NPC-info bit writers — the hottest loops on the server — for no benefit. An interface abstraction would have to abstract 8 files of raw bit-twiddling whose *shapes* genuinely differ (LEB128 vs u16 slots, different mask bits, reordered records).
- The opennxt backend stays the fallback throughout: it is selected by `OPENNXT_GAME_BACKEND`, which is the only switch that turns the Ataraxia path on at all. Add token `ataraxia950`; leave `ataraxia947` working.

---

## PART 3 — THE STAGED PLAN

Every stage is independently testable against a live 950 client, and every stage leaves opennxt selectable by env var.

---

### STAGE 0 — Scaffolding (no wire changes)
**Gain:** nothing user-visible. This is the stage that makes every later stage a one-variable experiment.

**Files:**
- `OpenNXT/src/main/kotlin/com/opennxt/net/login/Ataraxia947Handoff.kt` → `Ataraxia950Handoff.kt`: `supportsClientBuild` = `major==950 && minor==1` (live capture `10 02 ba | 00 00 03 b6 | 00 00 00 01`); `OpenNXT.config.build != 950`; `enabled` accepts `ataraxia950`; `playerSavePath` requires `modern950/players`; fix the OUT_OF_DATE log string. Note this file **imports the wire package** (`Native947Packets`, and `interfaceBootstrap()` returns `List<Native947Packets.Packet>`) — it moves with the port.
- `Native947Save.CLIENT_REVISION = 947` → 950 (`Native947SaveStore.java:205` writes it, `:312` throws on mismatch). Bump `SCHEMA_VERSION`.
- `ClientProfile.java`: add `NATIVE_950(950, /* update unknown */ 0)` + a `"950"` parse branch. `getRevision()/getUpdate()` have no consumers; this is a pure identity tag.
- `Player.java:952`: `isNative947()` → `isNativeModern()` (profile != LEGACY_910), keep a deprecated delegating alias so the 93 call sites compile untouched.
- Copy `network/com/rs/network/protocol/modern947/` → `modern950/` verbatim (8 files, 4360 lines). Retype the 19 non-smoke adapter files under `game/com/rs/game/player/client/` to the new package.
- Add a **`ataraxia.native.verifyCache` kill switch** that turns every `verify()` / `pin()` gate into a WARN. Without it the 157 stale SHA pins fail-closed and you cannot reach a login prompt.

**Acceptance:** `OPENNXT_GAME_BACKEND=ataraxia950` + a 950 client → server log shows `authorize` returning null, `complete()` reserving a slot, pipeline surgery, and `Native950World.attach` succeeding. The client will then hang or fault — that is expected. `OPENNXT_GAME_BACKEND=opennxt` still works identically.

---

### STAGE 1 — An Ataraxia-backed player, standing in the 950 world, with the right body
**Gain:** the whole Ataraxia engine is now driving a live 950 session. Everything downstream is content, not plumbing.

**Files & changes:**
1. `Native950Protocol.java` — 35 opcode constants from the VERIFIED table, sizes copied **per-row from `950-server-sizes.toml`, not as a column** (A3). Replace `CLIENT_SIZES` with the 129-entry 950 table (opcodes 0..128) — 112 of the overlapping opcodes have different lengths; leaving it 947 mis-frames essentially every inbound packet. BUILD=950, new `ORIGINAL_CLIENT_SHA256` = `fc7492548627a4068e88a7a45ba4f0be93004adf246bb4b174b24ae588768b36`, UPDATE omitted/unknown.
2. `Native950InboundDecoder.java` — **delete the `decoded >= 128` two-byte-opcode branch and the `opcodeHigh` field.** ~~Also clamp the variable-length allocation to the client's own caps (0x104 for -1, 0x2710 for -2).~~ **RETRACTED 2026-09-09** — neither number is a length. 0x104 is unreachable (a one-byte length cannot exceed 255) and the sole 0x2710 in the parser range is `0x140149759 add rax, 0x2710` on a millisecond clock, i.e. a 10-second timer. Capping a two-byte length at 10000 would have dropped legitimate traffic. No cap beyond the field width.
3. `Native950Packets.java` — for Stage 1 only: `sceneHeader` (new 18-byte order), `tickEnd`, `keepAlive`.
4. `Native950PlayerInfo.java` — **no bit-layout edits** (A8). Two one-line fixes: add the `index == view.localIndex` guard to the `changedOccupant` branch of `processLocal` (the client refuses to remove its own slot and returns after 4 bits where the encoder writes 5 — one stray bit); and correct the `initialScene` speed-token comment (the client seeds the local slot to `&table[0]`). Re-cite ~40 client VAs.
5. `Native950PlayerMasks.java` — appearance only for this stage: mask bit **0x20**, length byte `len+128` unchanged, body written as `b ^ 0x80`, header markers 0x10/0x8000/0x40000.
6. `Native950Appearance.java` (login seed, writes at lines 56/59/61) **and** `GlobalPlayerUpdater.buildNative947AppearanceData` (:247/:253/:255) — LEB128 varint slots, kit base **2**, item base 0x800. Both, or every equip/kit change stays broken. Add the LEB128 helper once and share it.

**Acceptance:** log in on the 950 client. You see your own avatar at 3222,3222 wearing the seeded kit; no fault at `0x140131d53`; `SERVER_TICK_END` loop stable for 60 s with `NO_TIMEOUT` and inbound keepalive (104) round-tripping. Byte-exact unit vectors first: `00 7F F4` (idle) and `C0 7F F4 00 00 20 <len+128> <body^0x80>`.

**Highest-risk item in the entire port lives here:** wrong appearance framing does not degrade, it **deterministically faults the client** (the biased empty-slot byte 0x80 keeps the varint consuming). There is no partial credit. Write the LEB128 encoder against the verified 48-byte vector before you send anything.

---

### STAGE 2 — Movement, both directions
**Gain:** a player who can walk and run anywhere on the server's clip map, driven by Ataraxia's pathing.

**Files:** `Native950Actions.java` (walk 88 with the *flipped* modifier sign and the new byte order; minimap 78; keepalive 104), `Native950PlayerInfo` (no change — the walk/run/relocate forms are verified unchanged), `Native950Session` (repeat-rebuild path is already header-only and already correct).

**Acceptance:** ground-click walk, run, and minimap walk all move the avatar; server and client positions agree after 50 steps; a second `REBUILD_NORMAL` on a region change is header-only and does not desync (the GPI gate byte is cleared by the first rebuild). Add a scene-window-aware visibility policy: the window is fixed at 256 tiles, base `(chunk-16)*8`, and there is **no verified behaviour** for adding an actor outside it.

---

### STAGE 3 — The interface bootstrap: ribbon, tabs, skills, vars
**Gain:** the first stage where 910-era *content* becomes visible. Skills, the ribbon, the run orb, world map button, settings gear — the shell that every piece of Ataraxia content opens into.

**Files:**
- `Native950Packets.java` — the remaining UI writers, **all rewritten from the VERIFIED table**: `openTop`, `openSub`, `closeSub`, `hideInterface`, `interfaceEvents`, `interfaceText`, `interfacePlayerHead`, `runClientScript` (unchanged body), `updateStat`, and **all eight var-family writers plus `varcString`**. That is 20 changed writers, not 10 — the var family is the highest-traffic group in the protocol and every one of them moved.
- `Native950InterfaceBootstrap.kt` / `Native950Ribbon.kt` / `Native950RunOrb.kt` / `Native950ActionRouter.java` — **run orb 1465:14 → 1465:15 in all four places, atomically** (`ActionRouter.verified()` throws `IllegalStateException` if the literal and the JSON disagree). World map 1465:10 → **1465:11** *and* a regenerated pin `7c8b227a1ebffc100be5728550972ce77a1add8401f97c63e43b13fa4eb3c45b`.
- `resources/native950/ui-bindings-950.json` — new file, all-or-nothing loader. Minimap components +1 (run_orb 15, ring 16, energy_bar 18, icon 19, text 20); interface-level `sha256` (= component 0) carries across unchanged. **Action bar 1430 components +2** (hp_bar 9, prayer 16, summoning 22, auto_retaliate_icon 56, adrenaline 57, button 59), all six digests regenerated. 83 script pins regenerated.
- `Native950Bindings.java` — RESOURCE/REVISION → `native950` / 950.

**Acceptance:** ribbon renders with the right actors; skills tab shows every level and XP correctly (this is the `UPDATE_STAT` acceptance test — a wrong field order writes experience into the skill index); run orb toggles and the client echoes IF_BUTTON `ffffffb9050f00ffff` = 1465:15; `RESET_CLIENT_VARCACHE` + the six mode varbits + 18797 leave the client in modern mode.

**Cheap oracle:** `WorldPlayer.kt:3239-3271` in the 950RevTest project already ships this exact policy live. Diff against it.

**Trap:** varbit writes silently no-op when the definition provider is the wrong kind (`cmp dword [rax+0x3c], 4` at 0x140142226 — I confirmed this gate exists). A varbit that "doesn't work" may be a provider-state problem, not an encoding problem.

---

### STAGE 4 — Inventory, equipment, and clicking things
**Gain:** items. Backpack, worn equipment, bank, and every interface option Ataraxia's content binds — the point at which 910-era content starts actually running.

**Files:** `Native950Packets.inventoryFull` / `inventorySlots` — **3-byte medium instead of u16** for the item field (A1); relax the `> 65534` guard cautiously (the client-side ceiling is underived). `Native950Actions.java` — IF_BUTTON (u24 item, new hash order, 9 bytes), drag (opcode 12, 18 bytes, no +128 anywhere), dialogue click (101, fields swapped), object/npc/ground-item/player option families. **Item sentinel:** accept **both** `0xFFFFFF` and `0x00FFFF` as absent — the 24-bit "absent" value is *not* verified (the client applies no fold to the item field, unlike every slot and hash field, which are visibly folded to -1).

**Acceptance:** backpack shows the right items in the right slots after a full and after a partial update; item-on-item drag fires with correct src/tgt; each of the ten IF_BUTTON options routes to the right option index; object and NPC options resolve to the right target and the right option.

---

### STAGE 5 — NPCs
**Gain:** 621 files of NPC content, spawns, and combat targets become visible.

**Files:** `Native950NpcInfo.addRecord()` — **the eight-field reorder** (A7), one method. `Native950NpcMasks.java` — 7 mask bits + 4 marker constants + the 7-line emit order + 4 changed transforms (animation delay loses its +128; faceCoordinate x becomes plain BE; forceMovement's 6 bytes and 3 shorts; colourTint's 6 fields). Keep `Update.hits()` throwing for **both** NPC bit 5 and bit 33. `resources/native950/id-validity-950.json` regenerated (every data-driven spawn is gated on it).

**Acceptance:** land the addition reorder first and prove it with `00 00 01 FB 00 3D C0 10` — an NPC of type 494 must appear at exactly (+2,-3) from the viewer. Then one mask block at a time, starting with SAY (`01 9F FF E0 00 00 40 <text> 00`), which exercises the header, the alignment and the two skipped bytes with zero selector risk.

**Ordering requirement:** PLAYER_INFO must be sent before NPC_INFO in the same tick — the NPC offset base reads the local player's queued path destination. And `REBUILD_NORMAL` must precede the first `NPC_INFO`: `npcBits` lives at `npcMgr+0xC0E8` and is written *only* by the rebuild, with **no gate** protecting NPC_INFO from running before it.

---

### STAGE 6 — Other players
**Gain:** a multiplayer world. Everything from Stage 1–5 was single-occupant.

**Files:** `Native950PlayerInfo` (already correct — but nothing in the four-pass state machine has ever been exercised on either revision), `Native950PlayerMasks` (the remaining nine blocks, in the new positional order), `Native950Actions` player-option family.

**Acceptance — this stage is 80% test-writing:** byte-exact unit vectors for two locals where one skips and one moves; a skip run ending exactly at a pass boundary; an external add with each of the three nested region forms; an add plus a mask block (pins the two-zero-byte prefix and the pending-mask ordering); a removal; an index-reuse removal at a non-local slot. Then two real clients in view of each other for 10 minutes.

**Why the tests are the deliverable:** at every pass boundary a leftover skip counter aborts the movement parser at `0x140125fe9` — and it aborts *after* the byte cursor has been written back and after the world mutations of the completed passes are committed, and *before* the rotation loop that swaps the pass flags. So one stray bit gives you partial world mutation, a permanently wrong pass membership from the next frame on, and a mask section parsed from the middle of the movement bitstream. It is not a clean bail-out; assert on committed state, not just on bytes.

---

### STAGE 7 — Chat, dialogue, music, world map, settings
**Gain:** the conversational and menu surface that most of the 1024 content files actually use. Quest dialogue, shops, bank interfaces.

**Files:** `Native950Packets.gameMessage` (unchanged body, opcode 33), `Native950Actions` public chat 87 / private chat 72 / count 120 / string 17 / name 53 / pause 11 / close modal 5, Huffman table re-sourced from the 950 cache (**index 10, group 1, file 0, 256 bytes, sha `77946046…` — I confirmed it exists and that ' '=3, 'h'=5, 'i'=4 bits, matching the 947 worked example**), `Native950Dialogues` (1184/1188/1186 pins carry across; **1191 changed** — re-derive), `Native950WorldMap`, `Native950Settings` (**worst single file: 82 of 105 resolvable pins fail**), `Native950RegionMusicCatalog` (2 unmeasured pins; music mapping likely drifted).

**Acceptance:** send and receive public and private chat; open a multi-option dialogue and pick each option; open the world map; open all six settings pages.

**STRING(17) vs NAME(53) is a coin-flip made by two agreeing address orderings.** The wire formats are identical, so a wrong guess is a mislabelled prompt, not corruption — but if a name prompt behaves like a free-text prompt, swap them.

---

### STAGE 8 — Hardening
**Gain:** the ability to trust the thing.

Re-derive the remaining SHA pins (147 of them are index-12 clientscripts — but **15 of 17 high-value scripts I'd flag have identical byte length with different content**, which is the signature of a recompile with renumbered operands, not re-authored logic, so this is materially cheaper than "147 unknowns"). Turn the `verifyCache` kill switch back on. Add the two standing regression tests: (a) every `ServerPacket.size()` equals `950-server-sizes.toml` at that opcode; (b) **a compile-time assertion that no mask constant in `PlayerMasks` appears in both the 947 and 950 sets** — every one of the ten 947 player-mask bits collides with a live 950 bit meaning something else, and the worst case (`FORCE_TALK_LOCAL` 0x8000 → 950's `HEADER_MARKER_BYTE3`) makes the client eat the first payload byte as a mask byte with no error.

---

## PART 4 — SIZE AND RISK

**Honest size: 6–9 weeks for one experienced developer.**
- `modern950` wire layer (8 files, 4360 lines): ~2 weeks. Roughly 20 of 31 live outbound writers change bytes; only 11 survive untouched. Every byte layout in that table has been derived twice or three times now, so this is transcription plus test vectors, not research.
- Adapters + handoff + engine hooks (19 wire-coupled files, ~9,700 lines): ~1 week, mostly mechanical retyping; four files (`EntityMasks`, `NpcViewport`, `EntityFrames`, `Viewport`) carry real per-revision logic.
- Cache, bindings, pins, `id-validity`: ~2–3 weeks, and this is the schedule driver, not the wire layer.
- Multi-player GPI test vectors and live shakeout: ~1–2 weeks.

**The single biggest risk of the whole port: the cs2 / interface layer in Stages 3–7, because that is exactly where the value of the port lives and it is the only layer with essentially no verified evidence.**
The wire layer is bounded, adversarial-verified, and mostly derived twice — I found five real errors in it and fixed them in half a day of disassembly. The interface layer is the opposite shape: **0 of 147 pinned clientscripts survive**, two interfaces renumbered by different amounts (1465 +1, 1430 +2) and a third (1475) renumbered *non-uniformly*, and the client parses interface packets positionally with silent failure — a wrong component hash targets a real but different component, a wrong varbit silently no-ops on the wrong provider kind, and neither reports an error. The 950 client's structural bindings (enums, structs, wearpos, varbit defs, interface 1477's Ataraxia-relevant components) are byte-identical, which is what makes the port viable at all; but "the structure is identical" says nothing about whether a recompiled script still takes its arguments in the same order. Years of 910-era content reaches the player through those scripts. Budget for re-authoring some of them, not just rehashing them, and decompile the two length-changing scripts (343, 1369) first as the canary.

**Runner-up risk:** the multi-player GPI four-pass state machine (Stage 6) has never been exercised on *either* client, and its failure mode is a silent permanent desync with partial world mutation committed — not a clean abort. That risk is fully mitigable with offline unit vectors, which is why it is the runner-up and not the leader.

**Runner-up-runner-up, and the one most likely to bite on day one:** appearance framing in Stage 1. It is the only failure in the port that crashes the client outright — which, perversely, makes it the safest of the three.