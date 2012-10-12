<?php
// (c) 2004 MedCommons, Inc.
// wld 9/9/04
// Upate Routing Queue Element Info
require_once("../lib/xmlheader.inc"); //
$requestid = cleanreq ('requestID');

$og = cleanreq('orderGuid');
$mg = cleanreq('dataGuid');
$globalStatus = cleanreq('globalStatus');
$bytesTotal = cleanreq('bytesTotal');
$bytesTransferred = cleanreq('bytesTransferred');
$restartCount = cleanreq('restartCount');
$timeStarted = cleanreq('timeStarted');
$timeCompleted = cleanreq('timeCompleted');

$query = db_prepare_routing_queue_update ($globalStatus,$bytesTotal,$bytesTransferred,
								$restartCount,$timeStarted,$timeCompleted,$requestid);								
xmlupdate("routing_queue", $query,false);
?>
