<?php
// (c) 2004 MedCommons, Inc.
// wld 8/30/04
// sendcommandutil.php
// the sql can use some cleaning up
require_once ("../lib/htmlsubs.inc");

function sendcommandutil()
{
			
$ec = cleanreq('CommandCode');
$ed = cleanreq('CommandString');
$gw = cleanreq('Gateway');

htmltop("Send Command to Gateway ".$gw,PINKBOX_STYLE);

db_sql_insert_into_remote_commands($gw,$ec,$ed,'pending');



if (db_affected_rows()==1)	htmlbody( "insert complete"); else {
$status = db_error();/////
if ($status!="") die($status);
}



echo htmlfooter();
}
sendcommandutil();
?>




