<?PHP
require_once "securewslibdb.inc.php";
require_once "../securelib.inc.php";
require_once "utils.inc.php";

/**
 * Updates access rights for an account according to the specifications in the 
 * request parameters.
 *
 * Accepts parameters as account ids, with values of each parameter indicating
 * the requested sharing level for the account.
 *
 * There are 3 special formats accepted for account ids:
 *
 *  - prefix of "es_" is treated as an external share and is updated accordingly
 *  - prefix of "at_" is treated as an application and is updated accordingly 
 *
 * @param accid - target account for which consents are to be updated
 * @param <mcid> - access level for given medcommons account id <mcid>
 * @param es_<esId> - access level for given external share <esId> 
 * @param auth  - auth token proving rights to update this account
 *
 * Example:
 *
 *   updateAccess.php?accid=1012576340589251&1013062431111407=RW : updates Jane H's consents 
 *                                                                 so that account 1012576340589251 can access her CCR
 *   updateAccess.php?accid=1012576340589251&es_13872=R : updates external sharer 13872 to have only R access to Jane H's CCR.
 */
class updateAccessWs extends securejsonrestws {
    
  function verify_caller() {
    try {
        verify_local_call();
    }
    catch(Exception $ex) {
        // If we cannot verify that this is a local call,
        // allow it to come from a remote gateway by 
        // delegating to parent to check node key
        parent::verify_caller();
    }
  }
 
  function jsonbody() {
     $db = DB::get();
     
     

    $storageAccId = req('accid');
    if($storageAccId==null)
      throw new Exception("Required parameter accid missing");

    $auth = req('auth');
    
    $expiry = get_account_expiry($storageAccId);
    
    // The caller must have access
    $rights = get_rights($auth, $storageAccId);

    if(strpos($rights,"W")===FALSE) 
      throw new Exception("insufficient rights to update consents for account $storageAccId");

    dbg("Updating storage account ".$storageAccId);
    $returnResult = new stdClass;

    // Note we can't use $_REQUEST because PHP mutilates the variable names
    $name_value_pairs = explode("&",$_SERVER['QUERY_STRING']);
    foreach($name_value_pairs as $nvp) {
      $param = explode('=',$nvp);
      $query_string_params[urldecode($param[0])] = count($param>1) ? urldecode($param[1]) : "";
    }

    foreach($query_string_params as $accid => $rights ) {
      if($accid == "accid")
        continue;

      dbg("acct: $accid => $rights");
      if(preg_match("/^[0-9]{16}$/",$accid) || preg_match("/^es_.*/",$accid) || preg_match("/^at_.*/",$accid) || is_url($accid)) { // If parameter matches account id format

        $esId = null;
        $at = null;

        if(is_url($accid)) {
          // Look up the external share id or create one
          $row = $db->first_row("select es_id from external_share where es_identity = ? and es_identity_type = 'openid'", array($accid));
          if($row) { // This openid has already been shared with before.  We can reuse that id.
            $esId = $row[0];
            dbg("Found existing external share $esId for openid $accid");
          }
          else { // This openid has not been shared with before
            $esId = $db->execute("insert into external_share (es_id,es_identity,es_identity_type) values (NULL,?,'openid')",
                          array($accid));
          }
        }
        else
        if(preg_match("/^es_.*/",$accid)) {
          $s = explode("_",$accid);
          $esId = $s[1];
        }
        else
        if(preg_match("/^at_.*/",$accid)) {
          $s = explode("_",$accid);
          $at = $s[1];
          dbg("Application token = $at");
        }

        // Deactivate old rights entries
        if($esId !== null) { // specified account pointed to external share
          $rows = $db->query("select r.* from rights r where es_id=? and storage_account_id = ? and active_status = 'Active'",
              array($esId, $storageAccId));
        }
        else
        if($at !== null) {
          // Find if there is an external share for this application yet
          $rows = $db->query("select distinct r.*
              from rights r, external_share es, authentication_token at, authentication_token atp
              where es.es_id = r.es_id
              and at.at_es_id = es.es_id
              and atp.at_id = at.at_parent_at_id
              and r.storage_account_id = '$storageAccId'
              and r.active_status = 'Active'
              and atp.at_token = ?
              and es.es_identity not like '%/%'
              and es.es_identity_type = 'Application'",array($at));

          dbg("found ".count($rows)." rights for application token ".$at);
        }
        else {
          // Find all rights entries for the given account
          $rows = $db->query("select r.* from rights r left join external_share es on es.es_id = r.es_id
                                 where (account_id = ? or es.es_identity = ?) and storage_account_id = ? and active_status = 'Active'",
              array($accid,$accid,$storageAccId));
        }

        $deactivated = array();
        foreach($rows as $right) {
          dbg("Deactivating right ".$right->rights_id." for account $accid = ".$right->rights);
          $deactivated[$right->es_id]=$right;
          $db->execute("update rights set active_status = 'Inactive', expiration_time = CURRENT_TIMESTAMP where rights_id = ".$right->rights_id);
        }

        // Insert the new value
        if($at != null) { // is it an application level right?
          foreach($deactivated as $esId => $right) {
            dbg("adding application right es_id = $esId");
            $db->execute("insert into rights (account_id,es_id,document_id,storage_account_id,rights,expiration_time) ".
                "VALUES(NULL,$esId,NULL, ?,?,?)",array($storageAccId,$rights,$right->expiration_time));
          }
        }
        else {
          $accidValue="'$accid'";
          if($esId) { // If parameter matches OpenID format
            $accidValue="NULL";
            $esIdValue=$esId;
          }
          else {
            $esIdValue = "NULL";
          }
          
          $sql = "INSERT INTO rights (account_id,es_id,document_id,storage_account_id,rights,expiration_time) ".
                 "VALUES($accidValue,$esIdValue,NULL, $storageAccId,?,?)";
          
          $id = $db->execute($sql,array($rights,$expiry));
          dbg("Inserted right $id for account $accid = ".$rights);
        }
        
        // Finally, remove any access to individual documents so that they do not conflict with
        // the new settings we have saved
        if(!$esId && !$at) {
          dbg("cleaning up");
          $db->execute("update rights r, document d
                         set r.active_status = 'Inactive'
                         where r.account_id = ?
                         and r.document_id = d.id
                         and d.storage_account_id = ?",array($accid,$storageAccId));
        }
      }
    }
    return $returnResult;
	}
}

// main
global $is_test;
if(!isset($is_test)) {
  $x = new updateAccessWs();
  $x->handlews("response_updateAccess");
}
?>
