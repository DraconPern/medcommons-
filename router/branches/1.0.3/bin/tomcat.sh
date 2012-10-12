#!/bin/bash
###############################################################
#
# Tomcat Runner Script
#
# Usage:
#
#  tomcat.sh [debug]
#
###############################################################

###############################################################
#
# Output an error message and exit
#
###############################################################
function err() {
  echo
  printf "$1\n"
  echo
  exit 1;
}

###############################################################
#
# Output a message
#
###############################################################
function msg() {
  echo
  echo "$1"
  echo
}

START_COMMAND=run
if [ debug == "$1" ];
then
  START_COMMAND="jpda start"
fi

# Try and orient ourselves in the right directory
if [ -f tomcat_loop.sh ];
then
  cd ..
fi

# Check if derby needs to be started 
if grep -q "^NoRunDb=true$" stage/tomcat/conf/MedCommonsBootParameters.properties;
then
  echo
  echo "Derby configured to run separately. Make sure you are running Derby"
  echo
  echo "Eg. using ./bin/derby.sh"
  echo
fi

if [ -d stage ];
then
  cd stage/tomcat || err "Unable to change to Tomcat directory.  Please check application is correctly deployed."
else
  [ -e webapps ] || err "Unablet to find tomcat/webapps directory.  Please run this script from the root of your router distribution."
fi

# Not sure why, but ant chmod doesn't seem able to set the perms in a way that Cygwin accepts
chmod uga+rx bin/*.sh bin/*.bat

# Run tomcat
./bin/catalina.sh $START_COMMAND; 

