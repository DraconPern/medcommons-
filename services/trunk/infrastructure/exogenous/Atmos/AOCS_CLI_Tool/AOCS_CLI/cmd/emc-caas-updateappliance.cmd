@echo off

setlocal

set CHK_HOME=_%CLOUD_HOME%

if "%CHK_HOME:"=%" == "_" goto HOME_MISSING

"%CLOUD_HOME:"=%\cmd\emc-caas-cmd" updateAppliance %*
goto DONE
:HOME_MISSING
echo CLOUD_HOME is not set
exit /b 1

:DONE