# Offline Ability Pass Checks

Run from the repository root after `Build-Ataraxia950.ps1 -Tasks test,jar`.
These tools do not connect to the server or access player profiles.

```powershell
New-Item -ItemType Directory -Force logs/ability-probe | Out-Null
& runtime/java8/bin/javac.exe -cp 'Ataraxia950/build/classes/java/main;OpenNXT/runtime/lib/*' -d logs/ability-probe tools/ability-pass/Native950AbilityPassAcceptance.java
& runtime/java8/bin/java.exe -cp 'logs/ability-probe;Ataraxia950/build/libs/ataraxia-950-1.0-UNTRACKED.jar;OpenNXT/runtime/lib/*' com.rs.game.player.client.Native950AbilityPassAcceptance
& runtime/java8/bin/java.exe -cp 'Ataraxia950/build/libs/ataraxia-950-1.0-UNTRACKED.jar;OpenNXT/runtime/lib/*' com.rs.game.player.client.Native950AbilityCoverage cache logs/ability-pass-coverage.json
```

`InspectBooks.java` is a small read-only Java25 source-file inspection utility for
the exact category/filter varbit slices, script hashes and interface hook witnesses.
It is not a production parser and must not be used to infer unverified interface
fields. Script564/8426/8437 dispatch evidence, not visual similarity, establishes
the defensive book mapping documented in `docs/ABILITY-ROUGH-PASS-20260922.md`.
