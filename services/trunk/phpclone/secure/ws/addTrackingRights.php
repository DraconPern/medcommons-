<?PHP
require_once "securewslibdb.inc.php";
require_once "../securelib.inc.php";
require_once "utils.inc.php";
require_once "mc.inc.php";

/**
 * Adds additional documents to the primary document accessed by a tracking number.
 * 
 * @author ssadedin
 */
class addTrackingRights extends securejsonrestws {

	function jsonbody() {
	     $db = DB::get();
	     
	     $tn = req('trackingNumber');
	     if(preg_match("/^[0-9]{12}$/",$tn) !== 1)
	       throw new Exception("Bad value for parameter 'trackingNumber'");
	     
	     $guids = explode(',',req('guids',''));
	     
	     foreach($guids as $guid) {
		     if(!is_valid_guid($guid))
		       throw new Exception("Bad value for parameter 'guid'");
	     }
	     
	     $hpin = req('hpin');
	     if(!is_valid_guid($hpin))
	       throw new Exception("Bad value for parameter 'hpin'");
	       
	     dbg("Adding rights to documents $guids for tracking number $tn");
	       
	     // Find the external share for the specified tracking number
	     $track = $db->first_row("select * from tracking_number where tracking_number = ? and encrypted_pin = ?",
	                              array($tn, $hpin));
	     if(!$track)
	       throw new Exception("Unable to locate tracking number $tn with supplied pin");
	       
	     if(!$track->es_id)
	       throw new Exception("No external share found for tracking number $tn");
	       
	     foreach($guids as $guid) {
	         
		     // Find the document to add
		     $doc = $db->first_row("select * from document where guid = ?", array($guid));
		     if(!$doc)
		       throw new Exception("No document found for supplied guid $guid");
		       
		     if(isset($last_storage_id) && ($last_storage_id != $doc->storage_account_id)) {
			     $expiry = get_account_expiry($doc->storage_account_id);
			     dbg("Document $guid has expiry $expiry");
			     $last_storage_id = $doc->storage_account_id;
		     }
		       
		     $db->execute("insert into rights (rights_id, document_id, rights, es_id, expiration_time) 
		                   values (NULL,?, 'RW', ?, ?)", array($doc->id, $track->es_id, $expiry));
	     }
	}
}

// main
global $is_test;
if(!isset($is_test)) {
  $x = new addTrackingRights();
  $x->handlews("response_addTrackingRights");
}
?>
