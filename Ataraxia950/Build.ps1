[CmdletBinding()]
param([string[]]$Tasks=@('test','jar'), [string]$JavaHome, [switch]$Offline)
$arguments=@{Tasks=$Tasks}
if($JavaHome){$arguments.JavaHome=$JavaHome}
& (Join-Path (Split-Path -Parent $PSScriptRoot) 'Build-Ataraxia950.ps1') @arguments
