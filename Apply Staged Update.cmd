@echo off
setlocal
if /i "%~1"=="--check-only" goto check
echo SHNORKSCAPE combat polish: persistent manual queues, Necromancy Overloads, soul lifetime, completion unlocks and conjure commands.
echo Preserves the bank/library design, global search, presets and bank sorting. Berserk needs live acceptance; Living Death and conjure attack/expiry effects remain partial.
echo Before applying: close both game clients and run Stop.cmd.
echo After successful installation: launch Play.cmd normally.
echo Live checklist: docs\COMBAT-POLISH-LIVE-CHECKLIST-20260924.md
echo.
"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -ExecutionPolicy Bypass -File "%~dp0Apply-PlayabilityUpdate.ps1"
set "result=%errorlevel%"
pause
exit /b %result%
:check
"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -ExecutionPolicy Bypass -File "%~dp0Apply-PlayabilityUpdate.ps1" -CheckOnly
exit /b %errorlevel%
