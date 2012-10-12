<?
require_once 'healthbook.inc.php';
error_log("foo");

try {

  // *** basic start for all medcommons facebook programs
  list ($facebook,$user) =fbinit(false); 

  // *** end of basic start
  list ($u,$t) =mustloadtarget($facebook,$user);


  $email = req('email');
  $phoneNumber = req('phoneNumber');
  $carrier = req('carrier');
  $firstName = req('firstName');
  $lastName = req('lastName');

  dbg("Sharing account of {$t->mcid} to phone number {$phoneNumber} on carrier {$carrier}");

  $api = $t->getOAuthAPI();
  $result = $api->share_phr_to_phone($t->mcid, $phoneNumber, $carrier, $firstName, $lastName);

  dbg("successfully shared phr to phone number using access code {$result->accessCode}");
}
catch(Exception $e) {
  error_log("Failed to share account {$t->mcid} to phoneNumber {$phoneNumber}: ". $e->getMessage());
  $error = hsc($e->getMessage());
  echo "<fb:title>Error Occurred</fb:title>
       <p>We were unable to share the requested health records. The following error information was reported:</p>
       <p style='color: red;'>$error</p>
       <p>Please try again later or contact support for help.</p>";

  // Hmm, do we handle errors better?
  // eg. Account 9280779153917857 does not have a Current CCR
  exit;
}
?>
<p><b>Successfully shared!</b></p>
<p>The access code for this transaction is <?=$result->accessCode?> </p>
<p><a href='#' onclick='share_hide();'>Share Again</a></p>
