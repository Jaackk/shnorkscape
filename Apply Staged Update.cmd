@echo off
setlocal
if /i "%~1"=="--check-only" goto check
echo SHNORKSCAPE: owned object cleanup and Cache / Gamevals browser.
echo New commands: ;;clearobjects [radius] and ;;gameval [name or ID].
echo Includes saved-placement safety, owner isolation and 1385 audited symbols.
echo Before applying: close both game clients and run Stop.cmd.
echo After successful installation: launch Play.cmd normally.
echo Live checklist: docs/DEVELOPER-TOOLS-GAMEVALS-20260924.md
echo.
"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -ExecutionPolicy Bypass -File "%~dp0Apply-PlayabilityUpdate.ps1"
set "result=%errorlevel%"
pause
exit /b %result%
:check
"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -ExecutionPolicy Bypass -File "%~dp0Apply-PlayabilityUpdate.ps1" -CheckOnly
exit /b %errorlevel%
