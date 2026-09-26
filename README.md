# Shnorkscape

Shnorkscape is a revision 950 RuneScape 3 private-server development project
built from the Ataraxia and OpenNXT lineage. It combines a Java gameplay engine
with the Kotlin/Java OpenNXT frontend, exact-cache protocol bindings, automated
tests, and native-client development tooling.

The project has moved substantially beyond its original base. Implemented areas
include modern combat foundations, Necromancy resources and conjures, native
buff and action-bar integration, banking and equipment tools, broad skilling and
world content, and a native Developer Console. Coverage is still incomplete:
some bosses, quests, encounters, presentation bindings, and retail edge cases
remain under development.

## Requirements

The proven development profile currently targets Windows x64 and the workspace
path `C:\Games\950OpenSource`. It uses OpenRS2 cache **2691** (English revision
950.1). Download its
[Flat file archive](https://archive.openrs2.org/caches/runescape/2691/flat-file.tar.gz)
and extract it into the repository so `cache\255\12.dat` exists.

Runtime JDKs, the Kotlin compiler, native clients, client patches, and dependency
JARs are deliberately excluded from Git because they are generated, local, or
third-party material. The normal local development bundle supplies them under
`runtime`, `compiler`, `client`, `patches`, `dependencies`, and
`OpenNXT/runtime`. A source clone needs equivalent local prerequisites before it
can use the bundled Windows scripts. See [licensing and provenance](LICENSING.md)
before distributing a runnable bundle.

## Build

Stop the local server and clients, then run:

```powershell
.\Build-All.ps1
```

`Build-All.cmd` is the double-click entry point. The build runs the Ataraxia
tests, builds the OpenNXT frontend and revision-950 overrides, and verifies the
protocol and JS5 payload. Individual build scripts remain available for focused
work.

## Run

With the local prerequisites and cache in place, use `Play.cmd`. It starts the
complete local profile and the validated Vulkan client. `Play-OpenGL.cmd` is the
explicit fallback renderer. `Stop.cmd` stops only processes recorded for this
workspace, and `Check-Setup.cmd` validates the local bundle.

`Update and Play.cmd` is the developer workflow for an already prepared staged
candidate: it checks, stops, applies with rollback protection, waits for server
readiness, and launches the client. It is not required for a normal source build.

The login service is for local development. Use a disposable password; do not
enter credentials used anywhere else. Character saves, cache data, logs, client
state, and local configuration are ignored by Git.

## Repository layout

- `Ataraxia950/` — gameplay engine, content, resources, and tests.
- `OpenNXT/` — frontend, protocol, cache-serving, and launcher source.
- `protocol-analysis/` — revision-specific inputs and durable technical evidence.
- `tools/` — reproducible cache, protocol, staging, and diagnostic tools.
- `docs/` — maintained architecture, workflow, and subsystem documentation.

Useful starting points include the
[engineering workflow](docs/AI-ENGINEERING-WORKFLOW.md),
[native UI capability reference](docs/DEV-UI-NATIVE-CAPABILITIES-950.md),
[client initialization guide](docs/README-client-initialization.md),
[custom boss template](docs/CUSTOM-BOSS-TEMPLATE.md), and
[Artaven comparison](docs/SHNORKSCAPE-ARTAVEN-COMPARISON-20260926.md).

## Project and licensing status

This repository is an active research and development snapshot rather than a
claim of complete RuneScape parity. Revision-specific IDs and contracts are
pinned to the paired cache where evidence exists; incomplete systems should be
treated as such. Component ownership and redistribution constraints are detailed
in [LICENSING.md](LICENSING.md). The RuneScape client and cache are third-party
assets and are not licensed by this repository.
