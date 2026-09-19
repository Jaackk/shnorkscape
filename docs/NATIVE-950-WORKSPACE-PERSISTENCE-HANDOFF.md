# Native 950 Workspace Persistence Handoff

Status: protocol and ownership model proven; production implementation intentionally not started.

This note records the evidence needed to implement native revision-950 workspace persistence without another layout guess. It supersedes the earlier packet-family hypotheses and the blanket ServerpermVarc omission experiment.

## Proven native owner

The revision-950 client already owns the workspace model. Cache scripts `8707`, `8708` and `8709` operate on native panel state rather than server-authored coordinates:

- `8707` reads live panel geometry/state, including position, size and relationship/state fields, then invokes `8709`.
- `8708` participates in preset/tab restoration and invokes `8701` and `8709`.
- `8709` is the central workspace serializer. It writes a large preset/slot matrix through client var and varbit operations.

The geometry fields observed in the scripts are clamped to 12-bit ranges (`0..4095`). Other fields cover panel relationships, tabs and visibility/state. The varbits are domain 2 and are backed by permanent parent client variables. For example, parent varc `3296` contains varbits `19037` through `19040` across bits `0..31`.

Therefore the workspace source of truth is the client's permanent-variable domain. `Settings.jcache` is not established as the workspace store, and SHNORKSCAPE must not add a parallel `x/y/width/height` layout model.

## Client upload: opcode 14

The original revision-950 WIN64 client sends changed permanent variables with client opcode `14`, descriptor size `-2` (unsigned-short frame length).

Evidence:

- Original client SHA-256: `fc7492548627a4068e88a7a45ba4f0be93004adf246bb4b174b24ae588768b36`.
- Sender wrapper: `0x140154780`; sender body begins at `0x1401547b3`.
- Descriptor reference at `0x140154b5c` resolves to client opcode `14`.
- Live SHNORKSCAPE logs recorded `Unhandled native input opcode=14 bytes=1291`.
- `1291 == 1 + (215 * 6)`, exactly matching the 215 ServerpermVarcs sent during healthy login.

The integer payload format is:

```text
u8 completion              0 = more chunks, 1 = final chunk
repeat until frame end:
    u16 variable_id        big-endian
    i32 value              big-endian
```

The current 215-value login set consists of integer values, so its complete client upload is one completion byte plus 215 six-byte records. The implementation must still validate variable definitions against the revision-950 cache before accepting records; payload shape alone is not authority for an arbitrary ID.

## Native batching lifecycle

The 950 sender matches the mature 876 permanent-variable domain lifecycle:

1. Permanent client-variable changes add their IDs to a changed-ID set.
2. When no batch is in flight, the client snapshots the changed IDs and clears the changed set.
3. The sender does not begin a frame when the network queue exceeds 1200 bytes (`0x4b0`).
4. It limits a packet to approximately 1500 bytes (`0x5dc`).
5. The completion byte is `0` when more records remain and `1` on the final chunk.
6. Sends are throttled by 1000 ms (`0x3e8`).
7. The completed in-flight batch is retained until the server acknowledgement arrives.
8. Only after acknowledgement does the client clear that batch, allowing changes accumulated meanwhile to become the next upload.

This explains the current user-visible failure: the first 215-entry upload is framed correctly, but SHNORKSCAPE discards opcode `14` and never acknowledges it. The client's first batch remains in flight, later workspace mutations cannot flush, and the next login receives the same static defaults again.

## Proven acknowledgement: server opcode 136

Server opcode `136`, fixed payload size `0`, is the revision-950 permanent-variable acknowledgement.

Its handler at `0x140141630` operates on the exact pending-batch fields used by the opcode-14 sender:

- the sender's permanent-variable domain object is at controller offset `+0x7620`;
- the pending vector at domain `+0xA8` is controller `+0x76C8`;
- the completion index at domain `+0xC0` is controller `+0x76E0`;
- opcode `136` compares the completion index with the pending-vector size, clears a completed vector and resets the index.

This offset identity proves the relationship. Opcode `136` is not inferred from an older revision's number.

The server must send opcode `136` only after the final chunk has been validated and durably persisted. A failed write must not be acknowledged. Non-final chunks are accumulated but not acknowledged as a completed batch.

## Required persistence lifecycle

Implement one account-scoped store for the client's native permanent integer-variable map. This is not a custom layout schema; it is persistence for the client's own authoritative domain.

Required flow:

```text
healthy static 215 defaults
    + validated player-specific persisted values (persisted values win)
    -> login ServerpermVarc chunk
    -> client native workspace reconstruction

client opcode 14 chunk(s)
    -> authenticate session/account ownership
    -> validate completion byte and every cache-defined persistent integer variable
    -> merge records into the session's native permanent-variable map
    -> on final chunk, atomically persist the merged map
    -> only after successful persistence, send empty server opcode 136
    -> client releases its completed batch and can send later workspace mutations
```

Do not remove entries from the healthy 215-value bootstrap. On a first login or when no valid account store exists, send all current defaults exactly as today. On later logins, overlay validated player-specific values onto those defaults. Unknown/unpersisted defaults remain intact. This preserves required root/workspace initialization while allowing the client's own mutable state to win.

The persisted map should be separate from gameplay/container state unless a carefully versioned extension of the modern profile is chosen. A small dedicated, checksummed, atomic file beside the modern account profile has lower migration risk and still stores the native domain directly, not a second interpretation of layout geometry. The login handler and attached world session must use the same store implementation.

## Validation and security requirements

- Accept opcode `14` only on an authenticated, attached native-950 game session.
- Bind writes to the session's canonical account name; never accept a username/path from packet data.
- Require payload length of at least one byte and `(length - 1) % 6 == 0` for the proven integer format.
- Require completion to be exactly `0` or `1`.
- Resolve every ID against the selected revision-950 cache and accept only the permanent integer client-variable domain. Do not use the static 215 IDs as the sole allowlist: legitimate later workspace mutations may include other cache-defined permanent IDs.
- Reject out-of-range, non-persistent, string or otherwise incompatible definitions rather than reinterpret their bytes.
- Bound records per frame, accumulated chunks, total map entries and file size. The native sender's approximately 1500-byte chunk is useful evidence for conservative limits.
- Define deterministic duplicate handling within a batch (normally last record wins) and test it.
- Persist with checksum validation, same-directory temporary file, forced write and atomic replacement. Never replace a previously valid file with malformed input.
- Acknowledge only a successfully committed final batch. Persistence failure should leave the previous file valid and fail closed without opcode `136`.
- Do not log full maps or raw personal state by default. Log bounded counts, completion state and failures.
- Preserve unknown stored values that remain valid under the cache definition; do not reset them merely because SHNORKSCAPE does not interpret them.

## Expected implementation sites

The smallest coherent implementation is expected to touch these locations:

- `Ataraxia950/network/com/rs/network/protocol/modern950/Native950Actions.java`
  - define client opcode `14` from the proven sender;
  - decode the completion byte and repeated `u16/i32` records into one bounded semantic action;
  - include it in implemented-opcode and descriptor-size coverage.
- `Ataraxia950/network/com/rs/network/protocol/modern950/Native950Protocol.java`
  - add the proven empty server packet `136`.
- `Ataraxia950/network/com/rs/network/protocol/modern950/Native950Packets.java`
  - add a named zero-payload permanent-variable acknowledgement writer.
- `Ataraxia950/game/com/rs/game/player/client/Native950Session.java`
  - accumulate chunks on the world/session owner;
  - merge, atomically persist and acknowledge only a successful final chunk;
  - clear incomplete session accumulation on disconnect.
- `Ataraxia950/game/com/rs/game/player/client/Native950World.java`
  - pass the shared account permanent-variable store into the attached session if constructor wiring requires it.
- A new narrowly scoped `Native950ServerpermStore` (name illustrative) under the same client package
  - canonical account keying, bounded map, checksum, strict parser and atomic save;
  - no coordinates, docking assumptions or gameplay state.
- `OpenNXT/src/main/kotlin/com/opennxt/net/login/Ataraxia950Handoff.kt`
  - own/share the store used by both login and attached session;
  - expose a safe method that overlays persisted values onto the default login map.
- `OpenNXT/src/main/kotlin/com/opennxt/net/login/LoginServerHandler.kt`
  - retain `TODORefactorThisClass.populateServerpermVarcs(map)`;
  - overlay the authenticated account's validated persisted map before encoding the login chunk.
- `OpenNXT/src/main/kotlin/com/opennxt/model/lobby/TODORefactorThisClass.kt`
  - preserve the complete healthy 215 defaults; do not filter the 92 overlapping entries.

`Native950PacketDispatcher.sendStoreServerPermVarcs()` is currently a no-op. Do not route the native handshake through the legacy dispatcher merely because that method name exists; the native session/store path above owns it.

The existing OpenNXT compatibility logic that labels world opcode `0` as serverperm is not the revision-950 game upload and must not be reused for this repair. The original client proves opcode `14`.

## Required tests

1. Opcode `14` decodes `completion + u16/i32 records` exactly and agrees with `clientSize(14) == -2`.
2. The captured shape `1 + 215 * 6 == 1291` decodes to 215 values without truncation.
3. Completion values other than `0/1`, non-six-byte tails, oversized batches and invalid IDs fail closed.
4. Multi-chunk batches accumulate without acknowledgement; one successful final commit emits exactly one opcode `136`.
5. Save failure emits no acknowledgement and leaves the previous persisted file byte-valid.
6. Store round-trip, checksum rejection, truncated-file rejection, path/canonical-account isolation and atomic replacement.
7. Defaults-only login still sends all 215 healthy entries.
8. Persisted valid values overlay defaults, while unspecified defaults and unknown-but-valid persisted values are preserved.
9. Invalid persisted IDs/types are rejected or quarantined without breaking the healthy bootstrap.
10. Disconnect clears an unfinished in-memory batch and does not commit it.
11. Acknowledgement framing is opcode `136`, size `0`, and does not consume or alter action-bar packets.
12. Regression coverage proves the main action bar remains attached/visible, existing bindings are untouched, and the yellow manual-press/keybind path is unchanged.
13. A restart-level integration test writes a changed permanent value, reconnects, and verifies the login ServerpermVarc map returns it over the static default.
14. Final live test covers move, resize, dock, tab, active tab and open/closed state across logout/relogin and a full client restart.

Automated state and wire tests are structural verification. Only the final live Vulkan test proves visual native workspace restoration.

## Disproven approaches

- Client opcodes `33` and `125` are repeated/delta-like input streams. Controlled captures showed no stable interface IDs, geometry or tab structure. They are not workspace persistence.
- Client opcodes `54` and `65` occur with an ordinary empty-world click in the same fixed pattern as workspace operations. They are pointer/input transport, not persistence.
- Omitting all 92 ServerpermVarcs overlapping the legacy `ILayoutDefaults` map is unsafe. Commit `66f8024` produced a black world, missing native workspace/ribbon and an empty `MANAGEMENT WINDOWS` shell. The recovery restored the complete set. At least some of those values are required root/workspace bootstrap state.
- `Settings.jcache` existing on disk does not establish it as the workspace store; it did not change during the controlled workspace mutation test.
- Do not resume random opcode capture, blanket varc filtering, guessed scripts or hard-coded panel coordinates.

## Current safe baseline

The source baseline when this handoff was written is commit `ec328c45bc9d802e8d1dc1d4d56e395c7255f9f4`. It retains the healthy 215-entry login bootstrap, visible main action bar, working bindings/keybinds and native yellow press feedback. No production workspace-persistence change is included in this handoff.
