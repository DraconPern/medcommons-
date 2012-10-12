#!/bin/bash

APP_NAME=$1
ORDERS_WAR="$APP_NAME.war"

if [ -z "$APP_NAME" ];
then
  echo "APP_NAME var not set"
  exit 1;
fi

if [ -z "$ORDERS_WAR" ];
then
  echo "ORDERS_WAR var not set"
  exit 1;
fi

cd /var/apache-tomcat/webapps 
rm -rf $APP_NAME $ORDERS_WAR
mkdir $APP_NAME
cd $APP_NAME
/usr/bin/jar -xf /tmp/$ORDERS_WAR 

chgrp -R mc_admin /var/apache-tomcat/webapps/orders 
chmod g+w -R /var/apache-tomcat/webapps/orders 

