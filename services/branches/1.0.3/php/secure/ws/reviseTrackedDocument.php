<?php
require_once "../ws/securewslibdb.inc.php";
/**
 * reviseTrackedDocumentWs 
 *
 * Updates a specified tracking number with a new guid.
 *
 * Inputs:
 *    trackingNumber - tracking number to update
 *    pinHash - hashed form of pin for specified tracking number
 *    guid - new guid for specified tracking number
 */
class reviseTrackedDocumentWs extends securedbrestws {
	function xmlbody() {
     $db = DB::get();
		
		$this->gethostarg();
		// pick up and clean out inputs from the incoming args
		$guid = $this->cleanreq('guid');
		$rights = $this->cleanreq('rights');
		$ekey = $this->cleanreq('ekey');		
		$intstatus = $this->cleanreq('intstatus');
		$pinHash = $this->cleanreq('pinHash');
		$tracking = $this->cleanreq('trackingNumber'); 
		
		//
		// echo inputs
		//

		$this->xm($this->xmnest ("inputs",	
      $this->xmfield("rights",$rights).
      $this->xmfield("pinHash",$pinHash).	
      $this->xmfield("ekey",$ekey).	
      $this->xmfield("trackingNumber",$tracking).			
      $this->xmfield("guid",$guid)));
		
		// check to make sure the entry is correct
		$query = "SELECT * from tracking_number WHERE tracking_number = '$tracking' AND (encrypted_pin='$pinHash' OR encrypted_pin='999999999999')";

		$rows = $db->query($query);
		if (count($rows) != 1) $this->xmlend("Can not find requested tracking number with given credentials"); 

    $trackingNumber = $rows[0];
    if($trackingNumber === false) {
      $trackingNumber = new stdClass;
    }
    
    // Find the correct document id
    $query = "select id from document where guid = ?";
		$documentRow = $db->first_row($query, array($guid));
    if(!$documentRow) {
      $this->xmlend("no rows found for requested document in reviseTrackedDocument - ");
    }
    $docid = $documentRow[0];

    // Update the rights table to point at the new document
    $update = "update rights set document_id = $docid where rights_id = ?";
		$result = $db->execute($update,array($trackingNumber->rights_id));

		// return outputs
		// docid,rightsid,mcid
		$this->xm($this->xmnest("outputs",
		$this->xmfield("docid",$docid).		
    $this->xmfield("rightsid",isset($trackingNumber->rights_id) ? $trackingNumber->rights_id : "" ).
		$this->xmfield("status","ok")));
	}
}

//main
$x = new reviseTrackedDocumentWs();
$x->handlews("reviseTrackedDocument_Response");

?>
