@echo off

setlocal

set CHK_JAVA_HOME=_%JAVA_HOME%
set CHK_CLOUD_HOME=_%CLOUD_HOME%
set CHK_CLOUD_ENDPOINT=_%CLOUD_ENDPOINT%

if "%CHK_CLOUD_HOME:"=%" == "_" goto CLOUD_HOME_MISSING
if "%CHK_JAVA_HOME:"=%" == "_" goto JAVA_HOME_MISSING
REM CLOUD_ENDPOINT is optional

REM If a classpath exists preserve it
SET CP=%CLASSPATH%

SET CP=%CP%;%CLOUD_HOME%\lib


REM grab class name
if "%1" == "" GOTO CANNOT_RUN

SET CMD=%1

REM SHIFT doesn't affect %* so we need this clunky hack
SET ARGV=%2
SHIFT
SHIFT
:ARGV_LOOP
IF (%1) == () GOTO ARGV_DONE
SET ARGV=%ARGV% %1
SHIFT
GOTO ARGV_LOOP
:ARGV_DONE
if "%CHK_CLOUD_ENDPOINT:"=%" == "_" goto CLOUD_ENDPOINT_MISSING
"%JAVA_HOME:"=%\bin\java" -Djava.util.logging.config.file=$CLOUD_HOME/src/logging.properties -DCLOUD_ENDPOINT=%CLOUD_ENDPOINT% -classpath "%CP%\*" com.emc.caas.restclient.%CMD% %ARGV% 
goto DONE
:CLOUD_ENDPOINT_MISSING
"%JAVA_HOME:"=%\bin\java" -Djava.util.logging.config.file=$CLOUD_HOME/src/logging.properties -classpath "%CP%\*" com.emc.caas.restclient.%CMD% %ARGV%
goto DONE
:JAVA_HOME_MISSING
echo JAVA_HOME is not set
exit /b 1

:CLOUD_HOME_MISSING
echo CLOUD_HOME is not set
exit /b 1

:CANNOT_RUN
echo This command is not supposed to run.
exit /b 1

:DONE
endlocal
