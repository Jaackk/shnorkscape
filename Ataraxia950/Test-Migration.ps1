[CmdletBinding()]
param(
    [string] $CachePath,
    [switch] $Offline
)

$ErrorActionPreference = 'Stop'
$taskWorkspace = Split-Path -Parent $PSScriptRoot
. (Join-Path $taskWorkspace 'Portable-Paths.ps1')
$CachePath = Resolve-AstraNxtCachePath -Workspace $taskWorkspace -CachePath $CachePath
$taskJavaHome = Resolve-AstraNxtJavaHome -Workspace $taskWorkspace -Major 8 -RequireJdk
$taskManifest = Get-Content -LiteralPath (Join-Path $PSScriptRoot 'target-947.json') -Raw | ConvertFrom-Json
foreach ($taskArtifact in $taskManifest.artifacts.PSObject.Properties) {
    $taskFile = Join-Path $taskWorkspace $taskArtifact.Value.workspacePath
    $taskHash = (Get-FileHash -LiteralPath $taskFile -Algorithm SHA256).Hash.ToLowerInvariant()
    if ($taskHash -ne $taskArtifact.Value.sha256) {
        throw "Pinned 947 artifact changed: $($taskArtifact.Name). Review the revision evidence before updating target-947.json."
    }
}

& (Join-Path $PSScriptRoot 'Build.ps1') -Tasks @('test', 'installDist', 'writeRuntimeClasspath') -Offline:$Offline
$taskClassPath = (Get-Content -LiteralPath (Join-Path $PSScriptRoot 'build\runtime-classpath.txt') -Raw).Trim()
$taskReport = Join-Path $PSScriptRoot 'build\cache947-report.json'
Push-Location $PSScriptRoot
try {
    $taskOutput = & (Join-Path $taskJavaHome 'bin\java.exe') -Xmx1g -cp $taskClassPath com.rs.tools.modern.CacheMigrationProbe ([IO.Path]::GetFullPath($CachePath))
    if ($LASTEXITCODE -ne 0) { throw 'Modern cache validation failed.' }
    $taskParsed = ($taskOutput -join [Environment]::NewLine) | ConvertFrom-Json
    $taskPinnedCache = Get-Content -LiteralPath (Join-Path $PSScriptRoot 'target-cache.json') -Raw | ConvertFrom-Json
    $taskActualIndexes = @{}
    foreach ($taskIndex in $taskParsed.indexes) { $taskActualIndexes[[string]$taskIndex.index] = $taskIndex.referenceSha256 }
    if ($taskActualIndexes.Count -ne $taskPinnedCache.indexes.Count) { throw 'The cache index set differs from target-cache.json.' }
    foreach ($taskIndex in $taskPinnedCache.indexes) {
        if ($taskActualIndexes[[string]$taskIndex.index] -ne $taskIndex.referenceSha256) {
            throw "Cache index $($taskIndex.index) differs from the pinned snapshot. Review the candidate cache before updating target-cache.json."
        }
    }
    $taskOutput | Set-Content -LiteralPath $taskReport -Encoding UTF8
    Write-Host "947 artifact identities, migration tests, and $($taskParsed.indexes.Count) cache reference tables verified."
    Write-Host "Cache report: $taskReport"
    foreach ($taskProbe in @(
        'com.rs.tools.modern.ModernRegionProbe',
        'com.rs.game.player.client.Native947WorldSmoke',
        'com.rs.game.player.client.Native947InteractionsSmoke',
        'com.rs.game.player.client.Native947NpcSmoke',
        'com.rs.game.player.client.Native947EquipmentSmoke',
        'com.rs.game.player.client.Native947PersistenceSmoke'
    )) {
        & (Join-Path $taskJavaHome 'bin\java.exe') -Xmx2g -cp $taskClassPath $taskProbe ([IO.Path]::GetFullPath($CachePath))
        if ($LASTEXITCODE -ne 0) { throw "947 world validation failed: $taskProbe" }
    }
    Write-Host 'Modern map collision, real Player movement, cipher handoff, NPC visibility and interactions, bank and backpack interactions, equipment and visible appearance, item conservation, local profile persistence across JVM restart, and session cleanup verified.'
    Write-Host 'Use the parent Build-Ataraxia947.ps1 and Start-Ataraxia947.cmd to run the integrated native client.'
} finally {
    Pop-Location
}
