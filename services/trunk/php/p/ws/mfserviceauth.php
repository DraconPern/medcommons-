<?

// appliance based service to satisfy multi-function patient list queries

require_once "login.inc.php";
require_once "wslibdb.inc.php";
require_once "../../acct/alib.inc.php";

require_once "urls.inc.php";
require_once "DB.inc.php";
require_once "utils.inc.php";


function my_query_patient_list($practiceId, $limit, $start=0, $whereclause="",
$viewStatusClause=" AND e.ViewStatus = 'Visible' ")
{

	$db = DB::get();
	//  echo "my query patient list with";print_r($practiceId);
	$practice = $db->first_row("select * from practice where practiceid=?",array($practiceId));

	$select = "SELECT e.*, wia.wi_id as wi_available_id, wid.wi_id as wi_downloaded_id, c.couponum,
	c.status as couponstatus, c.voucherid as voucherid, do.ddl_status as order_status, do.callers_order_reference as order_reference,
	do.custom_00, 	do.custom_01, 	do.custom_02, 	do.custom_03, 	do.custom_04, 	do.custom_05, 	do.custom_06, 	do.custom_07, 	do.custom_08, 	do.custom_09,
	do.ddl_status as dicom_order_status, u.photoUrl 
	FROM practice p, practiceccrevents e
	LEFT JOIN workflow_item wia ON e.PatientIdentifier = wia.wi_target_account_id AND wia.wi_type = 'Download Status' AND wia.wi_active_status = 'Active' and wia.wi_status = 'Available' AND wia.wi_source_account_id = $practice->accid
	LEFT JOIN workflow_item wid ON e.PatientIdentifier = wid.wi_target_account_id AND wid.wi_type = 'Download Status' AND wid.wi_active_status = 'Active' and wid.wi_status = 'Downloaded' AND wid.wi_source_account_id =$practice->accid
	LEFT JOIN modcoupons c on c.mcid = e.PatientIdentifier
	LEFT JOIN dicom_order do on do.mcid = e.PatientIdentifier
LEFT JOIN users u  on u.mcid = e.PatientIdentifier
	WHERE e.practiceid = '$practiceId' AND e.practiceid = p.practiceid
	AND ((p.accid = wia.wi_source_account_id) OR (wia.wi_source_account_id is NULL))
	AND ((p.accid = wid.wi_source_account_id) OR (wid.wi_source_account_id is NULL))
	$whereclause $viewStatusClause
	GROUP BY e.PatientGivenName, e.PatientFamilyName, e.Guid
	ORDER BY e.CreationDateTime DESC LIMIT $start,$limit";


	return $db->query($select);
}

function inlinequery($mcid, $filter, $filtermask, $groupid,$maxpatients=200)
{
	// how this works - the $filter variable is matched against a custom variable named 'custom_0$filtermask'

	$db = DB::get();
	$row = $db->first_row("select * from users where mcid = '$mcid'  " );
	if(!$row)
	die("No such user on re-read of ".$mcid);

	$info = new stdClass;
	$info->accid=$row->mcid;
	$info->fn=$row->first_name;
	$info->ln = $row->last_name;
	$info->email = $row->email;
	$info->idp = '';

	//$info = get_full_account_info($info);
	// What other groups does this user have?
	$gggroups = $db->query("select gi.accid,gi.name, gi.parentid from groupinstances gi, groupmembers gm
                                    where gm.memberaccid = ? and gm.groupinstanceid = gi.groupinstanceid", array($info->accid));


	//$practiceIds = array();
	$matched=false;
	foreach($gggroups as $g)
	if ($g->accid === $groupid)  // only return from the current practice id
	{

		$where='';//
		$matched = true;
		$info->matchedGroupID = $groupid;
		if ($filter!=0) $where = " AND do.custom_0{$filtermask} = '$filter' "; else $where = '';
		$patients = my_query_patient_list($g->parentid, 200,0, $where, " AND e.ViewStatus in ('Visible','Hidden')"); //was  AND e.ViewStatus = 'Visible' 
		break;
	}

	if ($matched===false)
	throw new ValidationFailure("found nogroup with groupid $groupid");

	$info->patients = $patients;
	//	print_r ($info);
	//	$xml->result =  $info;
	//	$xml->status = "ok";
	//	return $xml;
	return $info;
}

class mfService extends jsonrestws  {

	function verify_caller() {
		// No verification needed
	}

	function jsonbody() {

		global $Secure_Url;
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
		pdo_first_row("select * from groupinstances where accid = ?", array($groupAccountId));

		if(!$group)
		throw new Exception("Unknown group");



		$filter1 = 0; $filter2 = 0;
		if (isset($_REQUEST['mcid']))
		{
			$filter1 = req('mcid');
			if(!$filter1)
			throw new ValidationFailure('Filter mcid not provided');

			if (isset($_REQUEST['fieldmask']))
			$filter2 = req('fieldmask');
			else
			throw new ValidationFailure('Fieldmask needed if filter mcid provided');
			
			if (($filter2<0)||($filter2>9))
			throw new ValidationFailure('Fieldmask must be between 0 and 9');

		}



		$maxpatients = intval(req('maxpatients', 20));

		//how to turn $auth into $accid

		$userrec = User::from_auth_token($auth);

		return inlinequery($userrec->mcid,$filter1,$filter2,$groupid,$maxpatients); // return whole board

	}
}

$ws = new mfService();
$ws->handlews("mfService");

?>
