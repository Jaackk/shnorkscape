# Starts the complete950 gameplay profile; legacy feature arguments are compatibility-only.
param([switch]$NoWalk,[switch]$NoRibbon,[switch]$NoSettings,[switch]$NoRegions,
 [switch]$Collision,[switch]$Ataraxia,[switch]$LumbridgeNpcs,[switch]$AllNpcs,[switch]$DevTools,[switch]$Minimal,[switch]$Vulkan,[switch]$OpenGL,
 [switch]$UnverifiedCacheBindings,
 [switch]$Walk,[switch]$Ribbon,[switch]$Settings,[switch]$Regions)
$ErrorActionPreference='Stop'
$root=[IO.Path]::GetFullPath($PSScriptRoot)
$recordPath=Join-Path $root 'logs\server.pid.json'
$running=$false
if (Test-Path -LiteralPath $recordPath) {
 $record=Get-Content -LiteralPath $recordPath -Raw | ConvertFrom-Json
 $process=Get-CimInstance Win32_Process -Filter "ProcessId = $($record.ProcessId)"
 $running=$null -ne $process -and $record.Workspace -eq $root -and $process.ExecutablePath -eq $record.JavaPath -and $process.CreationDate.ToUniversalTime().Ticks -eq ([datetime]$record.CreatedUtc).ToUniversalTime().Ticks -and $process.CommandLine.Contains($record.ClassPath)
}
if ($running) {
 # Never silently reuse a restricted server from an older launcher.
 foreach($required in @('-Dopennxt.950.walk=true','-Dopennxt.950.ribbon=true','-Dopennxt.950.settings=true','-Dopennxt.950.regions=true','-Dopennxt.950.collision=true','-Dataraxia950.npcSpawns=true','-Dataraxia950.npcRegions= ','-Dataraxia950.devTools=true','-Dataraxia950.worldMap=true')) {
  if (!$process.CommandLine.Contains($required)) { throw 'An older restricted server is running. Use Stop-950Test.cmd, then launch again to load all content.' }
 }
 if ($process.CommandLine.Contains('-Dataraxia.native.verifyCache=false')) { throw 'The running server has cache verification disabled. Stop it and launch again.' }
}
if (!$running) { & (Join-Path $root 'Start-950Server.ps1') }
$ready=$false
for($attempt=0;$attempt -lt 60;$attempt++) {
 try { $reply=(Invoke-WebRequest 'http://127.0.0.2:8950/jav_config.ws?binaryType=2' -UseBasicParsing -TimeoutSec 2).Content; if($reply -match '(?m)^server_version=950\s*$') {$ready=$true;break} } catch {}
 Start-Sleep -Milliseconds 500
}
if (!$ready) {throw '950server is not ready; inspect logs/server.err.log.'}
& (Join-Path $root 'Start-950Client.ps1') -Vulkan:$Vulkan -OpenGL:$OpenGL


