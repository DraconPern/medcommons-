#!/bin/bash
#
# This script starts Derby in Network Server mode which allows
# connection from remote clients.
#

# Check the directory is correct
if [ ! -e bin ];
then
  echo
  echo "Please run this script from the root of the distribution."
  echo
  exit 1;
fi

# Get the util scripts
. bin/utils.sh

# Detect if we are actually running under windows and use the right separator
SEPARATOR=":"
if [ `uname | grep -c CYGWIN` = 1 ];
then
  SEPARATOR="\\;";
fi

# Now set classpath
CP=./build$SEPARATOR./conf

# Figure out the derby system home
DB_HOME=$PWD/stage/tomcat/data/derby
if [ `uname | grep -c CYGWIN` = 1 ];
then
  DB_HOME=`cygpath -ma "$DB_HOME"`;
fi


# Add everything in lib
for i in `ls lib/derby/*.jar`;
do
  CP="${CP}${SEPARATOR}$i";
done
msg "Running with classpath = $CP"
msg "Running with DB_HOME = $DB_HOME"

# Launch the Derby network server
java -classpath "$CP"  -Dderby.system.home="$DB_HOME" "-Dij.connection.routerDb=jdbc:derby:routerdb;user=medcommons;password=d3rbi" org.apache.derby.tools.ij $*

