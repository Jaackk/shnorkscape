# Static Vulkan read-only diagnostic

Current: **V3 manual Save/Load diagnostic**, documented below. V2 runtime reader
validation passed; Save/Load endpoint equality is awaiting the V3 manual test.

## Historical V2 lookup controls

Manual v1 runtime result `workspace-static-25488.json`: world entry worked,
409 buckets / 219 elements, 3296 not found. This does not distinguish absence
from a bad reader. It does prove the static wrapper executed in the real client.

Exact Vulkan follow-up:
- Domain primary vtable 0xc6dbb8 delegates from getter 0x153a60 via domain+8.
- Secondary vtable 0xc6d818 slot+8 is getter 0x2eb480. It takes the descriptor's
  integer ID directly, uses id modulo bucketCount, compares node+0, follows
  node+0x28, and returns node+8. No domain packing occurs in this lookup.
- Its absent-key path compares with bucket[count], then delegates to the
  definition's default provider. A valid native value need not have a stored node.
  The diagnostic deliberately does not invoke this getter or default provider.
- Native integer accessor 0x1501d0 checks tag zero at value+0x18 (node+0x20).
- Setter 0x10dff0 uses the same descriptor ID directly. These three functions
  are now separately hash-pinned in the image builder and runtime preflight.
- 3296 is a legitimate domain-2 parent (for example varbits 19038/19039), but
  is absent from the 215 bootstrap IDs. The descriptor is not an instantiated-node
  inventory. 219 elements being close to 215 is suggestive, not proof of contents.

V2 adds direct, typed bootstrap controls 2852, 2912, 3721, 4955, 5139, 6458.
Their identities/initial values are tested against both the descriptor and
TODORefactorThisClass.populateServerpermVarcs. Values may subsequently change;
matching the static value is reported but is not required for a valid read.
V2 distinguishes found-zero, absent-null, absent-sentinel, type mismatch,
cycle/bounds and bucket/key inconsistency. It checks both domain vtables.

It records entry and settled (three seconds later) samples on the same callback
thread, then disables itself. Malformed traversal stops immediately. Files:
`logs/workspace-static-v2-PID-entry.json` and `...-settled.json`.
`controls-resolved-3296-absent` would establish direct-key control reads while
3296 has no stored node at those observations. That result has NOT yet been
observed; do not claim that v1 alone proved non-instantiation.

Separate executable: `client/rs2client-vulkan-workspace-diag-v2.exe`
SHA-256: `fb96d190afcba769ca7983d2f76d45a4955cf7919d19e8ff4584a5e265e59b67`
Separate DLL: `client/shnork_workspace_probe_v2.dll`
SHA-256: `9c8d09346594671fe44f0b6242987104d4b3b47f5ee1e7ec92b488ca470803b7`

Manual v2 test: close the other client normally, launch this EXE with the same
config URL as v1, login and wait 10 seconds. Do not open Edit Mode or Save/Load.
Return both result files. No tool-driven launch or alternative execution route
is part of this task. Production and the v1 diagnostic files remain untouched.

The remainder documents the original v1 construction and execution history;
current source builds v2 and must use a fresh v2 output/manifest path.

This is a separate pre-launch diagnostic client, not a persistence implementation.
No injection, Funchook, runtime detour installation, security changes, server changes,
or RuneScape variable writes. Production remains `client/rs2client-vulkan.exe`.

## Construction

`build_image.py` accepts only deployed SHA-256
`36c45c1cf6eed0c6cb0b789ca1672d685c1746def9d65d6f18d72638f0087bc9`.
It verifies the independently established exact-Vulkan code spans. It appends two
sections (RX code, RW data), a normal PE import of `shnork_workspace_probe.dll`,
and x64 unwind metadata. It changes only the existing main-logic vtable slot to
the wrapper. The original function at RVA 0x25550 and all existing executable
sections remain byte-identical. The existing vtable relocation supports ASLR.

The wrapper calls the original first, preserves post-call volatile registers,
flags and MXCSR, calls the probe, then returns. Its unwind record accounts for
the 216-byte aligned stack frame. No original function prologue is overwritten.
This is a normal startup DLL import, not a DLL loaded into a running process.

The DLL checks the exact generated executable hash and live critical code hashes.
Only calls from the wrapper are accepted. It waits for exact-950 world state 30,
then checks Client aliases, vtable, manager/stat neighbour and two bounded sparse
lookups of integer workspace parent 3296. A missing key is NOT interpreted as zero.
The result is written once to ignored `logs/workspace-static-PID.json`; the probe
then disables itself. `int32Projection` means the evidenced integer read at node+8,
not a claim that all native variant types/labels have been decoded.

## Reproduce

Use Python with pefile/capstone, MSVC x64 19.50.35730 and CMake 4.2.
Run `test_image.py`. Generate into a fresh ignored output path using:

```
python tools/vulkan-static-probe/build_image.py client/rs2client-vulkan.exe temp/rs2client-vulkan-workspace-diag.exe temp/workspace-static-manifest.json
```

Configure this directory with `PROBE_IMAGE_HASH` and `PROBE_THUNK_RVA` from that
manifest; build Release; run `probe_test.exe`. `/Brepro` and nonincremental linking
are enabled. Record the DLL hash alongside the image hash. Copy the separate EXE
and DLL beside the normal client dependencies without replacing production.
Launch the diagnostic EXE with the same config URL as Start-950Client.ps1; do not
change the normal launcher. No server restart is required.

Generated EXE SHA-256:
`361a86d1b7d69634e84e30dd98f1c21c742386d276cd7c39df778949e3d7f581`.
Diagnostic DLL SHA-256:
`cfd559413b174dfa0fd7919d15d6671c7a7c97fc421b11887c22286ab8d95a1d`.
The build manifest records the wrapper/IAT/unwind RVAs and code-span hashes.

Rollback is a normal client exit followed by launching the unchanged production
client. Do not run both clients against the same account. If security software
blocks this separate build, stop; do not change exclusions or evade it.

## Evidence boundary

Undercut supplied the structural pattern; all addresses here were independently
checked against exact Vulkan 950. Ataraxia's server-side cached variable map does
not provide these unreported native values. The prior injector never ran.

Passing offline tests proves image construction and bounded synthetic reads only.
The first live result must precede any wider workspace snapshot. No Custom-slot
Save/Load equality, label typing, durability or restoration is claimed by this gate.

## Execution result: 2026-09-19

Five image tests and the native bounded-lookup test passed. The EXE generation
was byte-identical across two builds. The DLL used reproducible compiler/linker
flags. Source checkpoint 61bee76 was pushed before attempting a live launch.

The execution tool rejected the diagnostic Start-Process request as "blocked by
policy" before process creation. This is not evidence of a native failure or an
antivirus diagnosis. No alternate launch route was attempted. The unchanged
production client was reopened through Start-950Client.ps1 (PID 44560).
No live 3296 read, Custom-slot snapshot, or Save/Load comparison was obtained.
## Current V3 manual Save/Load diagnostic

V2 is live validated: six known bootstrap controls resolved as stable int32 with
exact expected values both at entry (409 buckets/219 elements) and settled
(1741/1209). Parent 3296 was validly absent at both points. Its absence is not a
reader failure and is not synthesized into zero. V2 binaries remain untouched.

V3 uses the same static import/wrapper/lookup, no injector and no runtime hook.
It adds a foreground-only Ctrl+Shift+F9 rising-edge trigger, sampled only in world
state 30. It does not consume/remap native key events. Avoid assigning that chord
to a gameplay action. The first three presses capture A, B, C; then it disables
itself. No workspace sampling occurs between presses. No new server command,
protocol acknowledgement, native variable writes, persistence or restoration.

The generated schema covers 907 unique integer IDs:
- 900 domain-2 parents: Custom slots 6/7/12/13 and active slot 8, each with
  90 actors, eight fields per actor, 720 varbits and 180 parents.
- Seven directly script-evidenced integer metadata IDs: 8372/8373/8374 from
  8754 and 8269/8272/8275/8278 from the iload1/setvar branches of 19719.
  These seven have separate evidence; the existing 1694-ID descriptor is NOT
  silently expanded or changed. No string/label contents are read.

`snapshot_schema.py` pins the exact cache containers, opcode map and descriptor.
It resolves slot/actor switches in 8709 and maps the eight argument positions to
domain-2 varbit definitions in archive 2/69. All 900 matrix parents must belong
to the original descriptor. Generated JSON/header are deterministic. Staging 9
is deliberately omitted: its row uses direct variables rather than the packed
preset representation, and it is unnecessary for this committed-layout test.

Each press validates the Client/domain/control relationships and reads the entire
907-ID set twice on the native main-logic thread. Wrong type, failed bounds or
unstable data terminates sampling. Present int32 zero and absent/null are distinct.
No native default getter is called. Output contains IDs, typed values and bounded
table counts, not raw pointers/memory. Snapshot files are diagnostic evidence only,
never used for restoration and never committed to Git.

### Manual test

Close the production client normally first. Launch the separate V3 EXE from
Windows Run with the existing config URL below. Do not launch through an injector
or loader; if Windows/security software blocks it, stop without bypassing it.

```
"C:\Games\950OpenSource\client\rs2client-vulkan-workspace-diag-v3.exe" "http://127.0.0.2:8950/jav_config.ws?binaryType=2&baseConfigSource=patched&localRewrite=1&hostRewrite=0&lobbyHostRewrite=1&gameHostOverride=127.0.0.2&gamePortOverride=43650&contentRouteRewrite=0&worldUrlRewrite=0&codebaseRewrite=0&downloadMetadataSource=patched"
```

1. Login, wait for the world to settle. Keep the client window dimensions fixed.
2. Press/release Ctrl+Shift+F9 once: A.
3. Enter the working native editor, change Backpack, Save & Exit to Custom 1.
4. Press/release Ctrl+Shift+F9 once: B.
5. Change the workspace again WITHOUT saving over Custom 1; Load Custom 1 and
   visually confirm it returns to the saved arrangement.
6. Press/release Ctrl+Shift+F9 once: C.
7. Close normally using the window X. Return to the unchanged production Play.cmd.

No popup is added. Results appear under `C:\Games\950OpenSource\logs\`:
`workspace-static-v3-<pid>-A.json`, `-B.json`, `-C.json`, plus per-shot
`-A-controls.json` etc. Successful snapshots say `snapshot-stable`, with 907 items
and `nativeWrites:false`. If any shot is refused, stop and supply that result.
The diagnostic never acknowledges a packet or changes server/player data.

Offline comparison (Python):
```
python tools/vulkan-static-probe/compare_snapshots.py <A.json> <B.json> <C.json>
```

This checks exact parent retention for B6 versus C6, plus 720 corresponding
actor/field values from saved B6 versus active C8. It reports missing entries as
indeterminate, not equal-to-zero. It reports differences rather than forcing
equality: native Load can transform fields, especially across viewport sizes.
Metadata changes are reported separately. The three snapshots prove endpoints;
the deliberate intermediate alteration is Jack's visual control, not a fourth
captured state. Actual Save/Load equality is still pending Jack's live files.

### V3 build checkpoint

Source-generated EXE SHA256:
`38bd30b4a7bbc68e00af8de4d892585fea17cf3dfc4cce5ea8ff743e24689f98`

DLL `shnork_workspace_probe_v3.dll` SHA256:
`a815a7308a166cd45c6a2fa78aeff9e753fdbbc1b0240d2f66ac39d0992ddce7`

Schema SHA256:
`6f86d06ad857f7d878e0375372b9bdc93e598abd0586f6d4b2f936f34cb3269c`

Generate schema with `snapshot_schema.py`; generate the image with `build_image.py`
using a fresh V3 output path and production input. Configure CMake with that image
hash and `PROBE_THUNK_RVA=24379392`; build Release using the compiler below.
Run `python -m unittest discover -s tools/vulkan-static-probe -p "test_*.py"`
and the resulting `probe_test.exe`. No server build/deploy/restart is involved.

The historical first-read gate above is superseded by V2 live proof.
