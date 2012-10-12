<?
/**
 * Service for updating the name of a group
 */
require_once "alib.inc.php";
require_once "JSON.php";
require_once "DB.inc.php";
require_once "login.inc.php";

nocache();

$result = new stdClass;
try {
  $user = get_validated_account_info();
  if(!$user)
    throw new Exception("Must be logged in");
  
  validate_query_string();
  
  $logoUrl = req('logoUrl',false);
  if($logoUrl === false)
    throw new Exception("Missing required parameter 'logoUrl'");
   
  // Get user's current practice / group
  if(!$user->practice) 
    throw new Exception("You do not currently have an active group");

  $groupId = $user->practice->providergroupid;

  // Update the logo url
  $db = DB::get();
  $db->execute("update groupinstances set logo_url = ? where groupinstanceid = ?",
                array($logoUrl, $groupId));

  $u = User::load($user->accid);
  $u->login();
  
  $result->status = "ok";
}
catch(Exception $e) {
  $result->status = "failed";
  $result->error = $e->getMessage();
  error_log("Update group logo failed: ".$e->getMessage());
}
$json = new Services_JSON();
echo $json->encode($result);
?>


