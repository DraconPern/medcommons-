

<?php
//wsInsertXDS
// (c) 2004 MedCommons, Inc.
// wld 10/1/04
require_once("../lib/xmlheader.inc");


$id='9999996';
$studyguid=cleanreq('studyguid');
$tracking=cleanreq('tracking');
$vaetitle=cleanreq('vaetitle');
$nimages=cleanreq('nimages');
$nseries=cleanreq('nseries');
$studydescription=cleanreq('studydescription');
$studytime=cleanreq('studytime');
$comments=cleanreq('comments');
$modaility=cleanreq('modality');
$affiliate=cleanreq('affiliate');
$history=cleanreq('history');
$patient=cleanreq('patient');
$patientaddr=cleanreq('patientaddr');
$status=cleanreq('status');



$insert=	db_prepare_xds_insert($id,$studyguid,$tracking,$vaetitle,$nimages,$nseries,$studydescription,$studytime,$comments,
$modaility,$affiliate,$history,$patient,$patientaddr,$status);								
									

xmlinsert("xdsregistry",$insert,false);

?> 

