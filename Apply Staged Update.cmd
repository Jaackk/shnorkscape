@echo off
setlocal
if /i "%~1"=="--check-only" goto check
echo SHNORKSCAPE combat polish: Army availability, Entropic soul cap, combat soul retention, conjure lifecycle and ability presentation.
echo Preserves live-passed combat and the bank/library. New visuals need live testing; Ghost attacks and general scene priority remain partial.
echo Before applying: close both game clients and run Stop.cmd.
echo After successful installation: launch Play.cmd normally.
echo Live checklist: docs\COMBAT-POLISH2-LIVE-CHECKLIST-20260924.md
echo.
"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -ExecutionPolicy Bypass -File "%~dp0Apply-PlayabilityUpdate.ps1"
set "result=%errorlevel%"
pause
exit /b %result%
:check
"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -ExecutionPolicy Bypass -File "%~dp0Apply-PlayabilityUpdate.ps1" -CheckOnly
exit /b %errorlevel%
