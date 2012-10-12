#!/bin/bash
if [ -f run.sh ] && [ -f ../bin/run.sh ];
then
	cd ..;
fi

cd ./stage/jboss-3.2.3/bin
./run.sh -c router
