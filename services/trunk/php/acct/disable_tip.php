<?
$_GLOBALS['no_session_check']=true;
/**
 * Toggles whether a tip is shown for a particular user or not 
 */
require_once "DB.inc.php";
require_once "mc.inc.php";
require_once "alib.inc.php";
require_once "utils.inc.php";
require_once "JSON.php";

$json = new Services_JSON();
$result = new stdClass;
nocache();
try {
    
    // Removed because gw needs to call this
    // validate_query_string();
    
    $info = get_validated_account_info();    
    if(!$info)
        throw new Exception("Must be logged in to access this function");
    
    $tip = (int)req('tip');
    if(!is_integer($tip))
        throw new Exception("Must supply valid tip to disable");
    
    $db = DB::get();    
    $tips = (int)$db->first_column("select tip_state from users where mcid = ?", array($info->accid));
    
    $enabled = req('enabled', 'false');
    if($enabled === 'true')
        $newtips = $tips | $tip;
    else
        $newtips = $tips ^ $tip;
    
    dbg("Changing tips for user {$info->accid} from $tips to $newtips");
    
    $db->execute("update users set tip_state = ? where mcid = ?", array($newtips, $info->accid));   
    $result->status = "ok";
}
catch(Exception $ex) {
    $result->status = "failed";
    $result->error = $ex->getMessage();
}

echo $json->encode($result);
?>
