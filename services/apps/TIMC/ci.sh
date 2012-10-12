#!/bin/bash
##############################################################
#
# Orders Continuous Integration Script
#
# Creates WAR file and deploys it to /var/apache-tomcat/webapps
#
# Author:  ssadedin@medcommons.net
#
##############################################################

COMPONENTS=~/build/components/
SUDO=sudo
: ${SPAM_LIST:="ssadedin@medcommons.net"}
: ${APP_NAME:="orders"}
: ${ORDERS_WAR:="$APP_NAME.war"}
: ${ORDERS_ENVIRONMENT:="ci"}

export APP_NAME
export ORDERS_WAR

source $COMPONENTS/ci/cilib.sh

OLDSVNREV=`svnversion -n . | grep -o '^[0-9]*'`
(
  # If a build is already running, wait
  count=0
  while [ -e build.running ];
  do
    sleep 1;
    echo "Found build running flag: waiting ..."
    let 'count=count+1'
    if [ $count -gt 120 ];
    then
      err "Waited too long"
    fi
  done

  echo $! > build.running

  (
    echo "================== "`date`" ==================="
    msg "Building / Deploy $APP_NAME"
    svn update . | tee svn.log

    if [ "$1" != "-f" ] &&  svn_log_has_no_changes svn.log;
    then
      echo "No changes in orders svn tree"
      exit
    fi

    rm -f orders*.war

    grails -Dgrails.env=$ORDERS_ENVIRONMENT war || err "Unable to build orders war file"

    mv orders*.war /tmp/$ORDERS_WAR || err "Unable to move war file"

    msg "Stopping tomcat"

    sudo /etc/init.d/tomcat stop || err "Unable to stop tomcat"

    msg "Deploying"

    $SUDO `pwd`/deploy.sh $APP_NAME || err "Failed to deploy war file"

    msg "Starting tomcat ..."

    # Must run in background because it blocks until tomcat starts completely
    # *sometimes* but not always (eg: different on portal vs ci)!
    sudo /etc/init.d/tomcat start &

    waitFor /var/apache-tomcat/logs/catalina.out "startup in" || err "Tomcat failed to start or did not start cleanly"

    msg "Finished - successfully built and deployed ${ORDERS_WAR} file"

    cat svn.log | mail -s "orders Build Succeeded" $SPAM_LIST

  ) || {
    msg "Reverting to revision $OLDSVNREV"
    svn update -r $OLDSVNREV .
    echo "Orders Build Failed" | groovy $COMPONENTS/ci/smack.groovy 'ssadedin@gmail.com'
    cat ci.log |  mail -s "orders Build failed" $SPAM_LIST
    err "Build failed: sent spam"
  }

) 2>&1 | tee ci.log

rm -f build.running
