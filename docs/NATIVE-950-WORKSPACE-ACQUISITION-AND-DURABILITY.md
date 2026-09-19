# Native workspace acquisition proof and durability design

## Status

2026-09-19: **native acquisition is live proven for the captured representation**.
V3 source/build checkpoint: `39f227d`. No production durability or restoration
has been implemented. Preserve native editor baseline `20258e4` and the static
diagnostic client. Do not retry opcode14/136 experiments, pointer-input packet
hunts, Settings.jcache speculation, or removal of the 92 bootstrap values.

This document supersedes earlier acquisition uncertainty, not the safeguards.
It does NOT claim complete B-to-C equality or a universal varc registry.

## Exact live evidence

Local-only files `logs/workspace-static-v3-41364-{A,B,C}.json`:

| File | SHA256 |
| --- | --- |
| A | `4b40193fc286cf1b3d866ed5c9e74fce5ed4fdab149b18ea092db4d6921987c5` |
| B | `b9dd666fe4cdd70800e25d3d0424bf3eedc61338e2a798ff70c6b6cf618b9787` |
| C | `9e4b3ee1b42ed1f081ae5a11de5d7ad4bcc1e41afd885fa615f0aa7bbc7572e6` |

All are snapshot-stable, same client/game thread/schema, nativeWrites=false.
Jack visually confirmed alter -> Save Custom1 -> alter without overwriting ->
Load Custom1 restores Backpack. No raw snapshot/player data is committed here.

The repository comparator confirms:
- A->B: 229 of 907 entries change.
- B->C: 897 entries identical, ten changed; no out-of-schema ID.
- Custom1 has 178/180 parents present after Save and after Load. Parent3296
  becomes present on Save and stays stable/present on Load.
- Slots7/12/13 remain entirely absent, not zero-filled.
- Active8 has 180/180 parents present.
- B Custom1 versus B active8: all 712 readable fields equal.
- C Custom1 versus C active8: all 712 readable fields equal.
- B Custom1 versus C active8: 707 equal, five different, eight indeterminate.
- The eight indeterminate fields are actor1039, Custom parents6056/6057, absent
  at both B and C. They are IN the schema; this is sparse absence, not omitted
  capture coverage. Never infer default zero or invoke a mutating default getter.
- Metadata8372=6,8373=6,8374=0,8269=0 unchanged B->C; other three tokens absent.

## Ten differences mapped

Each row is one identical change in Custom6 and active8. Arguments are the exact
8709 integer argument positions, not guessed human window names.

| Actor | Argument | Custom6 parent | Active8 parent | Bits | B -> C |
| --- | --- | --- | --- | --- | --- |
| 1015 | 3 | 3371 | 2988 | 12..23 | 435 -> 430 |
| 1017 | 2 | 3375 | 2992 | 0..11 | 2089 -> 2086 |
| 1038 | 3 | 5958 | 5948 | 12..23 | 3293 -> 3288 |
| 1045 | 3 | 6428 | 6418 | 12..23 | 3293 -> 3288 |
| 46 | 7 | 8205 | 8185 | 24..30 | 19 -> 0 |

8702 instructions159..179 explicitly transform the first two fields through
8711/8710 before writing the active matrix at191. Those helpers perform integer
coordinate conversion, rounding and clamping. Four differences occupy those
coordinate fields. This supports normalization as a candidate, but does not
prove the precise numerical cause without the intermediate viewport/state.

Argument7 is NOT one of those coordinate-transform arguments. Do not label its
19->0 change coordinate rounding. Also, a pure source->active copy is insufficient
to explain changes to the Custom6 source itself. A subsequent native commit or
another native update between snapshots could explain the paired source changes.
Jack corrected his recollection: subsequent editor testing was AFTER C. Treat C
as the intended post-Load snapshot. Nothing in the ordered timestamps or captured
state contradicts that account. The tentative repeated-save explanation is
withdrawn. Do not request a repeat capture on that basis.

Further exact script analysis:8741 dispatches the native Load through8884/8885.
8884 calls8885 then8781.8781 reads8701, handles visibility, clamps actor sizes,
converts positions with8711, rebuilds tab/link relationships through8350, and
schedules8782 follow-up callbacks for positioning/collision handling.8784 tracks
completion work. This is not a byte-preserving matrix copy.

The fifth difference (argument7) is the forward link field consumed by8728,
which8350 follows while rebuilding relationships; it is not a coordinate.
Four normalized-coordinate changes plus one link change are consistent with this
native application path. The exact instruction updating each Custom6 source
parent and the numeric rounding inputs were not captured, so do not claim that
the precise five transitions were individually replayed/proven. The mirrored
field changes do not show dropped IDs or reader coverage failure.

Conclusion: the reader and sampled native Save/Load relationship are proven,
with visual restoration and two internally matching saved/active matrices.
**Strict immutable B6=C6 is false**. The comparator retains that failure and
separately reports paired changes; it does not weaken equality into a pass.

## Additional bounded metadata requirement

V3 was a matrix acquisition diagnostic, not yet a complete durability schema.
The directly called helper8703 copies per-slot metadata via2257. Its switch maps:

| Slot | Varbit | Domain2 parent | Bits |
| --- | --- | --- | --- |
| 6 | 19035 | 3295 | 0..4 |
| 7 | 19627 | 3380 | 0..4 |
| 12 | 31442 | 5215 | 0..4 |
| 13 | 31920 | 5352 | 0..4 |
| 8 | 19331 | 2912 | 0..4 |

These five parents are absent from the907 snapshot scope.2912 was separately
read as a V3 control; the other four were not captured. Only2912 belongs to the
existing1694 descriptor. The other four need explicit8703/2:69 evidence in a
separate versioned schema, NOT a silent expansion of the old descriptor.
This is a concrete five-ID completion, not another packet/domain investigation.
Retain only the evidenced low-five-bit fields for restoration; preserve sibling
bits. The full native integer can be captured read-only to verify the mask.

Pinned cache container evidence used in this review:
- 8702: `b560f33028b23b3d13f878bec0af1251dd66835a6524a033534f5b7444e56622`
- 8703: `20fbc3c6f541d6dc77bbd2338f641415d98f50095c4b2200a3cbb61ef8cf4541`
- 8710: `2c34d9f5464a3e0ede4ea3d1135a1091106e9b020fceb40de13944cf9ae9c6e4`
- 8711: `042fe48f7db98dcc98cacfcad83bbc59b533fa81b607ec5221461c390ae1ea0a`

The exact2/69 container/reference-table pins and matrix mappings remain in
`tools/vulkan-static-probe/snapshot-schema.json` and its generator.

## Smallest production design

Reference-first comparison: inherited Ataraxia `Player.iLayoutVars` seeds its map
from `ILayoutDefaults`; a server map cannot acquire the unseen client state.
Undercut's `core/.../model/PlayerVars.kt` explicitly notes client scripts rewrite
varcs without telling the server, separates `saveVarc` from `setVarc`, and overlays
`storedServerpermVarcs` onto defaults in `serverpermBlock`. Reuse that ownership
separation, not its revision IDs/packet encodings. Exact950 V3 now provides the
previously missing acquisition; existing950 packet codecs are the preferred
restore candidate, subject to the live receive/application gate below.

Prefer a **read-only client bridge plus existing server-to-client native setters**,
not direct native memory writes. Native scripts keep all geometry/tab semantics.
This avoids implementing native hash-table allocation, setters, change listeners,
or arbitrary native-call interfaces. None of the following is deployed yet.

### Capture and account boundary

Keep the pinned static main-logic reader. Copy a bounded, typed native snapshot
into adapter-owned memory at the post-native-update boundary; perform transport
and file I/O off the game thread. Never queue a mutable pointer to native state.

Use one narrowly typed local bridge, authenticated and bound to the actual native
server session/account, for snapshot request/response and receipt only. A short-
lived single-session capability must be minted by the server after player binding;
the bridge pairing must verify the actual client session, not accept an account
name or arbitrary PID supplied in a snapshot. No account-global client file,
filename-based account identity, public memory API, or unauthenticated HTTP endpoint.
Exact pairing integration is a prerequisite to implementation, not proven by V3.

Save events enqueue capture requests without changing the existing editor recipe.
The server emitting8743 is NOT proof that the client already executed it. Require
a client execution/order fence or observed post-commit receipt before accepting a
snapshot; elapsed delay, window closure alone and two equal samples are insufficient.
An unchanged re-save must also receive a receipt. Cancel must never publish edits.
Observe Load/selection separately for active-layout persistence without replacing
Custom contents with staging9. These event fences need narrowly scoped integration
tests; the read mechanism itself does not need re-investigation.

### Stored representation

Separate versioned account-scoped workspace sidecar, independent of inventory,
skills, bindings and the existing character save. Store the exact presence-tagged
native integer parents for Custom1-4, active8 and proven metadata, plus schema/client
hash, monotonic save revision and checksum. No translated X/Y records or strings.
Suggested hard bounds: schema exact ID membership, <=912 records, <=64KiB encoded,
unique IDs, int32-only, exact presence tags; reject unknown IDs/types/duplicates.
Absent records stay absent and must not delete or zero default values on login.
Validate selected slot against6/7/12/13 and its metadata/present state; no fabricated
preset when data is incomplete. Preserve masked metadata siblings.

Write a new same-directory temporary file, flush, atomically replace, retain a
known-good previous generation; serialize/coalesce account saves by revision.
Receipt only after durable success. Failure retains the previous file and reports
not-saved; never claim success just because native Save & Exit closed. Do not
acknowledge via136. Reject stale/session-replayed responses and cross-account data.

### Restore mechanism and order to validate first

Use `Native950Packets.varc/varcLarge/varcBit*` on the authenticated game channel
instead of a native pointer setter. These are existing revision950 packet codecs,
but writing previously absent Custom parents through them is NOT yet live-proven.
Do not assume their older comments prove the runtime domain or notification path.

First a disposable-account, memory-only round trip: after healthy world attachment,
send ONE independently evidenced Custom parent using the existing setter, then
read it via the proven reader. Check exact ID/domain/value/type, another control,
and absence/instantiation behaviour; reset by ending that disposable session.
No Jaxa replay or broad matrix write until this narrow receive path passes.

Then validate a complete captured Custom1 with the five metadata parents included:
1. Preserve the entire healthy215 bootstrap, native-world ownership and action bar.
2. Wait for a proven post-bootstrap/native workspace readiness fence, not a timer
   or mode30 alone; rule out later default replay on the same session.
3. Send only present Custom1-4 matrix values through verified native setters.
4. Apply evidenced masked slot metadata and integer tokens after matrix contents.
5. Restore the selected/active relationship through the native load path.8702
   supports source Custom -> target8 with native conversion, but8700 ONLY sets a
   per-slot bit5 flag. It does not apply actor layout.8741/8884/8885/8781 form the
   observed Load/application path, including asynchronous callbacks; their guards
   and completion must be retained rather than replaced with a bare8702 call.
   Verify this invocation can run outside editor mode before adopting it. Do NOT
   use8743/8754 or mount the editor on login; they commit staging and tear down UI.
6. Apply the selected-layout metadata at its verified phase, take a post-application
   read receipt, and prove no later bootstrap overwrites it. Do not call the server's
   queued packet a client completion. Restore once per new bound session.

Never blindly replay an old active matrix AND load a preset afterwards; choose one
native application recipe based on the controlled restore proof. Saving active8
allows exact native state retention, but selected-Custom native application is
preferred for viewport adaptation. Dirty unsaved changes must not silently replace
the last committed preset. Explicit reset/deletion semantics require native
evidence before clearing absent slots in an already-populated same-client session.

The required final flow is: committed native snapshot -> account durable sidecar ->
healthy login unchanged -> verified native replay/application -> automatic selected
layout. Full client/server restarts use the same sidecar, not a process-local cache.

## Implementation checkpoints and tests

1. Complete the five-ID metadata schema and test its pinned8703 switch/2:69 masks;
   test the setter on a disposable session. Do not modify the production editor.
2. Validate native application/readback after healthy bootstrap with an in-memory
   fixture. Require world brightness, interaction, action bar and keybind live checks.
3. Add authenticated session-bound transport and post-commit fence. Test stale,
   duplicate, wrong-account, disconnected and reordered requests; cancellation,
   same-value save, multiple saves, logout during capture, and failed receipt.
4. Add bounded atomic sidecar with corruption/truncation/checksum/schema/type/ID
   rejection, account isolation, rollback generation and write-failure tests. Old
   player saves remain byte-compatible; do not add workspace fields to Jaxa's save.
5. Add login replay once ordering is proven. Tests retain all215 defaults for absent
   entries, protect non-workspace values and masked bits, retain Custom1-4, active
   selection, sparse absence, changed viewport sizes, and no default replay afterward.
6. Jack: Save Custom1 -> relog -> automatic layout + Custom1 available -> full
   client/server restart -> same. Test another Custom slot and cancel an edit too.

Acquisition is closed as a solved prerequisite. Remaining gates are safe native
application, bounded metadata completion, and durable/session ordering, not another
search for a native upload opcode. No production files, client binaries or saves
were changed by this review; only offline comparison/tests and this checkpoint.

## Schema-aware restore preparation checkpoint

`tools/vulkan-static-probe/restore_schema.py` now derives a separate912-ID schema
from the unchanged pinned V3 schema and8703/8700/2:69. It proves that per-slot
metadata is bits0..4 and the neighbouring flag is bit5 of the same parent. Masked
restore intentions exclude that flag and all unknown sibling bits. No V3 binary,
907-ID schema or1694-ID descriptor was changed.

The offline planner validates exact account/session/schema/ID/type/presence
membership, rejects incomplete907-only input, and emits NON-EXECUTABLE ordered
intentions: Custom matrices -> masked slot metadata -> integer metadata. Active8
is retained but not independently replayed, avoiding a competing layout owner.
Absent values are skipped, never synthesized. Native application and post-bootstrap
readback remain mandatory gates. The planner does not send packets, call native
code, implement storage, or prove authenticated session binding by comparing
caller-provided strings. Runtime binding must supply those independently.

Six new offline tests cover deterministic912 coverage, masked sibling protection,
absence, wrong account/session, duplicates/unknown IDs/types/ranges, schema/version
and selected-slot rejection. This is restore preparation, not a deployed restore.
