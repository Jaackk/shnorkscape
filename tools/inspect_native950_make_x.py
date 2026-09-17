#!/usr/bin/env python3
"""Read-only verification of the native950 Make-X bindings against the supplied flat cache."""
import hashlib
import sys
from pathlib import Path
sys.dont_write_bytecode = True
import cache950 as cache
ROOT = Path(__file__).resolve().parents[1]

def main():
    resource = ROOT / 'Ataraxia950/resources/native950/production-make-x-950.properties'
    count = 0
    for line in resource.read_text(encoding='ascii').splitlines():
        if not line.startswith('pin.'):
            continue
        key, expected = line.split('=', 1)
        index, group, file = map(int, key.split('.')[1:])
        raw = cache.archive(index, group)[file]
        actual = hashlib.sha256(raw).hexdigest()
        if actual != expected:
            raise SystemExit('MISMATCH ' + key)
        count += 1
    central = cache.enum(7716)[1007]
    values = cache.params(central)
    print('PASS:', count, 'Make-X SHA-256 bindings in', cache.CACHE)
    print('Central struct:', central, 'host:', values[3505] >> 16, values[3505] & 65535)
    for root in (6939, 6981, 8403, 8405):
        print('Category root', root, cache.enum(root))
    print('Protocol: frame1370:30 Make(pause101,mask1),1370:32 Close(IF_BUTTON); product1371:22 slot=1+4*ordinal;')
    print('quantity1371:20 slot=count-1; category opener1371:28 -> armed1477:896 ordinal.')

if __name__ == '__main__':
    main()

