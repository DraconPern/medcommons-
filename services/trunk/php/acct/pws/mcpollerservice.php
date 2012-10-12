<?

/**
 * reloads details for a logged-in user, primarily getting back new list of groups
 */

require_once "wslibdb.inc.php";
require_once "../../acct/alib.inc.php";

require_once "urls.inc.php";
require_once "DB.inc.php";
require_once "utils.inc.php";


class groupsPollerService extends jsonrestws  {

	function verify_caller() {
		// No verification needed
	}

	function jsonbody() {

		global $Secure_Url;

	$db = DB::get();
	
		$reqid = req('reqid');
		if(!$reqid) $reqid = '-1';

	
		$auth = req('auth');
		if(!$auth)
		throw new ValidationFailure('auth not provided');

		$token = pdo_first_row("select * from authentication_token where at_token = ? and at_priority = 'G'", array($auth));
		if(!$token)
		throw new Exception("Unknown auth token or not authorized for group.");

	$info = get_account_info(); // for some reason, validated account info didnt work
    $info -> reqid = $reqid;
    
	//$info = get_full_account_info($info);
	// What other groups does this user have?
	$info->groups = $db->query("select gi.accid,gi.name, gi.parentid,gi.logo_url from groupinstances gi, groupmembers gm
                                    where gm.memberaccid = ? and gm.groupinstanceid = gi.groupinstanceid", array($info->accid));

	// add in our own photoUrl, pity we have to go read again
  	$info->photoUrl = $db->first_column("select photoUrl from users where mcid = ?",array($info->accid));
  
	return $info;

	}
}

$ws = new groupsPollerService();
$ws->handlews("grService");

?>
