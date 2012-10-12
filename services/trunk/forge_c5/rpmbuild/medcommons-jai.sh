#!/bin/sh

VERS=`python vers.py SPECS/medcommons-jai.spec` ||
    exit 

mv RPMS/x86_64/medcommons-jai-*.x86_64.rpm HISTORY || :

cp SPECS/medcommons-jai.spec HISTORY/medcommons-jai-$VERS.spec &&

rpmbuild -bb SPECS/medcommons-jai.spec &&
ls -l RPMS/x86_64/medcommons-jai-$VERS-1.x86_64.rpm
