#!/bin/bash;

export SRC="jai-1_1_3-lib-linux-amd64.tar.gz";
export DST="medcommons-jai.tar.gz";
export PKG="./medcommons-jai";
export PTH="/usr/java/default/jre";
export LBZ="jai-1_1_3/lib"

rm -rf ${PKG}; mkdir -p ${PKG}${PTH};

tar xvzf ${SRC} -C ${PKG}${PTH} ${LBZ}
mv ${PKG}/${PTH}/${LBZ} ${PKG}/${PTH}/lib
rmdir ${PKG}/${PTH}/jai-1_1_3
tar cvzf ${DST} ${PKG};

rm -rf ${PKG};
