# Privacy-clean build — 12 September 2026

Source and deployed engine/frontend/overrides rebuilt after privacy cleanup. Protocol, paired-cache preflight and 5,145 JS5 checks passed. Offline dependencies resolved successfully with all previous Gradle caches removed. Generated validation logs were preserved outside the shareable folder. See PRIVACY-CHECK.md.

Engine SHA256: `0a241431d045c88f2a4270cadf771a991e62da0c616cf8c3583718a08aad65c1`. Frontend SHA256: `42127c595d44781ed9056a81a3474758addf921e5cc8985d3e9b3d3415e82a49`.

---

# Update validation — 12 September 2026

The standalone was updated from 950RevTest while retaining portable launchers, sanitized local service settings and JS5/cache-preparation improvements. Its source was rebuilt using the bundled tools; the main project's engine JAR was not copied.

- Engine build/tests: 1277 tests, 0 failures, 0 errors, 2 skipped.
- Current lobby/ribbon overrides rebuilt and protocol verification passed.
- Both client renderers, portable paths, bundled runtimes and paired-cache preflight passed.
- All 5,145 JS5/HTTP payload checks passed.
- All 214 synchronized source/resource/report files matched the main project before building.
- Deployed engine SHA256: `f029b09cea07f9456222b62161daba87433c3821d5b53a8ad9c7b5872a7e838a`.

No standalone server or game session was launched. The existing main-project game remains running. Known exit-menu and boss-stat issues are documented in docs/UPDATE-2026-09-12.md. The older .7z archive was not rebuilt.

---

# Package validation — September 12, 2026

- Rebuilt the sanitized Ataraxia engine with bundled Java 8 and local offline Gradle dependencies: 1,187 tests, 1,185 passed, 2 existing skips, no failures/errors.
- Rebuilt the full OpenNXT frontend from 335 Kotlin and 2 Java sources with bundled Java 25/Kotlin. The original frontend JAR and patch classes were excluded from the full compiler classpath.
- Rebuilt current 950 overrides against the new frontend and engine; existing protocol vectors passed, including bank mounts, inventory menus, movement and interface packets.
- The complete Build-All chain ran under Windows PowerShell 5.1. All root PowerShell entrypoints passed parsing. The standalone protocol check needs no cache.
- Portable client initializer passed 28 checks each under Windows PowerShell 5.1 and PowerShell 7, including exact known-client reproduction, changed folder paths, spaces/Unicode, idempotence, bad seeds and path-capacity failures.
- Bundled tools passed the current 950 cache preflight against the existing project's cache read-only: UI bindings, settings, dialogue, maps/lodestones, combat assets/equipment, and implemented skill cache data.
- The bundled server successfully started on a separate loopback address and served a revision 950 configuration. The smoke server was stopped and its configuration restored. The original project stayed running and its engine hash stayed unchanged.
- Missing-cache startup and Check-Setup show cache 2691 extraction guidance. Stop succeeds safely with no bundle processes. No player session was created during package validation; a fresh interactive login was not tested in this copy.
- Sanitized source/classes/JAR checks found no remaining matches for 11 known old operational credential/token literals across 22,948 files and archive entries.

No cache, existing character saves or existing client state is included. Temporary build output and smoke-test state were removed after recording these results. PACKAGE-MANIFEST.json records the delivered files and hashes; runtime state and generated client binaries are excluded from that manifest because launch regenerates them for the current path.

## Startup performance update

The September 12 startup preparation and compressed-transfer changes were additionally validated with a fresh native client, existing-profile preservation, and 5,145 transfer checks. See [performance results](docs/performance/RESULTS.md).
