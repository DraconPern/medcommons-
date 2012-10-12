#!/bin/sh

VERS=`python vers.py SPECS/medcommons-schema_db.spec` ||
    exit 1

DST=SOURCES/medcommons-schema_db-$VERS

mv SOURCES/medcommons-schema_db-*.tar.gz HISTORY || :
mv RPMS/noarch/medcommons-schema_db-*.noarch.rpm HISTORY || :
rm -rf SOURCES/medcommons-schema_db-* || :				 # mv SOURCES/medcommons-schema_db-* HISTORY || :

mkdir -p $DST/root/schema_db &&
find ${FB_ROOT}/services/${FB_TRUNK}/schema -type f -regex ".*\.\(sql\|sh\|py\)" -exec cp {} $DST/root/schema_db \; &&
cp SPECS/medcommons-schema_db.spec HISTORY/medcommons-schema_db-$VERS.spec &&

pushd SOURCES &&
tar czf medcommons-schema_db-$VERS.tar.gz medcommons-schema_db-$VERS &&
popd &&
rpmbuild -bb SPECS/medcommons-schema_db.spec &&
ls -l RPMS/noarch/medcommons-schema_db-$VERS-1.noarch.rpm
