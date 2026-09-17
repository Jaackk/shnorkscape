[CmdletBinding()]
param([switch]$SkipTests)
$ErrorActionPreference='Stop'
$root=$PSScriptRoot
New-Item -ItemType Directory -Path (Join-Path $root 'logs') -Force | Out-Null
$recordPath=Join-Path $root 'logs\server.pid.json'
if(Test-Path -LiteralPath $recordPath){
 $record=Get-Content -LiteralPath $recordPath -Raw | ConvertFrom-Json
 $process=Get-CimInstance Win32_Process -Filter "ProcessId = $($record.ProcessId)"
 if($process -and $process.ExecutablePath -eq $record.JavaPath -and $process.CommandLine.Contains($root)){
  throw 'Use Stop.cmd before rebuilding this running bundle.'
 }
}
$tasks=@('test','jar')
if($SkipTests){$tasks=@('jar')}
& (Join-Path $root 'Build-Ataraxia950.ps1') -Tasks $tasks -Deploy
& (Join-Path $root 'Build-OpenNXT.ps1') -Deploy
& (Join-Path $root 'Build-950Lobby.ps1')
& (Join-Path $root 'Test-Protocol.ps1')
& (Join-Path $root 'tools\Verify-Js5Payload.ps1')
Write-Host 'All server source rebuilt and installed using bundled offline dependencies.'
