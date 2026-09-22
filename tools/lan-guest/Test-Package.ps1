param([Parameter(Mandatory=$true)][string]$Zip)
$ErrorActionPreference='Stop'
$test=Join-Path ([IO.Path]::GetTempPath()) ('SHNORKSCAPE guest space & test '+[guid]::NewGuid().ToString('N'))
Expand-Archive -LiteralPath $Zip -DestinationPath $test
$expected=@('client\rs2client-vulkan.exe','Play SHNORKSCAPE.cmd','README-FIRST.txt')
$actual=@(Get-ChildItem -LiteralPath $test -Recurse -File | ForEach-Object {$_.FullName.Substring($test.Length+1)})
if (@(Compare-Object $expected $actual).Count) { throw 'Unexpected package entries.' }
$launcher=Join-Path $test 'Play SHNORKSCAPE.cmd'
if ((Get-Content -LiteralPath $launcher -Raw) -match '(?i)powershell|executionpolicy') { throw 'Guest launcher must not invoke PowerShell.' }
& $env:ComSpec /d /c "`"$launcher`" --check-only"
if ($LASTEXITCODE -ne 0) { throw 'Arbitrary-directory preflight failed.' }
$exe=Join-Path $test 'client\rs2client-vulkan.exe'
$bytes=[IO.File]::ReadAllBytes($exe)
$seed=[IO.File]::ReadAllBytes((Join-Path $PSScriptRoot '..\..\client\rs2client-vulkan.exe'))
if($bytes.Length -ne $seed.Length){throw 'Binary size changed.'}
$pathLength=[Text.Encoding]::Unicode.GetByteCount("C:\Games\950OpenSource\client-state`0")
for($i=0;$i -lt $bytes.Length;$i++) {
 if($bytes[$i] -ne $seed[$i] -and ($i -lt 0xd92348 -or $i -ge (0xd92348+$pathLength))) { throw 'Unexpected binary change outside storage string.' }
}
if([Text.Encoding]::Unicode.GetString($bytes).Contains('C:\Games\950OpenSource')) { throw 'Absolute host storage path remains.' }
Move-Item -LiteralPath $exe -Destination ($exe+'.test-held')
& $env:ComSpec /d /c "`"$launcher`" --check-only"
if($LASTEXITCODE -eq 0){throw 'Missing client was not rejected.'}
Move-Item -LiteralPath ($exe+'.test-held') -Destination $exe
$offline=Join-Path $test 'Unreachable host test.cmd'
[IO.File]::WriteAllText($offline,(Get-Content -LiteralPath $launcher -Raw).Replace(':8950/',':1/'),[Text.Encoding]::ASCII)
& $env:ComSpec /d /c "`"$offline`" --check-only"
if($LASTEXITCODE -eq 0){throw 'Unreachable endpoint was not rejected.'}
Write-Output "PASS: package allowlist, portable binary delta, no guest PowerShell, spaces/ampersand path, live host preflight and missing-client rejection. Test folder: $test"
Write-Output 'PASS: unreachable host rejected. No native game executable was launched. Rendered launch/storage behaviour remains a manual second-PC test.'
