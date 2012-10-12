<?php


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
