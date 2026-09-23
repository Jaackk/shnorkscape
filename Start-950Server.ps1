[CmdletBinding()]
param(
 [string]$JavaPath = '',
 # Compatibility only: old shortcuts may still pass these switches. They no longer
 # enable/disable content or bypass cache verification. Every launch uses the full profile.
 [switch]$NoWalk,[switch]$NoRibbon,[switch]$NoSettings,[switch]$NoRegions,
 [switch]$Collision,[switch]$Minimal,[switch]$Ataraxia,[switch]$LumbridgeNpcs,
 [switch]$AllNpcs,[switch]$DevTools,[switch]$UnverifiedCacheBindings,
 [switch]$Walk,[switch]$Ribbon,[switch]$Settings,[switch]$Regions,
 [switch]$WorkspaceDurabilityGate,[switch]$WorkspaceCaptureGate,[switch]$WorkspaceJaxaRollout,
 [string]$LanAddress = ''
)
$ErrorActionPreference = 'Stop'
if ($WorkspaceJaxaRollout -and (-not $WorkspaceDurabilityGate -or -not $WorkspaceCaptureGate)) {
 throw 'Jaxa rollout requires both explicit workspace gates. Normal startup remains unchanged.'
}
if (-not $JavaPath) { $JavaPath = (Join-Path $PSScriptRoot 'runtime\java25\bin\java.exe') }
$root = [IO.Path]::GetFullPath($PSScriptRoot)
$server = Join-Path $root 'OpenNXT'
$log = Join-Path $root 'logs'
$classPath = (Join-Path $root 'patches\classes')+';'+(Join-Path $server 'runtime\lib\*')
$ports = @(80,8950,43650)
# Optional local configuration survives normal Play.cmd server restarts. If the
# approved home interface is unavailable, retain loopback operation only.
$lanConfig=Join-Path $root 'server-home\lan-host.json'
if (-not $LanAddress -and (Test-Path -LiteralPath $lanConfig)) {
 try {
  $lan=Get-Content -LiteralPath $lanConfig -Raw | ConvertFrom-Json
  if ($lan.enabled -eq $true) {
   $adapter=Get-NetAdapter -Name $lan.interfaceAlias -ErrorAction Stop
   $profile=Get-NetConnectionProfile -InterfaceIndex $adapter.ifIndex -ErrorAction Stop
   $assigned=@(Get-NetIPAddress -InterfaceIndex $adapter.ifIndex -AddressFamily IPv4 -ErrorAction Stop | Where-Object {$_.IPAddress -eq $lan.address})
   if ($adapter.Status -ne 'Up' -or $profile.NetworkCategory -ne 'Private' -or $assigned.Count -ne 1) { throw 'Approved Private LAN interface/address is not available.' }
   $LanAddress=[string]$lan.address
  }
 } catch { Write-Warning "LAN disabled; local Play.cmd remains available: $($_.Exception.Message)" }
}
if ($LanAddress) {
 if ($LanAddress -notmatch '^(10\.(\d{1,3}\.){2}\d{1,3}|172\.(1[6-9]|2\d|3[01])\.\d{1,3}\.\d{1,3}|192\.168\.\d{1,3}\.\d{1,3})$') { throw 'LAN mode requires an explicit private IPv4 address.' }
 if (!(Get-NetIPAddress -AddressFamily IPv4 -IPAddress $LanAddress -ErrorAction SilentlyContinue)) { throw 'LAN address is not assigned to this PC.' }
 if (!(Test-Path -LiteralPath (Join-Path $root 'server-home\lan-credentials.properties'))) { throw 'Provision an invited guest with New-LAN-Guest.cmd before starting LAN mode.' }
}
if (!(Test-Path -LiteralPath $JavaPath) -or !(Test-Path -LiteralPath (Join-Path $root 'cache\255'))) { throw 'Bundled Java or the cache is missing. Read README.md: download OpenRS2 cache 2691 (Flat file) and extract its cache folder here.' }
$reference=Join-Path $root 'cache\255\12.dat'
if (!(Test-Path -LiteralPath $reference) -or (Get-FileHash -LiteralPath $reference -Algorithm SHA256).Hash -notin @('8A45E12B3D5B3BF35CDB02CDEC9DDEDBD46200B4FEF086ADC0679FB0D020EF8C','21AAE886E340146ED851949C0F900FAE44208BE899E305E7331C12F1D6F44E89')) {
 throw 'Wrong or incomplete cache. This bundle is paired with OpenRS2 cache 2691 (950.1). Extract its Flat file archive into this folder; see README.md.'
}
$conflicts = @(Get-NetTCPConnection -State Listen -ErrorAction SilentlyContinue | Where-Object {$_.LocalPort -in $ports -and $_.LocalAddress -in @('127.0.0.2','0.0.0.0','::')})
if ($conflicts.Count) {
 $owners = foreach ($group in ($conflicts | Group-Object OwningProcess)) {
  $owner = Get-CimInstance Win32_Process -Filter ("ProcessId = " + $group.Name) -ErrorAction SilentlyContinue
  $description = "PID $($group.Name)"
  if ($owner) {
   $description += " ($($owner.Name))"
   if ($owner.CommandLine -match '(?i)(?:^|\s)-cp\s+"([^";]+)') {
    $firstEntry = $Matches[1]
    if ($firstEntry -match '(?i)\\patches\\classes$') {
     $otherRoot = $firstEntry.Substring(0, $firstEntry.Length - '\patches\classes'.Length)
     $description += " from $otherRoot"
     $stopPath = Join-Path $otherRoot 'Stop.cmd'
     if (!(Test-Path -LiteralPath $stopPath)) { $stopPath = Join-Path $otherRoot 'Stop-950Test.cmd' }
     if (Test-Path -LiteralPath $stopPath) { $description += ". Close that copy using $stopPath" }
    }
   }
  }
  $description + ". Ports: " + (($group.Group.LocalPort | Sort-Object -Unique) -join ', ')
 }
 throw ("Another server is already using the local game ports.`n" + ($owners -join "`n") + "`nThis folder's Stop.cmd only stops this copy. Stop the other copy, then run Play.cmd here.")
}
foreach($dir in @($log,(Join-Path $root 'temp'),(Join-Path $root 'server-home'),(Join-Path $root 'players'))) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }
$values = @{
 OPENNXT_CACHE_PATH = Join-Path $root 'cache'
 OPENNXT_OFFLINE = '1'
 OPENNXT_GAME_BACKEND = 'ataraxia950'
 # The handoff refuses any save path not ending in modern950/players, so a 947-era or legacy
 # save directory cannot be opened by mistake.
 OPENNXT_PLAYER_SAVE_PATH = Join-Path $root 'players\modern950\players'
 OPENNXT_ENABLE_RETAIL_LOGGED_OUT_JS5_PASSTHROUGH = '0'
 OPENNXT_DISABLE_RETAIL_LOGGED_OUT_JS5_PROXY = '1'
 OPENNXT_ENABLE_RETAIL_RAW_CHECKSUM_PASSTHROUGH = '0'
 OPENNXT_DISABLE_CHECKSUM_OVERRIDE = '1'
 OPENNXT_DISABLE_HTTP_CHECKSUM_OVERRIDE = '1'
 OPENNXT_STARTUP_PROBE = '1'
}
$previous = @{}
try {
 foreach($key in $values.Keys) { $previous[$key]=[Environment]::GetEnvironmentVariable($key,'Process'); [Environment]::SetEnvironmentVariable($key,$values[$key],'Process') }
 # One complete, verified gameplay profile. No feature switches are required.
 $features = ' -Dopennxt.950.walk=true -Dopennxt.950.ribbon=true -Dopennxt.950.settings=true -Dopennxt.950.regions=true -Dopennxt.950.collision=true -Dataraxia950.npcSpawns=true -Dataraxia950.npcRegions= -Dataraxia950.devTools=true -Dataraxia950.worldMap=true -Dataraxia.native.verifyCache=true'
 $features += ' -Dataraxia950.devAccounts=jaxa,nooby -Dataraxia950.lanDevAccounts=nooby'
 if ($LanAddress) {
  $features += ' -Dopennxt.lan.address=' + $LanAddress
  $features += ' "-Dopennxt.lan.credentials=' + (Join-Path $root 'server-home\lan-credentials.properties') + '"'
 }
 if ($WorkspaceDurabilityGate) { $features += ' -Dataraxia950.layoutDurabilityGate=true' }
 if ($WorkspaceJaxaRollout) { $features += ' -Dataraxia950.workspaceJaxaRollout=true' }
 if ($WorkspaceCaptureGate) {
  if (-not $WorkspaceDurabilityGate) { throw 'Capture gate requires the disposable durability gate.' }
  $features += ' -Dataraxia950.workspaceCaptureGate=true'
 }
 $dataRoot = Join-Path $root 'Ataraxia950\data'
 if (!(Test-Path -LiteralPath (Join-Path $dataRoot 'npcs\spawns.json'))) {
   throw "The Ataraxia backend needs its data root; $dataRoot has no npcs\spawns.json."
 }
 $features += ' "-Dataraxia950.data=' + $dataRoot + '"'
 Write-Host '950 backend: Ataraxia950'
 Write-Host '950 content: all implemented gameplay, interfaces, collision, world regions, NPC spawns and local development commands'
 $argsText = '--enable-native-access=ALL-UNNAMED'+$features+' -Xmx4g -Dorg.slf4j.simpleLogger.log.com.opennxt.net.js5.Js5Session=warn -Dorg.slf4j.simpleLogger.log.com.opennxt.net.http.HttpRequestHandler=warn -Dorg.slf4j.simpleLogger.log.com.opennxt.net.http.endpoints.Js5MsEndpoint=warn "-Djava.io.tmpdir='+(Join-Path $root 'temp')+'" "-Duser.home='+(Join-Path $root 'server-home')+'" -cp "'+$classPath+'" com.opennxt.MainKt run-server --skip-http-file-verification'
 # Always validate the paired cache before login can reach the content.
 $previousPreference=$ErrorActionPreference
 $ErrorActionPreference='Continue'
 try {
   & $JavaPath --enable-native-access=ALL-UNNAMED -Xmx2g ("-Djava.io.tmpdir="+(Join-Path $root 'temp')) -cp $classPath com.rs.tools.modern.Native950CachePreflight (Join-Path $root 'cache') *> (Join-Path $log 'cache-preflight.log')
   $preflightExit=$LASTEXITCODE
 } finally { $ErrorActionPreference=$previousPreference }
 if ($preflightExit -ne 0) {
   Get-Content -LiteralPath (Join-Path $log 'cache-preflight.log') -Tail 14
   throw '950 cache preflight failed before server start. See logs/cache-preflight.log.'
 }
 $started = Start-Process -FilePath $JavaPath -ArgumentList $argsText -WorkingDirectory $server -WindowStyle Hidden -PassThru -RedirectStandardOutput (Join-Path $log 'server.out.log') -RedirectStandardError (Join-Path $log 'server.err.log')
} finally { foreach($key in $previous.Keys) { [Environment]::SetEnvironmentVariable($key,$previous[$key],'Process') } }
$info = Get-CimInstance Win32_Process -Filter "ProcessId = $($started.Id)"
if (!$info) { throw '950 server exited. See logs/server.err.log.' }
[pscustomobject]@{ProcessId=$started.Id;Workspace=$root;CreatedUtc=$info.CreationDate.ToUniversalTime().ToString('o');JavaPath=$JavaPath;ClassPath=$classPath} | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $log 'server.pid.json') -Encoding UTF8
Write-Host "950 test server started: PID $($started.Id). Test ports8950 /43650. See logs for readiness."




