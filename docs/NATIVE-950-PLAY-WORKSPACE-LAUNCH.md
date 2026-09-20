# Normal Play launch of proven workspace client

## Acceptance completed: 2026-09-20

Jack completed the instructed Play -> NEW Save -> close/Stop -> Play -> restore
test. Post-savePID31896 A (02:14:11), freshPID45192 A (02:15:14) validate against
Jaxa revision2. All912 stored entries match post-save; restored Custom matrices
and masked metadata match exactly. Thirty Custom parents changed from revision1.
Client31896 records native-exit-snapshot-sent then durable-receipt. Fresh server
PID30404 started02:14:43, loaded Jaxa revision2 and queued native Load; the normal
launcher PID record identifies V5 PID45192. This covers fresh server flag startup,
not merely reuse of the manually configured server.

Raw active8 is native-reconstructed:681/720 fields equal,39 differ, none missing;
no raw whole-active equality is claimed. The account-bound comparison passed.
No production behavior changed during verification. This Play productionisation
gate is complete; preserve bd1d56f together with proven workspace baseline1ded397.
Resume combat work, not workspace investigation. The earlier pending instructions
below are retained as the reproducible test procedure.

Protected acquisition/save/restore baseline:1ded397. No native client, engine,
editor, account policy, storage or application behavior changed here.

`Play.cmd -> Launch.ps1 -> Start-950Test.ps1 -> Start-950Client.ps1` now selects
the exact existing V5 executable and DLL. Their diagnostic filenames remain
because authenticated peer verification pins the exact executable path. Normal
Play does not rebuild, rename, inject or patch either binary. The original
Vulkan executable remains intact. Existing config URL, client working directory,
launch lock, cache preparation and tracked PID shutdown stay unchanged.

Shared `Client-WorkspaceLaunch.ps1` checks exact bundle location, V5 EXE/DLL hashes
and912-ID schema hash before server startup and again before client launch.
Missing/changed assets fail closed, without silent OpenGL fallback, downloading,
quarantine restoration or security exclusions. Explicit Play-OpenGL still uses
the original renderer, with a warning that automatic capture is unavailable.

Fresh normal startup adds the three existing switches:
`-WorkspaceDurabilityGate -WorkspaceCaptureGate -WorkspaceJaxaRollout`.
Reusing a running server requires exactly one true JVM property for each, in
addition to existing runtime identity/JAR-age/cache/content checks. A mismatching
server is refused with close/Stop.cmd/Play.cmd instructions, never auto-killed.
Direct Start-950Server default and engine eligibility remain unchanged. Only
Jaxa is eligible in this production profile; no global capture was enabled.

Pre-edit rollback/save/runtime backup:
`backups/pre-edit-20260920-020829-219`; GitHub1ded397 verified before edits.
23 PowerShell preflight/flag/syntax/wiring checks pass without launching a process,
including wrong pins, relocation, missing flags and duplicate overriding flags.
No JAR rebuild/redeployment or client execution by the agent is required for
these source launcher changes. The real Play route still needs Jack's live test.

## Final live gate (pending)

1. Close the current manually launched V5 client normally.
2. Double-click Play.cmd; login Jaxa in the intended maximised/fullscreen setup.
3. Wait for Workspace automatic capture ready, then make a small recognizable
   NEW edit and Save & Exit to Custom1. Wait for the durable revision receipt.
4. Ctrl+Shift+F9 once for optional independent post-save verification.
5. Close the client normally. Use Stop.cmd once to stop the server as well;
   this additionally verifies fresh-server flags on the next launch.
6. Double-click Play.cmd again; same fullscreen/maximised setup, login Jaxa.
   No commands/editor/Load: confirm the NEW layout automatically restores.
7. Ctrl+Shift+F9 once for fresh-process readback, then report the visible result.

If Windows/security blocks launch, stop/report; never bypass it. Do not classify
the normal Play productionisation as live-proven until this test passes. Keep
combat work paused at this gate; workspace semantics are not reopened.
