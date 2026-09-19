# Complete disposable fixture/application gate

Prerequisite: PID33492 setter readback passed by reviewed evidence; the two
non-target changes were confirmed manual Chat/Action Bar movement with matching
cache mappings. Keep the original strict checker result, not a global ignore rule.

## Complete fixture capture ready: V4

V4 is the same read-only static diagnostic plus exactly five metadata parents:
3295/3380/5215/5352/2912. Total912 IDs. Native lookup, wrapper, no-native-writes and
Ctrl+Shift+F9 A/B/C controls are unchanged. V3 and production binaries remain.
No server build, restart or new command is required for fixture capture.

EXE: `C:\Games\950OpenSource\client\rs2client-vulkan-workspace-diag-v4.exe`
SHA256: `3d4e432e8cb81d5b83cd3cb2064669228d24231779ae997fe78335a81e43364d`

DLL: `C:\Games\950OpenSource\client\shnork_workspace_probe_v4.dll`
SHA256: `c1da680c2a067f00538f273b9b5de990d8a80acfec1c958f2830d31f9c01c5df`

Capture schema file SHA256:
`53c06c70b12ba0f68ff71a3e46db4c46fdc7f3a3792e3ce6c2656066e692119d`

Reproduce with `generate_v4_schema.py`, `build_image.py --v4`, and CMake
`-DPROBE_FULL_WORKSPACE=ON` plus the exact EXE hash and thunk RVA24379392.
V3 remains the default build. No runtime injection, native setter, or security bypass.

### One manual capture needed

Close any current client normally. Launch V4 through Windows Run:
```
"C:\Games\950OpenSource\client\rs2client-vulkan-workspace-diag-v4.exe" "http://127.0.0.2:8950/jav_config.ws?binaryType=2&baseConfigSource=patched&localRewrite=1&hostRewrite=0&lobbyHostRewrite=1&gameHostOverride=127.0.0.2&gamePortOverride=43650&contentRouteRewrite=0&worldUrlRewrite=0&codebaseRewrite=0&downloadMetadataSource=patched"
```

1. Login to disposable **layoutgate1**, never Jaxa.
2. Enter native Edit Mode, arrange an obvious layout, Save & Exit to Custom1.
3. Once settled, press/release Ctrl+Shift+F9 ONCE. This A is the complete saved
   fixture, not a new absent-parent test. Record the PID and a screenshot.
4. Stop there. Do not enter setter commands or repeat the old A/B/C test.

The file is `logs/workspace-static-v4-<PID>-A.json` (plus A-controls). Expected:
snapshot-stable,912 items,nativeWrites:false. If refused, stop. Security software
blocking the image also means stop, not bypass it. Raw fixture stays local/no Git.

This capture is necessary: V3 did not read four of the five additional parents
at all, and its control-only2912 lacks the other per-slot metadata. Do not invent
them or combine values from different sessions/accounts. This is completing the
fixture for application, not re-proving acquisition. Save generates a legitimate
matrix rather than using the deliberately incomplete3296-only setter fixture.

## Approved source fixture: PID35192

V4 A and A-controls were captured on layoutgate1 (Jack confirmed account), after
Save Custom1 and settling. Both are local-only, not committed to Git.
Fixture SHA256: `958508114f036246a433060ab0b02e6afccbd964e52e567e9efd8dac4efec1e0`.
Controls SHA256: `dc61ee972be3a91cdd328aeed7102f3e1fec188d6270c472ac0e815dc0616881`.
All912 records stable and correctly typed/presence-preserving. Six bootstrap
controls exact. Custom1 has178 present parents;712 readable fields equal active8.
6056/6057 (actor1039's eight fields) are absent, not zero. Other Custom matrices
are absent. Active8 has180 present parents. Metadata8372/8373=6,8374/8269=0;
other token entries absent. This is a complete capture scope, NOT912 present values.

Validate with `tools/vulkan-static-probe/check_application_gate.py`.

## Disposable application command

`;;layoutfixture stage` and `;;layoutfixture apply` are strictly limited to
local native950 **layoutgate2**, with existing developer tools enabled. Jaxa and
layoutgate1 are refused. No file path, ID, value or target argument is accepted.
The source fixture and schema hashes are pinned in Native950LayoutFixture;
raw paired-cache Load/matrix helper and varbit evidence is pinned separately.
All inputs are checked before writes. Missing/changed files fail closed.
The two actions are ordered and one-shot per channel. Nothing runs on login.

Stage sends only present Custom parents, low-five-bit metadata via native CLIENT
varbit setters (not PLAYER varbits), and present integer metadata. It skips absent
entries and retains active8 for comparison, not blind replay. Apply invokes
native8741(6), retaining its8884/8885/8781 guards, callbacks and normalization.
This is a gate for that wrapper outside Edit Mode, not a claim of visible success.
It never calls8743/8754 or modifies the working editor lifecycle. Partial send
failure requires a fresh session, not repeated staging. No durable state is stored.

### Manual target test after deployment

1. Close V4 normally. Launch the same V4 build; use a **fresh layoutgate2** account.
   Never use Jaxa or layoutgate1. Keep the same client dimensions as source capture.
2. Wait for the normal world/workspace, then Ctrl+Shift+F9 once: target A.
   Do not enter Edit Mode or create a Custom preset in the target session.
3. Enter `;;layoutfixture stage`. Wait for the queued response and settle. Capture B.
4. Enter `;;layoutfixture apply`. Allow native callbacks to settle. Capture C.
5. Report the new PID and whether the distinctive saved layout visibly returned.
   Stop on any error/dimming/stuck UI; do not try additional setter/script commands.

Compare with `check_application_gate.py <target-A> <target-B> <target-C>`.
It checks exact V4 scope/session/order, staging readback, absent-vs-zero and masked
sibling preservation. It reports Custom changes during native application and
Custom/active semantic correspondence; it does not silently waive normalization
or infer visual success. Native snapshot nativeWrites:false refers to the reader;
the explicit server commands DO set client variables through the native protocol.

## Application acceptance (pending live target test)

After validating the exact912 fixture, prepare a narrowly scoped server fixture
action restricted to a separate fresh disposable target session/account. Do not
paste hundreds of commands, add a native-memory writer, or replay on Jaxa.
The action must accept only the hash-pinned fixture and schema, preserve absent
values, use native varc setters for evidenced integer parents and native varbit
setters for masked0..4 slot metadata, preserving the neighbouring flag/sibling bits.
The source fixture account and explicitly authorized disposable target must be
recorded separately; this is NOT a production cross-account transfer facility.

Read the complete912 state before/after setters in V4. Then exercise the verified
native Load/application wrapper and capture its settled result; bare8702/8700 is
not a visible application recipe. Native actor/layout callbacks own normalization,
tabbing and dimming. Keep the healthy215 bootstrap and working editor unchanged.

Require both a matching schema-aware applied representation (accounting for native
transforms) and Jack's visible reconstruction confirmation. Matrix acceptance alone
does not pass. Only then enable work on account-bound persistence. At this point
no full matrix has been replayed, no storage enabled and no application pass claimed.
