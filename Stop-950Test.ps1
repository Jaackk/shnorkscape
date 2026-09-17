$ErrorActionPreference='Stop'
$root=[IO.Path]::GetFullPath($PSScriptRoot)
$stopped = 0
foreach($kind in @('client','server')) {
 $recordPath=Join-Path $root "logs\$kind.pid.json"
 if (!(Test-Path -LiteralPath $recordPath)) { continue }
 $record=Get-Content -LiteralPath $recordPath -Raw | ConvertFrom-Json
 $process=Get-CimInstance Win32_Process -Filter "ProcessId = $($record.ProcessId)"
 if (!$process) { continue }
 if ($record.Workspace -ne $root -or $process.CreationDate.ToUniversalTime().Ticks -ne ([datetime]$record.CreatedUtc).ToUniversalTime().Ticks) { throw "Identity mismatch for $kind; no process stopped." }
 if ($kind -eq 'server') {
  if ($process.ExecutablePath -ne $record.JavaPath -or !$process.CommandLine.Contains($record.ClassPath)) { throw 'Server identity mismatch.' }
 } elseif (!$process.ExecutablePath.StartsWith($root+'\',[StringComparison]::OrdinalIgnoreCase)) { throw 'Client outside test folder.' }
 $tracked = Get-Process -Id $record.ProcessId -ErrorAction SilentlyContinue
 Stop-Process -Id $record.ProcessId
 if ($tracked -and !$tracked.WaitForExit(10000)) { throw "The $kind process did not exit within ten seconds. Try Stop.cmd again before restarting." }
 $stopped++
 Write-Host "Stopped950 test $kind PID $($record.ProcessId)."
}


if ($stopped -eq 0) { Write-Host "No server or client is running from $root. A server launched from another folder must be stopped using that folder's Stop launcher." }
