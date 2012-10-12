#!/bin/sh

#
#  This program is used in CaaS client vm. 
#
#  This shell script should be owned by root and chmod to 700. 
#

# ################################################################
# Copyright (C) 2009, All Rights Reserved, by
# EMC Corporation, Hopkinton, MA.
#
# This software is furnished under a license under either the EMC Cloud
# Infrastructure ISV Program Agreement, the EMC Velocity Atmos Partner
# Program Agreement,or the Atmos OnLine Compute and Storage Service
# Agreement (License) and may be used and copied only  in  accordance
# with  the  terms  of such  License and with the inclusion of the above
# copyright notice. This software or  any  other copies thereof may not
# be provided or otherwise made available to any  other person.
# No title to and ownership of  the  software  is  hereby transferred.
#
# The information in this software is subject to change without  notice
# and  should  not be  construed  as  a commitment by EMC Corporation.
#
# EMC assumes no responsibility for the use or  reliability  of its
# software on equipment which is not supplied by EMC.
# ################################################################

DESTDIR=""
BIN="$DESTDIR/usr/bin"
INIDIR="$DESTDIR/etc/init.d"

PATH=$PATH:/sbin:/usr/sbin

SRCDIR=$(echo $0 | sed 's/install\.sh$//')./

# Installing maui client tool
echo "Installing maui client tool ..."
mkdir -p $BIN || exit $?
mkdir -p $INIDIR || exit $?
cp -f $SRCDIR/python/caas_getkey $BIN || exit $?
cp -f $SRCDIR/python/caas_getnfsserver $BIN || exit $?
cp -f $SRCDIR/python/caas_getldap $BIN || exit $?
cp -f $SRCDIR/shell/caas_configurehostname $BIN || exit $?
cp -f $SRCDIR/shell/mauinfs $INIDIR || exit $?
chmod 755 $INIDIR/mauinfs || exit $?
chmod 700 $BIN/caas_getkey || exit $?
chmod 700 $BIN/caas_getnfsserver || exit $?
chmod 700 $BIN/caas_getldap || exit $?
chmod 700 $BIN/caas_configurehostname || exit $?

echo "Installation done!"

