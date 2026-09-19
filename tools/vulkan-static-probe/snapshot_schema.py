"""Generate only the pinned native workspace matrix/read schema (no client writes)."""
import bz2
import gzip
import hashlib
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
HERE = Path(__file__).resolve().parent
PINS = {
    'cache/2/69.dat': 'a99230ac94b61142e85488f7ef7a8149bb8909665e5ac073abe881ce53783486',
    'cache/255/2.dat': '7fe5f5282da440c513ba1167f53a636d1709c08909db773ac8002c0d3ccab73a',
    'cache/12/8709.dat': 'cbad0add67edcae1a02451a6bd79cdb4172b642951ce157660f1e709b9c444b7',
    'cache/12/8754.dat': 'c67a13b98db934c42b9d1072497d7284b52bc8499869193f9493f7c05bc5275f',
    'cache/12/19719.dat': 'a317173783487be81a473a6dd9405ca068110ea06fbdb674e0d12df4a851b3f7',
    'protocol-analysis/ui-scripts-950-evidence.json': 'c218c696d1822a0477ea38d3f6ff2234314fe20a6133cc58e65cdd7dd9cd1406',
    'Ataraxia950/resources/native950/workspace-integer-descriptor-950.properties': 'd87b6b742f3f780153b7086f62c77c9a65faa6df9d10a0410a54883a246b7fe2',
}

def require(ok, message):
    if not ok:
        raise ValueError(message)

def pinned(name):
    data = (ROOT / name).read_bytes()
    require(hashlib.sha256(data).hexdigest() == PINS[name], 'Evidence hash mismatch: ' + name)
    return data

class Reader:
    def __init__(self, data):
        self.data, self.p = data, 0
    def take(self, n):
        require(0 <= n <= len(self.data)-self.p, 'Truncated evidence')
        b = self.data[self.p:self.p+n]
        self.p += n
        return b
    def num(self, n, signed=False):
        return int.from_bytes(self.take(n), 'big', signed=signed)
    def smart(self):
        require(self.p < len(self.data), 'Missing smart')
        return self.num(4) & 0x7fffffff if self.data[self.p] & 128 else self.num(2)

def container(data):
    r = Reader(data)
    kind, n = r.num(1), r.num(4)
    size = n if kind == 0 else r.num(4)
    payload = r.take(n)
    require(kind in (0, 1, 2), 'Unexpected compression')
    out = payload if kind == 0 else bz2.decompress(b'BZh1'+payload) if kind == 1 else gzip.decompress(payload)
    require(len(out) == size, 'Container size mismatch')
    return out

def varbits():
    # Same ordering as OpenNXT ReferenceTable.decode and Archive.decode.
    r = Reader(container(pinned('cache/255/2.dat')))
    version = r.num(1)
    require(version in (5, 6, 7), 'Reference format')
    if version >= 6:
        r.take(4)
    mask = r.num(1)
    require(mask <= 15, 'Reference flags')
    number = r.smart if version >= 7 else lambda: r.num(2)
    count = number()
    ids, last = [], 0
    for _ in range(count):
        last += number()
        ids.append(last)
    r.take(count * (4 + (4 if mask & 1 else 0) + (4 if mask & 8 else 0) +
                    (64 if mask & 2 else 0) + (8 if mask & 4 else 0) + 4))
    counts = [number() for _ in ids]
    file_ids = {}
    for group, n in zip(ids, counts):
        entries, last = [], 0
        for _ in range(n):
            last += number()
            entries.append(last)
        file_ids[group] = entries
    data = container(pinned('cache/2/69.dat'))
    entries = file_ids[69]
    chunks = data[-1]
    tail = len(data)-1-chunks*len(entries)*4
    require(chunks > 0 and tail > 0, 'Invalid group chunks')
    table, pos = Reader(data[tail:-1]), 0
    files = {i: bytearray() for i in entries}
    for _ in range(chunks):
        size = 0
        for i in entries:
            size += table.num(4, True)
            require(size >= 0 and pos+size <= tail, 'Invalid chunk size')
            files[i].extend(data[pos:pos+size])
            pos += size
    require(pos == tail, 'Archive split mismatch')
    return files

def script(n, inverse):
    data = container(pinned(f'cache/12/{n}.dat'))
    end = len(data)-int.from_bytes(data[-2:], 'big')-18
    r = Reader(data)
    while r.num(1):
        pass
    ins = []
    four = {0x35e,0x592,0x713,0x647,0x412,0xab,0x454,0x73d,0x3f2,0x25a,0x717,0x895,0x267,0x51a,0x56,0x96,0x639,0x1ca,0x195,0x30,0xa2}
    while r.p < end:
        op = inverse[r.num(2)]
        if op == 0x511:
            kind = r.num(1)
            require(kind in (0,1,2), 'Push type')
            if kind == 2:
                arg = bytearray()
                while (v := r.num(1)):
                    arg.append(v)
            else:
                arg = r.num(8 if kind == 1 else 4, True)
        else:
            arg = r.num(4 if op in four else 1, op in four)
        ins.append((op, arg))
    require(r.p == end, 'Script boundary')
    require(r.num(4) == len(ins), 'Instruction count')
    r.take(12)
    switches = []
    for _ in range(r.num(1)):
        switches.append({r.num(4, True): r.num(4, True) for _ in range(r.num(2))})
    require(r.p == len(data)-2, 'Switch boundary')
    return ins, switches

def generate():
    for name in PINS:
        pinned(name)
    props = dict(line.split('=',1) for line in pinned(next(p for p in PINS if p.endswith('.properties'))).decode().splitlines() if '=' in line and not line.startswith('#'))
    allowed = set(map(int, props['ids'].split(',')))
    inverse = {int(v['opcode950'],16): int(k,16) for k,v in json.loads(pinned('protocol-analysis/ui-scripts-950-evidence.json'))['opcodeMap947to950'].items()}
    ins, switches = script(8709, inverse)
    files = varbits()
    slots, parents = {}, set()
    require(ins[50] == (0x51a, 0), 'Root slot switch changed')
    for slot in (6,7,12,13,8):
        start = 51+switches[0][slot]
        require(ins[start] == (0x35e,1) and ins[start+1][0] == 0x51a, 'Actor switch changed')
        rows, slot_parents = {}, set()
        for actor, jump in sorted(switches[ins[start+1][1]].items()):
            at = start+2+jump
            require(ins[at:at+8] == [(0x35e,i) for i in range(2,10)], 'Field arguments changed')
            refs = ins[at+8:at+16]
            require(all(op == 0xa2 for op,arg in refs), 'Non-varbit row')
            fields = []
            for op,arg in reversed(refs):
                bit = (arg >> 8) & 65535
                raw = bytes(files[bit])
                require(hashlib.sha256(raw).hexdigest() == props[f'varbit.{bit}.sha256'], 'Varbit pin mismatch')
                r = Reader(raw)
                require(r.num(1) == 1 and r.num(1) == 2, 'Not a domain-2 parent')
                parent = r.smart()
                require(r.num(1) == 2, 'Missing bit range')
                lo, hi = r.num(1), r.num(1)
                require(0 <= lo <= hi < 32 and r.num(1) == 0 and r.p == len(raw), 'Unexpected varbit definition')
                require(parent in allowed and parent == int(props[f'varbit.{bit}.parent']), 'Parent outside descriptor')
                fields.append([bit,parent,lo,hi])
                slot_parents.add(parent)
            rows[str(actor)] = fields
        require(len(rows) == 90 and len(slot_parents) == 180, 'Expected 90 actors/180 parents')
        slots[str(slot)] = rows
        parents.update(slot_parents)
    # These typed script references are separate evidence, not an expansion of the 1694-ID descriptor.
    metadata = [8372,8373,8374,8269,8272,8275,8278]
    save,_ = script(8754,inverse)
    tokens,_ = script(19719,inverse)
    require(set(metadata[:3]) <= {(arg>>8)&65535 for op,arg in save if op in (0x1ca,0x195) and arg>>24 == 2}, 'Save metadata evidence')
    for i, (op,arg) in enumerate(tokens):
        if op == 0x195:
            require(arg>>24 == 2 and ((arg>>8)&65535) in metadata[3:] and tokens[i-1] == (0x35e,1), 'Token is not an evidenced integer')
    require({(arg>>8)&65535 for op,arg in tokens if op == 0x195} == set(metadata[3:]), 'Token coverage')
    return dict(version=3, pins=PINS, ids=sorted(parents|set(metadata)), descriptorIds=sorted(parents), metadataIds=metadata, slots=slots)

def encoded(schema):
    return (json.dumps(schema,sort_keys=True,separators=(',',':'))+'\n').encode()

if __name__ == '__main__':
    data = encoded(generate())
    schema = json.loads(data)
    (HERE/'snapshot-schema.json').write_bytes(data)
    (HERE/'snapshot-schema.h').write_text('// Generated by snapshot_schema.py; do not edit.\n#pragma once\nstatic const char* schemaHash = "'+hashlib.sha256(data).hexdigest()+'";\nstatic const int workspaceIds[] = {'+','.join(map(str,schema['ids']))+'};\n',encoding='ascii')
    print(f"Generated {len(schema['ids'])} bounded IDs; schema SHA256 {hashlib.sha256(data).hexdigest()}")
