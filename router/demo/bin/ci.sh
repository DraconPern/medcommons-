#!/bin/bash
###################################################################
#
# MedCommons Continuous Integration Script
#
# This script is designed to be run by cron to perform
# a continuous integration style cycle for the MedCommons Router
# source code.
#
# The script can send emails and IM notifications when the build
# fails.  The recipients of these messages can be controlled 
# by setting environment variables (SPAM_LIST, IM_SPAM_LIST).
#
# This script supports the MedCommons "appliance" environment 
# when the -a flag is passed.  In this mode, files are deployed
# to standard locations used by the appliances and using 
# permissions compatible with the standard users and groups
# on the appliance.
#
# For appliance mode to work the user running this script must be
# be given certain sudo rights.  Lines such as the following 
# must be added to the sudoers file to achieve this:
#
# ci      ALL= NOPASSWD: /etc/init.d/gateway, /etc/init.d/tomcat, /usr/bin/unzip, /bin/rm -rf /opt/gateway/data, /bin/chown gateway -R /opt/gateway/data, /bin/cp identity.war /var/apache-tomcat/webapps, /bin/rm -rf /var/apache-tomcat/webapps/identity/
#
# ci      ALL= (gateway) NOPASSWD: /usr/bin/unzip, /bin/rm, /bin/cp
#
# Author:  Simon Sadedin, MedCommons Inc.
# 
####################################################################

if [ -e ci.sh ];
then
  cd ..
fi

if [ ! -d bin ];
then
  echo
  echo "Cannot find bin directory.  Please run this script from the root of the distribution."
  echo
  exit 1;
fi

# Get the utilities
. bin/utils.sh || { echo "Internal error:  unable to load build utilities script"; exit 1; }

. ./lib/dependencies/cilib.sh || { 
  ant dependencies || err "Unable to load cilib:  please run 'ant dependencies' to retrieve it."
  . ./lib/dependencies/cilib.sh || err "Problem loading cilib."
}

# Set default email notification list here
: ${SPAM_LIST:="ssadedin@badboy.com.au"} 

# Set default IM notification list here
: ${IM_SPAM_LIST:=""} 

# Set default MYSQL User here
: ${MYSQL_USER:="root"} 

# Set base directory of components if they are to be built before gateway
: ${COMPONENT_BASE_DIR:=""} 

# The demo data set needs an authentication token to upload successfully
# The token below is added by demodata.php to support the default demo
# data set.
: ${DEMO_GROUP_AUTH_TOKEN:="9e9596cc8117d36327370a219e5bb7692ca23137"} 

TIME_STAMP=`date +"%m_%d_%Y_%H_%M"`

usage='
ci.sh [-f] [-q] [-a]

Options:
  -f : forces the build to run even if there are no  updates from svn
  -q : send no mail
  -a : appliance mode: install artifacts into standard appliance locations instead of standalone
  -m : minimal - do not reload test data, etc.
'

APPLIANCE_MODE="false"
MINIMAL=false

CHECK_SVN_UPDATES=true;
while getopts "afmqb:" options; do
  case $options in
    f ) CHECK_SVN_UPDATES=false;;
    q ) SPAM_LIST=nobody;;
    m ) MINIMAL=true;;
    a ) APPLIANCE_MODE="true";;
    \? ) echo "$usage"
         exit 1;;
    * ) echo "$usage"
          exit 1;;
  esac
done

# Run in sub-shell so that we can catch any error
(
( 
  msg "Building Gateway: $TIME_STAMP";
  echo "Notifications: emails => $SPAM_LIST and IMs => $IM_SPAM_LIST";
  msg "Appliance mode = $APPLIANCE_MODE"

  # Get the utilities
  . bin/utils.sh || err "Internal error:  unable to load build utilities script";

  # Don't let the script run more than once simultaneously
  [ -f running ] && {
    echo > wasrunning;
    err "Previous test has not completed - presumed hung.  Please investigate."
  }

  rm -f wasrunning

  # Write the flag signaling that the build is running
  echo "" > running;


  # First, check if components are to be built
  if [ ! -z "$COMPONENT_BASE_DIR" ];
  then
    pushd "$COMPONENT_BASE_DIR" > /dev/null
    ./ci.sh || err "Unable to build components"
    popd
  fi

  # Now sync up the code (only if NO_SVN_UPDATE is NOT defined - useful for testing)
  rm -f svn.log
  [ -z "$NO_SVN_UPDATE" ] && {
    msg "Update Log:"
    svn log -r BASE:HEAD | awk 'BEGIN { linecount=0; }  /----/ { linecount++; }  { if(linecount >= 2) { print $0; } }' | tee svnchanges.log
    msg "Changes:"
    svn update . | tee svn.log || err "Unable to update code from subversion."
    ant update-version build-timestamp || err "Unable to update svn version or build timestamp"
  }

  # If there were no updates from subversion, do not run
  svn_log_has_no_changes svn.log && {
    if $CHECK_SVN_UPDATES ;
    then
      msg "No updates from svn.  Skipping this build iteration."
      rm -f running
      echo > "skipped"
      exit 0;
    fi
  }

  DO_BUILD=true
  DO_UNIT_TESTS=true
  DO_DEPLOY=true
  DO_TEST_IMAGES=true

  $MINIMAL && {
    DO_UNIT_TESTS=false
    DO_TEST_IMAGES=false
    ENABLE_WEB_TESTS=""
  }

  $CHECK_SVN_UPDATES && {
    svn_log_has_only_changes svn.log tests/ && {
      echo "Only Badboy tests changed in this checkin - no need for full build"
      DO_BUILD=false
      DO_UNIT_TESTS=false
      DO_DEPLOY=false
      DO_TEST_IMAGES=false
      CLEAN_MYSQL=""
    }
  }

  # Before killing things, block Badboy tests
  if [ ! -z "$ENABLE_WEB_TESTS" ];
  then 
        curl "http://badboy.medcommons.net:8030/wtm/activateBlock?block.id=753664" > activateBlock.html
  fi

  $DO_BUILD && {
    # Clean the code
    ! $MINIMAL && {
      ant clean || err "Unable to clean build."
    }

    # Get rid of any old installers
    rm -f installer/Medcommons*.sh

    # Make the installer
    ./bin/make_installer.sh || err "Error occurred while making installer"
  }

  # If CLEAN_MYSQL is enabled then do it
  [ ! -z $CLEAN_MYSQL ] &&  ! $MINIMAL && {
    msg "Cleaning MYSQL tables CLEAN_MYSQL=$CLEAN_MYSQL";
    PASS=""
    [ ! -z "$MYSQL_PASSWORD" ] && {
      PASS="-p$MYSQL_PASSWORD"
    }

    mysql -u $MYSQL_USER $PASS mcx <<!
    delete from document;
    delete from document_location;
    delete from tracking_number;
    delete from rights;
    delete from document_type;
    delete from ccrlog;
    delete from practiceccrevents;
    delete from node;
!

    mysql -u $MYSQL_USER $PASS facebook <<!
    delete from patients;
!


    # Since we deleted all the data, restore the test data
    for TEST_DATA_URL in "http://localhost/acct/testdata.php?refresh=true" "http://localhost/acct/demodata.php?refresh=true";
    do
      msg "Resetting Test Data using URL: $TEST_DATA_URL"
      curl "$TEST_DATA_URL"
    done
  }

  DIR=`pwd`



  $DO_DEPLOY && {
    # Deploy gateway code
    msg "Stopping gateway ..."
    sudo /etc/init.d/gateway stop || err "Unable to stop gateway ..."
    pushd /opt/gateway/webapps

    msg "Deploying router ..."
    sudo /bin/rm -rf /opt/gateway/webapps/router/*
    cd router
    sudo unzip -o -q $DIR/build/dist/router.war || er "Unable to unzip router war file"
    cd ..
    msg "Deploying gateway ..."
    sudo /bin/rm -rf /opt/gateway/webapps/gateway/*
    cd gateway
    sudo unzip -o -q $DIR/build/dist/gateway.war || er "Unable to unzip gateway war file"
    cd /opt/gateway

    if [ ! -z $CLEAN_MYSQL ] && ! $MINIMAL;
    then
      sudo /bin/rm -rf /opt/gateway/data
    fi
      
    cp -R $DIR/build/installer/tomcat/data .
    sudo /bin/chown gateway -R /opt/gateway/data
    sudo /bin/cp $DIR/etc/configurations/log4j.xml /opt/apache-tomcat/common/classes
    cp -R $DIR/build/installer/tomcat/conf/{MedCommonsBootParameters.properties,config.xml,medcommons-config.xml,SimpleRepositoryConfiguration.properties} conf
    popd

    # problem - the base unit tests cause a node key to be registered in MySQL!
    # should fix them but for now, kill it
    [ ! -z $CLEAN_MYSQL ] && ! $MINIMAL && {
      echo "delete from node;" | mysql -u $MYSQL_USER $PASS mcx 
    }

    msg "Starting gateway ..."
    sudo /etc/init.d/gateway start ; # exits with fail status even if succeeds :-(
    TOMCAT_LOG=/opt/gateway/logs/catalina.out

    # Wait for it to start
    msg "Waiting for tomcat to start ..."
    sleep 5
    waitFor $TOMCAT_LOG "Server startup in" || err "Timed out waiting for server startup."
  }

  $DO_UNIT_TESTS && {
    # Run base tests
    msg "Running base JUnit tests ..."
    ant tests > tests.log || {
      cat tests.log  # to include them in log file when it is mailed
      err "One or more base JUnit tests failed."
    }
  }

  $DO_UNIT_TESTS && {
    # Package transfer application
    ant package-application-transfer  || err "Failed to generate transfer application"

    # Run JUnit tests
    msg "Running module tests ..."
    ant test-modules || err "One or more unit tests failed."
  }
  
  $DO_TEST_IMAGES && {
    # Check if there are test images
    if [ -e ~/demodata ];
    then
      # Load with images
      cd build/dist
      java -jar medcommons-transfer-application.jar http://localhost:9080/gateway/services/CXP2 /home/ci/build/testdata/demo $DEMO_GROUP_AUTH_TOKEN  ||     { err "Unable import demo data."	exit 1;}
      cd ../..
    else
      msg "Unable to find test images at ~/demodata"
    fi
  }
  
  $DO_UNIT_TESTS && {
    # Run JUnit tests
    ant test-applications || err "One or more unit tests failed."
  }
  
  # copy the badboy test to the server
  if [ ! -z "$ENABLE_WEB_TESTS" ];
  then
    printf "Current directory is "
    pwd
    echo

    echo "Parsing svn changes for emails: "
    cat svnchanges.log

    changers=`cat svnchanges.log | grep "^r[0-9]\{4\}.*line" | awk '{ print $3 "@medcommons.net"}' | sort -u | awk 'BEGIN{X="";} {X=X $1 ","} END { print X;}'`
    msg "Running Badboy Tests with notifications to $changers"
    # Unblock test server
    curl "http://badboy.medcommons.net:8030/wtm/removeBlock?block.id=753664" > removeBlock.html
    curl 'http://badboy.medcommons.net:8030/bbq/addScript?planId=65536&notifyEmail='$changers > bbq.html
  fi

  # Success!
  msg "All tests succeeded.  No need for spam."
  
) 2>&1 || { # Error occurred!

  failureMsg="Build/Test failed ($TIME_STAMP)";
  # Email everyone
  [ "nobody" != "$SPAM_LIST" ] && {
      cat ci-$TIME_STAMP.log | mail -s "$failureMsg" $SPAM_LIST
  }

  imMsg="$failureMsg"
  if [ -e /var/www/html/buildfailures ];
  then
    imMsg="$failureMsg: http://"`hostname`"/buildfailures/ci-$TIME_STAMP.log"
    cp ci-$TIME_STAMP.log /var/www/html/buildfailures
  fi

  [ "nobody" != "$IM_SPAM_LIST" ] && {
    echo "$imMsg" | groovy $COMPONENT_BASE_DIR/ci/smack.groovy $IM_SPAM_LIST
  }

  echo "" > broken
  rm -f running
  if [ -f wasrunning ];
  then 
    rm wasrunning
    echo > running
  fi

  exit 1;
}
) | tee ci-$TIME_STAMP.log 

# Clean the running flag so that next invocation can run
msg "Removing running flag ..." >> ci-$TIME_STAMP.log
rm -f running

[ -e running ] && {
  msg "Warning:  running flag still exists after I removed it"  >> ci-$TIME_STAMP.log
}
 
[ ! -e skipped ] && [ ! -e broken ] && {
  # Always e-mail simon for successful build
  [ "nobody" != "$SPAM_LIST" ] && {
    cat ci-$TIME_STAMP.log | mail -s "Build/Test Succeeded ($TIME_STAMP)"  $SPAM_LIST
  }
}

[ -f broken ] && [ ! -e skipped ] && {
  rm broken
}

# Success  -  OR the build was skipped because there were no updates
msg "Checking/Removing skipped flag ..."  >> ci-$TIME_STAMP.log
[ -e skipped ] && {
  rm -f skipped
}
    
 
