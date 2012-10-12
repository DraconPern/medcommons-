<?php
/**
 * YAHOO Gadget
 */

require_once "utils.inc.php";
require_once "template.inc.php";
require_once "gadgets.inc.php";
require_once "DB.inc.php";
require_once "../acct/alib.inc.php";

nocache();

$ctx = GadgetContext::get();

$layout = template($ctx->get_layout())
             ->set("userid",$ctx->userid)
             ->set("ctx", $ctx);

// Look for user in ids table
if(!$ctx->mcid) {
    echo $layout->set("content",$layout->fetch("gadget_login.tpl.php"))->fetch(); 
    exit;
}

$db = DB::get();

$group = $db->first_row("select gi.* from groupinstances gi, users u where u.active_group_accid = gi.accid and u.mcid = ?",array($ctx->mcid));
if($group) {
    $_REQUEST['pid'] = $group->parentid;
    $_REQUEST['limit'] = 5;
    $_GET['fmt'] = "gadget";
    
    $rls_template = template("gadget_patient_list.tpl.php")->set("group",$group);
    
    $no_login_necessary = true;
    $auth = $ctx->auth;
    
    ob_start();
    include("../acct/rls.php");
    $txt = ob_get_contents();
    ob_end_clean();
    $layout->set("group",$group);
}
else {
    $gw = allocate_gateway($ctx->mcid);
    $txt = get_url($gw."/CurrentCCRWidget.action?miniActivity&accid=".$ctx->mcid."&auth=".$ctx->auth);
}

echo $layout->set("content",$txt)
            ->fetch();
?>
