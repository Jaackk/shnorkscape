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
if($hash -notin @('8A45E12B3D5B3BF35CDB02CDEC9DDEDBD46200B4FEF086ADC0679FB0D020EF8C','21AAE886E340146ED851949C0F900FAE44208BE899E305E7331C12F1D6F44E89','95CA10C1A35C2B5B397FB5583B46DDA0876C5BE03619A6BB18BD6782F8DE4648','A0A233157B1709AEA860B1EFD9961950DE1C15FAF0B29805E3E7EAED0E6A3494','F3514AE304A825E2670E5157045CF0DEA42B0CBF9B2756608525FCE111BD8AB8','3B6396C1F2514C9C1E3CD9B18DE624D519915A7B9CFCC484A6C4DD4DCB9BF8D2','0CD7E76462FDA725B96704763C088FC85CBA827D472F21E70EB4C3F1F4E8DD51','C036A346B4E455F3FED09B81275997DC0757D8E2A77F699784417C7BB603DFF0','FFE16E04B1275A7AC467DF8483306128A82F686E99AD927DE7DCCC27C941EFCC','440072F3F702D5C117DF3634E8AD57D0CD321218250AE9A902F035EC1BDE5505','8C1E2D31A35B5CEA52A9101A3033A5BF53FF730965C99E105537C4D4FBC44EF5','950231CE6307F99B6473CBFBF909EFE3AB6EB83FC29B218851AC35ECAB4948D9','6D4D86AF13754607EDE54871A6349D3C901DBAB0F99CDF1CB40E369596A15239','8F747EC27A1397077F7A27CE9CB018ACA19473BC9D47BDEE283CF55EA9A86A3B')){throw 'This is neither the paired cache nor its verified Developer Library update.'}
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
