<?php
// (c) 2004 MedCommons, Inc.
// wld 8/28/04
require_once("../lib/xmlheader.inc"); //


$og = cleanreq('OrderGuid');

$query = db_prepare_order_data_select ($og);
//$GLOBALS['debug'] = "debug"; //set to debug to show on screen as plain text
xmlquery("order_data", $query,false);
?>
