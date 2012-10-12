#!/bin/bash

function msg() {
  echo
  printf "$1\n"
  echo
}

function err() {
  echo
  echo "ERROR:  $1"
  echo
  exit 1;
}

function input() {
  if ! $QUIET;
  then
    read -p "$1 [${!2}]: ";
    [ ! -z $REPLY ] && export $2="$REPLY"  
  fi
}

function setPortOffset() {
    PORTOFFSET=$1;

    # Tomcat HTTP Port    
    let 'TomcatHTTPPort = 9080 + PORTOFFSET'
    sed -i.bak 's/<Connector port="9080"/<Connector port="'$TomcatHTTPPort'"/' $INSTALLDIR/tomcat/conf/server.xml
    msg "Set Tomcat HTTP Port to $TomcatHTTPPort"

    # Tomcat HTTPS Port
    let 'TomcatHTTPSPort = 8443 + PORTOFFSET'
    sed -i.bak 's/port="8443"/port="'$TomcatHTTPSPort'"/; s/redirectPort="8443"/redirectPort="'$TomcatHTTPSPort'"/ ' $INSTALLDIR/tomcat/conf/server.xml
    msg "Set Tomcat HTTPS Port to $TomcatHTTPSPort"
    
    # Tomcat Shutdown Port
    let 'TomcatShutdownPort = 8005 + PORTOFFSET'
    sed -i.bak 's/<Server port="8005" shutdown="SHUTDOWN">/<Server port="'$TomcatShutdownPort'" shutdown="SHUTDOWN">/' $INSTALLDIR/tomcat/conf/server.xml
    msg "Set Tomcat Shutdown Port to $TomcatShutdownPort"

    LBP="$INSTALLDIR/tomcat/conf/LocalBootParameters.properties"

    # RemotePort
    RemotePort=`grep RemotePort tomcat/conf/MedCommonsBootParameters.properties | grep -o '[0-9][0-9][0-9][0-9]'`
    let 'RemotePort = RemotePort + PORTOFFSET'
    if [ -e "$LBP" ] && grep -q RemotePort "$LBP";
    then
      msg "Local configuration already contains RemotePort:  leaving unmodified.";
    else
      echo "RemotePort=$RemotePort" >> "$LBP"
      msg "Set RemotePort to $RemotePort"
    fi

    # DICOM CSTORE Port
    CSTOREPort=`grep CSTOREPort tomcat/conf/MedCommonsBootParameters.properties | grep -o '[0-9][0-9][0-9][0-9]'`
    let 'CSTOREPort = CSTOREPort + PORTOFFSET'
    echo "CSTOREPort=$CSTOREPort" >> "$INSTALLDIR/tomcat/conf/LocalBootParameters.properties"
    msg "Set DICOM CSTORE Port to $CSTOREPort"

    # Derby Port
    DerbyPort=`grep DerbyPort tomcat/conf/MedCommonsBootParameters.properties | grep -o '[0-9][0-9][0-9][0-9]'`
    [ -z $DerbyPort ] && {
      DerbyPort=1527
    }
    let 'DerbyPort = DerbyPort + PORTOFFSET'
    echo "DerbyPort=$DerbyPort" >> "$INSTALLDIR/tomcat/conf/LocalBootParameters.properties"

    # Hibernate
    # Not needed - code is smart enough to set this itself now
    #sed -i.bak 's,jdbc:cloudscape:net://localhost:1527/,jdbc:cloudscape:net://localhost:'$DerbyPort',' $INSTALLDIR/tomcat/conf/hibernate.properties
    
    msg "Set Derby Port to $DerbyPort"
}

function cygwin() {
   uname | grep -qi CYGWIN;
}

usage='
medcommons.sh [-q] [-l <install dir>]

Options:
  -q : performs a quiet install using default parameters
  -l : specifies an install location
  -a : advanced install, lets you set advanced options
  -p : set the default port offset to use for install
'

INSTALLDIR="$PREV/medcommons"
QUIET=false
ADVANCED=false

while getopts "aqp:l:m:" options; do
  case $options in
    q ) QUIET=true;;
    l ) INSTALLDIR=$OPTARG;;
    a ) ADVANCED=true;;
    p ) PORTOFFSET=$OPTARG;;
    m ) MEMORY=$OPTARG;;
    h ) echo "$usage";;
    \? ) echo "$usage"
         exit 1;;
    * ) echo "$usage"
          exit 1;;
  esac
done

echo "Welcome to the MedCommons Unix Installer."
echo
echo "This script will guide you through the process of installing MedCommons Software."
echo

# Make sure USER env variable is set
[ -z $USER ] && export USER=`whoami`

# Does the user have java installed?  If not, tell them to get it
type -t javac > /dev/null || 
  err "You do not appear to have the Java SDK installed, or it is not in your PATH.  Please install Java before installing MedCommons."

[ ! -z "$JAVA_HOME" ] || {

  POSSIBLE_JAVA_HOME=`type javac | awk '{ print $3 }' | xargs dirname | xargs dirname`

  cygwin && {
    POSSIBLE_JAVA_HOME=`cygpath -ma $POSSIBLE_JAVA_HOME` 
  }

  msg "JAVA_HOME is not set.  In order for your MedCommons Gateway to work it must have JAVA_HOME configured correctly"
  msg "Please select a Java installation to use:"
  HOMES=`locate bin/javac | sed 's,bin/javac,,g'`
  [ -z "$HOMES" ] && {
    # If not found it might be that they just installed it and the locate db is not up to date
    HOMES=$POSSIBLE_JAVA_HOME;
  }
  select POSSIBLE_JAVA_HOME in $HOMES; do break; done
  INCJH=y
  input "Do you want to modify your environment to include JAVA_HOME?" INCJH
  if [ y == "$INCJH" ];
  then
    echo "export JAVA_HOME=$POSSIBLE_JAVA_HOME" >> ~/.bash_profile
    JAVA_HOME="$POSSIBLE_JAVA_HOME"
    EXIT_MESSAGE="Your login scripts have been modified.  Please logout and in again before starting your router."
  fi
}

# Get install directory
input "Where would you like to install the software?" INSTALLDIR ;
msg "Install directory is $INSTALLDIR"

# Create install directory
if [ ! -d "$INSTALLDIR" ];
then
  CREATEDIR="y"
  input "Install directory does not exist.  Do you want to create it?" CREATEDIR 
  if [ "$CREATEDIR" == "y" ];
  then
    mkdir -p "$CREATEDIR"
  else 
    err "Aborting: must have install directory to proceed."
  fi
fi

# Check for existing database directory
keepdb=false
[ -e "$INSTALLDIR"/tomcat/data/derby/routerdb ] && {
  msg "Warning!  You appear to have an existing database in your installation path.\nPlease select an option:"
  if ! $QUIET;
  then
    select dboption in "Keep existing database" "Replace Existing Database";
    do
      case $dboption in
        "Keep existing database") keepdb=true; break;;
        "Replace Existing Database") 
          keepdb=false; 
          REPLY=n
          input "All existing data will be destroyed. Are you sure? (y/n)" REPLY;
          if [ $REPLY == "y" ];
          then
            rm -rf "$INSTALLDIR"/tomcat/data/derby/routerdb ||\
              err "Unable to remove your old database.  Please check the software is not running and you have correct permissions to the directory."
            rm -rf "$INSTALLDIR"/tomcat/data/images ||\
              err "Unable to remove your old data.  Please check the software is not running and you have correct permissions to the directory."
          else
            msg "Aborting - please re-run install script."
            exit 1;
          fi
          break;;
      esac
    done
  else
    $keepdb = true;
  fi

  $keepdb && {
    INSTALL_SCHEMA_VERSION=`cat schema_version.txt`
    EXISTING_SCHEMA_VERSION=0
    [ -e "$INSTALLDIR"/tomcat/data/derby/schema_version.txt ] && {
      EXISTING_SCHEMA_VERSION=`cat "$INSTALLDIR"/tomcat/data/derby/schema_version.txt`
    }
    if [ ! "$INSTALL_SCHEMA_VERSION" == "$EXISTING_SCHEMA_VERSION" ];
    then
      msg "Warning! Your existing schema version ($EXISTING_SCHEMA_VERSION) does not match the version required by this installation ($INSTALL_SCHEMA_VERSION)."
      printf "After installation please upgrade your schema BEFORE running the software.\n\nPress enter to continue."
      read
    fi

    mv "$INSTALLDIR"/tomcat/data/derby/routerdb "$INSTALLDIR"/tomcat/data/derby/routerdb.old || \
      err "Unable to backup existing database.  Please ensure all java processes are stopped."
  }
}

# Annoying tomcat behavior / bug - it may not update exploded war after new one is deployed
if [ -e "$INSTALLDIR/tomcat/router/webapps/gateway" ];
then
  msg "Removing existing unzipped gateway code"
  rm -rf "$INSTALLDIR/tomcat/router/webapps/gateway"
fi

# Copy installable files
msg "Copying files ..."
cp -R . "$INSTALLDIR" ||
  err "An error occurred while copying the files.  Please check that you have sufficient disk space and permission to write to the destination directory."

# Restore the old database since they chose to keep it
$keepdb && {
  rm -rf  "$INSTALLDIR"/tomcat/data/derby/routerdb || \
    err "Unable to remove default database to restore original.\n\nYour old database was saved at $INSTALLDIR/tomcat/data/derby/routerdb.old"

  mv  "$INSTALLDIR"/tomcat/data/derby/routerdb.old "$INSTALLDIR"/tomcat/data/derby/routerdb ||\
   err "Unable to restore original database.\n\nYour old database was saved at $INSTALLDIR/tomcat/data/derby/routerdb.old"

  echo $EXISTING_SCHEMA_VERSION > "$INSTALLDIR"/tomcat/data/derby/routerdb/schema_version.txt;
}

# Setup start-on-boot if user wants it
cd $INSTALLDIR
RUNONBOOT="n"

input "Do you want to start the MedCommons Router on boot?" RUNONBOOT
if [ $RUNONBOOT == "y" ];
then
  echo
  if [ ! "$USER" == root ] && ! cygwin ;
  then
    msg "Your root password is required to modify startup scripts.  Please enter your root password now:"
    while true;
    do
      if su -c "./start_on_boot.sh \"$INSTALLDIR\" $USER";
      then
        break;
      else
        msg "The script modifications did not appear to complete correctly.";
        read -p "Retry? ";
        if [ ! "y" == "$REPLY" ];
        then
          break;
        fi
      fi
    done
  else
    ./start_on_boot.sh "$INSTALLDIR" $USER
  fi
fi

FREE=`free -m | grep -i Mem | awk '{ print $2 }'`
if [ -z "$MEMORY" ];
then
  MEMORY=128
fi
# Advanced options
if $ADVANCED 
then
  msg "You appear to have ${FREE}M total memory on your computer."
  MEMORY="$((FREE > 512 ? FREE/4: 128))"
  input "How much memory (in M) would you like to use to run the MedCommons Router?" MEMORY
  
  echo
  input "Enter a port offset to use to configure ports:" PORTOFFSET
  [ ! -z $PORTOFFSET ] && {
    setPortOffset $PORTOFFSET
  }
fi

if [ ! -z $PORTOFFSET ];
then
  setPortOffset $PORTOFFSET
fi

# Create the start script
if [ ! -d "$INSTALLDIR/bin" ];
then
  mkdir "$INSTALLDIR/bin"
fi

echo '#!/bin/bash
# MedCommons Start Script
[ "$UID" == 0 ] && {
  echo
  echo "Please do not run this script as root.";
  echo 
  exit 1;
}
export JAVA_HOME='\'$JAVA_HOME\''
export PATH="$JAVA_HOME/bin:$PATH"
export JAVA_OPTS="-Djava.awt.headless=true -Xmx'$MEMORY'm"
export LD_ASSUME_KERNEL=2.4.1
if [ -z "$MOD_JK_SO" ];
then
  MOD_JK_SO=/home/apache/modules/mod_jk.so
fi
if grep -q  "EnableModJK.*=.*true" '"$INSTALLDIR"'/tomcat/conf/LocalBootParameters.properties;
then
  sed -i.bak '"'"'s,<!-- modjk DO NOT REMOVE THIS COMMENT -->,<!-- modjk DO NOT REMOVE THIS COMMENT -->\n<Listener className=\"org.apache.jk.config.ApacheConfig\" modJk=\"'"'"'$MOD_JK_SO'"'"'\" />,g'"'"' '"$INSTALLDIR"'/tomcat/conf/server.xml
else
  sed -i.bak "/<Listener className=\"org.apache.jk.config.ApacheConfig\" modJk=.*$/ d" '"$INSTALLDIR"'/tomcat/conf/server.xml
fi
if [ -e  '"$INSTALLDIR"'/jboss ];
then
  cd '"$INSTALLDIR"'/jboss
  nohup ./bin/run.sh -c router > '"$INSTALLDIR"'/log/medcommons.log 2>&1 &
else
  cd '"$INSTALLDIR"'/tomcat
  nohup ./bin/catalina.sh run > '"$INSTALLDIR"'/log/medcommons.log 2>&1 &
fi
echo $! > medcommons.pid
echo
echo "MedCommons Router started."
echo
echo "Check the log file in '"$INSTALLDIR"'/log/medcommons.log for progress."
echo
' > "$INSTALLDIR/bin/start.sh"

# Create the stop script
echo '#!/bin/bash
# MedCommons Stop Script
export JAVA_HOME='\'$JAVA_HOME\''
export PATH="$JAVA_HOME/bin:$PATH"
if [ -e  '"$INSTALLDIR"'/jboss ];
then
  cd '"$INSTALLDIR"'/jboss
  kill `cat medcommons.pid`
else
  cd '"$INSTALLDIR"'/tomcat
  ./bin/catalina.sh stop
fi
echo
echo "Shutdown signal sent to MedCommons Router"
echo
echo "Checking Java processes ..."
echo
i=0; 
while pgrep -u $USER java > /dev/null; 
do 
  let "i=i+1"; 
  printf "."
  if [ $i -gt 8 ]; 
  then 
    echo "WARNING: Your router did not appear to exit properly."
    echo
    echo "Please check the log file in '"$INSTALLDIR"'/log/medcommons.log for progress."
    echo
    exit 1;
  fi; 
  sleep 2; 
done
echo 
echo "Your router has successfully exited."
echo
' > "$INSTALLDIR/bin/stop.sh"

chmod ug+rx "$INSTALLDIR"/bin/*.sh
chmod ug+rx "$INSTALLDIR"/tomcat/bin/*.sh

# Create log directory
if [ ! -d "$INSTALLDIR/log" ];
then
  mkdir "$INSTALLDIR/log"
fi

# Remove the installer scripts
rm "$INSTALLDIR"/*.sh

# Finished!
msg "MedCommons has been successfully installed."
msg "To start your router, type:"
msg "    $INSTALLDIR/bin/start.sh"
if [ ! -z "$EXIT_MESSAGE" ]
then
  msg "$EXIT_MESSAGE"
fi

