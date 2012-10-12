#!/bin/bash
#
# Cygwin Script to run MedCommons Self Tests
#

if [ z$1 == 'z-patch' ];
then
  INSTALLER=medcommons-patch.exe
  DELAY=10000
else
  INSTALLER=medcommons.exe
  DELAY=120000
fi

# Target host
TEST_HOST=192.168.101.22

cp $INSTALLER installer.exe

# Start the thttpd server to deliver the file
thttpd -p 8050 &

# Make it non-executable or httpd won't touch it

chmod uga-x installer.exe

# Start vnc viewer
vncviewer FullScreen=0 192.168.101.22 &

sleep 5


# Run badboy script to execute tests
bbcmd -i 1 -D downloadTime=$DELAY iad-test.bb

