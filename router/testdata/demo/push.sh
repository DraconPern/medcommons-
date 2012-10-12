#!/bin/bash 
SERVERS="http://healthurl.medcommons.net http://tenth.medcommons.net http://timc.medcommons.net http://n0001.medcommons.net http://n0000.medcommons.net"
cd transferapp
for s in $SERVERS;
do

  echo ""
  echo "----------------------------------- $s ----------------------------------------------"
  echo 
  echo ""
  ./transfer.sh $s ../demo 4e9396cc8817d36327370a219e5bb7692ca26131
  echo 
  echo "----------------------------------- end $s ----------------------------------------------"

done


