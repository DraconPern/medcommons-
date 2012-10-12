#!/bin/bash
############################################################
# 
# Appliance Gateway Update Script
#
# Copies a new version of the gateway from a source server
# and installs it on the local machine in standard locations.
#
# NOTE: the update performed is not a complete update.  Only
# the war files are deployed.  This is not a complete update.
#
############################################################

# Location where war files will be sync'ed from 
: ${DIST_PATH:="/home/ci/build/demo/build/"}
: ${DIST_SOURCE:="ci@mcpurple04.medcommons.net:$DIST_PATH"}

function er() {
  echo "Error: $1";
  echo
  exit 1
}
echo
echo "Syncing files from $DIST_SOURCE"
echo
rsync -rv --progress "$DIST_SOURCE/installer/tomcat/conf $DIST_PATH/installer/tomcat/data  $DIST_PATH/dist/*.war " . || er "Unable to copy property files"
echo

# backup code
echo "Making backups ..."
echo
[ ! -d /mnt/backups ]  || [ ! -w /mnt/backups ] || [ ! -x /mnt/backups ]  && {
  er "/mnt/backups does not exist.  Please create and assign rwx permissions to $USER";
}

# Require 2000M space
let REQ='1000*1024'
FREE=`df -k . | awk '{ print $4 }' | grep "[0-9]"`
[ "$FREE" -lt $REQ ] && {
  er "Less than $REQ k free space available.  Please increase free space before upgrading.";
}

TS=`date +'%Y%m%d_%H%M%S'`
pushd /opt/gateway/webapps || er "Unable to change directory to expected gateway location /opt/gateway/webapps"
tar -czf /mnt/backups/router-$TS.tar.gz router || er "Unable to backup router"
tar -czf /mnt/backups/gateway-$TS.tar.gz gateway || er " Unable to backup gateway"
popd
echo
echo "Finished backup."
echo
echo "Stopping gateway ..." 
echo
DIR=`pwd`
sudo /etc/init.d/gateway stop
echo "Unzipping files ..."
echo
pushd /opt/gateway/webapps
sudo rm -rf /opt/gateway/webapps/router/* /opt/gateway/webapps/gateway/* 
cd router
sudo jar -xf "$DIR"/router.war | awk '{ printf "." }' || er "Unable to unzip router war file"
cd ../gateway 
sudo jar -xf "$DIR"/gateway.war | awk '{ printf "." }' || er "Unable to unzip gateway war file"
popd
echo "Copying Boot Parameters ..."
echo
cd conf
sudo cp -uv *.properties medcommons-config.xml /opt/gateway/conf/ || er "Unable to copy Boot Parameters file"
cd ..
sudo cp -uvR data/stylesheets /opt/gateway/data/ || er "Unable to data files"
echo
echo "Starting gateway ..." 
echo
sudo /etc/init.d/gateway start || er "Unable to start gateway "
echo "Done"
echo
