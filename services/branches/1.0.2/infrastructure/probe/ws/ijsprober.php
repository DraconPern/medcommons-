<?PHP
// report status of medcommons appliance as JSON
require 'settings.php';
require_once 'utils.inc.php';
require_once "ijswslib.inc.php";

function countof($table)
{

	global $pdo,$IDENTITY_PDO, $IDENTITY_USER, $IDENTITY_PASS, $DB_SETTINGS;
	$db = new PDO($IDENTITY_PDO, $IDENTITY_USER, $IDENTITY_PASS, $DB_SETTINGS);

	$sql = <<<EOF
	SELECT COUNT(*)
	FROM $table
EOF;

	$stmt = $db->prepare($sql);
	$result = $stmt->execute(array());

	if ($result) {
		if ($count = $stmt->fetchColumn()) {
			return $count;
		}
	}
	return false;

}
class mcApplianceProbe extends jsonws {
	private $row_number = 0;
	private $data ;  //
	protected  $map;  //accumulates any additional data rows to be returned as part of the jsws reply

	function addprop($tag,$val)
	{
		$this->map[$tag]=$val;
	}

	function main_web_service ()
	{

		$this->table_info(); // all side effects

		$diskfree = disk_free_space("/")/(1024*1024*1024);
		$diskfree = number_format($diskfree, 2, '.', '');

		$disktotal = disk_total_space("/")/(1024*1024*1024);
		$disktotal = number_format($disktotal, 2, '.', '');

		$x =  exec('cat /proc/loadavg');
		$loadavg='';
		for ($i=0; $i<strlen($x); $i++)
		{
			$c =  (substr($x,$i,1));
			if (($c=='.')||($c>='0'&&$c<='9')) $loadavg .=$c; else break;
		}
		return array(
		"diskfreespace"=>$diskfree,
		"disktotalspace"=>$disktotal,
		'loadavg'=>$loadavg,
		'data'=>$this->data);
	}
	// these functions are executed for side effects to the data map
	function one_table($table)
	{
		$this->map = array(); // will accumulate in here
		$this->addprop("id",$this->row_number++);
		$this->addprop("table",$table);
		$this->addprop("count",countof($table));
		$this->addprop("status",mysql_error());
		$this->data[]=$this->map;
	}

	function table_info()
	{
		$this->data = array();
		$this->one_table("document");
		$this->one_table("groupinstances");
		$this->one_table("users");
		$this->one_table("tracking_number");
		$this->one_table("modcoupons");
	}

}// end outer clas

//main

$jsws = new mcApplianceProbe();
$jsws->json_service_handler("monitor_db_counters");

?>