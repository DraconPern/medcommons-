<?php


// Local setup file, overrides defaults in this file
// to allow deployment on other systems, please
// do not check in a file of name 'local_setup.inc.php'!
if(file_exists("local_setup.inc.php")) {
  include "local_setup.inc.php";
}
$GLOBALS['DB_Connection'] = "mysql.internal";
$GLOBALS['DB_User']= "medcommons";
$GLOBALS['DB_Database'] = "songbook";


function sql_connect()
{
	if (!isset($GLOBALS['db_connected']) ){
		$GLOBALS['db_connected'] = mysql_connect($GLOBALS['DB_Connection'] ,$GLOBALS['DB_User'] );
		$db = $GLOBALS['DB_Database'];
		mysql_select_db($db) or die ("can not connect to database $db ".mysql_error());
	}

}
function dosql($q)

{
	sql_connect();
	$status = mysql_query($q);
	if (!$status) die ("dosql failed $q ".mysql_error());
	return $status;
}
function clean($s)
{
	$s= csv_strip ($s);
	return mysql_real_escape_string(trim($s));
}
function csv_strip ($v)
{
	// make data safe for csv readers
	// throw out everything even slightly hostile
	// tested against mac numbers 09

	$new = '';
	$len = strlen($v);
	for ($j=0; $j<$len; $j++)
	{
		$c = substr($v,$j,1);
		if (ord($c) >= ord(' '))
		if ($c!='"')
		if ($c!=',')
		if ($c!="'")
		
		if ($c!="&")
		if ($c!='\\')
		$new.=$c;
	}
	return $new;
}
?>
