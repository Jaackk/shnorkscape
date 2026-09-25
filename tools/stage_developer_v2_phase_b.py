"""Package the already-built Phase-B candidate. Never install or restart.

Run after source verification/checkpoint. Existing candidate and installed files
are read-only inputs. The new cache delta must already have been authored.
"""
import hashlib,json,re,shutil,subprocess
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
NAME='developer-console-v2-phase-b-20260925'

def sha(path):return hashlib.sha256(path.read_bytes()).hexdigest().upper()
def edit(path,fn):
    p=ROOT/path;raw=p.read_bytes();s=raw.decode();p.write_bytes(fn(s).encode())

def main():
    mp=ROOT/'protocol-analysis/playability-candidate-20260923.json';m=json.loads(mp.read_bytes())
    assert m['candidate']=='developer-console-v2-phase-a-20260925','Only the audited predecessor is supported'
    old=ROOT/'dist'/m['candidate'];dest=ROOT/'dist'/NAME
    assert (dest/'cache-v4/12/21151.dat').is_file(),'Build the complete script delta first'
    for e in m['files']:
        assert sha(old/e['source'])==e['sha256'],('Previous candidate changed',e['source'])
        installed=ROOT/e['target'];assert installed.is_file(),e['target']
        assert sha(installed) in (e['sha256'],e['beforeSha256']),('Unreviewed runtime change',e['target'])
        target=dest/e['source'];target.parent.mkdir(parents=True,exist_ok=True)
        # Preserve the new script delta; copy only unchanged predecessor artifacts.
        if not target.exists():shutil.copyfile(old/e['source'],target)
        e['beforeSha256']=sha(installed)
    jar=ROOT/'Ataraxia950/build/libs/ataraxia-950-1.0-UNTRACKED.jar'
    shutil.copyfile(jar,dest/'ataraxia-950-1.0-UNTRACKED.jar')
    for sid in range(21144,21152):
        assert not (ROOT/f'cache/12/{sid}.dat').exists(),'New helper unexpectedly exists'
        m['files'].append({'source':f'cache-v4/12/{sid}.dat','target':f'cache/12/{sid}.dat','beforeSha256':'ABSENT'})
    for e in m['files']:e['sha256']=sha(dest/e['source'])
    m.update(candidate=NAME,sourceCommit=subprocess.check_output(['git','rev-parse','HEAD'],cwd=ROOT).decode().strip(),
             validationReport='protocol-analysis/developer-console-v2-phase-b-950.json',
             liveChecklist='docs/DEVELOPER-CONSOLE-V2-PHASE-B-20260925.md',
             liveAcceptance='Phase B composition checkpoint. Automated verification is recorded in the report; new Vulkan controls remain live-test pending.')
    mp.write_bytes((json.dumps(m,indent=2)+'\n').encode())
    ref=next(e['sha256'] for e in m['files'] if e['target']=='cache/255/12.dat')
    for gate in ('Start-950Server.ps1','Test-Bundle.ps1','Prepare-ClientCache.ps1'):
        def patch(s):
            match=re.search(r'@\(([^\r\n]*8A45E12B[^\r\n]*?)\)',s);assert match
            return s if ref in match.group(1) else s[:match.end(1)]+",'"+ref+"'"+s[match.end(1):]
        edit(gate,patch)
    def installer(s):
        s=s.replace(",'developer-console-v2-phase-a-20260925')",",'developer-console-v2-phase-a-20260925','"+NAME+"')")
        at='if ($manifest.candidate -notin '
        s=s.replace(at,"if ($manifest.candidate -eq '"+NAME+"') { $expectedTargets += @(21131..21151 | ForEach-Object { \"cache/12/$_.dat\" }) }\n"+at,1)
        return s.replace('$planned = @()',"if ($manifest.candidate -eq '"+NAME+"') { $newTargets = @(21144..21151 | ForEach-Object { \"cache/12/$_.dat\" }) }\n$planned = @()",1)
    edit('Apply-PlayabilityUpdate.ps1',installer)
    def launcher(s):
        s=s.replace('V2 Phase A test candidate.','V2 Phase B test candidate.')
        s=s.replace('Native NPC scrolling, full model previews, drag rotation and integrated search.','Home, contextual navigation, grouped NPCs and actual searchable item rows.')
        s=s.replace('Primitive acceptance checkpoint: final V2 page composition follows the live test.','Includes item grants, loadouts, player preview, zoom and placement diagnostics.')
        return s.replace('docs/DEVELOPER-CONSOLE-V2-PHASE-A-20260925.md',m['liveChecklist'])
    edit('Apply Staged Update.cmd',launcher)
    print('Packaged',NAME,len(m['files']),'files; jar',sha(jar),'reference',ref)

if __name__=='__main__':main()
