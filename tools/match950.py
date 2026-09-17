#!/usr/bin/env python3
"""Map a verified 947 packet parser onto its 950 opcode by normalized-shape matching.

The 947 project verified each packet's parser address (see AstraNXT
OpenNXT/data/prot/947/generated/native947-3/verified/verifiedNames.toml). This walks that parser,
normalizes it, and scores it against every 950 handler parserTarget from
protocol-analysis/950-native.json, so the 950 opcode can be read off instead of guessed from
registration order.

Normalization drops absolute addresses and rip displacements, keeping mnemonics, operand shapes,
register roles and immediates. Matching is a similarity ratio over that token sequence.

  match950.py one   <947VA> [--top N]        rank 950 handlers against one 947 parser
  match950.py table [--top N]                run the whole verified 947 table
  match950.py show  <VA> [--rev 947|950]     print the normalized token stream
"""
import argparse
import difflib
import json
import re
import sys
from pathlib import Path

sys.dont_write_bytecode = True
sys.path.insert(0, str(Path(__file__).resolve().parent))
from dis950 import Image, EXE_947, EXE_950  # noqa: E402

NATIVE_950 = Path(__file__).resolve().parents[1] / "protocol-analysis" / "950-native.json"

# name -> 947 parser VA, from the 947 project's verifiedNames.toml evidence comments.
VERIFIED_947 = {
    "UPDATE_STAT": (66, 0x140140410),
    "VARP_SMALL": (10, 0x1401414A0),
    "VARP_LARGE": (111, 0x140141330),
    "VARBIT_SMALL": (50, 0x1401410E0),
    "VARBIT_LARGE": (71, 0x140140FF0),
    "CLIENT_SETVARC_SMALL": (1, 0x140140EE0),
    "CLIENT_SETVARC_LARGE": (112, 0x140140DC0),
    "CLIENT_SETVARCBIT_SMALL": (115, 0x140140B60),
    "CLIENT_SETVARCBIT_LARGE": (55, 0x140140A60),
    "CLIENT_SETVARCSTR_SMALL": (67, 0x140140940),
    "CLIENT_SETVARCSTR_LARGE": (15, 0x140140810),
    "RESET_CLIENT_VARCACHE": (48, 0x1401415C0),
    "UPDATE_RUNENERGY": (116, 0x1401064A0),
    "UPDATE_RUNWEIGHT": (108, 0x140106400),
    "UPDATE_REBOOT_TIMER": (52, 0x14010A780),
    "IF_SETEVENTS": (35, 0x140109450),
    "MUSIC": (87, 0x14010BF70),
}

HEX = re.compile(r"0x[0-9a-f]+")


def _walk(img, start, limit):
    """Basic-block walk from a function start, returned in address order."""
    import capstone
    seen, todo, out = set(), [start], {}
    while todo:
        va = todo.pop()
        if va in seen:
            continue
        seen.add(va)
        cur = va
        while len(out) < limit:
            ins = img.dis(cur, 32)
            if not ins:
                break
            i = ins[0]
            if i.address in out:
                break
            out[i.address] = i
            m = i.mnemonic
            if m.startswith("ret") or m in ("int3", "ud2"):
                break
            if m.startswith("j"):
                tgt = (i.operands[0].imm
                       if i.operands and i.operands[0].type == capstone.x86.X86_OP_IMM else None)
                if tgt is not None and start - 0x8000 < tgt < start + 0x8000:
                    todo.append(tgt)
                if m == "jmp":
                    break
            cur = i.address + i.size
    return [out[k] for k in sorted(out)]


def tokens(img, va, limit=400):
    """Normalized shape-only token stream for a parser function."""
    out = []
    for i in _walk(img, va, limit):
        ops = i.op_str
        ops = re.sub(r"rip \+ 0x[0-9a-f]+", "rip+D", ops)
        ops = re.sub(r"\[0x[0-9a-f]+\]", "[A]", ops)
        # keep small immediates (they encode field transforms like 0x80), abstract large ones
        def imm(m):
            v = int(m.group(0), 16)
            return m.group(0) if v <= 0xffff else "IMM"
        ops = HEX.sub(imm, ops)
        out.append(i.mnemonic + " " + ops)
    return out


def handlers_950():
    data = json.loads(NATIVE_950.read_text())
    rows = []
    for h in data["handlers"]:
        t = h.get("parserTarget") or h.get("dispatchMethod")
        if t:
            rows.append((h["opcode"], int(t, 16)))
    return rows


def score(a, b):
    return difflib.SequenceMatcher(None, a, b).ratio()


def rank(img947, img950, va, cache, top):
    ref = tokens(img947, va)
    scored = []
    for opcode, target in cache:
        if target not in cache_tokens:
            cache_tokens[target] = tokens(img950, target)
        scored.append((score(ref, cache_tokens[target]), opcode, target))
    scored.sort(reverse=True)
    return ref, scored[:top]


cache_tokens = {}


def main():
    p = argparse.ArgumentParser()
    sub = p.add_subparsers(dest="cmd", required=True)
    q = sub.add_parser("one"); q.add_argument("va"); q.add_argument("--top", type=int, default=5)
    q = sub.add_parser("table"); q.add_argument("--top", type=int, default=3)
    q = sub.add_parser("show"); q.add_argument("va"); q.add_argument("--rev", default="950")
    a = p.parse_args()

    if a.cmd == "show":
        img = Image(EXE_950 if a.rev == "950" else EXE_947)
        for t in tokens(img, int(a.va, 0)):
            print("  " + t)
        return

    img947, img950 = Image(EXE_947), Image(EXE_950)
    rows = handlers_950()
    if a.cmd == "one":
        _, best = rank(img947, img950, int(a.va, 0), rows, a.top)
        for s, opcode, target in best:
            print("  %.3f  950 opcode %-4d parser 0x%x" % (s, opcode, target))
        return

    # Score every packet against every handler, then assign greedily by best score so two
    # packets can never claim the same 950 opcode.
    grid = {}
    for name, (op947, va) in VERIFIED_947.items():
        _, best = rank(img947, img950, va, rows, len(rows))
        grid[name] = best
    pairs = sorted(((s, name, o, t) for name, best in grid.items() for s, o, t in best),
                   reverse=True)
    taken_name, taken_op, assigned = set(), set(), {}
    for s, name, o, t in pairs:
        if name in taken_name or o in taken_op:
            continue
        taken_name.add(name); taken_op.add(o)
        assigned[name] = (s, o, t)

    print("%-26s %-11s %-11s %-7s %s" % ("packet", "947 opcode", "950 opcode", "score", "runners-up"))
    for name, (op947, _va) in VERIFIED_947.items():
        s, o, t = assigned.get(name, (0.0, -1, 0))
        others = "  ".join("%.2f:%d" % (x, y) for x, y, _ in grid[name][1:1 + a.top])
        grade = "EXACT" if s >= 0.98 else "strong" if s >= 0.85 else "weak" if s >= 0.70 else "NONE"
        print("%-26s %-11d %-11s %-7s %-6s %s" % (name, op947, o if s > 0 else "-",
              "%.3f" % s, grade, others))


if __name__ == "__main__":
    main()
