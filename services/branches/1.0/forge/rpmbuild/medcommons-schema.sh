#!/bin/sh

VERS=`python vers.py SPECS/medcommons-schema.spec` ||
    exit 1

DST=SOURCES/medcommons-schema-$VERS

mv SOURCES/medcommons-schema-*.tar.gz HISTORY || :
mv RPMS/noarch/medcommons-schema-*.noarch.rpm HISTORY || :
rm -rf SOURCES/medcommons-schema-* || :				 # mv SOURCES/medcommons-schema-* HISTORY || :

mkdir -p $DST/root/schema &&
find ${FB_ROOT}/services/${FB_TRUNK}/schema -type f -regex ".*\.\(sql\|sh\|py\)" -exec cp {} $DST/root/schema \; &&
cp SPECS/medcommons-schema.spec HISTORY/medcommons-schema-$VERS.spec &&

pushd SOURCES &&
tar czf medcommons-schema-$VERS.tar.gz medcommons-schema-$VERS &&
popd &&
rpmbuild -bb SPECS/medcommons-schema.spec &&
ls -l RPMS/noarch/medcommons-schema-$VERS-1.noarch.rpm

