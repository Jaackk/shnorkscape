# 950OpenSource — portable local 950 project

This Windows x64 bundle contains the current 950 server, both Windows client renderers, server source, gameplay data, Java runtimes and offline build dependencies. Supply the cache separately. Every launch enables all currently implemented content.

## Current update

2026-09-20: normal **Play.cmd** now selects the unchanged, live-proven V5 Vulkan
workspace client and starts/requires the explicit Jaxa-only capture/restore
server profile. No separate manual diagnostic launch is needed. This build is
hash/path pinned to `C:\Games\950OpenSource`; it must not silently regenerate or
fall back if assets differ. See [production launch verification](docs/NATIVE-950-PLAY-WORKSPACE-LAUNCH.md).
OpenGL remains explicit and does not support V5 automatic workspace capture.

Updated from the main project on 12 September 2026. See [update notes](docs/UPDATE-2026-09-12.md) for included work and known UI issues. The older archive was moved outside this folder during the privacy cleanup. Read [the privacy check](PRIVACY-CHECK.md), then create a fresh archive before launching again.

## Play

1. Keep this proven V5 production build at `C:\Games\950OpenSource`. Moving it requires a separately validated build; the normal launcher fails closed on another path.
2. Download **Cache (Flat file)** from [OpenRS2 cache 2691 — English 950.1, September 7, 2026](https://archive.openrs2.org/caches/runescape/2691). [Direct download](https://archive.openrs2.org/caches/runescape/2691/flat-file.tar.gz).
3. Extract the archive **into this project folder**. It already contains `cache`. The result must include `cache\255\12.dat`, not `cache\cache\255\12.dat`. Allow about 24 GB for the extracted server cache, plus space for the download and the client's own local cache.
4. Double-click **Play.cmd**. No Java, Python, database, IDE or build-tool installation is required. If your graphics hardware lacks Vulkan support, use **Play-OpenGL.cmd**. A suitable installed graphics driver is required.
5. Enter a local username and any disposable password, select **World 1**, and press **Play Now**. Characters are created automatically. Use the same username to resume that character.

This is a local development login: passwords are **not verified**. Do not enter real account credentials. The server binds to this computer's loopback address `127.0.0.2`, on ports 8950/43650 and HTTP alias 80. Stop another copy using these ports first. The launcher identifies a conflicting server and its folder. Each folder's Stop button stops only its own copy. If another copy is running, use the Stop launcher in that other folder before Play.cmd here.

**Stop.cmd** closes this bundle's server and client. **Check-Setup.cmd** checks tools and cache compatibility. Launch errors remain visible; details are in `logs`.

With Windows' built-in extractor, run this from the project folder, replacing the archive path:

```powershell
tar -xzf "C:\Downloads\flat-file.tar.gz" -C .
```

Use cache **2691**. Cache **2687** is also revision 950.1 but has different scripts. Launch checks reject incompatible cache data.

## First launch and new areas

Play.cmd now prepares the client's startup assets directly from the supplied flat cache before opening the game. This is automatic, uses the bundled Java runtime and needs no internet. A fresh preparation imports 28,579 startup groups and 45 reference tables into roughly 40 MB of client databases. On the development machine it took about 27 seconds; the isolated prepared client then reached the login screen within 11 seconds. Timings vary with the device and filesystem cache.

Later launches use a completion marker and skip the import. Existing valid assets are reused; settings, shaders and characters are preserved. If you need to recheck/rebuild the prepared startup assets, close this client's window and run **Prepare-ClientCache.cmd**. It resumes safely after an interrupted preparation. It refuses to write while this copy's client is open.

`cache` contains the server's original game files; `client-state` contains the client's native databases. Keep `client-state` between sessions. World models, textures, audio and other assets outside the startup set still arrive from the local server as needed. The server now forwards compressed cache payloads without unnecessarily decompressing them first. This improves the transfer path but does not preload the entire world or promise an eleven-second first visit to every area.

## Move and back up

Use Stop.cmd before backing up or zipping. The current pinned V5 production path
is not automatically relocatable; do not apply the older portable-client recipe
to it. Keep `workspace-state950` alongside character and client-state backups.

Characters live in `players\modern950\players`; client preferences and its local cache live in `client-state`. Keep these when backing up your own game. This initial bundle contains no existing characters or client state. Logs and temporary files also stay here.

For a fresh shareable copy, exclude contents of `cache`, `client-state`, `players`, `server-home`, `logs` and `temp`. Keep CACHE.json and this README for the matching cache instructions. Review LICENSING.md before public distribution.

## Build and develop

After stopping this bundle, double-click **Build-All.cmd**. It builds/tests the Java gameplay engine, builds the full Kotlin/Java OpenNXT frontend and rebuilds the current 950 overrides, then runs protocol and cache-transfer checks. It uses the included Java 8, Java 25, Kotlin compiler and pinned offline dependencies. The normal build needs no internet.

- `Ataraxia950`: gameplay engine, skills, tests and content data.
- `OpenNXT/src`: frontend/protocol/cache-serving source, including generated mappings.
- `OpenNXT/runtime/lib`: prebuilt server and dependencies.
- `patches/classes`: compiled 950 overrides, rebuilt by Build-950Lobby.ps1.
- `tools`: porting and diagnostic source. Optional historical Python research tools require Python and their imports; normal play and Build-All do not.
- `runtime`, `compiler`, `.gradle-ataraxia`: included build/runtime tools.
- `dependencies/maven`: portable offline dependencies; build indexes regenerate locally.
- `docs/history`: prior porting notes, retained as historical context. This README is the portable bundle's entry point.

This is the current migration snapshot, not a complete implementation of every RuneScape system. Implemented skills, interfaces, banking, equipment, combat, drops/XP, teleports and world NPC population are included. Legacy source beyond the ported systems remains available for further development. Local diagnostic commands are enabled, including `;;item <id> <quantity>`, `;;npc <id>` and `;;obj <id>`.

See LICENSING.md for component ownership and VALIDATION.md for package checks.
