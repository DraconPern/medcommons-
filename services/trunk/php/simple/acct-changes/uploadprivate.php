<?php
  
  /*
   * Some notes: outer frame to upload privately
   */

require_once "DB.inc.php";
require_once "alib.inc.php";
require_once "template.inc.php";
require_once "patientlist.inc.php";

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



$info = get_validated_account_info();

if(!$info) 
    header("Location: login.php?next=/info.html"); // changed by bill on 11 aug 10

// Defaults	                  
$DEFAULT_LOGO_URL = '/images/logoHeader.gif';
$group = false;	                  
$uploadURL = false;

$t = template( 'uploadprivate.tpl.php');
if($info->practice) {
    list($group,$groups) = queryGroups($info);
    $t->set('group',$group);
}


if (isset($_REQUEST['next']))
  $t->esc('next', $_REQUEST['next']);

echo $t->fetch();

?>
