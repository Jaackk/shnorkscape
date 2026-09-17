# Native 950 startup cache preparation

Entry point: `com.opennxt.tools.bundle.PrepareClientCache <sourceFlatCache> <destinationRuneScapeDirectory> [startup]`.

The helper runs offline with the bundled Java runtime and existing OpenNXT dependencies. Its caller must hold the bundle client-launch mutex and verify that neither native renderer is running. The helper writes only inside the supplied destination; the source flat cache is read-only. Startup is the only supported mode.

It writes reference rows for every available index (45 in the pinned cache) and groups for the 28 indexes observed completely loaded at startup: 2, 3, 10, 12, 13, 16, 17, 18, 19, 20, 21, 22, 23, 24, 26, 27, 28, 29, 42, 49, 57, 58, 59, 60, 61, 62, 65, 66. Index 14 is intentionally excluded: it contains 1.66 GB of compressed audio. World assets continue to arrive through JS5 as needed.

Native tables are `cache` and `cache_index`, each with `KEY INTEGER PRIMARY KEY, DATA BLOB, VERSION INTEGER, CRC INTEGER`. References use KEY=1. Both reference and selected startup rows use `ZLB` plus byte 1, a big-endian 32-bit decoded length, and a standard zlib stream. The CRC column is the original JS5 container CRC plus 1 with signed 32-bit wraparound; VERSION is the complete reference-table version, never the two-byte trailer. Reference payloads are the decompressed reference table. Single-file group payloads are the original decompressed contents. Multi-file group payloads are byte 1, a big-endian offset array with fileCount+1 entries, then the assembled files in reference-table order. The first offset is 5+4*fileCount and the last is the total decoded length.

The native client also supports a distinct raw-container CRC+2 representation for certain indexes, including observed sprites/models. That representation is not interchangeable with startup mode 1; this helper does not seed those groups.

The helper validates each imported source group CRC and source container lengths, rejects unexpected destination schemas, uses prepared inserts in batches of 512 within a transaction per index, preserves matching version/CRC+1 rows and unrelated rows, and repairs stale or incorrect-mode rows. Each newly created database remains a temporary file until it is complete and closed, then is published by a same-directory rename without replacement. Existing databases are filled transactionally. A failed index can be safely retried. No destination table or row is dropped.

Each index prints inserted/reused counts, source bytes, and elapsed milliseconds. Exit zero is the completion contract. A thrown error produces a nonzero exit; earlier completed indexes remain usable, the failed existing index rolls back, and a failed new index is not published. The launcher owns any completion marker and should write it only after success.

The September 2026 pinned-cache fixture wrote 28,579 groups and all 45 references from 25,936,764 group source bytes into 41,168,896 database bytes. Initial generation took 26.590 seconds on the test machine with variable source file read latency; a repeated run reused all rows in 0.947 seconds and preserved every database byte. The faster raw-format prototype is not used because raw mode is incompatible with these startup requests.

Validation passed all database quick checks, all 45 decoded reference comparisons, 75 sampled existing native group payload/version/CRC comparisons, exact-byte resume preservation, incorrect-mode repair, preservation of unrelated rows, corruption rollback for an existing index, nonpublication of an incomplete new index, and refusal to alter an unexpected schema. All live native cache reads used SQLite URI mode=ro. Test output and failures were confined to temporary folders.
