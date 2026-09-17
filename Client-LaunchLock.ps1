# Shared by the client launcher and cache preparer; Windows mutexes are re-entrant
# on the calling thread, so the launcher can safely invoke the preparer.
function Enter-950ClientLock([string]$Root) {
 $sha=[Security.Cryptography.SHA256]::Create()
 try {$hash=([BitConverter]::ToString($sha.ComputeHash([Text.Encoding]::UTF8.GetBytes([IO.Path]::GetFullPath($Root).ToLowerInvariant())))).Replace('-','')}finally{$sha.Dispose()}
 $mutex=New-Object Threading.Mutex($false,('Local\950Client-'+$hash))
 try {
  try {$acquired=$mutex.WaitOne(0)}catch [Threading.AbandonedMutexException]{$acquired=$true}
  if(!$acquired){throw 'This copy is already starting or preparing its client cache. Wait for that operation to finish.'}
  return $mutex
 } catch {$mutex.Dispose();throw}
}
