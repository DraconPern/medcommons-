<?php

$_GLOBALS['no_session_check']=true;

require_once 'login.inc.php';
require_once 'urls.inc.php';
require_once 'email.inc.php';
require_once 'verify.inc.php';

require_once 'settings.php';
require_once 'skey.inc.php';

require_once 'DB.inc.php';
require_once 'utils.inc.php';
require_once 'mc.inc.php';
require_once 'alib.inc.php';



$db = DB::get();

if(!(isset($_GET['mcid']) && isset($_GET['hmac']) && isset($_GET['email'])))
    throw new SystemFailure("Bad verification link.  Please contact support for help.", "One of mcid, hmac or email was missing");

$mcid = clean_mcid($_GET['mcid']);
$email = $_GET['email'];
$hmac = hash_hmac('SHA1', $mcid . $email, $SECRET);
if ($hmac != $_GET['hmac'])
    throw new SystemFailure("Bad verification link.  Please contact support for help.", "Invalid hmac.  Got: ".$_GET['hmac'].", expected ".$hmac);

$row = $db->first_row("SELECT first_name, last_name, enc_skey, active_group_accid, email_verified " .
                      "FROM users WHERE mcid = ?", array($mcid));
$pretty_mcid = pretty_mcid($mcid);

$next = req('next');

global $acDomain;


if ($row->enc_skey) {  // Skeys exist, just display empty page
    $sql = "UPDATE users".
           " SET email = :email, email_verified = NOW()".
           " WHERE mcid = :mcid";

    $params = array("email" => $email, "mcid" => $mcid);

    $pendingUsers = array();
}
else { // no SKeys, create them and show to user
    $t = template($acTemplateFolder . 'receipt.tpl.php');

    $a = array();

    $seed = mcrypt_create_iv(8);

    $a = array();

    for ($i = 0; $i < 12; $i++) {
        $seed = skey_step($seed);
        array_push($a, skey_put($seed));
    }

    $seed = skey_step($seed);

    $sql = "UPDATE users".
           " SET email = :email, email_verified = NOW(), enc_skey = :skey".
           " WHERE mcid = :mcid";

    $params = array("email" => $email, "skey" => base64_encode($seed), "mcid" => $mcid);

    $pendingUsers = $db->query("select p.* from users p, users u, users_group_invite ugi
                                where p.mcid = ugi.ugi_accid
                                and ugi.ugi_group_accid = u.active_group_accid
                                and u.mcid = :mcid", array("mcid" => $mcid));


    $group = $db->first_row("select * from groupinstances where accid = ?", array($row->active_group_accid));

    $uploadUrl = get_upload_url($group);

    $t->set('skey', $a)
    ->set('first_name', $row->first_name)
    ->set('last_name', $row->last_name)
    ->set('email', $email)
    ->set('mcid', $pretty_mcid)
    ->set('uploadUrl', $uploadUrl)
    ->set('signInUrl', gpath("Secure_Url")."/acct/")
    ->set('domain',$acDomain);
    
    send_mc_email($email, "Thank you for registering with $acApplianceName",
    $t->fetch("receiptText.tpl.php"),
    $t->fetch("receipt.tpl.php"),
    array('logo' => get_logo_as_attachment()));
}

$db->begin_tx();
try {
    // Notify the global redirector of the mapping from this verified email to the mcid we
    // have now registered.
    get_url($acGlobalsRoot . 'login/register.php?name='. urlencode($email) . '&mcid=' . $mcid);

    $db->execute($sql,$params);

    if($pendingUsers) {

        $accids = array();
        $emails = array();
        $params = array();
        foreach($pendingUsers as $u) {
            dbg("Found user {$u->mcid} with invite pending on verification of account $mcid");
            $accids[]=$u->mcid;
            $emails[]=$u->email;
            $params[]="?";
        }
        $user = User::load($mcid);
        invite_group_users($user->get_info(), $accids, $emails, $user->active_group_accid);

        dbg("deleting ".count($accids)." pending user invites");
        $db->execute("delete from users_group_invite where ugi_group_accid = ? and ugi_accid in (".join(",",$params).")",
        array_merge(array($user->active_group_accid), $accids));
    }


    $db->commit();
}
catch(Exception $e) {
    $db->rollback();
    throw $e;
}

$user = User::load($mcid);

$url = "register_thankyou.tpl.php";
if($next)
    $url.="?next=".urlencode($next);
    
// Using the verify link shouldn't log you in again    
$info = get_validated_account_info();
if($row->email_verified && (!$info || ($info->email != $email))) 
    header("Location: login.php?prompt=already_verified&email=".urlencode($email)."&next=".urlencode($url));
else
    $user->login($url);  // First time bonus:  we log you in for free
?>
