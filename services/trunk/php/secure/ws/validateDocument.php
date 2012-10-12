<?php

require_once "../ws/securewslibdb.inc.php";

/**
 * Validate a tracking number and return information about stored 
 * referenced document, as well as authentication token for access
 *
 * @param trackingNumber - tracking number to validate
 * @param pinHash - sha1 hash of pin for tracking number
 *
 * @author ssadedin@medcommons.net
 */
class validateWs extends securedbrestws {

	function xmlbody(){

    try {
      // get clean inputs
      $trackingNumber = req('trackingNumber');
      $pinHash = req('pinHash');
      $auth = req('auth');
      $esId = req('esId');
      
      
      if($auth && (strpos($auth,"token:")===0)) {
          $auth = substr($auth,6,40);
      }
      
      // echo inputs
      $this->xm($this->xmnest ("inputs",
            $this->xmfield("trackingNumber",$trackingNumber).
            $this->xmfield("pinHash",$pinHash)));

      $db = DB::get();

      // $this->gethostarg();

      // dbg("node id is ".$this->nodeid);

      $tns = $db->query( "SELECT * FROM tracking_number t, document d, document_location l, node n
                          WHERE t.tracking_number = ? and t.encrypted_pin = ?
                          AND d.id = t.doc_id
                          AND l.document_id = d.id
                          AND n.node_id = l.node_id
                          AND t.access_constraint <> 'EXPIRED'",
                          array($trackingNumber, $pinHash));

      if(count($tns)==0)
        throw new Exception("Tracking Number $trackingNumber not found");

      if(count($tns) > 1) 
        throw new Exception("Internal error - multiple entries for $trackingNumber found");
      
      $tn = $tns[0];
      
      $atEsId = $tn->es_id;
      
      // If the constraint is REGISTERED_EMAIL then the auth token for this 
      // transaction MUST correspond to one of the specified email 
      // addresses that is linked to the tracking number
      if($tn->access_constraint == "REGISTERED_EMAIL") {
          
          if($esId === null) 
              throw new Exception("Parameter esId required for tracking numbers linked to email addresses");    
          
          dbg("esId = $esId");
          $logins = $db->query("select * from users u, external_share es, authentication_token at
                                where
                                    u.email = es.es_identity
                                    and at.at_token = ?
                                    and at.at_account_id = u.mcid
                                    and es.es_identity_type = 'Email'
                                    and es.es_tracking_number = ?",
                   array($auth, $trackingNumber));
                   
          if(count($logins) == 0) {
              
              $email = $db->first_column(
                          "select es_identity from external_share where es_id = ? and es_identity_type ='Email'", 
                          array($esId));
              $this->xm($this->xmfield("email", $email));
              $this->xmlend("invalid email address");
              return;
          }
          
          $atEsId = $esId;
      }

      // Create a new authentication token
      $token = generate_authentication_token();
      $db->execute("insert into authentication_token (at_id, at_token, at_es_id) 
                    values (NULL,?,?)", array($token, $atEsId));

      dbg("Created authentication token $token for access to tracking number $trackingNumber");

      // If constraint is ONE_TIME then expire the tracking number
      if($tn->access_constraint == "ONE_TIME") {
        dbg("Accessing one time tracking number");
        $db->execute("update tracking_number set access_constraint = 'EXPIRED' where tracking_number = ?", array($trackingNumber));
        $tn->access_constraint = "EXPIRED";
      }

      // return outputs
      $this->xm($this->xmnest("outputs",
        $this->xmfield("mcid",$tn->storage_account_id).
        $this->xmfield("docid",$tn->doc_id).
        $this->xmfield("guid",$tn->guid).
        $this->xmfield("storageId",$tn->storage_account_id).
        $this->xmfield("host",$tn->hostname).
        $this->xmfield("node",$tn->node_id).
        $this->xmfield("node_key",$tn->client_key).
        $this->xmfield("ekey",$tn->encrypted_key).
        $this->xmfield("access_constraint",$tn->access_constraint).
        $this->xmfield("auth",$token).
        $this->xmfield("status","ok")));
    }
    catch(Exception $e) {
      $this->xmlend("internal error  - ".$e->getMessage());
    }
  }
}

//main

$x = new validateWs();
$x->handlews("validate_Response");



?>
