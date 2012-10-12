#!/bin/bash
#
############################################################################
#
# MedCommons Installer Script
#
# This is the master script which does a brand new clean build to create an
# executable installer.
#
# REQUIREMENTS:
#
#   Cygwin
#   PsKill (www.sysinternals.com)
#   Subversion command line client (install with Cygwin)
#
# $Id:$
#
############################################################################
PATCH=false

usage='
make_installer.sh [-pj]

Options:
  -p : creates a patch installer instead of a full installer
'

JBOSS=false
while getopts "po:" options; do
  case $options in
    p ) PATCH=true;;
    j ) JBOSS=true; JBOSSARG=-j;;
    h ) echo "$usage";;
    \? ) echo "$usage"
         exit 1;;
    * ) echo "$usage"
          exit 1;;
  esac
done

echo
echo "PATCH=$PATCH"
echo



# Allow to run inside bin directory
if [ -e make_installer.sh ];
then
  cd ..
fi

# Check we are located correctly
[ -e schema ] || { 
  echo; echo "Please run this script in the root directory of the distribution to create the installer from"; echo; exit 1; 
}

if [ -e bin ];
then
  . bin/utils.sh
fi

INSTALLER_TARGET=installer-tomcat
if $PATCH;
then
  msg "Creating patch build...";
  INSTALLER_TARGET="installer-patch";
  PATCH=true;
  PATCHARG="-p"
else 
  PATCH=false;
fi

if $JBOSS;
then
  INSTALLER_TARGET="installer-jboss";
fi

msg "Stopping all Java applications..."
killproc java || err "Could not run 'killproc java'.  Do you have pskill/pkill installed?"

if [ -f build.properties ]
then
  msg "Temporarily disabling custom build.properties ..."
  mv build.properties build.properties.bak || err "Unable to backup build.properties.";
fi

$JBOSS && {
  msg "Cleaning existing JBoss deployment..."
  rm -Rf stage/jboss-3.2.3/server/router || err "Unabled to clean router deployment.  Is JBoss running?";

  msg "Reinstalling JBoss ..."
  ant jboss || err "Unable to reinstall JBoss.  Please check that stage directory is not locked."
}

$JBOSS || {
  msg "Cleaning existing Tomcat deployment..."
  rm -rf stage/tomcat
}

msg "Cleaning build..."
ant clean || err "Unable to clean.  Please ensure build directory is available for deletion.";

# Ensure code is up to date
#svn update . || err "Unable to update code.  Please check that Subversion is installed and working."

# Figure out the version of the schema that the current build is using
mkdir -p build/installer
svn -N update schema > /dev/null || err "Unable to update schema directory with revision info from svn."
SCHEMA=`svn info schema | grep "Last Changed Rev:" | awk '{ print $4 }'`
echo "$SCHEMA" > build/installer/schema_version.txt
echo "$SCHEMA" > src/net/medcommons/schema_version.txt

if [ $PATCH == false ];
then

  sleep 1;

  # ./bin/derby.sh > derby.log 2>&1 &

  #waitFor derby.log "Server is ready to accept connections" 1 30 ||
  #  err "Timed out waiting for derby to start.  Please check derby starts successfully."
  


  msg "Killing Java procs ..."
  killproc java
fi

ant $INSTALLER_TARGET || err "Error compiling or deploying.  Please investigate and fix.";

# For some reason derby creates a 1M log file - it seems to be ok
# to remove it, which helps reduce the installer size.
#rm -f build/installer/tomcat/data/derby/routerdb/log/log2.dat

#ant deploy || err "Unable to build and deploy code.  Please check that build works."


if [ ! -f build.properties ] && [ -e build.properties.bak ];
then  
  msg "Restoring original build.properties ..."
  mv build.properties.bak build.properties || err "Unable to restore original build.properties.  sorry.";
fi

echo "Build version is `cat src/net/medcommons/version.txt`"; 
echo "Build timestamp is `cat src/net/medcommons/timestamp.txt`"; 



# Run the nsis installer
msg "Running installer script..."
cd installer

if $PATCH;
then
  PATCHARG=-patch
fi

msg "Making linux installer..."
./make_installer.sh linux $JBOSSARG $PATCHARG

# Can't make a windows installer unless we are running on cygwin
cygwin && {
  msg "Making windows installer... (see log nsis.log)"
  ./make_installer.sh windows $JBOSSARG $PATCHARG > nsis.log
}

echo
echo "Done!"
echo

