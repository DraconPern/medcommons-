#!/usr/bin/env bash
echo 
echo "Compiling ..."
echo

SEP=":"
if uname | grep -iq CYGWIN;
then
  SEP=';'
fi
export CLASSPATH=".${SEP}lib/commons-codec.jar${SEP}lib/jsonrpc.jar${SEP}lib/oauth.jar"
javac -d . src/GetCCR.java || echo "javac failed - check that you have the JDK installed"
javac -d . src/AuthorizeAccess.java || echo "javac failed - check that you have the JDK installed"
javac -d . src/GetImageURLs.java || echo "javac failed - check that you have the JDK installed"

echo 
echo "Running GetCCR ..."
echo
echo "----------------- Output ---------------------------------"
java GetCCR
echo "----------------- End Output For GetCCR -----------------------------"
echo 

read -p "Press enter to continue"

echo 
echo "Running AuthorizeAccess ..."
echo
echo "----------------- Output ---------------------------------"
java AuthorizeAccess
echo "----------------- End Output For Authorize Access -----------------------------"
echo 

read -p "Press enter to continue"

echo 
echo "Running GetImageURLs ..."
echo
echo "----------------- Output ---------------------------------"
java GetImageURLs
echo "----------------- End Output For GetImageURLs-----------------------------"
echo 
