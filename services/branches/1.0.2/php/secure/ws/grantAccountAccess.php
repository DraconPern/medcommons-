<?php

/**
 * Grants a specified account access rights to another account.
 *
 * @param accessBy - comma separated list of accounts that will be granted access
 * @param accessTo - target account that will be madded accessible to accessBy
 * @param es_identity - an external identity to be granted access
 * @param es_identity_type - an external identity type to be granted access
 * @param es_first_name - optional first name of person granted access
 * @param es_last_name - optional last name of person granted access
 * @param es_auth_token - a token to act of parent token created for external share.
 * @param auth - a token having equal or greater rights as those being granted.
 * @param rights - string of characters indicating rights to be granted (eg. "RW").
 *
 * TODO: require auth / login to secure this service
 */
require_once "../ws/securewslibdb.inc.php";
require_once "../securelib.inc.php";
require_once "utils.inc.php";

class grantAccountAccessWs extends securejsonrestws {

 function jsonbody(){
     $db = DB::get();

    $accessTo=$_REQUEST["accessTo"];
    $accessBy=req('accessBy','');
    $rights=req('rights');
    $es_identity = req('es_identity');
    $es_identity_type = req('es_identity_type');
    $es_auth_token = req('es_auth_token');
    $es_first_name = req('es_first_name');
    $es_last_name = req('es_last_name');

    $ret = new stdClass;

    if($rights == null) 
      throw new Exception('parameter rights is required');

    $accessBys=explode(',',$accessBy);
    
    $expiry = get_account_expiry($accessTo);
    
    dbg("Expiry time for account $accessTo is $expiry");

    foreach($accessBys as $ab) {
      $esId=null;
      $mcid=null;
      if(is_url($ab)) { // External Id (OpenID)
        $esId = $db->execute("insert into external_share (es_id, es_identity, es_identity_type )
                       values (NULL, ?, 'openid')",array($ab));
      }
      else {
        $mcid = $ab;
      }
      
      $result = $db->execute("INSERT INTO rights (account_id,es_id,storage_account_id,rights,creation_time, expiration_time) ".
                             "VALUES(?,?,?,?,NOW(),?)", array($mcid,$esId,$accessTo,$rights,$expiry));
    }

    if($es_identity) {

        $esId = $db->execute("insert into external_share (es_id, es_identity, es_identity_type, es_first_name, es_last_name)
                                values (NULL, ?, ?, ?, ?)",
                                array($es_identity, $es_identity_type, $es_first_name, $es_last_name));

        $result = $db->execute(
          "INSERT INTO rights (account_id,es_id,storage_account_id,rights,creation_time,expiration_time) ".
          "VALUES(NULL,$esId,?,?,NOW(),?)",array($accessTo,$rights,$expiry));

        $parent_at = $db->first_row("select * from authentication_token where at_token = '$es_auth_token'");
        if(!$parent_at)
          throw new Exception("Failed to find authentication token ".$es_auth_token);

        // Create a new authentication token 
        $at = generate_authentication_token();
        $secret = generate_authentication_token();

        $db->execute("insert into authentication_token (at_id, at_token, at_secret, at_es_id, at_parent_at_id) 
                       values (NULL, '$at','$secret', $esId, {$parent_at->at_id})");

        $ret->es_id = $esId;
        $ret->authentication_secret = $secret;
        $ret->authentication_token = $at;
    }
    return $ret;
  }
}

// main
$x = new grantAccountAccessWs();
$x->handlews("grantAccountAccess_Response");
?>
