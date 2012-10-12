<?
/**
 * Service for sending invitation emails to new group members
 */
require_once "alib.inc.php";
require_once "JSON.php";
require_once "login.inc.php";
require_once "email.inc.php";
require_once "urls.inc.php";
require_once "template.inc.php";
require_once "DB.inc.php";

global $acCommonName, $acApplianceName, $acDomain, $Secure_Url;
global $URL, $NS, $SECRET;

nocache();

$result = new stdClass;
try {
  $user = get_validated_account_info();
  
  $db = DB::get();
  $db->begin_tx();

  if(!$user)
    throw new Exception("Must be logged in");

  validate_query_string();

  $emails = req('emails');
  if(!$emails)
    throw new Exception("Required parameter emails not provided");

  $emails = explode(',',$emails);

  foreach($emails as $email) {
      if(!is_email_address($email))
          throw new Exception("Email address '$email' is not a valid address");
  }

  // Get user's current practice / group
  if(!$user->practice) 
    throw new Exception("You are not currently a member of a group");

  $groupId = $user->practice->providergroupid;
  $accids = array();
  foreach($emails as $email) {
      
      // If the user doesn't exist, create it
      $accid = $db->first_column("select mcid from users where email = ? order by since limit 1",array($email));
      if(!$accid) {
          dbg("Invited user is new user - inserting");
          $accid = User::insert(null,null,null,false,"PENDING_INVITE", $email, $user->practice->accid);
      }
      else
          dbg("Invited user is existing user - using existing users record");
      
      $db->execute("insert into users_group_invite (ugi_id, ugi_accid, ugi_group_accid) values (NULL,?,?)",
                   array($accid, $user->practice->accid));
          
      $accids[]= $accid;
  }

  invite_group_users($user, $accids, $emails, $user->practice->accid);

  $db->commit();
  dbg("Successfully invited all users");
  $result->status = "ok";
}
catch(Exception $e) {
  $db->rollback();
  $result->status = "failed";
  $result->error = $e->getMessage();
  $loginRequired = false;
}
$json = new Services_JSON();
echo $json->encode($result);
?>


