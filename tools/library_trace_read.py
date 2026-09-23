"""Decode exported native scripts using the project's established opcode map."""
import ast, json, sys
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
scope={}
for node in ast.parse((ROOT/'tools/verify_950_ui_scripts.py').read_text()).body:
    if (isinstance(node,ast.Assign) and any(isinstance(t,ast.Name) and t.id in ('FOUR','THREE','NAMES') for t in node.targets)) or (isinstance(node,ast.FunctionDef) and node.name in ('decode','lines')):
        exec(compile(ast.Module(body=[node],type_ignores=[]),'decoder','exec'),scope)
mapping=json.loads((ROOT/'protocol-analysis/ui-scripts-950-evidence.json').read_text())['opcodeMap947to950']
inverse={int(v['opcode950'],16):int(k,16) for k,v in mapping.items()}
def read(sid):
    return scope['decode']((ROOT/f'temp/library-followup-trace/12-{sid}-0.bin').read_bytes(),inverse)
if __name__=='__main__':
    for sid in map(int,sys.argv[1:]):
        ins,tail=read(sid)
        print('SCRIPT',sid,'ARGS',[int.from_bytes(tail[n:n+2],'big') for n in (10,12,14)])
        print('\n'.join(f'{i} {s}' for i,s in enumerate(scope['lines'](ins))))
