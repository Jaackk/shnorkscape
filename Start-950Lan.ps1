$ErrorActionPreference='Stop'
$address=Read-Host 'This PC private LAN IPv4 address (see ipconfig)'
& (Join-Path $PSScriptRoot 'Start-950Server.ps1') -Ataraxia -WorkspaceDurabilityGate -WorkspaceCaptureGate -WorkspaceJaxaRollout -LanAddress $address
Write-Host 'LAN server started. Use normal Play.cmd locally. No firewall or router rule was changed.'
