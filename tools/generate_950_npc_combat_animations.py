#!/usr/bin/env python3
"""Compare every authored NPC combat sequence against read-only 910/950 caches.

Writes only explicitly requested files. Runtime consumes the generated classpath
resource; neither the 910 cache nor this development tool is required to launch.
SeqType widths are from AnimationDefinitions' documented 950 native decoder.
"""
import argparse
from collections import Counter
from functools import lru_cache
import hashlib
import json
from pathlib import Path
import sys
sys.dont_write_bytecode = True
ROOT = Path(__file__).resolve().parents[1]

class Reader:
    def __init__(self, data): self.data, self.pos = data, 0
    def take(self, n):
        if self.pos + n > len(self.data): raise ValueError("truncated sequence")
        out = self.data[self.pos:self.pos+n]; self.pos += n; return out
    def integer(self, n=1): return int.from_bytes(self.take(n), 'big')
    def smart(self):
        return self.integer(2) - 32768 if self.data[self.pos] >= 128 else self.integer()
    def bigsmart(self):
        return self.integer(4) & 0x7fffffff if self.data[self.pos] >= 128 else self.integer(2)
    def string(self):
        while self.integer() != 0: pass

def sequence(data, modern=True):
    r = Reader(data); ops = {}; duration = 0; frame_count = 0; base_duration = 0
    while True:
        op = r.integer()
        if op == 0: break
        start = r.pos
        if op == 1:
            frame_count = r.integer(2)
            duration = sum(r.integer(2) for _ in range(frame_count))
            r.take(frame_count * 4)
        elif op == 3:
            if modern:
                for _ in range(r.smart()): r.smart()
            else: r.take(r.integer())
        # The paired 910 files (e.g.422/14223) also use unsigned-short
        # 0xffff item sentinels; the legacy Java bigSmart reader is stale.
        elif op in (6, 7): r.take(2)
        elif op in (2, 23, 24, 25): r.take(2)
        elif op in (5, 8, 9, 10, 11, 22, 27): r.take(1)
        elif op in (14, 15, 16, 18) or (op == 4 and not modern): pass
        elif op == 26: r.take(2); base_duration = r.integer(2)
        elif op in (12, 112): r.take(4 * r.integer(1 if op == 12 else 2))
        elif op == 13:
            for _ in range(r.integer(2)):
                count = r.integer()
                if count: r.take(3 + 2 * (count - 1))
        elif op in (19, 119): r.take(2 if op == 19 else 3)
        elif op in (20, 120): r.take(5 if op == 20 else 6)
        elif op == 249:
            for _ in range(r.integer()):
                kind = r.integer(); r.take(3)
                if kind == 1: r.string()
                elif kind == 0: r.take(4)
                else: raise ValueError("unknown parameter kind")
        else: raise ValueError("unknown opcode %d at %d" % (op, start - 1))
        ops[op] = data[start:r.pos].hex()
    if r.pos != len(data): raise ValueError("trailing bytes")
    return dict(opcodes=ops, durationCycles=duration+base_duration, frameCount=frame_count)

def compatibility(old_data, modern_data):
    modern = sequence(modern_data)
    # Definitions with no playback duration are not usable combat actions.
    if modern['durationCycles'] <= 0: return None, modern, 'no-duration'
    if old_data == modern_data: return 'identical-definition', modern, None
    old = sequence(old_data, modern=False)
    a, b = old['opcodes'], modern['opcodes']
    # A missing opcode1 cannot prove identity: modern25/26 uses a different asset
    # binding. Reject that path unless the ENTIRE raw definition matched above.
    if not old['frameCount'] or not modern['frameCount']: return None, modern, 'no-shared-frame-binding'
    if a.get(1) != b.get(1): return None, modern, 'changed-frames-or-durations'
    # These fields select a second frame source / change total duration. Matching
    # opcode1 alone is insufficient when a modern definition introduces either.
    if any(a.get(op) != b.get(op) for op in (12, 112, 25, 26)):
        return None, modern, 'changed-secondary-frame-binding'
    return 'identical-frame-duration-binding', modern, None

def main():
    import verify_950_hitbars as v
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--old-cache', type=Path, default=ROOT.parent/'Ataraxia-PS/data/cache')
    parser.add_argument('--combat-defs', type=Path, default=ROOT/'Ataraxia950/data/npcs/combatDefs.json')
    parser.add_argument('--resource', type=Path, required=True)
    parser.add_argument('--report', type=Path, required=True)
    args = parser.parse_args()
    raw_combat = args.combat_defs.read_bytes()
    records = json.loads(raw_combat.decode('utf-8-sig'))
    uses = {}
    for row in records:
        for key in ('attackAnim', 'defenceAnim', 'deathAnim'):
            seq = row['combatDefinition'].get(key, -1)
            if seq >= 0: uses.setdefault(seq, set()).add(row['npcId'])
    old = v.Packed(args.old_cache)
    old_archive = lru_cache(None)(old.archive)
    rows = []; counts = Counter(); accepted = []
    for seq, npcs in sorted(uses.items()):
        row = dict(id=seq, npcCount=len(npcs))
        try:
            modern = v.cache.archive(20, seq >> 7).get(seq & 127)
            if modern is None: raise KeyError('missing 950 definition')
            old_data = old_archive(20, seq >> 7).get(seq & 127)
            if old_data is None: raise KeyError('missing 910 definition')
            kind, parsed, reason = compatibility(old_data, modern)
            row.update(sha256=hashlib.sha256(modern).hexdigest(), durationCycles=parsed['durationCycles'],
                       compatibility=kind, reason=reason)
            if kind: accepted.append(row)
        except (ValueError, KeyError, AssertionError, IndexError, FileNotFoundError) as error:
            row['reason'] = str(error)
        counts[row.get('compatibility') or row['reason']] += 1
        rows.append(row)
    lines = ['# Generated by tools/generate_950_npc_combat_animations.py; do not edit.',
             '# sequenceId=950SHA256,durationCycles,910Compatibility',
             '# Authored combatDefs SHA256: '+hashlib.sha256(raw_combat).hexdigest(),
             '# Index20 group=id>>>7 file=id&127; durations are 20ms client cycles.']
    lines.extend('%d=%s,%d,%s' % (r['id'], r['sha256'], r['durationCycles'], r['compatibility']) for r in accepted)
    args.resource.parent.mkdir(parents=True, exist_ok=True)
    args.resource.write_text('\n'.join(lines)+'\n', encoding='ascii')
    report = dict(authoredCombatRows=len(records), authoredSequences=len(uses), acceptedSequences=len(accepted),
                  counts=dict(counts), sequences=rows,
                  limitation='Matching config/frame references does not prove identical rendered models or frame assets. Changed/new bindings are omitted; missing -1 animations remain absent.')
    args.report.parent.mkdir(parents=True, exist_ok=True)
    args.report.write_text(json.dumps(report, indent=2)+'\n', encoding='utf-8')
    print(json.dumps({k:report[k] for k in ('authoredCombatRows','authoredSequences','acceptedSequences','counts')}, indent=2))

if __name__ == '__main__': main()
