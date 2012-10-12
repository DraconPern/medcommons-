<?php
require_once "../ws/securewslibdb.inc.php";
// see spec at ../ws/wsspec.html
class revokeTrackingNumberWs extends securedbrestws {


	function xmlbody(){
     $db = DB::get();

		// pick up and clean out inputs from the incoming args
		$tn = req('trackingNumber');
		$hp = req('hashedPin');

		// echo inputs
		$this->xm($this->xmnest ("inputs",
      $this->xmfield("trackingNumber",$tn).
      $this->xmfield("hashedPin",$hp)
      )
		);
		
		// make sure both arguments are supplied
		if ($hp=="") $this->xmlend("needs hashedPin");
		if ($tn=="") $this->xmlend("needs trackingNumber");

		// delete the record from the tracking_number table by setting the pin to an imposible value
		$update="UPDATE tracking_number SET encrypted_pin = '0000000000000000000000000000000000000000'
		               WHERE (tracking_number = ?) and (encrypted_pin = ?)";
		 
		$db->execute($update,array($tn,$hp));
		
		$status = "ok";
		$this->xm($this->xmnest("outputs", $this->xmfield("status",$status)));
	}
}

//main

$x = new revokeTrackingNumberWs();
$x->handlews("revokeTrackingNumber_Response");
?>
