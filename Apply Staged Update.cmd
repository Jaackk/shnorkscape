@echo off
setlocal
if /i "%~1"=="--check-only" goto check
echo SHNORKSCAPE pre-dev cleanup: binding dropdowns, soul timer, field cooldowns, safe quick gear, cracker targeting and bar removal.
echo Preserves preceding DM/combat/library updates. This is the cleanup candidate; Developer Console is not included.
echo Before applying: close both game clients and run Stop.cmd.
echo After successful installation: launch Play.cmd normally.
echo Live checklist: docs\PREDEV-CLEANUP-20260924.md
echo.
"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -ExecutionPolicy Bypass -File "%~dp0Apply-PlayabilityUpdate.ps1"
set "result=%errorlevel%"
pause
exit /b %result%
:check
"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -ExecutionPolicy Bypass -File "%~dp0Apply-PlayabilityUpdate.ps1" -CheckOnly
exit /b %errorlevel%
