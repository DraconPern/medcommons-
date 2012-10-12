<?php
// (c) 2004 MedCommons, Inc.
// wld 8/28/04

require_once("../lib/xmlheader.inc"); // needs $request

$table = cleanreq('table');
$filter = cleanreq('filter');//input fields need cleansing

$query =  db_prepare_select_general($table,$filter);


xmlquery($table,$query,false);


?>
