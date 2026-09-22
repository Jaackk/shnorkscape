@echo off
setlocal DisableDelayedExpansion
set "LAN_HOST=@LAN_ADDRESS@"
set "CLIENT_HASH=b6a7e8688625198a69aa31fdd70f7b8cc9e82b1ac0b4f99cd71d5657f51491d5"
cd /d "%~dp0"
if errorlevel 1 goto folder_error
if exist "OpenNXT\" goto host_error
set "CLIENT=%~dp0client\rs2client-vulkan.exe"
set "CONFIG=http://%LAN_HOST%:8950/jav_config.ws?binaryType=2"
if not exist "%CLIENT%" goto missing_client
if not exist "%SystemRoot%\System32\vulkan-1.dll" goto missing_vulkan
if not exist "%SystemRoot%\System32\curl.exe" goto missing_curl
if not exist "%SystemRoot%\System32\certutil.exe" goto missing_certutil
"%SystemRoot%\System32\certutil.exe" -hashfile "%CLIENT%" SHA256 | "%SystemRoot%\System32\findstr.exe" /i /x "%CLIENT_HASH%" >nul
if errorlevel 1 goto hash_error
if not exist "client-state\" mkdir "client-state"
if not exist "temp\" mkdir "temp"
if not exist "client-state\" goto folder_error
if not exist "temp\" goto folder_error
echo Checking SHNORKSCAPE at %LAN_HOST% ...
"%SystemRoot%\System32\curl.exe" --noproxy "*" --fail --silent --show-error --connect-timeout 5 --max-time 15 --output "temp\guest-jav-config.txt" "%CONFIG%"
if errorlevel 1 goto network_error
type "temp\guest-jav-config.txt" | "%SystemRoot%\System32\findstr.exe" /b /c:"server_version=950" >nul
if errorlevel 1 goto config_error
type "temp\guest-jav-config.txt" | "%SystemRoot%\System32\findstr.exe" /b /c:"param=3=%LAN_HOST%" >nul
if errorlevel 1 goto config_error
if /i "%~1"=="--check-only" (
 echo PASS: portable guest files, hash, Vulkan loader and LAN configuration.
 exit /b 0
)
echo Launching SHNORKSCAPE. First cache download may take a while.
start "" /wait "%CLIENT%" "%CONFIG%"
if errorlevel 1 goto client_error
echo Client closed.
pause
exit /b 0
:missing_client
echo ERROR: Client missing. Extract the entire ZIP before launching, not just this file.
goto failure
:missing_vulkan
echo ERROR: The Windows Vulkan loader is missing. Install your graphics vendor's normal supported driver.
echo Do not download individual DLLs from third-party sites.
goto failure
:missing_curl
echo ERROR: Windows curl.exe is missing. This package requires current Windows 10 or 11.
goto failure
:missing_certutil
echo ERROR: Windows certutil.exe is unavailable; the integrity check cannot run.
goto failure
:hash_error
echo ERROR: Client integrity check failed. Stop and request a fresh ZIP. Do not bypass security.
goto failure
:network_error
echo ERROR: Cannot reach Jack's server. He must start Play.cmd first.
echo Use the main home Wi-Fi, not isolated guest Wi-Fi. Host: %LAN_HOST%
goto failure
:config_error
echo ERROR: The host did not return the expected revision-950 LAN configuration.
goto failure
:folder_error
echo ERROR: Cannot use this folder. Extract to a writable local folder such as Desktop.
goto failure
:host_error
echo ERROR: This is the server installation. Use normal Play.cmd on Jack's PC.
goto failure
:client_error
echo ERROR: The client exited with an error. Report any Windows message or missing DLL name.
echo If security software blocked it, stop and report it. Do not change security settings.
:failure
if /i "%~1"=="--check-only" exit /b 1
pause
exit /b 1
