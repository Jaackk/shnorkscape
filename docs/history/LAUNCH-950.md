# Launch the full950 project

Double-click **Start-950Test.cmd**. No arguments are needed. If already running, the existing full server/client session is reused.

Every launch includes the Ataraxia950 backend, all implemented skills/combat/UI, walking, collision, map regions, full-world source NPC spawns and local development commands. Cache verification and the isolated950 save/data directories are always used. Content still awaiting a port is not enabled by these defaults.

**Stop-950Test.cmd** stops this project's client and server. For command-line use, `./Start-950Test.ps1` starts everything; `./Start-950Server.ps1` starts only the fully configured server. Vulkan remains the default client renderer; `-OpenGL` is available for hardware compatibility.

Old feature arguments are accepted for compatibility but no longer change the content profile, including old Minimal/No* or Lumbridge-only arguments. Changes and previous launcher copies are recorded in implementation-backup/2026-09-12-full-launch-defaults.
