@echo off
setlocal
if /i "%~1"=="--check-only" goto check
echo SHNORKSCAPE live-combat refinement: conjure movement and formation, conduit availability, native soul effects and buff panels.
echo Preserves the bank/library design, global search, presets and bank sorting. New visuals require Vulkan acceptance; conjure attack animations remain partial.
echo Before applying: close both game clients and run Stop.cmd.
echo After successful installation: launch Play.cmd normally.
echo Live checklist: docs\COMBAT-REFINEMENT-LIVE-CHECKLIST-20260923.md
echo.
"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -ExecutionPolicy Bypass -File "%~dp0Apply-PlayabilityUpdate.ps1"
set "result=%errorlevel%"
pause
exit /b %result%
:check
"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -ExecutionPolicy Bypass -File "%~dp0Apply-PlayabilityUpdate.ps1" -CheckOnly
exit /b %errorlevel%
