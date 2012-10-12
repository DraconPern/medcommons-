<?php

require_once 'login.inc.php';
require_once 'urls.inc.php';
require_once 'verify.inc.php';

require_once 'settings.php';
require_once 'skey.inc.php';

require_once 'DB.inc.php';
require_once 'utils.inc.php';
require_once 'mc.inc.php';


$db = DB::get();

if(!(isset($_GET['mcid']) && isset($_GET['hmac']) && isset($_GET['email']))) 
    throw new SystemFailure("Bad verification link.  Please contact support for help.", "One of mcid, hmac or email was missing");
    
$mcid = clean_mcid($_GET['mcid']);
$email = $_GET['email'];
$hmac = hash_hmac('SHA1', $mcid . $email, $SECRET);
if ($hmac != $_GET['hmac']) 
    throw new SystemFailure("Bad verification link.  Please contact support for help.", "Invalid hmac.  Got: ".$_GET['hmac'].", expected ".$hmac);
    
$row = $db->first_row("SELECT first_name, last_name, enc_skey " .
                      "FROM users WHERE mcid = ?", array($mcid));
$pretty_mcid = pretty_mcid($mcid);

if ($row->enc_skey) {  // Skeys exist, just display empty page
    $t = template($acTemplateFolder . 'verified.tpl.php');

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
    
    // Check if this user is the first in a group which has PENDING_INVITE users
    // who should be invited now that this user is verified.
    /*
    $pendingUsers = $db->query("select p.* from users p, users u 
                                where p.acctype = 'PENDING_INVITE' 
                                and p.active_group_accid = u.active_group_accid
                                and u.mcid = :mcid
                                and p.active_group_accid is not NULL", array("mcid" => $mcid));
    */
    $pendingUsers = $db->query("select p.* from users p, users u, users_group_invite ugi
                                where p.mcid = ugi.ugi_accid
                                and ugi.ugi_group_accid = u.active_group_accid
                                and u.mcid = :mcid", array("mcid" => $mcid));
        
    $t->set('skey', $a)->esc('email',$email);
}

$db->begin_tx();
try {
    // Notify the global redirector of the mapping from this verified email to the mcid we
    // have now registered.
    get_url($acGlobalsRoot . 'login/register.php?name='. urlencode($email) . '&mcid=' . $mcid);
    
    $db->execute($sql,$params);
    
    if($pendingUsers) {
        
        require_once 'alib.inc.php';
        
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
    
    global $acDomain;
    
    $t->set('first_name', $row->first_name);
    $t->set('last_name', $row->last_name);
    $t->set('email', $email);
    $t->set('mcid', $pretty_mcid);
    $t->set('domain',$acDomain);
    
    $db->commit();
}
catch(Exception $e) {
    $db->rollback();
    throw $e;
}


    $text = <<<EOF
Welcome New MedCommons User
Login to:   https://www.medcommons.net/
Email:     ${email}
MCID:      ${pretty_mcid}
EOF;

    $html = <<<EOF
<html>
  <body>
    <h1>Welcome New MedCommons User</h1>
    <table>
      <tr>
        <th>Login</th>
        <td><a href='https://www.medcommons.net/'>https://www.medcommons.net/</a> </td>
      </tr>
      <tr>
        <th>Email</th>
        <td><a href='mailto:${email}'>${email}</a></td>
      </tr>
      <tr>
        <th>MCID</th>
        <td>${pretty_mcid}</td>
      </tr>
    </table>
  </body>
</html>
EOF;
send_mc_email($email, "Welcome New Medcommons User $email", $text, $html, 
             array('logo' => get_logo_as_attachment()));
//just redirect back to inbox, there's no point in putting up this page anymore
header("Location: /acct/index.php ");
//echo $t->fetch();
?>
