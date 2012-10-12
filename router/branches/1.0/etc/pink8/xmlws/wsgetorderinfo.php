<?php
// (c) 2004 MedCommons, Inc.
// wld 8/28/04
require_once("../lib/xmlheader.inc"); //


$og = cleanreq('OrderGuid');

$query = db_prepare_order_select($og);
xmlquery("orders", $query,false);
?>
