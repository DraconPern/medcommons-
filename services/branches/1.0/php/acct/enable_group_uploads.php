<?
/**
 * Service for updating the name of a group
 */
require_once "alib.inc.php";
require_once "JSON.php";

nocache();

$result = new stdClass;
try {
  $user = get_validated_account_info();
  if(!$user)
    throw new Exception("Must be logged in");

  $enable = req('enabled')=='true' ? 1 : 0;
   
  // Get user's current practice / group
  $practices = q_member_practices($user->accid);

  if(!$practices) 
    throw new Exception("You are not currently a member of a group");

  $groupId = $practices[0]->providergroupid;

  // Update the name
  pdo_execute("update groupinstances set enable_uploads = ? where groupinstanceid = ?",array($enable,$groupId));

  $result->status = "ok";
}
catch(Exception $e) {
  $result->status = "failed";
  $result->error = $e->getMessage();
  error_log("Update group name failed: ".$e->getMessage());
}
$json = new Services_JSON();
echo $json->encode($result);
?>


