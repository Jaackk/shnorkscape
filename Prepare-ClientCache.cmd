@echo off
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0Prepare-ClientCache.ps1" -Force
pause
