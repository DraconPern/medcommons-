#!/bin/bash;

export SRC="commons-daemon-1.0.3-bin-linux-x86_64.tar.gz";
export DST="medcommons-jsvc.tar.gz";
export PKG="./medcommons-jsvc";
export PTH="/opt/apache-tomcat/bin";

rm -rf ${PKG}; mkdir -p ${PKG}${PTH};

tar xvzf ${SRC} -C ${PKG}${PTH} jsvc;
tar cvzf ${DST} ${PKG};

rm -rf ${PKG};
