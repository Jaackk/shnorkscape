from pathlib import Path
r=Path(__file__).resolve().parents[1];p=r/'OpenNXT/data/prot/950'
s=(p/'serverProtNames.toml').read_text();s += '' if '129 = "WORLDLIST_FETCH_REPLY"' in s else '129 = "WORLDLIST_FETCH_REPLY"\n';(p/'serverProtNames.toml').write_text(s,encoding='utf-8')
s=(p/'clientProtNames.toml').read_text().replace('104 = "NATIVE_950_CLIENT_104"','104 = "NO_TIMEOUT"').replace('108 = "NATIVE_950_CLIENT_108"','108 = "WORLDLIST_FETCH"');(p/'clientProtNames.toml').write_text(s,encoding='utf-8')
(p/'clientProt/WORLDLIST_FETCH.txt').write_text('checksum int\n',encoding='utf-8')
s=(r/'tools/install_950_lobby.py').read_text().replace("183:'NO_TIMEOUT'}","183:'NO_TIMEOUT',129:'WORLDLIST_FETCH_REPLY'}")
s=s.replace("for i in range(129)),encoding='utf-8')", "for i in range(129)).replace('104 = \\\"NATIVE_950_CLIENT_104\\\"','104 = \\\"NO_TIMEOUT\\\"').replace('108 = \\\"NATIVE_950_CLIENT_108\\\"','108 = \\\"WORLDLIST_FETCH\\\"'),encoding='utf-8')")
(r/'tools/install_950_lobby.py').write_text(s,encoding='utf-8')
print('Mapped client108 world-list request and104 keepalive; server129 world-list reply.')
