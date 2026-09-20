# Automatic native Save capture: disposable V5 gate

Protected baseline: `11c7c52`. Full client-restart restoration of the reviewed
layoutgate2 sidecar is already proven. This change connects NEW native Save events
to that store. It is implemented/tested, but the NEW Save/restart loop needs Jack's
live acceptance. Jaxa and other accounts are excluded.

## Ownership and completion

The server registers a capture endpoint only after the first scene report for
the authenticated layoutgate2 game channel, with both explicit launch flags on.
The normal launch profile has no endpoint. The local Windows named pipe rejects
remote clients and requires exclusive first-instance creation. Windows supplies
the peer PID; the server verifies that PID owns the exact established game TCP
connection (both addresses and ports) already bound to its Player. No account name,
snapshot filename or claimed PID is accepted as authentication. It then verifies
the expected V5 EXE path/hash and adjacent DLL hash. The client independently checks
that the pipe server PID owns the reverse game connection. No login changes.

Per-pipe random128-bit nonce plus monotonically increasing per-editor request
number bind responses to this session. Open arms a bounded read request; Save marks
that request eligible; X/native-close cancels it. A new editor cycle cancels any
unconsumed previous request. Cancel, stale or malformed responses cannot publish.
Same-value re-saves have separate request identities, not content-hash deduplication.

The V5 static adapter uses the proven post-main-logic boundary and reader. While
armed, it observes the evidenced logical3477 native Edit Mode variable. It must
observe1 followed by0, not a timer or server mount removal. The protected native
Save wrapper8743 reaches8754/2464; the server separately requires that this editor
cycle took the Save branch, not discard. After original main logic returns on that
exit, it copies two complete stable passes of the912-ID schema, with six stable
integer controls.3477 is fence-only, not added to the stored schema. No native
getter/default insertion/setter or layout write is used. The exact live Save
completion/readback fence is part of this acceptance test, not assumed proven by
the synthetic test. If the readback differs, retain that failure.

The added cache-backed test independently checks the fence's exact operand:
2462 instructions8/9 write1 to `((2<<16)|3477)<<8`;2464 instructions11/12 write0
to the same integer domain2 ID.8754 commits through8702 at6/9,19719 at12 and
selected8372 at14, before calling2464 at15. This is not a guessed local/permanent
domain. The worker's full snapshot is taken after the original main-logic call,
not from inside2464. Existing editor cache guards are unchanged.

Transport and automatic file IO run on workers, never on the native game thread.
Only an immutable bounded copy crosses to the worker. The command contains no
address, arbitrary ID, account or write operation. The adapter remains statically
imported through the existing source-generated pre-launch image, not injected.
V4 and the production executable/DLLs remain untouched. No security exclusions.

The response has magicWSC5, nonce16, sequence64, status32, byteLength32, followed by
entry/exit/capture ticks64, count16, and912 ordered(id16,presence8,[int32]) entries.
All integers on this pipe are big-endian. Max response body7000 bytes. Schema,
selection, masks and types are validated before the separate account sidecar is
updated. The pipe-owning worker serializes revisions and forces atomic replacement
before issuing a receipt. No136 or other client protocol acknowledgement is added.
The existing sidecar format remains compatible; its image field accepts the
explicit V4/V5 pins, retaining capture provenance. The previous generation remains.
Old saves and absent workspace state are not rewritten on login.

The proven restore plan is unchanged: present Custom matrices, masked metadata,
integer metadata, native8741(selected). Active8 is retained in storage, not blindly
replayed alongside native Load. Absent is not zero. No geometry translation.

## Build pins

Input production Vulkan:
`36c45c1cf6eed0c6cb0b789ca1672d685c1746def9d65d6f18d72638f0087bc9`

V5 EXE (`client/rs2client-vulkan-workspace-diag-v5.exe`):
`19323515092bbccd0090033badbc56177d0ca599216e4d702be27432cf693a13`

V5 DLL (`client/shnork_workspace_probe_v5.dll`):
`1df5b7a0f33c94932c4ecdbacd9b65ec23b8e4539e6d591d797d34c891a404c6`

Unchanged V4 schema:
`53c06c70b12ba0f68ff71a3e46db4c46fdc7f3a3792e3ce6c2656066e692119d`

Use `build_image.py ... --v5`; CMake `PROBE_FULL_WORKSPACE=ON`,
`PROBE_AUTO_CAPTURE=ON`, the above V5 image hash and thunkRVA24379392. MSVC19.50,
Visual Studio18 2026 x64 Release, /Brepro. The existing critical code/signature
checks remain mandatory. Build pins are checked before the server accepts data.

## Manual acceptance

Automated checks: focused bootstrap/action-bar/bindings/save/protocol/workspace
JUnit suite passed (one existing skip); final capture/pipe/store rerun11 passed.
The Windows ABI test uses a separate test pipe and test-JVM TCP socket, not a
game account. Native editor acceptance passed63 checks with an isolated temp
profile and explicit staged data root. C++ lookup/fence tests both passed. Python
schema/application/restart/store/fence tests passed. EXE generation and a clean
DLL rebuild reproduced the recorded hashes. These do NOT prove live NEW Save
completion, visual restoration or production readiness.

Start server with `-WorkspaceDurabilityGate -WorkspaceCaptureGate`. Jack launches
V5 normally from Windows, never by injector or security bypass. Use layoutgate2
only. Keep the same client dimensions. If security software blocks V5, stop.

1. Login; allow the existing stored layout to restore. Wait for the chat notice
   "Workspace automatic capture ready (disposable gate)."
2. Open native Edit Mode. Make an obvious NEW Backpack/layout change, then Save
   Custom1 using Save & Exit. Complete this disposable edit within two minutes.
3. Wait for "Workspace saved durably (disposable gate, revision N)." A closed
   editor alone is NOT success. If no receipt after about5 seconds, stop/report;
   do not assume it saved durably. Normal editor behaviour remains independent.
4. Verification-only capture: Ctrl+Shift+F9 once (post-Save A). This hotkey
   does NOT cause storage; the receipt/revision must already exist before it.
5. Close V5 normally. Reopen V5, login layoutgate2. Do not run commands or Load.
6. Confirm the NEW layout automatically returns. Ctrl+Shift+F9 once in this fresh
   process records post-restart A for independent readback. Do not edit first.

The unchanged V4 read/schema logic emits V5-named912-ID snapshots and controls.
`check_automatic_save.py <post-save-A> <fresh-process-A>` verifies the newer sidecar
generation, exact Custom matrices, metadata masks, stable readbacks and reports
raw active differences without waiving them. Visible confirmation is also required.
Sidecar and all payload values remain ignored/local. Bounded worker status log:
`logs/workspace-auto-v5-PID.log` (no values; at most256 lines). Server log identifies
authenticated PID and committed revision. No manual fixture import or file watcher.

Do not classify same-process Logout as this gate. Do not roll out to Jaxa after
automated tests alone. Rollback is the protected JAR/flags and untouched V4 client;
if rolling all the way back to11c7c52, use the backed-up disposable sidecar as that
older loader does not know the V5 provenance hash. Never restore character saves
as a workspace rollback.
