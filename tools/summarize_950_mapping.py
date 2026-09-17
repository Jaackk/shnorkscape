"""Consolidate the bounded mapping pass; never promotes or edits live protocol files."""
from pathlib import Path
import json,csv,difflib
import pefile,capstone
root=Path(__file__).resolve().parents[1]; out=root/'protocol-analysis'
builds={r:json.loads((out/f'{r}-native.json').read_text()) for r in ('947','950')}
rows=json.loads((out/'server-candidates.json').read_text()); seq=[]
for r in ('947','950'):
 sizes={p['opcode']:p['size'] for p in builds[r]['tables']['server']['packets']}
 seq.append(sorted(builds[r]['handlers'],key=lambda x:int(x['binding']['instructionVA'],16)))
 for h in seq[-1]:h['size']=sizes[h['opcode']]
match=difflib.SequenceMatcher(None,[h['size'] for h in seq[0]],[h['size'] for h in seq[1]],autojunk=False)
order={}
for block in match.get_matching_blocks():
 for i in range(block.size):order[seq[0][block.a+i]['opcode']]={'opcode950':seq[1][block.b+i]['opcode'],'blockLength':block.size}
for row in rows:
 row['orderedBindingCandidate']=order.get(row['opcode947'])
 c=row['candidates']; o=row['orderedBindingCandidate']
 row['independentAgreement']=bool(o and row['uniqueStrongCandidate'] and c[0]['opcode950']==o['opcode950'])
(out/'server-candidates.json').write_text(json.dumps(rows,indent=2))
with (out/'server-map-review.csv').open('w',newline='') as f:
 writer=csv.writer(f);writer.writerow(['name947','opcode947','candidate950_order','wire_length','sequence_block_length','candidate950_shape','shape_score','independent_agreement','payload_verified'])
 for r in sorted(rows,key=lambda r:r['opcode947']):
  o=r['orderedBindingCandidate'] or {};c=(r['candidates'] or [{}])[0]
  writer.writerow([r['name947'] or '',r['opcode947'],o.get('opcode950',''),r['size'],o.get('blockLength',''),c.get('opcode950',''),c.get('score',''),r['independentAgreement'],False])
client=json.loads((out/'client-candidates.json').read_text())
summary={'server_descriptor_count':223,'client_descriptor_count':129,'server_handlers_recovered':len(builds['950']['handlers']),'server_order_candidates':len(order),'server_shape_strong_candidates':sum(r['uniqueStrongCandidate'] for r in rows),'server_independent_agreements':sum(r['independentAgreement'] for r in rows),'client_unique_sender_shape_candidates':sum(len(r['candidates'])==1 for r in client),'payload_layouts_verified':0,'active_protocol_files_changed':False}
(out/'mapping-result.json').write_text(json.dumps(summary,indent=2))
lines=['# 947 to950 packet mapping — bounded pass','',
'All results are static review artifacts. Active protocol maps, binaries and servers were not changed by this mapping pass. No live950 lobby success is claimed.','',
'Extracted all223 server and129 client descriptor lengths. Recovered221 native950 server handler bindings. Registration-order alignment gives208 server opcode candidates.71 of these independently agree with strong normalized handler-shape matches (73 strong shape candidates total). Five client opcodes have unique exact normalized sender-shape candidates.','',
'Native code-address relocations were normalized; direct call targets and RIP-relative data targets were abstracted. Consequently shape agreement does not establish called helper semantics, byte transformations, or packet field order. All payload layouts remain unverified. Equal wire lengths or long matching registration sequences alone do not prove packet meaning.','',
'## Minimum lobby/world candidates','', '|Name inherited from947|947|950 candidate|Evidence|','|---|---:|---:|---|']
for r in rows:
 if not r['name947']:continue
 o=r['orderedBindingCandidate']
 if not o:continue
 evidence='Order + strong handler shape' if r['independentAgreement'] else 'Registration-order candidate; payload review required'
 if r['name947']=='IF_SETHIDE':evidence='Order suggests67; shape-only ranking misleadingly prefers85'
 lines.append(f"|{r['name947']}|{r['opcode947']}|{o['opcode950']}|{evidence}|")
lines+=['','## Files','',
'- server-map-review.csv: compact947→950 comparison, with independent agreement and payload-verification columns.',
'- server-candidates.json: ranked handler-shape candidates and independent registration-order candidates.',
'- client-candidates.json: exact sender-shape candidates, still review-only.',
'- 950-server-sizes.toml /950-client-sizes.toml: actual950 descriptor lengths; never combine with inherited947 names in a live server.',
'- 947-native.json /950-native.json: constructor sites, descriptors, vtable bindings, parser targets and normalized code for follow-up without repeating extraction.',
'- lobby-parser-comparison.txt: unnormalized disassembly of candidate lobby/world handlers, for the next field-layout review.',
'','## Next smallest implementation step','',
'Validate IF_OPENTOP94→1, IF_OPENSUB8→100 and RUNCLIENTSCRIPT121→35 field order/transforms against the saved native parsers, then update only those950 codecs and test the lobby. Resolve IF_SETHIDE103→67 separately. NO_TIMEOUT216→183 is an order-based candidate; its leaf handler did not produce a fingerprint. Do not install the full208-entry draft automatically.',
'','## Reproduce','',
'Run tools/map_950_protocol.py followed by tools/summarize_950_mapping.py with Python and pefile/capstone installed. The existing AstraNXT .venv was read-only for this pass; no dependencies were installed. The original947 executable is only read as a reference.']
(out/'README.md').write_text('\n'.join(lines)+'\n', encoding='utf-8')
paths={'947':(root.parent/'AstraNXT/OpenNXT/data/clients/947/win64/original/rs2client.exe'),'950':root/'OpenNXT/data/clients/950/win64/original/rs2client.exe'}
cs=capstone.Cs(capstone.CS_ARCH_X86,capstone.CS_MODE_64);cs.skipdata=True
pes={r:pefile.PE(str(p)) for r,p in paths.items()};asm=[]
for row in rows:
 if not row['name947'] or not row['orderedBindingCandidate']:continue
 asm.append('\n'+row['name947'])
 for rev,op in [('947',row['opcode947']),('950',row['orderedBindingCandidate']['opcode950'])]:
  h=next(h for h in builds[rev]['handlers'] if h['opcode']==op);pe=pes[rev];start=int(h['parserTarget'],16)
  asm.append(f'{rev} opcode{op} parser {hex(start)}')
  n=len(h['fingerprint'])
  for i,(addr,size,mn,oper) in enumerate(cs.disasm_lite(pe.get_data(start-pe.OPTIONAL_HEADER.ImageBase,16000),start)):
   if i>=n:break
   asm.append(f'{addr:x}  {mn} {oper}')
(out/'lobby-parser-comparison.txt').write_text('\n'.join(asm))
print(json.dumps(summary))

