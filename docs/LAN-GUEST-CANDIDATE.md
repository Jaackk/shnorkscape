# LAN Guest Candidate

Status: implementation/testing candidate. A real second-PC Vulkan login has not
yet been verified. This is private LAN testing, not public internet hosting.

## Production activation (September 22)

`Enable-Home-LAN.cmd` requests normal Windows administrator approval. Its script
backs up the selected connection category, sets the approved physical home
interface Private, and creates `SHNORKSCAPE-Private-LAN-TCP`: inbound allow,
Private only, bundled Java executable, TCP80/8950/43650, selected local address
and interface, RemoteAddress LocalSubnet. No router/security bypass is involved.

The ignored `server-home/lan-host.json` enables LAN across ordinary Play.cmd
server starts. If the approved interface/IP is missing or no longer Private,
startup falls back to loopback only. With no configuration the default is still
loopback only. DHCP changes require updating the host configuration/firewall
local address and the guest's single `LAN-HOST.txt` file, not source edits.
To disable LAN, set `enabled` false in that local JSON, close clients, stop the
server, then use normal Play.cmd. The optional firewall rule can be removed by
name through Windows Firewall; do not remove unrelated rules.

`NativeLanAccess --generate` uses the same invitation validation and credential
format as console provisioning. It creates a random 144-bit password only in a
new handoff file beside the private credential store, never stdout/argv/Git.
Existing profiles and protected names remain forbidden. The live credential
and handoff files are restricted to the host user, SYSTEM and Administrators.

`Build-LAN-Guest.ps1 -LanAddress <private-ip>` creates the actual ZIP, preconfigured
with `LAN-HOST.txt`, `Play SHNORKSCAPE.cmd`, a friend-specific README and the
unchanged hash-pinned non-diagnostic Vulkan client. The friend extracts to
`C:\Games\950OpenSource` because that existing binary uses a fixed isolated
storage path. This package must not overwrite another installation. It has no
password, server, saves, V5 DLL or workspace capture tooling.

Ethernet host and Wi-Fi guest can share the same home LAN; guest-network/client
isolation may prevent this. Second-PC login, simultaneous visible players and
combat/disconnect behaviour still require the physical Wi-Fi client test.

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
   are unchanged. Without the new opt-in local JSON, ordinary startup is local.

The server validates credentials before opening LAN listeners. It binds only
the selected RFC1918 address plus its original loopback address. TCP80/8950
serve the existing HTTP/JS5 routes; TCP43650 handles lobby/world traffic.
No database, account store or development endpoint is exposed. The optional
administrator-approved script above configures the firewall, never the router.
If Windows Firewall blocks the test, review a
Private-profile rule scoped to these ports and LocalSubnet. Do not enable a
Public-profile rule or router forwarding. Do not disable security software.

## Guest PC

Build-LAN-Guest.ps1 packages the existing hash-pinned Vulkan executable and a
normal Windows launcher, without modifying the executable. Transfer the ZIP
privately. It contains no Jaxa data, passwords, saves, V5 DLL or workspace files.

Extract to `C:\Games\950OpenSource` on the guest PC. This location is required by
the already-existing isolated client build. Double-click Play SHNORKSCAPE.cmd
and log into the invited account/password; the host address is preconfigured.
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
