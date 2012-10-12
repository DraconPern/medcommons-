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

# Figure out the derby system home
DB_HOME=$PWD/stage/tomcat/data/derby

# Get the util scripts
. bin/utils.sh

# Detect if we are actually running under windows and use the right separator
SEPARATOR=":"
if [ `uname | grep -c CYGWIN` = 1 ];
then
  SEPARATOR="\\;";
  DB_HOME=`cygpath -ma "$DB_HOME"`;
fi

# Now set classpath
CP=./build$SEPARATOR./conf


# Add everything in lib
for i in `ls lib/derby/*.jar`;
do
  CP="${CP}${SEPARATOR}$i";
done

msg "Running with classpath = $CP"
msg "Derby system home is $DB_HOME"

# Run javaw if it exists, otherwise java
JAVA=javaw
type $JAVA > /dev/null || JAVA=java

# Launch the Derby network server
$JAVA -classpath "$CP" -Dderby.system.home="$DB_HOME" org.apache.derby.drda.NetworkServerControl start 

