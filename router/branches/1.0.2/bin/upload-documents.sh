#!/bin/bash
#
# Bash script to upload documents to specified CXP server
#
# Usage:  upload-documents.sh <CXP Endpoint> <directory>
#
# $Author: $
#
if [ -z $1 ];
then
  echo "Usage: upload-documents.sh <CXP Endpoint> <directory>";
  echo "Example: upload-documents.sh http://localhost:9080/gateway/services/CXP2 ~/demodata"
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

MODULE_ROOT="../build/stage/modules"

CP="${MODULE_ROOT}/medcommons-cxp.jar${SEPARATOR}${MODULE_ROOT}/medcommons-crypto.jar${SEPARATOR}${MODULE_ROOT}/medcommons-utils.jar"
CP="${CP}${SEPARATOR}../build/dist/medcommons-transfer-application.jar"
CP="${CP}${SEPARATOR}../lib/xfire-1.2.2/xfire-all-1.2.2.jar${SEPARATOR}../lib/xfire-1.2.2/lib/activation-1.1.jar"
CP="${CP}${SEPARATOR}../lib/xfire-1.2.2/lib/wsdl4j-1.5.2.jar${SEPARATOR}../lib/xfire-1.2.2/lib/commons-logging-1.0.4.jar"
CP="${CP}${SEPARATOR}../lib/xfire-1.2.2/lib/stax-api-1.0.1.jar${SEPARATOR}../lib/xfire-1.2.2/lib/jdom-1.0.jar"
CP="${CP}${SEPARATOR}../lib/xfire-1.2.2/lib/commons-httpclient-3.0.jar${SEPARATOR}../lib/xfire-1.2.2/lib/commons-codec-1.3.jar"
CP="${CP}${SEPARATOR}../lib/xfire-1.2.2/lib/mail-1.4.jar${SEPARATOR}../lib/xfire-1.2.2/lib/jsr173_api-1.0.jar"
CP="${CP}${SEPARATOR}../lib/xfire-1.2.2/lib/stax-api-1.0.1.jar${SEPARATOR}../lib/xfire-1.2.2/lib/stax-utils-snapshot-20040917.jar"
CP="${CP}${SEPARATOR}../lib/xfire-1.2.2/lib/wstx-asl-3.0.1.jar"


CP="${CP}${SEPARATOR}../lib/apache/axis/log4j-1.2.8.jar${SEPARATOR}../lib/dcm4che/dcm4che.jar"

echo "Claspath is ${CP}";
java -classpath "${CP}" net.medcommons.application.transfer.UploadFileAgent $1 $IMAGEDIR
