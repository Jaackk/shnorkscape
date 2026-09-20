# LAN Guest Candidate

Status: implementation/testing candidate. A real second-PC Vulkan login has not
yet been verified. This is private LAN testing, not public internet hosting.

## Host PC

1. Close the game normally before restarting the server.
2. Once the new server candidate is deployed, run `New-LAN-Guest.cmd` in a normal
   Windows console. Provision Nooby with a unique test password. Existing player
   profiles, Jaxa and diagnostic accounts cannot be claimed by this command.
   Password input is hidden and never passed in command arguments.
3. Stop the previous server normally with the existing Stop.cmd workflow after
   closing the client. Run `Start-LAN.cmd`, entering the host's private IPv4
   address from `ipconfig` (Ethernet/Wi-Fi, not a VPN or169.254 address).
4. Use the ordinary `Play.cmd` to play locally. Its client and workspace path
   are unchanged. Restart with ordinary Play.cmd to return to loopback-only mode.

The server validates credentials before opening LAN listeners. It binds only
the selected RFC1918 address plus its original loopback address. TCP80/8950
serve the existing HTTP/JS5 routes; TCP43650 handles lobby/world traffic.
No database, account store or development endpoint is exposed. No firewall or
router changes are automated. If Windows Firewall blocks the test, review a
Private-profile rule scoped to these ports and LocalSubnet. Do not enable a
Public-profile rule or router forwarding. Do not disable security software.

## Guest PC

Build-LAN-Guest.ps1 packages the existing hash-pinned Vulkan executable and a
normal Windows launcher, without modifying the executable. Transfer the ZIP
privately. It contains no Jaxa data, passwords, saves, V5 DLL or workspace files.

Extract to `C:\Games\950OpenSource` on the guest PC. This location is required by
the already-existing isolated client build. Double-click Play-LAN-Guest.cmd,
enter the host's private address, and log into the invited account/password.
Do not run this launcher in the host server installation. If Windows/security
software blocks it, stop and report the block; do not bypass it.

The guest downloads cache content from the host. Initial loading may take time.
Guest workspace durability is intentionally not enabled; Jaxa-only behaviour
has not been broadened.

## Security boundary and acceptance

Remote fresh lobby/world logins require account-specific PBKDF2-HMAC-SHA256
credentials (600,000 iterations, random128-bit salt,256-bit verifier), bounded
input/storage and per-address attempt limits. No shared master password exists.
Credentials live only under the ignored server-home directory. Provisioning
uses forced temporary-file writes and atomic replacement, and refuses existing
characters rather than permitting account takeover.

The inherited latest-account-by-IP reconnect shortcut is local-only. LAN
reconnects require the prior authenticated128-bit session keys, a matching peer
and the existing server challenge. Tokens expire after two minutes and are
consumed once. Unknown/expired tokens fail closed; a fresh lobby login is the
recovery path. Native client compatibility with this corrected handoff still
requires the second-PC acceptance test; no IP-only fallback is permitted.

Before calling LAN playable: verify wrong password rejection; Nooby fresh login
through lobby into world; Jaxa remains independent; both players see each other;
ordinary attacks, damage attribution and disconnect cleanup; no remote access
to dev commands; normal local Play.cmd/workspace behaviour unchanged.
