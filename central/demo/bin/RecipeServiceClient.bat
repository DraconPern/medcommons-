@echo off

set CLASSPATH=build/classes
set CLASSPATH=%CLASSPATH%;lib/medcommons/common/common.jar
set CLASSPATH=%CLASSPATH%;lib/jboss/axis.jar
set CLASSPATH=%CLASSPATH%;lib/jboss/jaxrpc.jar
set CLASSPATH=%CLASSPATH%;etc/jboss/central/deploy/jboss-net.sar/commons-logging.jar
set CLASSPATH=%CLASSPATH%;etc/jboss/central/deploy/jboss-net.sar/commons-discovery.jar
set CLASSPATH=%CLASSPATH%;etc/jboss/central/deploy/jboss-net.sar/saaj.jar

java net.medcommons.central.ws.recipe.RecipeServiceClient %1
