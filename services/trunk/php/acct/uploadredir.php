<?
/**
 * Redirect to the upload form for a group.
 * <p>
 * In most cases this is just a straight passthrough to dod.php, however
 * it is possible for groups to have custom upload forms, in which case we 
 * use the template specified in the groupinstances table.
 */
$_GLOBALS['no_session_check']=true;

require_once "utils.inc.php";
require_once "DB.inc.php";

$accid = req('accid');
if(!$accid) {
    header("Location: dod.php");
    exit;
}

$db = DB::get();

$group = $db->first_row("select upload_form from groupinstances where accid = ? and upload_form is not null", array($accid));
if($group) 
    header("Location: {$group->upload_form}");
else
    header("Location: /acct/dod.php?accid=".urlencode($accid));
?>
