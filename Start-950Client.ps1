param([switch]$Vulkan,[switch]$OpenGL)
$ErrorActionPreference='Stop'
$root=[IO.Path]::GetFullPath($PSScriptRoot)
. (Join-Path $root 'Client-LaunchLock.ps1')
$launchMutex=Enter-950ClientLock $root
try {
# Default Vulkan uses the unchanged, pinned V5 image and DLL, retaining their
# existing filenames because the authenticated server verifies the exact path.
# The original Vulkan executable remains untouched. Explicit -OpenGL selects
# the legacy renderer without V5 automatic capture; there is no silent fallback.
# The jav_config request stays binaryType=2 for both: the server resolves that parameter through
# BinaryType.values()[n], whose enum stops at MOBILE(7), so 10 would throw. The config is
# revision-specific rather than renderer-specific, so WIN64's is correct for either binary.
if ($Vulkan -and $OpenGL) {throw 'Choose one renderer: -Vulkan or -OpenGL, not both.'}
$useVulkan = -not $OpenGL
if ($useVulkan) {
 . (Join-Path $root 'Client-WorkspaceLaunch.ps1')
 $exe=(Get-950WorkspaceLaunchProfile $root).ExecutablePath
}
if (!$useVulkan) {
 Write-Warning 'Explicit OpenGL fallback: automatic V5 workspace capture is unavailable.'
 & (Join-Path $root 'Initialize-950Client.ps1') -Root $root -OpenGL
 $exe=Join-Path $root 'client\rs2client.exe'
 $reportName='logs\client-isolation.json'
 $report=Get-Content -LiteralPath (Join-Path $root $reportName) -Raw | ConvertFrom-Json
 if ($report.storage_root -ne (Join-Path $root 'client-state')) {throw 'Test folder moved: rerun the isolation patcher before launching.'}
 if ((Get-FileHash -LiteralPath $exe -Algorithm SHA256).Hash -ne $report.isolated_sha256) {throw 'Test client hash mismatch.'}
}
Write-Host ('950 renderer: ' + $(if ($useVulkan) {'Vulkan (binaryType 10)'} else {'OpenGL (binaryType 2)'}))
$exeName=[IO.Path]::GetFileName($exe)
$existing=@(Get-CimInstance Win32_Process -Filter "Name = '$exeName'" | Where-Object {$_.ExecutablePath -eq $exe})
if ($existing.Count) {Write-Host 'The950 test client is already open.'; return}
& (Join-Path $root 'Prepare-ClientCache.ps1')
$uri='http://127.0.0.2:8950/jav_config.ws?binaryType=2&baseConfigSource=patched&localRewrite=1&hostRewrite=0&lobbyHostRewrite=1&gameHostOverride=127.0.0.2&gamePortOverride=43650&contentRouteRewrite=0&worldUrlRewrite=0&codebaseRewrite=0&downloadMetadataSource=patched'
$config=(Invoke-WebRequest -Uri $uri -UseBasicParsing).Content
if ($config -notmatch '(?m)^server_version=950\s*$') {throw 'The test endpoint is not serving revision950.'}
$oldTemp=$env:TEMP
$oldTmp=$env:TMP
try {
 $env:TEMP=Join-Path $root 'temp'
 $env:TMP=$env:TEMP
 $client=Start-Process -FilePath $exe -ArgumentList ('"'+$uri+'"') -WorkingDirectory (Join-Path $root 'client') -PassThru
} finally {$env:TEMP=$oldTemp; $env:TMP=$oldTmp}
$info=Get-CimInstance Win32_Process -Filter "ProcessId = $($client.Id)"
if (!$info) {throw '950client exited immediately.'}
[pscustomobject]@{ProcessId=$client.Id;Workspace=$root;CreatedUtc=$info.CreationDate.ToUniversalTime().ToString('o');ExecutablePath=$exe} | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $root 'logs\client.pid.json') -Encoding UTF8
Write-Host ("950 test client started: PID {0} ({1})." -f $client.Id, $(if ($useVulkan) {'Vulkan'} else {'OpenGL'}))


} finally { $launchMutex.ReleaseMutex(); $launchMutex.Dispose() }
