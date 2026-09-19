"""Read-only comparison of two independently launched automatic-restore sessions."""
import hashlib
import json
import sys
from pathlib import Path
from check_application_gate import source, controls, require, IMAGE, SCHEMA
from compare_snapshots import load, state, matrix_compare


def compare(first, restarted):
    schema, fixture, _ = source()
    captures = [load(p, 'A', schema, SCHEMA) for p in (first, restarted)]
    require(captures[0][0]['pid'] != captures[1][0]['pid'], 'Not separate client processes')
    for path, (data, items) in zip((first, restarted), captures):
        require(data['version'] == 4 and data['imageSha256'] == IMAGE, 'Wrong diagnostic image')
        controls(path, data)
    a, b = [items for data, items in captures]
    custom_ids = {ref[1] for slot in ('6', '7', '12', '13')
                  for row in schema['slots'][slot].values() for ref in row}
    changes = [i for i in schema['ids'] if state(a[i]) != state(b[i])]
    fixture_changes = [[i for i in sorted(custom_ids) if state(items[i]) != state(fixture[i])]
                       for items in (a, b)]
    return {
        'pids': [data['pid'] for data, items in captures],
        'snapshotSha256': [hashlib.sha256(Path(p).read_bytes()).hexdigest()
                           for p in (first, restarted)],
        'entries': len(a), 'changedIdsAcrossRestart': changes,
        'customFixtureMismatchIds': fixture_changes,
        'activeAcrossRestart': matrix_compare(a, b, schema['slots']['8'], schema['slots']['8']),
        'exactRestartReadbackPassed': not changes and not any(fixture_changes),
        'scope': 'Separate processes; account and visible automatic application require user confirmation. '
                 'Does not prove same-process relog, new Save capture, or raw fixture-active equality.'
    }


if __name__ == '__main__':
    if len(sys.argv) != 3:
        raise SystemExit('Usage: check_restart_gate.py first-A.json restarted-A.json')
    result = compare(*sys.argv[1:])
    print(json.dumps(result, indent=2))
    if not result['exactRestartReadbackPassed']:
        raise SystemExit(1)
