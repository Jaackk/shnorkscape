# Installed950 lobby support

The first950 lobby implementation is installed. Start-950Test.cmd loads patches/classes before the original runtime JAR, so this test uses the three rebuilt classes. Build-950Lobby.ps1 rebuilds these overrides using the compiler/dependencies contained in this folder. No AstraNXT runtime or source was edited.

Active server packets:

|Packet|950opcode|Native-derived payload|
|---|---:|---|
|IF_OPENTOP|1|Interface ID little-endian,1 unused byte,16 unused bytes;19 total|
|IF_OPENSUB|100|Parent hash big-endian,12 unused bytes,interface ID little-endian with low byte+128,128-minus flag,4 unused bytes;23 total|
|IF_SETHIDE|67|Parent hash middle-endian,plain boolean;5 total|
|RUNCLIENTSCRIPT|35|NUL-terminated descriptor,reverse argument order,standard strings/big-endian integers,big-endian script ID|
|SERVER_TICK_END|160|Empty payload|
|NO_TIMEOUT|183|Empty payload|

The complete native950 client/server length tables are active. Unmapped client packets have explicit950placeholder names and are framed then logged/discarded. Old hardcoded client handlers28/50/82/106 are disabled for950, as are the heuristic length overrides. The950lobby uses the proven minimal sequence: open906,open814 at906:37,run10936. The906/814 cache archive payloads are identical between the supplied947/950caches. Script10936 exists in950 but its cache bytes changed; live verification remains required.

Validation: five independently derived packet byte vectors passed (root,child,visibility,mixed script arguments,and empty-argument lobby script). Native evidence includes950 parsers0x140107dd0,0x1400fb790,0x140107590,0x1400f73c0 and its later discontiguous body through0x1400f77b2. The script reader's optimized rotate/mask path is equivalent to a big-endian read; it is not middle-endian.

Logs/verify-950-lobby.log contains the byte checks. Logs/build-950-lobby.log contains compiler results. Startup confirms registrations1/35/67/100/160/183 and accepts950.1 JS5. The client reached the username/password form. Manual x/x login succeeded and the user confirmed the lobby renders. Play Now initially did nothing; the captured client108 request was unhandled. WORLDLIST_FETCH client108 and WORLDLIST_FETCH_REPLY server129 are now installed, along with client keepalive104. The world-list sender uses a big-endian checksum at0x1401646d9 and the reply parser consumes a plain final-chunk byte at0x1401648af. The refreshed client is awaiting the next World1/Play Now check; world entry is not yet claimed.

World/player updates and other unverified payloads have not been activated. World-list response129 is enabled for the current live test. Their candidates remain in protocol-analysis. The former full947seed is backed up under implementation-backup/prot950. The first mapping implementation backs up and overrides only the950test.


## World-loading fix (2026-09-08)

Live testing confirmed lobby rendering, World 1 selection, and the game-login exchange.
The next failure was a black loading screen: the server explicitly skipped the unmapped
REBUILD_NORMAL packet. The isolated overlay now enables the minimal native world path.

- REBUILD_NORMAL opcode 63: 18-byte header after the initial player-position bitstream.
  Fields are chunkX (BE low byte +128), mapSize (plain, must be 5), npcBits (+128),
  two ignored bytes, chunkY (BE), areaType (BE), and two BE hashes.
- PLAYER_INFO opcode 36: the appearance mask changed from 0x04 to 0x20. The two
  preceding bytes and +128 length are retained. Idle player updates use the existing
  one-local-player layout; native tick-end 160 and keepalive 183 complete each tick.
- Opens the world root, game view and minimap using current-cache interface slots.
- Unknown 950 world requests are traced and released without invoking the inherited
  947 compatibility handlers. Movement and other gameplay are not yet enabled here.

Build-950Lobby.ps1 also compiles WorldPlayer.kt and Native950WorldBootstrap.kt.
Seven native-derived protocol checks passed, including the region header and appearance
flag. Native receive-code excerpts are in protocol-analysis/world-bootstrap-950.txt.
Pre-fix world log: logs/world-black-screen-before-rebuild-fix.log.
Original files before this change: implementation-backup/world950/.
The restarted client is ready; whether the world renders awaits the next manual login.


## Appearance fix (2026-09-08, later)

The black screen after Play Now was a client crash, not a stall. The client's own
`/nxtclienterror.ws` report (persisted under `OpenNXT/data/debug/clienterror/`) decodes to
`Main: access violation reading (0000000000000218)` with a native stack running
packet dispatch -> PLAYER_INFO(36) `0x1401436d8` -> appearance decode `0x14012dd07` -> fault at
`0x140131d53`. Two independent 947->950 changes in the appearance block caused it. Full derivation,
with addresses and reproduction commands, is in `protocol-analysis/appearance-950.md`.

1. **Body bias.** The appearance body copy at `0x14012dc81` dispatches through `0x14010ddf0` using a
   transform byte the caller installs at `0x14012dc46`. 950 points it at `.rdata 0x140B5FEC9` which
   holds `0x02` — the forward copy that adds 0x80 to every byte (`0x14010dea4`). 947 points at
   `0x140B91BAD` which holds `0x00`, a plain memcpy. The 950 server must send each body byte as
   `b xor 0x80`. The header, the `0x20` mask bit and the `len+128` length byte were already correct.
2. **Wearpos slot framing.** 950 reads each slot as an unsigned LEB128 varint
   (`0x140131c60`-`0x140131c8b`), classified by the constants at `0x140131a70`: 0 empty, 1 at
   wearpos index 0 is the npc-morph escape, below `0x800` is `kitId + 2`, at or above `0x800` is
   `itemId + 0x800`. 947 read a single 0 byte or a big-endian unsigned short with kit base `0x100`.
   Both revisions share the `0x800` item base — that part did not change.

Why it crashed deterministically: the old server's first five equipment bytes `00 00 00 00 01`
became `80 80 80 80 81` after the client's +0x80, all with the continuation bit set, so the varint
ran on until the `0x8B` of the trailing `short 2699`. The result was far above `0x800`, forcing the
item branch, and there is no null check on any path into `0x140131d53`.

Changed files (originals under `implementation-backup/appearance950/`):

- `model/world/Native950WorldBootstrap.kt` — added `transformBody()` (xor 0x80) applied in
  `initialAppearance()`, and `encodeWearposSlot()`, the shared LEB128 writer.
- `model/entity/player/appearance/PlayerModel.kt` — slot values now go through `putWearposSlot()`,
  which uses the 950 varint plus kit base 2 when `OpenNXT.config.build == 950` and keeps the 947
  byte/short encoding with kit base `0x100` otherwise. Item base is `0x800` for both.
- `Build-950Lobby.ps1` — compiles `PlayerModel.kt` as well, so the override actually shadows the
  runtime JAR.
- `tools/Verify950.kt` — the appearance vector now expects the biased body, plus eight LEB128
  boundary vectors.

`Verify950Kt` passes all eight checks (`logs/verify-950-appearance.log`). Simulating the client's
own decode over the new bytes yields the intended kit ids 18/26/38/3/34/42/14, bitmask 0, colours
`[3,16,16,0...]`, short 2699, the name, combat 3, levels `(0,-1)` and trailer 0, consuming exactly
48 of 48 body bytes. No slot value can now reach `0x800`, which makes the `0x218` fault
structurally unreachable while equipment stays disabled.

Not yet done, and not attempted here: if the client survives but the view stays black, the next
suspect is the loading-overlay gate. With no top-level interface open, `0x1401977c2` short circuits
to the clear at `0x14019783c` and only `SERVER_TICK_END` matters; with one open it also tests
`[ifmgr+0x189]` at `0x1401977e8`, whose only observed set is a constructor at `0x14019c8a1`.
Bootstrapping the world with the interface stage skipped would settle it. Also latent:
REBUILD_NORMAL `chunkX`/`chunkY` may be swapped (invisible at the 3222,3222 spawn), and 950 client
opcodes 9/33/18/28/98 are still unregistered.

**Confirmed live (2026-09-08).** After a full stop/start so the JVM reloaded the patched classes,
an x/x login into World 1 followed by Play Now rendered the game world: scene geometry, minimap,
chat and backpack panels, and the local player drawn with the kit ids the server sent
(18/26/38/3/34/42/14). No new `nxtclienterror` report was produced, and the game channel stayed
open past `native950-world-root` instead of closing there. This is the first 950 world entry.

Note for future test runs: `Start-950Test.ps1` reuses an already-running server if its recorded PID
is still alive, so a rebuild is only picked up after `Stop-950Test.cmd`.

Scope: this is login and first render only. None of the 947 project's content, UI bindings or
gameplay is ported to 950. Movement, npc info, varps, stats and the interface deck remain unwired,
and the equipment branch of the appearance encoder is still disabled.


## Walking and settings/ribbon UI (2026-09-08, later)

Ported from the 947 project as packet policy, not code: the 947 settings/ribbon implementation
lives in the Ataraxia947 backend, and this test runs the opennxt backend. Full derivation with
native addresses is in `protocol-analysis/walking-and-ui-950.md`.

Ten server opcodes and two client opcodes were derived and added, each cross-checked against the
native descriptor lengths: IF_SETEVENTS 24, IF_CLOSESUB 69, IF_SETTEXT 115, IF_SETCOLOUR 83,
IF_SETPLAYERHEAD 38, IF_SETSCROLLPOS 16, UPDATE_STAT 92, VARP_SMALL 79, VARP_LARGE 4,
RESET_CLIENT_VARCACHE 23, plus VARBIT_SMALL 28 sent raw, and client walk opcodes 88 and 78.
`tools/match950.py` independently reproduced the var-family answers by matching the 947 project's
verified parser addresses against the 950 handler table.

Changed files (originals under `implementation-backup/`):

- `model/world/Native950WorldBootstrap.kt` — `walkStep()` (PLAYER_INFO type-3 short relocate) and
  `readWalkRequest950()`. The 947 walk formula is incompatible and the regression asserts the two
  decoders disagree on the same payload.
- `model/world/WorldPlayer.kt` — `native950WalkTarget`, `walkNative950Step()`, walk decode in the
  950 receive arm, `sendNative950Varbit()`, `native950InterfacePolicy()`, and a widened retained
  slot set so the ribbon is not hidden immediately after being opened.
- `data/prot/950/serverProtNames.toml` — the ten opcodes above.
- `data/prot/950/serverProt/*.txt` — IF_SETEVENTS rewritten from parser 0x140107970; IF_CLOSESUB,
  IF_SETTEXT, IF_SETPLAYERHEAD, UPDATE_STAT, VARP_SMALL, VARP_LARGE, IF_SETSCROLLPOS corrected;
  RESET_CLIENT_VARCACHE created empty.
- `Start-950Server.ps1`, `Start-950Test.ps1` — `-Walk` and `-Ribbon` switches.
- `tools/dis950.py` — `drefs` now resynchronises after undecodable bytes. The previous single
  linear sweep stopped at the first data island, so "0 data refs" was a false negative.
- `tools/cache950.py`, `tools/match950.py` — new read-only cache and opcode-matching tools.

`Verify950Kt` passes 13 checks (`logs/verify-950-walk-ui.log`), including the walk step vectors,
the walk request decode, the 947-incompatibility guard, and both IF_SETEVENTS wire vectors. The
IF_SETEVENTS bytes produced by the new field declaration match the vectors derived independently
from the native parser.

Both features are OFF by default and gated behind `-Dopennxt.950.walk` and `-Dopennxt.950.ribbon`,
because each adds code to the verified world-entry path. Neither has been exercised against a live
client yet. Reaching the Graphics page still needs the inbound half: an IF_BUTTON decoder for the
gear click, and the gear's component id pinned from a real capture rather than the 947 note's
clientscript-derived "dynamic slot 7". RUNCLIENTSCRIPT's 950 field layout is also still unverified,
so the 947 policy's closing scripts 1362 and 8491 are deliberately not sent.

### Native Settings window (2026-09-09)

The live ribbon test settled the last unknowns. Five IF_BUTTON captures decoded cleanly, every one
landing on a component that exists in the 950 cache: Escape is `1477:8` slot -1 and the Settings
gear is `1431:0` slot 7 — exactly the 947 binding. The client was already asking the server to open
Settings; nothing was answering.

`RUNCLIENTSCRIPT` is also no longer a risk: parser 0x1400f73c0 reads a NUL-terminated descriptor,
then arguments in reverse order with big-endian integers (`bswap eax` at 0x1400f7696), then a
big-endian script id — exactly what the inherited codec emits.

`Native947Settings` is therefore ported into `WorldPlayer` behind `-Dopennxt.950.settings=true`
(`-Settings` on the launchers). Escape and the gear open the management window at host `1477:715`
and focus Graphics; `1477:714` switches tabs; `1477:717` slot 1 closes. It needs one further
packet, CLIENT_SETVARC_LARGE opcode 119, derived from parser 0x140141DB0 and confirmed against the
registration table at base 0x140E94EF0 + opcode*80 + 0x10 (self-consistent across seven opcodes).

The pure decoders now live in `Native950WorldBootstrap` so they are testable: `readInterfaceButton`,
`encodeVarbit`, `encodeVarc`. `Verify950Kt` passes 15 checks, with all five live captures pinned as
regression vectors (`logs/verify-950-settings.log`).

### Vulkan client (2026-09-09)

Graphics Mode selects a client binary, not an in-client setting, which is why no Vulkan code exists
in the win64 build. Probing `https://world35.runescape.com/jav_config.ws?binaryType=N` showed the
space is exactly 0-12 (13+ errors), all `server_version=950`, and that OpenNXT's `BinaryType` enum
stopping at `MOBILE(7)` is why the downloader never saw the rest. **binaryType 10 is
`NXT-Windows-64-Vulkan`, engine `RS2Engine-950-NXT-1`** - the same revision this project targets.
It imports `vulkan-1.dll` and no OpenGL or Direct3D at all. Full evidence in
`protocol-analysis/walking-and-ui-950.md`.

Two new tools patch it, and neither is trusted on assertion alone - each reproduces a known-good
artifact before being used on the new binary:

- `tools/patch_rsa_950.py` - the RSA key swap, reimplementing `ClientPatcher.patchFile`
  (`RSAUtil.findRSAKey` locates the Jagex modulus as an ASCII hex run; the local modulus replaces
  it). `verify` reproduces `win64/patched` from `win64/original` **byte-for-byte**.
- `tools/isolate_client_storage_generic.py` - the storage redirect, deriving every offset instead
  of hardcoding it: the `SHELL32.dll!SHGetFolderPathW` import is found through the import table,
  every `call qword ptr [rip+disp]` reaching it is found by scanning `.text`, and a stub is placed
  in section slack with the host section's VirtualSize extended. `verify` reproduces the existing
  `client/rs2client.exe` from `win64/patched` **byte-for-byte**.

The Vulkan build needed the generic version: its `.text` slack is only 48 bytes, enough for the
35-byte stub but not the 90-byte path, so the path goes into `.rdata` slack and the stub's
rip-relative displacement is recomputed. The original tool's hardcoded offsets are all wrong for it.

Launch with `Start-950Test.ps1 -Vulkan` (or `Start-950Client.ps1 -Vulkan`). The jav_config request
deliberately still uses `binaryType=2`: the server resolves that parameter through
`BinaryType.values()[n]`, so 10 would throw, and the config is revision-specific rather than
renderer-specific.

Verified live: the client launched, opened its window, stayed responsive, and established a
connection to 127.0.0.2:43650. `vulkan-1.dll` and NVIDIA's Vulkan ICD (`nvoglv64.dll`) are resident
in the process. No client error report was produced. Both shared Jagex folders were byte-for-byte
unchanged across the launch (`C:\ProgramData\Jagex` 153 files / 48813364104 bytes, delta 0;
`%LOCALAPPDATA%\Jagex` 4 files / 147456 bytes, delta 0) while `client-state` took 6.9 GB of the
Vulkan client's own cache - the isolation patch works on this binary.

**Vulkan is now the default renderer** (2026-09-09). Both clients are kept in `client/`:

| file | binaryType | platform string | renderer |
|---|---:|---|---|
| `rs2client-vulkan.exe` | 10 | `NXT-Windows-64-Vulkan` | `vulkan-1.dll` — **default** |
| `rs2client.exe` | 2 | `NXT-Windows-64` | `opengl32.dll` |

Both are the same engine, `RS2Engine-950-NXT-1`, so they are interchangeable against this server.
`Start-950Test.ps1` and `Start-950Client.ps1` take `-OpenGL` to select the legacy client on
hardware without a usable Vulkan driver; `-Vulkan` is accepted and redundant. Passing both is
refused. If the Vulkan binary or its isolation report is absent the launcher warns and falls back
to the OpenGL client, so the project stays usable on a machine where the Vulkan build was never
produced. The chosen renderer is printed at launch.

Note the already-open check now derives the process name from the selected executable; it
previously hardcoded `rs2client.exe` and so could never have matched a running Vulkan client.

## Region serving and collision (2026-09-09)

Two tracks, each behind its own flag. Derivation and evidence in
`protocol-analysis/regions-and-collision-950.md`.

**Track C, region serving (`-Regions`, `-Dopennxt.950.regions=true`).**
The client's scene window is 256 tiles with `base = (chunk - 16) * 8` (`[scene+0x6a0] = 0x100` at
0x1401177c9, applied at 0x1400f6fc5). `Viewport.mapSize` was `SIZE_104`, so `getLocalX/getLocalY`
disagreed with the client by 80 tiles on each axis - that is what confined walking. With the flag
on the viewport switches to `SIZE_256` and a header-only REBUILD_NORMAL re-centres the scene once
the player passes local 40..215. `Viewport.init` is never re-run: it unconditionally writes the
~5.1 KB GPI bitstream, and the client only consumes that while the gate byte is set, which the
first rebuild clears at 0x1400f7189.

**A latent bug fixed on the way:** `data/prot/950/serverProt/REBUILD_NORMAL.txt` had chunkX and
chunkY transposed. Bytes 6..7 land in scene slot 0 (0x1400f6fe5), the axis bounds-checked against
+0x14034 and fed by hash1's high 14 bits, and `TileLocation.tileHash` packs x high - so bytes 6..7
are X and bytes 0..1 are Y. The names are now the right way round. This was invisible because the
spawn is diagonal (chunkX == chunkY), which also makes the fix a no-op on today's wire. The
regression vector is now deliberately off-diagonal so a transposition cannot pass again.

**Track D, collision (`-Collision`, `-Dopennxt.950.collision=true`).**
New `model/world/Collision950.kt` (added to `$sources`). Builds a sparse global clip map from the
cache: index 5 `group = regionX or (regionY shl 7)`, file 3 terrain (`jagx` + v1, 4x64x64), file 0
locs, and loc definitions from index 16. Walls stamp both their own tile and the mirrored bit on
the neighbour; diagonals require the destination and both orthogonal neighbours to be clear; an
unloaded map square reads as -1 so it fails every test rather than reading as walkable.

Both decoders were validated in Python over the whole cache before any Kotlin was written:
5173/5173 map squares decode with the loc buffer consumed exactly, and 140220 of 140252 loc
definitions decode - the 32 failures being the undecoded opcode-209 records, matching the
derivation pass exactly. The clip map was then sanity-checked on Lumbridge: the bridge at
y=3225-3226 is clear and walkable straight across while the River Lum is blocked on both sides,
which is the bridge plane-shift rule working.

Two traps worth recording: `clipType = 1` comes from **opcode 27**, not 22 (22 is a payload-less
flag), and **opcode 17 clears two fields**, not one - treating it as an alias of 18 would turn
39,196 non-clipping definitions like flowerbeds and signposts into solid walls.

Neither track has been exercised against a live client yet.

### Flag defaults flipped (2026-09-09)

The per-feature switches existed so a regression could be bisected to one feature while each was
unproven. Walking, the ribbon, the Settings window and region re-centring are now all confirmed
against a live client, so leaving them off by default made the default configuration the least
capable one and made a forgotten switch silently drop a working feature.

`Start-950Test.ps1` / `Start-950Server.ps1` now enable **walk, ribbon, settings and regions by
default** and print which features are active at launch.

| switch | effect |
|---|---|
| *(none)* | walk, ribbon, settings, regions |
| `-Collision` | adds the clip map. Still opt-in: not yet confirmed live, and a wrong clip map traps the player rather than merely letting them through walls |
| `-NoWalk` / `-NoRibbon` / `-NoSettings` / `-NoRegions` | drop one feature, for bisecting |
| `-Minimal` | the bare verified world-entry path only; overrides `-Collision` |
| `-Vulkan` / `-OpenGL` | renderer, Vulkan by default |

`-Walk`, `-Ribbon`, `-Settings` and `-Regions` are still accepted and redundant so commands written
before the flip keep working.

## Ataraxia engine port, Stage 0 (2026-09-09)

Groundwork for serving the 950 client from the full Ataraxia engine instead of the small opennxt
backend. Rationale and the staged plan are in `protocol-analysis/ataraxia-950-port-plan.md`.

The engine carries the 910 content: verified against `C:\Users\developer\Desktop\Ataraxia-PS`, all 1024
`content/` and 621 `npc/` files are present in the 947 tree with **zero missing** (1016 and 619
byte-identical respectively); only the 33 Discord api files did not come across. So this is a wire
layer port, not a content port.

**What Stage 0 did.**

- Copied the engine into `Ataraxia950/` (2872 java files, 76 MB) and the Gradle cache into
  `.gradle-ataraxia` (124 MB). Both are local copies: AstraNXT is read-only, and Gradle writes
  locks into `GRADLE_USER_HOME` so it cannot be shared.
- `Build-Ataraxia950.ps1` builds it offline against **JDK 8** (the engine is a JDK 8 codebase; a
  newer JDK silently breaks its class scanning). Baseline compile of the unmodified copy passed
  first, so every later change is measured against a known-good build.
- Renamed the wire layer surgically: the fully-qualified package
  `com.rs.network.protocol.modern947` -> `modern950`, and the identifier prefix `Native947` ->
  `Native950` (102 files renamed, 177 rewritten). Deliberately NOT renamed, because they are paths
  or config keys rather than identifiers: lowercase `native947` locals, `resources/native947`,
  `data/modern947`, and the `modern947.evidenceRoot` / `modern947.nativeSizes` property names.
- **A trap the rename created and hid:** `Player.isNative947()` became `isNative950()` but still
  read `getClientProfile() == ClientProfile.NATIVE_947`, so it would have been silently false for
  every 950 player across ~93 call sites. It now delegates to `ClientProfile.isNativeModern()`
  (profile != LEGACY_910), which is what all those call sites actually mean.
  `ClientSession.beginNative950Gameplay()` had the same stale constant and now returns NATIVE_950.
- `ClientProfile`: added `NATIVE_950`, a `"950"` parse branch and `isNativeModern()`.
- `Native950Save`: CLIENT_REVISION 947 -> 950, SCHEMA_VERSION 3 -> 4, so a 947-era save is refused
  rather than reinterpreted.
- New `NativeCacheVerification` with `-Dataraxia.native.verifyCache=false`. The adapters pin cache
  files by SHA-256; those pins were taken against the 947 cache and 950 recompiled every
  clientscript, so on 950 they fail closed and the server never reaches a login prompt. The switch
  downgrades a failed pin to a one-line report and records it, so unverified bindings stay visible.
  Enforcement is ON by default.
- `ObjectDefinitions`: added the 950 loc opcodes 108-111, 206, 207/208 and 255, and corrected 160
  to a plain byte count. Without these the engine throws on 8103 of the cache's 140252 definitions.
  This table is the one validated in Python over the whole cache (ok=140220, fail=32, the 32 being
  the undecoded opcode-209 records).

**State:** `jar` builds clean - `ataraxia-950-1.0-UNTRACKED.jar`, 6039 classes, 58 in
`protocol/modern950`, zero stale `modern947`. AstraNXT untouched throughout.

**Not yet done in Stage 0:** the Kotlin handoff (`Ataraxia947Handoff.kt` -> 950, gate
`major == 950 && minor == 1`, accept `OPENNXT_GAME_BACKEND=ataraxia950`) and wiring the new jar
into the server runtime. Until then the ataraxia path is unreachable and `opennxt` remains the
active backend, so nothing that currently works is affected.

### Stage 0 complete (2026-09-09)

The Kotlin half is done and the Ataraxia backend is selectable.

- The login handoff cluster was retargeted from the 947 engine to the 950 engine and renamed:
  `Ataraxia947Handoff` -> `Ataraxia950Handoff`, plus `Native947CacheContent` /
  `InterfaceBootstrap` / `Ribbon` / `RunOrb` -> `Native950*`. Scoped to `net/login/` only:
  `model/world/Native947WorldBootstrap.kt` belongs to the working opennxt path, and WorldPlayer's
  other `Native947*` hits are its own private method names, so neither was touched.
- The gate now reads `build.major == 950 && build.minor == 1` and `OpenNXT.config.build == 950`,
  the backend token is `ataraxia950`, and the save path must end in `modern950/players`.
- `LoginServerDecoder.kt` and `LoginServerHandler.kt` are now compiled overrides too, because they
  name the handoff object. All seven files are in `Build-950Lobby.ps1`.
- `OpenNXT/runtime/lib/ataraxia-947-1.0-UNTRACKED.jar` was replaced by the 950 build
  (`implementation-backup/engine950/` holds the original). This had to be atomic with the Kotlin
  change: the old compiled `Ataraxia947Handoff` is referenced from `LoginServerHandler` on every
  login, so leaving it in place against a renamed engine would have thrown NoClassDefFoundError on
  the working path.
- `Start-950Server.ps1` / `Start-950Test.ps1` take **`-Ataraxia`**, which sets
  `OPENNXT_GAME_BACKEND=ataraxia950` and points the save path at `players/modern950/players`. The
  chosen backend is printed at launch. Default remains `opennxt`.

**Acceptance (passed):** with `-Ataraxia` the server starts with zero ERROR lines and zero
NoClassDefFoundError/ClassNotFoundException, i.e. the whole renamed engine loads. Without it, the
opennxt path is byte-for-byte the behaviour it had before, and a live restart confirmed login,
world, walking, ribbon, settings, regions and collision all still run.

**Next: Stage 1** - the `modern950` wire layer. `Native950Protocol` opcode/size tables (sizes taken
per-row from `950-server-sizes.toml`, never as a column), the inbound decoder's two-byte-opcode
branch removed, `sceneHeader`/`tickEnd`/`keepAlive`, the appearance path (mask bit 0x20, body
`xor 0x80`, LEB128 wearpos slots with kit base 2), and the two one-line PlayerInfo fixes. Its
acceptance test is an Ataraxia-backed avatar standing in Lumbridge wearing the seeded kit.
