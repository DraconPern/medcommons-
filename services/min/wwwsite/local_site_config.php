<?php

/*

this is for the main web site touched 1059

*/

$WEBSITE = 'www.medcommons.net';  // no not include http or s
$GLOBALREDIRECTOR = 'https://www.medcommons.net'; // should always go to s
$GLOBALS['global_login_url']=$GLOBALREDIRECTOR.'/login/';

$GLOBALS['purchase_disabled'] = false;

$SOLOHOST='www.medcommons.net'; //only important if running single appliance configuration 

$HAS_LOCAL_APPLIANCE=false;


function select_random_appliance() {

	return "https://".'n000'.rand(1,1).".medcommons.net"; //where to allocate urls
}

global $CLUSTER_PREFIX;
$CLUSTER_PREFIX="n";
	

?>
