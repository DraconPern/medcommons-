<?php 
// library for the /acct service
require_once "dbparamsidentity.inc.php";
require_once "settings.php";
require_once "urls.inc.php";
require_once "utils.inc.php";
require_once "DB.inc.php";
require_once "JSON.php";
require_once "PDOLib.inc.php";

/**
 * Returns account information about the current user, derived
 * from the database. 
 * <p>
 * @param extra     pass true to return extended information (slower)
 */
function get_validated_account_info($extra=false) {
    
    // Get info from cookie
    if(!is_logged_in())
      return false;

    $info = get_account_info();
    if(!$info)
      return false;

	return get_full_account_info($info,$extra);
}

/**
 * Load full information for the given user based on the 
 * partial cookie information 
 *
 * @param  Object $info  cookie based user information
 * @return Object with full user information augmented from database
 */
function get_full_account_info($info,$extra=false) {
  try {
    if($extra===true) 
        $extraColumns=", gi.upload_notification";
    else
        $extraColumns="";
    
    $u = pdo_query("select u.email, u.first_name, u.last_name, u.acctype, u.enable_vouchers, 
      u.enable_dod, u.active_group_accid, p.*,gi.enable_uploads, gi.logo_url
      $extraColumns
      from users u
      left join groupinstances gi on gi.accid = u.active_group_accid
      left join practice p on p.providergroupid = gi.groupinstanceid
      where mcid = ?", $info->accid);

    if($u === false)
      error_page("Unable to load information about account ".$info->accid);

    if(count($u) === 0)
      return false;
      
    $u = $u[0];

    $info->email = $u->email;
    $info->fn = $u->first_name;
    $info->ln = $u->last_name;
    $info->acctype = $u->acctype;
    $info->enable_vouchers = $u->enable_vouchers;
    $info->enable_dod = $u->enable_dod;

    if($u->practiceid) {
      $p = new stdClass;
      $p->practiceid = $u->practiceid;
      $p->practicename = $u->practicename;
      $p->providergroupid = $u->providergroupid;
      $p->enable_uploads = $u->enable_uploads;
      $p->accid = $u->active_group_accid;
      $p->logo_url = $u->logo_url;
      if(isset($u->upload_notification))
          $p->upload_notification = $u->upload_notification;
      else
          $p->upload_notification = null;
      
      $info->practice = $p;
    }
    else
      $info->practice = false;

    return $info;
  }
  catch(Exception $e) {
    error_log("Failed to query user information for user ".$info->accid.": ".$e->getMessage());
    return false;
  }
    
}

/**
 * Returns Base URL of a gateway that may be used for creation of new content.
 *
 * @param accid - account id that will be creating the content.
 * @return - base url to gateway for creation of content, or false if error occurred.
 */
function allocate_gateway($accid) {
  // TODO: implement multiple gateway support
  return $GLOBALS['Default_Repository'];
}

/**
 * Return a url to a gateway for creation of a new CCR
 *
 * @param accid - account id, if any, for which the CCR is being created.
 * @return - complete url for creation of new ccr, or false if error occurred.
 */
function new_ccr_url($accid,$auth="",$action="new") {
  $gwUrl = allocate_gateway($accid);
  if($gwUrl !== false) {
    return $gwUrl."/tracking.jsp?tracking=$action&accid=$accid&auth=$auth";
  }
  else 
    throw new Exception("Unable to locate gateway for new account");
}

/**
 * Return the authentication_token record that contains the group api
 * keys for the specified group account id, or null if there is no
 * such record.
 * 
 * @param $accid    mcid of group to find keys for
 * @return Object representing authentication_token
 */
function query_group_api_keys($accid) {
	return pdo_first_row("select * from authentication_token 
                              where at_account_id = ? 
                                and at_secret IS NOT NULL
                                and at_parent_at_id = at_id
                                and at_priority = 'G'
                                and at_expired_date_time is NULL", array($accid));    
}

/**
 * create_group
 * 
 * @param	$user     user to create group for, as loaded by 
 *			          get_validated_account_info()
 * @param $groupName	name of group to create
 * @throws Exception	for database errors
 */
function create_group($user, $groupName) {

  dbg("Creating group for user ".$user->accid);

  global $URL, $NS;
  $client = new SoapClient(null, array('location' => $URL, 'uri' => $NS));
  $groupAccId = $client->next_mcid();

  dbg("New group accid = ".$groupAccId);
  
  $db = DB::get();

  // Add group user
  $db->execute("insert into users (mcid,acctype) values (?,?)", array($groupAccId, 'GROUP')); 

  $groupId = $db->execute("insert into groupinstances (groupinstanceid,name,groupLogo,adminUrl,memberUrl,accid,upload_notification) 
                           values (NULL,?,'','','',?,?)",array($groupName, $groupAccId,$user->email));

  dbg("New group groupId = $groupId");

  // Add practice associated with group
  $practiceId = $db->execute("insert into practice (practiceid,practicename,providergroupid,practiceRlsUrl,practiceLogoUrl,accid) 
               values (NULL,?,?,?,?,?)",array($groupName, $groupId, '','',$groupAccId));

  dbg("New group practiceId = $practiceId");

  $practiceRlsUrl = gpath('Accounts_Url').'/ws/R.php?pid='.$practiceId;

  dbg("Updating practice RLS url to $practiceRlsUrl");

  $db->execute('update practice set practiceRlsUrl = ? where practiceid = ?',array($practiceRlsUrl,$practiceId));

  $db->execute('update groupinstances set parentid = ? where groupinstanceid = ?',array($practiceId,$groupId));

  // Add user to the group
  $db->execute("insert into groupmembers (groupinstanceid,memberaccid) values (?,?)",array($groupId,$user->accid));

  // Add user as admin of group
  $db->execute("insert into groupadmins (groupinstanceid,adminaccid) values (?,?)",array($groupId,$user->accid));

  // Make newly created group default active group for user
  $db->execute("update users set active_group_accid = ? where mcid = ?",array($groupAccId, $user->accid));

  return array($groupAccId, $practiceRlsUrl);
}

/**
 * Sends an email to specified user inviting them to join the specified group.
 * The email must be associated with an *existing* user record.  The user record
 * will be set to type GROUP_INVITE.
 *
 * @param Object       $user    info about user who invited
 * @param unknown_type $accid
 * @param unknown_type $email
 */
function invite_group_users($user, $accids, $emails, $groupAcctId) {
    
  global $acCommonName, $acApplianceName, $acDomain, $Secure_Url, $SECRET;
  
  
  $db = DB::get();
  
  // Find the group to invite the user to
  $group = $db->first_row("select gi.* from groupinstances gi where gi.accid = ?",array($groupAcctId));
  
  $i=0;
  foreach($accids as $accid) {
      $email = $emails[$i++];
        
      // Add the user to the group
      $db->execute("replace into groupmembers (groupinstanceid,memberaccid,comment) values (?,?,?)",
                  array($group->groupinstanceid, $accid, ''));
    
      // Create a URL for verification with signature
      $url  = rtrim($Secure_Url,'/')."/acct/group_registration.php";
      $params = "accid=$accid&email=".urlencode($email);
      $hmac = hash_hmac('SHA1', $params, $SECRET);
      $url .= "?".$params."&enc=$hmac";
      
      $t = template("group_invite_email_text.tpl.php")
            ->set("user",$user)
            ->set("acCommonName", $acCommonName)
            ->set("acApplianceName",$acApplianceName)
            ->set("acDomain", $acDomain)
            ->set("applianceUrl", $Secure_Url)
            ->set("groupName",$group->name);
    
      $t->set("url",$url);
    
      dbg("Sending email to $email");
      send_mc_email($email,
                    "You have been Invited to join a Group on ".$acCommonName,
                    $t->fetch(),
                    $t->fetch("group_invite_email_html.tpl.php"),
                    array());
  }
}

/**
 * Returns all practices of which the given account is a member as PHP Objects
 * Returns false if no practices found.
 *
 * @param accid - the account id to query
 * @param practiceId - optional practice id to filter on
 */
function q_member_practices($accid, $practiceId = null)
{
  $sql = "SELECT q.*,i.accid, i.enable_uploads from practice q, groupmembers p, users u, groupinstances i
            where p.memberaccid=? 
            and  q.providergroupid=i.groupinstanceid  
            and i.parentid>0 
            and  p.groupinstanceid= i.groupinstanceid 
            and p.memberaccid=u.mcid";
  if($practiceId !== null) {
    $sql .= " and q.practiceid = ?";
  }

  $practices =  pdo_query($sql,$accid,$practiceId);

  if($practices === false) {
    return false;
  }

  if(count($practices) > 0) {
    return $practices;
  }
  else
    return false;
}

// return array of ids of practices to which this member belongs
function q_member_practice_ids($accid) {
  $query = "select p.practiceid from practice p, groupinstances g, groupmembers m
              where p.providergroupid = g.groupinstanceid
              and m.groupinstanceid = g.groupinstanceid
              and m.memberaccid = '$accid'
              order by p.practiceid";
	$result = mysql_query ($query) or die("can not query $query - ".mysql_error());
	return mysql_fetch_array($result);
}

// counted queries

// administrator of  practices
function count_admin_practices($accid)
{
	$query = "SELECT COUNT(*) from practice q, groupadmins p, groupinstances i , users u
	where p.adminaccid='$accid' and  q.providergroupid=i.groupinstanceid  and 
	i.parentid>0 and  p.groupinstanceid= i.groupinstanceid and 
	p.adminaccid=u.mcid 
              ";
	$result = mysql_query ($query) or die("can not query $query - ".mysql_error());
	$count = mysql_fetch_array($result);
	mysql_free_result($result);
	return $count[0];
	return $count[0];
}


function count_member_practices($accid)
{
	// member of practices

	$query = "SELECT COUNT(*)  from practice q, groupmembers p, groupinstances i , users u
	where p.memberaccid='$accid' and  q.providergroupid=i.groupinstanceid  and 
	i.parentid>0 and  p.groupinstanceid= i.groupinstanceid and 
	p.memberaccid=u.mcid
              ";
	$result = mysql_query ($query) or die("can not query $query - ".mysql_error());
	$count = mysql_fetch_array($result);
	mysql_free_result($result);
	return $count[0];
}
// administrator of groups
function count_admin_groups($accid)
{
	$query = "SELECT COUNT(*)  from groupadmins p, groupinstances i , users u
	where p.adminaccid='$accid' and  i.parentid=0 and  p.groupinstanceid= i.groupinstanceid and p.adminaccid=u.mcid
 	order by p.groupinstanceid,p.adminaccid ";
	$result = mysql_query ($query) or die("can not query $query - ".mysql_error());
	$count = mysql_fetch_array($result);
	mysql_free_result($result);
	return $count[0];
}
// member of groups

function count_member_groups($accid)
{
	$query = "SELECT COUNT(*)  from groupmembers p, groupinstances i , users u
	where p.memberaccid='$accid' and i.parentid=0 and p.groupinstanceid= i.groupinstanceid and p.memberaccid=u.mcid 
	order by p.groupinstanceid,p.memberaccid ";
	$result = mysql_query ($query) or die("can not query $query - ".mysql_error());
	$count = mysql_fetch_array($result);
	mysql_free_result($result);
	return $count[0];
}

// returns array of rows of document_type that are ccr merges for $accid
function q_ccr_merges($accid,$maxrows) {
  $query = "select * from document_type
              where dt_type = 'CURRENTCCR'
              and dt_account_id = '$accid'
              order by dt_create_date_time desc
              limit $maxrows";
	$result = mysql_query ($query) or die("can not query $query - ".mysql_error());
  $merges = array();
  while($row = mysql_fetch_array($result)) {
    $merges[]=$row;
  }
	return $merges;
}

/**
 * Returns the guid of the current ccr of this user or false if there is none
 */
function getCurrentCCRGuid($accid) {
  $db = DB::get();
  $obj = $db->first_row("select dt_guid from document_type 
                        where dt_type='CURRENTCCR' and dt_account_id=? order by dt_create_date_time desc, dt_id desc limit 1",
                    array($accid));
  if(!$obj) 
    return false;
   else
     return $obj->dt_guid;
}

/**
 * return a url for the current ccr of this user, or return false if there is none.
 */
function tryCCCR($accid) {
  $guid = getCurrentCCRGuid($accid);
  if($guid === false)
     return false;
     
  return $GLOBALS['Commons_Url']."gwredirguid.php?guid=".$guid;
}

function aconnect_db()
{
  global $IDENTITY_HOST, $IDENTITY_USER, $IDENTITY_PASS, $DB_SETTINGS, $IDENTITY_DB;
  
  if(!mysql_pconnect("$IDENTITY_HOST", $IDENTITY_USER, $IDENTITY_PASS))
    throw new Exception("Cannot connect to mysql");
    
  if(!mysql_select_db($IDENTITY_DB))
    throw new Exception("Cannot connect to database $IDENTITY_DB");
}


function testif_logged_in()
{
	if (!isset($_COOKIE['mc'])) //wld 10 sep 06 strict type checking
	return false;
	$mc = $_COOKIE['mc'];

	$accid=""; $fn=""; $ln = ""; $email = ""; $idp = ""; $auth="";
	if ($mc!='')
	{
		$accid=""; $fn=""; $ln = ""; $email = ""; $idp = ""; $auth="";
		$props = explode(',',$mc);
		for ($i=0; $i<count($props); $i++) {
			list($prop,$val)= explode('=',$props[$i]);
			switch($prop)
			{
				case 'mcid': $accid=$val; break;
				case 'fn': $fn = $val; break;
				case 'ln': $ln = $val; break;
				case 'email'; $email = $val; break;
				case 'from'; $idp = stripslashes($val); break;
				case 'auth'; $auth = $val; break;
			}
		}
	}
	return array($accid,$fn,$ln,$email,$idp,$mc,$auth);
}

function aconfirm_member_access($accid,$gid){
    $db = DB::get();
    
	// does not return if this user is not a group member
	$rec = $db->first_row("Select * from groupmembers where memberaccid=? and groupinstanceid=?",array($accid,$gid));
	if($rec) 
    	return $rec;
	group_error_template($accid);
};

/**
 * Query for all members of specified group account id
 *
 * @param String $accid    group mcid
 * @return array of strings for each account id that is member of the group
 */
function q_group_members($accid) {   
  $db = DB::get();
    
  $query = "select gm.memberaccid
            from groupmembers gm, groupinstances gi
            where gm.groupinstanceid = gi.groupinstanceid
            and gi.accid = ?";
            
  $results = $db->query($query,array($accid));
  $members = array();
  foreach($results as $m) {
      $members[]=$m->memberaccid;
  }
  
  return $members;
}

/**
 * Hide the given patient in the worklist / patient list
 *
 * @return json encoded status
 */
function hide_patient($practiceId,$patientId) {
  $json = new Services_JSON();
  // Update patient
  error_log("hiding patient $patientId for practice $practiceId");
  try {
    pdo_execute("update practiceccrevents set ViewStatus='Hidden' where practiceid=? and PatientIdentifier=? and ViewStatus = 'Visible'",array($practiceId,$patientId));
  }
  catch(Exception $e) {
    error_log("Failed to hide patient: ".$e->getMessage());
    return $json->encode(array('status'=>'failed', 'error'=>'Unable to update ccr events'));
    exit;
  }
  return $json->encode(array('status'=>'ok'));
}

/**
 * Unhide a given specified patient in the worklist / patient list
 *
 * @return json encoded status
 */
function unhide_patient($practiceId,$patientId) {
  $json = new Services_JSON();
  $info = get_account_info();
  $practices = q_member_practices($info->accid);
  $practice = $practices[0];

  error_log("Hiding patient $patientId for practice ".$practiceId);

  // Update patient
  try {
    // Find most recent row
    $result = pdo_query("select * from practiceccrevents where ViewStatus = 'Hidden' and PatientIdentifier=? and practiceid=? order by CreationDateTime desc limit 1", $patientId, $practiceId);

    if(($result === false) || (count($result)==0))
      throw new Exception("Unable to locate hidden patient $patientId");

    // Unhide this record
    pdo_execute("update practiceccrevents set ViewStatus='Visible' where practiceid=? and PatientIdentifier=? and ViewStatus = 'Hidden' and ConfirmationCode=?",array($practiceId,$patientId,$result[0]->ConfirmationCode));
  }
  catch(Exception $e) {
    error_log("Failed to restore patient: ".$e->getMessage());
    return $json->encode(array('status'=>'failed', 'error'=>'Unable to update ccr events'));
    exit;
  }
  return $json->encode(array('status'=>'ok'));
}

function aconfirm_logged_in($fail_if_not=false)
{
	// $fail_if_not is optional string that forces complete death if not logged on

	if (isset($GLOBALS['__mckey']))
	{
		list ($sha1,$accid,$email)=explode('|',base64_decode($GLOBALS['__mckey'])); //if starting automagically
		return array($accid,'','',$email,'','');
	}
	else
	if(!isset($_COOKIE['mc'])) {
 		if ($fail_if_not) die($fail_if_not); 

 		header("Location: /acct/login.php");
		exit;
	}
	
	$info = get_account_info();

	return array($info->accid,$info->fn,$info->ln,$info->email,$info->idp,"",$info->auth);
}



function group_error_template($accid) {
  $tpl = new Template(resolveUp("widget.tpl.php"));
  $tpl->set("content",new Template(resolveUp("group_error.tpl.php")));
  $tpl->set("accid",$accid);
  echo $tpl->fetch();
  exit;
}



function get_user_interests() {
  if(!is_logged_in()) {
    return false;
  }

  $info = get_account_info();

  aconnect_db();

  $result = mysql_query("select interests from users where mcid = ".$info->accid);
  if($result !== false) {
    $allA = mysql_fetch_array($result);
    if($allA) {
      $all = $allA[0];
      // Got the interests, split them out
      return explode("|",$all);
    }
  }

  // no interests
  return array();
}

/**
 * Check that the given authentication token is correct format
 * (implemented) and is a valid auth token in the system (TODO).
 */
function validate_auth($auth) {
  if(preg_match("/^(token:){0,1}[a-z0-9]{40}$/",$auth)!==1)
    throw new Exception("Invalid authentication token");
}

/**
 * Returns the consents / permissions that the current logged in user
 * has wrt to the specified account in the form of a string of permission
 * values. eg:
 *
 *   get_user_permissions($patientId) =>  "RW"
 *
 * @param  16 digit mcid $toAccount
 * @return String
 */
function get_user_permissions($toAccount) {
    $info = get_account_info();
    if(!$info || !$info->auth)
      throw new Exception("Cannot query permissions:  user not logged in");

    return getPermissions($info->auth, $toAccount);
}
?>
