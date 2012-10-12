<?
/**
 * Connects a specified gadget user to the current logged in 
 * user.
 */
require_once "mc.inc.php";
require_once "utils.inc.php";
require_once "template.inc.php";
require_once "session.inc.php";
require_once "DB.inc.php";

global $Secure_Url;

nocache();

$qs = get_encrypted_query_string($_SERVER['QUERY_STRING']);
if(!$qs)
    throw new SystemFailure("Invalid request", "Encrypted query string ".$_SERVER['QUERY_STRING']." could not be verified");

parse_str($qs, $params);

$_GET['mc_gadget_ctx'] = $params['mc_gadget_ctx'];
    
require_once "gadgets.inc.php";
    
$userid = $params['userid'];
if(!is_safe_string($userid))
    throw new SystemFailure("Unexpected format for userid", "userid = ".$userid);

$idp = $params['idp'];
if(!is_safe_string($idp))
    throw new SystemFailure("Unexpected format for idp", "idp = ".$idp);
    
$ctx = GadgetContext::get();    
$info = get_account_info();

$db = DB::get();
$db->begin_tx();
try {
    $idpId = $db->first_column("select * from identity_providers where source_id = ?",array($idp));
    if(!$idpId) 
        $idpId = $db->execute("insert into identity_providers (id,source_id,name,display_login) values(NULL,?,?,?)",array($idp, $ctx->name,0));
    
    // Remove old mappings for the user, if there are any
    $db->execute("delete from external_users where mcid = ? and provider_id = ?",array($info->accid, $idpId));
    
    // Add the user to the table
    $db->execute("insert into external_users (mcid,provider_id,username) values (?,?,?)",array($info->accid, $idpId, $userid));
    $db->commit();
}
catch(Exception $ex) {
    $db->rollback();
    throw $ex;
}
$content = "<br/><br/><p style='margin-bottom: 1em;'>Congratulations, your MedCommons Account is now connected to {$ctx->name}!</p>

<p>You can unlink your account any time from the <a href='$Secure_Url/acct/settings.php?page=identities'>Identities</a> tab in your Account Settings.</p>
";
        
echo template("base.tpl.php")->set("content",$content)
                             ->set("title","Connected to $idp</p>")
                             ->fetch();
?>
