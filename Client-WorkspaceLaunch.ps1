# Shared, read-only preflight for the live-proven V5 production launch.
function Get-950WorkspaceLaunchProfile([string]$Root) {
 $rootPath=[IO.Path]::GetFullPath($Root).TrimEnd('\')
 if ($rootPath -ne 'C:\Games\950OpenSource') {
  throw 'The proven V5 build is pinned to C:\Games\950OpenSource. Do not relocate or silently fall back.'
 }
 $pins=@{
  'client\rs2client-vulkan-workspace-diag-v5.exe'='19323515092bbccd0090033badbc56177d0ca599216e4d702be27432cf693a13'
  'client\shnork_workspace_probe_v5.dll'='1df5b7a0f33c94932c4ecdbacd9b65ec23b8e4539e6d591d797d34c891a404c6'
  'tools\vulkan-static-probe\snapshot-schema-v4.json'='53c06c70b12ba0f68ff71a3e46db4c46fdc7f3a3792e3ce6c2656066e692119d'
 }
 foreach($relative in $pins.Keys) {
  $path=Join-Path $rootPath $relative
  if (!(Test-Path -LiteralPath $path -PathType Leaf) -or
      (Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash -ne $pins[$relative]) {
   throw "Workspace launch preflight failed: $relative is missing or has changed. Stop; do not bypass security or substitute another client."
  }
 }
 [pscustomobject]@{ExecutablePath=(Join-Path $rootPath 'client\rs2client-vulkan-workspace-diag-v5.exe')}
}

function Assert-950WorkspaceServerFlags([string]$CommandLine) {
 foreach($key in @('layoutDurabilityGate','workspaceCaptureGate','workspaceJaxaRollout')) {
  $flag='-Dataraxia950.'+$key+'='
  $matches=[regex]::Matches($CommandLine,'(?:^|\s)'+[regex]::Escape($flag)+'([^\s]+)')
  if ($matches.Count -ne 1 -or $matches[0].Groups[1].Value -cne 'true') {
   throw 'The running server lacks the proven Jaxa workspace launch profile. Close the client, use Stop.cmd, then Play.cmd. No automatic restart was attempted.'
  }
 }
}
