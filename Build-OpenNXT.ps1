[CmdletBinding()]
param(
    [string]$Root,
    [string]$OutputDirectory,
    [string]$JavaHome,
    [switch]$Deploy
)

$ErrorActionPreference = 'Stop'
if (-not $Root) { $Root = $PSScriptRoot }
$Root = (Resolve-Path -LiteralPath $Root).Path
if (-not $OutputDirectory) { $OutputDirectory = Join-Path $Root 'build\opennxt' }
$OutputDirectory = [IO.Path]::GetFullPath($OutputDirectory)
if (-not $JavaHome) { $JavaHome = Join-Path $Root 'runtime\java25' }
$JavaHome = (Resolve-Path -LiteralPath $JavaHome).Path
$runtimeDirectory = Join-Path $Root 'OpenNXT\runtime\lib'
$compilerDirectory = Join-Path $Root 'compiler\lib'
$java = Join-Path $JavaHome 'bin\java.exe'
$javac = Join-Path $JavaHome 'bin\javac.exe'
$jar = Join-Path $JavaHome 'bin\jar.exe'
foreach ($tool in @($java, $javac, $jar)) {
    if (-not (Test-Path -LiteralPath $tool -PathType Leaf)) { throw "Missing bundled JDK tool: $tool" }
}

# Never use an existing OpenNXT jar or patches to supply missing project classes.
$dependencies = @(Get-ChildItem -LiteralPath $runtimeDirectory -Filter '*.jar' -File |
    Where-Object { $_.Name -notlike 'OpenNXT*.jar' } | Sort-Object Name)
$compilerJars = @(Get-ChildItem -LiteralPath $compilerDirectory -Filter '*.jar' -File | Sort-Object Name)
if (-not ($compilerJars | Where-Object { $_.Name -like 'kotlin-compiler-embeddable-*.jar' })) {
    throw "The bundled Kotlin compiler is missing from $compilerDirectory"
}
$dependencyClasspath = ($dependencies.FullName -join [IO.Path]::PathSeparator)
$compilerClasspath = ((@($compilerJars.FullName) + @($dependencies.FullName)) -join [IO.Path]::PathSeparator)
$javaSources = @(Get-ChildItem -LiteralPath (Join-Path $Root 'OpenNXT\src\main\java') -Filter '*.java' -File -Recurse | Sort-Object FullName)
$kotlinSources = @(
    Get-ChildItem -LiteralPath (Join-Path $Root 'OpenNXT\src\main\kotlin') -Filter '*.kt' -File -Recurse
    Get-ChildItem -LiteralPath (Join-Path $Root 'OpenNXT\src\generated\kotlin') -Filter '*.kt' -File -Recurse
) | Sort-Object FullName
if ($javaSources.Count -eq 0 -or $kotlinSources.Count -eq 0) { throw 'The complete Java and Kotlin source trees are required.' }

New-Item -ItemType Directory -Path $OutputDirectory -Force | Out-Null
# Fresh directories prevent deleted or renamed source classes surviving a rebuild.
$runDirectory = Join-Path $OutputDirectory ('run-' + [Guid]::NewGuid().ToString('N'))
$classes = Join-Path $runDirectory 'classes'
New-Item -ItemType Directory -Path $classes -Force | Out-Null
$utf8 = New-Object Text.UTF8Encoding($false)

function Write-ResponseFile([string]$Path, [string[]]$Arguments) {
    $quoted = foreach ($argument in $Arguments) {
        '"' + $argument.Replace('\', '/').Replace('"', '\"') + '"'
    }
    [IO.File]::WriteAllLines($Path, $quoted, $utf8)
}

function Invoke-BuildTool([string]$Executable, [string[]]$Arguments, [string]$LogPath, [string]$Description) {
    Write-Host $Description
    # Java writes warnings to stderr even when successful. Use its exit status.
    $previousPreference = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    try {
        & $Executable @Arguments *> $LogPath
        $buildExitCode = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $previousPreference
    }
    if ($buildExitCode -ne 0) {
        Get-Content -LiteralPath $LogPath -Tail 80
        throw "$Description failed (exit $buildExitCode). See $LogPath"
    }
}

$javacArgs = Join-Path $runDirectory 'javac.args'
Write-ResponseFile $javacArgs (@('--release', '25', '-encoding', 'UTF-8', '-classpath', $classes, '-d', $classes) + @($javaSources.FullName))
Invoke-BuildTool $javac @('@' + $javacArgs) (Join-Path $runDirectory 'javac.log') "Compiling $($javaSources.Count) Java source files..."

$kotlinArgs = Join-Path $runDirectory 'kotlin.args'
$sourceClasspath = $classes + [IO.Path]::PathSeparator + $dependencyClasspath
Write-ResponseFile $kotlinArgs (@('-no-stdlib', '-no-reflect', '-jvm-target', '25', '-module-name', 'OpenNXT', '-classpath', $sourceClasspath, '-d', $classes) + @($kotlinSources.FullName))
Invoke-BuildTool $java @('-Xmx3g', '-cp', $compilerClasspath, 'org.jetbrains.kotlin.cli.jvm.K2JVMCompiler', ('@' + $kotlinArgs)) (Join-Path $runDirectory 'kotlin.log') "Compiling all $($kotlinSources.Count) Kotlin source files..."

$resources = Join-Path $Root 'OpenNXT\src\main\resources'
if (Test-Path -LiteralPath $resources -PathType Container) {
    Get-ChildItem -LiteralPath $resources -Force | ForEach-Object {
        Copy-Item -LiteralPath $_.FullName -Destination $classes -Recurse -Force
    }
}
$builtJar = Join-Path $runDirectory 'OpenNXT-1.0.0.jar'
Invoke-BuildTool $jar @('--create', '--file', $builtJar, '--main-class', 'com.opennxt.MainKt', '-C', $classes, '.') (Join-Path $runDirectory 'jar.log') 'Packaging the complete OpenNXT server...'

Add-Type -AssemblyName System.IO.Compression.FileSystem
$archive = [IO.Compression.ZipFile]::OpenRead($builtJar)
try {
    foreach ($requiredEntry in @('com/opennxt/MainKt.class', 'com/opennxt/OpenNXT.class', 'com/opennxt/util/ISAACCipher.class', 'com/opennxt/util/Whirlpool.class', 'META-INF/OpenNXT.kotlin_module')) {
        if (-not $archive.GetEntry($requiredEntry)) { throw "Built jar is missing $requiredEntry" }
    }
    $entryCount = $archive.Entries.Count
} finally { $archive.Dispose() }

$artifact = Join-Path $OutputDirectory 'OpenNXT-1.0.0.jar'
Copy-Item -LiteralPath $builtJar -Destination $artifact -Force
$report = [ordered]@{
    artifact = $artifact
    sha256 = (Get-FileHash -LiteralPath $artifact -Algorithm SHA256).Hash
    javaSourceCount = $javaSources.Count
    kotlinSourceCount = $kotlinSources.Count
    dependencyCount = $dependencies.Count
    existingOpenNXTJarOnCompileClasspath = $false
    moduleName = 'OpenNXT'
    jarEntryCount = $entryCount
    buildLogDirectory = $runDirectory
}
[IO.File]::WriteAllText((Join-Path $OutputDirectory 'build-report.json'), ($report | ConvertTo-Json -Depth 4), $utf8)
if ($Deploy) {
    Copy-Item -LiteralPath $artifact -Destination (Join-Path $runtimeDirectory 'OpenNXT-1.0.0.jar') -Force
    Write-Host 'Built and installed OpenNXT. Rebuild the 950 overrides before launching.'
} else {
    Write-Host "Built OpenNXT: $artifact"
    Write-Host 'Use -Deploy to install it into this bundle.'
}
