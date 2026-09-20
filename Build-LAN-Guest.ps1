$ErrorActionPreference='Stop'
$source=Join-Path $PSScriptRoot 'client\rs2client-vulkan.exe'
$hash='36c45c1cf6eed0c6cb0b789ca1672d685c1746def9d65d6f18d72638f0087bc9'
if ((Get-FileHash -LiteralPath $source -Algorithm SHA256).Hash -ne $hash) { throw 'Known guest Vulkan seed changed.' }
$out=Join-Path $PSScriptRoot ('dist\lan-guest-'+(Get-Date -Format 'yyyyMMdd-HHmmss'))
if (Test-Path -LiteralPath $out) { throw 'Output already exists; no existing bundle will be overwritten.' }
New-Item -ItemType Directory -Path (Join-Path $out 'client') -Force | Out-Null
Copy-Item -LiteralPath $source -Destination (Join-Path $out 'client\rs2client-vulkan.exe')
foreach($name in @('Play-LAN-Guest.cmd','Play-LAN-Guest.ps1')) {
 Copy-Item -LiteralPath (Join-Path $PSScriptRoot ('tools\lan-guest\'+$name)) -Destination (Join-Path $out $name)
}
Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'docs\LAN-GUEST-CANDIDATE.md') -Destination (Join-Path $out 'README.md')
Compress-Archive -Path (Join-Path $out '*') -DestinationPath ($out+'.zip')
Write-Host "Guest bundle: $out.zip (no accounts, credentials, saves, cache, workspace snapshots or diagnostic DLLs)."
