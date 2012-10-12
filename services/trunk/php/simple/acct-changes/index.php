<?php

$start = microtime(true);

/**
 * Loads patients from a user's inbox and patient member roster 
 * and forwards to a template to display them (see inbox.tpl.php for 
 * default HTML rendering).
 */
require_once "DB.inc.php";
require_once "alib.inc.php";
require_once "template.inc.php";
require_once "patientlist.inc.php";

// The appliance root path
$ROOT = gpath('Secure_Url');

////////////////////////////////////  Support Functions ////////////////////////////////////

/**
 * Create a URL compatible with Bill's patented base64 url encoding scheme
 * that invokes the correct uploader page for the specified group,
 * based on grouptypeid.
 */
function getUploadUrl($g) {
	global $ROOT;
    $ROOTUPLOAD = $ROOT.'/acct/putils/';
    $UploadHandler = $ROOTUPLOAD.'uh.php';
	$GroupUploadURL = $ROOT.$g->accid.'/upload';
	$arg = base64_encode ($g->name.'|'.$g->name.'|'.$g->accid.'|'
	.$GroupUploadURL.'|'.$g->logo_url.'|'.$UploadHandler);
	
    if ($g->grouptypeid	!=0) 
    	$UploadForm = $ROOTUPLOAD."uploader1.php"; else $UploadForm = $ROOTUPLOAD."uploader0.php";
    	
	return "$UploadForm?a=$arg";
}

/**
 * Return the correct group context for the page based on the 'g' 
 * parameter in the request, and also the whole list of groups
 * that the logged in user is a member of.
 */
function queryGroups($info) {
    $db = DB::get();
    $groups = $db->query("select gi.groupinstanceid, gi.accid,gi.name, gi.parentid,gi.logo_url,gi.grouptypeid, gm.comment 
                          from groupinstances gi, groupmembers gm
                          where gm.memberaccid = ? and gm.groupinstanceid = gi.groupinstanceid",
                      array($info->accid));
                      
    $practice = $info->practice;                      
    $groupId = req('g',$practice->providergroupid); 
    foreach($groups as $g) {
        if($g->groupinstanceid == $groupId) {
            $group = $g;
            break;
        }
    }
    
    if(!isset($group)) 
        throw new Exception("Invalid group id");
        
    return array($group, $groups);
}

////////////////////////////////////  Main Code Starts Here ////////////////////////////////////

$info = get_validated_account_info();

if(!$info) 
    header("Location: login.php?next=/info.html"); // changed by bill on 11 aug 10

// Defaults	                  
$DEFAULT_LOGO_URL = '/images/logoHeader.gif';
$group = false;	                  
$uploadURL = false;

if($info->practice) {
    list($group,$groups) = queryGroups($info);
    if(!$group->logo_url)
        $group->logo_url = $DEFAULT_LOGO_URL;
}

$inbox = new PatientList($group->parentid, "inbox");
$inboxPatients = $inbox->patients();

$members = new PatientList($group->parentid, "members");

$tpl = req("yui")?"inbox_yui.tpl.php":"inbox.tpl.php";

echo template($tpl)
        ->set("info", $info)
        ->set("members", $members->patients())
        ->set("groups", $groups)
        ->set("group", $group)
        ->set("ROOT", $ROOT)
        ->set("inbox", $inboxPatients)
        ->set("uploadUrl", getUploadUrl($group))
        ->fetch();
dbg("index.php took ".(microtime(true) - $start)." seconds");
?>