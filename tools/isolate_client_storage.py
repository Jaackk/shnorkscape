"""Create a950-only client whose Windows storage roots stay in this test folder.
Requires the OpenNXT RSA-patched WIN64 950-1 client. Does not edit its input.
All native offsets guarded against the verified downloaded build.
"""
from pathlib import Path
import hashlib,struct,json,zlib
root=Path(__file__).resolve().parents[1]
base=root/'OpenNXT/data/clients/950/win64'
original=(base/'original/rs2client.exe').read_bytes()
assert hashlib.sha256(original).hexdigest()=='fc7492548627a4068e88a7a45ba4f0be93004adf246bb4b174b24ae588768b36','Unexpected950client; re-audit native offsets'
raw=bytearray((base/'patched/rs2client.exe').read_bytes())
assert len(raw)==len(original) and raw!=original,'Run OpenNXT RSA patcher first'
state=(root/'client-state').resolve()
state.mkdir(exist_ok=True)
code=bytes.fromhex('488b442428488d151c00000031c9440fb7044a6644890448ffc1664585c075ee31c0c3')
assert len(code)==35
payload=code+b'\0'*5+str(state).encode('utf-16le')+b'\0\0'
assert len(payload)<=0x1c0,'Test path exceeds verified code padding'
assert raw[0x7f4040:0x7f4200]==b'\0'*0x1c0,'Code padding changed'
assert struct.unpack_from('<I',raw,0x228)[0]==0x7f3c40,'Unexpected .text virtual size'
patches=[(0x6dbb8b,'ff159f911100'),(0x6dbcf9,'ff1531901100'),(0x6dbe2d,'ff15fd8e1100'),(0x7962a0,'ff158aea0500')]
for offset,expected in patches:
 assert raw[offset:offset+6]==bytes.fromhex(expected),f'Call signature changed at{offset:x}'
 rva=offset+0xc00
 raw[offset:offset+6]=b'\xe8'+struct.pack('<i',0x7f4c40-(rva+5))+b'\x90'
raw[0x7f4040:0x7f4040+len(payload)]=payload
struct.pack_into('<I',raw,0x228,0x7f3c40+len(payload))
out=root/'client/rs2client.exe'
out.parent.mkdir(exist_ok=True)
out.write_bytes(raw)
report={'source_version':'950-1','original_sha256':hashlib.sha256(original).hexdigest(),'original_crc32':zlib.crc32(original),'isolated_sha256':hashlib.sha256(raw).hexdigest(),'storage_root':str(state),'redirected_calls':[hex(x[0]) for x in patches],'stub_rva':'0x7f4c40','payload_length':len(payload)}
(root/'logs/client-isolation.json').write_text(json.dumps(report,indent=2))
print(json.dumps(report,indent=2))
