"""Report exact-cache binding gaps without treating every absent optional graphic as a defect."""
import json, collections
from pathlib import Path
root=Path(__file__).resolve().parents[2]
source=root/'protocol-analysis/combat-canonical-matrix-950-20260923.json'
audit=json.loads((root/'protocol-analysis/combat-presentation-audit-950-20260923.json').read_text())
matrix=json.loads(source.read_text(encoding='utf-8'))
by_id={r['struct']:r for r in matrix['abilities']}
pins={int(line.split('=')[0][3:]) for line in (root/'Ataraxia950/resources/native950/combat-effects-950.properties').read_text().splitlines() if line.startswith('id.')}
rows=[]
for binding in audit['abilities']:
    sid=binding['struct']; canonical=by_id.get(sid,{})
    fields={}
    for name,key in [('animation','sequence'),('casterGraphic','casterGraphic'),('projectile','projectile'),('impactGraphic','impactGraphic')]:
        ids=sorted({v[key] for v in binding['variants'] if v.get(key) is not None and v[key]>=0})
        fields[name]={'ids':ids,'status':'CACHE BOUND; LIVE ACCEPTANCE REQUIRED' if ids else 'UNRESOLVED REQUIREMENT/IDENTITY'}
        if name=='animation' and not ids:fields[name]['status']='MISSING BINDING'
        if name!='animation' and ids:
            rejected=[i for i in ids if i not in pins]
            fields[name]['rejectedByIdentityGate']=rejected
            if rejected:fields[name]['status']='REJECTED BY GRAPHICS GATE'
    if sid in (47129,14229,14231):fields['animation']['status']='MISSING BINDING; NO GUESSED MOVEMENT ANIMATION'
    if sid==48314:
        fields['projectile']['status']='LIVE PASS: travelling/bouncing; preserve'
        fields['impactGraphic']['status']='LIVE PARTIAL: separate impact asset not established'
    if sid in (48302,48304,48306,31820,33965,48303,48305,48307,32342):
        fields['conjureActorAttack']={'status':'MISSING: actor attack identity unproven; not replaced by player casting animation'}
        fields['conjureActorSpawnDespawnCommandFx']={'status':'PARTIAL: exact actor bindings unproven; not generic NPC substitutions'}
        fields['conjureIdleFollow']={'status':'CACHE BOUND via companion BAS; visual model semantics remain live pending'}
    rows.append({'struct':sid,'name':binding['name'],'nativeBooks':canonical.get('nativeBooks',[]),'implementation':'PARTIAL','stages':fields})
report={'revision':950,'sourceMatrix':source.name,'scope':'Every admitted ability from exact-cache presentation audit; canonical entries plus admitted conditional/utility entries. No COMPLETE promotion.',
 'interpretation':'An absent parameter does not prove a projectile/impact is required. UNRESOLVED REQUIREMENT/IDENTITY needs semantic or visual evidence, not an invented graphic. Cache-bound IDs prove admission, not rendered suitability.',
 'summary':{'admittedAbilities':len(rows),'missingAnimationBindings':sum(not r['stages']['animation']['ids'] for r in rows),'graphicsRejectedByCurrentGate':sum(len(s.get('rejectedByIdentityGate',[])) for r in rows for s in r['stages'].values()),'complete':0},'abilities':rows}
output=root/'protocol-analysis/combat-presentation-gaps-950-20260923.json'
output.write_bytes((json.dumps(report,indent=2)+'\n').encode())
print(json.dumps(report['summary']))
