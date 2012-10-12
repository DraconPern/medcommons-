<?
require_once "settings.php";
require_once "../securelib.inc.php";
require_once "securewslibdb.inc.php";
require_once "wslibdb.inc.php";
require_once "JSON.php";
require_once "utils.inc.php";

/**
 * Extended web service class containing utility functions for 
 * working with nodes and consents.
 */
abstract class securedbrestws extends dbrestws {

    protected $nodeid;
    
    protected $nodeKey;

    function generate_tracking() {
        global $TRACKING_URL;

        $db = DB::get();

        /*
         * TTW April 30, 2007
         * Use the mc_locals soap/rest server to worry about global
         * tracking number allocation.
         *
         * There must be a mc_locals service running at the IP address
         * 'mcid.internal' (put the name in the /etc/hosts file)
         */
        $trackingUrl = 
          isset($TRACKING_URL) ? $TRACKING_URL : 'http://mcid.internal:1080/tracking_number'; // default
         $tn = get_url($trackingUrl);
         $db->execute("INSERT INTO tracking_number (tracking_number, encrypted_pin) 
                       VALUES(?, '999999999999')",array("$tn"));
         return $tn;
     }

     function gethostarg(){
          $node= ($this->cleanhost($this->cleanreq('host'))); // get host name as supplied or use own ip address
          $this->nodeid =$node->node_id;
     }

     function getnodeid()
     {
         return $this->nodeid;
     }

     function muship($ip) {
        $mushedip = preg_replace("/\./", "","$ip");
        if(strlen($mushedip)>10) {
            $mushedip = substr($mushedip, strlen($mushedip)-9,10);
        }
        return $mushedip;
     }

     function findnodebykey($nodeKey) {
        $db = DB::get();
        $node = $db->first_row("select * from node where client_key = ?", array($nodeKey));
        return $node ? $node : false;
     }

     function findnodebyhost ($host) {
        $db = DB::get();
        $select="SELECT * FROM node WHERE (hostname= ?)";
        $node = $db->first_row($select,array($host));
        if(!$node) $this->xmlend("internal failure to find record in node table");
        return $node;
    }

    /**
     * Legacy wrapper - see securelib.inc.php
     *
     * Note: throws Exception for failures.
     */
    function get_authorized_rights($auth, $toAccount, $ctx=null) {
      return get_rights($auth, $toAccount,$ctx);
    }
    
    /**
     * Attempts to resolve a tracking number from the given document id
     */
    function resolveDocumentTrackingReference($docid) {
        $db = DB::get();
        $result = $db->query("select t.* 
                              from tracking_number t
                              where t.doc_id = $docid");

        // If the tracking number is ambiguous, return false
        if(count($result) == 1) 
          return $result[0];
        else
          return false;
    }
    
    //wld 11/22/05 - get document id given a guid
    function finddocument($storageId, $guid) {
        $db = DB::get();
        if(!$storageId)
	         throw new Exception("Invalid storage id (blank)");

        $select = "SELECT id FROM document WHERE (guid='$guid') and (storage_account_id = ?)";
        $result = $db->first_row($select,array($storageId));
        if(!$result)
          return ""; 
        else
         return $result->id;
    }
    
    /**
     * Returns the decryption key for the specified document.
     * Note that the guid argument is used only for the error message to avoid
     * reporting the docid back to the user.
     */
    function finddocumentDecryptionKey($guid, $docid, $node) //wld 11/22/05 - get document id given a guid
    {
        $db = DB::get();
        $select="SELECT encrypted_key FROM document_location WHERE (document_id = ?) and (node_id = ?)";
        $result = $db->first_row($select,array($docid,$node));

        if(!$result)
          return "";

        return $result->encrypted_key;
    }
    
    function updatedocumentlocation($docid, $nodeid, $ekey, $intstatus) //wld 11/22/05
    {
        $db = DB::get();

        $insert="UPDATE document_location SET
                 encrypted_key = ?, integrity_status = ?, integrity_check = NOW()
                 WHERE (document_id=?) AND (node_id = ?)";

        $db->execute($insert,array($ekey,$intstatus,$docid,$nodeid));
        return "ok";
    }

    function deletedocumentlocation($docid, $nodeid) {
        $db = DB::get();
        $delete="DELETE from document_location
                 WHERE (document_id=?) AND (node_id = ?)";
        $db->execute($delete, array($docid, $nodeid));
        return "ok";
    }

    // wld 11/22/05
    function adddocumentlocation($docid, $nodeid, $ekey, $intstatus) {
        $db = DB::get();
        $insert="INSERT INTO document_location (document_id,node_id,copy_number,
                 encrypted_key, integrity_check,integrity_status) 
                 VALUES(?,?,'1',?, NOW(),?)";

        //pick up the id we just created
        return $db->execute($insert,array($docid,$nodeid,$ekey,$intstatus));
    }

    /**
     * Return the first document location accessible for the given guid and storage id
     * with the given rights
     */
    function resolveGuid($storageId, $guid, $auth="", $ctx=null) {
        return resolve_guid($storageId, $guid, $auth, $ctx);
    }

    /**
     * Return the first document location accessible for the given tracking number and PIN
     */
    function resolveTracking($tracking, $pinHash=null, $auth=null) {
        return resolve_tracking($tracking,$pinHash, $auth);
    }

    function cleanhost($h = "")
    {
        // If there is a node key provided, look up node using that
        $this->nodeKey = req('node_key',req('nodeKey'));
        if(!$this->nodeKey && isset($_SERVER['HTTP_X_MEDCOMMONS_KEY'])) 
          $this->nodeKey = $_SERVER['HTTP_X_MEDCOMMONS_KEY'];
          
          //throw new Exception("FFOO - $nodeKey");
          
        if($this->nodeKey) {
          $node = $this->findnodebykey($this->nodeKey);
          if($node) {
            dbg("Found node {$node->node_id} using node_key {$this->nodeKey}");
            return $node;
          }
        }

        // We used to check ip address here, but it gets into lots of trouble
        // when our hosts are multi-homed.  Nowadays we use the node key
        // to properly identify hosts, so ip check is not necessary
        if($node)
            return $node;

        // if here, we failed
        $this->xmlend("Unable to identify node");
    }
}

/**
 * An extension of dbrestws to make it render JSON instead of
 * XML.  
 */
abstract class securejsonrestws extends securedbrestws {
  /**
   * Convenience method - sets error message and returns failure status.
   */
  function error($msg) {
    $this->message = $msg;
    return false;
  }

  /**
   * Dummy method
   */
  function xmlbody() {
    return $this->jsonbody();
  }

  /**
   * Handler to execute web service
   */
  function handlews($servicetag) {
      $this->set_servicetag($servicetag);
      
      $ex = null;
            
       try {
        $this->verify_caller();
        $result = $this->jsonbody();
      }
      catch(Exception $e) {
        $this->error($e->getMessage());
        $result = false;
        $ex = $e;
      }

      // Ensure the content type indicates javascript
      header ("Content-type: text/javascript");
      $json = new Services_JSON();
      $out = new stdClass;
      if($result !== false) {
        // If user has set the $result variable on the class, use that
        // as the whole response rather than the returned value.
        // this allows child class to override the whole response if 
        // desired.
        if(isset($this->result))
          $out = $this->result;
        else {
          $out->status = "ok";
          $out->result = $result;
        }
      }
      else {
        $out->status = "failed";
        if(isset($this->message)) {
          $out->message = $this->message;
          if($e && isset($e->code)) {
              $out->code = $e->code;
          }
        }
      }
      echo $json->encode($out);
  }
}
?>
