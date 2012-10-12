<?

/**
 * Loads patients from a user's inbox and patient member roster
 * and returns as JSON. for iPhad clients
 */
 
 // appliance based service to satisfy multi-function patient list queries

require_once "DB.inc.php";
require_once "utils.inc.php";


require_once "wslibdb.inc.php";
require_once "patientlist.inc.php";
require_once "../alib.inc.php";
require_once "login.inc.php";

class mfService extends restws  {

	function verify_caller() {
		// No verification needed
	}

	function xmlbody() {

		global $Secure_Url;
		
		
	$starttime = microtime(true);
	
			$db = DB::get();

		$reqid = req('reqid');
		if(!$reqid) $reqid = '-1';
		
		if ($reqid > 0) $reqinfo = " request-id='$reqid' "; else $reqinfo='';
			
		$groupid = req('groupid');
		if(!$groupid)
		throw new ValidationFailure('groupid not provided');
		
				$email = req('email');
		if(!$email)
		throw new ValidationFailure('email not provided');

		if(!is_email_address($email))
		throw new ValidationFailure('Invalid email '.$email.' provided');

		$password = req('password');
		if(!$password)
		throw new ValidationFailure('password not provided');

		// Resolve user
		$row = User::resolveEmail($email, $password);
		if(!$row) {

			throw new ValidationFailure('No such user / invalid password');
		}
		
		// Got user, log them in
		
	$user = User::load($row->mcid);


		$group =
		pdo_first_row("select * from groupinstances where accid = ?", array($groupid));

		if (!$group) throw new ValidationFailure('Bad group id');


		$info = get_account_info();
		$info -> reqid = $reqid; 
			
		//$info->accidx =$info->accid; // move this into info block
   
		$info->groupidx = $groupid;
	
	
		$info->accid =$row->mcid; // move this into info block
	
		$inbox = new PatientList($group->parentid, "inbox");
		
		$info->patients = $inbox->patients();
			
		$members = new PatientList($group->parentid , "members");

		$info->members = $members->patients();
	
		  $srva = $_SERVER['SERVER_ADDR'];
        $srvp = $_SERVER['SERVER_PORT'];
        $gmt = gmstrftime("%b %d %Y %H:%M:%S");
    
      $servicetime = 1000*(microtime(true)-$starttime);
      
      $servicetime = number_format($servicetime, 2, '.', '');
      $servername = $_SERVER ['SERVER_NAME'];     
      $rem = $_SERVER['REMOTE_ADDR'];
      
      $patientxml1 = ''; $count1=0;
      foreach ($info->patients as $patient)
      {
      $pos = strpos($patient->ViewerURL,'router/access');
      if ($pos)
       $url = substr($patient->ViewerURL,0,$pos).$patient->PatientIdentifier;
       else $url = "malformed";
       $count1++;
       $patientxml1 .=
      "
      <item>
      <name><first>{$patient->PatientGivenName}</first>
      <last>{$patient->PatientFamilyName}</last></name>
      <mcid>{$patient->PatientIdentifier}</mcid>
       <url>{$url}</url>
      <ref>{$patient->callers_order_reference}</ref>
      <comments>{$patient->Comment}</comments>
      </item>
      ";
      }
      
         $patientxml2 = ''; $count2=0;
        foreach ($info->members as $patient)
      {
      $pos = strpos($patient->ViewerURL,'router/access');
      if ($pos)
       $url = substr($patient->ViewerURL,0,$pos).$patient->PatientIdentifier;
       else $url = "malformed";
       $count2++;
      $patientxml2 .=
      "
      <item>
      <name><first>{$patient->PatientGivenName}</first>
      <last>{$patient->PatientFamilyName}</last></name>
      <mcid>{$patient->PatientIdentifier}</mcid>
       <url>{$url}</url>
      <ref>{$patient->callers_order_reference}</ref>
      <comments>{$patient->Comment}</comments>
      </item>
      ";
      }
 
		
$msg = 		
"<?xml version='1.0' ?>
<hdoxGroupService localserver='$servername $srva:$srvp' remoteserver='$rem' >
<time>
$gmt
</time>
<inputs $reqinfo>
<groupid>
$groupid
</groupid>
<username>
$email
</username>
<lists>
inbox,members
</lists>
</inputs>
<outputs elapsedms='$servicetime' >
<list name='inbox' count='$count1' >
$patientxml1
</list>
<list name='members' count='$count2' >
$patientxml2
</list>
</outputs>
<status  >
ok
</status>
</hdoxGroupService>
";

   header("Cache-Control: no-store, no-cache, must-revalidate");
    header("Pragma: no-cache");
	header("Content-type:text/xml");
	echo $msg;
	exit;


	}
}


$ws = new mfService();
$ws->handlews("mfService");

?>




?>
