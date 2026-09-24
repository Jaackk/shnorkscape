"""No-launch regression: every cache gate must accept the staged reference hash."""
import hashlib,json,re,unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
FILES=('Start-950Server.ps1','Test-Bundle.ps1','Prepare-ClientCache.ps1')

class LaunchIdentity(unittest.TestCase):
    def test_staged_reference_is_accepted_by_every_launch_gate(self):
        manifest=json.loads((ROOT/'protocol-analysis/playability-candidate-20260923.json').read_bytes())
        entry=next(e for e in manifest['files'] if e['target']=='cache/255/12.dat')
        staged=ROOT/'dist'/manifest['candidate']/entry['source']
        self.assertEqual(entry['sha256'],hashlib.sha256(staged.read_bytes()).hexdigest().upper())
        for name in FILES:
            with self.subTest(name=name):
                script=(ROOT/name).read_text()
                arrays=re.findall(r'@\(([^\r\n]*8A45E12B[^\r\n]*?)\)',script)
                self.assertEqual(1,len(arrays),'Expected explicit paired-cache allowlist')
                hashes=re.findall(r"'([0-9A-F]{64})'",arrays[0])
                self.assertIn(entry['sha256'],hashes)
                self.assertIn('8A45E12B3D5B3BF35CDB02CDEC9DDEDBD46200B4FEF086ADC0679FB0D020EF8C',hashes)
                self.assertNotIn('0'*64,hashes)

if __name__=='__main__':unittest.main()
