#!/bin/sh

VERS=`python vers.py SPECS/medcommons-tools.spec` ||
    exit 1

DST=SOURCES/medcommons-tools-$VERS

mv SOURCES/medcommons-tools-*.tar.gz HISTORY || :
mv RPMS/noarch/medcommons-tools-*.noarch.rpm HISTORY || :
rm -rf SOURCES/medcommons-tools-* || :

mkdir -p $DST &&
pushd $DST &&
cp -a ${FB_ROOT}/services/${FB_TRUNK}/infrastructure/exogenous/rsyslog/mc_f7_rsyslog/etc . &&
cp -a ${FB_ROOT}/services/${FB_TRUNK}/infrastructure/exogenous/rsyslog/mc_f7_rsyslog/sbin . &&
cp -a ${FB_ROOT}/services/${FB_TRUNK}/infrastructure/exogenous/rsyslog/mc_f7_rsyslog/usr . &&
find . -depth -type d -name ".svn" -exec rm -rf {} \; &&
popd &&
cp SPECS/medcommons-tools.spec HISTORY/medcommons-tools-$VERS.spec &&

pushd SOURCES &&
tar czf medcommons-tools-$VERS.tar.gz medcommons-tools-$VERS &&
popd &&
rpmbuild -bb SPECS/medcommons-tools.spec &&
ls -l RPMS/noarch/medcommons-tools-$VERS-1.noarch.rpm
