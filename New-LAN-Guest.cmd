@echo off
cd /d "%~dp0"
"runtime\java25\bin\javac.exe" -d "temp\lan-account-tool" "OpenNXT\src\main\java\com\opennxt\security\NativeLanAccess.java"
if errorlevel 1 goto done
"runtime\java25\bin\java.exe" -cp "temp\lan-account-tool" com.opennxt.security.NativeLanAccess "%~dp0server-home\lan-credentials.properties" "%~dp0players\modern950\players"
:done
pause
