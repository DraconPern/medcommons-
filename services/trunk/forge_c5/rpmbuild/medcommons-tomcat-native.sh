#!/bin/sh

RPM_DIR=${FB_ROOT}/rpmbuild
RPM_SOURCES_DIR=${RPM_DIR}/SOURCES
RPM_BUILD_DIR=${RPM_DIR}/BUILD
RPM_HISTORY_DIR=${RPM_DIR}/HISTORY
RPM_RPMS_DIR=${RPM_DIR}/RPMS
RPM_SPECS_DIR=${RPM_DIR}/SPECS

BAS=tomcat-native
VERS=1.1.16
NAM=${BAS}-${VERS}
MNM=medcommons-${NAM}

mv ${RPM_SOURCES_DIR}/medcommons-${NAM}.tar.gz ${RPM_HISTORY_DIR} || :
mv ${RPM_RPMS_DIR}/x86_64/medcommons-${NAM}-*.x86_64.rpm ${RPM_HISTORY_DIR} || :
cp ${RPM_SPECS_DIR}/medcommons-${BAS}.spec ${RPM_HISTORY_DIR}/${MNM}.spec &&

pushd ${RPM_SOURCES_DIR} &&
rm -rf ${MNM} &&
cp ${FB_ROOT}/services/${FB_TRUNK}/infrastructure/exogenous/apache-tomcat/${NAM}-src.tar.gz . &&
tar xzf ${NAM}-src.tar.gz &&
mv ${NAM}-src ${MNM} &&
tar czf ${MNM}.tar.gz ${MNM} &&
popd &&

echo $RPM_BUILD_ROOT

rpmbuild -ba SPECS/medcommons-${BAS}.spec &&
ls -l ${RPM_RPMS_DIR}/x86_64/medcommons-${NAM}-*.x86_64.rpm