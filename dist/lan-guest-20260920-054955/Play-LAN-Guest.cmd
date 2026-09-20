@echo off
cd /d "%~dp0"
powershell.exe -NoProfile -File "%~dp0Play-LAN-Guest.ps1"
pause
