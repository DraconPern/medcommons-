###############################################################
#
# Output an error message and exit
#
###############################################################
function err() {
  echo
  printf "$1\n"
  echo
  exit 1;
}

###############################################################
#
# Output a message
#
###############################################################
function msg() {
  echo
  echo "$1"
  echo
}

###############################################################
#
# Returns true if the OS is Win32+Cygwin
#
###############################################################
function cygwin() {
   uname | grep -qi CYGWIN;
}

###############################################################
#
# Attempts to kill process by name passed as argument
#
###############################################################
function killproc() {
  if cygwin;
  then
    type pskill > /dev/null || return 0
    pskill "$1"
    return 0
  else
    pkill $1 || true # or pkill failing will make us return false
  fi
}

###############################################################
#
# Monitors a given file, watching until a given text string
# appears in the file at least a given number of times.  The
# file is checked every 3 seconds.
#
# The time spent waiting is returned as a variable "WAIT_TIME".
#
# Usage:  waitFor <file> <text string> <count> <time-out>
#
###############################################################
waitFor () {
  FILE=$1;
  TEXT="$2";
  COUNT=$3;
  TIMEOUT=$4;
  START_TIME=`date +"%s"`;
  if [ -z $COUNT ];
  then
    COUNT=1;
  fi
  if [ -z $TIMEOUT ];
  then
    TIMEOUT=300;
  fi
  #echo "Waiting for text $TEXT in file $FILE counted $COUNT times with timeout $TIMEOUT"
  export WAIT_TIME=0;
  export TIMED_OUT=false;
  while [ `grep -c "$2" $1` -lt $COUNT ];
  do
    if [ $WAIT_TIME -gt $TIMEOUT ];
    then
      export TIMED_OUT=true;
      break;
    fi
    sleep 3;
    let WAIT_TIME="$WAIT_TIME + 3";
    #echo "Wait count = $WAIT_TIME";
  done
  END_TIME=`date +"%s"`;
  let "WAIT_TIME = $END_TIME - $START_TIME";
  if [ $TIMED_OUT == true ];
  then
    return 1;
  else
    return 0;
  fi
}
  
