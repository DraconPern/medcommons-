<?php
// getPgatewayinfo - just what the purple box ordered
//   does queries against multiple tables to form results
//(c) 2004 MedCommons, Inc.
// wld 8/28/04
require_once("../lib/xmlheader.inc"); //needs it in $request

$gw = cleanreq('gateway');//input fields need cleansing
$table = "virtrad"; 
$filter = "(vr_GW_Gateway ='".$gw."')";

$query = db_prepare_virtrad_select($gw);


$query2 = db_prepare_gateways_select ($gw);


$request =$query." and then ".$query2;


xmltop($request,false);

$result = db_query($query,$GLOBALS['DB_Link']) or 
        xmldie(db_error());

xmltabledump($table,$result);
db_free_result($result);

$result = db_query($query2,$GLOBALS['DB_Link']) or 
        xmldie(db_error());

xmltabledump("gateways",$result);
db_free_result($result);

xmlend($time_start, "OK");
?>