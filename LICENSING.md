# Component licences and provenance

The folder name is a project label, not a licence grant over every included component.

- **OpenNXT source:** the original GNU GPL version 3 text is preserved in `OpenNXT/LICENSE`. Original authorship and copyright comments remain in the source. This bundle includes the current source and an offline build script for its deployed frontend.
- **Ataraxia source and content:** source and attribution are included, with local migration changes. No project-wide LICENSE/COPYING file or permission granting a new licence was found in the supplied source. Its redistribution terms must be established before publishing it as an open-source release. This package does not relicense that material.
- **RuneScape native clients and game cache:** third-party Jagex software/assets, separate from the server source's licence. Native binaries are included from the existing local project; their source is not available here. The cache is excluded. OpenRS2's availability of a cache download does not make those game assets open source or grant redistribution rights over them.
- **Eclipse Temurin/OpenJDK:** Java 8 and Java 25 distributions are included unchanged with their original licence and third-party notices (`runtime/java8/LICENSE`, `runtime/java8/THIRD_PARTY_README`, `runtime/java25/legal`). Source archives included by those distributions remain present.
- **Gradle 4.9:** its licence and notices remain under `.gradle-ataraxia/wrapper/dists/.../gradle-4.9`. Pinned offline dependencies retain their original metadata and embedded notices.
- **Kotlin/compiler and runtime libraries:** original JARs are preserved with embedded META-INF licence/notice files where supplied. Dependency versions are visible in filenames and the engine build definition.
- **Historical third-party data:** content provenance is retained in the source's porting notes, including references to 2009scape combat data. These references are attribution, not a blanket licence assertion.

## Portable snapshot changes

Prepared from the working 950RevTest snapshot on September 12, 2026. The original project was left unchanged. Portable launch/build tools resolve paths from their own folder, and the client initializer regenerates isolated storage paths after a move. The engine and full frontend are rebuilt from the included source.

The portable copy removes embedded operational database/site/donation credentials, token-bearing mailing recipients, fixed privileged legacy account lists and legacy service addresses. Optional legacy integrations are disabled by default and use empty configuration values. Local protocol RSA/TLS keys remain to match the bundled local client; they are development keys, not credentials for a public service. Login is for local development and does not verify passwords.

The matching cache is OpenRS2 2691 (950.1), identified in CACHE.json. Current player saves, client state, caches, private project logs and implementation backups are excluded.
