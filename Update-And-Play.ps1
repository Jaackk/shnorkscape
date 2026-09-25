[CmdletBinding()]
param([switch]$CheckOnly,[switch]$OpenGL)
$ErrorActionPreference = 'Stop'

function Get-950UpdateSession([string]$Root) {
    $processes = @(Get-CimInstance Win32_Process | Where-Object { $_.Name -in @('java.exe','javaw.exe') -or $_.Name -like 'rs2client*' })
    $server = $null
    $lanAddress = ''
    $recordPath = Join-Path $Root 'logs\server.pid.json'
    if (Test-Path -LiteralPath $recordPath) {
        $record = Get-Content -LiteralPath $recordPath -Raw | ConvertFrom-Json
        if ($record.Workspace -ne $Root) { throw 'Server record belongs to another folder. Nothing stopped.' }
        $server = $processes | Where-Object { $_.ProcessId -eq $record.ProcessId } | Select-Object -First 1
        if ($record.LanAddress) { $lanAddress = [string]$record.LanAddress }
        if ($server) {
            if ($server.ExecutablePath -ne $record.JavaPath -or
                $server.CreationDate.ToUniversalTime().Ticks -ne ([datetime]$record.CreatedUtc).ToUniversalTime().Ticks -or
                !$record.ClassPath -or !$server.CommandLine.Contains($record.ClassPath)) {
                throw 'Server identity does not match its launch record. Nothing stopped.'
            }
            # Older records did not include LAN mode. Recover only this validated
            # JVM's address; never copy or print its complete command line.
            if ($server.CommandLine -match '(?:^|\s)-Dopennxt\.lan\.address=([0-9.]+)(?:\s|$)') { $lanAddress = $Matches[1] }
        }
    }
    $clients = @($processes | Where-Object {
        $_.Name -like 'rs2client*' -and $_.ExecutablePath -and
        $_.ExecutablePath.StartsWith($Root+'\',[StringComparison]::OrdinalIgnoreCase)
    })
    $ownedIds = @($clients | ForEach-Object { $_.ProcessId })
    if ($server) { $ownedIds += $server.ProcessId }
    $unrelated = @($processes | Where-Object { $_.ProcessId -notin $ownedIds })
    if ($unrelated.Count) {
        $names = ($unrelated | ForEach-Object { "$($_.Name) (PID $($_.ProcessId))" }) -join ', '
        throw "The update installer requires Java/game processes closed. Close these separately first: $names. They have not been stopped."
    }
    if ($lanAddress) {
        $parsed = $null
        if (![Net.IPAddress]::TryParse($lanAddress,[ref]$parsed) -or $parsed.AddressFamily -ne [Net.Sockets.AddressFamily]::InterNetwork) {
            throw 'The saved LAN address is invalid. Nothing stopped.'
        }
    }
    return [pscustomobject]@{Server=$server;Clients=$clients;LanAddress=$lanAddress}
}

function Close-950UpdateClients($Clients) {
    foreach ($client in $Clients) {
        $current = Get-CimInstance Win32_Process -Filter "ProcessId = $($client.ProcessId)"
        if (!$current) { continue }
        if ($current.ExecutablePath -ne $client.ExecutablePath -or $current.CreationDate -ne $client.CreationDate) {
            throw 'Client identity changed during update preparation. Nothing installed.'
        }
        $tracked = Get-Process -Id $client.ProcessId -ErrorAction SilentlyContinue
        if (!$tracked) { continue }
        $null = $tracked.CloseMainWindow()
        if (!$tracked.WaitForExit(10000)) {
            Stop-Process -InputObject $tracked -ErrorAction Stop
            if (!$tracked.WaitForExit(10000)) { throw 'A bundle client did not exit. Update cancelled.' }
        }
    }
}

function Invoke-950UpdateAndPlay([string]$Root,[switch]$CheckOnly,[switch]$OpenGL) {
    $Root = [IO.Path]::GetFullPath($Root)
    . (Join-Path $Root 'Client-LaunchLock.ps1')
    $lock = Enter-950ClientLock $Root
    try {
        Write-Host 'Checking the staged update before stopping anything...'
        & (Join-Path $Root 'Apply-PlayabilityUpdate.ps1') -CheckOnly
        $session = Get-950UpdateSession $Root
        if ($CheckOnly) { Write-Host 'Update and Play checks passed. Nothing stopped, installed or launched.'; return }
        Write-Host 'Closing this bundle''s game clients...'
        Close-950UpdateClients $session.Clients
        if ($session.Server) {
            # Leave the server alive while its disconnected clients finish their
            # normal logout/checkpoint path. Never kill an unseen LAN session.
            for ($attempt=0; $attempt -lt 30; $attempt++) {
                $connections = @(Get-NetTCPConnection -State Established -ErrorAction SilentlyContinue | Where-Object {
                    $_.OwningProcess -eq $session.Server.ProcessId -and $_.LocalPort -eq 43650
                })
                if (!$connections.Count) { break }
                Start-Sleep -Milliseconds 500
            }
            if ($connections.Count) { throw 'Another game client is still connected. Log it out and run Update and Play again; the server and update files were left untouched.' }
            Start-Sleep -Seconds 2
        }
        Write-Host 'Stopping the server...'
        & (Join-Path $Root 'Stop-950Test.ps1')
        Write-Host 'Applying the verified update (with the existing rollback backup)...'
        & (Join-Path $Root 'Apply-PlayabilityUpdate.ps1')
        Write-Host 'Starting the server and waiting for readiness...'
        if ($session.LanAddress) {
            if ($OpenGL) { & (Join-Path $Root 'Start-950Server.ps1') -LanAddress $session.LanAddress }
            else { & (Join-Path $Root 'Start-950Server.ps1') -LanAddress $session.LanAddress -WorkspaceDurabilityGate -WorkspaceCaptureGate -WorkspaceJaxaRollout }
        }
        & (Join-Path $Root 'Start-950Test.ps1') -OpenGL:$OpenGL
        Write-Host 'Update complete. The game client is open.' -ForegroundColor Green
    } finally { $lock.ReleaseMutex(); $lock.Dispose() }
}

if ($MyInvocation.InvocationName -ne '.') {
    try { Invoke-950UpdateAndPlay -Root $PSScriptRoot -CheckOnly:$CheckOnly -OpenGL:$OpenGL }
    catch { Write-Host $_.Exception.Message -ForegroundColor Red; Write-Host 'Stopped at the failed step. See the logs folder for details.'; exit 1 }
}
