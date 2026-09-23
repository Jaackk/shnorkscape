@echo off
setlocal
if /i "%~1"=="--check-only" goto check
echo SHNORKSCAPE combat candidate: native queue, Revolution controls, Dive, Necromancy resources and expanded ability effects.
echo Preserves the bank/library design, global search, presets and bank sorting. Full retail combat remains partial.
echo Before applying: close both game clients and run Stop.cmd.
echo After successful installation: launch Play.cmd normally.
echo Live checklist: docs\COMBAT-LIVE-CHECKLIST-20260923.md
echo.
"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -ExecutionPolicy Bypass -File "%~dp0Apply-PlayabilityUpdate.ps1"
set "result=%errorlevel%"
pause
exit /b %result%
:check
"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -ExecutionPolicy Bypass -File "%~dp0Apply-PlayabilityUpdate.ps1" -CheckOnly
exit /b %errorlevel%
