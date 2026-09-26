# Component licences and provenance

The folder name is a project label, not a licence grant over every included component.

- **OpenNXT source:** the original GNU GPL version 3 text is preserved in `OpenNXT/LICENSE`. Original authorship and copyright comments remain in the source. This bundle includes the current source and an offline build script for its deployed frontend.
- **Ataraxia source and content:** source and attribution are included, with local migration changes. No project-wide LICENSE/COPYING file or permission granting a new licence was found in the supplied source. Its redistribution terms must be established before publishing it as an open-source release. This package does not relicense that material.
- **RuneScape native clients and game cache:** third-party Jagex software/assets, separate from the server source's licence. They are not tracked in this source repository. OpenRS2's availability of a cache download does not make those game assets open source or grant redistribution rights over them.
- **Eclipse Temurin/OpenJDK:** local development bundles may use Java 8 and Java 25 distributions under their own licences and notices. Those runtime distributions are not tracked here.
- **Gradle and offline dependencies:** local wrapper distributions, caches, and binary dependency mirrors retain their own licences and metadata and are excluded from Git.
- **Kotlin/compiler and runtime libraries:** local compiler and runtime binaries remain subject to their original licences and notices and are excluded from Git. Source build definitions record the versions expected by the project.
- **Historical third-party data:** content provenance is retained in the source's porting notes, including references to 2009scape combat data. These references are attribution, not a blanket licence assertion.

## Repository distribution

This public repository contains source, tests, required source resources, and development tooling. Operational credentials, player saves, client state, caches, logs, implementation backups, native clients, and local build/runtime distributions are excluded. Optional legacy integrations are disabled by default and use empty configuration values. Local protocol keys retained in source are development material for the paired local client, not credentials for a public service. Login is for local development and does not verify passwords.

The matching cache is OpenRS2 2691 (950.1), identified in `CACHE.json`, and must be supplied separately.
