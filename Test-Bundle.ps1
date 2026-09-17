[CmdletBinding()]
param([string]$CachePath='')
$ErrorActionPreference='Stop'
if (-not $CachePath) { $CachePath = (Join-Path $PSScriptRoot 'cache') }
$root=$PSScriptRoot
foreach($relative in @('runtime\java25\bin\java.exe','runtime\java8\bin\java.exe','compiler\lib','OpenNXT\runtime\lib\OpenNXT-1.0.0.jar','OpenNXT\runtime\lib\ataraxia-950-1.0-UNTRACKED.jar','Ataraxia950\data\npcs\spawns.json')){
 if(!(Test-Path -LiteralPath (Join-Path $root $relative))){throw "Missing bundled dependency: $relative. Extract the complete bundle again."}
}
& (Join-Path $root 'Initialize-950Client.ps1') -Root $root
if(!(Test-Path -LiteralPath (Join-Path $CachePath '255\12.dat'))){throw 'Cache missing or nested incorrectly. Extract OpenRS2 cache 2691 (Flat file) into the project root. Expected: cache\255\12.dat. Read README.md.'}
$hash=(Get-FileHash -LiteralPath (Join-Path $CachePath '255\12.dat') -Algorithm SHA256).Hash
if($hash -ne '8A45E12B3D5B3BF35CDB02CDEC9DDEDBD46200B4FEF086ADC0679FB0D020EF8C'){throw 'This is not paired OpenRS2 cache 2691. Another 950 cache may contain incompatible interface scripts.'}
New-Item -ItemType Directory -Path (Join-Path $root 'logs') -Force | Out-Null
New-Item -ItemType Directory -Path (Join-Path $root 'temp') -Force | Out-Null
$classPath=(Join-Path $root 'patches\classes')+';'+(Join-Path $root 'OpenNXT\runtime\lib\*')
$preference=$ErrorActionPreference
$ErrorActionPreference='Continue'
try {
 & (Join-Path $root 'runtime\java25\bin\java.exe') --enable-native-access=ALL-UNNAMED -Xmx2g ("-Djava.io.tmpdir="+(Join-Path $root 'temp')) -cp $classPath com.rs.tools.modern.Native950CachePreflight $CachePath *> (Join-Path $root 'logs\cache-preflight.log')
 $code=$LASTEXITCODE
} finally {$ErrorActionPreference=$preference}
if($code -ne 0){Get-Content (Join-Path $root 'logs\cache-preflight.log') -Tail 15;throw 'Cache compatibility checks failed. See logs/cache-preflight.log.'}
Write-Host 'Bundled runtimes, client isolation and cache compatibility checks passed.'
