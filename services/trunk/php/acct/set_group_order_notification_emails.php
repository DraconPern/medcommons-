<?
/**
 * Service for setting the emails to which order notifications go
 */
require_once "alib.inc.php";
require_once "JSON.php";
require_once "DB.inc.php";
require_once "utils.inc.php";

nocache();

$result = new stdClass;
try {
  $user = get_validated_account_info();
  if(!$user)
    throw new Exception("Must be logged in");
  
  if(!req('enc'))
    throw new Exception("Missing expected parameter 'enc'");
    
  validate_query_string();
  
  $emails = req('emails');
  if(!$emails)
    throw new Exception("Missing expected parameter 'emails'");
  
  // Emails must not be more than 255 chars
  if(strlen($emails)>=255) 
    throw new ValidationFailure("Total length of group order notification field cannot be more than 255 characters");
    
  // Emails must be valid
  foreach(explode(",",$emails) as $e) {
      if(!check_email_address(trim($e))) 
        throw new ValidationFailure("Email address $e is not valid");
  }
   
  // Get user's current practice / group
  if(!$user->practice) 
    throw new Exception("You do not currently have an active group");

  $db = DB::get();
  
  dbg("Updating order notification emails for group ".$user->practice->practicename." to ".$emails);
  
  $db->execute("update groupinstances set upload_notification = ? where accid = ?", array($emails, $user->practice->accid));
  
  $result->status = "ok";
}
catch(ValidationFailure $e) {
  $result->status = "validation failed";
  $result->error = $e->getMessage();
  error_log("Validation Failure:  ".$e->getMessage());
}
catch(Exception $e) {
  $result->status = "failed";
  $result->error = $e->getMessage();
  error_log("Update group order emails failed: ".$e->getMessage());
}
$json = new Services_JSON();
echo $json->encode($result);
?>


