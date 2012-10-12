<?php
require_once "../ws/wslib.inc.php";
// see spec at ../ws/wsspec.html
class trackDocumentWs extends dbrestws {

	function generate_tracking() {
		return rand(100000,999999).rand(100000,999999);
	}
	function xmlbody(){
		// pick up and clean incoming arguments
		$rightsid=$this->cleanreq('rightsid');
		$pinHash=$this->cleanreq('pinHash');

		//
		// echo inputs
		//
		$this->xm($this->xmfield ("inputs",
		$this->xmfield("rightsid",$rightsid).
		$this->xmfield("pinHash",$pinHash)));

		//
		// make up a tracking number and add to the tracking table

		$timenow=time();
		
		$tracking = $this->generate_tracking(); // pick one out of thin air
		

		$insert="INSERT INTO tracking_number (tracking_number,encrypted_pin, rights_id) ".
					"VALUES('$tracking','$pinHash','$rightsid')";
		$this->dbexec($insert,"can not insert into table tracking_number - ");
		
		//
		// return outputs
		//
		$this->xm($this->xmfield ("outputs",
		$this->xmfield("trackingNumber",$tracking).
		$this->xmfield("status","ok")));
	}
}

//main

$x = new trackDocumentWs();
$x->handlews("trackDocument_Response");



?>