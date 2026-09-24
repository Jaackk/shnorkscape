"""Read-only evidence audit. Inputs are public 2670 exports/raw groups under --inputs.
Does not emit runtime symbols or alter the cache. See the investigation report for downloads.
"""
import argparse, hashlib, json, struct
from pathlib import Path
from build_necromancy_polish_cache import unpack

def decode_gameval(raw, component=False):
    version,count=struct.unpack_from('>II',raw)
    if version not in (1,2) or count>2_000_000:raise ValueError('Unsupported gameval header')
    width=8 if version==2 else 4;base=8+width*count
    result={}
    for i in range(count):
        key,offset=struct.unpack_from('>ii',raw,8+i*8) if version==2 else (i,struct.unpack_from('>i',raw,8+i*4)[0])
        if offset<0:continue
        start=base+offset
        if start>=len(raw):raise ValueError('Invalid string offset')
        name=raw[start:raw.index(0,start)].decode('cp1252').lower()
        if component:name=name.replace('__',':',1)
        if key in result:raise ValueError('Duplicate key')
        result[key]=name
    return result

def main():
    p=argparse.ArgumentParser();p.add_argument('--inputs',type=Path,required=True);p.add_argument('--output',type=Path,required=True);a=p.parse_args()
    def txt(t):return dict(l.split('\t',1) for l in (a.inputs/f'2670-{t}.txt').read_text(encoding='utf-8').splitlines() if l)
    maps={t:txt(t) for t in ('component','interface','var_player','varbit','enum','loc','obj','dbrow')}
    checks=[]
    for g,t in ((0,'component'),(24,'interface'),(61,'var_player')):
        raw=unpack((a.inputs/f'2670-67-{g}.dat').read_bytes());r=decode_gameval(raw,t=='component')
        if t=='component':r={f'{k>>16}:{k&65535}':v for k,v in r.items()}
        else:r={str(k):v for k,v in r.items() if not v.startswith('_')}
        differences=[{'key':k,'raw':r.get(k),'viewer':maps[t].get(k)} for k in sorted(r.keys()|maps[t].keys()) if r.get(k)!=maps[t].get(k)]
        maps[t]=r  # Original cache names are primary; viewer discrepancies stay explicit.
        checks.append({'type':t,'archive':g,'entries':len(r),'rawSha256':hashlib.sha256(raw).hexdigest(),'viewerMatchesRaw':not differences,'differingKeys':len(differences),'sourceOnly':sum(x['viewer'] is None for x in differences),'viewerOnly':sum(x['raw'] is None for x in differences),'differenceExamples':differences[:8]})
    rawvars=decode_gameval(unpack((a.inputs/'2670-67-61.dat').read_bytes()))
    offset=min(k for k,v in rawvars.items() if v.startswith('_'))
    varbits={str(k-offset):v[1:] for k,v in rawvars.items() if v.startswith('_')}
    varbit_differences=[k for k,v in varbits.items() if maps['varbit'].get(k)!=v]
    assert not varbit_differences
    viewer_extra=sorted(maps['varbit'].keys()-varbits.keys(),key=int)
    maps['varbit']=varbits
    requested={'component':['623:27','623:26','623:16','623:17','623:1','623:3','623:5','623:12','623:11','623:37','1448:11','1448:14','1477:713','1477:736','91:25'], 'interface':['623','1448','517','91','938','947'], 'var_player':['10986','11035'], 'varbit':['28041','42886','42889','42892'], 'enum':['15005'], 'obj':['61880'], 'loc':['138056','48496'], 'dbrow':['18480']}
    symbols=[{'type':t,'sourceId':k,'name':maps[t][k]} for t,ks in requested.items() for k in ks]
    old={t:json.loads((a.inputs/f'727-{t}.json').read_bytes()) for t in ('interface','component','inv','obj','seq','quest')}
    report={'scope':'Research only; source IDs are NOT blanket-approved 950 runtime mappings', 'sourceCache':2670,'sourceRevision':949,'targetCache':2691,'targetRevision':950,'rawDecoderChecks':checks,'varbitSplit':{'baseOffset':offset,'entries':len(varbits),'allRawEntriesMatchViewer':True,'viewerAdditionalEntries':len(viewer_extra),'viewerAdditionalEntriesNotValidated':True},'representativeSymbols':symbols,'groupComparisons':json.loads((a.inputs/'comparisons.json').read_text()),'darkan727Coverage':{t:d['coverage'] for t,d in old.items()},'downloads':json.loads((a.inputs/'downloads.json').read_text())}
    comparison=a.inputs/'file-comparison-final.tsv'
    if comparison.exists():report['productionReaderComparisonRows']=[line.split('\t') for line in comparison.read_text(encoding='utf-16').splitlines()]
    a.output.write_bytes((json.dumps(report,indent=2)+'\n').encode('utf-8'))
    print('Audited raw index67; viewer mismatches retained explicitly, not silently accepted. Research inventory written.')
if __name__=='__main__':main()
