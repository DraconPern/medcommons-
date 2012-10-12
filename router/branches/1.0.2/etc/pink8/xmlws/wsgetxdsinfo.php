<?php
// (c) 2004 MedCommons, Inc.
// wld 8/28/04
require_once("../lib/xmlheader.inc"); //


$og = cleanreq('studyGuid');

$query = db_prepare_xds_select($og);
xmlquery("xdsregistry", $query,false);
?>
