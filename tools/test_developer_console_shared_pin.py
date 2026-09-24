"""Verify the installed bridge's original body and both independent consumers' pins."""
import hashlib,re,struct,unittest
from pathlib import Path
from build_library_search_bridge import unpack,encode
from library_trace_read import scope,inverse,ROOT

class SharedPin(unittest.TestCase):
    def test_bridge_preserves_original_and_settings_pin_matches_console(self):
        raw=unpack((ROOT/'cache/12/8286.dat').read_bytes())
        ops,tail=scope['decode'](raw,inverse)
        self.assertEqual(28,len(ops))
        original=[(o,a) for _,_,o,a,_ in ops[:19]]+[(0x495,0)]
        original_raw=b'\0'+b''.join(encode(o,a) for o,a in original)+struct.pack('>I',20)+tail[4:]
        self.assertEqual('5bd6296bb761633d83eb47a86757815ee88d0369ab844439a11f13792f24d5f7',hashlib.sha256(original_raw).hexdigest())
        digest=hashlib.sha256(raw).hexdigest()
        settings=(ROOT/'Ataraxia950/game/com/rs/game/player/client/Native950Settings.java').read_text()
        pinned=re.search(r'pin\(12, 8286, 0, "([a-f0-9]+)"\)',settings).group(1)
        props=dict(line.split('=',1) for line in (ROOT/'Ataraxia950/resources/native950/developer-console-950.properties').read_text().splitlines())
        self.assertEqual(digest,pinned)
        self.assertEqual(digest,props['8286'])

if __name__=='__main__':unittest.main()
