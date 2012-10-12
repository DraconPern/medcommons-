#!/bin/bash
#
# Bash script to start transfer application
#
# Usage:  transfer.sh <medcommons-id> <directory>
#
# $Author: $
#
if [ -z $1 ];
then
  echo "Usage:transfer.sh <host> <directory> [<auth>]";
  echo
  echo "Example:  transfer.sh http://localhost:9080 ~/images"
  echo "Example:  transfer.sh http://localhost:9080/gateway/services/CXP2 ~/images"
  exit 1;
fi

# Detect if we are actually running under windows and use the right separator
SEPARATOR=":"
if [ `uname | grep -c CYGWIN` = 1 ];
then
  SEPARATOR="\\;";
fi

GATEWAY="$1"
# Add the CXP2 bit if user didn't provide it
if [[ ! "$GATEWAY" =~ '.*/gateway/services/CXP2' ]];
then
  GATEWAY="$GATEWAY/gateway/services/CXP2";
fi

IMAGEDIR="$2"
uname | grep -qi cygwin && {
  IMAGEDIR=`cygpath -ma "$2"`
}

#java -classpath "build/dist/medcommons-transfer-application.jar${SEPARATOR}lib/xfire-1.2-RC/lib/log4j-1.2.6.jar${SEPARATOR}lib/dcm4che-2.0.7/lib/dcm4che-core-2.0.7.jar${SEPARATOR}lib/dcm4che-2.0.7/lib/nlog4j-1.2.19.jar" net.medcommons.application.transfer.TransferAgent $MEDCOMMONSID $IMAGEDIR
echo "java -jar build/dist/medcommons-transfer-application.jar $GATEWAY $IMAGEDIR"

java -jar "build/dist/medcommons-transfer-application.jar" $GATEWAY "$IMAGEDIR" "$3"
