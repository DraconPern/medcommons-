rem run this to setup an ODBC connection to the Router database
rem you need to install the DB2 DRDA client first
rem
rem
echo off
db2 CATALOG TCPIP NODE testnode REMOTE localhost SERVER 1527
db2 CATALOG DB routerdb AT NODE testnode AUTHENTICATION SERVER

echo -
echo Now go to your ODBC Settings in Control Panel and add a connection for ROUTERDB
echo -



