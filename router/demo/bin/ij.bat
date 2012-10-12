rem !/bin/bash
rem 
rem  This script starts Derby in Network Server mode which allows
rem  connection from remote clients.
rem 
rem

@echo off

rem  Check the directory is correct
if exist bin goto binok
echo -
echo Please run this script from the root of the distribution.
echo -

:binok

set CP=./conf;./build;./conf;lib/derby/db2jcc.jar;lib/derby/db2jcc_license_c.jar;lib/derby/derby-hibernate.jar;lib/derby/derby.jar;lib/derby/derbynet.jar;lib/derby/derbytools.jar

rem  Figure out the derby system home
set DB_HOME=%cd%\stage\tomcat\data\derby

echo Running with classpath = %CP% 
echo Running with DB_HOME = %DB_HOME%

rem  Launch the Derby network server
java -classpath %CP%  -Dderby.system.home=%DB_HOME% -Dij.connection.routerDb=jdbc:derby:routerdb;user=medcommons;password=d3rbi org.apache.derby.tools.ij %1 %2 %3 %4  

