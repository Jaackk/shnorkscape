@echo off
setlocal
cd /d "%~dp0"
echo Host PC only: choose Nooby's password. Input is hidden and is not saved as plaintext.
"runtime\java25\bin\javac.exe" -d "temp\lan-account-tool" "OpenNXT\src\main\java\com\opennxt\security\NativeLanAccess.java"
if errorlevel 1 goto failed
"runtime\java25\bin\java.exe" -cp "temp\lan-account-tool" com.opennxt.security.NativeLanAccess "%~dp0server-home\lan-credentials.properties" "%~dp0players\modern950\players" --change nooby
if errorlevel 1 goto failed
echo Password stored. Tell Jack to close clients and restart the server to activate it.
pause
exit /b 0
:failed
echo Password change did not complete. Read the error above.
pause
exit /b 1
