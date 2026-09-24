"""Bounded generator checks; no writes to the installed cache."""
import json, tempfile, unittest
from pathlib import Path
from build_gameval_lookup_950 import build
ROOT=Path(__file__).resolve().parents[1]
class Generator(unittest.TestCase):
    def test_deterministic_and_exact_subset(self):
        audit=json.loads((ROOT/'protocol-analysis/gameval-investigation-950.json').read_bytes())
        rows=build(ROOT/'temp/gameval-research',ROOT/'cache',audit)
        self.assertEqual(rows,build(ROOT/'temp/gameval-research',ROOT/'cache',audit))
        self.assertEqual(1385,len(rows))
        self.assertFalse(any(r[1].startswith('1477:') for r in rows))
        self.assertTrue(any(r[2]=='machinima_skybox_filter:skybox_tab' for r in rows))
    def test_changed_target_is_rejected(self):
        audit=json.loads((ROOT/'protocol-analysis/gameval-investigation-950.json').read_bytes())
        with tempfile.TemporaryDirectory() as temp:
            root=Path(temp);(root/'3').mkdir();(root/'3/91.dat').write_bytes(b'changed')
            with self.assertRaises(Exception):build(ROOT/'temp/gameval-research',root,audit)
    def test_changed_name_source_is_rejected(self):
        audit=json.loads((ROOT/'protocol-analysis/gameval-investigation-950.json').read_bytes())
        audit['rawDecoderChecks'][0]['rawSha256']='0'*64
        with self.assertRaisesRegex(ValueError,'name source changed'):build(ROOT/'temp/gameval-research',ROOT/'cache',audit)
if __name__=='__main__':unittest.main()
