Current update: six950 lobby packets and rebuilt test overrides are installed. See [950-LOBBY-IMPLEMENTATION.md](950-LOBBY-IMPLEMENTATION.md) for current status. The remaining content below records the original bootstrap investigation.

# 950 revision test

Status (2026-09-08): the downloaded Windows64 client950-1 launches and renders its login screen using the supplied950cache through the local OpenNXT resource server. This is a successful client/cache bootstrap test, not a working950gameplay port.

## Launch and stop

Use the provided launcher rather than opening rs2client.exe directly. Double-click Start-950Test.cmd. It starts the test server if needed and opens the contained client. Double-click Stop-950Test.cmd to stop only the recorded test processes. Process identity is checked before stopping; neither script stops AstraNXT.

Requires the installed Java25 runtime at C:\Program Files\Eclipse Adoptium\jdk-25.0.1.8-hotspot. Start-950Server.ps1 accepts -JavaPath for another Java25 installation. Java itself is not bundled.

Test HTTP: http://127.0.0.2:8950
Test game/JS5 socket:127.0.0.2:43650
The existing947 project remains on127.0.0.1 ports80/8080/43594 and was left running. The950 test uses127.0.0.2 ports80/8950/43650. Its own port80 alias keeps native fixed-port asset downloads separate too; no extra Windows adapter or system network change is needed for this loopback address.

## Contents and isolation

- cache/: your supplied950 flat cache. OpenNXT opens it read-only; no conversion or modification was performed.
- client/rs2client.exe: locally patched950-1 executable used by the launcher.
- client-state/: separate native client cache, preferences, settings and launcher instance lock.
- OpenNXT/runtime/lib/: copied runnable OpenNXT frontend and its dependencies. The selected backend is opennxt; the947Ataraxia backend is not enabled for950.
- OpenNXT/src/: source snapshot of the existing customized OpenNXT frontend, provided for reference. This test uses the copied built JARs; it is not a rebuilt950game server or a complete standalone source build distribution.
- OpenNXT/data/clients/950/: official downloaded clients and original configuration manifests. OpenNXT's downloader retrieves all platform variants; only WIN64 was patched/tested.
- OpenNXT/data/config/: separate server config and freshly generated development RSA keys.
- OpenNXT/data/tls/: separate local development TLS certificates, including the final127.0.0.2 test address. No certificate was installed into Windows trust stores.
- OpenNXT/data/prot/950/: experimental copied947 protocol seed required by the current server startup. It is NOT a verified950protocol implementation.
- players/, temp/, server-home/, logs/: test-only writable locations.
- tools/isolate_client_storage.py: guarded, repeatable storage-path patch. This writes a separate output executable and retains both the official original and the RSA-patched input.

The native client normally shares Windows Jagex folders across versions. The test executable redirects its four verified Windows folder lookups to client-state. The patch was checked against the exact950binary and its calling convention was tested in memory. First launch created its settings/cache/lock under client-state. All445 existing shared Jagex files checked retained their sizes and modification timestamps after launch. Windows itself may still record normal process/graphics-driver activity outside this project.

The storage path is embedded as an absolute path. If this folder is moved, run tools/isolate_client_storage.py using Python3 at the new location before starting. Start-950Client.ps1 checks the stored path and executable hash and refuses an outdated location. Update the Java path if needed on another computer.

No hosts-file changes, shared launcher preference edits, Windows trust changes, or original AstraNXT file edits were used. The server runs in offline mode: it serves this supplied cache and does not fetch replacement archives from retail.

## Verified results

1. Official downloaded original WIN64 file reports950-1. Its CRC32 is2474565715 and matches its official manifest. SHA256:fc7492548627a4068e88a7a45ba4f0be93004adf246bb4b174b24ae588768b36.
2. All45 populated cache reference tables decoded, covering562,772groups. Every referenced file exists; there are no extra group files relative to these reference tables.
3.254 sampled groups passed CRC, trailer, container and decompression checks, including every group in indices2,10,26,28,32,33,34. This was not a full checksum scan of all562,772files.
4. Server opened the supplied cache and generated/signed its local resource tables successfully.
5. Actual native950.1 JS5 handshake was accepted, and tens of thousands of resource responses were served over the isolated local socket. After moving to the dedicated127.0.0.2 address, actual950 HTTP /ms requests for index40 were also served successfully by the test server; its HTTP master-table request returned200 with5878bytes.
6. The actual950 client displayed the rendered login screen and remained responsive. It continued background cache loading locally.
7. A later local x login received the lobby login response. The947 bootstrap then produced a black view and mismatched packet framing; gameplay was not reached.

## Current boundary and next step

Do not treat the login screen as proof of login or world compatibility. A subsequent local x login completed the initial login exchange and reached the server-side lobby. The client then displayed a black view while the server sent the inherited947 bootstrap packets. A rendered lobby and game-world entry therefore did not succeed. The final client is open at its rendered username/password login form; that UI is present in950. A Jagex Account upgrade panel also appeared on the initial launch. Use only local test credentials.

Native table extraction establishes that the947game packet maps cannot be reused as-is:

| Table |947 entries|950 entries|Existing opcode positions with different lengths|
|---|---:|---:|---:|
|Server-to-client|218|223|203|
|Client-to-server|130|129|111|
|Initial handshake|12|12|0|

Examples of conflicting947named opcode positions: IF_OPENSUB8 changes23 to variable-byte; PLAYER_INFO27 changes variable-short to6; REBUILD_NORMAL90 changes variable-short to6; RUNCLIENTSCRIPT121 changes variable-short to variable-byte; SERVER_TICK_END195 changes0 to1. These comparisons describe table positions, not how much gameplay logic changed. Equal lengths alone do not prove identical meaning.

The next milestone is to extract950packet names and field encodings, remap lobby/bootstrap packets, finish validating the950login response and lobby rendering, then attempt world entry with a small validated bootstrap. Only after that should the947Ataraxia handoff and game/UI bindings be ported. The unchanged initial handshake and compatible cache format already provide a working foundation.

## Reproduction notes

All Java tools ran from this folder's OpenNXT directory with its own config/keys:

- com.opennxt.MainKt run-tool rsa-key-generator
- com.opennxt.MainKt run-tool client-downloader
- com.opennxt.MainKt run-tool client-patcher --version 950 --binary-type win64 --skip-launcher

The downloader has no historical-revision pin: rerunning it later obtains the then-current official client, so retain the current950originals and manifests.

Final check: the original947 server process6616 remained running, and all445 monitored existing shared client files retained their sizes and modification times.

The initial launch used verbose resource logs as evidence. Future server launches suppress per-archive informational logging. Logs are overwritten on a fresh server start, while first-start logs are retained separately. The downloaded configuration and tool logs remain under logs/.





