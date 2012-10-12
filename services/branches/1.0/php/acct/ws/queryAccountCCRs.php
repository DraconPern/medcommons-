<?php
require_once "wslibdb.inc.php";
require_once "utils.inc.php";

/**
 * queryAccountCCRs.php 
 *
 * Returns JSON representing the CCRs for a given user's account
 *
 */

header("Cache-Control: no-store, no-cache, must-revalidate");
header("Pragma: no-cache");
require_once "../alib.inc.php";
require_once "wslibdb.inc.php";
require_once "utils.inc.php";
require_once "mc.inc.php";

class queryAccountCCRsWs extends jsonrestws {
	function jsonbody() {
     $db = DB::get();

    $accid = clean_mcid(req('accid'));
    if(!is_valid_mcid($accid,true))
      throw new Exception("missing / invalid parameter: accid");

    $sql = "select guid, status, date_format(date , '%Y-%m-%d %H:%i:%s') as date, tracking from ccrlog where accid=?
      and (merge_status not in ('Hidden','Replaced') or merge_status is NULL)
      order by date desc";

    $results = $db->query($sql,array($accid));

    $this->result = new stdClass;
    $this->result->status="ok";
    $this->result->ccrs = $results;
    return true;
  }
}

$x = new queryAccountCCRsWs();
$x->handlews("response_queryAccountCCRs");
?>
