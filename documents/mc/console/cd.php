<?php

$limit = $_REQUEST['limit'];
$filter = $_REQUEST['filter'];
$url = $_REQUEST['url'];
header ("Location: $url?limit=$limit&filter=$filter");
exit;
?>