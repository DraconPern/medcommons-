<?php


require_once "../ws/securewslibdb.inc.php";
require_once "DB.inc.php";
require_once "mc.inc.php";

/**
 * Returns the decryption key for the specified document at the specified node.
 * <p>
 * It might make sense to also test the status of the file - perhaps if the file
 * failed an integrity check it should return some type of warning. 
 */
class getDocumentDecryptionKeyWs extends securedbrestws {

    function xmlbody() {

        // pick up and clean out inputs from the incoming args
        $guid = $this->cleanreq('guid');
        $storageId = req('storageId');
        if(!is_valid_mcid($storageId, true)) 
            throw new Exception("Parameter storageId has invalid value $storageId");
        
        $nodeKey = req('nodeKey');
        if(!is_valid_guid($nodeKey))
            throw new Exception("Parameter nodeKey has invalid value $nodeKey");
            
        $node = $this->cleanhost();
        $nodeId = $node->node_id;

        // echo inputs
        $this->xm($this->xmnest("inputs", $this->xmfield("node", $nodeId).$this->xmfield("guid", $guid)));

        // We used to use per-document keys and actually look for the individual document 
        // Now however we use a per-patient encryption key and thus finding *any* 
        // previous key for any document for the patient works
        
        $db = DB::get();

        $key =$db->first_row("select * from encryption_key
                              where ek_accid = ?
                              and ek_node_key = ?",array($storageId, $this->nodeKey));
        if($key) {
            $this->xm($this->xmnest("outputs", 
              $this->xmfield("entry", 
                $this->xmfield("encrypted_key", $key->ek_key),
                $this->xmfield("guid", ""),
                $this->xmfield("status", "ok"))));
            return;
        }

        // Legacy
        $loc = $db->first_row("select * 
                               from document_location l, document d, node n
                               where d.storage_account_id = ? 
                                 and l.encrypted_key is NOT NULL
                                 and l.encrypted_key <> ''
                                 and l.node_id = n.node_id
                                 and l.document_id = d.id
                                 and n.client_key = ?
                               limit 1",
                              array($storageId, $nodeKey));
        // return outputs
        if(!$loc) {
            $this->xm($this->xmfield("status", "can't find key for storage id $storageId"));
            return;
        }
        
        $this->xm($this->xmnest("outputs", 
	        $this->xmfield("entry", 
		        $this->xmfield("encrypted_key", $loc->encrypted_key),
		        $this->xmfield("guid", $guid),
		        $this->xmfield("status", "ok"))));
		        
        return;
	}
}

// main
$x = new getDocumentDecryptionKeyWs();
$x->handlews("getDecryptionKey_Response");
?>
