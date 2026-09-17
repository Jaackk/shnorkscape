# M7c regional music catalogue evidence

Inspected 2026-09-08. This note covers regional selection and cache resources;
packet decoding, session ownership and native listening acceptance have separate
checks. No legacy music interface or playlist is enabled by the catalogue.

## Selection policy and coverage

`Native947RegionMusicCatalog` uses the **first** name returned by the existing
`Region.getMusicNames(regionId)`. That method puts explicit regional additions
before `getMusicName1`, `getMusicName2` and `getMusicName3`. For example, region
9012 selects Elven Daffodil, the first of its seventeen explicit choices.

This is intentionally first-name selection, rather than the initially considered
first-resolvable-name search. An unavailable primary selection returns null; it
does not silently pick a secondary track, a random unlocked track or an audio
resource from another index. The regional controller decides when null clears
playback. The selection has no dependence on the legacy all-unlocked shuffle.

A read-only Python census parsed the actual regional switch cases and resolved
their primary names using the paired 947 enums and index 40 reference table:

| Coverage | Count |
|---|---:|
| Regions with an explicit regional name entry | 315 |
| Regions with a nonempty primary name | 309 |
| Regions resolved to available music | 307 |
| Distinct selected tracks / resources | 241 / 241 |

The eight unresolved entries are region 9781 (`Gnome Village`), region 11575
(`Spiritual`), and six intentionally empty entries: 13360, 13407, 18516, 18517,
18773 and 18775. `Gnome Village` cannot distinguish the existing I and II tracks;
`Spiritual` is absent from both inspected catalogues. Regions without any entry,
including 13106 immediately east of Lumbridge, also return null.

Only three explicit spelling aliases and surrounding whitespace trimming apply:

| Existing regional name | 947 catalogue name | Affected region(s) |
|---|---|---|
| Barbarianims | Barbarianism | 12341 |
| Sea Shanty2 | Sea Shanty II | 12082 |
| Wandar | Wander | 11570 |
| Wilderness I followed by a space | Wilderness I | 12445, 12446, 13114 |

These mismatches already existed in the 910 source/cache pairing. No fuzzy name
matching is used. Duplicate `Distant Land` IDs 353 and 577 both resolve to resource
15482; the catalogue chooses 353 deterministically. Duplicate `Far Away` IDs 292
and 582 resolve to different resources (5305 and 30432). The explicit policy
chooses 292, retaining the earlier catalogue's first reverse-lookup candidate
without depending on unspecified HashMap iteration order. Any other ambiguous
name with different resources is rejected.

## Verified identity and pins

The logical music track ID and audio archive ID are different domains:
enum 1345 maps track ID to name; enum 1351 maps track ID to music resource.
Both logical files are pinned before decoding:

| Logical file | Length | SHA-256 |
|---|---:|---|
| 17/5/65, enum 1345 | 24209 | `7c2ef4dc5628915d72e4a1801fca8c5f7c1a761d9dd347e1a2f57fff28829da8` |
| 17/5/71, enum 1351 | 9699 | `c9b9263c23f204e5f0d26eef47613d3eab685d4da14a995c80f2695c01284fed` |

The raw logical bytes are independently extracted in
`tests/fixtures/native947-region-music.json`. The decoder admits only the pinned
enum formats: extended integer key type, string/music-resource value types and
sparse unsigned-short keys. The names file also has tag 3 at offset 24205,
containing the one-space string default; the resource file has tag 4 at offset
9693, containing signed integer default 2167. These defaults are consumed as
metadata, never inserted as regional selections or used for missing keys.
Missing files or changed bytes fail verification.

Direct read-only comparison with `Ataraxia-PS/data/cache` found 1503 names and
1501 resource mappings in 910, versus 1616 names and 1614 resources in 947.
**All 1501 old logical track-to-resource mappings are unchanged.** Fourteen labels
changed (Halloween spelling, twelve Citadel trailing spaces, Anachronia spelling);
none explains the regional mismatches above. The runtime still resolves against
947 instead of assuming that old resource IDs are permanently interchangeable.

| Region | Track | Logical ID | Index 40 resource |
|---:|---|---:|---:|
| 12850 | Harmony | 58 | 36067 |
| 12851 | Autumn Voyage | 17 | 37990 |
| 12849 | Yesteryear | 161 | 37355 |
| 12593 | Book of Spells | 23 | 38764 |
| 9008 and 9007 | Lost Soul | 204 | 6147 |
| 9012 | Elven Daffodil | 1234 | 7012 |

## Resource format and availability

The paired cache stores this music in **index 40**. Index 14 contains different
sound resources even when the numeric archive ID matches; indices 6 and 11 have
no reference tables in this cache. The catalogue never substitutes those indices.

All 85522 referenced index 40 groups have files. A broader audit of every exact
regional name, including secondary choices, found 260 unique resources. Their
decoded roots had valid `JAGA` headers, stereo audio, 22050 or 44100 Hz rates,
segment tables and inline Ogg data. All 11576 distinct continuation references
were present in the index 40 table and on disk. This census did not decode every
continuation's contents.

The observed root layout is `JAGA`, four big-endian integers, a segment count at
offset 20, then `(byteLength, groupId)` pairs from offset 24. Group zero identifies
inline Ogg data after the table; later groups are index 40 continuations. This
describes the inspected bytes, not an asserted native decoder implementation.

Harmony's root `40/36067/0` is 87319 decoded bytes, SHA-256
`c855f6e27d71c5a813a69a85f8bba3a5c5cc0bacf2afb44bdef20040effc170e`.
Its 49-segment table ends at offset 416. The first inline segment is 86903 bytes.
Continuations 74553, 74554 and 74555 were decoded independently; their lengths
(86660, 86000 and 100578) match the table and each starts with `OggS`.

Production availability checking requires the index 40 archive reference and a
nonempty compressed root file. It reads that root only once per distinct resource
and does not retain audio in the index's decoded-file cache. Continuation delivery
and CRC checks belong to the installed resource smoke, not this inexpensive
selection predicate.

## Lifetime and verification

Construction performs no cache I/O. A single immutable holder publishes the Store
identity and decoded catalogue together. Region hits and misses, plus resource
availability results, are memoized across players. Thus movement does not decode
enums or rescan region names repeatedly. Cache-free session fixtures return null
from lookup; explicit `verify()` requires the paired read-only cache.

Eleven independent catalogue tests cover raw pin drift/missing files, neighboring
region identities, all declared-region coverage, explicit aliases, duplicate
policy, unknown/empty regions, deterministic multi-choice selection, unavailable
primary music despite seventeen alternatives, shared resource probes and
cache-free construction. The corrected integrated build passes all 646 tests;
installed-cache and native listening status are in `M7c-region-music-acceptance.md`.
