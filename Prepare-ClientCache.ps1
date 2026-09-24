[CmdletBinding()]
param([switch]$Force)
$ErrorActionPreference='Stop'
$root=[IO.Path]::GetFullPath($PSScriptRoot)
. (Join-Path $root 'Client-LaunchLock.ps1')
$mutex=Enter-950ClientLock $root
try {
 $clients=@(Get-CimInstance Win32_Process -Filter "Name='rs2client.exe' OR Name='rs2client-vulkan.exe'" | Where-Object {$_.ExecutablePath -and $_.ExecutablePath.StartsWith((Join-Path $root 'client')+'\',[StringComparison]::OrdinalIgnoreCase)})
 if($clients.Count){throw 'Close this copy of the game client before preparing its cache. Existing cache and settings have been left intact.'}
 $reference=Join-Path $root 'cache\255\12.dat'
 $acceptedReferences=@('8A45E12B3D5B3BF35CDB02CDEC9DDEDBD46200B4FEF086ADC0679FB0D020EF8C','21AAE886E340146ED851949C0F900FAE44208BE899E305E7331C12F1D6F44E89','95CA10C1A35C2B5B397FB5583B46DDA0876C5BE03619A6BB18BD6782F8DE4648','A0A233157B1709AEA860B1EFD9961950DE1C15FAF0B29805E3E7EAED0E6A3494','F3514AE304A825E2670E5157045CF0DEA42B0CBF9B2756608525FCE111BD8AB8','3B6396C1F2514C9C1E3CD9B18DE624D519915A7B9CFCC484A6C4DD4DCB9BF8D2','0CD7E76462FDA725B96704763C088FC85CBA827D472F21E70EB4C3F1F4E8DD51')
 if(!(Test-Path -LiteralPath $reference)){throw 'The paired cache reference is missing.'}
 $expected=(Get-FileHash -LiteralPath $reference -Algorithm SHA256).Hash
 if($expected -notin $acceptedReferences){throw 'Install the paired cache or the verified Developer Library update before preparing the client.'}
 # Include every reference: item/NPC metadata changes must also refresh native databases.
 $referenceHashes=@(Get-ChildItem -LiteralPath (Join-Path $root 'cache\255') -Filter '*.dat' | Sort-Object Name | ForEach-Object { $_.Name+':'+(Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash })
 $digest=[Security.Cryptography.SHA256]::Create()
 try{$fingerprint=[BitConverter]::ToString($digest.ComputeHash([Text.Encoding]::UTF8.GetBytes(($referenceHashes -join '|')))).Replace('-','')}finally{$digest.Dispose()}
 $destination=Join-Path $root 'client-state\Jagex\RuneScape'
 $markerPath=Join-Path $root 'client-state\prepared-cache.json'
 if(!$Force -and (Test-Path -LiteralPath $markerPath)){
  try {$marker=Get-Content -LiteralPath $markerPath -Raw|ConvertFrom-Json}catch{$marker=$null}
  if($marker -and $marker.format -eq 2 -and $marker.reference -eq $expected -and $marker.fingerprint -eq $fingerprint -and @($marker.databases).Count -eq 45){
   $present=$true
   foreach($name in $marker.databases){if($name -notmatch '^js5-\d+\.jcache$' -or !(Test-Path -LiteralPath (Join-Path $destination $name))){$present=$false;break}}
   if($present){Write-Host 'Client startup assets are ready.';return}
  }
 }
 foreach($path in @($destination,(Join-Path $root 'temp'),(Join-Path $root 'logs'))){New-Item -ItemType Directory -Path $path -Force|Out-Null}
 $mode='startup'
 Write-Host 'Preparing startup assets directly from your local cache...'
 $cp=(Join-Path $root 'OpenNXT\runtime\lib\*')
 $saved=$ErrorActionPreference;$ErrorActionPreference='Continue'
 try {
  & (Join-Path $root 'runtime\java25\bin\java.exe') --enable-native-access=ALL-UNNAMED -Xmx2g ("-Djava.io.tmpdir="+(Join-Path $root 'temp')) -cp $cp com.opennxt.tools.bundle.PrepareClientCache (Join-Path $root 'cache') $destination $mode 2>&1 | Tee-Object -FilePath (Join-Path $root 'logs\prepare-client-cache.log') | ForEach-Object {Write-Host $_}
  $exitCode=$LASTEXITCODE
 }finally{$ErrorActionPreference=$saved}
 if($exitCode -ne 0){throw 'Client cache preparation did not finish. See logs/prepare-client-cache.log. Run Play.cmd again to resume.'}
 $names=@(Get-ChildItem -LiteralPath (Join-Path $root 'cache\255') -Filter '*.dat'|ForEach-Object {'js5-'+$_.BaseName+'.jcache'})
 if($names.Count -ne 45 -or @($names|Where-Object {!(Test-Path -LiteralPath (Join-Path $destination $_))}).Count){throw 'Client cache preparation is missing reference databases.'}
 $marker=[ordered]@{format=2;reference=$expected;fingerprint=$fingerprint;mode=$mode;databases=$names;completedUtc=[datetime]::UtcNow.ToString('o')}
 $temporary=$markerPath+'.tmp'
 [IO.File]::WriteAllText($temporary,($marker|ConvertTo-Json -Depth 3),(New-Object Text.UTF8Encoding($false)))
 if(Test-Path -LiteralPath $markerPath){[IO.File]::Replace($temporary,$markerPath,[NullString]::Value)}else{[IO.File]::Move($temporary,$markerPath)}
}finally{$mutex.ReleaseMutex();$mutex.Dispose()}
