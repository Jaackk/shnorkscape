# Ataraxia 947 migration build

This workspace copy retains the Java 8 gameplay engine. The original Ataraxia project is not required at build time and is not modified.

## Build and test

From this directory in PowerShell:

```powershell
.\Build.ps1
```

The script selects an installed Eclipse Adoptium Java 8 JDK, uses the pinned Gradle 4.9 wrapper, and places downloaded build dependencies in `../.gradle-ataraxia`. To choose a different Java 8 installation, pass `-JavaHome 'C:\path\to\jdk8'`. Once the dependencies are cached, add `-Offline` to verify without network access.

The default tasks compile the Java source, execute regression tests, create the application distribution, and write the complete runtime classpath. They do not launch the game server.

Outputs:

- `build/reports/tests/test/index.html`: regression test results.
- `build/install/ataraxia-947/lib`: packaged application JAR and runtime dependencies.
- `build/runtime-classpath.txt`: absolute classpath for isolated diagnostic entry points.

## Read-only modern cache check

```powershell
.\Build.ps1 -Tasks @('cacheMigrationProbe', '-PflatCachePath=C:\Users\developer\Desktop\rs3cache\cache')
```

The migration probe opens the supplied cache for reading and validates reference tables and representative raw files. It reports sizes and hashes; this is not a claim that the legacy gameplay definition decoders accept every modern field. It does not call `com.rs.ServerLauncher`, open account saves, or start legacy integrations.

The generated application launch scripts still target the original `com.rs.ServerLauncher`. Do not use those scripts for the modern world. From the parent directory, `Build-Ataraxia947.ps1 -Offline` builds and tests both Ataraxia and its OpenNXT frontend. `Start-Ataraxia947.cmd` starts the integrated 947 world through that frontend.

`Test-Migration.ps1 -Offline` also runs `ModernRegionProbe` and `Native947WorldSmoke` against the actual read-only cache. These validate collision, an encrypted walk on a real Player, a scene crossing, single-character admission, and disconnect/reconnect. The world probe takes roughly half a minute because movement runs at the actual 600 ms cadence.

Stop the local server before rebuilding the integrated distribution. Java 8 compiles Ataraxia; Java 25 compiles OpenNXT and runs the combined process. OpenNXT supplies the shared Netty, Kotlin and common library versions; only the additional Ataraxia dependencies are copied into its distribution.

## Build decisions

- Keep the original Java 8 compiler and Gradle 4.9 while establishing gameplay compatibility, avoiding simultaneous framework and language migrations.
- Remove the absent `kotlin` subproject from settings.
- Resolve pinned dependencies through Maven Central and JitPack; remove the retired JCenter repository.
- Declare Lombok's annotation processor explicitly.
- Include migration regressions from `tests` through JUnit 4.13.2.
- Preserve ISO-8859-1 source encoding for existing legacy text.
