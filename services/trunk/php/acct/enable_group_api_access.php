<?
/**
 * Service for enabling / disabling API keys
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
  
  if(!post('enc'))
    throw new Exception("Missing expected parameter 'enc'");
    
  validate_query_string();
  
  $enabled = (post('enable','false') == 'true');
   
  // Get user's current practice / group
  if(!$user->practice) 
    throw new Exception("You do not currently have an active group");

  $db = DB::get();
  if($enabled) {
      
      // If they are already enabled then just return the keys
      $keys = query_group_api_keys($user->practice->accid);
      if(!$keys) {
          // No keys - add them
          dbg("Adding new api keys for group {$user->practice->accid}");
          
          $keys = new stdClass;
          $keys->consumerToken = sha1($user->practice->accid.microtime(true).mt_rand(0,10000).time());
          sleep(1);
          
          // Unix/Linux platform? - use a better random generator
          $pr_bits = '';
          $fp = @fopen('/dev/urandom','rb');
          if($fp !== FALSE) {
            $pr_bits .= @fread($fp,16);
            @fclose($fp);
          }
          
          global $secret;
          $keys->consumerSecret = hash_hmac('SHA1', $user->practice->accid.time(), $secret.rand(0,10000));
          
          dbg("Creating new api tokens: {$keys->consumerToken} / {$keys->consumerSecret}");
          
          $atId = $db->execute("insert into authentication_token (at_id, at_token, at_secret, at_account_id, at_priority)
                                values (NULL,?,?,?,'G')",
	                            array($keys->consumerToken, $keys->consumerSecret, $user->practice->accid));
          $db->execute("update authentication_token set at_parent_at_id = ? where at_id = ?", array($atId, $atId));
          
		  $result->keys = $keys;
      }
      else {
        $result->keys = new stdClass;
        $result->keys->consumerToken = $keys->at_token;
        $result->keys->consumerSecret = $keys->at_secret;
      }
  }
  else {
      $db->execute("delete from authentication_token where at_account_id = ? and at_parent_at_id = at_id", array($user->practice->accid));
  }
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


