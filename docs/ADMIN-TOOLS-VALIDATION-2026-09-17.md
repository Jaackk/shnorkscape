# Admin tools validation - 17 September 2026

- Java engine compiled with bundled JDK 8 and Gradle 4.9, offline.
- Command: `Build-Ataraxia950.ps1 -Tasks @('test','--tests','*Native950*','jar')`.
- Result: 135 test suites, 1,259 cases; 1,257 passed, 2 skipped, 0 failures, 0 errors.
- Skips: `Native950QuantityInputTest.independentlyInspectedFilesPassPinsAndEverySingleFileDriftIsRejected` needs a missing 950 quantity-input evidence JSON; `Native950VarWireTest.serverPacketNamesAgreeWithVerifiedNamesTomlWhenItIsAvailable` needs optional verified packet-name evidence. The running cache preflight separately passed its quantity-input validation.
- New tests cover explicit account/admin grants, moderator/ordinary-account rejection, remote and disabled-mode rejection, dead/locked-player rejection, malformed input, independent toggles, real energy/prayer consumption after disabling, direct HP protection, lethal native hits, logout cleanup, cache-defined stat caps and preservation of higher XP.
- Initial focused test run found a test-fixture mistake: Netty cached the fake local remote address. Replaced it with a separate remote channel; the final remote permission test passes.
- Engine JAR built and deployed to `OpenNXT/runtime/lib/ataraxia-950-1.0-UNTRACKED.jar`.
- `Build-950Lobby.ps1` rebuilt the existing Kotlin overrides against the deployed engine successfully. Existing deprecation/unchecked-cast warnings remain; no frontend source was edited.
- `Start-950Server.ps1` completed cache preflight and started server PID 21812. Startup log confirms game bind; local ports 80, 8950 and 43650 were listening on 127.0.0.2.
- Previous runtime JAR, compiled overrides and character files were copied to `backups/admin-tools-20260917-002358` before deployment. Jaxa's current character file SHA-256 matched that backup after startup. No character stats/inventory were changed by the implementation or tests.
- Test XML and HTML: `Ataraxia950/build/test-results/test` and `Ataraxia950/build/reports/tests/test/index.html`. Deployment build log: `logs/build-ataraxia950.log`; override log: `logs/build-950-lobby.log`; cache/startup logs: `logs/cache-preflight.log`, `logs/server.out.log`, `logs/server.err.log`.

No manual in-game test was performed. Startup/listener/cache checks do not establish manual command UI, boss, ability or full gameplay correctness. The native engine bootstraps further gameplay state on client admission. Use Jaxa and `;;devhelp` for the new tools; `;;devstatus` reports the independent toggle states. `;;max` changes saved levels/XP only when explicitly invoked.
