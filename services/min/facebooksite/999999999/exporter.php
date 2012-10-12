<?php
// facebook exporter, runs outside facebook context
require_once 'adminsetup.inc.php';

function export_fbusers($fbidlist,$app,$fbidme)
{
	// as xml into file

	function xmlset($tag,$skipcount, $query)
	{
		// run the query, wrap all results in the tag, emit all non-empty fields
		$any =false; $out='';

		$result=mysql_query($query) or die ("Cant $query ". mysql_error());

		while ($row = mysql_fetch_assoc($result)) {
			$out.="<$tag>";
			// wrtie out the data row
			$fieldcount = 0;
			foreach ($row as $key => $value) {
				if (($fieldcount>=$skipcount)&&($value!=''))
				{
					// skip leading fields
					$out .="
					<$key>$value</$key>";
					$any = true;
				}
				$fieldcount++;
			}
				$out.="</$tag>";
		}
		mysql_free_result($result);
		//if ($any) $out = "
		//<$tag>".$out."
		//</$tag>";
		return $out;
	}
	$now = time();
	$buf =<<<XXX
<?xml version="1.0" encoding="ISO-8859-1"?>
  <facebook_export>
	<exporter_fbid>$fbidme</exporter_fbid>
	<export_time>$now</export_time>
	<app_index_key>$app->key</app_index_key>
	<app_name>$app->healthbook_application_name</app_name>
	<new_account_appliance>$app->new_account_appliance</new_account_appliance>
XXX;


	$fbids = explode(',',$fbidlist);
	foreach ($fbids as $fbid)
	{
		$buf.="
     <facebook_user_export fbid='$fbid' >";
		// the facebook user's record
		$buf .= xmlset('user',0, "select * from users where fbid='$fbid' ")	;
		// the patients he has created
		$buf .= '
       <patients>'.	xmlset('patient',0, "select * from patients where familyfbid='$fbid' ").'
       </patients>';
		// the careteam he is running
		$buf .= '
       <familyteam>'.xmlset('member', 0,"select * from teams where teamfbid='$fbid' ").'
       </familyteam>';	
		// and any teams he is on
		$buf .= '
       <otherteams>'.xmlset('giver', 0,"select * from teams where userfbid='$fbid' ")  .'
       </otherteams>';

		$buf.= '
     </facebook_user_export>';
	}
	$buf .='
  </facebook_export>';
	header ("Content-type:text/xml");
//	Header ("Content-disposition: attachment; filename=medcommonsFacebookExport$now.xml ");
	echo $buf;
	exit;

}
//**start here


// these variables are passed in from the facebook blucare app
$me = $_GET['appkey'];
$user = $_GET['user'];

mysql_connect($GLOBALS['DB_Connection'], $GLOBALS['DB_User']) or die("facebook boostrap: error  connecting to database.");
$db = $GLOBALS['DB_Database'];
mysql_select_db($db) or die("can not connect to database $db");
$result = mysql_query("SELECT * FROM `fbapps` WHERE `key` = '$me' ") or die("$me in $db is not registered with medcommons as a healthbook application".mysql_error());
$r = mysql_fetch_object($result);
if ($r===false)  die("$me is not registered with medcommons as a healthbook application");
if (isset($_GET['all']) ) {
	//thow everything in for testing
	$all = $pre='';
	$result = mysql_query("SELECT * FROM users ") or die("cant scan users table".mysql_error());
	while ($r2 = mysql_fetch_object($result))
	{
		$all .= $pre.$r2->fbid;
		$pre =' , ';
	}
	export_fbusers($all,$r,$user);
	exit;
}

if (isset($_GET['export']) ) export_fbusers($_GET['export'],$r,$user);
else die ("Export called with invalid arguments");
?>
