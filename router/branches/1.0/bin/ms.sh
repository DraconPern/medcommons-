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

IMAGEDIR="$2"
uname | grep -qi cygwin && {
  IMAGEDIR=`cygpath -ma "$2"`
}

java -classpath "../build/dist/modality-simulator.jar${SEPARATOR}../lib/gnu/getopt.jar${SEPARATOR}../lib/apache/axis/log4j-1.2.8.jar${SEPARATOR}../lib/dcm4che/dcm4che.jar" net.medcommons.simulator.Modality $1 $IMAGEDIR
