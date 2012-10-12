#!/bin/bash
echo
echo "==============================================================="
echo "MedCommons Installer ##VERSION## (Revision ###REVISION##)"
echo "==============================================================="
echo
echo "Unpacking installer - please wait..."
echo

# create a temp directory to extract to.
export WRKDIR=`mktemp -d /tmp/selfextract.XXXXXX`

SKIP=`awk '/^__ARCHIVE_FOLLOWS__/ { print NR + 1; exit 0; }' $0`

# Take the TGZ portion of this file and pipe it to tar.
tail -n +$SKIP $0 | tar xz -C $WRKDIR

# execute the installation script
export PREV=`pwd`
cd $WRKDIR
./install.sh $*

# delete the temp files
cd $PREV
rm -rf $WRKDIR

exit 0

__ARCHIVE_FOLLOWS__
