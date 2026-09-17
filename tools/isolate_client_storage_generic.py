#!/usr/bin/env python3
"""Redirect a 950 client's Windows storage roots into this test folder, deriving every offset.

The original tools/isolate_client_storage.py hardcodes offsets for the win64 build. The Vulkan
build (binaryType 10) is a different, larger binary, so every one of those offsets is wrong for it.
This derives them instead:

  * the SHELL32.dll!SHGetFolderPathW import is located through the PE import table
  * every `call qword ptr [rip+disp]` reaching that IAT slot is found by scanning .text
  * a stub is placed in section slack (the zero padding between VirtualSize and SizeOfRawData)
  * each 6-byte indirect call becomes `e8 rel32` + `nop` to that stub
  * the host section's VirtualSize is extended so the stub is actually mapped

The stub ignores the request and writes a fixed path into SHGetFolderPathW's 5th argument
(`pszPath`, at [rsp+0x28] on entry), returning S_OK:

    mov  rax, [rsp+0x28]        ; pszPath
    lea  rdx, [rip+disp]        ; our UTF-16 path
    xor  ecx, ecx
  copy:
    movzx r8d, word [rdx+rcx*2]
    mov  [rax+rcx*2], r8w
    inc  rcx
    test r8w, r8w
    jne  copy
    xor  eax, eax               ; S_OK
    ret

If the executable slack can hold the stub and the path together, they are laid out contiguously,
which reproduces the original tool byte-for-byte. When it cannot (the Vulkan build has only 48
bytes of .text slack) the path is placed in a readable data section's slack instead and the stub's
rip-relative displacement is recomputed to reach it.

`verify` reproduces the existing, known-good client/rs2client.exe from win64/patched before any new
binary is touched.

  isolate_client_storage_generic.py verify
  isolate_client_storage_generic.py apply <clientDir> <outputExe>
"""
import hashlib
import json
import struct
import sys
from pathlib import Path

import pefile

ROOT = Path(__file__).resolve().parents[1]
CLIENTS = ROOT / "OpenNXT/data/clients/950"
STATE = (ROOT / "client-state").resolve()

STUB = bytes.fromhex(
    "488b442428"          # mov rax, [rsp+0x28]
    "488d15" "1c000000"   # lea rdx, [rip+disp]   (displacement patched below)
    "31c9"                # xor ecx, ecx
    "440fb7044a"          # movzx r8d, word [rdx+rcx*2]
    "6644890448"          # mov [rax+rcx*2], r8w
    "ffc1"                # inc ecx
    "664585c0"            # test r8w, r8w
    "75ee"                # jne copy
    "31c0"                # xor eax, eax
    "c3"                  # ret
)
LEA_DISP_OFF = 8          # offset of the rip-relative displacement inside STUB
LEA_END = 12              # first byte after the lea, for rip-relative arithmetic
assert len(STUB) == 35


def sections(pe):
    return sorted(pe.sections, key=lambda s: s.VirtualAddress)


def slack_of(pe, raw, sec):
    """The zero padding between VirtualSize and SizeOfRawData, and its room to the next section."""
    start = sec.PointerToRawData + sec.Misc_VirtualSize
    end = sec.PointerToRawData + sec.SizeOfRawData
    pad = raw[start:end]
    if not pad or set(pad) != {0}:
        return None
    ordered = sections(pe)
    i = ordered.index(sec)
    nxt = ordered[i + 1].VirtualAddress if i + 1 < len(ordered) else None
    headroom = (nxt - (sec.VirtualAddress + sec.Misc_VirtualSize)) if nxt else len(pad)
    return start, len(pad), headroom


def find_import_rva(pe, dll, name):
    base = pe.OPTIONAL_HEADER.ImageBase
    for e in getattr(pe, "DIRECTORY_ENTRY_IMPORT", []):
        if e.dll.decode().lower() != dll.lower():
            continue
        for imp in e.imports:
            if imp.name and imp.name.decode() == name:
                return imp.address - base
    return None


def find_call_sites(raw, text, iat_rva):
    delta = text.VirtualAddress - text.PointerToRawData
    lo = text.PointerToRawData
    hi = text.PointerToRawData + text.SizeOfRawData - 6
    out = []
    i = lo
    while i < hi:
        if raw[i] == 0xFF and raw[i + 1] == 0x15:
            disp = struct.unpack_from("<i", raw, i + 2)[0]
            if (i + delta) + 6 + disp == iat_rva:
                out.append(i)
        i += 1
    return out


def build(path_exe, state_path):
    pe = pefile.PE(path_exe)
    pe.parse_data_directories()
    raw = bytearray(Path(path_exe).read_bytes())
    text = [s for s in pe.sections if s.Name.rstrip(b"\0") == b".text"][0]
    delta = text.VirtualAddress - text.PointerToRawData

    iat = find_import_rva(pe, "SHELL32.dll", "SHGetFolderPathW")
    if iat is None:
        raise SystemExit("SHGetFolderPathW import not found")
    sites = find_call_sites(bytes(raw), text, iat)
    if not sites:
        raise SystemExit("no SHGetFolderPathW call sites found")

    payload_path = str(state_path).encode("utf-16le") + b"\0\0"

    exec_slack = slack_of(pe, bytes(raw), text)
    if exec_slack is None:
        raise SystemExit(".text slack is not zero padding")
    stub_raw, exec_len, exec_room = exec_slack

    contiguous = exec_len >= 40 + len(payload_path) and exec_room >= 40 + len(payload_path)
    grow = {}
    if contiguous:
        # Original layout: stub, 5 zero bytes, then the path. lea displacement stays 0x1c.
        path_raw = stub_raw + 40
        stub = bytearray(STUB)
        grow[text.get_file_offset() + 8] = text.Misc_VirtualSize + 40 + len(payload_path)
    else:
        host = None
        for s in sections(pe):
            if s is text or not s.IMAGE_SCN_MEM_READ:
                continue
            info = slack_of(pe, bytes(raw), s)
            if info and info[1] >= len(payload_path) and info[2] >= len(payload_path):
                host = (s, info)
                break
        if host is None:
            raise SystemExit("no data section slack large enough for the path")
        hsec, (path_raw, plen, proom) = host
        if exec_len < len(STUB) or exec_room < len(STUB):
            raise SystemExit(".text slack too small even for the stub")
        stub_rva = stub_raw + delta
        path_rva = path_raw + (hsec.VirtualAddress - hsec.PointerToRawData)
        stub = bytearray(STUB)
        struct.pack_into("<i", stub, LEA_DISP_OFF, path_rva - (stub_rva + LEA_END))
        grow[text.get_file_offset() + 8] = text.Misc_VirtualSize + len(STUB)
        grow[hsec.get_file_offset() + 8] = hsec.Misc_VirtualSize + len(payload_path)

    stub_rva = stub_raw + delta
    raw[stub_raw:stub_raw + len(stub)] = stub
    raw[path_raw:path_raw + len(payload_path)] = payload_path
    for off, value in grow.items():
        struct.pack_into("<I", raw, off, value)

    for off in sites:
        rva = off + delta
        rel = stub_rva - (rva + 5)
        raw[off:off + 6] = b"\xe8" + struct.pack("<i", rel) + b"\x90"

    report = {
        "call_sites": [hex(s) for s in sites],
        "iat_rva": hex(iat),
        "stub_rva": hex(stub_rva),
        "stub_file": hex(stub_raw),
        "path_file": hex(path_raw),
        "layout": "contiguous" if contiguous else "split",
        "storage_root": str(state_path),
        "grown_headers": {hex(k): hex(v) for k, v in grow.items()},
    }
    return bytes(raw), report


def cmd_verify(_a):
    src = CLIENTS / "win64/patched/rs2client.exe"
    expected = ROOT / "client/rs2client.exe"
    produced, report = build(str(src), STATE)
    print(json.dumps(report, indent=2))
    want = expected.read_bytes()
    same = produced == want
    print("\n  reproduced the known-good client/rs2client.exe byte-for-byte: %s" % ("YES" if same else "NO"))
    if not same:
        diff = [i for i in range(min(len(produced), len(want))) if produced[i] != want[i]]
        print("  %d differing bytes, first at 0x%x" % (len(diff), diff[0] if diff else -1))
        raise SystemExit(1)


def cmd_apply(a):
    src = Path(a[0]) if Path(a[0]).is_absolute() else CLIENTS / a[0]
    if src.is_dir():
        src = src / "patched" / "rs2client.exe"
    out = Path(a[1])
    STATE.mkdir(exist_ok=True)
    produced, report = build(str(src), STATE)
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_bytes(produced)
    report["input"] = str(src)
    report["input_sha256"] = hashlib.sha256(Path(src).read_bytes()).hexdigest()
    report["output"] = str(out)
    # Named to match the field Start-950Client.ps1 checks before launching.
    report["isolated_sha256"] = hashlib.sha256(produced).hexdigest()
    report_path = ROOT / "logs" / ("client-isolation-%s.json" % out.stem.replace("rs2client-", ""))
    report_path.parent.mkdir(exist_ok=True)
    report_path.write_text(json.dumps(report, indent=2), encoding="utf-8")
    report["report"] = str(report_path)
    print(json.dumps(report, indent=2))


CMDS = {"verify": cmd_verify, "apply": cmd_apply}

if __name__ == "__main__":
    if len(sys.argv) < 2 or sys.argv[1] not in CMDS:
        sys.exit(__doc__)
    CMDS[sys.argv[1]](sys.argv[2:])
