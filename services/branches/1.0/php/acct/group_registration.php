<?
/**
 * Verifies users link and creates session variables so that their registration
 * automatically uses the predetermined account id instead of creating a new one.
 */
require_once "JSON.php";
require_once "login.inc.php";
require_once "email.inc.php";
require_once "urls.inc.php";
require_once "mc.inc.php";
require_once "alib.inc.php";
require_once "template.inc.php";
require_once "settings.php";
require_once "DB.inc.php";

global $acCommonName, $acApplianceName, $acDomain, $Secure_Url;
global $URL, $NS, $SECRET;

nocache();

$result = new stdClass;

$email = req('email');
if(!$email)
  throw new Exception("Expected parameter 'email' not provided");

if(!is_email_address($email))
  throw new Exception("Bad format for parameter 'email'");

$accid = req('accid');
if(!$accid)
  throw new Exception("Expected parameter 'accid' not provided");

if(!is_valid_mcid($accid,true))
  throw new Exception("Bad format for parameter 'accid'");

$enc = req('enc');
if(!$enc || !is_safe_string($enc))
  throw new Exception("Missing or bad format for parameter 'enc'");

$params = "accid=$accid&email=".urlencode($email);
$hmac = hash_hmac('SHA1', $params, $SECRET);

if($hmac != $enc)
  throw new SystemFailure("Bad value for parameter 'enc'",
                          "User sent enc=$enc but calculated $hmac from parameters $params");
$db = DB::get();

// Is the user an existing, registered user?
$mcid = $db->first_column("select mcid from users where email = ? and acctype = 'USER' order by since limit 1",array($email));
if($mcid) {
    // Is the user already logged in? If not, make them log in and verify again
    $info = get_validated_account_info();
    if($info && ($info->accid === $mcid)) { // Logged in as invited user
        echo template("group_add_existing_user.tpl.php")->set("email",$email)->set("accid",$accid)->fetch();
    }
    else { 
        // Not logged in or logged in as different user ... send them through login and return back here
        $next = "group_registration.php?".$params."&enc=".$enc;
        header("Location: login.php?email=".urlencode($email)."&prompt=group_registration_login_prompt&next=".urlencode($next));
        exit;
    }
}
else { // No user registered with that email ... let them register it new
    session_start();
    $_SESSION['reg_accid'] = $accid;
    $_SESSION['reg_email'] = $email;
    header("Location: register.php");
}

?>