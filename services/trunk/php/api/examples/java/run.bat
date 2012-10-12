@echo off
echo .
echo Compiling ...
javac -classpath lib/commons-codec.jar;lib/jsonrpc.jar;lib/oauth.jar -d . src/GetCCR.java
javac -classpath lib/commons-codec.jar;lib/jsonrpc.jar;lib/oauth.jar -d . src/AuthorizeAccess.java
javac -classpath lib/commons-codec.jar;lib/jsonrpc.jar;lib/oauth.jar -d . src/GetImageURLs.java

echo .
echo Running GetCCR ...
echo .
echo ----------------- Output ---------------------------------
java -classpath lib/commons-codec.jar;lib/jsonrpc.jar;lib/oauth.jar;. GetCCR
echo .
echo ----------------- End Output For GetCCR -----------------------------
echo .

pause

echo .
echo Running AuthorizeAccess ...
echo .
echo ----------------- Output ---------------------------------
java -classpath lib/commons-codec.jar;lib/jsonrpc.jar;lib/oauth.jar;. AuthorizeAccess
echo .
echo ----------------- End Output For AuthorizeAccess-----------------------------
echo .

pause

echo .
echo Running GetImageURLs ...
echo .
echo ----------------- Output ---------------------------------
java -classpath lib/commons-codec.jar;lib/jsonrpc.jar;lib/oauth.jar;. GetImageURLs
echo .
echo ----------------- End Output For GetImageURLs-----------------------------
echo .
