[CmdletBinding()]
param(
    [string]$DistributionRoot,
    [string]$SourceRoot,
    [string]$OutputDirectory
)

$ErrorActionPreference = 'Stop'
if (-not $DistributionRoot) { $DistributionRoot = Split-Path $PSScriptRoot -Parent }
if (-not $SourceRoot) { $SourceRoot = Split-Path $PSScriptRoot -Parent }
$DistributionRoot = (Resolve-Path -LiteralPath $DistributionRoot).Path
$SourceRoot = (Resolve-Path -LiteralPath $SourceRoot).Path
if (-not $OutputDirectory) { $OutputDirectory = Join-Path $env:TEMP ('950-js5-verification-' + [Guid]::NewGuid().ToString('N')) }
$OutputDirectory = [IO.Path]::GetFullPath($OutputDirectory)
$classes = Join-Path $OutputDirectory 'classes'
New-Item -ItemType Directory -Path $classes -Force | Out-Null
$java = Join-Path $DistributionRoot 'runtime\java25\bin\java.exe'
$runtime = Join-Path $DistributionRoot 'OpenNXT\runtime\lib'
$baseJar = Join-Path $runtime 'OpenNXT-1.0.0.jar'
$compiler = Join-Path $DistributionRoot 'compiler\lib'
$runtimeJars = @(Get-ChildItem -LiteralPath $runtime -Filter '*.jar' -File | Sort-Object Name)
$classpath = $runtimeJars.FullName -join [IO.Path]::PathSeparator
$compilerClasspath = (Join-Path $compiler '*') + [IO.Path]::PathSeparator + (Join-Path $runtime '*')
$sources = @(
    'OpenNXT\src\main\kotlin\com\opennxt\filesystem\Js5ContainerPayload.kt',
    'OpenNXT\src\main\kotlin\com\opennxt\net\js5\Js5Session.kt',
    'OpenNXT\src\main\kotlin\com\opennxt\net\http\endpoints\Js5MsEndpoint.kt',
    'tools\VerifyJs5Payload.kt'
) | ForEach-Object { Join-Path $SourceRoot $_ }
foreach ($required in @($java, $baseJar) + $sources) {
    if (-not (Test-Path -LiteralPath $required -PathType Leaf)) { throw "Missing verification input: $required" }
}
$arguments = @('-no-stdlib', '-no-reflect', '-jvm-target', '25', '-module-name', 'OpenNXT', ('-Xfriend-paths=' + $baseJar), '-classpath', $classpath, '-d', $classes) + $sources
$quoted = foreach ($argument in $arguments) { '"' + $argument.Replace('\', '/').Replace('"', '\"') + '"' }
$argumentFile = Join-Path $OutputDirectory 'compiler.args'
[IO.File]::WriteAllLines($argumentFile, $quoted, [Text.UTF8Encoding]::new($false))
$compileLog = Join-Path $OutputDirectory 'compile.log'
$runLog = Join-Path $OutputDirectory 'verification.log'
$oldOffline = [Environment]::GetEnvironmentVariable('OPENNXT_OFFLINE', 'Process')
$oldPreference = $ErrorActionPreference
Push-Location $OutputDirectory
try {
    [Environment]::SetEnvironmentVariable('OPENNXT_OFFLINE', 'true', 'Process')
    $ErrorActionPreference = 'Continue'
    & $java -Xmx1g -cp $compilerClasspath org.jetbrains.kotlin.cli.jvm.K2JVMCompiler ('@' + $argumentFile) *> $compileLog
    $compileExit = $LASTEXITCODE
    $ErrorActionPreference = 'Stop'
    if ($compileExit -ne 0) { Get-Content -LiteralPath $compileLog; throw "Verification compilation failed: $compileLog" }
    $ErrorActionPreference = 'Continue'
    & $java -Xmx256m '-Dorg.slf4j.simpleLogger.defaultLogLevel=warn' -cp ($classes + [IO.Path]::PathSeparator + $classpath) com.opennxt.verification.VerifyJs5Payload (Join-Path $OutputDirectory 'fixtures') *> $runLog
    $runExit = $LASTEXITCODE
    $ErrorActionPreference = 'Stop'
    Get-Content -LiteralPath $runLog
    if ($runExit -ne 0) { throw "Verification failed: $runLog" }
    Write-Host "Verification output: $OutputDirectory"
} finally {
    $ErrorActionPreference = $oldPreference
    [Environment]::SetEnvironmentVariable('OPENNXT_OFFLINE', $oldOffline, 'Process')
    Pop-Location
}
