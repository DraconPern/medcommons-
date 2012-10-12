<?

/**
 * Loads patients from a user's inbox and patient member roster
 * and returns as JSON. for iPhad clients
 */
require_once "DB.inc.php";
require_once "wslibdb.inc.php";
require_once "patientlist.inc.php";
require_once "../alib.inc.php";
require_once "login.inc.php";

class mfService extends jsonrestws  {

	function verify_caller() {
		// No verification needed
	}

	function jsonbody() {

		global $Secure_Url;
		
		
			$db = DB::get();
			
			
		$reqid = req('reqid');
		if(!$reqid) $reqid = '-1';
			
		$groupid = req('groupid');
		if(!$groupid)
		throw new ValidationFailure('groupid not provided');

		$auth = req('auth');
		if(!$auth)
		throw new ValidationFailure('auth not provided');

		$token = pdo_first_row("select * from authentication_token where at_token = ? and at_priority = 'G'", array($auth));
		if(!$token)
		throw new Exception("Unknown auth token or not authorized for group.");

		$groupAccountId = $token->at_account_id;

		$group =
		pdo_first_row("select * from groupinstances where accid = ?", array($groupid));

		if(!$group)
		throw new Exception("Unknown group");

		//how to turn $auth into $accid
		$userrec = User::from_auth_token($auth);

		$info = get_account_info();
		$info -> reqid = $reqid; 
			
		//$info->accidx =$info->accid; // move this into info block
   
		$info->groupidx = $groupid;
	
		$info->mcidx = $userrec->mcid;
	
		$info->accid =$userrec->mcid; // move this into info block
	
		$inbox = new PatientList($group->parentid, "inbox");
		
		$info->patients = $inbox->patients();
			
		$members = new PatientList($group->parentid , "members");

		$info->members = $members->patients();
	
		return $info;
	}
}


$ws = new mfService();
$ws->handlews("mfService");

?>
