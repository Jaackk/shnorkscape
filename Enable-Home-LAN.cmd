@echo off
cd /d "%~dp0"
"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -File "%~dp0Enable-Home-LAN.ps1" -RequestElevation
echo.
echo If setup did not complete, send Jack the error shown above. Do not disable security.
pause
