[CmdletBinding()]
param([string]$InterfaceAlias='Ethernet',[string]$LanAddress='192.168.0.91',[switch]$RequestElevation)
$ErrorActionPreference='Stop'
$principal=[Security.Principal.WindowsPrincipal]::new([Security.Principal.WindowsIdentity]::GetCurrent())
if (-not $principal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) {
 if (-not $RequestElevation) { throw 'Run Enable-Home-LAN.cmd for normal Windows administrator approval.' }
 $shell=Join-Path $env:SystemRoot 'System32\WindowsPowerShell\v1.0\powershell.exe'
 $arguments='-NoProfile -NoExit -File "'+$PSCommandPath+'" -InterfaceAlias "'+$InterfaceAlias+'" -LanAddress "'+$LanAddress+'"'
 Write-Host 'Requesting Windows administrator approval. The setup window will stay open to show its result.'
 Start-Process -FilePath $shell -Verb RunAs -ArgumentList $arguments -Wait
 return
}
$root=$PSScriptRoot
$java=Join-Path $root 'runtime\java25\bin\java.exe'
if (-not (Test-Path -LiteralPath $java)) { throw 'Bundled server Java missing.' }
$adapter=Get-NetAdapter -Name $InterfaceAlias -ErrorAction Stop
if ($adapter.Status -ne 'Up' -or -not $adapter.HardwareInterface) { throw 'Select an active physical home-network adapter.' }
if (-not (Get-NetIPAddress -InterfaceIndex $adapter.ifIndex -IPAddress $LanAddress -AddressFamily IPv4 -ErrorAction SilentlyContinue)) { throw 'Address is not assigned to the selected adapter.' }
if ($LanAddress -notmatch '^(10\.|192\.168\.|172\.(1[6-9]|2\d|3[01])\.)') { throw 'Private IPv4 required.' }
$profile=Get-NetConnectionProfile -InterfaceIndex $adapter.ifIndex
$backup=Join-Path $root ('backups\lan-network-'+(Get-Date -Format 'yyyyMMdd-HHmmss')+'.json')
@{interfaceAlias=$InterfaceAlias;originalCategory=[string]$profile.NetworkCategory;address=$LanAddress} | ConvertTo-Json | Set-Content -LiteralPath $backup
$name='SHNORKSCAPE-Private-LAN-TCP'
if (Get-NetFirewallRule -Name $name -ErrorAction SilentlyContinue) { throw 'Rule already exists: review it rather than replacing it automatically.' }
Set-NetConnectionProfile -InterfaceIndex $adapter.ifIndex -NetworkCategory Private
New-NetFirewallRule -Name $name -DisplayName 'SHNORKSCAPE private home LAN' -Direction Inbound -Action Allow -Enabled True -Profile Private -Program $java -Protocol TCP -LocalPort 80,8950,43650 -LocalAddress $LanAddress -RemoteAddress LocalSubnet -InterfaceAlias $InterfaceAlias | Out-Null
@{enabled=$true;interfaceAlias=$InterfaceAlias;address=$LanAddress} | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $root 'server-home\lan-host.json')
Write-Host 'Private LAN rule/config ready. Restart SHNORKSCAPE after closing the client. No router settings changed.'
