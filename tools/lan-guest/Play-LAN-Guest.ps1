$ErrorActionPreference='Stop'
$root=[IO.Path]::GetFullPath($PSScriptRoot).TrimEnd('\')
if ($root -ne 'C:\Games\950OpenSource') { throw 'Extract this guest bundle to C:\Games\950OpenSource; the existing isolated client is pinned to that path.' }
if (Test-Path (Join-Path $root 'OpenNXT')) { throw 'This guest launcher is for the separate guest PC, not the production server folder.' }
$exe=Join-Path $root 'client\rs2client-vulkan.exe'
if ((Get-FileHash -LiteralPath $exe -Algorithm SHA256).Hash -ne '36c45c1cf6eed0c6cb0b789ca1672d685c1746def9d65d6f18d72638f0087bc9') { throw 'Guest client hash mismatch. Stop; do not bypass Windows security.' }
$address=Read-Host 'Host PC private LAN IPv4 address'
if ($address -notmatch '^(10\.(\d{1,3}\.){2}\d{1,3}|172\.(1[6-9]|2\d|3[01])\.\d{1,3}\.\d{1,3}|192\.168\.\d{1,3}\.\d{1,3})$') { throw 'Use the host private IPv4 address, not localhost or a public address.' }
$uri="http://${address}:8950/jav_config.ws?binaryType=2"
$config=(Invoke-WebRequest -Uri $uri -UseBasicParsing -TimeoutSec 10).Content
if ($config -notmatch '(?m)^server_version=950\s*$' -or $config -notmatch ('(?m)^param=3='+[regex]::Escape($address)+'\s*$')) { throw 'Host did not advertise the expected revision950 LAN endpoint.' }
foreach($name in @('client-state','temp')) { New-Item -ItemType Directory -Path (Join-Path $root $name) -Force | Out-Null }
# Ordinary manual Windows launch. No injection, binary patch, workspace DLL or security bypass.
Start-Process -FilePath $exe -ArgumentList ('"'+$uri+'"') -WorkingDirectory (Join-Path $root 'client')
