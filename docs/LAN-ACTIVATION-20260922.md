# Private LAN activation

Source checkpoint: `7ac7bfa`, based on protected combat deployment `a479b86`.
No combat, native workspace/editor, client binary or login protocol changes.
Only guest provisioning/packaging and opt-in host startup configuration changed.

## Deployed and verified

- Server PID13744: specific `192.168.0.91` and existing `127.0.0.2`, TCP80/8950/43650.
  No wildcard/public/VPN listener, router forwarding, UPnP or DMZ changes.
- Actual LAN-address jav_config advertises LAN game/content/world routes, not
  loopback. Both HTTP ports return the expected322443-byte JS5 witness.
  Game/lobby TCP43650 connects. Normal Play.cmd config still advertises loopback.
- LAN requests to debug, error-report and private-file routes return403.
- Fresh full engine test run:1437 tests, zero failures/errors, two skipped.
  Full OpenNXT build and override build pass; deployed-classpath credential,
  generated invitation, remote handoff and Kotlin ABI tests pass.
  Verify950Kt and5145 JS5 payload checks pass.
- All seven character/workspace files byte-identical to deployment backup
  `backups/pre-edit-20260922-165407-243`. Jaxa-only workspace enrollment unchanged.
- New independent invitation `nooby`; not a historical Darkan/Vernox migration.
  Password is only in ignored `server-home/nooby-invitation.txt`, restricted ACL.
  No guest save exists until first normal login. No guest developer enrollment.

Engine SHA256 (unchanged deployment):
`b1c85a08f48bc3b38b1cb50edefda6a93213789176f62702476f8d05c5f00d85`

OpenNXT SHA256:
`abfc172d9106769ce30270c69fa250fe1b4fc5b816069a7bae01036006af768d`

Actual friend ZIP:
`dist/lan-guest-20260922-165119.zip`

ZIP SHA256:
`4f3718b0efc900f440a6cc05a7f9f7add7e5acb9076f55ee8284c40490042e3d`

Inspected ZIP contains six entries only: unchanged non-diagnostic Vulkan EXE,
two CMD launcher names, launch PowerShell, LAN-HOST.txt, README-FIRST.txt.
No credentials, saves, sources, server cache, diagnostic DLL or workspace data.

## Windows gate verified after operator approval

Jack ran the corrected launcher (`ddacf60`) and supplied its successful result.
Read-only verification confirms Ethernet is now Private, the named inbound
allow rule is enabled for Private only, TCP80/8950/43650, local192.168.0.91,
RemoteAddress LocalSubnet, and the exact bundled java25 executable. The ignored
host JSON is enabled with Ethernet/address matching the running listeners.
The current PID13744 already has LAN enabled, so no additional restart is needed.
Normal subsequent Play.cmd starts retain the approved opt-in configuration.
Physical second-PC acceptance remains pending.

### Earlier activation state (superseded)

The assistant process is not elevated. Jack approved trusted home Ethernet;
`Enable-Home-LAN.cmd` was supplied for normal Windows administrator approval.
At the last check Ethernet was still Public and the proposed
`SHNORKSCAPE-Private-LAN-TCP` rule/config did not exist. Do not describe the
firewall as configured or the physical second PC as proven yet.

The running server was explicitly started with `-LanAddress 192.168.0.91` and
all three existing workspace switches. Normal Play.cmd can reuse it unchanged.
After the Windows script succeeds, its private local JSON preserves the opt-in
across subsequent normal server starts. Until then, a normal fresh server start
remains loopback-only. See LAN-GUEST-CANDIDATE.md for disable/rollback.

## Physical acceptance still needed

No native executable was launched by the agent. Host-to-own-LAN-IP checks do
not prove Wi-Fi/firewall reachability or the real native guest lobby handoff.
Use the main home Wi-Fi, not an isolated guest network. No forwarding needed.

Jack uses normal Play.cmd/Jaxa; Nooby extracts the bundle to the documented path
and opens Play SHNORKSCAPE.cmd. Verify wrong-password rejection, correct guest
login/world entry, both players visible, independent movement/combat and guest
disconnect leaving Jaxa unaffected. Existing automated owner/account/permission
checks are not a claim that a real simultaneous two-PC game has already passed.
If handoff fails, retain strict authentication; never fall back to IP identity.
