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
	
	// ss: if they have a truly custom form, use that
	if($g->upload_form) 
	    return $g->upload_form;
	else
	    return gpath("Orders_Url")."/order?groupAccountId=".urlencode($g->accid)."&patientId=".urlencode("N/A");
	 
	// no custom form, use whatever built in template they have been assigned
	/*
    $ROOTUPLOAD = $ROOT.'/acct/putils/';
    $UploadHandler = $ROOTUPLOAD.'uh.php';
	$GroupUploadURL = $ROOT.$g->accid.'/upload';
	$arg = base64_encode ($g->name.'|'.$g->name.'|'.$g->accid.'|'
	.$GroupUploadURL.'|'.$g->logo_url.'|'.$UploadHandler);
	
    if ($g->grouptypeid	!=0) 
    	$UploadForm = $ROOTUPLOAD."uploader1.php"; else $UploadForm = $ROOTUPLOAD."uploader0.php";
    	
	return "$UploadForm?a=$arg";
	*/
}

/**
 * Return the correct group context for the page based on the 'g' 
 * parameter in the request, and also the whole list of groups
 * that the logged in user is a member of.
 */
function queryGroups($info) {
    $db = DB::get();
    $groups = $db->query("select gi.groupinstanceid, gi.accid,gi.name, gi.parentid,gi.logo_url,gi.grouptypeid, gm.comment, gi.upload_form 
                          from groupinstances gi, groupmembers gm
                          where gm.memberaccid = ? and gm.groupinstanceid = gi.groupinstanceid",
                      array($info->accid));
                      
                      
    $groupId = req('g',$info->practice?$info->practice->providergroupid:-1);
    foreach($groups as $g) {
        if($g->groupinstanceid == $groupId) {
            $group = $g;
            break;
        }
    }
    
    if(!isset($group)) 
        $group = false;
        
    return array($group, $groups);
}

////////////////////////////////////  Main Code Starts Here ////////////////////////////////////

nocache();

$info = get_validated_account_info();

if(!$info) 
    header("Location: login.php?next=index.php");

// Defaults	                  
// $DEFAULT_LOGO_URL = '/images/logoHeader.gif';
$group = false;	                  
$uploadURL = false;

list($group,$groups) = queryGroups($info);

$statuses = array(
    "DDL_ORDER_UPLOAD_COMPLETE"=>"Uploaded",
    "DDL_ORDER_NEW"=>"Pending",
    "DDL_ORDER_XMITING"=>"Uploading",
    "DDL_ORDER_DOWNLOAD_COMPLETE"=>"Downloaded",
    "DDL_ORDER_ERROR"=>"Error",
    "DDL_ORDER_CANCELLED"=>"Cancelled"
);

// Set if the user is navigating back here after performing an upload
// Allows us to highlight the upload, show message, etc.
$upload = post('upload');

$tpl = req("yui")?"inbox_yui.tpl.php":"inbox.tpl.php";
if($group) {
    
    $inbox = new PatientList($group->parentid, "inbox");
    $inboxPatients = $inbox->patients();
    
    $members = new PatientList($group->parentid, "members");
    
    echo template($tpl)
            ->set("info", $info)
            ->set("members", $members->patients())
            ->set("groups", $groups)
            ->set("group", $group)
            ->set("ROOT", $ROOT)
            ->set("inbox", $inboxPatients)
            ->set("uploadUrl", getUploadUrl($group))
            ->set("blueUrl", $ROOT.'/router/blue?auth='.$info->auth)
            ->set("statuses", $statuses)
            ->set("upload", $upload)
            ->fetch();
}
else {
    echo template($tpl)
            ->set("info", $info)
            ->set("groups", $groups)
            ->set("ROOT", $ROOT)
            ->fetch();
}

dbg("index.php took ".(microtime(true) - $start)." seconds");
?>