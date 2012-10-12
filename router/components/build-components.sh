#!/bin/bash
############################################
#      MedCommons component build script
# This script builds the medcommons components in the correct
# order. It calls the ant build.xml file in each subdirectory.
#
# It would be better if this was a ant scxript - but there 
# appear to be some problems with ivy directories in nested
# calls to ant.
#
# This code is fragile in the following sense: if a new
# version of 'gateway-interfaces' was created and published -
# each of the ivy files in other directories needs to be 
# modified to take the new version - which may or may not mean
# that the project with a modified ivy file itself becomes a 
# new version.
############################################

pushd medcommons-utils/trunk
ant clean publish || err "Failed building utils"
popd

pushd gateway-interfaces/trunk
ant clean publish || err "Failed building gateway-interfaces"
popd

pushd configuration/trunk
ant clean publish || err "Failed building configuration"
popd

pushd crypto/trunk
ant clean publish || err "Failed building crypto"
popd

pushd activitylog/trunk
ant clean publish || err "Failed building activitylog"
popd

pushd services-api/trunk
ant clean publish || err "Failed building services-api"
popd

pushd phrdb/trunk
ant clean publish || err "Failed building phrdb"
popd

pushd ccrxmlbean/trunk
ant clean publish || err "Failed building ccrxmlbean"
popd

pushd dicomclient/trunk
ant clean publish || err "Failed building dicomclient"
popd



