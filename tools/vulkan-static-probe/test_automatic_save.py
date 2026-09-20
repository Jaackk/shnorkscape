import hashlib
import json
import struct
import tempfile
import unittest
from pathlib import Path
import check_automatic_save as gate


class AutomaticSaveTests(unittest.TestCase):
    def test_record_requires_explicit_matching_account(self):
        schema = json.loads((gate.HERE/'snapshot-schema-v4.json').read_bytes())
        def utf(text):
            raw = text.encode('ascii')
            return struct.pack('>H', len(raw)) + raw
        body = struct.pack('>III', 0x57533935, 1, 950)
        body += utf(gate.SCHEMA) + utf(gate.IMAGE) + utf('jaxa')
        body += struct.pack('>qH', 1, 912)
        body += b''.join(struct.pack('>HB', i, 0) for i in schema['ids'])
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory)/'record'
            path.write_bytes(body + hashlib.sha256(body).digest())
            revision, values, image = gate.record(path, schema, 'jaxa')
            self.assertEqual(revision, 1)
            self.assertEqual(len(values), 912)
            self.assertEqual(image, gate.IMAGE)
            with self.assertRaises(ValueError):
                gate.record(path, schema, 'layoutgate2')
        with self.assertRaises(ValueError):
            gate.compare('unused', 'unused', 'unreviewed')

    def test_fence_is_domain2_integer_and_save_precedes_native_exit(self):
        import snapshot_schema as s
        from unittest.mock import patch
        pins = {'cache/12/2462.dat': '497be2e0b34f935822f0677169ced05043dfb5c4dae2814111b5d44ab8bec0dd',
                'cache/12/2464.dat': 'febe8a855aa78960a6deba4b4885aa2be89c07e2c3f7fb876a1cd27ac057cce5'}
        inverse = {int(v['opcode950'],16): int(k,16) for k,v in
                   json.loads(s.pinned('protocol-analysis/ui-scripts-950-evidence.json'))['opcodeMap947to950'].items()}
        with patch.dict(s.PINS, pins):
            entry, _ = s.script(2462, inverse)
            exit_, _ = s.script(2464, inverse)
            save, _ = s.script(8754, inverse)
        packed = ((2 << 16) | 3477) << 8
        self.assertEqual(entry[8:10], [(0x511, 1), (0x195, packed)])
        self.assertEqual(exit_[11:13], [(0x511, 0), (0x195, packed)])
        self.assertEqual(save[6], (0x895, 8702))
        self.assertEqual(save[9], (0x895, 8702))
        self.assertEqual(save[12], (0x895, 19719))
        self.assertEqual(save[14], (0x195, ((2 << 16) | 8372) << 8))
        self.assertEqual(save[15], (0x895, 2464))

    def test_existing_seed_is_readable_without_fabricating_an_automatic_generation(self):
        path = gate.ROOT/'workspace-state950'/(hashlib.sha256(b'layoutgate2').hexdigest()+'.workspace950')
        if not path.exists():
            self.skipTest('Private local disposable sidecar')
        schema = json.loads((gate.HERE/'snapshot-schema-v4.json').read_bytes())
        revision, values, image = gate.record(path, schema)
        self.assertGreater(revision, 0)
        self.assertEqual(len(values), 912)
        self.assertIn(image, (gate.IMAGE, gate.OLD_IMAGE))

    def test_checksum_and_version_are_not_ignored(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory)/'record'
            for raw in (b'', b'0'*32, struct.pack('>III',1,1,950)):
                path.write_bytes(raw+hashlib.sha256(raw).digest())
                with self.assertRaises((ValueError, struct.error)):
                    gate.record(path, {'ids':[]})
