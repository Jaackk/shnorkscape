"""Exercise orchestration with disposable scripts and mocked process APIs, never live stop/start."""
import json, os, subprocess, tempfile, unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
PS=Path(os.environ['SystemRoot'])/'System32/WindowsPowerShell/v1.0/powershell.exe'

class UpdateAndPlay(unittest.TestCase):
    def run_case(self, case='normal', check=False):
        temp_root=ROOT/'temp'
        temp_root.mkdir(exist_ok=True)
        with tempfile.TemporaryDirectory(prefix='update-play-',dir=temp_root) as tmp:
            root=Path(tmp);(root/'logs').mkdir();events=root/'events.txt'
            def write(name,text): (root/name).write_text(text,encoding='utf-8')
            write('Client-LaunchLock.ps1', """function Enter-950ClientLock($Root) {
                $m=New-Object PSObject
                $m | Add-Member ScriptMethod ReleaseMutex { Add-Content $global:events 'unlock' }
                $m | Add-Member ScriptMethod Dispose {}
                return $m
            }""")
            write('Apply-PlayabilityUpdate.ps1', """param([switch]$CheckOnly)
                if($CheckOnly){Add-Content $global:events 'check';if($global:scenario -eq 'bad-stage'){throw 'bad stage'};return}
                Add-Content $global:events 'apply'
                if($global:scenario -eq 'apply-fails'){throw 'apply failed'}
            """)
            write('Stop-950Test.ps1', "Add-Content $global:events 'stop'; if($global:scenario -eq 'stop-fails'){throw 'stop failed'}")
            write('Start-950Server.ps1', """param($LanAddress,[switch]$WorkspaceDurabilityGate,[switch]$WorkspaceCaptureGate,[switch]$WorkspaceJaxaRollout)
                if(!$WorkspaceDurabilityGate -or !$WorkspaceCaptureGate -or !$WorkspaceJaxaRollout){throw 'workspace flags lost'}
                Add-Content $global:events ('lan:'+ $LanAddress)
            """)
            write('Start-950Test.ps1', "param([switch]$OpenGL); Add-Content $global:events 'ready-and-client'")
            record={'Workspace':str(root),'ProcessId':100,'CreatedUtc':'2026-09-25T00:00:00Z','JavaPath':str(root/'java.exe'),'ClassPath':str(root/'runtime/*')}
            if case=='stale': record['CreatedUtc']='2026-09-24T00:00:00Z'
            (root/'logs/server.pid.json').write_text(json.dumps(record))
            def quote(value): return "'"+str(value).replace("'","''")+"'"
            harness="""
                $global:events=EVENTS
                $global:scenario=SCENARIO
                $global:fixture=FIXTURE
                $global:closed=@{}
                function Get-CimInstance($ClassName,$Filter){
                    $server=[pscustomobject]@{Name='java.exe';ProcessId=100;ExecutablePath=(Join-Path $global:fixture 'java.exe');CreationDate=[datetime]'2026-09-25T00:00:00Z';CommandLine=((Join-Path $global:fixture 'runtime/*')+' -Dopennxt.lan.address=192.168.1.10 ')}
                    if($global:scenario -ne 'lan'){$server.CommandLine=Join-Path $global:fixture 'runtime/*'}
                    $all=@($server)
                    foreach($number in @(200,201)){
                        if(!$global:closed[$number]){$all += [pscustomobject]@{Name='rs2client-test.exe';ProcessId=$number;ExecutablePath=(Join-Path $global:fixture ('client'+$number+'\\rs2client-test.exe'));CreationDate=[datetime]'2026-09-25T00:00:00Z'}}
                    }
                    if($global:scenario -eq 'unrelated'){$all += [pscustomobject]@{Name='java.exe';ProcessId=999;ExecutablePath='C:\\unrelated\\java.exe';CreationDate=[datetime]'2026-09-25T00:00:00Z'}}
                    if($Filter){return $all | Where-Object {$_.ProcessId -eq [int]($Filter -replace '\\D','')}}
                    return $all
                }
                function Get-Process($Id,$ErrorAction){
                    $p=[pscustomobject]@{Id=$Id}
                    $p | Add-Member ScriptMethod CloseMainWindow {Add-Content $global:events ('close:'+ $this.Id);return $true}
                    $p | Add-Member ScriptMethod WaitForExit {param($ms);$global:closed[$this.Id]=$true;return $true}
                    return $p
                }
                function Stop-Process {throw 'No real process may be killed in tests'}
                function Start-Sleep {}
                function Get-NetTCPConnection($State,$ErrorAction){if($global:scenario -eq 'remote'){return [pscustomobject]@{OwningProcess=100;LocalPort=43650}}}
                . SCRIPT
                try {Invoke-950UpdateAndPlay -Root $global:fixture CHECK;exit 0}
                catch {Write-Host $_.Exception.Message;exit 1}
            """
            for key,value in {'EVENTS':quote(events),'SCENARIO':quote(case),'FIXTURE':quote(root),'SCRIPT':quote(ROOT/'Update-And-Play.ps1'),'CHECK':'-CheckOnly' if check else ''}.items(): harness=harness.replace(key,value)
            write('run.ps1',harness)
            result=subprocess.run([str(PS),'-NoProfile','-ExecutionPolicy','Bypass','-File',str(root/'run.ps1')],capture_output=True,text=True)
            return result.returncode, events.read_text().splitlines() if events.exists() else [],result.stdout+result.stderr

    def test_normal_order_closes_both_clients_before_stop_install_and_ready_launch(self):
        code,events,output=self.run_case();self.assertEqual(0,code,output)
        self.assertEqual(['check','close:200','close:201','stop','apply','ready-and-client','unlock'],events)

    def test_check_only_has_no_shutdown_install_or_launch(self):
        code,events,output=self.run_case(check=True);self.assertEqual(0,code,output);self.assertEqual(['check','unlock'],events)

    def test_invalid_candidate_stale_identity_and_unrelated_java_fail_before_shutdown(self):
        for case in ('bad-stage','stale','unrelated'):
            with self.subTest(case=case):
                code,events,output=self.run_case(case);self.assertNotEqual(0,code,output);self.assertEqual(['check','unlock'],events)

    def test_failed_stop_or_install_never_starts_server_or_client(self):
        for case in ('stop-fails','apply-fails'):
            with self.subTest(case=case):
                code,events,output=self.run_case(case);self.assertNotEqual(0,code,output)
                self.assertNotIn('ready-and-client',events);self.assertEqual('unlock',events[-1])
                if case=='stop-fails':self.assertNotIn('apply',events)

    def test_lan_address_and_workspace_profile_survive_restart(self):
        code,events,output=self.run_case('lan');self.assertEqual(0,code,output)
        self.assertEqual(['check','close:200','close:201','stop','apply','lan:192.168.1.10','ready-and-client','unlock'],events)

    def test_connected_remote_client_keeps_server_and_files_untouched(self):
        code,events,output=self.run_case('remote');self.assertNotEqual(0,code,output)
        self.assertEqual(['check','close:200','close:201','unlock'],events)

if __name__=='__main__':unittest.main()
