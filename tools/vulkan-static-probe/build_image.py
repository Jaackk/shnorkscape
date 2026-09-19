"""Build a separately named, hash-pinned diagnostic image; never edit production."""
import argparse
import hashlib
import json
import struct
from pathlib import Path
import pefile

INPUT_HASH = '36c45c1cf6eed0c6cb0b789ca1672d685c1746def9d65d6f18d72638f0087bc9'
SPANS = [
    (0x1c830,0x8b1,'6c051fb5fb9cb2c2b1fc9a38c34ac9ae62a25bfbb5396047c14defd77f0fc4a6'),
    (0x1b070,0x1ab,'e07682c0fa905789eb7a9867be0a73d823a7ed2aaf5312b80c7014f27131991b'),
    (0x21bc0,0x2412,'abf6cdbedc671e86acd0dddd2ae0a8a24f7b79b35f31657ddf8dc8260e0ea01d'),
    (0x1471e0,0x5dc,'b0318cf9af6e400158fd23f9ae8329b9a7b305b429d8b0adebcb40fdc086245e'),
    (0x147c40,0x94,'ffd0f52280cbe972b26b619d94611e6f4509573873b7af225c7a95907986c8c7'),
    (0x25550,0x1c4,'4da9e9691d5aac059f9c1c61b17545450daf077267dc9c111707a6f4c7fc74ae'),
]
def sha(data): return hashlib.sha256(data).hexdigest()
def align(n,a): return (n+a-1)//a*a
def require(ok,msg):
    if not ok: raise ValueError(msg)

def thunk(rva,iat):
    c=bytearray.fromhex('4881ecd8000000 48894c2420')
    c += b'\xe8'+struct.pack('<i',0x25550-(rva+len(c)+5))
    # Preserve post-original volatile registers, flags and floating-point state.
    stores=['4889442428','48894c2430','4889542438','4c89442440','4c894c2448','4c89542450','4c895c2458']
    loads =['488b442428','488b4c2430','488b542438','4c8b442440','4c8b4c2448','4c8b542450','4c8b5c2458']
    for h in stores:c+=bytes.fromhex(h)
    for i in range(6):
        off=0x60+i*16
        c+=b'\xf3\x0f\x7f'+bytes([0x84+i*8,0x24])+struct.pack('<I',off)
    c+=bytes.fromhex('9c58 48898424c0000000 0fae9c24c8000000 488b4c2420')
    c+=b'\xff\x15'+struct.pack('<i',iat-(rva+len(c)+6))
    c+=bytes.fromhex('0fae9424c8000000')
    for i in range(6):
        c+=b'\xf3\x0f\x6f'+bytes([0x84+i*8,0x24])+struct.pack('<I',0x60+i*16)
    for h in loads:c+=bytes.fromhex(h)
    c+=bytes.fromhex('ffb424c0000000 9d 488da424d8000000 c3')
    return bytes(c)

def build(raw):
    require(sha(raw)==INPUT_HASH,'Unapproved input image')
    p=pefile.PE(data=raw)
    for at,n,h in SPANS:require(sha(p.get_data(at,n))==h,f'Signature mismatch {at:x}')
    require(p.OPTIONAL_HEADER.ImageBase==0x140000000,'Unexpected image base')
    require(p.FILE_HEADER.NumberOfSections==7,'Unexpected sections')
    require(struct.unpack('<Q',p.get_data(0xc61ce0,8))[0]==0x140025550,'Unexpected vtable target')
    require(any(e.rva==0xc61ce0 and e.type==10 for b in p.DIRECTORY_ENTRY_BASERELOC for e in b.entries),'Missing vtable relocation')
    require(not p.OPTIONAL_HEADER.DllCharacteristics&0x4000,'CFG-enabled image requires separate validation')
    section_table=p.sections[0].get_file_offset()
    require(section_table+9*40<=p.OPTIONAL_HEADER.SizeOfHeaders,'No section-header room')
    fa=p.OPTIONAL_HEADER.FileAlignment; sa=p.OPTIONAL_HEADER.SectionAlignment
    code_rva=align(max(s.VirtualAddress+max(s.Misc_VirtualSize,s.SizeOfRawData) for s in p.sections),sa)
    data_rva=code_rva+sa
    data=bytearray()
    def put(blob,a=1):
        data.extend(b'\0'*(align(len(data),a)-len(data)));at=data_rva+len(data);data.extend(blob);return at
    dll=put(b'shnork_workspace_probe.dll\0')
    name=put(b'\0\0WorkspaceProbe\0',2)
    ilt=put(struct.pack('<QQ',name,0),8)
    iat=put(struct.pack('<QQ',name,0),8)
    desc=b''.join(raw[d.struct.get_file_offset():d.struct.get_file_offset()+20] for d in p.DIRECTORY_ENTRY_IMPORT)
    desc+=struct.pack('<IIIII',ilt,0,0,dll,iat)+bytes(20)
    imports=put(desc,4)
    code=thunk(code_rva,iat)
    require(len(code)<sa,'Thunk exceeds code section')
    # UWOP_ALLOC_LARGE: 0xd8 bytes, seven-byte prologue, no nonvolatile changes.
    unwind=put(bytes([1,7,2,0,7,1])+struct.pack('<H',0xd8//8),4)
    old=p.OPTIONAL_HEADER.DATA_DIRECTORY[3]
    table=p.get_data(old.VirtualAddress,old.Size)+struct.pack('<III',code_rva,code_rva+len(code),unwind)
    exception=put(table,4)
    out=bytearray(raw)
    code_file=align(len(out),fa);out.extend(bytes(code_file-len(out)));out.extend(code);out.extend(bytes(align(len(out),fa)-len(out)))
    data_file=len(out);out.extend(data);out.extend(bytes(align(len(out),fa)-len(out)))
    def set16(off,v):struct.pack_into('<H',out,off,v)
    def set32(off,v):struct.pack_into('<I',out,off,v)
    for i,(name_,rva,offset,size,flags) in enumerate([(b'.wcode',code_rva,code_file,len(code),0x60000020),(b'.wdata',data_rva,data_file,len(data),0xc0000040)]):
        hdr=struct.pack('<8sIIIIIIHHI',name_,size,rva,align(size,fa),offset,0,0,0,0,flags)
        out[section_table+(7+i)*40:section_table+(8+i)*40]=hdr
    set16(p.FILE_HEADER.get_field_absolute_offset('NumberOfSections'),9)
    set32(p.OPTIONAL_HEADER.get_field_absolute_offset('SizeOfImage'),align(data_rva+len(data),sa))
    set32(p.OPTIONAL_HEADER.get_field_absolute_offset('SizeOfCode'),p.OPTIONAL_HEADER.SizeOfCode+align(len(code),fa))
    set32(p.OPTIONAL_HEADER.get_field_absolute_offset('SizeOfInitializedData'),p.OPTIONAL_HEADER.SizeOfInitializedData+align(len(data),fa))
    set32(p.OPTIONAL_HEADER.get_field_absolute_offset('CheckSum'),0)
    for idx,addr,size in [(1,imports,len(desc)),(3,exception,len(table))]:
        d=p.OPTIONAL_HEADER.DATA_DIRECTORY[idx];set32(d.get_file_offset(),addr);set32(d.get_file_offset()+4,size)
    struct.pack_into('<Q',out,p.get_offset_from_rva(0xc61ce0),p.OPTIONAL_HEADER.ImageBase+code_rva)
    result=bytes(out);q=pefile.PE(data=result)
    require(len(q.DIRECTORY_ENTRY_IMPORT)==len(p.DIRECTORY_ENTRY_IMPORT)+1,'Import construction failed')
    for at,n,h in SPANS:require(sha(q.get_data(at,n))==h,'Existing code changed')
    return result,dict(input_sha256=INPUT_HASH,output_sha256=sha(result),thunk_rva=code_rva,iat_rva=iat,thunk_size=len(code),unwind_rva=unwind,critical_spans=SPANS)

if __name__=='__main__':
    ap=argparse.ArgumentParser();ap.add_argument('input',type=Path);ap.add_argument('output',type=Path);ap.add_argument('manifest',type=Path);args=ap.parse_args()
    require(args.input.resolve()!=args.output.resolve(),'Never replace production')
    result,manifest=build(args.input.read_bytes())
    require(not args.output.exists(),'Output already exists; do not overwrite')
    args.output.write_bytes(result);args.manifest.write_text(json.dumps(manifest,indent=2)+'\n')
    print(json.dumps(manifest,indent=2))
