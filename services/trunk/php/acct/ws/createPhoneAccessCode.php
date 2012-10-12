<?php
header("Cache-Control: no-store, no-cache, must-revalidate");
header("Pragma: no-cache");
require_once "wslibdb.inc.php";
require_once "../alib.inc.php";
require_once "utils.inc.php";
require_once "email.inc.php";
require_once "mc.inc.php";
require_once "urls.inc.php";

$pennysmsurl = "http://api.pennysms.com/xmlrpc";

/**
 * Creates an access code that can be used to authenticate a
 * phone user, and sends the user an SMS indicating the
 * access code to use.
 *
 * @param phoneNumber   phone number to authenticate
 * @param carrier       carrier associated with phone number
 * @param accessTo      account id of user being accessed
 * @return access code
 */
class createPhoneAccessCode extends jsonrestws {
	
	function xpost ($content)
	{
		global $pennysmsurl;
		$headers  =  array( "Content-type: text/xml" );
		$ch = curl_init();
		curl_setopt($ch, CURLOPT_URL, $pennysmsurl );
		curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
		curl_setopt($ch, CURLOPT_TIMEOUT, 20);
		curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
		curl_setopt($ch, CURLOPT_POSTFIELDS, $content);

		$data = curl_exec($ch);

		if (curl_errno($ch)) error_exit( curl_error($ch));
		else curl_close($ch);

		return $data;
	}
	


function smspost ($from,$to,$message)
{
global $acPennySMSKey;

$xmlmessage = xmlentities($message);

   //$from = str_replace('+','plus',$from);
	$body = <<<MSG
<?xml version="1.0"?>
<methodCall>
<methodName>send</methodName>
  <params>
	<param>
	<value><string>$acPennySMSKey</string></value>
	</param>
	<param>
	<value><string>cso@medcommons.net</string></value>
	</param>
	<param>
	<value><string>$to</string></value>
	</param>
	<param>
	<value><string>$xmlmessage</string></value>
	</param>
  </params>
</methodCall>
MSG;
	// post out to penny sms and analyze what we got back
	$response = $this->xpost($body); // just pass back whatever actually happens
	
	return $response;

}

  function jsonbody() {

    global $Secure_Url, $acFromName, $acFromEmail;

    $phoneNumber = req('phoneNumber');
    if(preg_match("/^[0-9]{10}$/",$phoneNumber)!==1)
      throw new Exception("Bad value for parameter 'phoneNumber'");

    $accessTo = req('accessTo');
    if(!is_valid_mcid($accessTo, true))
      throw new Exception("Bad value for parameter 'accessTo'");

    // $carrier = req('carrier');
    $carrier = "att";

    pdo_begin_tx();

    try {

      // Check if already have an access code for this phone - if so, use that
      $codes = pdo_query("select * from phone_authentication where pa_phone_number = ?",
                         array($phoneNumber));

      // Need to decide what should really happen here.  I think it should 
      // probably deactivate old codes rather than keep sending the same 
      // code over and over.
      if(count($codes) !== 0) { // Access code already exists
        $accessCode = $codes[0]->pa_access_code;
      }
      else { // Does not exist, make a new one

        // Generate an access code
        $accessCode = "";
        for($i = 0; $i<6; $i++) {
          $accessCode .= rand(0, 9);
        }

        // Insert it
        pdo_execute("insert into phone_authentication (pa_id, pa_phone_number, pa_access_code)
                     values (NULL,?,?)", array($phoneNumber,$accessCode));
      }

      // Send email
      // $emailAddress = "ssadedin@gmail.com";

      if($carrier == "att")
        $emailAddress = $phoneNumber."@txt.att.net";
      else
      if($carrier == "vrzn")
        $emailAddress = $phoneNumber."@vtext.com";
      else
      if($carrier == "sprintpcs")
        $emailAddress = $phoneNumber."@messaging.sprintpcs.com";
      else
      if($carrier == "tmob")
        $emailAddress = $phoneNumber."@tmomail.net";
      else
        throw new Exception("Bad SMS carrier: $carrier");

      $text = "MedCommons HealthURL Alert - PIN $accessCode\n".
              $Secure_Url."/acct/sms.php?mcid=".$accessTo;

   //  dbg("Sending SMS to $emailAddress");
      
      // OK - at this point lets post to PennySMS
      
              dbg ("To PennySMS $acFromName $phoneNumber, $text");
      
     
     $output = $this->smspost ($acFromName, $phoneNumber, $text);
     

     dbg("Output from pennysms: $output");
//     $xml = @simplexml_load_string("<foo><bar>tree</bar></foo>");
     $xml = @simplexml_load_string($output);
     
     if($xml === FALSE)
         throw new SystemFailure("Unable to parse result returned by sms provider");
     
     if(isset($xml->fault)) {
         $code = $xml->fault->value->struct->member[0]->value->i4;
         $message = $xml->fault->value->struct->member[1]->value->string;
         dbg("Unable to send to $phoneNumber: code = ".$code." message = ".$message);
         throw new Exception("Unable to send to $phoneNumber: code = ".$code." message = ".$message);
     }
     
        // throw new Exception("Unable to send PennySMS to notify $phoneNumber of access code");
     
//      
//
//      $headers = "From: $acFromName <$acFromEmail>\n"
//                ."Reply-To: $acFromEmail\n"
//                ."User-Agent: MedCommons Mailer 1.0\n";
//
////      if(!@mail($emailAddress.",ssadedin@gmail.com", "MedCommons Access Code", $text, $headers))
////        throw new Exception("Unable to send email to notify $phoneNumber of access code");
//      
//      if(!@mail($emailAddress, "MedCommons Access Code", $text, $headers))
//        throw new Exception("Unable to send email to notify $phoneNumber of access code");

      pdo_commit();

      return $accessCode;
    }
    catch(Exception $e) {
      pdo_rollback();
      throw $e;
    }
  }
}

$x = new createPhoneAccessCode();
$x->handlews("createPhoneAccessCode");
?>
