@echo off
setlocal
if /i "%~1"=="--check-only" goto check
echo SHNORKSCAPE: combat encounters, boss effects and world travel.
echo Improved Dagannoth Kings; normal Graardor and his minions; 26 Slayer Tower stair links.
echo War's Reaper portal: Graardor, Dagannoth Kings, Slayer Tower and Taverley Dungeon.
echo Includes owned object cleanup and 1438 verified symbolic browser entries.
echo Before applying: close both game clients and run Stop.cmd.
echo After successful installation: launch Play.cmd normally.
echo Live checklist: docs/COMBAT-WORLD-EXPANSION-20260924.md
echo.
"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -ExecutionPolicy Bypass -File "%~dp0Apply-PlayabilityUpdate.ps1"
set "result=%errorlevel%"
pause
exit /b %result%
:check
"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -ExecutionPolicy Bypass -File "%~dp0Apply-PlayabilityUpdate.ps1" -CheckOnly
exit /b %errorlevel%
