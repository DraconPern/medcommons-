
<?php
// (c) 2004 MedCommons, Inc.
// wld 8/28/04
require_once("../lib/xmlheader.inc"); //


$og = cleanreq('OrderGuid');
$status = cleanreq('OrderStatus');
$nseries = cleanreq('series');
$nimages = cleanreq('images');

$query = db_prepare_orders_update_status($status,$og,$nseries,$nimages);
xmlupdate("orders", $query, false);
?>
