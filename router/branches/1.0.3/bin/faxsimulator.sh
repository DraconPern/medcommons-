#!/bin/bash
#
# Bash script to start modality simulator
#
# Usage:  faxsimulator.sh <URL> <xml file>
#
# $Author: $
#
if [ -z $1 ];
then
  echo "Usage: faxsimulator.sh <URL> <xml file>";
  exit 1;
fi

# Detect if we are actually running under windows and use the right separator
SEPARATOR=":"
if [ `uname | grep -c CYGWIN` = 1 ];
then
  SEPARATOR="\\;";
fi


java -classpath "../build/classes${SEPARATOR}../lib/apache/commons-httpclient-2.0.2.jar${SEPARATOR}../lib/apache/axis/commons-logging-1.0.4.jar" net.medcommons.simulator.DataOnCallFax $1 $2
