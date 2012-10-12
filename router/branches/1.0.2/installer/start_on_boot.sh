#!/bin/bash

INSTALLDIR="$1"
INSTALLUSER="$2"

RCLOCAL=/etc/rc.local

[ ! -f $RCLOCAL ] && {
	RCLOCAL=/etc/rc.d/rc.local
}

[ ! -f $RCLOCAL ] && {
  echo
  echo "Error: unable to find rc.local file to add boot startup."
  echo
  exit 1;
}

if grep -q "$INSTALLDIR" $RCLOCAL;
then
  echo
  echo "This MedCommons installation is already setup to start on boot."
  echo
  exit 0
fi

# Set blank arg for sudo if we are running under cygwin
uname | grep -qi CYGWIN || SUDO="sudo -u $2"

cat >> $RCLOCAL <<!

# MedCommons Router Startup
$SUDO "$INSTALLDIR/bin/start.sh"
!

