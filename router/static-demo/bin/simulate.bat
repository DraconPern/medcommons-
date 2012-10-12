@echo off
REM
REM Utility script to populate sample images from a MedCommons repository
REM
REM Usage:  simulate.bat <path to image location>
REM
REM Example:  simulate.bat h:\medcommons\svn\router\main\etc\static-files\images
REM 
REM
REM

if not "%1" == "" goto imagesdefined
@echo -
@echo Please provide the path to the images you wish to load.
@echo -
@echo - Usage: simulate.bat [path to image location]
goto finish

:imagesdefined

set IMAGEPATH=%1
echo IMAGEPATH=%IMAGEPATH%
for /d %%j in ("%IMAGEPATH%\*.*") do call ./modality-simulator.bat dicom://ARCHIVE:DCMSND@127.0.0.1:3002 %%j

:finish

