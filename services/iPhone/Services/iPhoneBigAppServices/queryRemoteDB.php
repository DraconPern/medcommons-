<?php
/**
 * queryServicesList
 *
 * Returns JSON representing an arbirary table
 *
 */

header("Cache-Control: no-store, no-cache, must-revalidate");
header("Pragma: no-cache");
require_once "../acct/alib.inc.php";
require_once "wslibdb.inc.php";
require_once "utils.inc.php";
require_once "mc.inc.php";
//  and (merge_status not in ('Hidden','Replaced') or merge_status is NULL
class queryRemoteDB extends jsonrestws {
	function verify_caller() {

      return;
	}
	function jsonbody() {
     $db = DB::get();

   // $accid = clean_mcid(req('accid'));
   // if(!is_valid_mcid($accid,true))
   //   throw new Exception("missing / invalid parameter: accid");

   // $sql = "select guid, status, date_format(date , '%Y-%m-%d %H:%i:%s') as date, tracking from ccrlog where accid=?  order by date desc";

    $sql = "select * from nyc_last order by boro,DBA limit 500";
    $results = $db->query($sql,array());//$accid));
    $this->result = new stdClass;
    $this->result->status="ok";
	$this->result->query=$sql;
	$this->result->count=count($results);
    $this->result->foodplaces = $results;
    return true;
  }
}

$x = new queryRemoteDB();
$x->handlews("response_queryRemoteDB");
?>
