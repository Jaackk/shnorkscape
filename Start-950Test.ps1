# Starts the complete950 gameplay profile; legacy feature arguments are compatibility-only.
param([switch]$NoWalk,[switch]$NoRibbon,[switch]$NoSettings,[switch]$NoRegions,
 [switch]$Collision,[switch]$Ataraxia,[switch]$LumbridgeNpcs,[switch]$AllNpcs,[switch]$DevTools,[switch]$Minimal,[switch]$Vulkan,[switch]$OpenGL,
 [switch]$UnverifiedCacheBindings,
 [switch]$Walk,[switch]$Ribbon,[switch]$Settings,[switch]$Regions)
$ErrorActionPreference='Stop'
$root=[IO.Path]::GetFullPath($PSScriptRoot)
if ($Vulkan -and $OpenGL) { throw 'Choose one renderer: -Vulkan or -OpenGL, not both.' }
. (Join-Path $root 'Client-WorkspaceLaunch.ps1')
if (!$OpenGL) { $null=Get-950WorkspaceLaunchProfile $root }
$recordPath=Join-Path $root 'logs\server.pid.json'
$backendJar=Join-Path $root 'OpenNXT\runtime\lib\ataraxia-950-1.0-UNTRACKED.jar'
$running=$false
if (Test-Path -LiteralPath $recordPath) {
 $record=Get-Content -LiteralPath $recordPath -Raw | ConvertFrom-Json
 $process=Get-CimInstance Win32_Process -Filter "ProcessId = $($record.ProcessId)"
 $running=$null -ne $process -and $record.Workspace -eq $root -and $process.ExecutablePath -eq $record.JavaPath -and $process.CreationDate.ToUniversalTime().Ticks -eq ([datetime]$record.CreatedUtc).ToUniversalTime().Ticks -and $process.CommandLine.Contains($record.ClassPath)
}
if ($running) {
 if (!$OpenGL) { Assert-950WorkspaceServerFlags $process.CommandLine }
 # Classes in a running JVM cannot pick up a replacement JAR. Refuse to open a new
 # client against an older process after deployment, rather than silently hiding it.
 if ((Test-Path -LiteralPath $backendJar) -and (Get-Item -LiteralPath $backendJar).LastWriteTimeUtc -gt $process.CreationDate.ToUniversalTime()) {
  throw 'The deployed backend JAR is newer than the running server. Use Stop.cmd, then Play.cmd, so the updated code is loaded.'
 }
 # Never silently reuse a restricted server from an older launcher.
 foreach($required in @('-Dopennxt.950.walk=true','-Dopennxt.950.ribbon=true','-Dopennxt.950.settings=true','-Dopennxt.950.regions=true','-Dopennxt.950.collision=true','-Dataraxia950.npcSpawns=true','-Dataraxia950.npcRegions= ','-Dataraxia950.devTools=true','-Dataraxia950.worldMap=true')) {
  if (!$process.CommandLine.Contains($required)) { throw 'An older restricted server is running. Use Stop-950Test.cmd, then launch again to load all content.' }
 }
 if ($process.CommandLine.Contains('-Dataraxia.native.verifyCache=false')) { throw 'The running server has cache verification disabled. Stop it and launch again.' }
}
if (!$running) {
 if ($OpenGL) { & (Join-Path $root 'Start-950Server.ps1') }
 else { & (Join-Path $root 'Start-950Server.ps1') -WorkspaceDurabilityGate -WorkspaceCaptureGate -WorkspaceJaxaRollout }
}
$ready=$false
for($attempt=0;$attempt -lt 60;$attempt++) {
 try { $reply=(Invoke-WebRequest 'http://127.0.0.2:8950/jav_config.ws?binaryType=2' -UseBasicParsing -TimeoutSec 2).Content; if($reply -match '(?m)^server_version=950\s*$') {$ready=$true;break} } catch {}
 Start-Sleep -Milliseconds 500
}
if (!$ready) {throw '950server is not ready; inspect logs/server.err.log.'}
& (Join-Path $root 'Start-950Client.ps1') -Vulkan:$Vulkan -OpenGL:$OpenGL


