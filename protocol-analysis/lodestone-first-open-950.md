# Lodestone first-open lock audit — 2026-09-12

Read-only audit of the isolated 950 project. No production source, cache or client edits were made by this audit.

## Conclusion

The 29 destination unlock IDs and values are correct for this paired 950 cache. The existing server publishes them only in `Native950Lodestones.open()`, in the same outbound batch immediately before `IF_OPENSUB`. The native client maintains separate received and script-visible player-variable maps. Consequently that packet order alone does not ensure the interface onLoad sees the new unlock state. `CS6001` changes locked sprites conditionally and does not reset each ordinary unlocked sprite in the opposite branch, so stale first-open initialization is visible.

Recommended fix: verify bindings and publish the same complete presentation state during `Native950Lodestones.bootstrap()`, before enabling the Home Teleport button. This gives the client its unlock state during world initialization, well before an ordinary first click. Retaining the same-value resend on open is harmless and may protect against unrelated client changes; it is not itself the timing fix. Do not invoke CS6001 repeatedly to compensate: it owns keyboard context and other initialization, and it does not generally restore unlocked sprites.

## Paired-cache evidence

`tools/verify_950_lodestones.py` decodes CS14999 tooltip switch branches and CS13702 destination unlock checks, using the independently inferred 947-to-950 opcode map. The existing 29 runtime rows match these scripts. `collect()` completed successfully during this audit. The onLoad hook literal6001 appears in index3/group1092/component0 at raw byte offset50 (hook encoding `01 00 00001771`).

CS13702 reads the runtime destination unlock bits. Special thresholds are Bandit Camp varbit9482 >=15 and Lunar Isle10236 >=190; the other27 entries test their own bit against1. Actual index2/group69 definitions show all values fit. Most old mainland destinations share parent varp3: bits0..12,15..21,23..24. Menaphos36173, City of Um53270 and Wendlewick60739 occupy separate bits0,1,2 of parent varp7040. Fort Forinthry52518 occupies parent10762 bit6. Anachronia44270 occupies parent8595 bit22. No full-parent overwrite is needed: the existing varbit packets preserve siblings correctly.

CS6001 instructions0..7 demonstrate the pattern: push network ID3; gosub13702; compare with0; only the locked branch sets sprite23032 on1092:10. The same conditional pattern appears for other ordinary destinations, including Wendlewick network33 at instructions96..103. The script also owns member-context changes, seasonal visibility and keyboard context30. CS6002 calls tooltip script14999, which re-reads current vars later; this explains why subsequent interaction can be usable despite the initial lock presentation.

## Native 950 binary evidence

Inspected original `OpenNXT/data/clients/950/win64/original/rs2client.exe` with the read-only `tools/dis950.py` helper.

- VARBIT_LARGE opcode82 parser `0x140142100`: parses valueBE and idLE, resolves varbit, calls `0x1402EB320` at `0x14014218A` with owner+0x19FB8.
- Varbit setter `0x1402EB320`: merges into the server-received parent value; at `0x1402EB447` calls `0x1402EB100`.
- Received-var setter `0x1402EB100`: writes the parent variant into container+0x20 (`0x1402EB119..14A`), then queues changed ID in container+0x38170 with a timestamp and boolean marker (`0x1402EB182..1AE`).
- Container vtable at `0x140B75A90`: slot+8 = `0x1402EAF60`; slot+0x18 = `0x140150100`. The latter is generic varbit getter and calls slot+8 for its parent value.
- Getter `0x1402EAF60` reads a DIFFERENT map: container+0x1C0C8 (buckets +0x1C0D0, count +0x1C0D8). It returns the definition default if not yet in that visible map.
- Reconcile `0x1402EAA70` examines changed entries and their eligibility timestamp (`0x1402EAB62..79`). `0x1402EAD1A..D6C` copies parent variant from received map+0x20 to visible map+0x1C0C8.
- Reconcile is called from client processing at `0x1401488E6` and `0x1401489A4`; changed IDs are then inserted into the interface-var-transmit ring at `0x14014897A..996`. It is not part of the packet setter.

This establishes the deferred publication mechanism statically. A fresh-login visual check remains necessary to confirm the UI symptom disappears after the fix.

## Bootstrap verification and server state

`Native950Session.ready()` invokes interactions.bootstrap after the player is active and after the frontend initial scene. Cache loading already precedes this. `com.rs.tools.modern.Native950CachePreflight.main()` explicitly calls `Native950Lodestones.verify()` at line25, but preflight can be a separate JVM. Calling verifyBeforeOpen (or a renamed verifyCache helper) at the beginning of lodestone.bootstrap is safe and preserves fail-before-write behavior; its static verifiedStore cache makes repeated validation cheap within the same runtime. Existing injected verifier unit fixtures need their expected count updated from0 at bootstrap to1.

Keep the current direct `Native950Packets.varbitLarge` policy for this targeted fix. It is explicitly presentation-only local exploration and includes quest prerequisite variables; copying those values into Player.VarsManager would change server quest state. Native950Save has no general varp/lodestone section, so neither direct packets nor VarsManager alone persist these values. Replaying the unlock policy at each login is sufficient for current behavior.

There is a second practical blocker to casually replacing the sender with VarsManager: Native950World.installVarpSink routes it through the packet facade's binding resolver. `Native950Bindings.Resolver.varp()` permits only declared bindings, and the lodestone parent varps are not in resources/native950/ui-bindings-950.json. Such a replacement would require explicitly expanding verified bindings rather than simply switching APIs. Do not widen this UI timing fix into a persistence/binding migration.

Original910 Player.refreshLodestoneNetwork was invoked as part of player initialization; openLodestoneNetwork only mounted1092. Publishing unlocks on login restores that lifecycle principle while using the actual950 IDs.
