[CmdletBinding()]
param([switch]$CheckOnly)
$ErrorActionPreference = 'Stop'
$updateRoot = [IO.Path]::GetFullPath($PSScriptRoot)
$manifest = Get-Content -LiteralPath (Join-Path $updateRoot 'protocol-analysis\playability-candidate-20260923.json') -Raw | ConvertFrom-Json
$candidateRoot = Join-Path $updateRoot ('dist\' + $manifest.candidate)
$expectedTargets = @('OpenNXT/runtime/lib/ataraxia-950-1.0-UNTRACKED.jar',
    'patches/classes/com/opennxt/net/login/Native950InterfaceBootstrap.class',
    'patches/classes/com/opennxt/net/login/Native950InterfaceBootstrap$Panel.class',
    'patches/classes/com/opennxt/net/login/Native950InterfaceBootstrap$Slot.class')
$libraryUpdate=$manifest.candidate -in @('library-followup-20260923','p0-bank-focus-20260923')
if ($libraryUpdate) { $expectedTargets += @('cache/12/13903.dat','cache/12/13905.dat','cache/12/13909.dat','cache/12/15897.dat','cache/12/6963.dat','cache/255/12.dat') }
if ($manifest.candidate -notin @('playability-20260923','library-followup-20260923','p0-bank-focus-20260923') -or $manifest.files.Count -ne $expectedTargets.Count) { throw 'Unexpected update manifest.' }
$planned = @()
foreach ($entry in $manifest.files) {
    $expectedSource=if($entry.target.StartsWith('cache/')){'cache-v4/'+$entry.target.Substring(6)}else{[IO.Path]::GetFileName($entry.target)}
    if ($entry.target -notin $expectedTargets -or $entry.source -ne $expectedSource) { throw 'Unexpected update target.' }
    if ($planned | Where-Object { $_.Relative -eq $entry.target }) { throw 'Duplicate update target.' }
    $source = Join-Path $candidateRoot $entry.source
    $target = [IO.Path]::GetFullPath((Join-Path $updateRoot $entry.target))
    if (!$target.StartsWith($updateRoot + '\', [StringComparison]::OrdinalIgnoreCase)) { throw 'Target is outside the workspace.' }
    if ((Get-FileHash -LiteralPath $source -Algorithm SHA256).Hash -ne $entry.sha256) { throw "Candidate hash mismatch: $($entry.source)" }
    if (!(Test-Path -LiteralPath $target -PathType Leaf)) { throw "Expected existing runtime file: $($entry.target)" }
    if($entry.target.StartsWith('cache/')) {
        $currentHash=(Get-FileHash -LiteralPath $target -Algorithm SHA256).Hash
        if(!$entry.beforeSha256 -or $currentHash -notin @($entry.beforeSha256,$entry.sha256)){throw "Cache target differs from the audited base and staged update: $($entry.target)"}
    }
    $planned += [pscustomobject]@{Source=$source;Target=$target;Relative=$entry.target;Hash=$entry.sha256}
}
if ($CheckOnly) { Write-Host "Candidate verified: $($planned.Count) pinned files. Nothing installed or restarted."; return }
$running = Get-Process | Where-Object { $_.ProcessName -in @('java','javaw') -or $_.ProcessName -like 'rs2client*' }
if ($running) { throw 'Close both game clients and stop SHNORKSCAPE with Stop.cmd first. No files were replaced.' }
$backupRoot = Join-Path $updateRoot ('backups\playability-update-' + (Get-Date -Format 'yyyyMMdd-HHmmss-fff'))
New-Item -ItemType Directory -Path $backupRoot -ErrorAction Stop | Out-Null
foreach ($entry in $planned) {
    $backupFile = Join-Path $backupRoot $entry.Relative
    New-Item -ItemType Directory -Path (Split-Path -Parent $backupFile) -Force | Out-Null
    Copy-Item -LiteralPath $entry.Target -Destination $backupFile
    if ((Get-FileHash -LiteralPath $entry.Target).Hash -ne (Get-FileHash -LiteralPath $backupFile).Hash) { throw 'Runtime backup verification failed.' }
}
foreach ($relative in @('players\modern950\players','workspace-state950','client-state\prepared-cache.json','client-state\Jagex\RuneScape\js5-12.jcache')) {
    $source = Join-Path $updateRoot $relative
    if (Test-Path -LiteralPath $source) {
        $destination = Join-Path $backupRoot $relative
        New-Item -ItemType Directory -Path (Split-Path -Parent $destination) -Force | Out-Null
        Copy-Item -LiteralPath $source -Destination $destination -Recurse
    }
}
try {
    foreach ($entry in $planned) {
        Copy-Item -LiteralPath $entry.Source -Destination $entry.Target -Force
        if ((Get-FileHash -LiteralPath $entry.Target).Hash -ne $entry.Hash) { throw "Installed hash mismatch: $($entry.Relative)" }
    }
} catch {
    $installError = $_
    foreach ($entry in $planned) { Copy-Item -LiteralPath (Join-Path $backupRoot $entry.Relative) -Destination $entry.Target -Force }
    throw "Update failed; original runtime files restored. Backup: $backupRoot. $installError"
}
Write-Host "Playability candidate installed. Backup: $backupRoot"
Write-Host 'Start Play.cmd normally. Existing account and LAN settings are preserved.'
if($libraryUpdate){Write-Host 'Play.cmd will refresh the five native library scripts in the client cache. Curated tabs and real bank presets are preserved.'}
