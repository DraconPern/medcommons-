#!/bin/bash
########################################################
#
# $Id: ci.sh 2932 2008-10-01 06:20:36Z ssadedin $
#
# Continuous Integration Script for building components
#
# Author:  Simon Sadedin, MedCommons Inc.
#
########################################################

echo
echo "==================================================================="
echo
echo " Running Continuous Integration Update for MedCommons Components"
echo
echo " "`date`
echo
echo "==================================================================="
echo

#svn update ci
source ci/cilib.sh

# Note: components should be listed in dependency order so that latter components have their dependencies met
: ${COMPONENTS="configuration medcommons-utils ccrxmlbean gateway-interfaces crypto activitylog services-api appliance-api phrdb medcommons-cxplibrary medcommons-transfer"}

for c in $COMPONENTS;
do

  pushd $c/trunk > /dev/null

  svn update > update.log
  #if svn_log_has_no_changes update.log;
  #then
  #  msg "No changes in $c"
  #else
  #  svn update build.number
    msg "Building $c"
    if [ $c == "ccrxmlbean" ];
    then
      ant clean;  ant -f build-first.xml;  ant publish || err "Unable to build component $c"
    elif [ -f build.gant ]
    then
      gant publish || err "Unable to build component $c"
    else
      ant clean publish || err "Unable to build component $c"
    fi
  #fi

  popd > /dev/null
done
