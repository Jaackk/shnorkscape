"""Join exact-cache membership with observed offline execution. Never infer live acceptance."""
import argparse, collections, json
from pathlib import Path

def build(inventory, acceptance):
    observed={row['struct']:row for row in acceptance['abilities']}
    rows=[]
    for entry in inventory['abilities']:
        row=dict(entry)
        params=row['typedParameters']
        books=row['nativeBooks']
        utility=(row['name'].startswith('Shout -') or row['name'] in ('Surge','Escape','Dive','Bladed Dive','Eat Food','Regenerate','Incite')
                 or row['name'].startswith('Quiver ammo'))
        group='movement/utility' if utility else books[0] if books else 'conditional/unresolved'
        row['reportGroup']=group
        row['automatedTestStatus']='EXECUTED WITH REAL CACHE AND EQUIPMENT' if row['struct'] in observed else 'NOT EXECUTED BY THIS HARNESS'
        if row['struct'] in observed: row['executionEvidence']=observed[row['struct']]
        row['canonicalStatus']=row['implementationStatus'] if row['canonicalClass']=='CANONICAL' else (
            'CONDITIONAL/ALTERNATE' if row['canonicalClass']=='CONDITIONAL/ALTERNATE' else 'BLOCKED BY UNPROVEN 950 IDENTITY')
        if observed.get(row['struct'],{}).get('executionMode')=='automatic':
            row['canonicalStatus']='PARTIAL'
            row['implementationStatus']='PARTIAL'
            row['blockers']=['Automatic attack route is executed; explicit native book-click route and live visuals still require acceptance.']
        row['liveVisualStatus']='PENDING'
        row['effectEvidence']={
            'tooltip':params.get('2795'), 'rawTargetMode':params.get('8170'),
            'bleed':row.get('lifecycle',{}).get('effect')=='BLEED',
            'stun':row.get('lifecycle',{}).get('effect')=='STUN',
            'movement':row.get('lifecycle',{}).get('effect')=='MOVEMENT',
            'aoe':'per-ability geometry in Native950MeleeCombat; full acceptance pending',
            'transform':'CS8247 conditional branch referenced' if row['conditionalTransformReferenced'] else None,
        }
        rows.append(row)
    summary={}
    for group in ['melee','ranged','magic','defence','constitution','necromancy','movement/utility']:
        subset=[r for r in rows if r['canonicalClass']=='CANONICAL' and r['reportGroup']==group]
        counts=collections.Counter(r['canonicalStatus'] for r in subset)
        summary[group]={'canonicalEntries':len(subset),**{s:counts[s] for s in ['COMPLETE','PARTIAL','MISSING','BLOCKED BY UNPROVEN 950 IDENTITY']},
                        'automatedExecuted':sum(r['struct'] in observed for r in subset),'liveVerifiedThisPass':0}
    return {'schemaVersion':1,'revision':950,'scope':'Canonical native book entries; conditional transforms separate. Utility/minigame entries retained without pretending they are combat attacks.',
            'statusPolicy':'No COMPLETE promotion from generic damage or a decoder alone. Unreached structures are unresolved, not silently declared non-player-facing.',
            'summary':summary,'cacheInventoryCount':len(rows),'abilities':rows}

if __name__=='__main__':
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('inventory');parser.add_argument('acceptance');parser.add_argument('output')
    args=parser.parse_args()
    report=build(json.loads(Path(args.inventory).read_text()),json.loads(Path(args.acceptance).read_text()))
    Path(args.output).write_text(json.dumps(report,indent=2,ensure_ascii=False)+'\n',encoding='utf-8')
    print(json.dumps(report['summary'],indent=2))
