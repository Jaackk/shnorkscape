#!/usr/bin/env python3
"""Read-only 950 map reader: region lookup, terrain settings and loc (object) placement.

Ported from the 947 project's working implementation (READ ONLY reference):
  Ataraxia947/game/com/rs/game/Region.java          loadRegionMap / loadMapSettings / loadMapObjects
  Ataraxia947/game/com/rs/cache/loaders/ObjectData.java and ObjectData_1.java
  Ataraxia947/game/com/rs/utils/Utils.java          getMapArchiveId

The 947 and 950 caches hold byte-identical map data (verified on Lumbridge: same six files, same
sizes, same headers), so the 947 format applies unchanged to 950.

Validated over the whole 950 index 5: 5173 populated regions decode with the loc buffer consumed
EXACTLY and the settings buffer parsing all 16384 tiles, with zero failures and zero exceptions.
The other 3502 groups carry no file 0/3.

  map950.py region <regionX> <regionY>    summarise one region
  map950.py tile <absX> <absY> [plane]    settings byte and locs on one absolute tile
  map950.py validate                      re-run the whole-index decode check
"""
import glob
import os
import sys
from pathlib import Path

sys.dont_write_bytecode = True
os.environ.setdefault("PYTHONDONTWRITEBYTECODE", "1")

CACHE = os.environ.get("CACHE950_PATH", str(Path(__file__).resolve().parents[1] / "cache"))
os.environ["OPENNXT_CACHE_PATH"] = CACHE
sys.path.insert(0, os.environ.get("NXT_REFERENCE_TOOLS", str(Path(__file__).resolve().parents[2] / "AstraNXT/OpenNXT/tools")))

from inspect_native947_interface_slots import archive  # noqa: E402

MAP_INDEX = 5
FILE_LOCS = 0
FILE_SETTINGS = 3
JAGX = 0x6A616778


def map_group(region_x, region_y):
    """Utils.getMapArchiveId. NOT (regionX << 8) | regionY - that is a different region."""
    return region_x | (region_y << 7)


class Stream:
    def __init__(self, d):
        self.d, self.o = d, 0

    def u8(self):
        v = self.d[self.o]; self.o += 1; return v

    def u16(self):
        v = int.from_bytes(self.d[self.o:self.o + 2], "big"); self.o += 2; return v

    def i16(self):
        v = int.from_bytes(self.d[self.o:self.o + 2], "big", signed=True); self.o += 2; return v

    def usmart(self):
        return (self.u16() - 32768) if self.d[self.o] >= 128 else self.u8()

    def smart2(self):
        total = 0
        v = self.usmart()
        while v == 32767:
            total += 32767
            v = self.usmart()
        return total + v

    def left(self):
        return len(self.d) - self.o


def _read_extended(s):
    """ObjectData_1.method5473 - optional per-loc transform block, present when type byte has 0x80.

    Omitting this desynchronises the whole loc stream; it is why a naive port decodes only a
    fraction of the regions.
    """
    f = s.u8()
    if f & 0x01:
        for _ in range(4):
            s.i16()          # quaternion
    for bit in (0x02, 0x04, 0x08):
        if f & bit:
            s.i16()          # translation
    if f & 0x10:
        s.i16()              # uniform scale
    else:
        for bit in (0x20, 0x40, 0x80):
            if f & bit:
                s.i16()      # per-axis scale


def decode_locs(data, settings=None):
    """Region.loadMapObjects. Returns [(id, localX, localY, plane, shape, rotation)].

    When `settings` is supplied the bridge rule is applied: a loc on a tile whose plane-1 setting
    has bit 1 set is lowered one plane, matching the client.
    """
    s = Stream(data)
    out = []
    obj = -1
    while True:
        incr = s.smart2()
        if incr == 0:
            break
        obj += incr
        loc = 0
        while True:
            incr2 = s.usmart()
            if incr2 == 0:
                break
            loc += incr2 - 1
            lx, ly, plane = (loc >> 6) & 0x3F, loc & 0x3F, loc >> 12
            b = s.u8()
            shape, rot = (b >> 2) & 0x1F, b & 3
            if b & 0x80:
                _read_extended(s)
            if not (0 <= lx < 64 and 0 <= ly < 64):
                continue
            eff = plane
            if settings is not None and (settings[1][lx][ly] & 2) == 2:
                eff -= 1
            if 0 <= eff < 4:
                out.append((obj, lx, ly, eff, shape, rot))
    if s.left() != 0:
        raise ValueError("trailing loc bytes: %d" % s.left())
    return out


def decode_settings(data):
    """Region.loadMapSettings, modern jagx path. Returns settings[plane][x][y].

    Bit 0 of a settings byte marks the tile blocked; bit 1 on plane 1 marks a bridge.
    """
    s = Stream(data)
    magic = int.from_bytes(s.d[0:4], "big"); s.o = 4
    ver = s.u8()
    if magic != JAGX or ver != 1:
        raise ValueError("unsupported map settings header %08x/%d" % (magic, ver))
    settings = [[[0] * 64 for _ in range(64)] for _ in range(4)]
    for plane in range(4):
        for x in range(64):
            for y in range(64):
                v = s.u8()
                if v & ~15:
                    raise ValueError("unsupported tile flags %d at %d" % (v, s.o - 1))
                if v & 0x1:
                    s.u8(); s.usmart()
                if v & 0x2:
                    settings[plane][x][y] = s.u8()
                if v & 0x4:
                    s.usmart()
                if v & 0x8:
                    s.u16()
    # 16384 tiles are followed by a membership bitmap and then environment/lighting records,
    # which are rendering data and not inputs to collision.
    return settings


def load_region(region_x, region_y):
    gid = map_group(region_x, region_y)
    files = archive(MAP_INDEX, gid)
    raw_settings = files.get(FILE_SETTINGS)
    raw_locs = files.get(FILE_LOCS)
    settings = decode_settings(raw_settings) if raw_settings else None
    locs = decode_locs(raw_locs, settings) if raw_locs else []
    return gid, settings, locs


def cmd_region(a):
    rx, ry = int(a[0]), int(a[1])
    gid, settings, locs = load_region(rx, ry)
    print("region (%d,%d) -> index 5 group %d" % (rx, ry, gid))
    if settings is None:
        print("  no map settings"); return
    blocked = sum(1 for p in range(4) for x in range(64) for y in range(64) if settings[p][x][y] & 1)
    bridge = sum(1 for x in range(64) for y in range(64) if settings[1][x][y] & 2)
    print("  locs: %d   blocked tiles: %d   bridge tiles (plane 1 bit 1): %d" % (len(locs), blocked, bridge))
    from collections import Counter
    shapes = Counter(l[4] for l in locs)
    print("  shapes: %s" % dict(sorted(shapes.items())))


def cmd_tile(a):
    ax, ay = int(a[0]), int(a[1])
    plane = int(a[2]) if len(a) > 2 else 0
    rx, ry = ax >> 6, ay >> 6
    lx, ly = ax & 63, ay & 63
    gid, settings, locs = load_region(rx, ry)
    print("tile (%d,%d,%d) -> region (%d,%d) group %d local (%d,%d)" % (ax, ay, plane, rx, ry, gid, lx, ly))
    if settings:
        print("  settings byte: %d (blocked=%s)" % (settings[plane][lx][ly], bool(settings[plane][lx][ly] & 1)))
    here = [l for l in locs if l[1] == lx and l[2] == ly and l[3] == plane]
    print("  locs on this tile: %s" % (here or "none"))


def cmd_validate(_a):
    ids = sorted(int(os.path.basename(p)[:-4])
                 for p in glob.glob(os.path.join(CACHE, "5", "*.dat")))
    locs_ok = set_ok = absent = 0
    failures = []
    for gid in ids:
        files = archive(MAP_INDEX, gid)
        if FILE_LOCS not in files and FILE_SETTINGS not in files:
            absent += 1
            continue
        try:
            settings = decode_settings(files[FILE_SETTINGS]) if FILE_SETTINGS in files else None
            if settings is not None:
                set_ok += 1
            if FILE_LOCS in files:
                decode_locs(files[FILE_LOCS], settings)
                locs_ok += 1
        except Exception as exc:
            failures.append((gid, str(exc)[:60]))
    print("groups=%d  settings ok=%d  locs exact=%d  absent=%d  failures=%d"
          % (len(ids), set_ok, locs_ok, absent, len(failures)))
    for f in failures[:10]:
        print("   FAIL %s" % (f,))


CMDS = {"region": cmd_region, "tile": cmd_tile, "validate": cmd_validate}

if __name__ == "__main__":
    if len(sys.argv) < 2 or sys.argv[1] not in CMDS:
        sys.exit(__doc__)
    CMDS[sys.argv[1]](sys.argv[2:])
