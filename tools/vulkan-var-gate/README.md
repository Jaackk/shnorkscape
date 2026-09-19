# Vulkan read-only variable gate

Experimental, single-shot diagnostic. No server integration or startup registration.
NOT LIVE VALIDATED OR INSTALLED. Build and synthetic tests passed on 2026-09-19.
The proposed Run-Gate.ps1 loader became inaccessible (access denied to both Git
and PowerShell) before execution. No cause was established from the queried
Defender events. Do not bypass the block, change security settings, or substitute
another injection route. The loader is not part of the committed checkpoint.
Runtime 3296 resolution and Save/Load comparisons remain unperformed.
Requires the explicitly approved deployed SHA-256
`36c45c1cf6eed0c6cb0b789ca1672d685c1746def9d65d6f18d72638f0087bc9`.
Original evidence image: `f0dc2a07a8850daa110ea1a7417a540d897551f966f52e64baec1e74a8e2d568`.

Reference order: Ataraxia 910 server-side cached variables, Undercut client-side
MainLogic/Funchook access, independently verified exact Vulkan 950 instructions.
No 949/OpenGL address is used without the recorded Vulkan evidence.

Build with MSVC x64 and CMake, supplying FUNCHOOK_SOURCE at commit
`b4991704add411ecbc492dae020f375124d51f45` (kubo/funchook).
Build output belongs under ignored `temp/vulkan-var-gate-build`, not Git.
Run `var_gate_test.exe` before any client integration.

`Run-Gate.ps1` loads only the fixed diagnostic DLL into the single matching client.
The only loader data written is a temporary DLL-path allocation; no game state is
written. The DLL verifies file hash, complete critical code-span hashes, relocated
vtable target and Funchook's prepared five-byte span before activation.

The hook invokes the original function with the original arguments, then checks
the global Client aliases, vtable, manager and bounded sparse lookup for 3296 twice.
It runs once, not every tick. No values or raw memory are emitted. Results go to
ignored `logs/vulkan-var-gate.txt`. The worker attempts bounded uninstallation,
checks restored entry bytes, and records `restored=1` only on success. The inert
DLL/trampoline intentionally remain allocated until process exit to avoid unload
races. Do not load twice into one process. If restoration fails, stop; do not run
further diagnostics in that client.

This first gate does NOT prove Save/Load equality or descriptor coverage. Only
after a live pass may the next diagnostic add workspace-subset snapshots.
