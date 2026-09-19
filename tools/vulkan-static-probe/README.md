# Static Vulkan first-read diagnostic

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
