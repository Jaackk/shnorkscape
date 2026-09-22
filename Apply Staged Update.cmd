@echo off
setlocal
pushd "%~dp0" || exit /b 1
set "candidate=dist\staged-update\ataraxia-950-1.0-UNTRACKED.jar"
set "expected=e65069abafe0711feda6b403e9ba8128d9f57af050175840b3715945590327dc"
if not exist "%candidate%" goto missing
"%SystemRoot%\System32\certutil.exe" -hashfile "%candidate%" SHA256 2>nul | "%SystemRoot%\System32\findstr.exe" /i /x "%expected%" >nul
if errorlevel 1 goto corrupt
if /i "%~1"=="--check-only" (
 echo Ability, Chat and Follow candidate hash verified. Nothing installed or restarted.
 popd
 exit /b 0
)
"%SystemRoot%\System32\tasklist.exe" /fi "IMAGENAME eq java.exe" /nh | "%SystemRoot%\System32\findstr.exe" /i "java.exe" >nul
if not errorlevel 1 goto running
"%SystemRoot%\System32\tasklist.exe" /fi "IMAGENAME eq rs2client.exe" /nh | "%SystemRoot%\System32\findstr.exe" /i "rs2client.exe" >nul
if not errorlevel 1 goto running
set "backup=backups\manual-ability-chat-follow-%RANDOM%-%RANDOM%"
if exist "%backup%" goto failed
mkdir "%backup%" || goto failed
copy /b "OpenNXT\runtime\lib\ataraxia-950-1.0-UNTRACKED.jar" "%backup%\engine.jar" >nul || goto failed
"%SystemRoot%\System32\xcopy.exe" "players\modern950\players" "%backup%\players\" /e /i /y /q >nul
if errorlevel 1 goto failed
if exist "workspace-state950" (
 "%SystemRoot%\System32\xcopy.exe" "workspace-state950" "%backup%\workspace-state950\" /e /i /y /q >nul
 if errorlevel 1 goto failed
)
copy /b "%candidate%" "OpenNXT\runtime\lib\chat-follow-update.tmp" >nul || goto failed
"%SystemRoot%\System32\certutil.exe" -hashfile "OpenNXT\runtime\lib\chat-follow-update.tmp" SHA256 2>nul | "%SystemRoot%\System32\findstr.exe" /i /x "%expected%" >nul
if errorlevel 1 goto failed
move /y "OpenNXT\runtime\lib\chat-follow-update.tmp" "OpenNXT\runtime\lib\ataraxia-950-1.0-UNTRACKED.jar" >nul || goto failed
echo Ability, Chat and Follow update installed. Backup: %backup%
echo Start Play.cmd normally. Nooby keeps his existing launcher and password.
pause
popd
exit /b 0
:running
echo Nothing installed. Close both game clients and stop SHNORKSCAPE with Stop.cmd first.
echo A Java process is still running. This installer will not stop any process for you.
goto error
:missing
echo The staged update JAR is missing. Nothing installed.
goto error
:corrupt
echo The staged update does not match the tested SHA-256. Nothing installed.
goto error
:failed
echo Update failed. Do not start the server until the error has been checked.
echo Existing backup, if created: %backup%
:error
pause
popd
exit /b 1
