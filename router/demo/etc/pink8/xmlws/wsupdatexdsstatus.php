
<?php
// (c) 2004 MedCommons, Inc.
// wld 8/28/04
require_once("../lib/xmlheader.inc"); //


$og = cleanreq('studyGuid');
$status = cleanreq('status');

$query = db_prepare_xds_update_status($status,$og);
xmlupdate("xdsregistry", $query, false);
?>
