# Static Vulkan first-read diagnostic

## Current build: v2 lookup controls

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
