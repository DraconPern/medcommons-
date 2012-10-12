<?php
/**
 * queryServicesList.php - with no args, returns all, with appkey= returns just what is wanted
 *
 *
 */

header("Cache-Control: no-store, no-cache, must-revalidate");
header("Pragma: no-cache");
require_once "iphonewslibdb.inc.php";

class queryServicesList extends jsonrestws {
	function verify_caller() {

		return;
	}
	function jsonbody() {
		$db = DB::get();
		$this->result = new stdClass;
		$this->result->requesturi = $_SERVER['REQUEST_URI'];
			$this->result->postin = $_REQUEST;
		if (isset($_REQUEST['appkey']))
		{
			$appkey = $_REQUEST['appkey'];
			$sql = "select * from iphoneServices where category = '$appkey'  order by category,categorysortcode";
		} 
		else
		$sql = "select * from iphoneServices order by category,categorysortcode";
		$results = $db->query($sql,array());//$accid));
		if (count($results)==0)
		$this->result->status="failure - no Services";
		else
		{
			$this->result->status="ok";
			$this->result->services = $results;
	
			if (isset($_REQUEST['appkey']))

			$sql = "select * from iphoneApps where appkey = '$appkey' ";

			else
				
			$sql = "select * from iphoneApps ";

			$results = $db->query($sql,array());//$accid));
			if (count($results)==0)
			$this->result->status="failure - no iPhoneApp with key";
			else
			$this->result->appinfo = $results;

		}

	return true;
}
}

$x = new queryServicesList();
$x->handlews("response_queryServicesList");
?>
