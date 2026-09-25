@echo off
setlocal
if /i "%~1"=="--check-only" goto check
echo SHNORKSCAPE: Developer Console V3 polish candidate.
echo Home, contextual navigation, grouped NPCs and actual searchable item rows.
echo Includes item grants, loadouts, player preview, zoom and placement diagnostics.
echo Existing combat, Equipment Library, LAN and workspace settings are preserved.
echo Before applying: close both game clients and run Stop.cmd.
echo After successful installation: launch Play.cmd normally.
echo Live checklist: docs/DEVELOPER-CONSOLE-V3-20260925.md
echo.
"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -ExecutionPolicy Bypass -File "%~dp0Apply-PlayabilityUpdate.ps1"
set "result=%errorlevel%"
pause
exit /b %result%
:check
"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -ExecutionPolicy Bypass -File "%~dp0Apply-PlayabilityUpdate.ps1" -CheckOnly
exit /b %errorlevel%
