<?PHP
require_once "wslibdb.inc.php";
require_once "../ws/securewslibdb.inc.php";
require_once "utils.inc.php";
require_once "DB.inc.php";
require_once "mc.inc.php";

/**
 * Registers an encryption key for an account
 *
 * @param storageId       account that owns the documents
 * @param nodeKey         identity of caller
 * @param key             key to be registered
 * @return                JSON object { status: 'ok' } or { status: 'failed', error: 'an error message' }
 */
class registerKeyWs extends securejsonrestws {

	function jsonbody() {	    
	    
	    $decKey = req('key');
	    if(("NONE" != $decKey) && (preg_match("#^[A-Za-z0-9+/]{22}==$#", $decKey) !== 1))
	       throw new Exception("Parameter key has invalid value $decKey");
	       
	    $encKey = req('enc');
	    if(("NONE" != $encKey) && (preg_match("#^[A-Za-z0-9+/]{22}==$#", $encKey) !== 1))
	       throw new Exception("Parameter key has invalid value $encKey");

      if($decKey != $encKey)
	       throw new Exception("Only symmetric encrytpion supported by this key store.  Keys must be equal");
	    
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
	    
	    dbg("Registering key for account $storageId");
	    
	    $db = DB::get();

      // Check if key already exists
      $key = $db->first_row("select * from encryption_key
                            where ek_accid = ?
                            and ek_node_key = ?
                            and ek_key <> ?", array($storageId, $this->nodeKey, $decKey));
      if($key)
          throw new Exception("Account $storageId already has a encryption key");
        
      $db->execute("insert into encryption_key (ek_accid, ek_key, ek_node_key) 
                    values (?,?,?)", array($storageId,$decKey, $this->nodeKey));
	}
}

// main
global $is_test;
if(!isset($is_test)) {
  $x = new registerKeyWs();
  $x->handlews("response_registerKeyWs");
}
?>
