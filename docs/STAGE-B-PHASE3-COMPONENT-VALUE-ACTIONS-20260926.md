# Stage B Phase 3 — client opcode 124, component value/selection (2026-09-26)

Status: **ADOPTED 2026-09-26** (decode-only). Base: Shnorkscape
`39240df6cf3bf04996bee17532f6aca9a3b16f3d`. Artaven source (READ-ONLY):
`C:\Games\UpdatedAuthorFiles\950OpenSource-2026-09-25\950OpenSource`, specifically
`Ataraxia950/network/com/rs/network/protocol/modern950/Native950ComponentValueActions.java`
(22 lines) and `Ataraxia950/tests/modern950/Native950ComponentValueActionsTest.java`.

**Summary (read this first):**
1. The opcode 124 wire contract is **independently verified** against the client binary
   (disassembly in §1), not taken on Artaven's word.
2. Shnorkscape **now decodes it** (`Native950Actions.ComponentValueAction`, §6).
3. **No gameplay consumer was added.** Nothing reads a decoded `ComponentValueAction` anywhere;
   it is only counted by name instead of falling into the generic "unhandled opcode" bucket (§6).
4. **Equipment Binding was investigated and this packet is not its missing mechanism.** Interface
   365 components 19/20 (the row-select/value components that protocol actually uses) do not set
   the `IF_SETEVENTS` bit this opcode is gated on (§5, with a pinned negative-control test).
5. **The actual Equipment Binding application-side bug remains open and untouched.** It lives in
   `Native950CombatPreferences.equipmentChanged()`/`boundBar()`, not in any packet decode (§5).
6. **Live semantics of the 10 real bit-24 components remain unproven without a packet capture.**
   Cache evidence identifies them and narrows the two most plausible candidates; it cannot show
   what a live client actually sends (§9).

## 1. Exact opcode 124 contract

Independently disassembled (not copied from Artaven), client
`OpenNXT/data/clients/950/win64/original/rs2client.exe`
(SHA-256 `fc7492548627a4068e88a7a45ba4f0be93004adf246bb4b174b24ae588768b36`).

**Descriptor confirms the opcode.** `0x140e942e0 + 16*124 = 0x140e94aa0`. A data-reference sweep
(`tools/dis950.py drefs 0x140e94aa0`) finds exactly two references, both inside one function at
950 `0x1401a9e80` (`lea r8,[descriptor+4]; lea rax,[descriptor]; call 0x1400af710`, the same
allocator pattern documented in `Native950Actions.java`'s own header for every other opcode).

**Sender `0x1401a9e80`.** Signature `(this, componentCtx, hash /*r8d, saved to edi*/,
slot /*r9d, saved to ebx*/, value /*stack arg, [rsp+0x70]*/, selected /*stack arg, [rsp+0x78]*/)`.
After the allocator call it stamps 11 bytes onto the outbound frame, traced write-by-write:

| bytes | field | instructions |
|---|---|---|
| b0 | `value >>> 8` | `0x1401a9ee0 sar ecx,8` |
| b1 | `value & 0xff` | `0x1401a9f06 mov [cursor],r8b` |
| b2 | `value >>> 24` | `0x1401a9f11 sar ecx,0x18` |
| b3 | `value >>> 16` | `0x1401a9f14 sar r8d,0x10` |
| b4,b5 | `slot`, big-endian u16 | `0x1401a9f47..f65`: register byte-swapped in place, then one `mov word ptr` store |
| b6..b9 | `hash`, little-endian i32 | `0x1401a9f7e..fc1`: `edi`/`dil` shifted 0, 8, 16, 24 in that order |
| b10 | `0x80 - selected(0 or 1)` | `0x1401a9fc5 mov ecx,0x80; 0x1401a9fce sub cl,[flag]` |

11 bytes total, matching `Native950Protocol.CLIENT_SIZES[124] == 11` (a Shnorkscape-derived table,
already correct and unchanged by this pass — the sweep that produced it had already found this
opcode's true length independently of Artaven).

**Router `0x1401a91c0`.** The caller that decides whether to fire this sender at all. It calls
`0x1401a1070` — the same interface/component hash-table lookup that resolves the in-memory decoded
`IComponentDefinitions` for a `(interfaceId, componentId)` pair (bucket count and pointer table read
from fields at `[+0x118]`/`[+0x120]`, exactly the shape of a hashed definitions cache) — then tests
`byte[result+3] & 1`. Byte 3 of a 4-byte int is bits 24–31; masking with 1 isolates bit 24. This is
`IF_SETEVENTS` bit 24 of that component's option mask, the same 32-bit field
`IComponentDefinitions.activeProperties.settings` now decodes correctly since Stage B Phase 2 (before
that fix the field was read as 24 bits and bit 24 was structurally unreachable).

**Byte layout cross-check.** The above, decoded, reproduces Artaven's `decode()` exactly:
`value=(b[2]<<24)|(b[3]<<16)|(b[0]<<8)|b[1]`, `slot` big-endian at b4/b5, `hash=(b[9]<<24)|(b[8]<<16)|(b[7]<<8)|b[6]`,
`selected = (128-b[10])==1`. Agreement between an independent disassembly and Artaven's decoder is
exactly the corroboration the task asked for — it is not "trusting Artaven," it is two derivations
landing on the same bytes.

## 2. Artaven implementation

`Native950ComponentValueActions` (22 lines): `OPCODE=124`, a `Selection` record (`hash`, `slot`,
`value`, `selected`), `decode(byte[])` implementing the table above, `interfaceId()`/`componentId()`
split from `hash`. Its own comment: "Native list/dropdown changes... server notification gate is
IF_SETEVENTS bit24." Their test (`Native950ComponentValueActionsTest`) is four hand-built byte-string
fixtures (not a live capture) plus one call into their `Native950Actions.decode(124, ...)` dispatcher.

## 3. Shnorkscape before this pass

Opcode 124 did not appear anywhere in `Native950Actions.java` or `Native950Protocol.java`: not
decoded, not in `implementedOpcodes()`, not in any `UNDERIVED` row. A well-formed 11-byte frame with
this opcode landed in `Native950GameTransport.channelRead`'s final `else` branch — counted as
`unhandledFrames`, logged once via `observedUnhandledOpcodes`, and dropped without disconnecting.
This is the transport's documented fail-closed behaviour for anything not yet decoded (see its own
"Framing is verified, semantics are not" comment) — a safe state, not a bug, but it meant the frame's
contents were invisible to any Shnorkscape system.

## 4. Real 950 components using it

Ran the (now-correct, Stage B Phase 2) decoder over every one of the 104,285 index-3 components in
the local paired 950 cache and counted `activeProperties.settings & 0x01000000 != 0`. Exactly 10:

| component | type | additional/relevant hook |
|---|---|---|
| 590:7 | 16 (native "bar"/stepper) | `additionalHooks[2] = [3553, ...]` |
| 590:8 | 16 | `additionalHooks[2] = [13155, ...]` |
| 1438:18 | 16 | `additionalHooks[2] = [3553, ...]` |
| 1438:19 | 16 | `additionalHooks[2] = [13155, ...]` |
| 365:2 | 13 (native text input) | `additionalHooks[1] = [5714, -2147483647]` (no string tag) |
| 1498:6 | 13 | `additionalHooks[1] = [20646, -2147483647]` (no string tag) |
| 1401:42 | 13 | `additionalHooks[1] = [20229, "event_text", -2147483647]` |
| 1442:29 | 13 | `additionalHooks[1] = [15761, "event_text", -2147483647]` |
| 1479:13 | 13 | `additionalHooks[1] = [20284, "event_text", -2147483647]` |
| 1475:32 | 13 | no additional hooks at all |

Pinned in `Ataraxia950/tests/modern947/Native950ComponentValueContractCacheTest.java`.

Two clean pairs stand out: 590:7/8 and 1438:18/19 are literally the same two-widget "bar" layout at
two different interfaces, sharing the exact same pair of hook scripts (3553, 13155). A stepper's
two arrow buttons producing one shared resulting-value report is the most natural fit for opcode
124's (hash, slot, value, selected) shape of any of the ten. The type-13 group is more ambiguous:
three of six explicitly tag a string ("event_text") — a separate, string-carrying contract, almost
certainly not this opcode — and the remaining three (including one with no additional hooks at all)
are unresolved.

## 5. Equipment Binding relevance — NOT RELEVANT

`Native950Settings.java` already implements Equipment Binding's row-select/value protocol entirely
over ordinary `IF_BUTTON` clicks (ops opcode family, decoded as `InterfaceAction`) on interface 365:
component 19 selects a row (`preferences.select`, backing CS10451), component 20 commits its value
(`preferences.value`, backing CS10450). Checked both components directly against the real cache:
**neither sets bit 24** (`activeProperties.settings == 0` for both). Confirmed and pinned as a
negative control in the same cache test.

Artaven's own unit-test fixture happens to use a hash that decodes to interface 365 component 19 —
this is a coincidence of a hand-picked, readable test value (their test is named
`houseArrivalLiteral` and is a synthetic byte string, not a captured packet), not evidence that the
real component emits this opcode. The instruction was explicit not to conflate "this interface uses
bit 24" with "this fixes Equipment Binding," and the cache evidence bears that caution out: it
doesn't, because 365:19/20 don't use bit 24 at all.

The actual reported Equipment Binding bug ("weapon changes do not select the bar") is in
`Native950CombatPreferences.equipmentChanged()` /
`boundBar()` — an application-side call-timing or matching issue on an already-working transport,
unrelated to any missing packet decode.

## 6. What was adapted

Minimum decode-only support, in `Native950Actions.java`:
- `COMPONENT_VALUE_OPCODE = 124` with a field note recording the sender/router addresses and the
  bit-24/real-component evidence above (self-contained; does not require reading Artaven's file).
- `ComponentValueAction` (mirrors the existing `InterfaceAction` accessor style: `interfaceId()`,
  `componentId()`, `slot()`, `value()`, `selected()`), independently re-derived, not transcribed.
- `decodeComponentValue(byte[])`, wired into `decode()`, `isImplemented()`, and
  `implementedOpcodes()` (which fed `implementedOpcodes().length`; the existing
  `Native950ActionsTest` assertion for that count was updated from 59 to 60).

No change to `Native950GameTransport`, `Native950Interactions`, `Native950Settings`, or any other
consumer. The transport's existing generic instrumentation (`Native950BugTest.action(player, action)`,
called once per decoded action before any type-specific branch) now counts this action by its class
name instead of folding it into the generic "unhandled opcode" bucket — that is the entire behavioural
change. No gameplay feature was invented or wired to it, because no consumer of its value is proven
necessary yet (see §8).

## 7. Test results

- `tests/modern947/Native950ComponentValueActionTest.java` (new): opcode/size agreement, the
  house-arrival-shaped fixture, a signed-value/static-slot fixture, an endian/field-order fixture
  with distinct nibbles per field, and three malformed-frame rejections (short, invalid flag byte,
  null). All independently re-derived byte strings, cross-checked against Artaven's own fixtures
  (same inputs, same outputs, as expected for the same client).
- `tests/modern947/Native950ComponentValueContractCacheTest.java` (new): pins the exact 10-component
  set above (identities, types, hook scripts) and the 365:19/20 negative control, over the real
  paired 950 cache. Skipped when the untracked `cache/` is absent, matching
  `Native950ComponentDecoderCacheTest`'s pattern.
- `tests/modern947/Native950ActionsTest.java`: updated the `implementedOpcodes().length` expectation
  (59 → 60); its ascending/no-duplicate and `clientSize` agreement checks pass unchanged for opcode 124.
- Full suite: 187 classes, 1,617 tests, 0 failures (up from 185/1,611 before this pass; +2 test
  classes, +6 test methods net).

## 8. What this actually unlocks

Nothing gameplay-visible yet. It turns one previously-invisible, well-formed inbound frame type into
a named, typed, tested `Action` that any future consumer can pattern-match on — the same step Phase 2
took for the interface decoder before any feature used its new fields. It does **not** fix Equipment
Binding, and it does not yet feed any HUD, settings, or Developer Console system.

## 9. What remains unsolved

- Which of the 10 real components (if any) actually emit opcode 124 in live play, and with what
  `slot`/`value` semantics in practice, is not provable from cache data alone — it needs a packet
  capture during physical interaction with one of them (the 590/1438 "bar" pairs are the best
  candidates; they're the "counter" widgets used in a few settings/graphics-preset panels — exact
  in-game location not identified without opening the interface builder or capturing traffic).
- The type-13 "event_text" additional hook (a string-valued native text-commit contract) is a
  different transport this investigation did not decode; it is not opcode 124.
- 1475:32's additional hooks are all null despite setting bit 24 — mechanism unknown.
- No downstream consumer exists yet; one should only be added once a real interaction is observed
  and a concrete need is identified (see §8).

## READY FOR USER TEST

Not requesting one. This phase is decode-only with no gameplay effect, no server behaviour change,
and nothing to click that would look any different — a physical test right now would only reproduce
what the cache evidence and unit tests already establish more precisely. If you want to help close
§9's open question, the useful physical step is different in kind from a pass/fail test: open the
settings/graphics panel(s) that contain interfaces 590 and 1438 and watch outbound traffic (or the
transport's `unhandledFrames`/`observedUnhandledOpcodes` counters, which this change removes opcode
124 from) while adjusting whatever "bar"/stepper control lives there. That's worth doing on your own
schedule, not as a pass/fail gate on this phase.
