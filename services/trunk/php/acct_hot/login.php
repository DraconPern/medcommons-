<?php

$_GLOBALS['no_session_check']=true;

require_once 'settings.php';
require_once 'login.inc.php';
require_once 'mc.inc.php';
require_once 'OpenID.php';
require_once 'utils.inc.php';
require_once 'demodata_ids.inc.php';
require_once 'DB.inc.php';
require_once 'common.php';

function login($row) {
    global $next, $t, $openid_url;

    $mcid = $row->mcid;

    $token = get_authentication_token($mcid);

    info("Logged in ".$mcid." from ".$_SERVER['REMOTE_ADDR']." token ".substr($token,10)."...");

     //ssadedin: note we MUST remove the user's original input,
     //not whatever we found in the user record
    remove_trakking ($openid_url);//  donner -- dec 09 2009 -- if logged in successfully then delete from trakking table

    $user = User::load($mcid);
    $user->authToken = $token;
    

    if(!$next  && (trim($user->startparams)))
      $next = $user->startparams;    

    if ($row->acctype == 'CLAIMED')
        $user->login('claim.php?next=' . urlencode($next));
    else
        $user->login($next);
}
/*

function login($row) {
    global $next, $t, $openid_url;

    $mcid = $row->mcid;

    $token = get_authentication_token($mcid);

    
    // ssadedin: note we MUST remove the user's original input,
    // not whatever we found in the user record
    remove_trakking ($openid_url); // donner -- dec 09 2009 -- if logged in successfully then delete from trakking table

    $user = User::load($mcid);
    $user->authToken = $token;
    
      //   $info = get_account_info();
         $name = "groupName";//.$info->accid;
         
// set up another cookie to hold the current group name, the cookie is rewritten whenever the group is switched
$value = "groupName=".$name.",patientName=SamplePatient";
// send a cookie that expires in 24 hours
setcookie("mc_cursor",$value, time()+3600*24,'/');
  
warn("Logged on ".$mcid." from ".$_SERVER['REMOTE_ADDR']." cursor $value token ".substr($token,10)."...");

    if ($row->acctype == 'CLAIMED')
    $user->login('claim.php?next=' . urlencode($next));
    else
    {
        // if there's a next in the account record, USE THAT ! 2 14 10 BILL
     if (trim($user->startparams)!='') $user->login($user->startparams) ; else
    $user->login($next);
    }
}
*/
function handle_email($openid_url, $next, &$t)
{
    $t->esc('openid_url', $openid_url);
    if(isset($_POST['password'])) {
        $email = $openid_url;
        $password = $_POST['password'];
        $user = User::resolveEmail($email,$password);
        
        if($user) 
          login($user); // does not return

        warn("Login failed for user ".$openid_url. " from ".$_SERVER['REMOTE_ADDR']);

        $t->set('error', 'No such user/bad password');

        track_login_failure($openid_url); 
    }
}

/**
 * Redirects a user to a page authenticated by a phone number
 * and access code.
 */
function handle_phone_number($phoneNumber, $next, &$t) {

    $db = DB::get();
    
    $t->esc("openid_url",$phoneNumber);

    // If no password, allow to fall through to login page
    if(!isset($_POST['password']))
    return;

    $password = $_POST['password'];

    // Get phone free of unwanted separator characters (-, . space, etc.)
    $phoneNumber = preg_replace("/[ -\.]/", "", $phoneNumber);

    // Check against phone number table
    $matches = $db->query("select * from phone_authentication
                            where pa_phone_number = ?
                            and pa_access_code = ?", array($phoneNumber, $password));

    if(count($matches) === 0) {
        $t->set('error', 'No such user/bad password');
        return;
    }

    // Login as external identity
    $url = gpath('Commons_Url')."/ws/createAuthenticationToken.php?accountIds=tel:1".urlencode($phoneNumber);
    $json = get_url($url);
    $decoder = new Services_JSON();
    $result = $decoder->decode($json);
    if(!$result || ($result->status !== "ok"))
    error_page("Internal System Error creating Authentication Token",
                 "Invalid result returned when creating auth token url=$url result=$json");

    $token = $result->result;
    dbg("Got auth token $token for validated phone number $phoneNumber");

    remove_trakking ($phoneNumber); // donner -- 8 dec 2009 -- don't need to trak them once in

    // Set the anonymous auth cookie
    header('Set-Cookie: mc_anon_auth='.$token.'; path=/');

    dbg("next = $next");

    // If the user was on their way to a known HealthURL, just forward them
    if(preg_match("/.*\/acct\/cccrredir.php\?.*\&accid=[0-9]{16}.*$/",$next) === 0)
    $next  = gpath('Commons_Url')."/phoneredir.php";

    // In this case we send them over to /secure to look up the most recent
    // phone entry
    dbg("next = $next");
    redirect($next);
}
function handle_mcid($openid_url, $next, &$t)
{
    $db = DB::get();
    $mcid = clean_mcid($openid_url);

    /* hey! we're already logged in! */
    if ($mcid == get_login_mcid())
        redirect('/acct/index.php');

    $t->esc('openid_url', pretty_mcid($mcid));

    if (isset($_POST['password'])) {
        $password = $_POST['password'];

        $sha1 = User::compute_password($mcid, $password);

        $sql = <<<EOF
SELECT users.email, users.mcid,
       users.first_name, users.last_name, users.acctype
FROM   users
WHERE  users.mcid = :mcid AND
       users.sha1 = :sha1
EOF;

        $row = $db->first_row($sql, array("mcid" => $mcid, "sha1" => $sha1));
        if($row) {
            login($row);
        }
        warn("Login failed for user ".$mcid. " from ".$_SERVER['REMOTE_ADDR']);
        $t->set('error', 'No such user/bad password');
        track_login_failure($openid_url); // donner 8 dec 2009 -- might reset the error code
    }
}

function handle_openid($openid_url, $next, &$t) {

    dbg("Handling id $openid_url as openid");
    
    $db = DB::get();
    session_start();

    $t->esc('openid_url', $openid_url);
    $t->set('password', False);

    if(isset($_POST['idptype']) && ($_POST['idptype'] === 'otheridp')) {
        $mc_idp = 'otheridp';
        $mc_source_id = 'OpenID Provider';
    }
    else {
        $mc_idp = False;
        $mc_source_id = False;

        $result = $db->query("SELECT id, format, name FROM identity_providers");
        foreach($result as $row) {
            if (match_openid($openid_url, $row->format)) {
                /*
                 * Found the whitelisted identity provider
                 */
                $mc_idp = $row->id;
                $mc_source_id = $row->name;
                break;
            }
        }
    }

    $allow_anon_openid = false;
    if(isset($_POST['allow_anon_openid'])) {
        $_SESSION['mc_allow_anon_openid'] = $_POST['allow_anon_openid'];
        $allow_anon_openid = ($_POST['allow_anon_openid']=="true");
    }

    if(!$mc_idp && !$allow_anon_openid) {
        dbg("no matching idp found");
        $t->esc('error', 'OpenID provider not whitelisted on this system');
        echo $t->fetch('login.tpl.php');
        exit;
    }

    // Possible bug for load balanced environment
    $_SESSION['mc_next'] = $next;
    $_SESSION['mc_idp'] = $mc_idp;
    $_SESSION['mc_source_id'] = $mc_source_id;

    $scheme = 'http';
    if (isset($_SERVER['HTTPS']) and $_SERVER['HTTPS'] == 'on') {
        $defaultPort = 443;
        $scheme .= 's';
    }
    else {
        $defaultPort = 80;
    }

    $process_url = combine_urls(get_request_url(), "auth.php");
    $trust_root = $scheme . '://' . $_SERVER['SERVER_NAME'];

    if ($_SERVER['SERVER_PORT'] != $defaultPort)
    $trust_root .= ':' . $_SERVER['SERVER_PORT'];

    $trust_root .= dirname($_SERVER['PHP_SELF']);

    global $consumer;
    $auth_request = $consumer->begin($openid_url);
    if (!$auth_request) {
        $t->set('error', 'OpenID Authentication Error');
    }
    else {
        dbg("Redirecting using trust root $trust_root and process url $process_url");
        $redirect_url = $auth_request->redirectURL($trust_root, $process_url);

        dbg("Redirect url is $redirect_url");
        redirect($redirect_url);
    }
}

// starts here

nocache();

if (isset($_COOKIE['mc'])) { header("Location: ../info.html"); die("Redirecting from login because user is signed on"); }

$t = new Template();

$t->set('acOnlineRegistration', $acOnlineRegistration);

// Used for auto-accessing demo accounts
if(isset($_GET['access_accid'])) {
    $accessId = $_GET['access_accid'];
    dbg("got access id $accessId");
    if($accessId == $janesId) {
        dbg("accessId == jane");
        $_REQUEST['openid_url'] = "jhernandez@medcommons.net";
        $t->set('demo_password', "tester");
    }
}

if (isset($_POST['next']))
$next = $_POST['next'];
else if (isset($_GET['next']))
$next = $_GET['next'];
else
$next = '/acct/index.php';

if(isset($next) && (!$next || ($next ==="")))
$next = '/acct/index.php';

$t->esc('next', $next);
$t->set('password', False);

/*
 * if it's a complete POST request, must contain valid email and
 * matching passwords.  If valid, then *redirect* with cookie to
 * correct user page.  If not valid, display template with error
 * inserts.
 */

if (isset($_REQUEST['openid_url']))
$openid_url = trim($_REQUEST['openid_url']);
else if (isset($_REQUEST['mcid']))
$openid_url = trim($_REQUEST['mcid']);
else if (isset($_REQUEST['email']))
$openid_url = trim($_REQUEST['email']);
else
$openid_url = False;

$t->set('password', True);
if (!$openid_url)     $t->esc('openid_url', '');
else

// donner -- 8 dec 2009 -- regardless of what was entered, if we are still in the freeze period then reject this input right now
if (isfrozen($openid_url))  
    
    $t->set('error','Sorry, too many logins have been attempted on this account.  Please wait 5 minutes and try again.');
else
/* dispatch based on all forms of login */

if (is_openid_url($openid_url))
handle_openid($openid_url, $next, $t);

else if(is_phone_number($openid_url))
handle_phone_number($openid_url, $next, $t);

else if (is_valid_mcid($openid_url))
handle_mcid($openid_url, $next, $t);


else if (is_email_address($openid_url))
handle_email($openid_url, $next, $t);

else
$t->esc('openid_url', $openid_url); // donner -- 8 dec 2009 -- dont quite understand why this needs setting

if(req('prompt')) {
    $t->set('prompt',$t->fetch(req('prompt').".tpl.php"));
}

echo $t->fetch("login.tpl.php");

?>
