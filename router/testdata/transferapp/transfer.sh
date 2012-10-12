#!/bin/bash
#
# Bash script to start transfer application
#
# Usage:  transfer.sh <host> <directory>
#
# $Author: $
#
if [ -z $1 ];
then
  echo "Usage:transfer.sh <host> <directory>";
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

if ! echo $GATEWAY | grep -q "^http";
then
  GATEWAY="http://$GATEWAY"
fi

IMAGEDIR="$2"
uname | grep -qi cygwin && {
  IMAGEDIR=`cygpath -ma "$2"`
}

echo "java -jar medcommons-transfer-application.jar $GATEWAY $IMAGEDIR"

java -jar "medcommons-transfer-application.jar" $GATEWAY "$IMAGEDIR"
