<?
//require_once "globals.inc.php";
require_once "appinclude.php";
require_once "./OAuth.php";
require_once "./mc_oauth_client.php";

/**
 * Basic class modelling a HealthBook user.
 *
 */
class MedCommonsAccount {
	public $mcid = false;
	public $appliance;
	public $familyfbid;
	public $firstname;
	public $lastname;
	public $token = false;
	public $secret = false;
	public function getFirstName() {
		return $this->firstname;
	}
	public function getLastName() {
		return $this->lastname;

	}

	public function authorize($url, $u = null) {

		global $oauth_consumer_key;
		global $oauth_consumer_secret;

		if($u == null) $u = $this;

		if(strpos("$url","?")===FALSE) $url .= "?";	else $url .= "&";
		// Add identity information so that receiving appliance can
		// identify our facebook user
		$result = $url."identity_type=Facebook".
      "&identity=".$this->familyfbid.
      "&identity_name=".urlencode($u->getFirstName()." ".$u->getLastName());

		$api = $this->getOAuthAPI();
		if(!$api) {			error_log("Unable to create oauth api for user ".$this->mcid);	return false;	}

		return $api->sign($url);
	}

	/**
	 * Returns an OAuth API configured for accessing the user's appliance
	 */
	public function getOAuthAPI() {
		global $oauth_consumer_key, $oauth_consumer_secret;

		if(!$this->mcid)
		return false;

		if(!$this->token)
		return false;

		$api = new ApplianceApi($oauth_consumer_key, $oauth_consumer_secret, rtrim($this->appliance,"/"), $this->token, $this->secret);
		return $api;
	}
}
class HealthBookUser extends MedCommonsAccount {

	// THIS IS A FACEBOOK USER IN THE USERS TAB , THEY MAY HAVE NO MCID UNTIL THEY CREATE THE FIRST PATIENT ACCOUNT
	public $fbid = false;
	public $targetmcid;
	public $accountlabel;
	public $accountpic;
	public $accountpicdesc;
	public $accountlink;
	public static function load($fbid,$u=false) {
		if (isset($GLOBALS['fb_hbc'])&&($u===false))
		return $GLOBALS['fb_hbc']; // only do this once, no matter how many times its called

		$GLOBALS['fc_hbc'] =  $hbc = new HealthBookUser();
		error_log("loading facebook user $fbid");
		if ($u===false) // only read if nothing supplied
		{
			$q = "select * from users where fbid = '$fbid' ";
			$result = mysql_query($q) or die("cant select from  $q ".mysql_error());
			$u=mysql_fetch_object($result);
			if($u==false)	return false;
		}
		$hbc->mcid=$u->mcid ;
		$hbc->token = $u->oauth_token;
		$hbc->secret = $u->oauth_secret;
		$hbc->appliance = $u->applianceurl ? rtrim($u->applianceurl,'/')."/" : false;
		$hbc->targetmcid = $u->targetmcid; // hoping to figure out how to retire this
		$hbc->familyfbid = $u->familyfbid;
		$hbc->accountlabel = $u->accountlabel;
		$hbc->accountlink = $u->accountlink;
		$hbc->accountpic = $u->accountpic;
			$hbc->accountpicdesc = $u->accountpicdesc;
		$hbc->firstname = $u->firstname;
		$hbc->lastname = $u->lastname;
		$hbc->photoUrl = $u->photoUrl;
		$hbc->fbid = $fbid;
		$hbc->storage_account_claimed = isset($u->storage_account_claimed) && ($u->storage_account_claimed > 0);
		return $hbc;
	}
}
class HealthBookPatient extends MedCommonsAccount {

	// A PATIENT IS JUST A MEDCOMMONS ACCOUNT
	public static function loadpatient($mcid,$familyfbid,$u=false) {
		$fbid = 0;
		//echo "in loadpatient, why are we here at all?";
		if (isset($GLOBALS['mc_patient'])&&($u===false))
		return $GLOBALS['mc_patient']; // only do this once, no matter how many times its called

		$GLOBALS['mc_patient'] =  $patient = new HealthBookPatient();
		error_log("loading medcommons target mcid $mcid family fbid $familyfbid ");
		if ($u===false) {
			//,only read if no record supplied
			$q = "select *from patients where mcid = '$mcid' and familyfbid ='$familyfbid' ";
			$result = mysql_query($q) or die("cant select from  $q ".mysql_error());
			$u=mysql_fetch_object($result);
			if($u==false) 	return false;
		}
		$patient->mcid=$u->mcid ;
		$patient->appliance = $u->applianceurl ? rtrim($u->applianceurl,'/')."/" : false;
		$patient->familyfbid = $u->familyfbid;
		$patient->firstname = $u->firstname;
		$patient->lastname = $u->lastname;
		$patient->token = $u->oauth_token;
		$patient->secret = $u->oauth_secret;
		$patient->storage_account_claimed = isset($u->storage_account_claimed) && ($u->storage_account_claimed > 0);
		return $patient;
	}
}
?>
