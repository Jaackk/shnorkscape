# Disposable native setter gate

## PID33492 live result

Jack confirms both commands ran on disposable `layoutgate1`, never Jaxa.
Exact files: `logs/workspace-static-v3-33492-{A,B,C}.json` and their controls.

SHA256:
- A: `c00a792052d589d73dffdc449a2d463113c4709be4579d4e08d45c5483021857`
- B: `51c96af4f1da9312895497636c25676d4846c0e031be1e7de68886a5701127c2`
- C: `9dc0b0be23cbdb5160d22931d3f46c9f91722fe2d7e990b2a18ebdca00f77080`

The existing checker was run unchanged. Same ordered/pinned-image session, all
six bootstrap controls and absent -> int32 4097 -> present int32 zero PASS.
This proves the setter reaches the live domain and can instantiate/overwrite3296.
However the COMPLETE strict gate returns failure for other IDs2920/2921. Do not
claim an unqualified pass or suppress those differences in the checker.

Exact schema-aware changes (A->B; B=C):
- active8 actor1003 argument3, varbit19359/parent2920 bits12..23:
  normalized vertical field3060 ->2833. Other fields in that parent unchanged.
- active8 actor18 argument5, varbit19362/parent2921 bits12..23:
  height144 ->184. Other fields in that parent unchanged.

No other captured IDs changed. B->C changes ONLY3296. The unrelated changes
look like panel resizing/reflow, but their cause is not established by these
snapshots. Asked Jack whether a panel was resized/expanded between A and B.
Do not call the actor a particular named panel without descriptor evidence.

Until that discrepancy is accounted for, the prerequisite for the complete
application gate is not fully satisfied. No912-ID replay, client patch, production
deployment, persistence or Jaxa modification followed this result. Preserve the
successful targeted readback evidence; this does not reopen acquisition research.

No server/client changes or restart required. Existing native development command
`;;varc` calls `Native950Packets.varcLarge` directly after local/native/active-player
checks (Native950DevelopmentCommands.java:379). Do not add a second command or an
injector. This command itself is NOT account-restricted: isolation is enforced by
using a fresh disposable account, never Jaxa. Do not claim a code-level Jaxa guard.

## Manual execution

1. Close Jaxa's client normally. Launch the existing V3 diagnostic using the exact
   Windows Run command in `tools/vulkan-static-probe/README.md`. No new executable.
2. Login with a NEW disposable account, e.g. `layoutgate1`, not Jaxa. If the launcher
   automatically enters Jaxa, STOP without issuing either command. Change the
   login through the normal login UI; do not edit a save or Jaxa's account data.
3. Wait for the world to settle. Do not open Edit Mode or any presets. Press/release
   Ctrl+Shift+F9 once for A. Check A's3296 entry is `found:false`; if present, stop.
4. Enter exactly `;;varc 3296 4097`. Press/release Ctrl+Shift+F9 for B after the
   command response. A response is not acceptance: the reader must find int32=4097.
5. Enter exactly `;;varc 3296 0`. Press/release Ctrl+Shift+F9 for C. The reader must
   now find present int32=0, distinct from A's absence.
6. Close the client normally. Do not Load this incomplete Custom1. Return to the
   production client afterward. No character save is manually edited or deleted.

Only parent3296 is written, twice, through the existing server channel.4097 packs
small nonnegative 12-bit fields; zero tests native presence semantics. Both writes
affect an otherwise absent, unused Custom slot, not the active8 matrix. The normal
world login of this disposable account may create/update its own character profile;
no workspace durability exists and Jaxa is never the target. The diagnostic remains
read-only; its `nativeWrites:false` refers to the diagnostic, not the server command.

## Verification

Run `tools/vulkan-static-probe/check_setter_gate.py` with the three exact JSON paths.
It reuses the schema-aware snapshot validation, requires one ordered pinned-image
session, checks all six controls and the absent ->4097 ->present-zero transition,
and rejects changes to any other captured workspace ID. Unexpected changes require
inspection, not a weakened pass. Outputs do not establish account identity; confirm
the disposable login independently through the manual test/server session evidence.
No raw JSON or account data is committed.

This is only the native integer setter gate. It does not prove full matrix restore,
actor application, post-bootstrap ordering or durability. If it passes, proceed to
the complete912-ID captured-fixture/application gate on a disposable account, using
native wrapper semantics rather than raw-memory writes. Do not enable production
storage or replay, and do not apply a layout to Jaxa, until that gate passes too.
