[CmdletBinding()]
param(
    [string] $CachePath = '',
    [string] $OutputPath = '',
    [string] $JavaHome = ''
)

# Compiles only the standalone exporter. Does not build, deploy or start the engine.
$ErrorActionPreference = 'Stop'
if (-not $JavaHome) { $JavaHome = (Join-Path $PSScriptRoot 'runtime\java25') }
if (-not $OutputPath) { $OutputPath = (Join-Path $PSScriptRoot 'dumps') }
if (-not $CachePath) { $CachePath = (Join-Path $PSScriptRoot 'cache') }
$root = [IO.Path]::GetFullPath($PSScriptRoot)
$jar = Join-Path $root 'OpenNXT\runtime\lib\ataraxia-950-1.0-UNTRACKED.jar'
$libs = Join-Path $root 'OpenNXT\runtime\lib\*'
$source = Join-Path $root 'tools\CacheNameDump950.java'
$classes = Join-Path $root 'tools\cache-name-dump-classes'
$java = Join-Path $JavaHome 'bin\java.exe'
$javac = Join-Path $JavaHome 'bin\javac.exe'
if (!(Test-Path -LiteralPath $jar)) { throw "Packaged engine not found: $jar" }
if (!(Test-Path -LiteralPath $javac)) { throw "JDK not found: $JavaHome" }
New-Item -ItemType Directory -Path $classes -Force | Out-Null
$previousPreference = $ErrorActionPreference
$ErrorActionPreference = 'Continue'
try {
    & $javac -encoding UTF-8 -cp "$jar;$libs" -d $classes $source
    if ($LASTEXITCODE -ne 0) { throw 'Cache name exporter compilation failed.' }
    & $java --enable-native-access=ALL-UNNAMED -Xmx2g -cp "$classes;$jar;$libs" com.rs.cache.loaders.CacheNameDump950 $CachePath $OutputPath $jar
    if ($LASTEXITCODE -ne 0) { throw 'Cache export failed or reported incomplete definitions; inspect export-report.json.' }
} finally { $ErrorActionPreference = $previousPreference }
Write-Host "Cache lists written to: $OutputPath"
