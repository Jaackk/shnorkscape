# Sharing privacy check — 12 September 2026

Personal Windows profile/machine references were removed from project text, metadata and optional research paths. Legacy public-address defaults and embedded mail contact values were removed; optional mail settings are empty and the diagnostic TLS proxy requires an explicit destination. Author/licence attribution remains intact.

Generated clients, client state, player saves, machine-specific build indexes, logs, temporary outputs, local AI settings and the old nested archive were moved into a private backup beside this project. Do not include that backup in a shared archive. The client is recreated from clean, hash-verified seeds on first launch.

The engine, frontend and overrides were rebuilt from sanitized source. Protocol checks, paired-cache preflight, 5,145 cache-transfer checks and offline dependency resolution with fresh Gradle caches passed. The rebuilt runtime archives were scanned across 43,310 entries with no matches for the checked personal identifiers or removed address/contact constants. Project text and the retained seed/compiler/override files were also checked. Original third-party game cache data and vendor distributions were preserved.

Localhost/loopback addresses and documentation-only test addresses remain because they are functional configuration or test fixtures, not the developer's network address. Bundled local-development RSA/TLS keys remain necessary for this self-hosted client and are not personal account credentials.

Offline development dependencies now live in dependencies/maven (67 POMs and 41 JARs). Gradle-generated indexes are deliberately excluded; builds regenerate them locally. Preserve the first local repository in Ataraxia950/build.gradle when updating this portable package.

Zip the folder before launching or rebuilding it again. Those operations necessarily create new local paths, logs and state. The earlier 950OpenSource.7z was moved outside this folder and is not the cleaned release.
