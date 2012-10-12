#!/bin/bash
###############################################################
#
# Tomcat Runner Script - Runs tomcat in a loop.  Useful
# for development purposes.  After tomcat exits, will wait
# for a file "restart" to appear in the tomcat directory after
# which it will restart tomcat.  This means that you can run
# something like:
#
#   pkill java && ant && echo > stage/tomcat/restart
#
# from one terminal and it will stop, build and restart tomcat
# which may be running in another terminal.
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
if [ -d stage ];
then
  cd stage/tomcat || err "Unable to change to Tomcat directory.  Please check application is correctly deployed."
else
  [ -e webapps ] || err "Unablet to find tomcat/webapps directory.  Please run this script from the root of your router distribution."
fi

# Run in infinite loop, restarting when the restart signal is found
while true; 
do 
  # Clear restart signal
  rm -f restart; 

  # Run tomcat
  ./bin/catalina.sh $START_COMMAND; 

  msg "Stopped.  Waiting for restart signal ..."

  # Exited - wait for restart
  while [ ! -f restart ]; 
  do 
    sleep 1; 
  done; 
  
  # Found restart signal
  echo; 
  echo "Restarting..."; 
  echo; 
  sleep 1; 
done

