# Native 950 chat framing correction

Date: 2026-09-10. Read-only derivation from the shipped 950 Windows client:
`OpenNXT/data/clients/950/win64/original/rs2client.exe`.
SHA-256: `fc7492548627a4068e88a7a45ba4f0be93004adf246bb4b174b24ae588768b36`.

## Live failure and cause

`logs/server.err.log` recorded `Malformed native 950 action for opcode 87`
followed by reconnects when the user entered a development command. The live cache's
Huffman table was installed. The chat decoder mistakenly demanded a second packet
length inside a body whose transport length had already been consumed. Existing
chat fixtures repeated that mistake, so their success did not verify the real client.

The opcode mapping was correct. `tools/reg950.py --opcode 87` independently maps
client opcode 87, variable-byte size -1, to descriptor `0x140E94850`. Its sender is
`0x14009B1E0`. Client opcode 72 is variable-short size -2 and carries private chat.

## Native framing proof

Packet allocator `0x1400AF710` initializes its buffer through `0x14008E890`
and then writes the single encrypted opcode at `0x1400AF856` (or unencrypted at
`0x1400AF868`). It advances the buffer cursor by one, and returns. It does not
reserve another byte/word for variable length.

Public-chat sender selects descriptor `0x140E94850` at `0x14009B360` and calls
that allocator at `0x14009B38B`. It writes one zero byte at `0x14009B3A4`, remembers
the cursor after it, then writes colour at `0x14009B3BC`, effect at `0x14009B3CF`,
and smart count plus compressed text through `0x1403ED9E0` at `0x14009B3E1`.
At `0x14009B3E6..0x14009B3FF`, final minus saved cursor back-fills that one zero.
`0x14009B484..0x14009B49C` queues the existing full buffer length; no additional
header is inserted. Thus the sole byte count is transport framing, not body data.

Private-chat sender uses the same allocator at `0x14009B6CC`, writes one two-byte
placeholder at `0x14009B6E4`, recipient through `0x1400AE0C0` at `0x14009B71C`,
and compressed text at `0x14009B772`. `0x14009B792..0x14009B7C8` back-fills the
single word big-endian. These two bytes are likewise transport framing.

`Native950InboundDecoder.feed` consumes the descriptor's -1/-2 byte count before
allocating/filling `Frame.payload`. Therefore decoded public body starts with
colour and effect; decoded private body starts with the recipient.

## Independent literal examples

Zero ISAAC offset is shown to make the opcode legible:

- Public `hi`: `57 05 00 00 02 84 80`.
- Private `Bob` / `hi`: `48 00 07 42 6f 62 00 02 84 80`.
- Public `;;nxt banker`: `57 0d 00 00 0c 06 18 18 6b 15 b8 8b 4b 5c ea`.
- Public `;;nxt status`: `57 0d 00 00 0c 06 18 18 6b 15 b8 ef 27 7b 40`.

The command constants use the actual index 10/group 1/file 0 Huffman table,
identical to the checked-in 256-byte fixture. The `hi` bitstream and smart count
also match the independently recorded older client worked example. The test
now gives complete literal frames to the real incremental transport, at every
TCP split, rather than constructing a duplicate body prefix. ISAAC sequencing,
colour/effect limits, smart width, malformed compressed data, and missing-codec
behavior retain focused coverage.

## Command route

Actual 950 client scripts 112 and 1633 normalize identically to their 947 versions
with the established opcode mapping, including their metadata/footer. Their send
opcode (947 `0x2fc` -> 950 `0x1ce`) registers native function `0x14009B1E0`.
Its prefix parser `0x1400994E0` handles recognized colon-delimited colour/effect
words; `;;nxt banker` has no colon and reaches this public-chat sender unchanged.
The server's development-command matcher then runs after typed public-chat decode.
No 947 cheat opcode was guessed or enabled.

## Same defect in text/name prompt replies

The same opcode-only allocator is called at `0x140205E7C` / `0x140205A1C`.
The text/name senders precompute their byte count through `0x1400AC250`, write
it at `0x140205E98` / `0x140205A38`, then write the NUL-terminated string at
`0x140205ECA` / `0x140205A6A`. Queue finalization at `0x140205F59..0x140205F67`
and `0x140205AF9..0x140205B07` likewise adds no header. Thus opcodes 17 and 53
also wrongly expected their transport count a second time. They now decode plain
terminated text, including the valid empty string, after framing. Added literal
fragmented wire fixtures `11 04 42 6f 62 00` and `35 07 5a 65 7a 69 6d 61 00`.
The two identical-format rows' name/free-text semantic labels remain provisional.
Fixed-width amount opcode 120, used by Withdraw-X, is unaffected.

## Scope

Corrected `Native950Actions` public/private and string/name offsets, plus the chat/dialogue wire fixtures.
Huffman coding, incoming opcode assignments, transport framing, command permissions,
and the legacy 910/947 project remain unchanged. No build, client restart, or live
packet replay was performed by the author of this derivation. Live verification
belongs to the parent task after its combined build.
