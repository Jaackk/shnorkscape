@echo off
setlocal
if /i "%~1"=="--check-only" goto check
echo SHNORKSCAPE Developer Console: native UI, favourites, NPC/object search and owned world editor.
echo Preserves preceding combat/library updates. Includes explicit Save to World and temporary placement undo.
echo Before applying: close both game clients and run Stop.cmd.
echo After successful installation: launch Play.cmd normally.
echo Live checklist: docs\DEVELOPER-CONSOLE-20260924.md
echo.
"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -ExecutionPolicy Bypass -File "%~dp0Apply-PlayabilityUpdate.ps1"
set "result=%errorlevel%"
pause
exit /b %result%
:check
"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -ExecutionPolicy Bypass -File "%~dp0Apply-PlayabilityUpdate.ps1" -CheckOnly
exit /b %errorlevel%
