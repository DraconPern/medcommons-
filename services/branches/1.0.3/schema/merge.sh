#!/bin/bash
if [ -z "$1" ];
then
  echo "Usage: merge.sh <svn change>"
  exit 1;
fi
echo
for i in $*; 
do
  echo "Merging change $i ..."
  echo
  svn merge -c $i https://svn.medcommons.net/svn/services/trunk/schema .
done
