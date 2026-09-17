[CmdletBinding()]
param(
    [string[]] $Tasks = @('compileJava'),
    [string] $JavaHome = '',
    [switch] $Online,
    # Copy the built jar into the server's runtime library directory. Without this the server keeps
    # running whatever jar was deployed last, which is the failure mode this switch exists to stop:
    # a green build and an unchanged server look identical from the outside.
    [switch] $Deploy
)

# Builds this project's own copy of the Ataraxia engine. Nothing under AstraNXT is read or written:
# both the source tree and the Gradle home are local copies, because Gradle writes locks and caches
# into GRADLE_USER_HOME and so cannot share one read-only.
#
# The engine is a JDK 8 codebase. Building it with a newer JDK silently breaks its JDK-8-era class
# scanning, so the Java version is checked rather than assumed.

$ErrorActionPreference = 'Stop'
if (-not $JavaHome) { $JavaHome = (Join-Path $PSScriptRoot 'runtime\java8') }
$root = [IO.Path]::GetFullPath($PSScriptRoot)
$project = Join-Path $root 'Ataraxia950'
$gradleHome = Join-Path $root '.gradle-ataraxia'
$log = Join-Path $root 'logs\build-ataraxia950.log'

if (!(Test-Path -LiteralPath $project)) { throw "Engine source not found at $project" }
if (!(Test-Path -LiteralPath $gradleHome)) { throw "Offline Gradle home not found at $gradleHome" }
$javac = Join-Path $JavaHome 'bin\javac.exe'
if (!(Test-Path -LiteralPath $javac)) { throw "JDK not found at $JavaHome" }
# Read the version from the JDK's own release file rather than running javac: javac prints its
# version to stderr, and PowerShell turns a native command's stderr into a terminating error here.
$releaseFile = Join-Path $JavaHome 'release'
if (!(Test-Path -LiteralPath $releaseFile)) { throw "No release file in $JavaHome; cannot confirm JDK 8" }
$version = (Select-String -LiteralPath $releaseFile -Pattern '^JAVA_VERSION=' | Select-Object -First 1).Line
if ($version -notmatch '1\.8\.') { throw "Ataraxia requires JDK 8; $JavaHome reports '$version'" }

New-Item -ItemType Directory -Path (Split-Path -Parent $log) -Force | Out-Null

$previousJava = $env:JAVA_HOME
$previousGradle = $env:GRADLE_USER_HOME
try {
    $env:JAVA_HOME = $JavaHome
    $env:GRADLE_USER_HOME = $gradleHome
    Push-Location $project
    try {
        $arguments = @('--no-daemon')
        if (-not $Online) { $arguments += '--offline' }
        $arguments += $Tasks
        Write-Host ("Ataraxia950: gradlew " + ($arguments -join ' '))
        # javac writes notes and warnings to stderr, and with ErrorActionPreference 'Stop' a native
        # command's stderr becomes a terminating error even on a successful build. Judge the build
        # by its exit code, which is what actually reports failure.
        $previousPreference = $ErrorActionPreference
        $ErrorActionPreference = 'Continue'
        try {
            & (Join-Path $project 'gradlew.bat') @arguments *> $log
            $code = $LASTEXITCODE
        } finally { $ErrorActionPreference = $previousPreference }
    } finally { Pop-Location }
} finally {
    $env:JAVA_HOME = $previousJava
    $env:GRADLE_USER_HOME = $previousGradle
}

if ($code -ne 0) {
    Write-Host "Build FAILED (exit $code). Last 30 lines:"
    Get-Content -LiteralPath $log -Tail 30
    throw "Ataraxia950 build failed; see $log"
}
Write-Host "Ataraxia950 build OK ($($Tasks -join ', ')). Log: $log"

if ($Deploy) {
    # The server's classpath is patches\classes plus OpenNXT\runtime\lib\*, so the engine reaches
    # it only as this jar. Build order for a full deploy:
    #   1. .\Build-Ataraxia950.ps1 -Tasks jar -Deploy
    #   2. .\Build-950Lobby.ps1              (Kotlin overrides compile AGAINST the deployed jar)
    #   3. .\Start-950Server.ps1 -Ataraxia
    # Step 2 must follow step 1: the overrides reference engine classes, so a stale jar there
    # produces overrides compiled against the previous wire layer.
    $built = Join-Path $project 'build\libs\ataraxia-950-1.0-UNTRACKED.jar'
    if (!(Test-Path -LiteralPath $built)) {
        throw "No jar at $built. Run with -Tasks jar before -Deploy."
    }
    $target = Join-Path $root 'OpenNXT\runtime\lib\ataraxia-950-1.0-UNTRACKED.jar'
    Copy-Item -LiteralPath $built -Destination $target -Force
    $stamp = (Get-Item -LiteralPath $target).LastWriteTime.ToString('o')
    Write-Host "Deployed engine jar -> $target ($stamp)"
    Write-Host "Now rebuild the Kotlin overrides: .\Build-950Lobby.ps1"
}
