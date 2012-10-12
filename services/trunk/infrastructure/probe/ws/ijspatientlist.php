<?
// fast and trim patientlist
require_once "login.inc.php";
require_once "wslibdb.inc.php";
require_once "../../acct/alib.inc.php";
require_once "../../acct/rls.inc.php";
require_once "DB.inc.php";
require_once "utils.inc.php";


class PatientListService extends jsonrestws  {

	function verify_caller() {
		// No verification needed
	}

	function jsonbody() {
		$isloggedin = (isset($_COOKIE['mc'])?1:0); // send back our previous state

		if(!$isloggedin)
		throw new ValidationFailure('you must be logged in');
		$mcid = req('mcid');
		if(!$mcid)
		throw new ValidationFailure('you must supply an mcid');
		// Got user, skip the logging in
		$user = User::load($mcid);
		//$user->login();

	    $info = get_validated_account_info();
		// if($row->
		//acctype == 'VOUCHER') {
		//   $db = DB::get();
			//  $info->voucher = $db->firstRow("select couponum, auth, expirationdate from modcoupons where mcid = ?",$info->accid);
		
		// Get some rows of patient list
		if($info->practice)
		$info->practice->patients = query_patient_list($info->practice->practiceid, 5);
		$info->wasloggedin = $isloggedin;
		 
		return $info;
	}
}

$ws = new PatientListService();
$ws->handlews("PatientListService");

?>