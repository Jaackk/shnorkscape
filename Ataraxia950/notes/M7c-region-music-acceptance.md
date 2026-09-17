# M7c regional music

## Implementation

- `Native947RegionMusicCatalog` reuses the old region names and resolves them
  against two pinned 947 cache enums. Lookup is shared and memoized, including
  unavailable resources. See `M7c-region-music-catalog.md` for the exact mapping
  census and spelling repairs.
- `Native947RegionMusic` belongs to one session. It starts after bootstrap,
  observes the final post-movement region, avoids restarting identical resources,
  and emits one stop when entering an unmapped region.
- Server packet 87 carries an index-40 resource and volume. Client packet 59
  reports natural completion or requests playback after Audio unmute. Matching
  requests are coalesced until after movement; negative or stale IDs are ignored.
- No server write changes the user's local audio settings. The 910 manager no
  longer schedules its random replay timer or updates unmounted music widgets
  for native characters. The native session cancels ownership at logout.
- Snapshot diagnostics include selected region, logical track, resource, name,
  and total playback/stop requests (including completion/unmute replays).

## Verification

`logs/m7c-region-music-engine-final.log` passes all 572 engine tests with zero
failures, errors or skips. `logs/m7c-region-music-engine-smoke.log` passes the
encrypted real-cache probe, including all 149 audio chunks for Harmony,
Yesteryear and Autumn Voyage, and byte-for-byte local HTTP delivery of their
headers and first continuations. Region changes, completion/unmute requests,
stale notifications during teleport, silence and reconnect all pass.

The final install passed **641 tests** (572 engine and 69 frontend), with zero
failures, errors or skipped tests, in `logs/m7c-region-music-final-ship.log`.
`logs/m7c-region-music-installed-smoke.log` repeats the complete encrypted music
and local HTTP checks successfully using only the installed distribution JARs.
That first installation was PID **25432**, created **2026-09-08T20:56:33.6086780Z**;
rollback snapshot `backups/install-2026-09-08-155453` contains 112 JARs.

The native client was reopened and the restored character entered region 12850
with Harmony (logical 58, resource 36067). The first native listening check
**failed**: the user heard environmental sound effects but no music while
walking through several mapped regions. Code and packet tests alone did not
prove playback. A direct Audio UI inspection confirmed Global mute and Music
mute were both off and the Music slider was near maximum.
Read-only live health in
`logs/ui-debug/result-9f9cd12e-d66e-454f-9eeb-895e814c0717.txt` reports an active
session after 96 ticks and 67 movement steps, three scene rebuilds, and zero tick
failures, strict facade hits, dropped writes or scheduler failures. It still
counts 231 unimplemented client frames from the broader unfinished port. The
user retained control while walking; this is not an automated listening result.

## Native listening failure: HTTP storage trailer

The native client repeatedly requests the correct index-40 root headers without
progressing to audio continuations. The local `/ms` endpoint returns the flat
cache container including its two-byte storage version, unlike the existing
TCP JS5 path, which removes that trailer. For Harmony, the native request's CRC
`0x79618671` matches the 87324-byte wire container; the 87326-byte stored file
including `000f` has CRC `0xe39918ad`. The original HTTP smoke incorrectly
asserted equality with the entire stored file and therefore passed the broken
response. The correction must assert the wire container and reference CRC.

## HTTP correction installed

`Js5MsEndpoint.archiveHttpBody` removes only a structurally valid optional
storage version trailer, without decompressing or modifying the cache file.
Already normalized archives retain all bytes, including version-looking payload
endings. Sliced buffers, all four supported compression headers, invalid lengths,
and actual HTTP Content-Length/CRC are covered by five new frontend tests.
Reference/master tables and upstream responses keep their existing handling.

The final corrected package passes **646 tests** (572 engine, 74 frontend), zero
failures/errors/skips, in `logs/m7c-music-http-ship.log` after the independent
frontend pass in `logs/m7c-music-http-tests.log`. The installed smoke in
`logs/m7c-music-http-installed-smoke.log` passes using actual native-shaped
queries and the corrected wire length/CRC, alongside all music lifecycle checks.

Current server: **PID 19788**, created **2026-09-08T21:07:53.9052500Z**.
Rollback: `backups/install-2026-09-08-160609` (112 JARs).
Native client reopened as PID 11660. Listening confirmation after this correction
passed: the user reported **"music is playing now"**, then walked between zones
and confirmed **"yes it seems zone appropriate"**. This confirms audible playback
and the areas checked by the user, not exhaustive coverage of every mapped region.
The pre-fix retry sample is `logs/m7c-music-http-before.txt`.

The native client selected Unknown Land (track 133, resource 9368) on login at
(3104,3250), region 12338. `logs/m7c-music-http-live-progress.txt` records one
request for its header and one each for continuations 56382..56396, replacing
the prior repeating-header pattern. These are native client requests, distinct
from the smoke's three test songs. Read-only live health in
`logs/ui-debug/result-961ef42a-56f3-4519-bdc3-0fbfa32453b2.txt` reports 101 ticks,
36 movement steps, one scene rebuild, and zero tick, scheduler, strict-facade or
dropped-write failures. The broader port still counted 149 unimplemented client
frames. Automated region-selection and replay regressions also pass.

The dedicated `Native947RegionMusicSmoke` uses disposable characters with no
profile writes. It checks real JAGA streams and Ogg continuations, encrypted
session playback, region transitions, stale notifications and reconnects.
Its optional `--http` mode compares local HTTP responses with cache containers.
The smoke waits for actual encrypted tick-end boundaries before asserting
delivery, because session state can advance before queued output is flushed.
When launched from the workspace root, set `-Dataraxia947.data` to the absolute
`Ataraxia947/data` directory, as the normal launcher does.

## Scope

Manual music selection, playlists, forced quest/cutscene music and jingles are
not part of this regional playback port. Eight declared regions have an empty
or unresolved primary name; all other undeclared regions also remain silent.
The complete paired cache is required: a partial copy missing streamed audio
continuations can still fail after its root resource was successfully selected.
