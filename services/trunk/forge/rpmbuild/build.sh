#!/bin/bash
#
# One of "test" - which builds to appliance.medcommons.net/0.3.12/test/
#     or "dist" - which builds to appliance.medcommons.net/0.3.12/dist/
#
# Appliance /etc/yum.repos.d/medcommons.repos testing and release
# configurations correspond to these repositories
#
export FORGE_REPO="test"
if [ ! -z "$1" ]; then
	export FORGE_REPO=${1}
fi

# Check if already running
if [ -e /tmp/forge.pid ];
then
  OLDPID=`cat /tmp/forge.pid`
  if ps -ef | grep forge | grep -v grep | grep -q $OLDPID; 
  then
    echo
    echo "Found existing process $OLDPID corresponding to a possibly in-progress build"
    echo "Please check and remove /tmp/forge.pid if this build is no longer running."
    echo
    exit 1
  fi
fi

echo
echo "No other build is running"
echo
echo $$ > /tmp/forge.pid

#
# Appliance Repository
#
export FB_REPO="appliance.medcommons.net"
export FB_RREV="0.3.12"

#
# This roots the builds both here and in the package scripts...
#
export FB_ROOT="/home/forge/builds/trunk"
export FB_TRUNK="trunk"
#
# ...except for rpmbuild (which this localizes - every build needs one
# of it's own that sets %_topdir to the build's rpmbuild directory)
#
cat ./.rpmmacros > ${HOME}/.rpmmacros

function runbuild() {
	echo "" &&
	echo ">>->> $1 " &&
	sh ${1}.sh
}

function banner() {
	echo "" &&
	echo "========== ${1} =========" &&
	echo ""
}

banner "Medcommons Package and Repo Build of `date` on `hostname` to Repository $FORGE_REPO" &&

pushd ${FB_ROOT} &&

rm -f svn.log &&

banner "SVN Update" &&

svn update router/components >> svn.log &&
svn update router/demo >> svn.log &&
svn update services >> svn.log &&
#svn update TIMC >> svn.log &&
cat svn.log &&

banner "Router Build" &&

cd ${FB_ROOT}/router/demo &&
ant real-clean &&
ant update-version &&
ant build-timestamp && 
ant installer-tomcat &&

banner "DDL Build" && {
  # ssadedin: avoid building this if we can - it takes forever
  #if grep -q dicomclient ${FB_ROOT}/svn.log || [ ! -e ${FB_ROOT}/router/components/dicomclient/trunk/build/dist ]; 
  #then
    cd ${FB_ROOT}/router/components/dicomclient/trunk &&
    gant cleanall &&
    gant;
  #else
    #echo "Skipping DDL build due to no changes"
  #fi
} &&

banner "Identity Build" &&

cd ${FB_ROOT}/services/${FB_TRUNK}/java/identity &&
ant clean &&
ant &&
popd &&

banner "Package Builds" &&

#
# These unconditionally bump the version number in the SPECS/X.spec file and build the RPM. 
#  Uncomment config to build/distribute changes to /usr/bin/medcommons-commands
#  Uncomment developers to build/distribute changes to dev/administrator individuals/credentials
#  medcommons and medcommons-tomcat (possibly also jai and jsvc don't currently build on forge).
#
runbuild medcommons-DDL &&
runbuild medcommons-config &&
runbuild medcommons-console &&
runbuild medcommons-developers &&
runbuild medcommons-gateway &&
runbuild medcommons-identity &&
runbuild medcommons-orders &&
#runbuild medcommons-jai &&
#runbuild medcommons-jsvc &&
runbuild medcommons-mc_backups &&
runbuild medcommons-mc_locals &&
runbuild medcommons-php &&
runbuild medcommons-schema &&
runbuild medcommons-schema_db &&
runbuild medcommons-tomcat &&
runbuild medcommons-tomcat-native &&
runbuild medcommons &&
runbuild medcommons-tools &&

banner "Publish" &&

sh publish.sh &&

echo
echo "Removing PID flag ..."
echo
rm -f /tmp/forge.pid

banner "Yum repository ${FB_REPO} successfully updated."
