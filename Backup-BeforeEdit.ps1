[CmdletBinding()]
param([Parameter(Mandatory = $true)][string] $Task)

$ErrorActionPreference = 'Stop'
function Invoke-Git {
    param([string[]] $Arguments)
    $output = & git @Arguments
    if ($LASTEXITCODE -ne 0) { throw "Git failed: $($Arguments -join ' ')" }
    return $output
}

Push-Location $PSScriptRoot
try {
    $remote = Invoke-Git -Arguments @('remote', 'get-url', 'origin')
    if ($remote -ne 'https://github.com/Jaackk/shnorkscape.git') {
        throw 'Unexpected backup remote. Stop and inspect before uploading.'
    }
    $branch = Invoke-Git -Arguments @('symbolic-ref', '--quiet', '--short', 'HEAD')
    if (-not $branch) { throw 'Detached HEAD: select the intended branch first.' }

    $backup = Join-Path $PSScriptRoot ('backups\pre-edit-' + (Get-Date -Format 'yyyyMMdd-HHmmss-fff'))
    New-Item -ItemType Directory -Path $backup | Out-Null
    # These private/runtime files deliberately never enter GitHub history.
    foreach ($relative in @('players', 'OpenNXT\runtime\lib\ataraxia-950-1.0-UNTRACKED.jar', 'patches\classes')) {
        $source = Join-Path $PSScriptRoot $relative
        if (Test-Path -LiteralPath $source) {
            $target = Join-Path $backup $relative
            New-Item -ItemType Directory -Path (Split-Path -Parent $target) -Force | Out-Null
            Copy-Item -LiteralPath $source -Destination $target -Recurse
        }
    }

    Invoke-Git -Arguments @('add', '-A')
    $staged = Invoke-Git -Arguments @('diff', '--cached', '--name-only')
    if ($staged) {
        Invoke-Git -Arguments @('commit', '--quiet', '-m', "Pre-edit backup: $Task")
    }
    Invoke-Git -Arguments @('-c', 'credential.interactive=never', 'push', '-u', 'origin', $branch)
    $local = Invoke-Git -Arguments @('rev-parse', 'HEAD')
    $remoteRef = Invoke-Git -Arguments @('-c', 'credential.interactive=never', 'ls-remote', '--exit-code', 'origin', "refs/heads/$branch")
    $remoteHash = ($remoteRef -split '\s+')[0]
    if ($remoteHash -ne $local) { throw 'Remote verification failed. Do not edit.' }
    Write-Host "Verified GitHub backup: $branch $local"
    Write-Host "Local saves/runtime snapshot: $backup"
    Write-Host 'Before editing other ignored files, copy them into this snapshot too.'
} finally {
    Pop-Location
}
