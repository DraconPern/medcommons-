<?php

require "../dbparamsmcextio.inc.php";

require "sendfax.php.inc";

// send a fax via external service



//main

$filespec = $_REQUEST['file'];

if ($filespec == "") die ("usage - Need filespec");

$filecontents = base64_encode(file_get_contents($filespec));

$filetype = $_REQUEST['type'];

if ($filetype == "") die ("usage - Need filetype");

$faxnum = $_REQUEST['faxnum'];

if ($faxnum == "") die ("usage - Need faxnum");

$xmtnum = $_REQUEST['trackingnum'];

if ($xmtnum == "") die ("usage - Need trackingnum");

$recipient = $_REQUEST['recipient'];

if ($recipient == "") die ("usage - Need recipient");

echo "Attempting to fax $filespec of type $filetype to $faxnum with tracking $xmtnum....<br>\r\n";
echo sendfax ($xmtnum,$recipient,$faxnum,$filecontents,$filetype);
?> 