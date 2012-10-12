<?php
//wsInsertOrder
// (c) 2004 MedCommons, Inc.
// wld 8/28/04
require_once("../lib/xmlheader.inc");


$tracking = cleanreq('tracking');
$orderguid = cleanreq('orderGuid');
$origin= cleanreq('vAETitleOrigin');
$dest = cleanreq('vAETitleDest');
$description = cleanreq('description');
$patientname=cleanreq('patientName');
$patientid=cleanreq('patientId');
$modality = cleanreq('modality');



									
									
$insert = db_prepare_order_insert($id,$orderguid,$tracking,$origin,$dest,$description,
									$patientname,$patientid,$modality);
xmlinsert("orders",$insert,false);

?> 

