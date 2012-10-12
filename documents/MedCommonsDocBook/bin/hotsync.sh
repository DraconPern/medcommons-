#
# Continuously updates document's html form with its docbook source
#
if [ -f hotsync.pid ];
then
  if ps | grep -w -q `cat hotsync.pid`; 
  then
    err "hotsync already running!\n\nUse 'kill `cat hotsync.pid`' to stop it"
    exit 1;
  fi
fi

(
  while true;
  do
    nice make;
    sleep 5;
  done
) > hotsync.log &

echo $! > hotsync.pid

