#!/bin/sh

VERS=`python vers.py SPECS/medcommons-config.spec` ||
    exit 1

echo "VERSION - $VERS";

mv SOURCES/medcommons-config-*.tar.gz HISTORY || :
mv SOURCES/medcommons-config-* HISTORY || :
mv RPMS/noarch/medcommons-config-*.noarch.rpm HISTORY || :

: ${SERVICES_WORK:="${FB_ROOT}/services/${FB_TRUNK}"}

cp -R $SERVICES_WORK/forge/config SOURCES/medcommons-config-$VERS &&
find SOURCES/medcommons-config-$VERS -type d -name ".svn" -exec rm -rf {} \;

cp SPECS/medcommons-config.spec HISTORY/medcommons-config-$VERS.spec &&

pushd SOURCES &&
tar czf medcommons-config-$VERS.tar.gz medcommons-config-$VERS &&
popd &&
rpmbuild -bb SPECS/medcommons-config.spec &&
ls -l RPMS/noarch/medcommons-config-$VERS-1.noarch.rpm
