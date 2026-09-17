@echo off
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0Stop-950Test.ps1"
if errorlevel 1 pause
