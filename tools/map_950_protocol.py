"""Bounded947/950 mapping pass. Review artifacts only; never edits active maps."""
from pathlib import Path
import sys,json,re,hashlib,inspect,bisect,difflib,collections,tomllib
import pefile,capstone
import native_protocol_extractor as ex
root=Path(__file__).resolve().parents[1]
out=root/'protocol-analysis'
oldroot=root.parent/"AstraNXT/OpenNXT"
paths={'947':oldroot/'data/clients/947/win64/original/rs2client.exe','950':root/'OpenNXT/data/clients/950/win64/original/rs2client.exe'}
expected={'947':'c8128a6566749afc629453235c4373845a5a17920eebfb01f5705d6e007ce4b9','950':'fc7492548627a4068e88a7a45ba4f0be93004adf246bb4b174b24ae588768b36'}
cs=capstone.Cs(capstone.CS_ARCH_X86,capstone.CS_MODE_64)
cs.skipdata=True
builds={}
for rev,path in paths.items():
 raw=path.read_bytes(); assert hashlib.sha256(raw).hexdigest()==expected[rev]
 pe=pefile.PE(data=raw); groups=ex.extract_registrar_calls(pe)
 tables={'server':ex.select_table(groups,200,250,80),'client':ex.select_table(groups,120,160,16)}
 refs=ex.descriptor_references(pe,tables)
 # Retain the extractor's per-binding/vtable validation, adapting only the expected count.
 ns=dict(vars(ex)); count=tables['server']['count']-1
 source=re.sub(r"    if len\(result\).*?\n        raise ValueError\([^\n]*\)\n", "", inspect.getsource(ex.recover_server_handlers))
 exec(source,ns); rawhandlers=ns['recover_server_handlers'](pe,refs['server']); handlers=list({(h['opcode'],h['parserTarget']):h for h in rawhandlers}.values()); print(rev, 'raw',len(rawhandlers),'unique handlers',len(handlers),'unique opcodes',len(set(h['opcode'] for h in handlers)),flush=True)
 functions=sorted((pe.OPTIONAL_HEADER.ImageBase+e.struct.BeginAddress,pe.OPTIONAL_HEADER.ImageBase+e.struct.EndAddress) for e in pe.DIRECTORY_ENTRY_EXCEPTION)
 starts=[x[0] for x in functions]
 def fingerprint(address):
  i=bisect.bisect_right(starts,address)-1
  if i<0 or address>=functions[i][1]:return []
  end=functions[i][1]; tokens=[]
  for addr,size,mn,oper in cs.disasm_lite(pe.get_data(address-pe.OPTIONAL_HEADER.ImageBase,min(end-address,16000)),address):
   oper=re.sub(r'rip [+-] 0x[0-9a-f]+','rip DISP',oper)
   if mn.startswith('j') or mn=='call':
    if re.fullmatch(r'0x[0-9a-f]+',oper):
     target=int(oper,16)
     oper=('local:'+str(target-address)) if address<=target<end else 'TARGET'
   tokens.append(mn+' '+oper)
  return tokens
 for row in handlers:row['fingerprint']=fingerprint(int(row['parserTarget'],16))
 # Client descriptor references identify senders, not receive parsers.
 client_functions={op:sorted(set(row['functionVA'] for row in rows if row['functionVA'] and int(row['instructionRVA'],16)>0x22000)) for op,rows in refs['client'].items()}
 clients={op:[fingerprint(int(f,16)) for f in fs] for op,fs in client_functions.items()}
 builds[rev]={'sha256':expected[rev],'tables':tables,'handlers':handlers,'clientFunctions':client_functions,'clientFingerprints':clients}
 (out/f'{rev}-native.json').write_text(json.dumps(builds[rev],indent=2))
 print(f'{rev}: {len(handlers)} server handlers recovered',flush=True)
# Match server handlers using normalized instruction sequences and equal wire lengths.
old,new=builds['947'],builds['950']
sizes={r:{s:{p['opcode']:p['size'] for p in builds[r]['tables'][s]['packets']} for s in ('server','client')} for r in builds}
names={int(k):v for k,v in tomllib.loads((oldroot/'data/prot/947/serverProtNames.toml').read_text())['values'].items()}
for r in builds:
 for side in ('server','client'):
  (out/f'{r}-{side}-sizes.toml').write_text('# Native descriptor sizes only; meanings/fields require validation.\n[values]\n'+''.join(f'{k} = {v}\n' for k,v in sizes[r][side].items()))
rows=[]
for a in old['handlers']:
 scored=[]
 for b in new['handlers']:
  if sizes['947']['server'][a['opcode']]!=sizes['950']['server'][b['opcode']]:continue
  fa,fb=a['fingerprint'],b['fingerprint']
  if not fa or not fb:continue
  length_bound=2*min(len(fa),len(fb))/(len(fa)+len(fb))
  if length_bound<0.45:continue
  score=difflib.SequenceMatcher(None,fa,fb,autojunk=False).ratio()
  scored.append({'opcode950':b['opcode'],'score':round(score,5),'parser950':b['parserTarget'],'instructions950':len(fb),'exactShape':fa==fb})
 scored.sort(key=lambda x:x['score'],reverse=True)
 row={'opcode947':a['opcode'],'name947':names.get(a['opcode']),'size':sizes['947']['server'][a['opcode']],'parser947':a['parserTarget'],'instructions947':len(a['fingerprint']),'candidates':scored[:3],'status':'review-only: address normalization does not validate callee semantics or wire transforms'}
 rows.append(row)
# Reciprocal uniqueness: no multiple947handlers claiming the same high-confidence target.
claims=collections.Counter(r['candidates'][0]['opcode950'] for r in rows if r['candidates'] and r['candidates'][0]['score']>=.85)
for r in rows:
 c=r['candidates']
 r['uniqueStrongCandidate']=bool(c and c[0]['score']>=.85 and (len(c)<2 or c[0]['score']-c[1]['score']>=.10) and claims[c[0]['opcode950']]==1 and r['instructions947']>=8)
(out/'server-candidates.json').write_text(json.dumps(rows,indent=2))
client_rows=[]
for op,fs in old['clientFingerprints'].items():
 matches=[]
 for np,nfs in new['clientFingerprints'].items():
  if sizes['947']['client'][op]!=sizes['950']['client'][np]:continue
  exact=sum(1 for f in fs if len(f)>=12 and f in nfs)
  if exact:matches.append({'opcode950':np,'exactSenderShapes':exact})
 client_rows.append({'opcode947':op,'size':sizes['947']['client'][op],'candidates':matches,'status':'review-only sender shape matches'})
(out/'client-candidates.json').write_text(json.dumps(client_rows,indent=2))
summary={'serverStrongCandidates':sum(r['uniqueStrongCandidate'] for r in rows),'serverMatchedExactShapes':sum(bool(r['candidates'] and r['candidates'][0]['exactShape']) for r in rows),'clientUniqueExactSenderCandidates':sum(len(r['candidates'])==1 for r in client_rows),'minimumLobby':[r for r in rows if r['name947']]}
(out/'summary.json').write_text(json.dumps(summary,indent=2))
print(json.dumps(summary,indent=2))


