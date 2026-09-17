#!/usr/bin/env python3
"""Read-only cache introspection for the isolated 950 revision test.

Reuses the AstraNXT 947 research readers without modifying them: they resolve the cache through
OPENNXT_CACHE_PATH, so pointing that at this project's cache is enough. Bytecode writing is
disabled before the imports so importing them cannot drop __pycache__ into the AstraNXT tree.

Set CACHE950_PATH to read a different cache (e.g. the 947 cache) with the same commands.

  exists <index> <group> [file]   presence and size
  enum <id>                       decode an enum
  params <structId>               decode a struct's params
  iface <id>                      component count for an interface
  probe                           check every id the 947 UI policy depends on
  hashes                          sha256 those groups, for cross-cache comparison
"""
import hashlib
import os
import sys
from pathlib import Path

sys.dont_write_bytecode = True
os.environ.setdefault("PYTHONDONTWRITEBYTECODE", "1")

DEFAULT_CACHE = str(Path(__file__).resolve().parents[1] / "cache")
CACHE = os.environ.get("CACHE950_PATH", DEFAULT_CACHE)
TOOLS = os.environ.get("NXT_REFERENCE_TOOLS", str(Path(__file__).resolve().parents[2] / "AstraNXT/OpenNXT/tools"))

os.environ["OPENNXT_CACHE_PATH"] = CACHE
sys.path.insert(0, TOOLS)

from inspect_native947_interface_slots import archive, enum, params, FLAT  # noqa: E402

assert Path(FLAT).resolve() == Path(CACHE).resolve(), "cache override failed: %s" % FLAT

IDX_INTERFACE, IDX_CLIENTSCRIPT, IDX_ENUM, IDX_STRUCT = 3, 12, 17, 22

INTERFACES = [
    (1477, "root"), (1482, "game view"), (1465, "minimap"), (1473, "backpack"),
    (1462, "worn equipment"), (137, "all chat"), (1431, "ribbon"), (1448, "menu window"),
    (1426, "settings host"), (365, "gameplay/accessibility"), (1444, "controls"),
    (567, "ribbon editor"), (1433, "quick options"), (187, "legacy music"),
]
ENUMS = [(7716, "root slot keys"), (7699, "settings menu"), (13319, "ribbon actors"),
         (680, "skills")]
SCRIPTS = [8179, 8181, 8182, 8146, 8186, 8190, 8191, 10068, 5588, 5589,
           8411, 8491, 1362, 8682, 8178, 20343, 15532]
STRUCTS = [21277, 21278, 21293, 21301, 21303, 21304]


def group_present(index, group):
    return (Path(FLAT) / str(index) / ("%d.dat" % group)).is_file()


def try_archive(index, group):
    if not group_present(index, group):
        return None
    try:
        return archive(index, group)
    except Exception as exc:
        return exc


def describe(files):
    if files is None:
        return "ABSENT"
    if isinstance(files, Exception):
        return "ERROR %s" % files
    return "%d files" % len(files)


def cmd_exists(a):
    index, group = int(a[0]), int(a[1])
    files = try_archive(index, group)
    if files is None or isinstance(files, Exception):
        print("%d/%d: %s" % (index, group, describe(files)))
        return
    if len(a) > 2:
        f = int(a[2])
        print("%d/%d/%d: %s" % (index, group, f,
              ("%d bytes" % len(files[f])) if f in files else "ABSENT"))
    else:
        print("%d/%d: %d files, ids %s" % (index, group, len(files), sorted(files)[:12]))


def cmd_enum(a):
    e = int(a[0])
    try:
        v = enum(e)
        print("enum %d: %d values" % (e, len(v)))
        for k in sorted(v):
            print("   %s -> %s" % (k, v[k]))
    except Exception as exc:
        print("enum %d: ERROR %s" % (e, exc))


def cmd_params(a):
    s = int(a[0])
    try:
        print("struct %d: %s" % (s, params(s)))
    except Exception as exc:
        print("struct %d: ERROR %s" % (s, exc))


def cmd_iface(a):
    i = int(a[0])
    print("interface %d: %s" % (i, describe(try_archive(IDX_INTERFACE, i))))


def cmd_probe(_a):
    print("== interfaces")
    for i, what in INTERFACES:
        print("   %-5d %-24s %s" % (i, what, describe(try_archive(IDX_INTERFACE, i))))
    print("== enums")
    for e, what in ENUMS:
        try:
            state = "%d values" % len(enum(e))
        except Exception as exc:
            state = "ERROR %s" % exc
        print("   %-6d %-20s %s" % (e, what, state))
    print("== client scripts")
    for s in SCRIPTS:
        files = try_archive(IDX_CLIENTSCRIPT, s)
        state = describe(files)
        if isinstance(files, dict):
            state = "%d bytes" % len(next(iter(files.values())))
        print("   %-6d %s" % (s, state))
    print("== structs")
    for s in STRUCTS:
        try:
            state = str(params(s))[:110]
        except Exception as exc:
            state = "ERROR %s" % exc
        print("   %-6d %s" % (s, state))


def cmd_hashes(_a):
    targets = ([(IDX_INTERFACE, i) for i, _ in INTERFACES] +
               [(IDX_ENUM, e >> 8) for e, _ in ENUMS] +
               [(IDX_CLIENTSCRIPT, s) for s in SCRIPTS] +
               [(IDX_STRUCT, s >> 5) for s in STRUCTS])
    seen = set()
    for index, group in targets:
        if (index, group) in seen:
            continue
        seen.add((index, group))
        files = try_archive(index, group)
        if files is None or isinstance(files, Exception):
            print("%d/%-6d ABSENT -" % (index, group))
            continue
        blob = b"".join(files[k] for k in sorted(files))
        print("%d/%-6d %d %s" % (index, group, len(files), hashlib.sha256(blob).hexdigest()[:16]))


CMDS = {"exists": cmd_exists, "enum": cmd_enum, "params": cmd_params,
        "iface": cmd_iface, "probe": cmd_probe, "hashes": cmd_hashes}

if __name__ == "__main__":
    if len(sys.argv) < 2 or sys.argv[1] not in CMDS:
        sys.exit(__doc__)
    CMDS[sys.argv[1]](sys.argv[2:])
