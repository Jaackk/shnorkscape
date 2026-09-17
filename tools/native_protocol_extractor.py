"""Extract native WIN64 protocol descriptors without launching or modifying the client.

Outputs are review artifacts, never active protocol maps. Packet names cannot be
proven from packet sizes: the generated name candidates are explicitly unverified.
Requires pefile and capstone (available in the workspace .venv).
"""
from __future__ import annotations

import argparse
import bisect
import collections
import difflib
import hashlib
import json
import re
import struct
import tomllib
from datetime import datetime, timezone
from pathlib import Path

import capstone
import pefile


KNOWN_SHA256 = "c8128a6566749afc629453235c4373845a5a17920eebfb01f5705d6e007ce4b9"
ROOT = Path(__file__).resolve().parents[1]
REGISTER_ALIASES = {
    "eax": "rax", "ecx": "rcx", "edx": "rdx", "r8d": "r8", "r9d": "r9",
}


def signed32(value: int) -> int:
    return struct.unpack("<i", struct.pack("<I", value & 0xFFFFFFFF))[0]


def normalized_register(instruction, register: int) -> str:
    name = instruction.reg_name(register)
    return REGISTER_ALIASES.get(name, name)


def extract_registrar_calls(pe: pefile.PE, scan_size: int = 0x22000) -> dict:
    """Evaluate straight-line MOV/XOR/LEA constructor arguments, stopping at branches.

    The constructors pass descriptor pointer, opcode, and size in RCX/RDX/R8.
    JMP tail calls matter: native client descriptors use them instead of CALL.
    """
    cs = capstone.Cs(capstone.CS_ARCH_X86, capstone.CS_MODE_64)
    cs.detail = True
    cs.skipdata = True
    image_base = pe.OPTIONAL_HEADER.ImageBase
    text = next(section for section in pe.sections if section.Name.rstrip(b"\0") == b".text")
    data = text.get_data()[:scan_size]
    data_ranges = [
        (image_base + section.VirtualAddress,
         image_base + section.VirtualAddress + max(section.Misc_VirtualSize, section.SizeOfRawData))
        for section in pe.sections if section.Characteristics & 0x80000000
    ]
    groups = collections.defaultdict(list)
    registers = {}
    for instruction in cs.disasm(data, image_base + text.VirtualAddress):
        mnemonic = instruction.mnemonic
        if mnemonic in {"int3", "ret", ".byte"}:
            registers.clear()
            continue
        operands = instruction.operands
        if mnemonic == "mov" and len(operands) == 2 and operands[0].type == capstone.CS_OP_REG:
            destination = normalized_register(instruction, operands[0].reg)
            source_value = None
            if operands[1].type == capstone.CS_OP_IMM:
                source_value = operands[1].imm
            elif operands[1].type == capstone.CS_OP_REG:
                source_value = registers.get(normalized_register(instruction, operands[1].reg))
            registers.pop(destination, None)
            if source_value is not None:
                registers[destination] = source_value
        elif (mnemonic == "xor" and len(operands) == 2
              and operands[0].type == capstone.CS_OP_REG
              and operands[1].type == capstone.CS_OP_REG
              and operands[0].reg == operands[1].reg):
            registers[normalized_register(instruction, operands[0].reg)] = 0
        elif mnemonic == "lea" and len(operands) == 2 and operands[1].type == capstone.CS_OP_MEM:
            destination = normalized_register(instruction, operands[0].reg)
            memory = operands[1].mem
            base_name = normalized_register(instruction, memory.base)
            index_name = normalized_register(instruction, memory.index)
            base_value = (instruction.address + instruction.size if base_name == "rip"
                          else registers.get(base_name, 0 if not memory.base else None))
            index_value = registers.get(index_name, 0 if not memory.index else None)
            registers.pop(destination, None)
            if base_value is not None and index_value is not None:
                registers[destination] = base_value + index_value * memory.scale + memory.disp
        elif mnemonic in {"call", "jmp"}:
            if (len(operands) == 1 and operands[0].type == capstone.CS_OP_IMM
                    and all(key in registers for key in ("rcx", "rdx", "r8"))):
                descriptor, opcode = registers["rcx"], registers["rdx"]
                size = signed32(registers["r8"])
                if (0 <= opcode < 512 and -2 <= size < 10000
                        and any(start <= descriptor < end for start, end in data_ranges)):
                    groups[operands[0].imm].append({
                        "opcode": opcode, "size": size, "descriptorVA": hex(descriptor),
                        "descriptorRVA": hex(descriptor - image_base),
                        "callSiteVA": hex(instruction.address),
                        "transfer": mnemonic,
                    })
            registers.clear()
        elif mnemonic.startswith("j"):
            registers.clear()
        else:
            # Forget values when an unmodelled instruction writes their registers.
            for register in instruction.regs_access()[1]:
                registers.pop(normalized_register(instruction, register), None)
    return dict(groups)


def select_table(groups: dict, minimum: int, maximum: int, stride: int) -> dict:
    candidates = []
    for registrar, rows in groups.items():
        ordered = sorted(rows, key=lambda row: row["opcode"])
        if not minimum <= len(ordered) <= maximum:
            continue
        if [row["opcode"] for row in ordered] != list(range(len(ordered))):
            continue
        base = int(ordered[0]["descriptorVA"], 16)
        if not all(int(row["descriptorVA"], 16) == base + row["opcode"] * stride for row in ordered):
            continue
        candidates.append({"registrarVA": hex(registrar), "count": len(ordered),
                           "descriptorBaseVA": hex(base), "stride": stride, "packets": ordered})
    if len(candidates) != 1:
        raise ValueError(f"Expected one contiguous stride-{stride} registrar, found {len(candidates)}")
    return candidates[0]


def descriptor_references(pe: pefile.PE, tables: dict) -> dict:
    """Locate RIP-relative descriptor uses and containing .pdata functions.

    These references support follow-up parser/sender analysis; they do not infer
    packet names or dereference a live process.
    """
    image_base = pe.OPTIONAL_HEADER.ImageBase
    functions = sorted((entry.struct.BeginAddress, entry.struct.EndAddress)
                       for entry in pe.DIRECTORY_ENTRY_EXCEPTION)
    starts = [start for start, _ in functions]
    ranges = [(side, int(table["descriptorBaseVA"], 16), table["stride"], table["count"])
              for side, table in tables.items()]
    references = {side: collections.defaultdict(list) for side in tables}
    text = next(section for section in pe.sections if section.Name.rstrip(b"\0") == b".text")
    cs = capstone.Cs(capstone.CS_ARCH_X86, capstone.CS_MODE_64)
    cs.skipdata = True
    rip_pattern = re.compile(r"\[rip ([+-]) (0x[0-9a-f]+)\]")
    for address, size, mnemonic, operands in cs.disasm_lite(text.get_data(), image_base + text.VirtualAddress):
        if "rip" not in operands:
            continue
        match = rip_pattern.search(operands)
        if not match:
            continue
        displacement = int(match[2], 16) * (1 if match[1] == "+" else -1)
        target = address + size + displacement
        for side, base, stride, count in ranges:
            if not base <= target < base + stride * count:
                continue
            opcode, field_offset = divmod(target - base, stride)
            rva = address - image_base
            index = bisect.bisect_right(starts, rva) - 1
            function_rva = (functions[index][0]
                            if index >= 0 and rva < functions[index][1] else None)
            references[side][opcode].append({
                "instructionVA": hex(address), "instructionRVA": hex(rva),
                "mnemonic": mnemonic, "operands": operands, "fieldOffset": hex(field_offset),
                "functionVA": hex(image_base + function_rva) if function_rva is not None else None,
            })
    return {side: dict(rows) for side, rows in references.items()}


def load_values(path: Path) -> dict[int, object]:
    return {int(key): value for key, value in tomllib.loads(path.read_text(encoding="utf-8"))["values"].items()}


def write_json(path: Path, value) -> None:
    path.write_text(json.dumps(value, indent=2) + "\n", encoding="utf-8")


def recover_server_handlers(pe: pefile.PE, references: dict) -> list[dict]:
    """Recover vtable dispatch addresses from each constructor's binding sequence.

    This exact binary loads its vtable into RAX shortly before the descriptor's
    +0x10 binding argument. Slot two is the packet dispatch method. A short
    adjustor thunk may tail-jump into the parser; neither address proves a name.
    """
    base = pe.OPTIONAL_HEADER.ImageBase
    cs = capstone.Cs(capstone.CS_ARCH_X86, capstone.CS_MODE_64)
    cs.detail = True
    result = []
    for opcode, rows in sorted(references.items()):
        for row in rows:
            if not (row["fieldOffset"] == "0x10" and row["mnemonic"] == "lea"
                    and row["operands"].startswith("rdx,")
                    and int(row["instructionRVA"], 16) < 0x700000):
                continue
            start = int(row["functionVA"], 16)
            end = int(row["instructionVA"], 16)
            before = list(cs.disasm(pe.get_data(start - base, end - start), start))[-12:]
            loads = [i for i in before if i.mnemonic == "lea"
                     and len(i.operands) == 2 and i.op_str.startswith("rax, [rip")]
            if not loads:
                raise ValueError(f"No nearby vtable load for server opcode {opcode}")
            load = loads[-1]
            vtable = load.address + load.size + load.operands[1].mem.disp
            dispatch = struct.unpack("<Q", pe.get_data(vtable - base + 16, 8))[0]
            parser = dispatch
            thunk = list(cs.disasm(pe.get_data(dispatch - base, 32), dispatch))[:5]
            for instruction in thunk:
                if instruction.mnemonic == "jmp" and instruction.operands[0].type == capstone.CS_OP_IMM:
                    parser = instruction.operands[0].imm
                    break
                if instruction.mnemonic in {"call", "ret"}:
                    break
            result.append({"opcode": opcode, "vtable": hex(vtable),
                           "dispatchMethod": hex(dispatch), "parserTarget": hex(parser),
                           "binding": row})
    if len(result) != 217 or len({row["opcode"] for row in result}) != 217:
        raise ValueError("Expected 217 uniquely bound native server handlers")
    return result


def align_server_bindings(reference_dir: Path, table: dict, references: dict) -> dict:
    """Draft cross-build correspondences from handler-binding order and sizes.

    Matching order is stronger than sizes alone, but neither proves old semantic
    labels nor the transformed payload fields. Keep every correspondence a draft.
    """
    old = json.loads((reference_dir / "generated/phase2/serverParsers.json").read_text())
    old.sort(key=lambda row: int(row["assignSite"], 16))
    old_sizes = load_values(reference_dir / "serverProtSizes.toml")
    old_names = load_values(reference_dir / "serverProtNames.toml")
    sizes = {row["opcode"]: row["size"] for row in table["packets"]}
    native = []
    for opcode, rows in references.items():
        for row in rows:
            if (row["fieldOffset"] == "0x10" and row["mnemonic"] == "lea"
                    and row["operands"].startswith("rdx,")
                    and int(row["instructionRVA"], 16) < 0x700000):
                native.append({"opcode": int(opcode), **row})
    native.sort(key=lambda row: int(row["instructionVA"], 16))
    old_sequence = [old_sizes[row["opcode"]] for row in old]
    native_sequence = [sizes[row["opcode"]] for row in native]
    matcher = difflib.SequenceMatcher(None, old_sequence, native_sequence, autojunk=False)
    matches = []
    for block in matcher.get_matching_blocks():
        for index in range(block.size):
            prior, current = old[block.a + index], native[block.b + index]
            matches.append({
                "referenceOpcode": prior["opcode"], "nativeOpcode": current["opcode"],
                "referenceName": old_names.get(prior["opcode"]),
                "size": sizes[current["opcode"]], "matchingSequenceLength": block.size,
                "referenceBinding": prior, "nativeBinding": current,
                "status": "unverified-ordered-binding-candidate",
                "eligibleForAutomaticPromotion": False,
            })
    return {"method": "Ordered handler-binding size sequence alignment",
            "referenceBindingCount": len(old), "nativeBindingCount": len(native),
            "ratio": matcher.ratio(), "matchedCount": len(matches),
            "limitations": ["Inherited packet names may themselves be wrong",
                            "Repeated equal sizes can produce ambiguous alignments",
                            "Payload field order and transforms are not inferred"],
            "matches": matches}


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--client-exe", type=Path, required=True)
    parser.add_argument("--reference-dir", type=Path, default=ROOT / "data/prot/946")
    parser.add_argument("--output-dir", type=Path, default=ROOT / "data/prot/947/generated/native947-3")
    args = parser.parse_args()
    raw = args.client_exe.read_bytes()
    sha256 = hashlib.sha256(raw).hexdigest()
    if sha256 != KNOWN_SHA256:
        raise ValueError("This extraction is scoped to the verified original native947-3 image; SHA256 differs")
    pe = pefile.PE(data=raw)
    if pe.FILE_HEADER.Machine != 0x8664:
        raise ValueError("Expected AMD64 image")
    groups = extract_registrar_calls(pe)
    tables = {"server": select_table(groups, 200, 250, 80),
              "client": select_table(groups, 120, 160, 16)}
    if (tables["server"]["count"], tables["client"]["count"]) != (218, 130):
        raise ValueError("Unexpected descriptor counts for the verified image")
    references = descriptor_references(pe, tables)
    output = args.output_dir.resolve()
    if output == args.reference_dir.resolve():
        raise ValueError("Output directory must differ from active protocol directory")
    output.mkdir(parents=True, exist_ok=True)
    metadata = {
        "source": str(args.client_exe.resolve()), "sourceSha256": sha256,
        "sourceModified": False, "platform": "WIN64", "binaryType": 2, "version": "947-3",
        "generatedAt": datetime.now(timezone.utc).isoformat(),
        "imageBase": hex(pe.OPTIONAL_HEADER.ImageBase),
        "method": "Static x64 constructor-argument evaluation and RIP-relative descriptor references",
        "limitations": ["Names are not proven by size", "No active maps modified", "No live process inspected"],
    }
    write_json(output / "build-info.json", metadata)
    write_json(output / "registrars.json", tables)
    write_json(output / "descriptor-references.json", references)
    write_json(output / "native-handler-bindings.json", recover_server_handlers(pe, references["server"]))
    alignment = align_server_bindings(args.reference_dir, tables["server"], references["server"])
    write_json(output / "handler-binding-alignment.draft.json", alignment)
    named_alignment = [row for row in alignment["matches"] if row["referenceName"]]
    (output / "serverProtNames.ordered-binding.draft.toml").write_text(
        "# UNVERIFIED candidates: ordered handler bindings and sizes only.\n"
        "# Inherited semantic labels and payload fields require independent validation.\n[values]\n"
        + "".join(f"{row['nativeOpcode']} = {json.dumps(row['referenceName'])}\n"
                  for row in sorted(named_alignment, key=lambda row: row["nativeOpcode"])), encoding="utf-8")
    drafts = {}
    changes = {}
    for side, table in tables.items():
        old_sizes = load_values(args.reference_dir / f"{side}ProtSizes.toml")
        old_names = load_values(args.reference_dir / f"{side}ProtNames.toml")
        old_parsers_path = args.reference_dir / "generated/phase2" / f"{side}Parsers.json"
        old_parsers = ({row["opcode"]: row for row in json.loads(old_parsers_path.read_text())}
                       if old_parsers_path.exists() else {})
        sizes = {row["opcode"]: row["size"] for row in table["packets"]}
        header = ("# Extracted from original native WIN64 947-3, SHA256 " + sha256 + "\n"
                  "# Review artifact only: names and packet fields must be independently remapped.\n[values]\n")
        (output / f"{side}ProtSizes.generated.toml").write_text(
            header + "".join(f"{opcode} = {size}\n" for opcode, size in sizes.items()), encoding="utf-8")
        changes[side] = [{"opcode": opcode, "oldSize": old_sizes.get(opcode), "nativeSize": size,
                          "oldName": old_names.get(opcode)}
                         for opcode, size in sizes.items() if old_sizes.get(opcode) != size]
        drafts[side] = []
        for old_opcode, name in old_names.items():
            candidates = [opcode for opcode, size in sizes.items() if size == old_sizes.get(old_opcode)]
            prior_parser = old_parsers.get(old_opcode, {})
            drafts[side].append({
                "name": name, "referenceOpcode": old_opcode, "referenceSize": old_sizes.get(old_opcode),
                "candidateOpcodes": candidates, "status": "unverified-size-candidates",
                "confidence": "low", "eligibleForAutomaticPromotion": False,
                "referenceParser": prior_parser,
                "nativeCandidates": [{"opcode": opcode,
                                      "descriptorVA": table["packets"][opcode]["descriptorVA"],
                                      "references": references[side].get(opcode, [])}
                                     for opcode in candidates],
            })
    write_json(output / "size-diff.json", changes)
    write_json(output / "name-candidates.draft.json", drafts)
    lines = ["# Native WIN64 947-3 protocol extraction", "",
             f"Source SHA256: `{sha256}`.", "",
             "These artifacts come from static analysis of the exact local executable. Active protocol maps were not changed.", "",
             "| Direction | Registrar VA | Descriptor base | Count | Stride | Size differences from reference |",
             "|---|---|---|---:|---:|---:|"]
    for side, table in tables.items():
        lines.append(f"| {side} | {table['registrarVA']} | {table['descriptorBaseVA']} | {table['count']} | {table['stride']} | {len(changes[side])} |")
    lines.extend(["", f"Reference maps: `{args.reference_dir}`. Before runtime integration, the inherited 947 maps were identical to 946 and their extraction metadata identified a March 16, 2026 build-946 Ghidra project. The default uses 946 so later edits to active 947 maps cannot contaminate the comparison.",
                  "", "`name-candidates.draft.json` contains same-size candidates with constructor globals and code references. Even a unique size candidate is not a verified name. No names are promoted.",
                  "", "`native-handler-bindings.json` resolves 217 native server bindings to vtables, dispatch methods, and short adjustor-thunk targets. Native binding arguments use descriptor +0x10. Verify packet semantics, field order, and transforms independently before use; the vtable slot alone does not prove names.",
                  "", "The `verified/` subdirectory contains separately reviewed minimum-world packet evidence. It is not generated or overwritten by this extraction. Its semantic findings supersede conflicting draft labels, including the inherited REBUILD_NORMAL candidate at opcode 104.",
                  "", "Existing Ghidra driver `run_946_extraction.py` accepts explicit registrar addresses, but its default paths, project name, working directory, and external Java-script dependency require adjustment. The legacy addresses 0x140301280 / 0x140301100 are wrong for this image."])
    (output / "README.md").write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(json.dumps({"output": str(output), "serverCount": tables["server"]["count"],
                      "clientCount": tables["client"]["count"],
                      "serverSizeDifferences": len(changes["server"]),
                      "clientSizeDifferences": len(changes["client"]),
                      "referenceCounts": {side: sum(len(v) for v in rows.values()) for side, rows in references.items()}}))


if __name__ == "__main__":
    main()
