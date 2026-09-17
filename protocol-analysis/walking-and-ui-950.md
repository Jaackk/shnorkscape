# 950 walking and settings/ribbon UI — derivation and status

Addresses are VAs in `OpenNXT/data/clients/950/win64/original/rs2client.exe` (base 0x140000000).
Reproduce with `tools/dis950.py` (disassembly), `tools/cache950.py` (cache), `tools/match950.py`
(947-parser to 950-opcode matching). All three run under the AstraNXT `.venv` python, the only
local interpreter with pefile and capstone. AstraNXT itself was read-only throughout.

## Opcodes derived this pass

Identified from parser shape and cross-checked against the native descriptor lengths in
`950-server-sizes.toml` / `950-client-sizes.toml`. Registration order was never accepted as
evidence on its own.

| Name | Side | 947 | 950 | Size | Basis |
|---|---|---:|---:|---:|---|
| MOVE_GAMECLICK | client | 3 | **88** | 5 | registration 0x14000b160 (`mov edx,0x58` / `lea r8d,[rdx-0x53]`) |
| MOVE_MINIMAPCLICK | client | 28 | **78** | 18 | registration 0x14000b020 (`mov edx,0x4e` / `[rdx-0x3c]`) |
| IF_SETEVENTS | server | 35 | **24** | 12 | parser 0x140107970, decoded field by field |
| IF_CLOSESUB | server | — | **69** | 4 | parser shape |
| IF_SETTEXT | server | — | **115** | -2 | parser shape |
| IF_SETCOLOUR | server | — | **83** | 6 | parser shape |
| IF_SETPLAYERHEAD | server | — | **38** | 4 | parser shape |
| IF_SETSCROLLPOS | server | — | **16** | 6 | cs2 name-string reference |
| UPDATE_STAT | server | 66 | **92** | 6 | parser 0x140141290 (`neg al` twice, `bswap edx`) |
| VARP_SMALL | server | 10 | **79** | 3 | parser shape |
| VARP_LARGE | server | 111 | **4** | 6 | parser shape |
| VARBIT_SMALL | server | 50 | **28** | 3 | parser 0x1401421E0 (`add al,0x80`; id LE u16) |
| VARBIT_LARGE | server | 71 | **82** | 6 | parser shape |
| RESET_CLIENT_VARCACHE | server | 48 | **23** | 0 | exact parser match, size 0 in both |
| IF_BUTTON1..10 | client | — | **18,122,89,100,81,126,49,66,31,59** | 9 | pointer table 0x140b63c00 |

`tools/match950.py` reached the same var-family answers independently, by normalized-shape matching
against the 947 project's verified parser addresses. Its output is in
`opcode-match-947-to-950.txt`. Four packets matched exactly there and are recorded but not yet
used: RESET_CLIENT_VARCACHE 23, UPDATE_RUNENERGY 21, UPDATE_RUNWEIGHT 7, UPDATE_REBOOT_TIMER 31.

## Walking

**Client request.** Sender 0x1400e46b0, payload written at 0x1400e47f6-0x1400e4869:

| Byte | Native write | Decode |
|---:|---|---|
| 0..1 | `movzx edx,word[obj+0x50]`, `ror dx,8`, 2-byte store | destination y, plain big-endian u16 |
| 2 | `add bpl,0x80` into `[pos+2]` | `(modifier + 0x80) and 0xFF` |
| 3 | `lea ecx,[r8+0x80]` into `[pos+3]` | `(x + 0x80) and 0xFF` |
| 4 | `sar r8d,8` into `[pos+4]` | `(x ushr 8) and 0xFF` |

So `x = (b4 shl 8) or ((b3 - 0x80) and 0xFF)` and `y = (b0 shl 8) or b1`. Coordinates are absolute
world tiles; the packet carries no plane. Opcode 78 appends a fixed 13-byte trailer, ignored here.

**The 947 formula is not compatible** and must never be reused: there the modifier sits at b4 with
the inverse transform `(128 - b4)`, and the `+128` bias is on y rather than x. Applied to a 950
payload it turns modifier 1 into 255 and corrupts both coordinates. `Verify950Kt` asserts the two
decoders disagree on the same bytes, so re-porting the 947 version fails the build.

A draft of this work carried the example payload `0C 92 80 96 0C` for x=3222. That is wrong — it
omits the `+0x80` bias on b3. The correct payload is `0C 92 80 16 0C`, which is what the regression
asserts.

**Server response.** PLAYER_INFO type-3 short relocate, decoder 0x140126030: 1 bit update = 1,
1 bit mask = 0, 2 bits type = 3, 1 bit long = 0, then 15 bits of
`style(3) | planeDelta(2) | dx(5 signed) | dy(5 signed)` (0x1401264f2). The style index selects
from the table at 0x140C6EC88 = `{-1, 0, 1, 2, 3}`; **2 = WALK, 3 = RUN**. Indices 5..7 read past
the end of that table. Types 1 and 2 are unusable: type 2 never writes the style pointer, so the
step renders with no animation.

Verified vectors: `walkStep(1,0)` = `B2 02 00 7F F4` (east), `walkStep(-1,1)` = `B2 3E 10 7F F4`
(north-west), `walkStep(2,0,RUN)` = `B3 04 00 7F F4`. Idle stays `00 7F F4`.

**Hazard — never send a second REBUILD_NORMAL.** `Viewport.init()` unconditionally prepends a
~5.1 KB bitstream, but the client consumes it only while `byte [[client]+0x198b8 + 0x49] != 0`, and
both rebuild parsers clear that byte at their tail. A second rebuild would read its header 5.1 KB
too far in and silently corrupt the scene. Walk targets are clamped to the built scene instead;
`Viewport.kt` is not in the build list either way.

## Settings / ribbon UI

**Cache bindings are stable.** Every root slot binding (enum 7716, struct, param 3505 attach /
3503 wrapper) is byte-identical between the 947 and 950 caches, including the ribbon: key 1002 to
struct 38884 "Ribbon", attach `1477:64`, wrapper `1477:61`. All seven policy varbit definitions in
index 2 group 69 are byte-identical too, so the 947 values transfer unchanged. Interfaces 1431,
1473, 1462, 137, 1477, 1482, 365, 1444, 567, 1433, 1426, 1448 and struct 21301 are byte-identical.

**Two components moved.** Interface 1477 gained id 819 (a duplicate of 818), so 947 ids at or above
819 are +1 on 950; interface 1465 gained "View Leagues" at id 9, so 947 ids at or above 9 are +1.
Interface 1475 went 60 to 59 and is *not* a clean shift — re-derive, never shift. Everything the
settings path touches (1477:8, :61, :64, :708, :714, :715, :717, :805) is below 819 and identical.

**The one renumbering that reaches the wire is the run orb: 1465:14 on 947 is `1465:15` on 950.**
Confirmed by string anchor ("Toggle Run|Toggle Rest|Run Energy", length 173) in both caches.
Sending IF_SETEVENTS to 1465:14 on a 950 client wires the wrong widget.

**IF_SETEVENTS layout**, from parser 0x140107970:

```
mask     intv1        (b2<<24)|(b3<<16)|(b0<<8)|b1   @0x1401079a0-0x1401079be
toSlot   ushort       big-endian, rol ax,8            @0x1401079cf
fromSlot ushort128    (b6<<8) | ((b7-0x80)&0xFF)      @0x1401079eb-0x1401079ff
parent   intle        (b11<<24)|(b10<<16)|(b9<<8)|b8  @0x140107a0e-0x140107a3c
```

0xFFFF folds to -1 for both slots at 0x140107a42 / 0x140107a4d. Mask bit `(n+1)` enables component
option `n`; with the bit clear the client shows no option and sends nothing at all (gate
`lea ecx,[opIndex+1]; shr edx,cl; test dl,1` at 0x14016a752).

Wire vectors asserted by `Verify950Kt`:

- `IF_SETEVENTS(1473:5, 0, 27, 2360382)` = `04 3E 00 24 00 1B 00 80 05 00 C1 05`
- `IF_SETEVENTS(1465:15, -1, -1, 2)` = `00 02 00 00 FF FF FF 7F 0F 00 B9 05`

**Two silent-failure gates on every var write.** The definition provider at `[owner+0x18D68]` must
report kind 4 (checked at 0x140142226), and a varbit's parent varp must carry integer type tag 0
(0x1402EB3F2). Never write a varbit's parent varp with a long-form varp packet first: that stores
tag 1 and permanently blackholes every later varbit on that varp.

## Interface clicks — confirmed live

A live session produced five IF_BUTTON captures, every one of which decodes to a component that
exists in the 950 cache. This settles the layout:

| Payload | Interface | Component | Slot | What it is |
|---|---:|---:|---:|---|
| `ffffffc5050800ffff` | 1477 | 8 | -1 | Escape / Options |
| `ffffff970500000007` | 1431 | 0 | 7 | the Settings gear, matching the 947 "actor 7" binding |
| `ffffffb9050f00ffff` | 1465 | 15 | -1 | run orb, confirming the 14 to 15 renumbering |
| `ffffffb9052200ffff` | 1465 | 34 | -1 | another minimap orb |
| `ffffff8a032000ffff` | 906 | 32 | -1 | lobby world-select (captured earlier) |

Layout: `b0..b2` big-endian u24 item (0xFFFFFF is -1), `b3..b6` hash in wire order b2,b3,b0,b1,
`b7..b8` big-endian u16 dynamic slot (0xFFFF is -1). Plain big-endian, little-endian and the 947
order all produce interface ids that are absent from the cache. All five are pinned as regression
vectors in `Verify950Kt`.

The client was therefore asking the server to open Settings all along, and the server was not
answering. That is what the settings implementation below does.

## RUNCLIENTSCRIPT is safe on 950

Previously flagged as unverified. Parser 0x1400f73c0: NUL-terminated descriptor first, then the
arguments in reverse order, integers read big-endian (`bswap eax` at 0x1400f7696 behind the byte
order probe at 0x1400f7677), then the script id big-endian. That is exactly what the inherited
`RunClientScript.Codec` emits, so it can be used with integer arguments.

## Remaining unknowns

- The settings pages beyond Graphics (Gameplay, Controls, Audio, Ribbon, Accessibility) are wired
  by the same tab handler but have not been exercised.
- Audio's `hideInterface(1448, 5, ...)` path and the 365 pending-checkbox rejection from the 947
  implementation are ported structurally but untested.
- `CLIENT_SETVARC_LARGE` (opcode 119) is used only for var 2911, which the native close script
  reads. Its layout is derived from parser 0x140141DB0 but has not been round-tripped live.

## What is implemented

Both features are off by default and gated, because each adds code to the verified world-entry
path. `Start-950Test.cmd` still launches the plain configuration; use `Start-950Test.ps1 -Walk`
and/or `-Ribbon` to enable them.

- `-Dopennxt.950.walk=true` — decode client 88/78, drive `native950WalkTarget`, and emit one walk
  step per tick instead of idle.
- `-Dopennxt.950.ribbon=true` — send the seven policy varbits, mount ribbon 1431 plus backpack
  1473, worn equipment 1462 and all-chat 137, extend the retained-slot set so they are not hidden
  again, and send the three IF_SETEVENTS calls.
- `-Dopennxt.950.settings=true` — decode interface clicks and drive the native Settings window:
  Escape (1477:8) and the gear (1431:0 slot 7) open it, 1477:714 switches tabs, 1477:717 slot 1
  closes it. Ported from the 947 project's `Native947Settings`. Independent of the ribbon, since
  the Escape binding lives on the root.

The settings open sequence, for reference: RUNCLIENTSCRIPT 8179 and 8180(1,1) to retire the native
quick-options overlay, IF_SETEVENTS(1477:8, mask 252) to suppress the duplicate Options
notification while management is visible, IF_OPENSUB 1448 at host 1477:715, seven varbits cleared
plus 18994=9, CLIENT_SETVARC_LARGE 2911=9, then the Graphics page: varbit 19001=2,
RUNCLIENTSCRIPT 8288(9) and 8193, unhide 1477:708, IF_OPENSUB 1426 at 1448:3 and 742 at 1426:0,
RUNCLIENTSCRIPT 8283(21182, 0), the hide set, then IF_SETEVENTS on the six tab actors of 1477:714
and on the close actor 1477:717.

Neither has been exercised against a live client yet. The data-only half (opcode names and field
declarations under `data/prot/950/`) is always active and does not depend on either flag.

## The Vulkan client for revision 950 — found, binaryType 10

An earlier pass concluded "revision 950 has no Vulkan renderer". That was **wrong**, and the way it
was wrong is worth recording: it scanned only the eight client variants the project had already
downloaded, and OpenNXT's `BinaryType` enum stops at `MOBILE(7)`. `ClientDownloader` loops
`BinaryType.values()`, so its log line "Downloading all binary type clients" means all eight it
knows about. Binary types 8-12 were never requested.

**Graphics Mode selects a binary, not an in-client setting.** That is why no Vulkan code exists
inside the win64 build and why the launcher must "download the new executable" when the mode
changes. The three Windows modes are three separate executables:

| binaryType | platform string | Graphics Mode | renderer imports |
|---:|---|---|---|
| 2 | `NXT-Windows-64` | Normal | `opengl32.dll`, `dxgi.dll`, `d3d9.dll` |
| 6 | `NXT-Windows-64-ANGLE` | Compatibility | `dxgi.dll`, `d3d9.dll` + ANGLE (`libEGL`, `libGLESv2`) |
| **10** | **`NXT-Windows-64-Vulkan`** | **Vulkan** | **`vulkan-1.dll` only** |

All three report engine `RS2Engine-950-NXT-1` — the same revision and build this project targets.

### The full binary-type space

Probed `https://world35.runescape.com/jav_config.ws?binaryType=N`. Note the endpoint in
`ServerConfig.kt` uses port 8080, which is unreachable here; plain HTTPS on a world server returns
the same manifest. Types 13+ return an HTTP error, so the space is exactly 0-12, all
`server_version=950`:

| type | binary | crc | note |
|---:|---|---|---|
| 0, 7, 8, 11, 12 | none | - | empty slots |
| 1, 5 | rs2client.exe | 3485636912 | 38 KB stub |
| 2 | rs2client.exe | 2474565715 | WIN64, in use |
| 3 | rs2client | 4033208673 | macOS |
| 4 | rs2client | 1621460637 | Linux |
| 6 | rs2client.exe + 3 DLLs | - | WIN64C |
| **9** | rs2client | 4153246289 | Unix, **no Vulkan symbols** |
| **10** | rs2client.exe | 2082320373 | **Vulkan** |

### Evidence in binaryType 10

Downloaded to `OpenNXT/data/clients/950/bt10/original/rs2client.exe`, 15,089,416 bytes,
sha256 `f0dc2a07a8850daa110ea1a7417a540d897551f966f52e64baec1e74a8e2d568`. Its crc32 matches the
manifest's 2082320373 exactly, so the download is authentic and intact.

- Imports **`vulkan-1.dll`** and no OpenGL or Direct3D library at all.
- 674 distinct Vulkan symbols including `vkCreateInstance`, `vkGetInstanceProcAddr`,
  `vkEnumeratePhysicalDevices`, `vkCreateDevice`, `VK_KHR_swapchain`, `VK_EXT_memory_priority`.
- Build paths `runetek/libs/hal/render/context/Vulkan/Swapchain_Vulkan.cpp`,
  `device/Vulkan/RenderDevice_Vulkan.cpp`, and the literal `#define VULKAN_BACKEND`.
- Diagnostics `Vulkan API version too old: %i.%i`, `Vulkan Device Lost`.
- Delay-imports `steam_api64.dll`, consistent with the beta being distributed through the Jagex
  Launcher and Steam.

Binary type 9 is the Unix counterpart by position but contains **no** Vulkan symbols, so it is a
different variant rather than the Linux Vulkan build.

### What it would take to run it here

The engine revision is identical, so the cache and every protocol result in this project apply
unchanged. Still required before launch:

1. The RSA patch, so it trusts the local server's key. `client-patcher` selects its target by
   `BinaryType` name, and `bt10` is not in that enum, so either the enum needs an entry or the
   patch must be applied directly.
2. The storage-isolation patch (`tools/isolate_client_storage.py`), which redirects the four
   Windows folder lookups into `client-state/`. It was verified against the exact win64 binary and
   must be re-verified against this one before use.
3. A Vulkan-capable GPU and driver.

Also note the client reports its platform in the `nxt=` query parameter of `/nxtclienterror.ws`
and elsewhere. This build sends `NXT-Windows-64-Vulkan` rather than `NXT-Windows-64`; anything
keying off that string needs to accept it.
