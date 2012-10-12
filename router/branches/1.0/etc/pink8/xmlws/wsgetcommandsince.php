<?php
// (c) 2004 MedCommons, Inc.
// wld 8/28/04
require_once("../lib/xmlheader.inc"); // needs $request


$filter = cleanreq('FilterName');//input fields need cleansing


$query = db_prepare_select('remote_commands');

xmlquery("remote_commands",$query,false);


?>