#!/bin/bash
function msg() {
  echo
  echo "$1"
  echo
}

function err() {
  echo
  echo "ERROR:  $1"
  echo
  exit 1;
}

function usage() {
  msg "Usage:  make_installer.sh ( linux | windows ) [-p] [-j]"
}

TARGET_OS="$1"
shift

BASENAME="MedcommonsSetup"

PATCH=false
INSTALLER_TYPE=TOMCAT

while getopts "pot:" options; do
  case $options in
    p ) PATCH=true;;
    j ) INSTALLER_TYPE=JBOSS;;
    h ) echo "$usage";;
    \? ) echo "$usage"
         exit 1;;
    * ) echo "$usage"
          exit 1;;
  esac
done

if $PATCH ;
then
  BASENAME="$BASENAME-patch"
fi

if [ -z "$TARGET_OS" ];
then
  usage
  err "No target OS specified:  please specify either 'linux' or 'windows' on command line"
fi 

msg "Creating $INSTALLER_TYPE installer"

VERSION=`cat ../src/net/medcommons/public_version.txt` ||
  err "Unable to read public version from public_version.txt.  Please check this file exists and is readable."
  
REVISION=`cat ../src/net/medcommons/version.txt` || 
  err "Unable to read revision from version.txt.  Please check this file exists and is readable."

case $TARGET_OS in
  linux)
    OUTPUT="$BASENAME-$VERSION.sh"
    INSTALLERDIR="$PWD"
    cp install.sh start_on_boot.sh ../build/installer
    pushd ../build/installer > /dev/null
    tar -czf $INSTALLERDIR/medcommons.tar.gz . ||
      err "Unable to create tar file for inclusion in installer."
    popd > /dev/null
    sed "s/##VERSION##/$VERSION/g;  s/##REVISION##/$REVISION/g;" header.sh > header.tmp.sh || 
      err "Unable to substitute version and build strings"
    cat header.tmp.sh medcommons.tar.gz > $OUTPUT ||
      err "Unable to create installable file."
    chmod uga+x $OUTPUT
    rm -f header.tmp.sh
  ;;
  windows)
    OUTPUT="$BASENAME-$VERSION.exe"
    NSIS_MAKE='/cygdrive/c/Program Files/NSIS/makensis.exe'
    if [ ! -e "$NSIS_MAKE" ];
    then
      err "Cannot find NSIS make at $NSIS_MAKE.  Please make sure you installed NSIS!"
    fi
    if $PATCH;
    then
      INSTALL_SCRIPT=medcommons-patch.nsi
      NSIS_OUT=medcommons-patch.exe
    else
      INSTALL_SCRIPT=medcommons.nsi
      NSIS_OUT=medcommons.exe
    fi
    "$NSIS_MAKE" /D"$INSTALLER_TYPE"=true /DVERSION="$VERSION" /DREVISION="$REVISION" /DSCHEMA="$SCHEMA" $INSTALL_SCRIPT
    cp $NSIS_OUT $OUTPUT;;
esac
  
msg "Installer successfully created."
msg "Output file is $OUTPUT"

