<?php
require_once('../whitebox/wbsubs.inc');

require_once('../whitebox/displayorderform.inc');


$tracking = cleanreq('tracking');

readconfig();

//getxdsinfo($guid,$tracking,$account,$address,$history,$comments);

display_order_form(
$tracking,
$guid,
$account,
$address,
$history,
$comments,
$partnererror='please enter or modify your order',
$mcerror='');

?>
