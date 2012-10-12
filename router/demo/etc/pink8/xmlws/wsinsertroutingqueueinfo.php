
<?php
//wsInsertRoutingQueueInfo
// (c) 2004 MedCommons, Inc.
// wld 9/08/04
require_once("../lib/xmlheader.inc");

$requestid = cleanreq ('requestID');// fixed
$orderguid = cleanreq('orderGuid');
$mcguid = cleanreq('dataGuid');
$origin= cleanreq('vAETitleOrigin');
$dest = cleanreq('vAETitleDest');
$protocol = cleanreq('protocol');
$globalstatus=cleanreq('globalStatus');
$itemtype=cleanreq('itemType');


$insert = db_prepare_routing_queue_insert($requestid,$orderguid,$mcguid,$dest,$origin,
					$protocol,$globalstatus,$itemtype)	;				
xmlinsert("routing_queue",$insert,false);

?> 

