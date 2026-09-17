from pathlib import Path
import json
r=Path(__file__).resolve().parents[1]; prot=r/'OpenNXT/data/prot/950'
for side in ('server','client'):
 (prot/f'{side}ProtSizes.toml').write_text((r/f'protocol-analysis/950-{side}-sizes.toml').read_text(),encoding='utf-8')
server={1:'IF_OPENTOP',100:'IF_OPENSUB',67:'IF_SETHIDE',35:'RUNCLIENTSCRIPT',160:'SERVER_TICK_END',183:'NO_TIMEOUT',129:'WORLDLIST_FETCH_REPLY'}
(prot/'serverProtNames.toml').write_text('[values]\n'+''.join(f'{k} = "{v}"\n' for k,v in sorted(server.items())),encoding='utf-8')
(prot/'clientProtNames.toml').write_text('[values]\n'+''.join(f'{i} = "NATIVE_950_CLIENT_{i}"\n' for i in range(129)).replace('104 = \"NATIVE_950_CLIENT_104\"','104 = \"NO_TIMEOUT\"').replace('108 = \"NATIVE_950_CLIENT_108\"','108 = \"WORLDLIST_FETCH\"'),encoding='utf-8')
fields={'IF_OPENTOP':'id ushortle\nbool ubyte\nxtea0 int\nxtea1 int\nxtea2 int\nxtea3 int\n',
'IF_OPENSUB':'parent int\nxtea0 int\nxtea1 int\nxtea2 int\nid ushortle128\nflag u128byte\nxtea3 int\n',
'IF_SETHIDE':'parent intv1\nhidden ubyte\n'}
for name,data in fields.items():(prot/'serverProt'/f'{name}.txt').write_text(data,encoding='utf-8')
base=r/'OpenNXT/src/main/kotlin/com/opennxt'
p=base/'model/lobby/LobbyPlayer.kt';s=p.read_text();s=s.replace('if (OpenNXT.config.build == 947) {','if (OpenNXT.config.build == 947 || OpenNXT.config.build == 950) {');s=s.replace('Sent verified native lobby interface bootstrap','Sent native lobby interface bootstrap');p.write_text(s,encoding='utf-8')
p=base/'net/game/PacketRegistry.kt';s=p.read_text();first='        register(Side.CLIENT, "CLIENT_BOOTSTRAP_BLOB_28"';start=s.index(first);end=s.index('        if (opcodeFor(Side.CLIENT, "MAP_BUILD_COMPLETE")',start);s=s if 'if (OpenNXT.config.build != 950)' in s else s[:start]+'        if (OpenNXT.config.build != 950) {\n'+s[start:end]+'        }\n'+s[end:];p.write_text(s,encoding='utf-8')
p=base/'net/game/pipeline/GamePacketFraming.kt';s=p.read_text();s=s.replace('if (side != Side.CLIENT || bootstrapStage(channel)', 'if (OpenNXT.config.build == 950 || side != Side.CLIENT || bootstrapStage(channel)');p.write_text(s,encoding='utf-8')
p=r/'OpenNXT/data/config/server.toml';s=p.read_text().replace('compatServerpermAckOpcode = 206','compatServerpermAckOpcode = -1');p.write_text(s,encoding='utf-8')
print('Installed six950server packet mappings, native size tables, and scoped lobby/framing source changes.')

