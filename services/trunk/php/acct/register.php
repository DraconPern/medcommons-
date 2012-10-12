<?php

require_once 'template.inc.php';
require_once 'login.inc.php';
require_once 'settings.php';
require_once 'verify.inc.php';
require_once 'utils.inc.php';
require_once 'alib.inc.php';
require_once 'patientlist.inc.php';
require_once '../mod/modpay.inc.php';

nocache();

session_start();

/*
if (! $acOnlineRegistration) {
  $t = template($acTemplateFolder . 'login.tpl.php');
  $t->set('acOnlineRegistration', $acOnlineRegistration);
  $t->set('mcid', '');
  $t->set('error', 'Only sponsored registrations are currently supported');
  echo $t->fetch();
  exit;
 }
*/

/**
 * Activate the given MedCommons account using the specified
 * activation key.
 */
function activateAccount($mcid, $activationKey) {
  dbg('Activating account '.$mcid.' using key '.$activationKey);
  global $IDENTITY_WS_URL, $IDENTITY_WS_NS;
  $client = new SoapClient(null, array('location' => $IDENTITY_WS_URL, 'uri' => $IDENTITY_WS_NS));
  return $client->activate($mcid, $activationKey, "");
}

/**
 * Check if the config value indicating that this is a 
 * public appliance is set and if so, enable the account
 * for public access.
 *
 * @return - true if successful, false otherwise
 */
function check_enable_public_access($mcid) {
  global $acPublicAppliance;
  if(isset($acPublicAppliance) && ($acPublicAppliance=='true')) { // It's a public appliance!
    dbg("Enabling public access for account $mcid");
    $result = file_get_contents(gpath('Commons_Url')."/ws/grantAccountAccess.php?accessBy=0000000000000000&accessTo=".$mcid."&rights=R");
    if(preg_match("%<status>ok</status>%",$result)===false) {
      dbg("Failed to enable public access for account $mcid: ".$result);
      return false;
    }
  }
  return true;
}

function update_user($mcid, $fn, $ln, $pw1) {
    global $IDENTITY_PDO, $IDENTITY_USER, $IDENTITY_PASS, $DB_SETTINGS;
    
    $sha1 = User::compute_password($mcid,$pw1);
    
    $db = new PDO($IDENTITY_PDO, $IDENTITY_USER, $IDENTITY_PASS, $DB_SETTINGS);

    $stmt = $db->prepare("UPDATE users ".
                         "set first_name = :fn, last_name = :ln,".
                         "sha1 = :sha1, acctype = 'USER' ".
                         "where mcid = :mcid");

    dbg("Setting password hash for mcid ".$mcid." = ".$sha1);
    if (!$stmt->execute(array("mcid" => $mcid, "fn" => $fn, "ln" => $ln, "sha1" => $sha1))) {
      $e = $stmt->errorInfo();
      throw new Exception("Unable to update user: ".$e[2]);
    }
}

$MIN_PW_LEN = 6;

if (isset($_GET['layout']) && $_GET['layout'] == "none") {
  $t = template('register.tpl.php');
  $layout = template('widget.tpl.php')->nest("content", $t)->set("title", "Register a New Account");
}
else {
  $t = template('register.tpl.php');
  $layout = template('base.tpl.php')->nest("content", $t)->set("title", "Register a New Account");
}

if (isset($_POST['next']))
  $next = $_POST['next'];
else if (isset($_GET['next']))
  $next = $_GET['next'];
else
  $next = false;

$t->esc('next', $next);

$activationKey = req('ActivationKey',req('ak'));
if($activationKey) {
  $t->set('activationKey', $activationKey);
}

if(isset($_SESSION['reg_email'])) {
    $t->set('email',$_SESSION['reg_email']);
    $_GET['email'] = $_SESSION['reg_email'];
    $t->set('fixedEmail',true);
    $u = User::load($_SESSION['reg_accid']);
    $g = pdo_first_row('select * from groupinstances where accid = ?',array($u->active_group_accid));
    $t->set('reg_group',$g);
}

/*
 * if it's a complete POST request, must contain valid email and
 * matching passwords.  If valid, then *redirect* to correct login
 * page.  If not valid, display template with error inserts.
 */
if (count($_POST) == 0 || !isset($_POST['pw1'])) {
  if (isset($_GET['email']))
    $email = $_GET['email'];
  else
    $email = '';

  $t->esc('email', $email);
  $t->esc('ln', '');
  $t->esc('fn', '');

  echo $layout->fetch();
}
else {
    $email = trim($_POST['email']);
    $pw1 = $_POST['pw1'];
    $pw2 = post('pw2', false);
    $fn = post('fn');
    $ln = post('ln');
    $tou = isset($_POST['termsOfUse']) && $_POST['termsOfUse'];

    $errors = 0;

    /* validation */
    if ($tou!='on') {
        $errors++;
        $t->set('tou_error', 'Please confirm you agree with our Terms of Use');
    }

    if (!is_valid_email($email)) {
        $errors++;
        $t->set('email_error', 'Please enter a valid email address');
    }

    if (strlen($pw1) < $MIN_PW_LEN) {
        $errors++;
        $t->set('pw1_error', "Passwords must be at least $MIN_PW_LEN characters");
    }

    if (($pw2 !== false) && ($pw1 != $pw2)) {
        $errors++;
        $t->set('pw2_error', 'Passwords must match');
    }
    
    $db = DB::get();
    
    // Is the user already registered?  If so, send them back with complaints
    $existingEmail = $db->first_column("select email from users where email = ? and acctype <> 'PENDING_INVITE'", array($email));
    if($existingEmail)  {
        dbg("duplicate registration attempt for email: $email");
        $errors++;
        $t->set('dup_email_error', "The email you entered is already registered. 
                                    Did you mean to <a href='login.php?email=".urlencode($email)."'>Log In</a>?");
    }

    $t->esc('email', $email);
    $t->esc('ln', $ln);
    $t->esc('fn', $fn);
    
    

    if ($errors > 0) {
        echo $layout->fetch();
        exit;
    }
    

    /*
     * We have a validated email address, not yet confirmed,
     * and we have a valid password.  Let's do the register.
     */

    $is_pre_registered = (isset($_SESSION['reg_accid']) && $_SESSION['reg_accid']);

    if($is_pre_registered) {
        $mcid =  $_SESSION['reg_accid'];
        dbg("registering pre-allocated mcid $mcid");
    }
    else
        $mcid = false;

    /*
    $pk = openssl_pkey_new();
    $pkstr = '';
    openssl_pkey_export($pk, $pkstr, $pw1);

    $csr = openssl_csr_new(array("CN" => $mcid,
				 "O" => $acCommonName,
				 "GN" => $fn,
				 "SN" => $ln), $pk);

    $crt = openssl_csr_sign($csr, NULL, $pk, 365);

    $crtstr = '';
    openssl_x509_export($crt, $crtstr);

    openssl_pkey_free($pk);
    openssl_x509_free($crt);
     */

    $enableDOD = isset($_POST['dod']);
    if($is_pre_registered) {
        update_user($mcid, $fn, $ln, $pw1);
        $_SESSION['reg_accid'] = false;
    }
    else {
        $mcid = User::insert($fn, $ln, $pw1, $enableDOD);
    }

    // if activation key specified, activate the account
    if($activationKey) {
        activateAccount($mcid, $activationKey);
    }
    else {
        dbg("No activation key provided for account $mcid");
    }
    
    $info = new stdClass;
    $info->accid = $mcid;
    $user = get_full_account_info($info);
    $groupName = ($fn || $ln) ? "$fn $ln Group" : " ";
    list($groupAccId, $rlsUrl) = create_group($user, $groupName);
    
    // Add the sample patient to the group for people to play with
    $practice = $db->first_row("select * from practice where accid = ?", array($groupAccId));
    
    $demoAccId = "1013062431111407";
    $inbox = new PatientList($practice->practiceid, "inbox");
    $inbox->add($demoAccId);
    
    // Add to event stream
    $db->execute("INSERT INTO practiceccrevents (practiceid,PatientGivenName,PatientFamilyName,PatientIdentifier,PatientIdentifierSource,Guid,Purpose,SenderProviderId,ReceiverProviderId,DOB,CXPServerURL,CXPServerVendor,ViewerURL,Comment,CreationDateTime,ConfirmationCode,RegistrySecret,PatientSex,PatientAge,Status,ViewStatus)
                VALUES (?,'Sample','Patient',?,'Patient MedCommons Id','','','idp','idp','16 Jan 1968 05:00:00 GMT','','MedCommons','','3D Imaging Consult',?,'','','Female','','New','Visible')",
                array($practice->practiceid, $demoAccId, time()));
                
    // Add read-only consent
    $db->execute("INSERT INTO rights (rights_id,account_id,document_id,rights,creation_time,expiration_time,rights_time,storage_account_id) 
            VALUES (NULL,?,NULL,'R',CURRENT_TIMESTAMP,NULL,CURRENT_TIMESTAMP,?)", array($groupAccId, $demoAccId));
    

    /* redirect to new page */
    if(check_enable_public_access($mcid) !== true) {
        $t->esc('db_error', 'failed to enable public access for this account');
        echo $layout->fetch();
    }
    else {
        verify_new_email($mcid, $email, $next);
        $uemail = base64_encode($email);

        $user = User::load($mcid);
        $user->authToken = get_authentication_token($mcid);
        if($user->authToken === false)
	        echo $layout->fetch();
        else 
            header("Location: wait_confirm.php?v=1&next=".urlencode($next)."&accid=".$mcid."&email=".$uemail); // pass email to wait_confirm
    }
}

?>
