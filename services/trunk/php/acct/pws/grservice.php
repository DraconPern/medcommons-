<?

// appliance based service to satisfy multi-function patient list queries

require_once "login.inc.php";
require_once "wslibdb.inc.php";
require_once "../../acct/alib.inc.php";

require_once "urls.inc.php";
require_once "DB.inc.php";
require_once "utils.inc.php";




function inlinequery($mcid)
{
	

	
	// how this works - the $filter variable is matched against a custom variable named 'custom_0$filtermask'

	$db = DB::get();
	//	$row = $db->first_row("select * from users where mcid = '$mcid'  " );
	//	if(!$row)
	//	die("No such user on re-read of ".$mcid);
	//
	//	$info = new stdClass;
	//	get_account_info()
	//	$info->accid=$row->mcid;
	//	$info->fn=$row->first_name;
	//	$info->ln = $row->last_name;
	//	$info->email = $row->email;
	//	$info->idp = '';

	 

	// Got user, log them in
	$user = User::load($mcid);


	$user->login(); // Cookie set in here
	$info = get_account_info(); // for some reason, validated account info didnt work



	//$info = get_full_account_info($info);
	// What other groups does this user have?
	$info->groups = $db->query("select gi.accid,gi.name, gi.parentid,gi.logo_url from groupinstances gi, groupmembers gm
                                    where gm.memberaccid = ? and gm.groupinstanceid = gi.groupinstanceid", array($info->accid));

	// add in our own photoUrl, pity we have to go read again
  	$info->photoUrl = $db->first_column("select photoUrl from users where mcid = ?",array($info->accid));
  
	return $info;
}

class grService extends jsonrestws  {

	function verify_caller() {
		// No verification needed
	}

	function jsonbody() {

		global $Secure_Url;

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


		return inlinequery($row->mcid); // return whole board

	}
}

$ws = new grService();
$ws->handlews("grService");

?>
