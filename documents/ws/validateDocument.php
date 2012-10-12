<?php
require_once "../ws/wslib.inc.php";
// see spec at ../ws/wsspec.html
class validateWs extends dbrestws {

	function xmlbody(){
		//
		// get clean inputs
		//
		$trackingNumber=$this->cleanreq('trackingNumber');
		$pinHash =$this->cleanreq('pinHash');
		
		
		//
		// echo inputs
		//

		$this->xm($this->xmfield ("inputs",
		$this->xmfield("trackingNumber",$trackingNumber).
		$this->xmfield("pinHash",$pinHash)));


		$select ="SELECT * FROM tracking_number
					WHERE ((tracking_number='$trackingNumber') and
					(encrypted_pin = '$pinHash'))";

		$result = $this->dbexec($select,"can not select from table tracking_number - ");
		$count = mysql_numrows($result);
		$status = "ok";
		if ($count<1) $status = "not found"; else if ($count >1) $status = "too many matches";
		if ($status!="ok") {$this->xmfield ("lookup",$status); $this->xmlend ($status);}
		$trobj = mysql_fetch_object($result);
		$rights_id = $trobj->rights_id;
	
		// go to the rights table to get the user_medcommons_id
		$select="SELECT * FROM rights WHERE (rights_id = '$rights_id')";
		$result = $this->dbexec($select,"can not select from table rights - ");
		$robj = mysql_fetch_object($result);
		if ($robj===FALSE) $this->xmlend("internal failure to find record in rights table");

		
		$docid =$robj->document_ID;
	
		// go to the document table to get the guid
		$select="SELECT * FROM document WHERE (id = '$docid')";
		$result = $this->dbexec($select,"can not select from table document - ");
		$dobj = mysql_fetch_object($result);
		if ($dobj===FALSE) $this->xmlend("internal failure to find record in document table");


		//
		// return outputs
		//
		$this->xm($this->xmfield ("outputs",
		$this->xmfield("mcid",$robj->user_medcommons_user_id).
		$this->xmfield("docid",$docid).
		$this->xmfield("rightsid",$rights_id).
		$this->xmfield("guid",$dobj->guid).

		$this->xmfield("status",$status)));
	}
}

//main

$x = new validateWs();
$x->handlews("validate_Response");



?>