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

## Subsequent application (not yet enabled)

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
