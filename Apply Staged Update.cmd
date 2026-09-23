@echo off
setlocal
if /i "%~1"=="--check-only" goto check
echo SHNORKSCAPE update: sort the current bank tab with +; tier-first equipment pages and a fuller Best tab.
echo Before applying: close both game clients and run Stop.cmd.
echo After successful installation: launch Play.cmd normally.
echo.
"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -ExecutionPolicy Bypass -File "%~dp0Apply-PlayabilityUpdate.ps1"
set "result=%errorlevel%"
pause
exit /b %result%
:check
"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -ExecutionPolicy Bypass -File "%~dp0Apply-PlayabilityUpdate.ps1" -CheckOnly
exit /b %errorlevel%
