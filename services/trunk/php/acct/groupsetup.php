<?

$_GLOBALS['no_session_check']=true;

require_once "template.inc.php";
require_once "utils.inc.php";
require_once "verify.inc.php";
require_once "login.inc.php";
require_once "verify.inc.php";
require_once "alib.inc.php";
require_once "DB.inc.php";
require_once "../mod/modpay.inc.php";


$t = template("groupsetup.tpl.php");

if((!isset($_POST['submitButton']) && !isset($_GET['cancel']))) {
    Template::$fields->dicomIpAddress = 'localhost'; 
    Template::$fields->dicomPort = 11112;
    echo $t->fetch();
    exit;
}

$email = verify('email');

if(!is_valid_email($email))    
    Template::$errors->email = 'Email address is not valid';
    
$practiceName = verify('practiceName','Practice Name');

$pw1 = $pw2 = $fn = $ln = false;
if(!post("is_existing_account")) {
    $fn = verify("fn","First Name");
    $ln = verify("ln","Last Name");
    $pw1 = verify("pw1", "Password");
    $pw2 = verify("pw2", "Password");
}
else {
    // This is so that the field values get "registered" for redisplay
    post("fn");
    post("ln");
}

if($pw1 && $pw2 && $pw1 != $pw2) {
    Template::$errors->pw1 = "Passwords did not match";
}

$aeTitle = post('dicomAeTitle');
$ipAddress = post('dicomIpAddress');
$port = post('dicomPort');

$enableDicom = post('enableDicomSettings');
if($enableDicom) {
    verify('dicomAeTitle','AETITLE');
    
    // Used to limit this to *actual* ip addresses, but Adrian asked that
    // we accept things like 'localhost'
    verify('dicomIpAddress','Ip Address',"/^[0-9A-Za-z-\\.]{1,255}$/");
    verify('dicomPort','Port',"/^[0-9]{1,5}$/");
}

// Invitation emails - for now we don't verify them
$inviteEmails = post('inviteEmails');
$inviteEmails = $inviteEmails ? explode(",",$inviteEmails) : array();

if(!post('termsOfUse')) {
    Template::$errors->termsOfUse = 'Please confirm you have read and accept the Terms of Use';
}

if(Template::has_errors()) {
    if(isset($_GET['cancel']))
        Template::$errors = new stdClass;
        
    echo $t->fetch();
    exit;
}

$db = DB::get();

$existing_mcid = $db->first_column("select mcid from users where email = ? and acctype = 'USER'",array($email));
$is_new_user = ($existing_mcid == null);
if($is_new_user) {
    // Register the user
    $mcid = User::insert($fn, $ln, $pw1, true);
    $user = User::load($mcid);
}
else {
    
    // Is this the confirmation screen?
    if(!post('pw')) {
        echo $t->set('confirm_email',true)
               ->fetch("group_setup_existing_account.tpl.php");
        exit;
    }
    
    // Has user confirmed?
    $pw = post('pw');

    if(isfrozen($email))
            throw new ValidationFailure('Too many login attempts on this account.  Account frozen for 5 minutes.');
    
    if(User::compute_password($existing_mcid,$pw) != $db->first_column("select sha1 from users where mcid = ?",array($existing_mcid))) {
        // sleep(5); // Wait .... slow down attempts to brute force passwords
        
        track_login_failure($email);
        
        Template::$errors->pw = 'Incorrect password.  Please try again.';
        
        // User has not been shown or not checked 'yes' on confirmation screen
        echo $t->set("msg","Passwords did not match.  Please try again.")
               ->fetch("group_setup_existing_account.tpl.php");
        exit;
    }
    remove_trakking($email);
    $user = User::load($existing_mcid);
}

$info = $user->get_info();

// Add the dicom settings
if($enableDicom) {
    $db->execute("insert into users_dicom_settings 
                    (uds_id, uds_accid, uds_aetitle, uds_host, uds_port)
                values
                    (NULL, ?, ?, ?, ?)", array($mcid, $aeTitle, $ipAddress, $port));
}

// Since the email address may not be verified yet we set it here so that the group
// creation code will get the email address for the purposes of setting default notifications.
// This should be safe even if the user does not own the email address because it is a completely
// new group and even they will not have access to it unless they verify the email address 
$info->email = $email;

// Create the group for them
list($groupAccountId, $rlsUrl) = create_group($info, $practiceName);

// Enable MOD
enable_mod($info, true);

// Send verification email
if($is_new_user)
    verify_new_email($mcid, $email);

// Set user's active group to the new one
$db->execute("update users set active_group_accid = ? where mcid = ?",array($groupAccountId, $info->accid));
    
// Register other users, create group invites
foreach($inviteEmails as $inv) {
    // If the user doesn't exist, create it
    $mcid = $db->first_column("select mcid from users where email = ? order by since limit 1",array($inv));
    if(!$mcid)
        $mcid = User::insert(null,null,null,false,"PENDING_INVITE", $inv, $groupAccountId);
        
    $db->execute("insert into users_group_invite (ugi_id, ugi_accid, ugi_group_accid) values (NULL,?,?)",
                  array($mcid, $groupAccountId));
}
 
dbg("all good - success!");

$user->login("home.php");
?>