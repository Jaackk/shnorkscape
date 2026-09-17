@echo off
setlocal
title RuneScape 950 - Finish setup
cd /d "%~dp0"

echo Downloading the OpenRS2 revision 950.1 cache.
echo This is a large download and may take a while. Keep this window open.
echo.

"%SystemRoot%\System32\curl.exe" -L --fail --retry 5 --retry-delay 5 --output "flat-file.tar.gz" "https://archive.openrs2.org/caches/runescape/2691/flat-file.tar.gz"
if errorlevel 1 goto :failed

echo.
echo Extracting the cache...
"%SystemRoot%\System32\tar.exe" -xzf "flat-file.tar.gz" -C "."
if errorlevel 1 goto :failed

if not exist "cache\255\12.dat" (
  echo Expected cache file cache\255\12.dat was not found.
  goto :failed
)

echo.
echo Checking the bundle...
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0Test-Bundle.ps1"
if errorlevel 1 goto :failed

del /q "flat-file.tar.gz"

echo.
echo Setup passed. Starting the server and client...
call "%~dp0Play.cmd"
exit /b %errorlevel%

:failed
echo.
echo Setup did not complete. Leave this window open and review the error above.
pause
exit /b 1
