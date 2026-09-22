@echo off
cd /d "%~dp0"
powershell.exe -NoProfile -Command "Start-Process powershell.exe -Verb RunAs -ArgumentList '-NoProfile -NoExit -File ""%~dp0Enable-Home-LAN.ps1""'"
