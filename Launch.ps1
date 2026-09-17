param([switch]$OpenGL)
$ErrorActionPreference='Stop'
try {
 & (Join-Path $PSScriptRoot 'Start-950Test.ps1') -OpenGL:$OpenGL
 Write-Host 'Client opened. Enter a local username and disposable password, select World 1, then Play Now.'
} catch {
 Write-Host $_.Exception.Message -ForegroundColor Red
 Write-Host 'Read README.md and the logs folder for help.'
 Read-Host 'Press Enter to close'
 exit 1
}
