# Disposable account-bound durability gate

## Full client restart: PROVEN on layoutgate2 (2026-09-20)

Jack confirmed automatic visible restoration without commands/Edit Mode on initial
login, then normal full client close/reopen and login. Same client dimensions.
Initial PID3800 A SHA256
`d61d86ace873445955b5770b0afc9a876bb310223045ae50b9e3eca5cacd558f`;
restarted PID41956 A SHA256
`00cb165a12229a5aed6e4962c1069db7f60e46c2cd3350b23cfc2f72811139af`.
`tools/vulkan-static-probe/check_restart_gate.py` verifies stable typed V4 captures,
controls, different processes and exact equality of all912 entries. All720 active
fields match across these two automatic restores. All four Custom matrices match
the pinned source fixture, including absence. User-visible restoration takes about
1-2 seconds after world entry; it is not claimed to be pre-scene or flicker-free.

Do not conflate this with raw source-Custom versus automatic-active equality:
that comparison has45 different fields and8 absent fields. No new transformation
exemption is inferred. The proven result is repeatable exact state across the two
automatic sessions plus Jack's visible confirmation, with saved Custom data intact.

Same-process relog is NOT TESTED because the existing Logout path reconnects rather
than providing a genuine relog. Track that separately, not as a durability failure.
Automatic capture/update of a NEW Save is still unimplemented. The seeded fixture
does not prove that final loop. Jaxa remains excluded; no production rollout.

Prerequisite: application PID37768 is PROVEN, including all native8701 visibility
constants and Jack's positive visual confirmation. See the application gate note.

## Implemented, not a production rollout

Native950WorkspaceStore is a separate version1/revision950 bounded binary sidecar.
It stores account identity, monotonically increasing revision, exact schema/image
pins and all912 sorted presence-tagged int32 entries, followed by SHA256.
Absent is distinct from zero. The selected Custom slot must exist; all four Custom
slots and integer/masked metadata remain represented. No geometry translation.
Files are bounded to16KiB, account filenames hashed, malformed data refused, stale
revisions rejected. Writes force a temporary file before atomic replacement and
retain a verified previous generation. No fallback to non-atomic replacement.
Current caller serializes IO; no live snapshot-acceptance endpoint is exposed.

The one-time reviewed seed is EXACT PID37768 C, SHA256
`a3d22bfa661cad064859c892625e04dbe6f85f30327f4716ddef3dc793c428a8`.
Jack identified that session as layoutgate2. Native950WorkspaceGateSeed accepts
only `reviewed-layoutgate2-37768`, binds it explicitly to layoutgate2, and refuses
to replace a different existing sidecar. This is an approved offline fixture
import, NOT automatic account inference from a PID/filename and NOT a production
capture transport. Private sidecars live in ignored `workspace-state950/`.

Native950DisposableWorkspaceRestore is disabled unless explicitly launched with
`Start-950Server.ps1 -WorkspaceDurabilityGate`. It additionally requires native950,
loopback, dev tools and exact account layoutgate2. Jaxa/other accounts perform no IO
or restore. It runs once after the first native scene-complete report, loads and
validates on a bounded background executor, then queues existing setters and
8741(selected Custom) on the owning world thread. Its pending load is retired after
completion; no continuous workspace polling/synchronization.215 bootstrap and the
editor recipe are unchanged. This NEW automatic application timing is a disposable
live gate, not yet a generally proven native readiness fence.

## Live test after gated deployment

Deployment2026-09-20: implementation `fc1aa4a75d5e5fdbca1ac1617795c25dc55cd668`.
JAR SHA256 `f57d1df30e191909a1c29aa2a29f7b826de7666d8f7800cd6b759bb9a6647d58`.
Server PID15300 with explicit durability-gate flag.112 focused Java tests:
111 passed,1 existing skipped;30 Python tests passed. Exact229-byte8741 JS5
payload and HTTP200 verified, paired-cache startup preflight passed.
Backup `backups/pre-edit-20260920-003516-108` includes saves/runtime/sidecar.
All player saves hash-identical after restart, including Jaxa. The layoutgate2
sidecar SHA256 is `a118ba984a9e12c87872933b220d7a40820baf5d3114f8547f169fd4004f90c3`.
Automatic application timing/relog/full-client-restart are pending Jack's test.

Use the unchanged V4 executable and layoutgate2 only. Do not run stage/apply.
Do not save a different layout during this gate: it intentionally uses the exact
previously reviewed C capture. Automatic capture of NEW saves is not connected yet.

1. Login; wait for automatic application. Capture A and confirm the distinctive layout.
2. Relog normally; wait for automatic application. Capture B if still the same process.
3. Close the client normally. Reopen V4 and login layoutgate2. Capture A in the NEW PID.
4. Report both PIDs and whether each login restored the saved layout without manual Load.

Check Custom matrices/metadata against the seeded record and native semantic active
fields; require Jack's visible confirmation at each boundary. The reader's existing
three-capture limit remains. No new diagnostic client or security exception needed.

## Remaining before real Save-to-durable operation

The existing V4 hotkey output is manually captured and not an authenticated live
account/session channel. Do NOT treat newest-file selection as production pairing.
After this automatic restore gate, integrate an authenticated client/session capture
boundary and a post-native-commit receipt before writing subsequent revisions.
An elapsed delay or server sending8743 does not prove native execution. No production
Jaxa rollout until automatic NEW-save capture, relog and client restart all pass on
disposable accounts. The current gate does not claim that complete behaviour.
