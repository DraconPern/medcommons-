<?php
$GLOBALS['appliance'] = 'https://tenth.medcommons.net/'; // where urls get made
$GLOBALS['appliance_access_token'] ='a478fa35b20261d62c89a38d61dfc45fd93a9411';
$GLOBALS['appliance_access_secret'] ='96647642028e3ce11f0401161b23bd8b6ce47a6c';

$GLOBALS['DB_Connection'] = "mysql.internal";
$GLOBALS['DB_User']= "medcommons";
$GLOBALS['DB_Database'] = "mcst01";



// Local setup file, overrides defaults in this file
// to allow deployment on other systems, please
// do not check in a file of name 'local_setup.inc.php'!
if(file_exists("local_setup.inc.php")) {
  include "local_setup.inc.php";
}
?>
