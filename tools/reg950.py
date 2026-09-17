"""Recover the 950 client and server protocol descriptor tables from the executable itself.

Both tables are registered by a run of near-identical stubs, one per opcode, in ascending opcode
order. Each stub loads the opcode as an immediate and computes the size as a displacement off it:

    client stub (jmp form)          server stub (call form)
    mov  edx, <opcode>              mov  edx, <opcode>
    lea  rcx, [rip + <disp>]        lea  rcx, [rip + <disp>]     ; the descriptor object
    lea  r8d, [rdx - <k>]           lea  r8d, [rdx - <k>]        ; size = opcode - k, SIGNED
    jmp  0x1402f4260                call 0x1403113b0

so `size = opcode - k`, and because the lea is signed, the -1 and -2 variable-length markers fall
out of the same encoding (k > opcode). That is the whole descriptor table, readable statically.

The compiler does not stick to one shape, and every simplifying assumption about it costs coverage:
the size arrives as `lea r8d,[rdx-k]`, as `mov r8d,imm32` (every server opcode above 128) or as
`xor r8d,r8d` (every size-0 opcode), and the size-0 stubs put that xor BEFORE the `mov edx` the
others lead with. Capstone also prints small immediates in decimal, so a pattern anchored on
`edx, 0x...` drops opcodes 1..9 without saying so.

Two independent anchors therefore run, and their results are unioned:

  A. forward from `mov edx, <opcode>` - cannot attribute a size to the wrong opcode, because it
     starts at the operand that names the packet, but misses the reordered stubs;
  B. backward from the dispatch call, bounded by the previous call site - reaches the reordered
     stubs, at the risk of absorbing a neighbour's operands, which the bound prevents.

Where they overlap they must agree, and a disagreement is reported rather than resolved. On the
950 binary this recovers 122 client and 213 server opcodes, every one agreeing with the
checked-in toml; the handful neither anchor reaches are listed in the output.

Why this beats reading the table directly: the registration table those stubs populate lives in
.data and is filled at runtime, so its slots are zero on disk. Notes citing a handler as
"table base + N*80 + 0x10" describe where the initialiser writes, not something readable out of
the image. The stubs are the part that IS in the file.

Usage:
    python tools/reg950.py                 # both tables, with a check against the tomls
    python tools/reg950.py --rev 947       # the same for the 947 binary
    python tools/reg950.py --opcode 107    # one opcode from both tables
"""
import argparse
import io
import os
import re
import struct
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import capstone
import dis950 as D

# The two dispatch targets each family of stubs ends in. A stub is only accepted if it reaches one
# of these, which is what keeps an unrelated `mov edx, imm` from being read as a registration.
CLIENT_TAIL = 0x1402F4260
SERVER_TAIL = 0x1403113B0


# Capstone prints small immediates in decimal and larger ones as 0x..., so both forms appear in the
# same run of stubs. Matching only the 0x form silently drops every opcode below 10.
_IMM = re.compile(r"^(?:e|r)(?:dx|8d), (0x[0-9a-fA-F]+|\d+)$")


def _immediate(instruction, register):
    if instruction.mnemonic != "mov":
        return None
    m = _IMM.match(instruction.op_str)
    if not m or not instruction.op_str.startswith(register + ", "):
        return None
    return int(m.group(1), 0)


def _record(found, block, stub_va, conflicts):
    """Decode one stub's three operands and record it, flagging any disagreement."""
    opcode = descriptor = size_value = None
    for instruction in block:
        text_op = instruction.op_str
        immediate = _immediate(instruction, "edx")
        if immediate is not None:
            opcode = immediate
        elif instruction.mnemonic == "lea" and text_op.startswith("rcx, [rip"):
            m = re.search(r"\[rip \+ (0x[0-9a-f]+)\]", text_op)
            if m:
                descriptor = instruction.address + instruction.size + int(m.group(1), 16)
        # The size reaches r8d in one of three forms and the compiler picks whichever is shortest.
        # All three appear in the same run of stubs, so a reader that knows only the lea form
        # silently loses whole ranges - every server opcode above 128 here uses the mov form, and
        # every size-0 opcode uses the xor form.
        elif instruction.mnemonic == "lea" and text_op.startswith("r8d, [rdx"):
            m = re.search(r"\[rdx(?:\s*([+-])\s*(0x[0-9a-f]+|\d+))?\]", text_op)
            if m and opcode is not None:
                delta = 0 if m.group(2) is None else int(m.group(2), 0)
                if m.group(1) == "-":
                    delta = -delta
                size_value = opcode + delta
        elif _immediate(instruction, "r8d") is not None:
            size_value = _immediate(instruction, "r8d")
        elif instruction.mnemonic == "xor" and text_op in ("r8d, r8d", "r8, r8"):
            size_value = 0

    if opcode is None or size_value is None:
        return
    # These are signed 32-bit computations; -1 and -2 arrive as 0xFFFFFFFF / 0xFFFFFFFE.
    if size_value > 0x7FFFFFFF:
        size_value -= 1 << 32

    existing = found.get(opcode)
    if existing is not None and existing[0] != size_value:
        conflicts.append((opcode, existing[0], size_value))
        return
    if existing is None:
        found[opcode] = (size_value, descriptor, stub_va)


def _complete(block):
    """A block is a whole registration stub once it carries both the opcode and the descriptor."""
    return (any(_immediate(i, "edx") is not None for i in block)
            and any(i.mnemonic == "lea" and i.op_str.startswith("rcx, [rip") for i in block))


def _blocks_ending_at(text, base, md, call_offset, floor, window=0x40):
    """Disassemble the run of instructions that ends exactly at call_offset.

    Anchoring on the dispatch call rather than on the first operand is what makes this work: the
    compiler emits the three operands in whatever order suits it, and the size-0 stubs in
    particular put `xor r8d, r8d` before the `mov edx, <opcode>` that every other stub leads with.

    x86 has no reliable backward decode, so the start is searched for. Two bounds keep that search
    honest. `floor` is the end of the previous registration call, so a block can never absorb the
    previous stub's operands and report its opcode with this stub's size - a failure worse than
    missing the stub, because it looks like a real answer. Within that bound the int3 padding run
    is tried first, since most stubs are padded, and only then does it widen offset by offset.
    """
    lowest = max(floor, call_offset - window, 0)

    padded = call_offset
    while padded > lowest and text[padded - 1] != 0xCC:
        padded -= 1

    # Longest first: a short alignment decodes cleanly but stops before the operands.
    candidates = [padded] + list(range(lowest, call_offset - 3))
    for start in candidates:
        if start < lowest or start >= call_offset:
            continue
        block = []
        landed = False
        for instruction in md.disasm(text[start:call_offset + 8], base + start):
            if instruction.address == base + call_offset:
                landed = True
                break
            block.append(instruction)
        if landed and _complete(block):
            return block
    return []


def stubs(img, tail_target):
    """Every registration stub in .text that dispatches to tail_target."""
    for name, va0, size, data, is_code in img.secs:
        if name == ".text":
            text, base = data, va0
            break
    else:
        return {}

    md = capstone.Cs(capstone.CS_ARCH_X86, capstone.CS_MODE_64)
    md.detail = True
    found = {}
    conflicts = []

    # Every call/jmp rel32 in .text whose target is the dispatch function.
    sites = []
    for opbyte in (0xE8, 0xE9):
        start = 0
        while True:
            i = text.find(bytes([opbyte]), start)
            if i < 0 or i + 5 > len(text):
                break
            start = i + 1
            rel = struct.unpack("<i", text[i + 1:i + 5])[0]
            if base + i + 5 + rel == tail_target:
                sites.append(i)

    # Strategy A: anchor forward from `mov edx, <opcode>`. This cannot bleed into a neighbouring
    # stub, because it starts at the operand that names the packet and only reads forward, so
    # whatever it reports belongs to that opcode. It misses the stubs that put the size operand
    # before the opcode, which on this binary is every size-0 packet.
    start = 0
    while True:
        i = text.find(b"\xBA", start)
        if i < 0:
            break
        start = i + 1
        try:
            block = list(md.disasm(text[i:i + 0x20], base + i))
        except Exception:
            continue
        if len(block) < 4:
            continue
        head, second, third, fourth = block[:4]
        if _immediate(head, "edx") is None:
            continue
        if second.mnemonic != "lea" or not second.op_str.startswith("rcx, [rip"):
            continue
        if fourth.mnemonic not in ("jmp", "call"):
            continue
        try:
            if int(fourth.op_str, 16) != tail_target:
                continue
        except ValueError:
            continue
        _record(found, [head, second, third], base + i, conflicts)

    # Strategy B: anchor backward from the dispatch call, bounded by the previous call site so a
    # block can never absorb the previous stub's operands. This is what reaches the reordered
    # stubs. Where the two strategies overlap they must agree, and a disagreement is reported
    # rather than silently resolved - two readings of the same bytes differing means one of the
    # anchors is wrong, which is exactly the kind of thing that should not be papered over.
    sites.sort()
    for index, site in enumerate(sites):
        floor = sites[index - 1] + 5 if index else 0
        block = _blocks_ending_at(text, base, md, site, floor)
        if not block:
            continue
        _record(found, block, block[0].address, conflicts)
    return found, conflicts


def toml_sizes(path):
    out = {}
    if not os.path.isfile(path):
        return out
    for line in io.open(path, encoding="utf-8"):
        m = re.match(r'^\s*"?(\d+)"?\s*=\s*(-?\d+)', line)
        if m:
            out[int(m.group(1))] = int(m.group(2))
    return out


def report(label, table, reference, only=None):
    print("=== %s: %d opcodes recovered from .text ===" % (label, len(table)))
    disagreements = []
    for opcode in sorted(table):
        if only is not None and opcode != only:
            continue
        size, descriptor, stub = table[opcode]
        expected = reference.get(opcode)
        mark = ""
        if expected is None:
            mark = "  (not in the toml)"
        elif expected != size:
            mark = "  <-- TOML SAYS %d" % expected
            disagreements.append((opcode, size, expected))
        print("  opcode %3d  size %4d  descriptor 0x%X  stub 0x%X%s"
              % (opcode, size, descriptor or 0, stub, mark))
    missing = sorted(set(reference) - set(table))
    if only is None:
        if disagreements:
            print("  DISAGREEMENTS: %s" % disagreements)
        else:
            print("  every recovered size agrees with the toml")
        if missing:
            print("  in the toml but no stub found: %s" % missing)
    return disagreements, missing


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--rev", choices=("950", "947"), default="950")
    parser.add_argument("--opcode", type=int, default=None)
    args = parser.parse_args()

    exe = D.EXE_950 if args.rev == "950" else D.EXE_947
    img = D.Image(exe)
    root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    analysis = os.path.join(root, "protocol-analysis")

    client, client_conflicts = stubs(img, CLIENT_TAIL)
    server, server_conflicts = stubs(img, SERVER_TAIL)
    for label, rows in (("client", client_conflicts), ("server", server_conflicts)):
        if rows:
            print("!! %s: the two anchors disagree on %s (opcode, forward, backward)" % (label, rows))
    bad_c, _ = report("client protocol (packets the client sends)", client,
                      toml_sizes(os.path.join(analysis, "%s-client-sizes.toml" % args.rev)),
                      args.opcode)
    print()
    bad_s, _ = report("server protocol (packets the server sends)", server,
                      toml_sizes(os.path.join(analysis, "%s-server-sizes.toml" % args.rev)),
                      args.opcode)
    return 1 if (bad_c or bad_s) else 0


if __name__ == "__main__":
    sys.exit(main())
