<?php
require_once "wslibdb.inc.php";

/**
 * Set Emergency CCR
 */
class setRedCCRWs extends dbrestws {

	function xmlbody(){

    $db = DB::get();

		// pick up and clean out inputs from the incoming args
		$accid = req('accid');
		$guid = req('guid');
		$clear = req('clear');
		$einfo = req('einfo');

		if ($clear!=1) // case where we are setting, clear existing
		{
			$ob = "UPDATE ccrlog SET status='WASRED', einfo='' WHERE (accid = '$accid') AND (status ='RED')";
			$db->execute($ob,array($accid));

			$ob = "UPDATE ccrlog SET status='RED',einfo=? WHERE (guid = ?) and (accid = ?)";
			$db->execute($ob,array($einfo,$guid,$accid));
		}
		else // case where we are clearing, just do it
		{
			$ob = "UPDATE ccrlog SET status='WASRED', einfo='' WHERE (accid = ?) AND (status ='RED') AND (guid=?)";
			$db->execute($ob,array($accid,$guid));
		}

    // update the time we reset this
		$timenow = time();		

		$ob = "UPDATE users SET  ccrlogupdatetime = '$timenow' where (mcid = ?)";
		$db->execute($ob,array($accid));

    // Insert into document_type table
    $insert = "insert into document_type (dt_id, dt_account_id, dt_type, dt_guid, dt_privacy_level,dt_comment) values
               (NULL, ?,'EMERGENCYCCR',?, 'Private','Emergency CCR');";

		$result = $db->execute($insert,array($accid,$guid));

		// update the time we reset this
		$this->xm($this->xmnest("outputs",$this->xmfield("status","ok $ob")));
	}
}

$x = new setRedCCRWs();
$x->handlews("setRedCCR_Response");
?>
