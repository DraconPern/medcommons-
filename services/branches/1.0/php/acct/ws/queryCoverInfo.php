<?php
require_once "wslibdb.inc.php";
/**
 * queryCoverInfoWs 
 *
 * Returns information about a Cover previously registered
 *
 * Inputs:
 *    coverId - the id to query
 */
class queryCoverInfoWs extends dbrestws {
	function xmlbody(){
    $db = DB::get();

		// pick up and clean out inputs from the incoming args
		$coverId = $this->cleanreq('coverId');
		$sql = "select * from cover where cover_id = ?";
		$cover = $db->first_row($sql,array($coverId));
    if(!$cover) {
      $this->xm($this->xmnest("outputs",$this->xmfield("status","not found")));
    }
    else {
      $this->xm($this->xmnest("outputs",
        $this->xmfield("status","ok"). 
        $this->xmfield("accountId",$cover->cover_account_id).
        $this->xmfield("encryptedPin",$cover->cover_encrypted_pin).
        $this->xmfield("notification",$cover->cover_notification).
        $this->xmfield("providerCode",$cover->cover_provider_code).
        $this->xmfield("coverPin",$cover->cover_pin).
        $this->xmfield("title",$cover->cover_title).
        $this->xmfield("note",$cover->cover_note)
      ));
    }
	}
}

// main
$x = new queryCoverInfoWs();
$x->handlews("queryCoverInfo_Response");





?>
