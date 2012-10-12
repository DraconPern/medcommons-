#!/bin/bash
export PATH=$PATH:/cygdrive/c/apachefriends/xampp/mysql/bin
export PATH=$PATH:/cygdrive/c/xampp/mysql/bin
export PATH=$PATH:/usr/local/mysql/bin
export PATH=/Volumes/Macintosh\ HD/Applications/MAMP/Library/bin:$PATH

IGNOREUNTIL=""
if [ ! -z "$1" ];
then
  IGNOREUNTIL="$1"
fi

[ ! -z $MYSQLHOST ] && {
  MYSQLHOST="-h $MYSQLHOST";
}

[ -z $MYSQLUSER ] && {
  MYSQLUSER="root";
}
[ ! -z $MYSQLPASS ] && {
  MYSQLPASS="-p$MYSQLPASS";
}

[ -z $MYSQLDB ] && {
  MYSQLDB="mcx";
}

if [ -z "$IGNOREUNTIL" ];
then
  mysqladmin -u $MYSQLUSER $MYSQLPASS -f drop $MYSQLDB > /dev/null 2>&1
  mysqladmin -u $MYSQLUSER $MYSQLPASS create $MYSQLDB
fi

SQL_FILES=`ls *.sql | grep "^[0-9].*sql"`
for i in mcextio.sql $SQL_FILES;
do
  if [ ! -z "$IGNOREUNTIL" ];
  then

    echo "Ignoring file $i"

    if echo "$i" | grep -q "$IGNOREUNTIL";
    then
      echo "Found ignore pattern in $i"
      IGNOREUNTIL=""
    fi

    continue
  fi

  echo "Executing:   mysql -u $MYSQLUSER $MYSQLHOST $MYSQLPASS $MYSQLDB < $i"
  if mysql -u $MYSQLUSER $MYSQLHOST $MYSQLPASS $MYSQLDB < $i ;
  then
    echo
    echo "$i executed successfully."
    echo
  else
    echo
    echo "$i failed - please investigate."
    echo
    read -p "continue (y/n) ? "
    if [ $REPLY != "y" ];
    then
      exit 1;
    fi
  fi
done
