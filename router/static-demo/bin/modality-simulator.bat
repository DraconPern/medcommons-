REM modality-simulator.bat <DICOM URL> <directory>
java -classpath "..\build\dist\modality-simulator.jar;..\lib\jboss\getopt.jar;..\lib\jboss\log4j.jar;..\lib\dcm4che\dcm4che.jar;..\lib\getopt.jar;..\lib\log4j-boot.jar" net.medcommons.simulator.Modality %1 %2
