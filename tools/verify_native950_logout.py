"""Verify the zero-payload950 logout descriptor and native dispatch; no client/server mutations."""
import argparse,hashlib,json,struct
from pathlib import Path
import capstone,pefile
ROOT=Path(__file__).resolve().parents[1]
EXPECTED="fc7492548627a4068e88a7a45ba4f0be93004adf246bb4b174b24ae588768b36"
RANGES={
 "registrar202":(0x14000e930,0x14000e948),
 "bind202":(0x1400fdba2,0x1400fdbd8),
 "dispatch202":(0x1401052f0,0x1401052f9),
 "parser202":(0x1400fe2b0,0x1400fe4c9),
 "stateSetter":(0x140026b00,0x140026b66),
 "parser73":(0x140109100,0x1401091de)}
def main():
 ap=argparse.ArgumentParser(description=__doc__);ap.add_argument("--output",type=Path);a=ap.parse_args()
 path=ROOT/"OpenNXT/data/clients/950/win64/original/rs2client.exe"
 raw=path.read_bytes();actual=hashlib.sha256(raw).hexdigest();assert actual==EXPECTED
 pe=pefile.PE(data=raw);base=pe.OPTIONAL_HEADER.ImageBase
 cs=capstone.Cs(capstone.CS_ARCH_X86,capstone.CS_MODE_64)
 blocks={};programs={}
 for name,(start,end)in RANGES.items():
  data=pe.get_data(start-base,end-start)
  ins=list(cs.disasm_lite(data,start));programs[name]=ins
  blocks[name]={"start":hex(start),"endExclusive":hex(end),"sha256":hashlib.sha256(data).hexdigest(),
                "instructions":[f"{hex(addr)} {mn} {op}"for addr,size,mn,op in ins]}
 byaddr={addr:(mn,op)for block in programs.values()for addr,size,mn,op in block}
 assert byaddr[0x14000e934]==("xor","r8d, r8d") # wire size0
 assert byaddr[0x14000e93e]==("mov","edx, 0xca") # opcode202
 assert byaddr[0x14000e937]==("lea","rcx, [rip + 0xe8a4d2]")
 assert 0x14000e93e+0xe8a4d2==0x140e98e10
 assert byaddr[0x1400fdba2]==("lea","rax, [rip + 0xa5e5cf]")
 assert 0x1400fdba9+0xa5e5cf==0x140b5c178
 assert byaddr[0x1400fdbb7]==("lea","rdx, [rip + 0xd9b262]")
 assert 0x1400fdbbe+0xd9b262==0x140e98e20
 assert struct.unpack("<Q",pe.get_data(0xb5c178+16,8))[0]==0x1401052f0
 assert byaddr[0x1401052f0]==("add","rcx, 8")
 assert byaddr[0x1401052f4]==("jmp","0x1400fe2b0")
 assert byaddr[0x1400fe35a]==("mov","edx, 0xa")
 assert byaddr[0x1400fe35f]==("call","0x140026b00")
 assert byaddr[0x1400fe37d]==("xor","edx, edx")
 assert byaddr[0x1400fe37f]==("call","0x140026b00")
 assert byaddr[0x140026b55]==("mov","dword ptr [rdi + 0x19fa0], esi")
 assert byaddr[0x140109119]==("movzx","r8d, byte ptr [rax + r8]")
 report={"revision":950,"client":str(path),"clientSha256":actual,"opcode":202,"size":0,
  "descriptor":"0x140e98e10","handlerBinding":"0x1400fdbb7","vtable":"0x140b5c178",
  "vtableDispatchOffset":16,"dispatch":"0x1401052f0","parser":"0x1400fe2b0","ranges":blocks,
  "semanticEvidence":[
   "Registrar sets opcode202 and size0; the descriptor is bound to a vtable whose dispatch adjusts this+8 and jumps to the no-payload parser.",
   "Parser replaces incoming RDX with client state at0x1400fe2c7 before any use as packet data; no payload is consumed.",
   "0x1400fe2de..0x1400fe33e retires connection queues and resets login-attempt fields.",
   "0x1400fe35a and0x1400fe37d set state10 then0 through the proven state setter at0x140026b00.",
   "0x1400fe387..0x1400fe3eb clear additional client connection/world/input state.",
   "0x1400fe434 invokes login-context cleanup with flags0 and an empty string.",
   "Opcode73 instead consumes one plain unsigned reason byte, saves it in client state, and performs the state10 subset."],
  "limits":["Native semantic interpretation supports full logout, not exit-to-lobby or process quit.",
            "A live client must confirm visible return to login; flush completion must precede server channel close.",
            "The login-context cleanup callee is not independently proven to clear a username; no such narrower claim is made."]}
 if a.output:a.output.write_text(json.dumps(report,indent=2)+"\n",encoding="utf-8")
 print("PASS: paired950 opcode202 is a bound zero-payload full-session reset; distinct reason-byte opcode73 not used.")
if __name__=="__main__":main()
