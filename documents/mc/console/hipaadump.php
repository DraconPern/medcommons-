<?PHP
require_once "hipaadump.inc.php";

// main starts here
$u = new hipaadumper();
$u->set_table("hipaa");
echo $u->table_dump('Hipaa Log');
?>