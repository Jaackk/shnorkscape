@echo off
cd /d "%~dp0"
"runtime\java25\bin\java.exe" -cp "patches\classes;OpenNXT\runtime\lib\*" com.opennxt.security.NativeLanAccess "%~dp0server-home\lan-credentials.properties" "%~dp0players\modern950\players"
pause
