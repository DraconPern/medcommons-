<?php
require_once "../ws/securewslibdb.inc.php";
/**
 * updatePINWs 
 *
 * Updates a specified tracking number with a new guid.
 *
 * Inputs:
 *    trackingNumber - tracking number to update
 *    pinHash - hashed form of pin for specified tracking number
 *    guid - new guid for specified tracking number
 */
class updatePINWs extends securedbrestws {
	// variant of registerTrackDocument with trackingNumber supplied

	
	function xmlbody(){
     $db = DB::get();
		
		//$this->gethostarg();

		// pick up and clean out inputs from the incoming args
		$oldPinHash = req('oldPINHash');
		$newPinHash = req('newPINHash');
		$tracking = req('trackingNumber'); 
		
		//
		// echo inputs
		//
		$this->xm($this->xmnest ("inputs",	
		$this->xmfield("oldPINHash",$oldPinHash).
		$this->xmfield("newPINHash",$newPinHash).	
		$this->xmfield("trackingNumber",$tracking) ));
		
		// check to make sure the entry is correct
		$query = "SELECT * from tracking_number WHERE tracking_number = ? AND encrypted_pin=?";

		$rows = $db->query($query,array($tracking,$oldPinHash));
    if(count($rows) != 1) 
        $this->xmlend("Can not find requested tracking number with given credentials"); 

    $trackingNumber = $rows[0];
    
    // Update the tracking number table
    $update = "update tracking_number set encrypted_pin = ? where tracking_number =?";
		$db->execute($update,array($newPinHash,$tracking));
		
		// return outputs
		// docid,rightsid,mcid
		$this->xm($this->xmfield("status","ok"));
	}
}

// main
$x = new updatePINWs();
$x->handlews("updatePIN_Response");

?>
