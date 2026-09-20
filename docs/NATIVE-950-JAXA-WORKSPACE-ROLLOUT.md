# Guarded Jaxa workspace rollout

## Final live acceptance: PROVEN, 2026-09-20

Jaxa automatic native Save -> account storage -> full client close/reopen ->
automatic restoration passed visually and structurally in Jack's intended
maximised/fullscreen setup. This completes the scoped Jaxa durability acceptance.
Do not reopen workspace research. Same-process Logout remains separately untested.

Exact evidence (local/ignored): post-save V5 PID38656 A at01:56:30; fresh-process
PID21760 A at01:57:36, with their companion controls files. Both validate the
pinned V5 image, stable912-ID schema, typed absence/presence and six controls.
`check_automatic_save.py <38656-A> <21760-A> jaxa` accepts only the explicitly
selected account's checksummed record; no filename-derived account ownership.

Server log132 authenticates jaxa/PID38656,144 commits revision1; fresh login162
loads jaxa revision1,163 authenticates PID21760,164 queues native application.
Client log records native-exit-snapshot-sent then durable-receipt before the manual
post-save verification. No hotkey-triggered storage, seed import or operator action.
Jaxa sidecar revision1 SHA256:
`e7d26985180928636a6fd3846c5133e660ad9ce3a3955297de28a72aa95d426a`.
All912 stored entries equal post-save; all Custom matrices/presence and integer /
masked per-slot metadata equal fresh-process readback. Jaxa's record differs from
the disposable record in114 IDs. This is Jaxa's own Save, not the disposable seed.

Active8 is reconstructed by native Load, not verbatim replayed:669/720 raw fields
match post-save,51 differ, none are missing. These differences remain explicitly
reported; this proves exact persisted Custom data plus user-confirmed visible
restoration, NOT raw equality of every active field or a newly inferred universal
normalization rule. No new investigation or behavior changes were warranted.

Finished runtime remains source3ea2da1 / JARafc580f8...0a54c461 with the explicit
Jaxa-only switches. Capture/restore excludes disposable accounts in that mode;
fixture commands cannot run on Jaxa. Normal launch without gates still starts no
capture endpoint. Retain dormant disposable tooling for regression/recovery, not
as part of Jaxa's workflow. Manual snapshot hotkeys are verification-only and
unnecessary for normal saving. No client/server restart or production edit was
performed during this final verification checkpoint.

Fresh save/profile/sidecar backup and retained local logs/captures:
`backups/pre-edit-20260920-015905-063`. Live player saves can legitimately change
through gameplay; do not compare them to pre-login hashes and call that corruption.
This verification does not write player profiles or workspace state.
Verifier checks:36 Python tests pass, including explicit account mismatch rejection;
the existing disposable revision2 comparison also still passes. Runtime JAR hash
was rechecked unchanged. Only offline verifier/tests and handover documentation
changed in this final checkpoint; no rebuild/deployment was needed.
Earlier pending-live instructions below are historical and now superseded.

## Proven baseline and limits

Disposable automatic NEW Save -> durable revision2 -> fresh V5 process automatic
restore is PROVEN, checkpointd54534d. Source runtime6999ef8, deployment recordc6b9616.
All912 stored entries match PID39788's post-save capture. Revision2 SHA256:
`f77127fadbf5554b72a68b465feb0758ff86ad03027895d4988b4b39a8d6f9c8`.
All Custom matrices/presence and masked metadata match PID26564 after restart.
21 Custom parents differ from old seedrevision1. Jack confirmed NEW layout visually.
Active8 is rebuilt by native8741 and not a raw replay:693/720 raw active fields
match,27 differ, none missing. Do not claim raw whole-state equality or infer a
universal normalization rule for these differences. Same-process Logout is separate.

## Reviewed eligibility change

`-WorkspaceJaxaRollout` requires both `-WorkspaceDurabilityGate` and
`-WorkspaceCaptureGate`. It selects ONLY canonical authenticated account jaxa,
REPLACING layoutgate2 eligibility for this launch. Without it, the old disposable
scope remains. Without the two ordinary gates, capture stays off. Remote/non-native
clients remain excluded; local development-profile checks still apply.
No arbitrary account argument, wildcard, global capture or client-claimed identity.
The manual layoutfixture command remains layoutgate2-only even in Jaxa mode.

All existing protections remain: exact V5 EXE/DLL pins, OS peer PID and exact
authenticated game socket ownership, reciprocal server check, random nonce,
editor sequence, Save-only one-shot claim, close revocation, schema/type/mask bounds,
atomic checksummed account sidecar with previous generation, no character-save
writes by workspace storage. Decode uses a fixed validation-only dummy account;
the stored account always comes from authenticated Player, never payload or filename.
The pipe is still exclusive/single-instance: this is a single-local-client guarded
rollout, NOT a reviewed multi-client/public deployment. Native client/editor and
normal startup profile are unchanged. Missing Jaxa sidecar means healthy defaults;
no disposable fixture is copied to Jaxa. No automatic client deployment/launch.

## Backups and rollback

Pre-edit GitHub backupc6b9616 and local snapshot:
`backups/pre-edit-20260920-014125-052` (players, runtime, workspace sidecars).
Retain a fresh pre-deploy backup and original Jaxa hash before activation.
Disable all workspace flags and return to backed-up runtime to disable rollout;
use V5 or untouched production client as appropriate. Do NOT restore an old
character save just to roll back workspace behavior. Retain workspace sidecar
separately; never copy layoutgate2 state into Jaxa.

## Final Jaxa verification (pending)

Deployment2026-09-20: source3ea2da1044612a7c7c013dec46f8c72906d768af,
serverPID40312, all three explicit switches verified in the running command line.
Exact candidate/runtime JAR SHA256:
`afc580f8de6bcf86414fed37923e5a6cc1c1ee14fc94ddf980a25dee0a54c461`.
68 focused tests passed (capture/store/pipe/fixture/bootstrap/bindings/actionbar).
Override compilation completed before restart; HTTP200 and exact229-byte native8741
JS5 payload verified. Production/V5 EXE and V5 DLL pins unchanged.
Fresh pre-deploy backup:`backups/pre-edit-20260920-014759-308`.
All backed-up player/profile files are byte-identical after deployment. Jaxa SHA256:
`7c1de323f5fc63f54b7fc2d717385b4c2cc9e9b738338b6ed3078cddfce928e6`.
No Jaxa client was launched and no Jaxa workspace was seeded. Its live result is
still PENDING; disposable success does not substitute for that verification.

Only after guarded deployment/readiness is confirmed, launch the existing pinned
V5 executable normally using the same localhost configuration as the disposable
test. No injector/security bypass. Use the same client dimensions throughout.

1. Login Jaxa; wait for `Workspace automatic capture ready`. No seeded restore is
   expected if this account has never saved a workspace sidecar.
2. Open native Edit Mode, make a recognizable change, Save & Exit to Custom1.
3. Wait for `Workspace saved durably (revision N)` before closing. A closed editor
   alone does not prove durability. If no receipt, stop/report.
4. Ctrl+Shift+F9 once for independent post-save verification (not needed to save).
5. Close V5 normally with X; launch a fresh V5 process and login Jaxa.
6. Without commands/editor/Load, confirm the NEW layout automatically returns;
   wait for settle/ready, then Ctrl+Shift+F9 once. Report visual result.

Correlate authenticated account/PIDs and loaded/stored revision, verify912-ID
representation and controls, exact Custom matrices/masked metadata, and report
active native differences separately. Jaxa visual success must not be inferred
from the disposable pass or automated tests. Full restart only; broken Logout
does not invalidate this test.
