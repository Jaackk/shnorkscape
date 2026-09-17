#!/usr/bin/env python3
"""Read-only disassembly helper for the isolated 950 revision test.

Never writes to any client binary. Defaults to the 950 WIN64 original; --rev 947
selects the reference 947 client from the AstraNXT project (read-only).

Subcommands:
  range  <VA> <LEN>          linear disassembly of LEN bytes at VA
  func   <VA> [--max N]      follow basic blocks from VA, print in address order
  xrefs  <VA>                direct call/jmp sites targeting VA
  drefs  <VA>                rip-relative data references to VA
  str    <TEXT>              ASCII + UTF-16LE string search, with drefs
  bytes  <VA> <LEN>          hex dump
  vaof   <RVA>               RVA -> VA helper / section info
"""
import argparse, os, re, sys
from pathlib import Path

import capstone
import pefile

EXE_950 = str(Path(__file__).resolve().parents[1] / "OpenNXT/data/clients/950/win64/original/rs2client.exe")
EXE_947 = str(Path(__file__).resolve().parents[2] / "AstraNXT/OpenNXT/data/clients/947/win64/original/rs2client.exe")


class Image:
    def __init__(self, path):
        self.path = path
        self.pe = pefile.PE(path, fast_load=True)
        self.base = self.pe.OPTIONAL_HEADER.ImageBase
        self.secs = []
        for s in self.pe.sections:
            name = s.Name.rstrip(b"\x00").decode("latin1")
            data = s.get_data()
            self.secs.append((name, self.base + s.VirtualAddress, s.Misc_VirtualSize, data,
                              bool(s.IMAGE_SCN_MEM_EXECUTE)))
        self.md = capstone.Cs(capstone.CS_ARCH_X86, capstone.CS_MODE_64)
        self.md.detail = True

    def sec_of(self, va):
        for name, sva, vsz, data, ex in self.secs:
            if sva <= va < sva + max(vsz, len(data)):
                return (name, sva, vsz, data, ex)
        return None

    def read(self, va, n):
        s = self.sec_of(va)
        if not s:
            return b""
        off = va - s[1]
        return s[3][off:off + n]

    def dis(self, va, n):
        return list(self.md.disasm(self.read(va, n), va))


def fmt(i):
    return "%x  %-8s %s" % (i.address, i.mnemonic, i.op_str)


TERM = {"ret", "jmp", "int3", "ud2"}


def cmd_range(img, a):
    for i in img.dis(int(a.va, 0), int(a.length, 0)):
        print(fmt(i))


def cmd_bytes(img, a):
    va = int(a.va, 0); n = int(a.length, 0)
    d = img.read(va, n)
    for o in range(0, len(d), 16):
        chunk = d[o:o + 16]
        print("%x  %-47s  %s" % (va + o, chunk.hex(" "),
              "".join(chr(c) if 32 <= c < 127 else "." for c in chunk)))


def cmd_func(img, a):
    start = int(a.va, 0)
    limit = a.max
    seen = set()
    todo = [start]
    out = {}
    while todo:
        va = todo.pop()
        if va in seen:
            continue
        seen.add(va)
        cur = va
        steps = 0
        while steps < 4000:
            ins = img.dis(cur, 64)
            if not ins:
                break
            i = ins[0]
            if i.address in out:
                break
            out[i.address] = i
            steps += 1
            m = i.mnemonic
            if m == "ret" or m.startswith("ret") or m in ("int3", "ud2"):
                break
            if m.startswith("j"):
                tgt = None
                if i.operands and i.operands[0].type == capstone.x86.X86_OP_IMM:
                    tgt = i.operands[0].imm
                if tgt is not None and start - 0x40000 < tgt < start + 0x40000:
                    if tgt not in seen:
                        todo.append(tgt)
                if m == "jmp":
                    break
            cur = i.address + i.size
            if len(out) > limit:
                break
    for va in sorted(out):
        print(fmt(out[va]))


def cmd_xrefs(img, a):
    tgt = int(a.va, 0)
    n = 0
    for name, sva, vsz, data, ex in img.secs:
        if not ex:
            continue
        # E8 rel32 call / E9 rel32 jmp
        for op, mn in ((0xE8, "call"), (0xE9, "jmp")):
            idx = 0
            while True:
                idx = data.find(bytes([op]), idx)
                if idx < 0 or idx + 5 > len(data):
                    break
                rel = int.from_bytes(data[idx + 1:idx + 5], "little", signed=True)
                site = sva + idx
                if site + 5 + rel == tgt:
                    print("%s %x -> %x" % (mn, site, tgt))
                    n += 1
                idx += 1
    print("# %d direct xrefs" % n)


def _disasm_resync(img, data, sva):
    """Sweep a whole section, restarting one byte past any undecodable byte.

    capstone stops at the first invalid instruction; a single linear pass therefore silently
    misses every reference after the first jump table or data island, which reads as "0 refs".
    """
    off = 0
    n = len(data)
    while off < n:
        produced = 0
        for i in img.md.disasm(data[off:], sva + off):
            produced = (i.address - sva) + i.size - off
            yield i
        off += produced if produced else 1


def cmd_drefs(img, a):
    tgt = int(a.va, 0)
    n = 0
    for name, sva, vsz, data, ex in img.secs:
        if not ex:
            continue
        for i in _disasm_resync(img, data, sva):
            for op in i.operands:
                if op.type == capstone.x86.X86_OP_MEM and op.mem.base == capstone.x86.X86_REG_RIP:
                    if i.address + i.size + op.mem.disp == tgt:
                        print("%x  %-8s %s" % (i.address, i.mnemonic, i.op_str))
                        n += 1
                elif op.type == capstone.x86.X86_OP_IMM and op.imm == tgt and i.mnemonic in ("mov", "lea", "push", "cmp"):
                    print("%x  %-8s %s  (imm)" % (i.address, i.mnemonic, i.op_str))
                    n += 1
    print("# %d data refs" % n)


def cmd_str(img, a):
    needle = a.text
    hits = []
    for name, sva, vsz, data, ex in img.secs:
        for enc, pat in (("ascii", needle.encode("latin1")),
                         ("utf16", needle.encode("utf-16-le"))):
            idx = 0
            while True:
                idx = data.find(pat, idx)
                if idx < 0:
                    break
                hits.append((sva + idx, enc, name))
                idx += 1
    for va, enc, sec in hits[:200]:
        d = img.read(va - 32, 160)
        txt = "".join(chr(c) if 32 <= c < 127 else "." for c in d)
        print("%x  [%s %s]  ...%s..." % (va, sec, enc, txt))
    print("# %d string hits" % len(hits))


def cmd_vaof(img, a):
    rva = int(a.rva, 0)
    va = img.base + rva
    s = img.sec_of(va)
    print("base=%x rva=%x va=%x sec=%s" % (img.base, rva, va, s[0] if s else "?"))


def main():
    p = argparse.ArgumentParser()
    p.add_argument("--rev", default="950", choices=("950", "947"))
    p.add_argument("--exe", default=None)
    sub = p.add_subparsers(dest="cmd", required=True)
    q = sub.add_parser("range"); q.add_argument("va"); q.add_argument("length"); q.set_defaults(fn=cmd_range)
    q = sub.add_parser("bytes"); q.add_argument("va"); q.add_argument("length"); q.set_defaults(fn=cmd_bytes)
    q = sub.add_parser("func"); q.add_argument("va"); q.add_argument("--max", type=int, default=1200); q.set_defaults(fn=cmd_func)
    q = sub.add_parser("xrefs"); q.add_argument("va"); q.set_defaults(fn=cmd_xrefs)
    q = sub.add_parser("drefs"); q.add_argument("va"); q.set_defaults(fn=cmd_drefs)
    q = sub.add_parser("str"); q.add_argument("text"); q.set_defaults(fn=cmd_str)
    q = sub.add_parser("vaof"); q.add_argument("rva"); q.set_defaults(fn=cmd_vaof)
    a = p.parse_args()
    path = a.exe or (EXE_950 if a.rev == "950" else EXE_947)
    if not os.path.exists(path):
        sys.exit("missing binary: %s" % path)
    a.fn(Image(path), a)


if __name__ == "__main__":
    main()
