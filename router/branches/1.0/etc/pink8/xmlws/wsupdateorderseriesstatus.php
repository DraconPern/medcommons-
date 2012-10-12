<?php
// (c) 2004 MedCommons, Inc.
// wld 8/28/04

require_once("../lib/xmlheader.inc"); //

$og = cleanreq('dataGuid');
$status = cleanreq('seriesStatus');

$query = prepare_order_series_update($status,$og);

//$GLOBALS['debug'] = "debug"; //set to debug to show on screen as plain text
xmlupdate("order_series", $query,false);
?>