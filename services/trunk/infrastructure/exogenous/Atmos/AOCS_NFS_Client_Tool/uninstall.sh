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

echo "Uninstalling maui client tool"
rm -rf $BIN/caas_getkey $BIN/caas_getldap $BIN/caas_getnfsserver $INIDIR/mauinfs $BIN/caas_configurehostname

if [ "$?" -eq "0" ]; then
	echo "Uninstallation Done!"
else
	echo "$? Uninstalling fail"
fi



