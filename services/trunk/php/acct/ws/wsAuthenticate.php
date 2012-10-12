<?php
header("Cache-Control: no-store, no-cache, must-revalidate");
header("Pragma: no-cache");
require_once "DB.inc.php";
require_once "wslibdb.inc.php";
require_once "utils.inc.php";
require_once "login.inc.php";
require_once "mc.inc.php";

/**
 * Authorizes and grants consent to the requested external user for the given
 * account.  
 *
 * @param accid - account id 
 * @param password - password
 */
class authenticateWs extends jsonrestws {
    
    var $ALLOWED_ACCOUNT_STATES = array('USER','SPONSORED');
    
	function jsonbody() {

    $mcid = clean_mcid(req('accid'));
    $pwd = req('pwd');

    if(!$pwd)
      return $this->error("Password not provided");

    if(!is_valid_mcid($mcid,true))
      return $this->error("Account id not in correct format");

    $sha1 = User::compute_password($mcid,$pwd);

    dbg("mcid = $mcid sha1 = $sha1");

    $db = DB::get();
    $users = $db->query("SELECT u.mcid, u.acctype FROM users u WHERE u.mcid = ? AND u.sha1 = ?", array($mcid, $sha1));

    if($users === false)
      return $this->error("unable to query users");

    $result = new stdClass;
    if(count($users)!==1) {
      $result->status = "invalid";
    }
    else
    if(array_search($users[0]->acctype,$this->ALLOWED_ACCOUNT_STATES)===FALSE) {
      dbg("User credentials authenticated but account has non-allowed state ".$users[0]->acctype);
      $result->status = "invalid account type";
    }
    else {
      $result->status = "valid";
      $result->token = get_authentication_token(array($mcid)) ;
      $result->mcid = $mcid;
    }
    return $result;
  }
}

$x = new authenticateWs();
$x->handlews("response_authenticate");
?>
