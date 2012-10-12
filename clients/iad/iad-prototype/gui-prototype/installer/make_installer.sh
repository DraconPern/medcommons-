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

NSIS_MAKE='/c/Program Files/NSIS/makensis.exe'
if [ ! -e "$NSIS_MAKE" ];
then
  err "Cannot find NSIS make at $NSIS_MAKE.  Please make sure you installed NSIS!"
fi


if [ z$1 == 'z-patch' ];
then
  INSTALL_SCRIPT=medcommons-patch.nsi
else
  INSTALL_SCRIPT=medcommons.nsi
fi

"$NSIS_MAKE" $INSTALL_SCRIPT 
