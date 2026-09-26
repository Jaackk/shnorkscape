# Standalone cache performance changes — September 12, 2026

## Delivered

- Automatic startup-cache preparation integrated into Start-950Client.ps1 and therefore Play.cmd; uses only bundled Java/JDBC.
- 28,579 groups from 28 confirmed startup indexes and all 45 reference tables. Native ZLB representation, canonical multifile offset directory, full version and storage-mode CRC are preserved.
- Prepared statements, batches and per-index transactions; existing valid rows retained, wrong-mode rows corrected, unfamiliar schemas rejected. New databases publish only after completion. Source flat cache is read-only.
- Shared client-launch mutex, active-client checks, resumable import and a fast completion marker. Settings and player save hashes were unchanged after applying it to the real profile.
- Shared JS5/HTTP compressed-payload helper removes optional version trailers by validated container lengths without decompressing the transmitted assets. Reference-table paths stay unchanged.

## Evidence

- Empty fixture: 28,579 groups, 45 databases, 41,168,896 database bytes; preparation 26.590 seconds. Some index timings varied considerably with filesystem caching, so this is not a guaranteed time on another machine.
- Separate native client profile containing only that fixture reached the visible login/upgrade screen by 15:11:08.589 UTC after launch at 15:10:57.349 UTC: within 11.24 seconds. Authentication was not automated. Existing client-state was untouched by this test.
- Real profile: reused 28,567 groups, added 12 missing groups; 1.190-second import. Next preparation call skipped using the marker.
- Native verification: all 45 SQLite integrity checks and reference payloads; 75 group samples matched the actual native client's decoded payload and metadata.
- Resume preserved every database byte; incorrect-mode repair preserved unrelated rows; corrupt source rolled back existing databases and never published partial new databases; unexpected schema failed unchanged.
- 5,145 compressed-payload checks passed: types 0–3, opaque/real compressed data, exact trailer bounds, buffer positions/marks/order, invalid lengths, reference tables, continuation blocks, XOR and priority budgets.
- Existing protocol vectors passed against the newly built frontend. Full build compiled 337 Kotlin and 2 Java sources using bundled tools.
- The live-client guard correctly refused preparation while the normal client was open. All PowerShell launchers parsed under Windows PowerShell.

The full legacy gameplay test suite was not rerun for this historical performance measurement because no gameplay engine source changed. This result covers the standalone cache path only.

## Scope

The startup assets are prepared, not the full 23 GB world cache. New areas still request additional assets locally. A microbenchmark showed much less CPU work in compressed-payload formatting; it does not establish a comparable end-to-end speed multiplier. A Windows file-read microbenchmark did not establish a reliable large advantage from changing the server's file reader, so that reader was left unchanged.

The portable optional Python test also passed against the installed production jar: a fresh fixture prepared in 6.616 seconds on a subsequent run with filesystem caches warmed; exact-preservation resume took 1.041 seconds. Run `python tools/test-client-cache-preparation.py --skip-native-comparison` to repeat it using only Python's standard library and the bundled Java tools.

