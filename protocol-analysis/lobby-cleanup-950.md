# 950 lobby cleanup - 12 September 2026

The old lobby mounted account-summary interface814 at906:37. In the paired950 cache,906:37 is the tab underlay, so the summary's fixed-height content overlapped the tab buttons. Its authored placeholders were the visible PLAYER NAME,1234 currency and membership-expiry strip.

## Installed change

Only the950 branch in OpenNXT/src/main/kotlin/com/opennxt/model/lobby/LobbyPlayer.kt changes. It opens native root906 with world-list interface910 at906:45 and selects the authored World tab with script3059(1). It no longer mounts814. Display-name component906:118 now receives the account name;906:119 receives local-server status. The account/store icon container906:120 and promotion container906:139 are hidden. The native world selector and Play Now are retained.

The950 world list advertises the one actual local World1, with the current World player count instead of five sample worlds and fake counts. Other revisions retain their previous behavior. This does not implement the remaining lobby social/news/settings pages.

Native950LobbyLayout verifies the active OpenNXT filesystem once before using these bindings. It verifies decoded group SHA256 values; Java Cache.STORE is deliberately not used, because lobby startup precedes the Ataraxia world's initialization.

| Cache group | SHA256 |
| --- | --- |
|3/906|ff04c4a3f47b03da3d8f7e8b128cba8e1d2508017286399146b62fa005ba8f06|
|3/910|997e4eb11dbd3949afe3fd24f3abf8e9f48cf362100b151070f4357d41b2dc0f|
|12/3059|d99a945a395266eb40fd5ab898cdc73206457adb543b18202615d7bd36830df1|
|12/3060|279fe594e06a0e337badc90e7768f6637b50dce26a3aac95bf78b48f88b50a0d|

## Validation and deployment

- Build-950Lobby.ps1 completed; Kotlin overrides installed in patches/classes. Engine JAR and client binary were not modified.
- Existing Verify950Kt:19 passes, including backpack, bank mount/close, movement and packet encoding. Log:logs/verify-950-lobby-cleanup.log.
- tools/VerifyLobby950.java ran against the actual cache and actual LobbyPlayer.added() using an EmbeddedChannel. All four cache pins pass; correct root/world-child ownership, absence of814, single World1 and seven bootstrap packets pass. No sockets, authentication or player-save writes in this check. Log:logs/lobby-cleanup-verification.log.
- Mandatory startup cache preflight passed. ServerPID12164 bound127.0.0.2 ports80/8950/43650. Vulkan clientPID16152 reached the sign-in screen. The process records under logs remain authoritative.
- Visible post-login layout and Play Now need manual confirmation; user was asked to sign in and stay in lobby. No automated authentication performed.

Original source:implementation-backup/lobby-cleanup-950/LobbyPlayer.kt. Latest disconnected character save and former process records/logs:implementation-backup/lobby-cleanup-950/before-deploy-20260912-114844. Prior source can be restored and Build-950Lobby.ps1 rerun for rollback. Do not roll back the engine JAR or skills changes for this UI-only fix.
