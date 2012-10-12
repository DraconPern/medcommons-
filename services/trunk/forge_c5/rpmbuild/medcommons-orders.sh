#!/bin/sh

VERS=`python vers.py SPECS/medcommons-orders.spec` ||
    exit 1

DST=SOURCES/medcommons-orders-$VERS

# Note: for reasons I don't understand '~' does not 
# work to specify home directory on forge (why?).
: ${ORDERS_WORK:="${FB_ROOT}/services/${FB_TRUNK}/orders"} 
: ${CONFIG_WORK:="${FB_ROOT}/services/${FB_TRUNK}"}

mv SOURCES/medcommons-orders-*.tar.gz HISTORY || :
mv RPMS/noarch/medcommons-orders-*.noarch.rpm HISTORY || :
rm -rf SOURCES/medcommons-orders-* || :


mkdir -p $DST/var/apache-tomcat/webapps/orders $DST/var/apache-tomcat/conf $DST/etc/httpd/conf.d &&
pushd $ORDERS_WORK && 
grails prod clean && 
grails prod war && 
popd && 
pushd $DST/var/apache-tomcat/webapps/orders &&
jar xf $ORDERS_WORK/target/orders-0.1.war &&
popd &&
cp $CONFIG_WORK/config/linux/common/etc/httpd/conf.d/orders_ajp.conf $DST/etc/httpd/conf.d &&
cp SPECS/medcommons-orders.spec HISTORY/medcommons-orders-$VERS.spec &&

pushd SOURCES &&
tar czf medcommons-orders-$VERS.tar.gz medcommons-orders-$VERS &&
popd &&
rpmbuild -bb SPECS/medcommons-orders.spec &&
ls -l RPMS/noarch/medcommons-orders-$VERS-1.*noarch.rpm

