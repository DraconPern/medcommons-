<?php
// REST web service - given an mcid, return the appliance as an xml document
//
// bill - July 25, 2008 

require 'settings.php';
require 'mc.inc.php';
function query_number($q, $type) {
	$sql = <<<EOF
SELECT appliances.name, appliances.url
FROM   alloc_log, appliances, alloc_numbers
WHERE  alloc_numbers.name = '$type' AND
       alloc_log.seed = (cast('$q' as decimal(16)) - alloc_numbers.base) DIV
                         alloc_numbers.leap AND 
       alloc_log.numbers_id = alloc_numbers.id AND
       appliances.id = alloc_log.appliance_id
EOF;

	$result = mysql_query($sql) or die ("Cant $sql ". mysql_error());
	return $result;
}
$GLOBALS['DB_Database']='mcglobals';
$GLOBALS['DB_User'] = 'mc_globals';
$GLOBALS['DB_Connection']='mysql.internal';
$GLOBALS['DB_Password']='';

$q = $_REQUEST['q'];

$blurb = $_SERVER['HTTP_HOST'].$_SERVER['PHP_SELF']."?q=$q 'which appliance hosts medcommons account $q?' ";

$mcid = clean_mcid($q);

$db=$GLOBALS['DB_Database'];
mysql_pconnect($GLOBALS['DB_Connection'],
$GLOBALS['DB_User'],
$GLOBALS['DB_Password']
) or die ("can not connect to mysql");
$db = $GLOBALS['DB_Database'];
mysql_select_db($db) or die ("can not connect to database $db");

$mcid = clean_mcid($q);
$result = query_number($mcid, 'mcid');
$r = mysql_fetch_object($result);
if (!$r) $err = "Unknown MCID $mcid"; 
else {
	$doc = "<globals><request>$blurb</request><response><status>1</status><info><mcid>$mcid</mcid><appliance>$r->name</appliance>
	</info></response></globals>";
	header("Content-type: text/xml");
	echo $doc;	exit;
}
$doc = "<globals><request>$blurb</request><response><status>0</status><error>$err</error></response></globals>";
header("Content-type: text/xml");
echo $doc;
exit;
?>


