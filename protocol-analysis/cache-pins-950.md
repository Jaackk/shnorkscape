# Cache binding pins, re-pinned to the 950 cache — 2026-09-10

## What happened

Every native adapter pins the cache files behind its bindings by SHA-256, so that swapping the
cache under a running server cannot silently retarget an interface component or a clientscript.
All of those pins were taken against the **947** cache. The 950 cache recompiled every clientscript
and renumbered interfaces, so **114 of them failed** — which meant the server could only start with
the whole fail-closed set disabled via `-Dataraxia.native.verifyCache=false`.

A disabled guard protects nothing, so the pins were re-taken against the selected 950 cache.
107 distinct hashes, replaced across 7 files:

| file | pins |
|---|---|
| `Native950CacheContent.kt` | 56 |
| `Native950Ribbon.kt` | 19 |
| `Native950QuantityInput.java` | 15 |
| `Native950Dialogues.java` | 12 |
| `Ataraxia950Handoff.kt` (chat scripts) | 8 |
| `Native950RunOrb.kt` | 4 |
| `Native950Settings.java` | 1 |

## What this buys, and what it does not

**It restores drift detection.** From this cache forward, a swap is caught again and the server
fails closed with the file named. That is the guarantee the pins exist for, and it was entirely
absent while they were switched off.

**It does not verify any binding.** A recompiled clientscript with a new hash may well behave
differently. Re-pinning records what is in the cache now; it does not establish that what is there
still *means* what the 947 binding meant. So the status of every pin below moved from

> verified on 947, failing on 950

to

> pinned to this 950 cache, meaning unverified on 950

which is an honest downgrade in the strength of the claim, not a fix. Anything a re-pinned binding
guards should be treated as unproven until it is exercised.

## Evidence status by subsystem

There is a real asymmetry here and it is worth keeping straight.

**Has behavioural evidence on 950** — the opennxt backend runs against this exact cache with the
ribbon, the Settings window, region re-centring and walking confirmed working against a live
client. Bindings on that path have been exercised, not merely re-hashed:

- `Native950Ribbon` (19)
- `Native950RunOrb` (4)
- `Native950CacheContent` interface bootstrap (part of the 56)

**Has no behavioural evidence on 950 yet** — re-hashed only, never exercised against a live 950
client. These are the ones to be sceptical of:

- `Native950Dialogues` (12)
- `Native950QuantityInput` (15)
- `Native950Settings` (1)
- `Native950CacheContent` item/NPC definitions and chat scripts (the rest of the 56, plus the 8 in
  the handoff)

## How to reproduce

The pins were not hand-transcribed. The server was run once with enforcement off, which makes each
mismatch log `UNVERIFIED <what> (expected <hash>, found <hash>)`; those lines were parsed and each
expected hash replaced with its found hash across the source tree. Rewriting by hash rather than by
parsing the declarations matters: the pins are written in at least four different shapes, and a
SHA-256 string is unique enough to replace globally without a parser that could quietly miss one.

Note the log stream — the Kotlin gate reports through `println`, so those lines land in
`logs/server.out.log`, **not** `server.err.log` where the slf4j output goes. Grepping the wrong one
reports zero stale pins and looks like success.

## Standing rule

`-UnverifiedCacheBindings` (which sets `-Dataraxia.native.verifyCache=false`) exists as a porting
aid for exactly the situation above: a known cache change that has invalidated the whole set at
once. It should not be a normal run flag. With the pins re-taken, the server starts with
enforcement ON, and that is how any run whose result is meant to be trusted should be made.
