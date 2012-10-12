#!/bin/sh

VERS=`python vers.py SPECS/medcommons-jsvc.spec` ||
    exit 1

mv RPMS/x86_64/medcommons-jsvc-*.x86_64.rpm HISTORY ||

cp SPECS/medcommons-jsvc.spec HISTORY/medcommons-jsvc-$VERS.spec &&

rpmbuild -bb SPECS/medcommons-jsvc.spec &&
ls -l RPMS/x86_64/medcommons-jsvc-$VERS-1.x86_64.rpm
