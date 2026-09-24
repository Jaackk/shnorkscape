@echo off
rem The cumulative candidate includes the Chat and Follow changes.
call "%~dp0Apply Staged Update.cmd" %*
exit /b %errorlevel%
