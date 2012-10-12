REM modality-simulator.bat <DICOM URL> <directory>
java -classpath "../tomcat/webapps/router/WEB-INF/lib/modality-simulator.jar;..\build\dist\modality-simulator.jar;..\lib\gnu\getopt.jar;..\lib\apache\axis\log4j-1.2.8.jar;..\lib\dcm4che\dcm4che.jar;..\lib\getopt.jar;..\lib\log4j-boot.jar" net.medcommons.simulator.Modality %1 %2 %3
