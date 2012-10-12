<?PHP
require_once "hipaadump.inc.php";

// main starts here
$u = new hipaadumper();
$u->set_table("hipaa_trace");
echo $u->table_dump('Hipaa Trace');
?>