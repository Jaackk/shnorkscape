param([string]$JavaHome='')
$ErrorActionPreference='Stop'
if (-not $JavaHome) { $JavaHome = (Join-Path $PSScriptRoot 'runtime\java25') }
$root=$PSScriptRoot
$runtime=Join-Path $root 'OpenNXT\runtime\lib'
$jars=@(Get-ChildItem -LiteralPath $runtime -Filter '*.jar' | ForEach-Object {$_.FullName})
$classpath=$jars -join ';'
$compiler=(Join-Path $root 'compiler\lib\*')+';'+(Join-Path $runtime '*')
$sourceBase=Join-Path $root 'OpenNXT\src\main\kotlin\com\opennxt'
$sources=@('model\lobby\LobbyPlayer.kt','model\world\WorldPlayer.kt','model\world\Native950WorldBootstrap.kt','model\world\Collision950.kt','model\entity\player\appearance\PlayerModel.kt','net\game\PacketRegistry.kt','net\game\pipeline\GamePacketFraming.kt','net\login\Ataraxia950Handoff.kt','net\login\Native950ServerpermVarcs.kt','net\login\Native950CacheContent.kt','net\login\Native950InterfaceBootstrap.kt','net\login\Native950Ribbon.kt','net\login\Native950RunOrb.kt','net\login\LoginServerDecoder.kt','net\login\LoginServerHandler.kt') | ForEach-Object {Join-Path $sourceBase $_}
# The Kotlin compiler writes warnings to stderr - on JDK 25 it always emits at least the
# sun.misc.Unsafe deprecation notice - and under ErrorActionPreference 'Stop' PowerShell turns a
# native command's stderr into a TERMINATING error even on a successful compile, and even with the
# *> redirection already in place. So the preference is relaxed across the call and the result is
# judged by the exit code, which is what actually reports failure. Build-Ataraxia950.ps1 carries
# the same guard around gradlew for the same reason.
$previousPreference=$ErrorActionPreference
$ErrorActionPreference='Continue'
try {
 & (Join-Path $JavaHome 'bin\java.exe') -Xmx2g -cp $compiler org.jetbrains.kotlin.cli.jvm.K2JVMCompiler -no-stdlib -no-reflect -jvm-target 25 -module-name opennxt950 ('-Xfriend-paths='+(Join-Path $runtime 'OpenNXT-1.0.0.jar')) -classpath $classpath -d (Join-Path $root 'patches\classes') @sources (Join-Path $root 'tools\Verify950.kt') *> (Join-Path $root 'logs\build-950-lobby.log')
 $code=$LASTEXITCODE
} finally { $ErrorActionPreference=$previousPreference }
if ($code -ne 0) {Get-Content -LiteralPath (Join-Path $root 'logs\build-950-lobby.log') -Tail 24;throw '950override compilation failed'}
Write-Host '950 lobby overrides compiled.'


