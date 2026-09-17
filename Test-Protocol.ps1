$ErrorActionPreference='Stop'
$root=$PSScriptRoot
$cp=(Join-Path $root 'patches\classes')+';'+(Join-Path $root 'OpenNXT\runtime\lib\*')
New-Item -ItemType Directory -Path (Join-Path $root 'logs') -Force | Out-Null
Push-Location $root
try {
 $saved=$ErrorActionPreference;$ErrorActionPreference='Continue'
 try {& (Join-Path $root 'runtime\java25\bin\java.exe') --enable-native-access=ALL-UNNAMED -cp $cp Verify950Kt *> (Join-Path $root 'logs\protocol-verification.log');$result=$LASTEXITCODE}finally{$ErrorActionPreference=$saved}
 if($result -ne 0){Get-Content (Join-Path $root 'logs\protocol-verification.log') -Tail 18;throw '950 protocol checks failed.'}
 Write-Host '950 protocol checks passed.'
} finally {Pop-Location}
