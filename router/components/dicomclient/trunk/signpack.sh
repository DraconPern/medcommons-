#!/bin/bash
# Derived from Alan Burlison's blog: http://blogs.sun.com/alanbur/date/20060628
#compress and sign a JAR file, remove the original
# args are <JAR> <keystore> <password> <alias>
#pack200 --repack --no-keep-file-order --strip-debug $1
echo $1
pack200 --repack --segment-limit=-1 $1
jarsigner -keystore $2 -storepass $3 $1 $4
jarsigner -verify -certs $1
pack200 --effort=9 --segment-limit=-1 $1.pack.gz $1
## Don't drop the original jar - the original jar is needed to find the pack version.
#rm $1