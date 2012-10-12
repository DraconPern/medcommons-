<?php
require_once "DB.inc.php";
require_once "../acct/alib.inc.php";
/**
 * Login Service
 *
 * rewritten to run inline in the gdashboard and not go thru remote json webservice, which will be reserved for now for the iphone.
 */

function compute_password($mcid,$pw1) {
	$sha1 = strtoupper(hash('SHA1', 'medcommons.net' . $mcid . $pw1));
	return $sha1;
}

/**
 * Performs query on patient list for specified practice.
 *
 * @param int $practiceId             practice id to query
 * @param int $limit                  max number of rows to return
 * @param int  $start                 row to start at
 * @param String $whereclause         optional where clause to filter results
 * @param String $viewStatusClause    optional viewstatus clause to filter results
 */
function my_query_patient_list($practiceId, $limit, $start=0, $whereclause="", $viewStatusClause=" AND e.ViewStatus = 'Visible' ") {

	$db = DB::get();
	//  echo "my query patient list with";print_r($practiceId);
	$practice = $db->first_row("select * from practice where practiceid=?",array($practiceId));

	$select = "SELECT e.*, wia.wi_id as wi_available_id, wid.wi_id as wi_downloaded_id, c.couponum,
	c.status as couponstatus, c.voucherid as voucherid, do.ddl_status as order_status, do.callers_order_reference as order_reference,
	do.ddl_status as dicom_order_status
	FROM practice p, practiceccrevents e
	LEFT JOIN workflow_item wia ON e.PatientIdentifier = wia.wi_target_account_id AND wia.wi_type = 'Download Status' AND wia.wi_active_status = 'Active' and wia.wi_status = 'Available' AND wia.wi_source_account_id = $practice->accid
	LEFT JOIN workflow_item wid ON e.PatientIdentifier = wid.wi_target_account_id AND wid.wi_type = 'Download Status' AND wid.wi_active_status = 'Active' and wid.wi_status = 'Downloaded' AND wid.wi_source_account_id =$practice->accid
	LEFT JOIN modcoupons c on c.mcid = e.PatientIdentifier
	LEFT JOIN dicom_order do on do.mcid = e.PatientIdentifier
	WHERE e.practiceid = '$practiceId' AND e.practiceid = p.practiceid
	AND ((p.accid = wia.wi_source_account_id) OR (wia.wi_source_account_id is NULL))
	AND ((p.accid = wid.wi_source_account_id) OR (wid.wi_source_account_id is NULL))
	$whereclause $viewStatusClause
	GROUP BY e.PatientGivenName, e.PatientFamilyName, e.Guid
	ORDER BY e.CreationDateTime DESC LIMIT $start,$limit";


	return $db->query($select);
}
function my_resolveEmail($email,$password) {
	//	echo "in my resolveEmail";
	$db = DB::get();
	$users = $db->query("SELECT users.mcid, users.sha1,
                           users.first_name, users.last_name,
                           users.email, users.acctype
                           FROM users
                           WHERE users.email = ?
                           ORDER BY users.since desc", array($email));

	foreach($users as $u) {
		$sha1 = compute_password($u->mcid, $password);
		dbg("Computed password hash for ".$u->mcid." on password ".$password." = ".$sha1);
		if($u->sha1 == $sha1)
		return $u;
	}
	return false;
}

function inlinequery($email,$password,$allpatients=false,$customFields=false,$maxpatients=10)
{
	//	echo "gdashboard inlinequery has $email";
	global $Secure_Url;

	//  $email = req('email');
	if(!$email)
	die('email not provided');

	//	if(!is_email_address($email))
	//	die('Invalid email '.$email.' provided');

	//  $password = req('password');
	if(!$password)
	die('password not provided');


	echo "getting first row from users";
	$db = DB::get();

	if (!($u=my_resolveEmail($email,$password))) die ("No such user /invalid password");
	$mcid = sprintf('%s',$u->mcid);
	//			echo "gdashboard loginservice finallyhas $mcid";
		

		
	$row = $db->first_row("select * from users where mcid = '$mcid'  " );
	if(!$row)
	die('No such user / invalid password on re-read of '.$mcid);

	print_r($row);


		
	$info = new stdClass;
	$info->accid=$row->mcid;
	$info->fn=$row->first_name;
	$info->ln = $row->last_name;
	$info->email = $row->email;
	$info->idp = '';


	$info = get_full_account_info($info);

	//		print_r($info);

	if($row->acctype == 'VOUCHER') {
		$info->voucher = $db->first_row("select couponum, auth, expirationdate from modcoupons where mcid = ?",$info->accid);
	}

	$patients = array();

	// Get some rows of patient list
	if($info->practice && !$allpatients)  {
		$patients = my_query_patient_list($info->practice->practiceid, $maxpatients, 0, ""," AND e.ViewStatus = 'Visible' ",$customFields);
		$info->practice->patients = $patients;
	}

			// What other groups does this user have?
			$info->groups = $db->query("select gi.accid,gi.name, gi.parentid from groupinstances gi, groupmembers gm
	                                    where gm.memberaccid = ? and gm.groupinstanceid = gi.groupinstanceid", array($info->accid));
			$allgroups = array();
			if($allpatients) {
				$practiceIds = array();
				foreach($info->groups as $g) {
					$practiceIds[]=$g->parentid;
					$g->patients = array();
					$allgroups[$g->parentid] = $g;
				}
				$patients = my_query_patient_listx($practiceIds, $maxpatients,0,"", " AND e.ViewStatus = 'Visible' ", $customFields);
			}
	
	// Augment the patients with their photos, if we can
	$patientMcIds = array();
	foreach($patients as $p) {
		$patientMcIds[]=$p->PatientIdentifier;
	}

	if(count($patientMcIds)>0) {
		$i = 0;
		dbg("Querying photo urls for ".count($patientMcIds)." patients");
		$photos = $db->query("select photoUrl from users where mcid in (".join(",", $patientMcIds).")");
		foreach($photos as $photo) {
			$url = $photo->photoUrl;
				
			// Qualify to make absolute urls when they are relative
			if($url && (strpos($url, "http",0) !== 0)) {
				$url = $Secure_Url . "/". ltrim($url,"/");
			}
			$patients[$i]->photoUrl = $url;
			++$i;
		}
	}

	foreach($patients as $p) {
		$allgroups[$p->practiceid]->patients[]=$p;
	}

	$info->photoUrl = $db->first_column("select photoUrl from users where mcid = ?",array($info->accid));
	$xml->result =  $info;
	$xml->status = "ok";
	return $xml;
}



function remote_poke ()
{

	//medcommons7%2B1@gmail.com
	//return inlinequery($email='medcommons7+1@gmail.com',$password='tester',$allpatients=true,$customFields=true,$maxpatients=20) ;
		$remoteurl =  "http://ci.myhealthespace.com/acct/loginservice.php?email=medcommons7%2B1@gmail.com&password=tester&allpatients=1&maxpatients=20&customFields=true";
		//	echo ">> $remoteurl<br/>";
		try {
			// consumer token when creating patient
			$file = file_get_contents($remoteurl);
			//echo $file;
			$json = new Services_JSON();
			$result = $json->decode($file);
			if(!$result)
			throw new Exception("Unable to decode JSON returned from URL ".$remoteurl.": ".$file);
			if($result->status != "ok")
			throw new Exception("Bad status '".$result->status."' error='".$result->error."' returned from JSON call ".$remoteurl);
			return $result;
		}
		catch(Exception $ex) {
			die("Unsuccessful test completion ". $ex->getMessage());
			}
			return false;
	}
	/////////

	function choose_consultant_page()
	{

		$h = z('h');
		$targetacc = z('t');
		$patientname = z('pname');

		list($hn,$hlogo,$progind,$vn,$servicename,$serviceind) = ctx($h);
		if(!($me=Gtestif_logged_in())) please_login(); else
		list($accid,$fn,$ln,$email,$idp,$mc,$auth) =$me;
		// if we have admin privs then we can see everything, otherwise we just match on $accid
		$providername = trim ("$fn $ln");

		$ustart = ''; // if set, will get set to a link
		$out ="<img src='$hlogo' alt='missing $hlogo' />
		<div><span id=hospital>$hn</span>&nbsp;&nbsp;&nbsp;&nbsp;<span id=program>$vn</span>&nbsp;&nbsp;&nbsp;&nbsp;<span id=provider>$providername($accid)</span></div>";


		$out .= <<<XXX
		<div id=consultant_chooser_for_email_screen>


		<p>Forward case  $patientname $targetacc to:
	<ul style='list-style-type: none;' >	
XXX;


		$providers = dosql("select * from aJoined j,aProviders p,aServices s
		where hospitalind='$h' and programind='$progind' and p.role='consultant'
		and j.providerind=p.ind and j.serviceind=s.ind ");
		while ($provider=mysql_fetch_object($providers))
		{
			$phonelink = '';
			if ($provider->defaultPhone)	$phonelink = $provider->defaultPhone; //Dont make it a link
			$videolink = '';
			if ($provider->defaultVideoURL) $videolink = "<a href='$provider->defaultVideoURL'>video</a>";

			$out.="<li><a href='?handler=forward_email&h=$h&p=$provider->providerind&t=$targetacc&colleague=$providername&pname=$patientname' >$provider->provider</a>
			$provider->department $phonelink $videolink</li>";
		}
		$out .= <<<XXX
	</ul>
</div>
XXX;

		return $out;

	}




	function show_consultants_dashboard($h)
	{
	//	echo "getting consultants dashboard";
		$db = DB::get();

		// show the consultants filtered dashboard
		if(!($me=Gtestif_logged_in())) please_login(); else
		list($accid,$fn,$ln,$email,$idp,$mc,$auth) =$me;
		// if we have admin privs then we can see everything, otherwise we just match on $accid

		list($hn,$hlogo,$progind,$vn,$servicename,$serviceind) = ctx($h);

	//	echo "*********getting metaboard *********<br/>";
		$metaboard = remote_poke();
	//	print_r($metaboard);
		$providername = trim ("$fn $ln");
		$uprovidername = urlencode($providername);
		$practice = $metaboard->result->practice;
		$practicename = $practice->practicename;$practiceaccid = $practice->accid;

		$ustart = ''; // if set, will get set to a link
		$out ="<img src='$hlogo' alt='missing $hlogo' />
		<div><span id=hospital>$hn</span>&nbsp;&nbsp;&nbsp;&nbsp;<span id=program>$vn</span>&nbsp;&nbsp;&nbsp;&nbsp;
		<span id=provider>$providername($accid)</span>&nbsp;<a href='logout.php?next=/acct/g.php' >logout</a></div>
";

		$dcounter=0;

		// figure out if we have admin privs
		$providers = dosql("select provider,ind,mcid,role from aProviders p where mcid='$accid'");

		if (!($provider=mysql_fetch_object($providers)))  die ("Cant find provider with mcid $accid");

		if  ($provider->role=='admin') $banner =  "<h3>Admin Dashboard for $fn $ln</h3>"; else $banner="<h3>Consultant Dashboard for $fn $ln</h3>";
		$dcounter = 0;
		$out .= <<<XXX
		$banner
<table>
  <tbody>
	<tr>
	<th>Time</th>
	<th>Order Ref</th>
	<th>Hospital</th>
	<th>Sender</th>
	<th>Patient</th>
	<th>Clinical History</th>
	<th>View</th>
	<th>Forward</th>
	<th>Contact</th>
	</tr>
XXX;
		foreach ($metaboard->result->groups as $group)
		{
			$dcounter++;
			//	if ($d==$dcounter) // only take ...
			{

				if (isset($group->patients))
				foreach ($group->patients as $patient)
				{
					// only show this if we have admin
					if ( ($provider->role=='admin')||
					(isset($patient->custom_07)&&($patient->custom_07==$accid)))
					{
						$name = trim ("$patient->PatientGivenName $patient->PatientFamilyName");
						if (strlen($name) > 1)
						{

							// handle the custom fields

							//	'custom_00'=>$refhospital,
							//	'custom_01'=>$refphys,
							//		'custom_02'=>$email,
							//		'custom_03'=>$videourl,
							//		'custom_04'=>$phone,
							//		'custom_05'=>$pref,
							//		'custom_06'=>$clinical,
							//		'custom_07'=>$consultantmcid,

							$uname = urlencode($name);


							$extrabits = '';
							// for testing just simulate dashboard response fields
							$chooselink = "<a  href='?handler=forward_chooser&h=$h&t={$patient->PatientIdentifier}&pname=$name '>consultant</a>";

							// video link
							$time = strftime('%D %T', $patient->CreationDateTime);
							$hospital = $patient->custom_00;
							$sender = $patient->custom_01;
							$usender = urlencode($sender);
							if (isset($patient->custom_03)&&($patient->custom_03!=''))
							{
								if (strpos($patient->custom_03,'kype:')>0) $label = 'skype'; else $label = 'ooVoo';
								$videolink = "<a href='{$patient->custom_03}'>$label</a>";
							} else
							$videolink = '';

							// the sms link
							//<a target='_new' href='fsms.php?from=$providername&num=6175713857&name=$uname&v=$v&h=$h&d=d'>sms</a>
							if (isset($patient->custom_04)&&($patient->custom_04!=''))
							{
								if (strpos($patient->custom_04,'el:')>0)

								$smslink = "<a href='?handler=tel_call_msg&h=$h&t={$patient->PatientIdentifier}&pname=$uname&from=$email&phone={$patient->custom_04}&refphys=$usender '>call</a>";
								else
								$smslink = "<a
								href='?handler=sms_send_msg&h=$h&t={$patient->PatientIdentifier}&pname=$uname&from=$email&phone={$patient->custom_04}&refphys=$usender '>sms</a>";

							}
							else
							$smslink = '';


							// the email link
							//						if (isset($patient->custom_02)&&($patient->custom_02!=''))
							//						$emaillink = "<a href='{$patient->custom_02}'>email</a>"; else
							$emaillink = '';

							$xmllink = '';//"<a href='http://ci.myhealthespace.com/orders/orderstatus?callers_order_reference=$patient->order_reference&fmt=xml'>xml</a>";

							//http://ci.myhealthespace.com/orders/orderstatus?callers_order_reference=ord1266154702&fmt=xml
							$out .= "<tr><td>$time</td>
							<td title='consultant mcid:$patient->custom_07'><a href='http://ci.myhealthespace.com/orders/orderstatus?callers_order_reference=$patient->order_reference'>$patient->order_reference</a></td><td>$hospital</td><td>$sender</td><td>$name</td>
							<td>$patient->custom_06</td>
							<td><a target='_new' href='http://ci.myhealthespace.com/{$patient->PatientIdentifier}' >go</a></td>
							<td>$chooselink</td>
							<td>$smslink $videolink $emaillink $xmllink
							</td></tr>";
						}
					}// if for
				}

			}

		}// foreach table

		$out .= <<<XXX

	</tbody>
</table>
XXX;

		$out .= <<<XXX

</div>
XXX;


		//	$out .= "<br/><hr/><a href=?handler=propage&h=$h />admin options</a>";

		return $out;
	}
	?>