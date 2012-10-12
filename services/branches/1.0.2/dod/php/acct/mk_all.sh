#!/bin/bash
#####################################
#
# A simple utility that concatenates and compresses 
# javascript and CSS files using gzip
# so that they can be delivered in smallest possible
# form.
#
# Author:  ssadedin@medcommons.net
#
#####################################

d=`date`
rm -f acct_all.css acct_all.js

echo "/* 
 * Do not edit this file.  
 * 
 * This file was auto generated from source on $d .  See /acct/mk_all.sh 
 */
" >> acct_all.css
echo "creating css ..."
cat featurebox.css rls.css main.css autoComplete.css >> acct_all.css
echo "creating javascript ..."
cat sha1.js featurebox.js mini-mochi.js utils.js autoComplete.js ajlib.js contextManager.js >> acct_all_full.js
if [ "$1" != "big" ];
then
  echo "minifying ..."
  java -jar yuicompressor-2.3.5.jar acct_all_full.js > acct_all.js
  java -jar yuicompressor-2.3.5.jar settings.js > settings-min.js
else
  cp acct_all_full.js acct_all.js
  cp settings.js settings-min.js
fi

rm acct_all_full.js