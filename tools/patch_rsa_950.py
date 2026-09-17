#!/usr/bin/env python3
"""Apply the OpenNXT RSA key patch to a 950 client, outside the Kotlin tool.

The Kotlin ClientPatcher selects its target by BinaryType name and the enum stops at MOBILE(7),
so the Vulkan build (binaryType 10) cannot be addressed by it. This reimplements exactly what
ClientPatcher.patchFile does for a client binary:

  RSAUtil.findRSAKey(raw, bits) locates the Jagex modulus stored as an ASCII hex string of
  bits/4 characters, preceded by a NUL and followed by two NULs, then
  ByteArray.replaceFirst swaps it for the local modulus from data/config/rsa.toml.

Two keys are patched: js5 (4096-bit) and login (1024-bit). Nothing else is touched.

Correctness is not assumed: `verify` re-derives the existing win64 patch from its own original and
asserts the result is byte-identical to the win64/patched build that is already known to work
against this server. Only run `apply` after `verify` passes.

  patch_rsa_950.py verify              reproduce win64/patched from win64/original
  patch_rsa_950.py apply <dir>         patch <dir>/original/rs2client.exe -> <dir>/patched/
"""
import hashlib
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CLIENTS = ROOT / "OpenNXT/data/clients/950"
RSA_TOML = ROOT / "OpenNXT/data/config/rsa.toml"


def load_moduli():
    """Minimal reader for the [js5]/[login] modulus entries of rsa.toml."""
    section = None
    out = {}
    for line in RSA_TOML.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if line.startswith("[") and line.endswith("]"):
            section = line[1:-1]
        elif line.startswith("modulus") and section:
            out[section] = line.split("=", 1)[1].strip().strip('"')
    return out


def find_rsa_key(data, bits):
    """RSAUtil.findRSAKey: an ASCII hex run of bits/4 chars, NUL before, two NULs after."""
    size = bits // 4
    for i in range(1, len(data) - size - 2):
        if data[i] == 0 or data[i - 1] != 0:
            continue
        if data[i + size + 1] != 0 or data[i + size + 2] != 0:
            continue
        chunk = data[i:i + size]
        try:
            text = chunk.decode("ascii")
        except UnicodeDecodeError:
            continue
        if not re.fullmatch(r"[0-9a-fA-F]+", text):
            continue
        try:
            int(text, 16)
        except ValueError:
            continue
        return i, text
    return None, None


def patch(raw, moduli, label=""):
    data = bytearray(raw)
    report = {}
    for name, bits in (("js5", 4096), ("login", 1024)):
        off, old = find_rsa_key(bytes(data), bits)
        if off is None:
            raise SystemExit("%s: could not locate the %d-bit %s key" % (label, bits, name))
        new = moduli[name]
        # ClientPatcher relies on the replacement being the same length; a shorter modulus would
        # shift the string and corrupt the surrounding data.
        if len(new) != len(old):
            raise SystemExit("%s: %s modulus length %d != original %d" % (label, name, len(new), len(old)))
        data[off:off + len(old)] = new.encode("ascii")
        report[name] = (off, old[:24] + "...", new[:24] + "...")
    return bytes(data), report


def cmd_verify(_a):
    moduli = load_moduli()
    original = (CLIENTS / "win64/original/rs2client.exe").read_bytes()
    expected = (CLIENTS / "win64/patched/rs2client.exe").read_bytes()
    produced, report = patch(original, moduli, "win64")
    for k, (off, old, new) in report.items():
        print("  %-6s at 0x%x  %s -> %s" % (k, off, old, new))
    same = produced == expected
    print("\n  reproduced win64/patched byte-for-byte: %s" % ("YES" if same else "NO"))
    if not same:
        diff = [i for i in range(min(len(produced), len(expected))) if produced[i] != expected[i]]
        print("  %d differing bytes, first at 0x%x" % (len(diff), diff[0] if diff else -1))
        raise SystemExit(1)
    print("  sha256 %s" % hashlib.sha256(produced).hexdigest())


def cmd_apply(a):
    target = Path(a[0]) if Path(a[0]).is_absolute() else CLIENTS / a[0]
    src = target / "original" / "rs2client.exe"
    dst = target / "patched" / "rs2client.exe"
    moduli = load_moduli()
    raw = src.read_bytes()
    produced, report = patch(raw, moduli, str(target.name))
    for k, (off, old, new) in report.items():
        print("  %-6s at 0x%x  %s -> %s" % (k, off, old, new))
    dst.parent.mkdir(parents=True, exist_ok=True)
    dst.write_bytes(produced)
    print("\n  in  %s  sha256 %s" % (src, hashlib.sha256(raw).hexdigest()))
    print("  out %s  sha256 %s" % (dst, hashlib.sha256(produced).hexdigest()))
    print("  size unchanged: %s" % (len(produced) == len(raw)))


CMDS = {"verify": cmd_verify, "apply": cmd_apply}

if __name__ == "__main__":
    if len(sys.argv) < 2 or sys.argv[1] not in CMDS:
        sys.exit(__doc__)
    CMDS[sys.argv[1]](sys.argv[2:])
