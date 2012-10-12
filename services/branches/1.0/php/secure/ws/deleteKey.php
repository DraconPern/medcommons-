<?PHP
require_once "wslibdb.inc.php";
require_once "../ws/securewslibdb.inc.php";
require_once "utils.inc.php";
require_once "DB.inc.php";
require_once "mc.inc.php";

/**
 * Deletes the encryption key for an account
 *
 * @param storageId       account that owns the documents
 * @param nodeKey         identity of caller
 * @param key             key to be registered
 * @return                JSON object { status: 'ok' } or { status: 'failed', error: 'an error message' }
 */
class deleteKeyWs extends securejsonrestws {

	function jsonbody() {	    
	    
	    // Node key *should* be getting validated anyway, but 
	    // we might as well check here too
	    if(!isset($_SERVER['HTTP_X_MEDCOMMONS_KEY']))
	       throw new Exception("Parameter nodeKey not set");
	    
      $this->nodeKey = $_SERVER['HTTP_X_MEDCOMMONS_KEY'];
	    
	    dbg("Using node key ".$this->nodeKey);
	    if(!is_valid_guid($this->nodeKey,true))
	       throw new Exception("Parameter nodeKey has invalid value");
	    
	    $storageId = req('storageId');
	    if(!is_valid_mcid($storageId,true))
	       throw new Exception("Parameter storageId has invalid value $storageId");
	    
	    dbg("Deleting key for account $storageId");
	    
	    // The user must have at least one document registered before we can 
	    // set their encryption key
	    $db = DB::get();

      $db->execute("delete from encryption_key 
                    where ek_accid = ? 
                    and ek_node_key = ?", array($storageId, $this->nodeKey));

      $db->execute("update document_location l, document d, node n
                    set l.encrypted_key = NULL
                        where d.storage_account_id = ? 
                           and l.encrypted_key is NOT NULL
                           and l.node_id = n.node_id
                           and l.document_id = d.id
                           and n.client_key = ?",array($storageId, $this->nodeKey));
	}
}

// main
global $is_test;
if(!isset($is_test)) {
  $x = new deleteKeyWs();
  $x->handlews("response_deleteKeyWs");
}
?>
