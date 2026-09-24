@echo off
setlocal
if /i "%~1"=="--check-only" goto check
echo SHNORKSCAPE cumulative update: Almighty/DM cooldown-free Surge, Escape and Dive, plus preceding combat QoL changes.
echo Preserves live-passed combat and bank/library. Native settings and transformation need live checks; unresolved gaps are in the handoff.
echo Before applying: close both game clients and run Stop.cmd.
echo After successful installation: launch Play.cmd normally.
echo Live checklist: docs\DEVELOPER-MOVEMENT-20260924.md
echo.
"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -ExecutionPolicy Bypass -File "%~dp0Apply-PlayabilityUpdate.ps1"
set "result=%errorlevel%"
pause
exit /b %result%
:check
"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -ExecutionPolicy Bypass -File "%~dp0Apply-PlayabilityUpdate.ps1" -CheckOnly
exit /b %errorlevel%
