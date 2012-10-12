<?php
/**
 * queryAccountCCRs.php 
 *
 * Returns JSON representing the CCRs for a given user's account
 *
 */

header("Cache-Control: no-store, no-cache, must-revalidate");
header("Pragma: no-cache");
require_once "iphonewslibdb.inc.php";
require_once "utils.inc.php";
require_once "mc.inc.php";

class GenericServiceResponderFailure extends jsonrestws {
	function verify_caller() {

      return;
	}
	function jsonbody() {
 
    $this->result = new stdClass;
    $this->result->status="failure";
    return true;
  }
}

$x = new GenericServiceResponderFailure();
$x->handlews("GenericServiceResponderFailure");
?>
