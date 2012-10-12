<?php
// (c) 2004 MedCommons, Inc.
// wld 8/28/04
require_once("../lib/xmlheader.inc"); //


$og = cleanreq('destvAETitle');


$query=db_prepare_select_routing_queue($og);
//$GLOBALS['debug'] = "debug"; //set to debug to show on screen as plain text
xmlquery("routing_queue", $query, false);
?>

