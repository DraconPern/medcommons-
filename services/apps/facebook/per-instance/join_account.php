<?
/**
 * Handles callback from OAuth authorization, joining the user's
 * account to the connected account.
 *
 * This page is a return callback from the appliance after the
 * appliance OAuth procedure is invoked (see authorize_join.php).
 *
 * $Id: join_account.php 5031 2008-03-12 04:13:02Z ssadedin $
 */
require_once 'healthbook.inc.php';
require_once 'mc_oauth_client.php';
require_once 'utils.inc.php';
require_once 'fbuser.inc.php';

global $oauth_consumer_key, $oauth_consumer_secret;
$app_url = $GLOBALS['facebook_application_url'];
$appname = $GLOBALS['healthbook_application_name'];

try {

  // *** basic start for all medcommons facebook programs
  list ($facebook,$user) =fbinit(); 
  // *** end of basic start

  // Get and validate parameters
  $fbid = req('fbid');
  if(!$fbid)
    throw new Exception("parameter fbid is required");

  if($fbid !== $user)
    throw new Exception("incorrect facebook user $fbid != ".$facebook->get_loggedin_user());

  $hurl = req('hurl');
  if(!$hurl)
    throw new Exception("parameter hurl is required");

  $u = mustload($facebook,$fbid);

  if(!$u->token)
    throw new Exception("Request token not initialised on fbid=".$fbid);

  // Exchange request token for true access token
  $api = ApplianceApi::confirm_authorization($oauth_consumer_key, $oauth_consumer_secret, $u->token, $u->secret, $hurl);

  if(preg_match("/^[a-z0-9]{40}$/", $api->access_token->key) !== 1)
    throw new Exception("Invalid access token {$api->access_token->key} returned from confirmation");

  dbg("got access token $api->access_token->key");

  list($applianceUrl, $mcid) = ApplianceApi::parse_health_url($hurl);

  // Update user's table with access key **** NEEDS HEAVY UPGRADE TO NEW FORM OF USER RECOD
  // if(!mysql_query("REPLACE INTO users (fbid,mcid,applianceurl,sponsorfbid,familyfbid,targetmcid,groupid,oauth_token,oauth_secret,storage_account_claimed)
  //  VALUES ('$fbid','$mcid','".mysql_real_escape_string($appliance_url)."','$fbid','$fbid','$mcid', NULL, 
  //          '{$api->access_token->key}','{$api->access_token->secret}',1)"))
  //  throw new Exception("error inserting into users: ".mysql_error());

  // Create a patient using the contents of the healthurl
  // Get the current ccr
  $ccr = $api->get_ccr($mcid);

  $patientActorId = $ccr->patient->actorID;

  dbg("patient actor id = $patientActorId");

  foreach($ccr->actors->actor as $a) {
    if($a->actorObjectID == $patientActorId) {
      dbg("Found patient");
      $p = $a;
    }
  }

  // Figure out the patient
  $firstName = "Unknown";
  $lastName = "Unknown";
  $sex = "Unknown";
  if(isset($p)) {
    dbg("setting patient details");
    $firstName = $p->person->name->currentName->given;
    $lastName = $p->person->name->currentName->family;
    $sex = $p->person->gender->text;
  }

  $auth = $api->access_token->key;
  $secret = $api->access_token->secret;
  $photoUrl = ''; // note: can't be null
  $ghUrl = ''; // note: can't be null
  $hvUrl = ''; // note: can't be null
  $now = time();

  // Insert to patients table
  $rep="REPLACE INTO patients 
       (mcid,applianceurl,sponsorfbid,familyfbid, 
        groupid,oauth_token,oauth_secret,firstname,lastname,sex,photoURL,ghUrl,hvUrl)
        VALUES 
        ('$mcid','$applianceUrl','$fbid','$fbid',
         NULL,'$auth','$secret','$firstName','$lastName','$sex','$photoUrl',
         '$ghUrl','$hvUrl')";

  dbg($rep);
  mysql_query($rep) or die("error inserting into patients: ".mysql_error());

	$q = "REPLACE INTO carewalls set wallmcid = '$mcid', wallfbid='$fbid',authorfbid='$fbid',msg='I created this account for $firstName $lastName',time=$now ";
	mysql_query($q) or die ("Cant $q");

  // Send email to ops
  opsMailBody(  "$appname: facebook user $fbid {$u->getFirstName()} {$u->getLastName()} joined medcommons acc $mcid",
  "<html><h4>MedCommons Account $mcid was connected  by facebook user {$u->getFirstName()} {$u->getLastName()}</h4>
    <ul>
      <li>You can access the createor's facebook profile at <a href='http://www.facebook.com/profile.php?id=$fbid'>http://www.facebook.com/profile.php?id=$fbid</li>
      <li>You can attempt to access the user's healthurl at <a href='$hurl'>$hurl</a></li>
    </ul>
  </body></html>"
  );

  // Send notice to user
  // $fbml = "<br/>Your $appname Account was joined to a MedCommons HealthURL: <a class=applink href='$hurl' >$hurl</a>    ";
  // $facebook->api_client->notifications_send($fbid, "$fbml",  );

  $email = 
"Your $appname account on Facebook was connected to a HealthURL.
<br/>
<br/>
You can visit your HealthURL outside of Facebook any time by using the 
following link:
<br/>
<br/>
<a href='$hurl'>$hurl</a>
<br/>";

  $txt = strip_tags(str_replace("<br>", "\n", $email));

  // Send notification email to user
  $facebook->api_client->notifications_sendEmail($fbid,"Your Facebook Account has been connected to a HealthURL",$txt, $email);
}
catch(Exception $e) {
  error_log("Error while exchanging request token for healthurl $hurl:".$e->getMessage());
  die("<p>Apologies, your account could not be connected due to an internal system error.</p>
       <p>Please try again another time.</p>");
}
?>
<fb:fbml version='1.1'>
<?=dashboard($fbid)?>
  <fb:success>
    <fb:message><?=htmlentities($firstName).' '.htmlentities($lastName)?> was added to your Family.</fb:message>
     We added <?=htmlentities($firstName).' '.htmlentities($lastName)?> to your family as an elder.
     You can now begin caring for this person using <?=htmlentities($appname)?>.
  </fb:success>
</fb:fbml>
