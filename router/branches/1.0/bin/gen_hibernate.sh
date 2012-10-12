#!/bin/bash
#
# Hack script to generate hibernate mappings for a Java object
#

function usage() {
  echo
  echo "Usage:  $0 <file to process, referenced as src/net/medcommons/...>"
  echo
}

# File passed as first arg
SRC=$1

if [ -z $SRC ];
then
  usage;
  exit 1;
fi

if [ ! -f $SRC ];
then
  echo
  echo "Can't find the source file!"
  echo
  usage
  exit 1;
fi


if [ ! grep -q "^src/" ];
then
  echo
  echo "Please specify the a file starting with src/..."
  echo
  usage;
  exit 1;
fi

# Make the prefix the same as the class name
PREFIX=`echo $SRC | grep -o '[A-z]*.java$' | sed 's/.java//'`;
PROPERTIES=`grep -o "get.*()" $SRC  | sed 's/^get//g;s/()//';`;

CLASS=`echo $SRC | sed 's,/,\.,g; s/.java$//;s/^src.//'`

TABLE=`echo $CLASS | grep -o '[^\.][^\.]*$' | awk '{ print toupper($1) }'`

echo
echo "$CLASS - $TABLE"
echo

echo "
<class name='$CLASS' table='$TABLE'>
  <id name='id' type='long'>
   <column name='${PREFIX}_ID' sql-type='INTEGER'/>
   <generator class='native'/>
  </id>
"

for prop in $PROPERTIES;
do
  if [ ! $prop = Id ];
  then
    echo $prop | awk -v PREFIX=$PREFIX '{  print "  <property name=\"" tolower(substr($1,0,1)) substr($1,2)  "\" column=\"" toupper(PREFIX) "_" toupper($1) "\">"; }'
  fi
done

echo "</class>"


