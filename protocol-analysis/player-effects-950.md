# Player graphics engine bridge, revision 950

2026-09-10. Scope: `Native950PlayerEffects`, its dedicated tests, generated identity resource,
and reproducible read-only metadata survey. Original AstraNXT and Ataraxia-PS remain unchanged.
This document establishes source adaptation and packet semantics, not a completed visual acceptance.

## Integration

Call `Native950PlayerEffects.append(player, builder)` from the shared player mask bridge.
Its `Result.hasMasks()` contributes to the enclosing bridge's `any`; `refusedCount()` contributes
to that bridge's refusal counter. It appends only spotanims and transparent colour clears,
so animation, facing, appearance and other unrelated builder blocks survive.

`isVerifiedGraphic(id)` exposes the same cache identity policy for NPC graphics.
`queueClear(player)` queues four native slot clears without altering animation or other state.
The pure `append(Graphics[4], Colour, Builder, DefinitionSource)` overload and entity overload
with an explicit definition source allow tests without opening a cache or changing global Cache.STORE.

## Cache identities: all legacy spotanim definitions compared

`tools/verify_950_player_effects.py` reads the original sector cache using **rb-only** handles.
It does not instantiate the legacy Java Store, which would open writable handles. The modern
cache uses read-only flat-file reads. The script validates sector index/group/chunk numbers,
container unpacked lengths and multifile archive boundaries. Index21 uses group `id >>> 8`,
file `id & 255`, matching `GraphicDefinitions.getGraphicDefinitions`.

| Comparison | Count |
|---|---:|
| Legacy910 definitions | 7,259 |
| Modern950 definitions | 9,264 |
| Exact nonempty definition matches admitted | 6,962 |
| Legacy IDs with changed definition bytes, refused | 297 |
| Missing old definitions | 0 |
| New950 IDs lacking legacy identity proof, refused | 2,005 |

The generated `Ataraxia950/resources/native950/player-effect-identities-950.properties` pins
SHA256 for each admitted definition. The running server also checks its actual raw definition
against that pin before publishing the ID: an absent, changed or undecodable definition is a
counted refusal, not an exception that tears down the player's frame. -1 is a slot clear and
needs no definition. Resource format/index/revision are validated on load.

The report `player-effects-950-identities.json` retains both reference-table hashes, changed/new
ID lists, complete representative definition bytes and dependency checks. This is stricter than
assuming unchanged numbering. It still establishes **configuration identity**, not unchanged
rendered geometry, animation frames, materials, textures or sounds.

Four useful live examples are grounded in existing content:

| Graphic | Old content | Model ID | Sequence ID | Sequence definition comparison |
|---|---|---:|---:|---|
| 94 | `Magic.useTeleScroll`, player teleport effect | 58308 | 14294 | Exact match, 485 bytes |
| 184 | `Aubury.teleportToEssenceMines`; player action in `HomeCutScene` | 33372 | 328 | Exact match, 137 bytes |
| 436 | `Entity`, Redemption prayer effect | 9249 | 2596 | Exact match, 340 bytes |
| 1576 | `EliteDungeon` player teleport departure | 32718 | 8940 | Exact match, 199 bytes |

All four spotanim definitions also match exactly. Their model47 assets exist in both caches
but differ in bytes, so a live visual check remains required. Old model7 counterparts are not
used to claim that the modern rendered assets match. ID93 is an instructive refusal: its950
config adds opcode10. ID12's recolour data changes. Neither is allowed simply because it exists.

Regenerate the metadata/resource from the project root (no server or client runs):

```powershell
& 'C:/Users/developer/Desktop/AstraNXT/.venv/Scripts/python.exe' -B tools/verify_950_player_effects.py `
  --legacy 'C:/Users/developer/Desktop/Ataraxia-PS/data/cache' `
  --modern cache `
  --resource Ataraxia950/resources/native950/player-effect-identities-950.properties `
  --report protocol-analysis/player-effects-950-identities.json
```

Measured survey execution was below one second on this machine, excluding tool startup.
It does not enable spawns, combat, skills or any other content.

## Four slots, height, delay, flags and clear semantics

The source contract is explicit in `Graphics.java` and `LocalPlayerUpdate.java`:

- `getNextGraphics1/2/3/4` are four independent engine slots. Null means no update.
- The legacy writer packed `speed & 65535` in low16 and `height << 16` in high16.
- The second old byte stores `rotation & 7`, customValue shifted by3 and forceRefresh at bit7.

The950 player list parser at `0x14012CA43..0x14012CB3B` uses a reset selector script for each
addition. The full writer derivation is in `player-masks-950-derived.md`. Each record carries:
slot as128-minus; ID asLE+128 short; LE packed height/flag/delay; plain rotation/flag; LE offsets.
Source slots1..4 map directly to native slots0..3; refused/null records do not compact later slots.
The adapter extracts named source fields, never forwards an old packed record/hash as950 bytes.

The950 parser at `0x14012CA7B..0x14012CA8C` extracts:

- height as arithmetic `(packed >> 14) & ~3`, so a high16 value100 becomes400 render units;
- a separate flag from bit15;
- delay from low15.

The bridge preserves the source high16 height field; multiplying it before encoding would
multiply twice. Height0..32767 and delay0..32767 are supported. Larger/negative values are
refused, avoiding sign overflow and avoiding silently interpreting old delay bit15 as a new
flag. Rotation retains the old explicit modulo8 rule. CustomValue/forceRefresh are refused:
the950 packet's flag positions are known, but equivalent old semantic meanings are unproven.
Offsets and all three new flags remain zero for admitted ordinary legacy graphics.

At `0x1401284D6..0x1401284FA` the sink addresses the actor's slot map at+0x1018. Null definition
ID is tested at `0x14012850E..0x140128514`; the later -1 branch `0x1401286CA..0x1401286F5`
calls `0x14031DE60` using that **slot**. Consequently clearing is an ADD record with ID -1.
The removal list instead matches definition IDs, so old slot numbers must never be sent there.
The adapter canonicalizes a clear's unused delay/height/rotation/offset fields.

`Entity.setNextGraphics` deduplicates equal Graphics and fills the first available slot; a full
queue overwrites the fourth slot. Therefore `queueClear` first cancels all pending graphics via
four null setter calls, then queues four -1 records with distinct unused heights0/1/2/3 to defeat
source deduplication. The adapter emits canonical zeros while retaining each slot number.
This handles a clear arriving in the same tick as newly queued effects, not just an empty queue.

## Colour: explicit remaining blocker

`Colour` stores four unconstrained bytes in a packed integer. The910 writer reads them back as
four bytes and transmits delay/end offsets. Actual source content uses `(70,110,50,130)` in
`EffectsManager`, outside the950 typed hue0..63/saturation0..7/lightness0..127 ranges.
The950 parser forms a palette index at `0x14012E1B8..0x14012E1D3` and stores colour/alpha at
actor+0x190..0x19c. The precise950 byte transforms are proven; an intended910-to950 colour
conversion is not. Dividing raw bytes, clamping, or truncating them into HSL would invent a tint.

The bridge therefore refuses every nonzero-alpha legacy colour record and counts it. Zero-alpha
records safely clear the visible tint without interpreting their unused hue/saturation/lightness:
the bridge emits canonical zero HSL/alpha with validated0..65535 timing fields. The actual legacy
clear `new Colour(0,5,0,0,0,0)` is supported. Nonzero tint support still requires identifying the910
native colour sink or equivalent complete semantics and verifying the intended colour visually.
No colour clamp or implicit RGB-to-HSL conversion was added.

## Verification boundary

`Native950PlayerEffectsTest` contains17 tests with independent full definition bytes and literal950
packet fixtures. It covers real Player slot sourcing/reset, sparse slots, four additions, multiple
clears, replacing an already-full queue, simultaneous animation, known changed IDs, running-cache
mismatch/missing/decode failures, unknown flags, range boundaries, legacy rotation wrap, transparent
colour clearing and counted nonzero-colour refusal. Expected frames are literals rather than
encoder-generated expectations.

The survey and representative dependency reads were executed. This subtask did not invoke Gradle,
compile, restart or deploy; the parent task owns the combined build and client acceptance. The four
live effects should be tried separately first, then together and cleared. Check both server log
streams for refused effects or frame exceptions. Packet fixtures alone do not prove rendering.
