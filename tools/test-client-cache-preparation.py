"""Optional Python 3 standard-library verification for the bundled native cache seeder.

Run from any folder:
    python tools/test-client-cache-preparation.py --root C:\\Games\\950OpenSource

All generated databases, deliberately damaged fixtures, logs, and the JSON report
stay in a fresh temporary directory, or in the new/empty directory supplied with
--work. The production flat cache and existing native profile are read-only.
"""

import argparse
import bz2
import hashlib
import json
import lzma
import os
from pathlib import Path
import shutil
import sqlite3
import subprocess
import tempfile
import time
import zlib

STARTUP_INDEXES = {
    2, 3, 10, 12, 13, 16, 17, 18, 19, 20, 21, 22, 23, 24,
    26, 27, 28, 29, 42, 49, 57, 58, 59, 60, 61, 62, 65, 66,
}


def require(condition, message):
    if not condition:
        raise AssertionError(message)


def signed(value):
    return (value + 2**31) % 2**32 - 2**31


def container_end(raw):
    require(len(raw) >= 5 and raw[0] in range(4), "Invalid source JS5 container")
    size = int.from_bytes(raw[1:5], "big", signed=True)
    header = 5 if raw[0] == 0 else 9
    end = header + size
    require(size >= 0 and len(raw) >= header and len(raw) in (end, end + 2),
            "Invalid source JS5 container length")
    return end


def decode_container(raw):
    end = container_end(raw)
    kind = raw[0]
    body = raw[5:end] if kind == 0 else raw[9:end]
    if kind == 0:
        decoded = body
    elif kind == 1:
        decoded = bz2.decompress(b"BZh1" + body)
    elif kind == 2:
        decoded = zlib.decompress(body, wbits=31)
    else:
        # JS5 stores five LZMA properties followed by a raw stream. The standard
        # library's ALONE decoder also requires an eight-byte little-endian size.
        require(len(body) >= 5, "Truncated LZMA properties")
        size = int.from_bytes(raw[5:9], "big")
        stream = body[:5] + size.to_bytes(8, "little") + body[5:]
        decoded = lzma.LZMADecompressor(format=lzma.FORMAT_ALONE).decompress(stream)
    if kind:
        require(len(decoded) == int.from_bytes(raw[5:9], "big"), "Decoded container length differs")
    return decoded


def reference_header(decoded):
    require(decoded and decoded[0] in (5, 6, 7), "Invalid reference-table format")
    version = int.from_bytes(decoded[1:5], "big", signed=True) if decoded[0] >= 6 else 0
    position = 6 if decoded[0] >= 6 else 2
    width = 4 if decoded[0] >= 7 and decoded[position] >= 128 else 2
    groups = int.from_bytes(decoded[position:position + width], "big")
    if width == 4:
        groups &= 0x7fffffff
    return version, groups


def decode_native(blob):
    require(blob[:4] == b"ZLB\x01", "Expected native ZLB storage")
    decoded = zlib.decompress(blob[8:])
    require(len(decoded) == int.from_bytes(blob[4:8], "big"), "Native decoded length differs")
    return decoded


def open_read_only(path):
    return sqlite3.connect(path.resolve().as_uri() + "?mode=ro", uri=True, timeout=1)


def database_hashes(directory):
    return {path.name: hashlib.sha256(path.read_bytes()).hexdigest()
            for path in directory.glob("*.jcache")}


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--root", type=Path, default=Path(__file__).resolve().parent.parent,
                        help="Bundle root; defaults to the parent of this script's tools directory")
    parser.add_argument("--work", type=Path,
                        help="New or empty output directory; defaults to a fresh system temporary folder")
    parser.add_argument("--skip-native-comparison", action="store_true",
                        help="Skip optional read-only comparisons against this bundle's existing native cache")
    args = parser.parse_args()
    root = args.root.resolve(strict=True)
    source = root / "cache"
    reference_directory = source / "255"
    java = root / "runtime" / "java25" / "bin" / "java.exe"
    libraries = root / "OpenNXT" / "runtime" / "lib"
    require(java.is_file(), f"Missing bundled Java runtime: {java}")
    require(reference_directory.is_dir(), f"Missing flat reference tables: {reference_directory}")
    require((libraries / "OpenNXT-1.0.0.jar").is_file(), "Missing built OpenNXT runtime jar")
    references = {int(path.stem): path for path in reference_directory.glob("*.dat")
                  if path.stem.isdigit() and 0 <= int(path.stem) < 255}
    require(STARTUP_INDEXES <= references.keys(), "The source is missing required startup reference tables")
    if args.work:
        work = args.work.resolve()
        for protected in (source.resolve(), (root / "client-state").resolve()):
            require(work != protected and protected not in work.parents,
                    f"Test work must be outside the production cache/profile: {protected}")
        require(not work.exists() or (work.is_dir() and not any(work.iterdir())),
                "--work must name a new or empty directory")
        work.mkdir(parents=True, exist_ok=True)
    else:
        work = Path(tempfile.mkdtemp(prefix="950-cache-tests-")).resolve()
    classpath = os.pathsep.join(str(path) for path in sorted(libraries.glob("*.jar")))
    log_directory = work / "logs"
    log_directory.mkdir()
    report = {"root": str(root), "work": str(work), "checks": [], "status": "running"}
    report_path = work / "validation-results.json"
    print(f"Testing native cache preparation in {work}", flush=True)

    def record(name, **details):
        report["checks"].append({"name": name, "passed": True, **details})

    def run(label, input_source, destination, success=True):
        started = time.perf_counter()
        completed = subprocess.run([
            str(java), "-Xmx1g", "-Dfile.encoding=UTF-8", "--enable-native-access=ALL-UNNAMED",
            "-cp", classpath, "com.opennxt.tools.bundle.PrepareClientCache",
            str(input_source), str(destination), "startup",
        ], capture_output=True, text=True, encoding="utf-8", errors="replace", timeout=300)
        (log_directory / f"{label}.stdout.log").write_text(completed.stdout, encoding="utf-8")
        (log_directory / f"{label}.stderr.log").write_text(completed.stderr, encoding="utf-8")
        require((completed.returncode == 0) == success,
                f"{label}: unexpected exit {completed.returncode}; see {log_directory}\n{completed.stderr[-1500:]}")
        return completed, round(time.perf_counter() - started, 3)

    try:
        fixture = work / "fixture"
        _, seconds = run("initial-seed", source, fixture)
        record("initialSeed", seconds=seconds)
        require(len(list(fixture.glob("*.jcache"))) == len(references), "Reference database count differs")
        group_count = database_bytes = comparable_samples = stale_samples = 0
        native_unavailable = []
        for index, reference_path in sorted(references.items()):
            path = fixture / f"js5-{index}.jcache"
            database_bytes += path.stat().st_size
            with open_read_only(path) as database:
                require(database.execute("PRAGMA quick_check").fetchone() == ("ok",), f"SQLite check failed: {path}")
                row = database.execute("SELECT DATA,VERSION,CRC FROM cache_index WHERE KEY=1").fetchone()
                require(row is not None, f"Missing native reference row: {index}")
                raw = reference_path.read_bytes()
                decoded = decode_container(raw)
                version, expected_groups = reference_header(decoded)
                require(decode_native(row[0]) == decoded, f"Native reference payload differs: {index}")
                require(row[1] == version and row[2] == signed(zlib.crc32(raw[:container_end(raw)]) + 1),
                        f"Native reference metadata differs: {index}")
                actual_groups = database.execute("SELECT COUNT(*) FROM cache").fetchone()[0]
                require(actual_groups == (expected_groups if index in STARTUP_INDEXES else 0),
                        f"Seeded group count differs: {index}")
                group_count += actual_groups
                native_path = root / "client-state" / "Jagex" / "RuneScape" / path.name
                if args.skip_native_comparison or not native_path.is_file():
                    continue
                try:
                    with open_read_only(native_path) as native:
                        samples = native.execute("SELECT KEY,DATA,VERSION,CRC FROM cache LIMIT 3").fetchall()
                except sqlite3.Error as error:
                    native_unavailable.append({"index": index, "reason": str(error)})
                    continue
                for key, blob, native_version, native_crc in samples:
                    seeded = database.execute("SELECT DATA,VERSION,CRC FROM cache WHERE KEY=?", (key,)).fetchone()
                    if seeded is None:
                        continue
                    if seeded[1:] != (native_version, native_crc):
                        stale_samples += 1
                        continue
                    require(decode_native(blob) == decode_native(seeded[0]),
                            f"Existing native payload differs for matching metadata: {index}:{key}")
                    comparable_samples += 1
        report.update(databaseCount=len(references), groupCount=group_count, databaseBytes=database_bytes)
        record("sqliteIntegrityAndReferencePayloads", references=len(references))
        record("optionalNativeGroupSamples", compared=comparable_samples, staleSkipped=stale_samples,
               unavailable=native_unavailable, explicitlySkipped=args.skip_native_comparison)

        resume = work / "resume"
        shutil.copytree(fixture, resume)
        before = database_hashes(resume)
        completed, seconds = run("resume", source, resume)
        require(before == database_hashes(resume), "Resume changed an already prepared database")
        require(f"inserted=0 reused={group_count}" in completed.stdout, "Resume did not reuse every seeded group")
        record("resumePreservesEveryDatabaseByte", seconds=seconds)

        # Derive test group IDs from the new fixture rather than depending on a
        # previously cached native row or a prior test's metadata helper.
        with open_read_only(fixture / "js5-2.jcache") as database:
            test_keys = [row[0] for row in database.execute("SELECT KEY FROM cache ORDER BY KEY LIMIT 2")]
            group_keys = [row[0] for row in database.execute("SELECT KEY FROM cache ORDER BY KEY")]
        require(len(test_keys) == 2, "Index 2 needs two groups for the rollback fixture")
        repair_key, corrupt_key = test_keys
        unrelated_key = max(999999, max(group_keys) + 1)
        with sqlite3.connect(resume / "js5-2.jcache") as database:
            expected_crc = database.execute("SELECT CRC FROM cache WHERE KEY=?", (repair_key,)).fetchone()[0]
            raw = (source / "2" / f"{repair_key}.dat").read_bytes()
            database.execute("UPDATE cache SET DATA=?,CRC=? WHERE KEY=?", (raw, signed(expected_crc + 1), repair_key))
            database.execute("INSERT INTO cache VALUES(?,?,?,?)", (unrelated_key, b"preserve-me", 123, 456))
        _, seconds = run("wrong-mode-repair", source, resume)
        with open_read_only(resume / "js5-2.jcache") as database:
            require(database.execute("SELECT DATA,VERSION,CRC FROM cache WHERE KEY=?", (unrelated_key,)).fetchone()
                    == (b"preserve-me", 123, 456), "Repair changed an unrelated native row")
            require(database.execute("SELECT hex(substr(DATA,1,4)),CRC FROM cache WHERE KEY=?", (repair_key,)).fetchone()
                    == ("5A4C4201", expected_crc), "Repair did not replace incompatible raw mode")
        record("repairsWrongModeAndPreservesUnrelatedRows", seconds=seconds)

        bad_source = work / "bad-source"
        (bad_source / "255").mkdir(parents=True)
        (bad_source / "2").mkdir()
        for path in references.values():
            shutil.copy2(path, bad_source / "255" / path.name)
        for key in group_keys:
            shutil.copy2(source / "2" / f"{key}.dat", bad_source / "2" / f"{key}.dat")
        bad_file = bad_source / "2" / f"{corrupt_key}.dat"
        damaged = bytearray(bad_file.read_bytes())
        damage_offset = 5 if damaged[0] == 0 else 9
        require(container_end(damaged) > damage_offset, "Selected corruption fixture has no payload")
        damaged[damage_offset] ^= 1
        bad_file.write_bytes(damaged)
        rollback = work / "rollback"
        rollback.mkdir()
        for index in references:
            if index <= 2:
                shutil.copy2(fixture / f"js5-{index}.jcache", rollback / f"js5-{index}.jcache")
        with sqlite3.connect(rollback / "js5-2.jcache") as database:
            database.execute("DELETE FROM cache WHERE KEY IN (?,?)", test_keys)
            database.execute("UPDATE cache_index SET CRC=0 WHERE KEY=1")
        before = database_hashes(rollback)
        completed, _ = run("corruption-rollback", bad_source, rollback, success=False)
        require(f"CRC mismatch in flat cache group 2:{corrupt_key}" in completed.stderr, "Wrong corruption failure")
        require(before == database_hashes(rollback), "Failed index did not roll back completely")
        record("badSourceCrcRollsBackExistingIndexCompletely")

        atomic = work / "atomic"
        run("corruption-new-database", bad_source, atomic, success=False)
        require(not (atomic / "js5-2.jcache").exists(), "A partial new database was published")
        require(not list(atomic.glob("*.tmp*")), "A failed seed left staging files")
        record("badSourceNeverPublishesPartialNewDatabase")

        schema = work / "schema"
        schema.mkdir()
        first_index = min(references)
        with sqlite3.connect(schema / f"js5-{first_index}.jcache") as database:
            database.execute("CREATE TABLE cache(KEY INTEGER PRIMARY KEY,DATA BLOB)")
            database.execute("INSERT INTO cache VALUES(7,?)", (b"keep",))
        before = database_hashes(schema)
        completed, _ = run("unexpected-schema", source, schema, success=False)
        require("Unexpected native cache schema" in completed.stderr, "Wrong schema refusal")
        require(before == database_hashes(schema), "Schema refusal changed the existing database")
        record("unexpectedSchemaFailsWithoutChangingDatabase")
        report["status"] = "passed"
    except Exception as error:
        report["status"] = "failed"
        report["failure"] = str(error)
        raise
    finally:
        report_path.write_text(json.dumps(report, indent=2, ensure_ascii=False), encoding="utf-8")
        print(f"Verification {report['status']}; report: {report_path}", flush=True)
    print(json.dumps(report, indent=2, ensure_ascii=False))


if __name__ == "__main__":
    main()
