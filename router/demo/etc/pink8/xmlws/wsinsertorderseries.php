<?php
//wsInsertOrderSeries
// (c) 2004 MedCommons, Inc.
// wld 8/28/04
require_once("../lib/xmlheader.inc");

$orderguid = cleanreq('orderGuid');
$seriesguid = cleanreq('dataGuid');

$insert = db_prepare_order_data_insert($orderguid,$seriesguid);

xmlinsert("order_data",$insert,false);

?> 

