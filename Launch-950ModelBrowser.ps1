[CmdletBinding()]
param(
    [string] $CachePath = '',
    [string] $JavaHome = ''
)

# Compiles and runs only the standalone model browser. Does not build, deploy or start the engine.
$ErrorActionPreference = 'Stop'
if (-not $JavaHome) { $JavaHome = (Join-Path $PSScriptRoot 'runtime\java25') }
if (-not $CachePath) { $CachePath = (Join-Path $PSScriptRoot 'cache') }
$root = [IO.Path]::GetFullPath($PSScriptRoot)
$jar = Join-Path $root 'OpenNXT\runtime\lib\ataraxia-950-1.0-UNTRACKED.jar'
$libs = Join-Path $root 'OpenNXT\runtime\lib\*'
$source = Join-Path $root 'tools\Native950ModelBrowser.java'
$classes = Join-Path $root 'tools\model-browser-classes'
$java = Join-Path $JavaHome 'bin\java.exe'
$javac = Join-Path $JavaHome 'bin\javac.exe'
if (!(Test-Path -LiteralPath $jar)) { throw "Packaged engine not found: $jar" }
if (!(Test-Path -LiteralPath $javac)) { throw "JDK not found: $JavaHome" }
if (!(Test-Path -LiteralPath (Join-Path $CachePath '255'))) { throw "Not a flat-file cache (missing 255/): $CachePath" }
New-Item -ItemType Directory -Path $classes -Force | Out-Null
$previousPreference = $ErrorActionPreference
$ErrorActionPreference = 'Continue'
try {
    & $javac -encoding UTF-8 -cp "$jar;$libs" -d $classes $source
    if ($LASTEXITCODE -ne 0) { throw 'Model browser compilation failed.' }
    & $java --enable-native-access=ALL-UNNAMED -Xmx2g -cp "$classes;$jar;$libs" com.rs.tools.modelviewer.Native950ModelBrowser $CachePath
    if ($LASTEXITCODE -ne 0) { throw 'Model browser exited with an error.' }
} finally { $ErrorActionPreference = $previousPreference }
