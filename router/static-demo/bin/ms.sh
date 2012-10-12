#!/bin/bash
#
# Bash script to start modality simulator
#
# Usage:  ms.sh <DICOM URL> <directory>
#
# $Author: $
#
if [ -z $1 ];
then
  echo "Usage: ms.sh <DICOM URL> <directory>";
  exit 1;
fi

# Detect if we are actually running under windows and use the right separator
SEPARATOR=":"
if [ `uname | grep -c CYGWIN` = 1 ];
then
  SEPARATOR="\\;";
fi

java -classpath "../build/dist/modality-simulator.jar${SEPARATOR}../lib/jboss/getopt.jar${SEPARATOR}../lib/jboss/log4j.jar${SEPARATOR}../lib/dcm4che/dcm4che.jar" net.medcommons.simulator.Modality $1 $2
