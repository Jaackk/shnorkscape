import unittest
import struct
from pathlib import Path
import capstone
import pefile
from build_image import build,thunk,INPUT_HASH,sha

SOURCE=Path(r'C:\Games\950OpenSource\client\rs2client-vulkan.exe')
class ImageTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.raw=SOURCE.read_bytes();cls.out,cls.info=build(cls.raw);cls.p=pefile.PE(data=cls.out)
    def test_hash_and_determinism(self):
        self.assertEqual(sha(self.raw),INPUT_HASH);self.assertEqual(build(self.raw),(self.out,self.info))
    def test_wrong_input_rejected(self):
        with self.assertRaisesRegex(ValueError,'Unapproved'):build(self.raw[:-1])
    def test_import_unwind_and_relocation(self):
        d=self.p.DIRECTORY_ENTRY_IMPORT[-1];self.assertEqual(d.dll,b'shnork_workspace_probe.dll');self.assertEqual(d.imports[0].name,b'WorkspaceProbe')
        t=self.info['thunk_rva'];e=self.p.DIRECTORY_ENTRY_EXCEPTION[-1].struct
        self.assertEqual(e.BeginAddress,t);self.assertEqual(e.EndAddress,t+self.info['thunk_size'])
        self.assertEqual(self.p.get_data(e.UnwindData,8),bytes([1,7,2,0,7,1,27,0]))
        self.assertEqual(struct.unpack('<Q',self.p.get_data(0xc61ce0,8))[0],0x140000000+t)
        self.assertTrue(any(e.rva==0xc61ce0 and e.type==10 for b in self.p.DIRECTORY_ENTRY_BASERELOC for e in b.entries))
    def test_wrapper(self):
        m=capstone.Cs(capstone.CS_ARCH_X86,capstone.CS_MODE_64)
        code=self.p.get_data(self.info['thunk_rva'],self.info['thunk_size']);ins=list(m.disasm(code,self.info['thunk_rva']))
        self.assertEqual(sum(i.size for i in ins),len(code));self.assertEqual(ins[-1].mnemonic,'ret')
        calls=[i for i in ins if i.mnemonic=='call'];self.assertEqual(len(calls),2);self.assertEqual(calls[0].op_str,'0x25550')
        self.assertEqual(ins[0].op_str,'rsp, 0xd8')
        self.assertEqual(len([i for i in ins if i.mnemonic=='movdqu']),12)
    def test_no_rwx_and_existing_code_unchanged(self):
        for s in self.p.sections:self.assertFalse(s.Characteristics&0x20000000 and s.Characteristics&0x80000000)
        old=pefile.PE(data=self.raw)
        for s in old.sections:
            if s.Characteristics&0x20000000:self.assertEqual(s.get_data(),self.p.get_data(s.VirtualAddress,s.SizeOfRawData))
if __name__=='__main__':unittest.main()
