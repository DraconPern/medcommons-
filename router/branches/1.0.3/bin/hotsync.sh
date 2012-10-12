#!/bin/bash

if [ ! -e bin ];
then
  echo
  echo "Please run from distro root directory."
  echo
  exit 1;
fi

DIST_ROOT="$PWD"

. bin/utils.sh  


if [ "$1" == "-k" ];
then
  kill `cat hotsync.pid`
  rm hotsync.pid
  exit 0;
fi

if [ -f hotsync.pid ];
then
  if ps | grep -w -q `cat hotsync.pid`; 
  then
    err "hotsync already running!\n\nUse 'kill `cat hotsync.pid`' to stop it"
    exit 1;
  fi
fi

# WAR_DIR="$DIST_ROOT/stage/jboss-3.2.3/server/router/deploy/router.ear/router-web.war/"
WAR_DIR="$DIST_ROOT/stage/tomcat/webapps/router"


if [ ! -d  $WAR_DIR ];
then
  err "Unable to find dir $WAR_DIR.\n\nAre you deployed in exploded form?"
fi

(
#set -x
TS=`date`
while true; 
do 
  START=`date`
#  pushd $DIST_ROOT/src/net/medcommons/router/services/selection/ > /dev/null
#  nice cp -uvf *.jsp "$WAR_DIR";
#  popd > /dev/null
#
  # pushd $DIST_ROOT/etc/static-files/DemoHelp > /dev/null
  # nice cp -uvf *.js *.jsp *.htm* "$WAR_DIR";
  # popd > /dev/null

  cd /dm/src/net/medcommons/router/services/order/web
  FOUND=`find . -maxdepth 2 -newermt "$TS"`
  #if [ ! -z "$FOUND" ];
  #then
    nice cp -uvf *.gif *.js *.htm* *.jsp *.ftl  *.png *.jpg *.css "$WAR_DIR";
    nice cp -uvf images/*.* "$WAR_DIR/images";
  #fi

  cd /dm/src/net/medcommons/router/services/dicom/util/web
  FOUND=`find . -maxdepth 2 -newermt "$TS"`
  if [ ! -z "$FOUND" ];
  then
    nice cp -uvf *.jsp  "$WAR_DIR";
  fi

  cd /dm/src/net/medcommons/router/services/xds/consumer/web/
  FOUND=`find . -maxdepth 2 -newermt "$TS"` 
  if [ ! -z "$FOUND" ];
  then
    nice cp -uvf *.css *.jsp *.xsl *.js  "$WAR_DIR";
    nice cp -uvf images/*.gif images/*.jpg images/*.png  "$WAR_DIR/images";
  fi

  cd /dm

   nice cp -uvf etc/configurations/xpaths*.xml stage/tomcat/data
#  nice cp -uvf etc/static-files/xds-templates/*.xml stage/tomcat/data/xds-templates
   nice cp -uvf etc/static-files/stylesheets/ccr2htm.xsl stage/tomcat/data/stylesheets
   nice cp -uvf etc/configurations/medcommons-*config.xml etc/configurations/{MedCommonsBootParameters.properties,LocalBootParameters.properties} stage/tomcat/conf
   nice cp -uvf src/net/medcommons/router/web/WEB-INF/actions.xml stage/tomcat/webapps/router/WEB-INF
#  nice cp -uvf src/net/medcommons/router/web/WEB-INF/classes/StripesResources.properties stage/tomcat/webapps/router/WEB-INF/classes
   nice cp -uvf src/net/medcommons/router/web/WEB-INF/classes/net/medcommons/ApplicationResources.properties stage/tomcat/webapps/router/WEB-INF/classes/net/medcommons
#  nice cp -uvf /id/src/web/WEB-INF/jsp/* /dm/stage/tomcat/webapps/identity/WEB-INF/jsp
#
  if [ ! -z $1 ];
  then
    exit 0;
  fi

  TS="$START"
  
  sleep 2; 
done
) > hotsync.log 2>&1 &

echo $! > hotsync.pid

[ -z $1 ] && {
  echo
  echo "Hotsync started as pid $!"
  echo
}
