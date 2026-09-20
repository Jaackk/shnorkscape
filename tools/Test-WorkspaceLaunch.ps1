$ErrorActionPreference='Stop'
$root=Split-Path $PSScriptRoot -Parent
. (Join-Path $root 'Client-WorkspaceLaunch.ps1')
$checks=0
function Check([bool]$value,[string]$name) {
 if (!$value) { throw "FAIL: $name" }; $script:checks++
}
function Refuses([scriptblock]$action,[string]$name) {
 $refused=$false
 try { & $action | Out-Null } catch { $refused=$true }
 Check $refused $name
}
# Reads actual pins; never starts a server/client or edits an asset.
$profile=Get-950WorkspaceLaunchProfile $root
Check ($profile.ExecutablePath -eq (Join-Path $root 'client\rs2client-vulkan-workspace-diag-v5.exe')) 'V5 selected'
Refuses { Get-950WorkspaceLaunchProfile 'C:\OtherBundle' } 'relocation refused'
$script:badSuffix=''
function Get-FileHash {
 param($LiteralPath,$Algorithm)
 if ($script:badSuffix -and $LiteralPath.EndsWith($script:badSuffix)) { return [pscustomobject]@{Hash='bad'} }
 Microsoft.PowerShell.Utility\Get-FileHash -LiteralPath $LiteralPath -Algorithm $Algorithm
}
try {
 foreach($suffix in @('rs2client-vulkan-workspace-diag-v5.exe','shnork_workspace_probe_v5.dll','snapshot-schema-v4.json')) {
  $script:badSuffix=$suffix
  Refuses { Get-950WorkspaceLaunchProfile $root } "changed pin refused: $suffix"
 }
} finally { Remove-Item Function:\Get-FileHash; $script:badSuffix='' }
$flags=@('layoutDurabilityGate','workspaceCaptureGate','workspaceJaxaRollout') | ForEach-Object { '-Dataraxia950.'+$_+'=true' }
Assert-950WorkspaceServerFlags ('java '+($flags -join ' ')+' -cp example')
$checks++
foreach($flag in $flags) {
 Refuses { Assert-950WorkspaceServerFlags (($flags | Where-Object {$_ -ne $flag}) -join ' ') } "missing $flag"
 Refuses { Assert-950WorkspaceServerFlags (($flags -join ' ').Replace($flag,$flag+'extra')) } "partial match $flag"
 Refuses { Assert-950WorkspaceServerFlags (($flags -join ' ')+' '+$flag.Replace('=true','=false')) } "duplicate override $flag"
}
foreach($file in @('Client-WorkspaceLaunch.ps1','Start-950Client.ps1','Start-950Test.ps1','Launch.ps1')) {
 $tokens=$null; $errors=$null
 $null=[Management.Automation.Language.Parser]::ParseFile((Join-Path $root $file),[ref]$tokens,[ref]$errors)
 Check ($errors.Count -eq 0) "syntax $file"
}
$start=Get-Content (Join-Path $root 'Start-950Test.ps1') -Raw
Check ($start.Contains('-WorkspaceDurabilityGate -WorkspaceCaptureGate -WorkspaceJaxaRollout')) 'fresh server requests all flags'
Check ($start.Contains('Assert-950WorkspaceServerFlags $process.CommandLine')) 'reused server checked'
$client=Get-Content (Join-Path $root 'Start-950Client.ps1') -Raw
Check ($client.Contains('Get-950WorkspaceLaunchProfile $root')) 'client uses pinned selector'
Check (!$client.Contains('falling back to the OpenGL client')) 'no silent capture loss'
Write-Host "Workspace launch checks passed: $checks (no process launched)."
