<?php
require_once('../whitebox/wbsubs.inc');
require_once('../whitebox/displayhomepage.inc');
$tracking = cleanreq('tracking');
session_start();
sqltraceon();
display_home_page("Welcome to Medcommons, providing the world of medical imaging 
            on your desktop",'',$tracking);
sqltracedump();

?>
