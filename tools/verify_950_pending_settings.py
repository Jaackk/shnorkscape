"""Read-only verification of the settings recovery controls against the selected 950 cache."""
import contextlib, io, json, re, sys
from pathlib import Path
sys.dont_write_bytecode = True
import cache950  # Selects this test cache and disables writes to reference readers.
import inspect_native947_pending_settings as probe
result = io.StringIO()
with contextlib.redirect_stdout(result): probe.main()
actual = json.loads(result.getvalue())
root = Path(__file__).resolve().parents[1]
expected = json.loads((root / 'protocol-analysis/pending-settings-950.json').read_text())
assert actual == expected, '950 settings page/struct/script evidence has changed'
assert [(v['page'], v['slot'], v['setting']) for v in actual['pending']] == [
    (1,10240,41598), (1,10241,41599), (1,10242,6371), (1,15872,6371)]
source = (root / 'Ataraxia950/game/com/rs/game/player/client/Native950PendingSettings.java').read_text()
assert actual['combinedSha256'] in source
assert [tuple(map(int,m)) for m in re.findall(r'\{(\d+), (\d+), (\d+)\}', source)] == [
    (v['index'],v['group'],v['file']) for v in actual['bindings']]
print('PASS: 591 setting structs inspected; four server-acknowledged combat-mode controls; 93 exact cache bindings')
