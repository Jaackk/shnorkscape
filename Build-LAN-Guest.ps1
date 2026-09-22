param([Parameter(Mandatory=$true)][string]$LanAddress)
$ErrorActionPreference='Stop'
$parsed=$null
if (-not [Net.IPAddress]::TryParse($LanAddress,[ref]$parsed) -or $parsed.AddressFamily -ne 'InterNetwork' -or $LanAddress -notmatch '^(10\.|192\.168\.|172\.(1[6-9]|2\d|3[01])\.)') { throw 'A private IPv4 host address is required.' }
$source=Join-Path $PSScriptRoot 'client\rs2client-vulkan.exe'
$hash='36c45c1cf6eed0c6cb0b789ca1672d685c1746def9d65d6f18d72638f0087bc9'
if ((Get-FileHash -LiteralPath $source -Algorithm SHA256).Hash -ne $hash) { throw 'Known guest Vulkan seed changed.' }
$out=Join-Path $PSScriptRoot ('dist\lan-guest-'+(Get-Date -Format 'yyyyMMdd-HHmmss'))
if (Test-Path -LiteralPath $out) { throw 'Output already exists; no existing bundle will be overwritten.' }
New-Item -ItemType Directory -Path (Join-Path $out 'client') -Force | Out-Null
# Change only the existing storage string in a separate guest derivative.
$bytes=[IO.File]::ReadAllBytes($source)
$offset=0xd92348
$old=[Text.Encoding]::Unicode.GetBytes("C:\Games\950OpenSource\client-state`0")
$replacement=[Text.Encoding]::Unicode.GetBytes(".\client-state`0")
for($i=0;$i -lt $old.Length;$i++) { if($bytes[$offset+$i] -ne $old[$i]) { throw 'Storage string signature changed.' } }
for($i=0;$i -lt $old.Length;$i++) { $bytes[$offset+$i]=0 }
[Array]::Copy($replacement,0,$bytes,$offset,$replacement.Length)
$guest=Join-Path $out 'client\rs2client-vulkan.exe'
[IO.File]::WriteAllBytes($guest,$bytes)
if ((Get-FileHash -LiteralPath $guest -Algorithm SHA256).Hash -ne 'b6a7e8688625198a69aa31fdd70f7b8cc9e82b1ac0b4f99cd71d5657f51491d5') { throw 'Portable guest output hash mismatch.' }
$launcher=(Get-Content -LiteralPath (Join-Path $PSScriptRoot 'tools\lan-guest\Play-LAN-Guest.cmd') -Raw).Replace('@LAN_ADDRESS@',$LanAddress)
[IO.File]::WriteAllText((Join-Path $out 'Play SHNORKSCAPE.cmd'),($launcher -replace '\r?\n',"`r`n"),[Text.Encoding]::ASCII)
Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'tools\lan-guest\README-FIRST.txt') -Destination (Join-Path $out 'README-FIRST.txt')
Compress-Archive -Path (Join-Path $out '*') -DestinationPath ($out+'.zip')
Write-Host "Guest bundle: $out.zip (no accounts, credentials, saves, cache, workspace snapshots or diagnostic DLLs)."
