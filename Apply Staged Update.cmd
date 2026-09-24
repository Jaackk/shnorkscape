@echo off
setlocal
if /i "%~1"=="--check-only" goto check
echo SHNORKSCAPE Developer Console: native startup/input fix and revised console layout.
echo Includes the corrected Settings cache pin for the native console bridge.
echo Preserves combat/library and world editor. Heal, NPC search and tile placement physically verified after redraw.
echo Before applying: close both game clients and run Stop.cmd.
echo After successful installation: launch Play.cmd normally.
echo Live checklist: docs\DEVELOPER-CONSOLE-LIVE-FIX-20260924.md
echo.
"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -ExecutionPolicy Bypass -File "%~dp0Apply-PlayabilityUpdate.ps1"
set "result=%errorlevel%"
pause
exit /b %result%
:check
"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -ExecutionPolicy Bypass -File "%~dp0Apply-PlayabilityUpdate.ps1" -CheckOnly
exit /b %errorlevel%
