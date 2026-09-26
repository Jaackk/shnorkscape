"""Run CheckOnly against disposable fixture files; never install or touch live runtime."""
import json, os, shutil, subprocess, tempfile, unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]

class Installer(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        temp_root=ROOT/'temp'
        temp_root.mkdir(exist_ok=True)
        cls.base=Path(tempfile.mkdtemp(prefix='dev-installer-',dir=temp_root)).resolve()
        assert cls.base.is_relative_to((ROOT/'temp').resolve())
        cls.manifest=json.loads((ROOT/'protocol-analysis/playability-candidate-20260923.json').read_bytes())
        candidate=ROOT/'dist'/cls.manifest['candidate']
        if not candidate.is_dir():
            raise unittest.SkipTest(
                'requires the generated staged candidate under dist; source-only clones do not include staged binaries'
            )
        for name in ('Apply-PlayabilityUpdate.ps1','Start-950Server.ps1','Test-Bundle.ps1','Prepare-ClientCache.ps1'):
            shutil.copyfile(ROOT/name,cls.base/name)
        for entry in cls.manifest['files']:
            src=Path('dist')/cls.manifest['candidate']/entry['source'];dst=cls.base/src;dst.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(ROOT/src,dst)
            if entry.get('beforeSha256')!='ABSENT':
                dst=cls.base/entry['target'];dst.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(ROOT/entry['target'],dst)
        (cls.base/'protocol-analysis').mkdir()

    def setUp(self):
        (self.base/'protocol-analysis/playability-candidate-20260923.json').write_text(json.dumps(self.manifest))

    def check(self,expected):
        shell=Path(os.environ['SystemRoot'])/'System32/WindowsPowerShell/v1.0/powershell.exe'
        result=subprocess.run([str(shell),'-NoProfile','-ExecutionPolicy','Bypass','-File',str(self.base/'Apply-PlayabilityUpdate.ps1'),'-CheckOnly'],capture_output=True,text=True)
        self.assertEqual(expected,result.returncode==0,result.stdout+result.stderr)

    def test_base_idempotence_and_corrupt_script_rejected(self):
        self.check(True)
        entry=next(e for e in self.manifest['files'] if e['target']=='cache/12/21124.dat');dst=self.base/entry['target']
        before=dst.read_bytes() if dst.exists() else None
        shutil.copyfile(self.base/'dist'/self.manifest['candidate']/entry['source'],dst)
        try:
            self.check(True);dst.write_bytes(b'corrupt new archive');self.check(False)
        finally:
            if before is None:dst.unlink()
            else:dst.write_bytes(before)

    def test_new_marker_helper_is_idempotent_but_cannot_replace_unknown_file(self):
        entry=next(e for e in self.manifest['files'] if e['target']=='cache/12/21130.dat')
        dst=self.base/entry['target'];before=dst.read_bytes() if dst.exists() else None
        self.check(True)
        shutil.copyfile(self.base/'dist'/self.manifest['candidate']/entry['source'],dst)
        try:
            self.check(True);dst.write_bytes(b'unrelated existing script');self.check(False)
        finally:
            if before is None:dst.unlink()
            else:dst.write_bytes(before)

    def test_unknown_target_is_rejected(self):
        m=json.loads(json.dumps(self.manifest));m['files'][-1]['target']='cache/12/999999.dat'
        (self.base/'protocol-analysis/playability-candidate-20260923.json').write_text(json.dumps(m));self.check(False)

    def test_new_primitive_is_absent_or_exact_candidate_never_an_unknown_existing_script(self):
        entries=[e for e in self.manifest['files'] if e['target'].startswith('cache/12/211') and e['beforeSha256']=='ABSENT']
        if not entries:self.skipTest('Candidate predates V2 primitives')
        entry=entries[0];self.assertEqual('ABSENT',entry['beforeSha256']);dst=self.base/entry['target']
        self.check(True)
        shutil.copyfile(self.base/'dist'/self.manifest['candidate']/entry['source'],dst)
        try:
            self.check(True);dst.write_bytes(b'unrelated definition');self.check(False)
        finally:dst.unlink()

    def test_missing_existing_target_is_rejected(self):
        entry=next(e for e in self.manifest['files'] if e['target']=='cache/12/5591.dat');dst=self.base/entry['target'];raw=dst.read_bytes();dst.unlink()
        try:self.check(False)
        finally:dst.write_bytes(raw)

    def test_launch_gate_must_accept_new_reference(self):
        path=self.base/'Start-950Server.ps1';raw=path.read_bytes();ref=next(e['sha256'] for e in self.manifest['files'] if e['target']=='cache/255/12.dat')
        path.write_bytes(raw.replace(ref.encode(),b'0'*64))
        try:self.check(False)
        finally:path.write_bytes(raw)

if __name__=='__main__':unittest.main()
