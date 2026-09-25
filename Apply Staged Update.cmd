@echo off
setlocal
if /i "%~1"=="--check-only" goto check
echo SHNORKSCAPE: Developer Console V2 Phase A test candidate.
echo Native NPC scrolling, full model previews, drag rotation and integrated search.
echo Primitive acceptance checkpoint: final V2 page composition follows the live test.
echo Existing combat, Equipment Library, LAN and workspace settings are preserved.
echo Before applying: close both game clients and run Stop.cmd.
echo After successful installation: launch Play.cmd normally.
echo Live checklist: docs/DEVELOPER-CONSOLE-V2-PHASE-A-20260925.md
echo.
"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -ExecutionPolicy Bypass -File "%~dp0Apply-PlayabilityUpdate.ps1"
set "result=%errorlevel%"
pause
exit /b %result%
:check
"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -ExecutionPolicy Bypass -File "%~dp0Apply-PlayabilityUpdate.ps1" -CheckOnly
exit /b %errorlevel%
