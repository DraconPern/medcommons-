<?php

require_once 'template.inc.php';
require_once 'session.inc.php';
require_once 'urls.inc.php';
require_once 'JSON.php';
require_once 'settings.php';
require_once 'utils.inc.php';
require_once 'DB.inc.php';

/*
 * Redirect (HTTP 302) to another location.
 *
 * Expands $url to absolute if necessary.
 */
function redirect($url) {
  if (strncasecmp($url, "http://", 7) != 0 &&
      strncasecmp($url, "https://", 8) != 0) {

    if(isset($_SERVER['HTTPS']) && $_SERVER['HTTPS'])
      $prefix = 'https://';
    else
      $prefix = 'http://';

    $prefix .= $_SERVER['HTTP_HOST'];

    if ($url[0] != '/')
      $prefix .= rtrim(dirname($_SERVER['PHP_SELF']), '/\\') . '/';

    $url = $prefix . $url;
  }
  
  header("Location: " . $url);

  exit;
}


/**
 * Utility class to help with loading, logging in and verifying Users
 */
class User {

  /**
   * Flag in state bitmask indicating that user has not confirmed their email address
   */
  public static $UNCONFIRMED = 4;

  /**
   * Flag in state bitmask indicating that user has supplied a photo
   */
  public static $HASPHOTO = 8;

  /**
   * Flag in state bitmask indicating that user has simtrak application enabled
   */
  public static $HASSIMTRAK = 16;

  /**
   * Flag in state bitmask indicating that user has 'dod' enabled (services menu, etc)
   */
  public static $HASDOD = 32;
  
  /**
   * Flag in state bitmask indicating that user has a custom logo
   */
  public static $HASLOGO = 64;

  public $mcid;
  public $email;
  public $first_name = '';
  public $last_name = '';

  /* source of identity */
  public $source_name = 'MedCommons';
  public $user_id;
  public $authToken;

  public $hasServices = false;
  public $hasVouchers = false;
  public $hasPhoto = false;
  public $hasSimtrak = false;
  public $hasDOD = false;
  public $hasLogo = false;

  public static function compute_password($mcid,$pw1) {
    $sha1 = strtoupper(hash('SHA1', 'medcommons.net' . $mcid . $pw1));
    return $sha1;
  }

  /**
   * Attempts to load details of user with mcid = mcid
   *
   * @throws Exception - if fails to load user, or user not found
   * @return - a User object with fields initialized
   */
  public static function load($mcid) {

    if(preg_match("/^[0-9]{16}$/",$mcid)!==1)
      throw new Exception("account id $mcid in unexpected format");

    $q="select u.*, gi.logo_url 
        from users u 
        left join groupinstances gi on gi.accid = u.active_group_accid 
        where u.mcid = $mcid";
    
    $db = DB::get();
    $obj = $db->first_row($q);
    if(!$obj) 
      throw new Exception("User $mcid not found");

   	$u = new User();
    $u->mcid = $mcid;
    $u->email = $obj->email;
    $u->first_name = $obj->first_name;
    $u->last_name = $obj->last_name;
    $u->acctype = $obj->acctype;
    $u->hasVouchers = ($obj->enable_vouchers == 1);
    $u->dashboard_mode = ($obj->active_group_accid == null ? 'patient' : 'group');
    $u->hasPhoto = ($obj->photoUrl != null) && ($obj->photoUrl != '');
    $u->hasSimtrak = ($obj->enable_simtrak != null) && ($obj->enable_simtrak != 0);
    $u->hasDOD = ($obj->enable_dod != null) && ($obj->enable_dod != 0);
    $u->active_group_accid = $obj->active_group_accid;
    
    dbg("Logo URL: ".$obj->logo_url);
    $u->hasLogo = $obj->active_group_accid && $obj->logo_url;
    $u->startparams = $obj->startparams;
    return $u;
  }
  
  /**
   * Return an array containing all roles for the specified users wrt to the specified 
   * group.
   * 
   * @param String $accid
   * @param String $groupAccountId
   */
  public static function get_user_roles($accid, $groupAccountId) {
      $db = DB::get();
      $rows = $db->query("select * from group_user_role gur where gur.accid = ? and gur.group_accid = ?", 
                         array($accid, $groupAccountId));
      $result = array();
      foreach($rows as $r) {
          $result[]=$r->role;
      }
      return $result;
  }
  
  /**
   * Attempts to locate an individual login for the specified authentication token
   * and returns the user record for that token, if it exists.  If the token
   * does not exist or is expired, returns NULL
   * <p>
   * Note that some authentication tokens are not linked to user records and this
   * function will return NULL for these.
   * 
   * @param String $auth       authentication token to search for
   */        
  public static function from_auth_token($auth) {
      
      $db = DB::get();
      return $db->first_row("select u.* 
                  from users u,
                  authentication_token at
                  where mcid = at.at_account_id
                  and at_priority = 'I'
                  and at_token = ?
                  and (at_expired_date_time is NULL or at_expired_date_time > NOW())",
                 array($auth));
                 
    // TODO:  throw special exception if expired
    
  }
  
  
  /**
   * Returns an object compatible with the form returned by 
   * get_account_info(). 
   */
  public function get_info() {
      $result = new stdClass;
      $result->accid = $this->mcid;
      $result->fn = $this->first_name;
      $result->ln = $this->last_name;
      $result->email = $this->email;
      $result->idp = false;
      return $result;
  }
  
  /**
   * MCID allocation using SOAP...
   */
  public static function get_mcid() {
      global $URL, $NS;
      $client = new SoapClient(null, array('location' => $URL, 'uri' => $NS));
      return $client->next_mcid();
  }
  
  /**
   * Insert a new user with the requested mcid and other details as provided.
   *
   * @param String $mcid
   * @param String $fn
   * @param String $ln
   * @param String $password
   * @param String $enableDOD
   */
  public static function insert($fn, $ln, $password, $enableDOD=false, $accountType="USER", $email = null, $groupAccountId = null) {
    global $IDENTITY_PDO, $IDENTITY_USER, $IDENTITY_PASS, $DB_SETTINGS;
    
    $mcid = User::get_mcid();
    $sha1 = $password ? User::compute_password($mcid, $password) : null;

    $db = new PDO($IDENTITY_PDO, $IDENTITY_USER, $IDENTITY_PASS, $DB_SETTINGS);

    $stmt = $db->prepare("INSERT INTO users (".
             " mcid, first_name, last_name,".
             " sha1, server_id, acctype, enable_dod, active_group_accid, email".
             ") VALUES (".
             " :mcid, :fn, :ln," .
             " :sha1, 1, :type, :dod, :group, :email)");

    if(!$stmt->execute(array("mcid" => $mcid, 
                              "fn" => $fn, 
                              "ln" => $ln, 
                              "sha1" => $sha1, 
                              "type" => $accountType,
                              "dod" => $enableDOD?1:0, 
                              "group" => $groupAccountId,
                              "email" => $email
                             )
                        )
       ) {
          $e = $stmt->errorInfo();
          throw new Exception("Unable to insert new user: ".$e[2]);
    }
    
    return $mcid;
  }

  /**
   * Resolve the given email address to an mcid and returns
   * the database row corresponding to that user.
   * <p>
   * If the email address is not associated with an mcid
   * on this appliance then returns false.
   * 
   * @param String $mcid
   * @param String $password
   */
  public static function resolveEmail($email,$password) {
      $db = DB::get();
      $users = $db->query("SELECT users.mcid, users.sha1,
                           users.first_name, users.last_name,
                           users.email, users.acctype
                           FROM users
                           WHERE users.email = ?
                           ORDER BY users.since desc", array($email));
                          
      foreach($users as $u) {
          $sha1 = User::compute_password($u->mcid, $password);
          dbg("Computed password hash for ".$u->mcid." on password ".$password." = ".$sha1);
          if($u->sha1 == $sha1) 
              return $u;
      }
      return false;
  }

  /**
   * Sets the cookie(s) that identify this logged-in user
   * to other pages, then, if provided, redirects to the specified url.
   */
  public function login($url = null) {
    global $SECRET, $acCookieDomain;

    if(isset($this->mcid) && $this->mcid &&  (!isset($this->authToken) || ($this->authToken == null) || ($this->authToken == ''))) {
      dbg("auth not set:  creating new auth for account $this->mcid");
      $this->authToken = get_authentication_token($this->mcid);
      dbg("Got new auth $this->authToken");
    }

    $mc = 'mcid=' . $this->mcid;
    $mc .= ',from=' . $this->source_name;

    if ($this->first_name)
      $mc .= ',fn=' . $this->first_name;
    if ($this->last_name)
      $mc .= ',ln=' . $this->last_name;
    $mc .= ',email=' . $this->email;
    $state = 0;
    if($this->hasServices)
      $state |= 1;
    if($this->hasVouchers)
      $state |= 2;
    if(($this->email == null || $this->email == "") && ($this->acctype !== "VOUCHER")) 
      $state |= User::$UNCONFIRMED;
    if($this->hasPhoto) 
      $state |= User::$HASPHOTO;
    if($this->hasSimtrak)
      $state |= User::$HASSIMTRAK;
    if($this->hasDOD)
      $state |= User::$HASDOD;
    if($this->hasLogo)
      $state |= User::$HASLOGO;

    dbg("state = $state , email = ".$this->email);

    $mc .= ',s=' . $state;

    $mcenc = 'mcid=' . $this->mcid;
    $mcenc .= '&from=' . $this->source_name;

    if ($this->first_name)
      $mcenc .= '&fn=' . $this->first_name;

    if ($this->last_name)
      $mcenc .= '&ln=' . $this->last_name;

    $mcenc .= '&email=' . $this->email;
    $mcenc .= '&auth=' . $this->authToken;

    $mcenc = encrypt_urlsafe_base64($mcenc, $SECRET);

    $mc .= ',enc=' . $mcenc;

    $cookie = 'Set-Cookie: mc=' . rawurlencode($mc) . '; path=/';

    $domain = $acCookieDomain ?  '; domain=' . $acCookieDomain : '';

    $cookie .= $domain;
    
    header($cookie);

    $_COOKIE['mc'] = $mc;
    
    // Set the session timeout cookie
    header('Set-Cookie: mcses='.time()."; path=/", false);
    
    if($url) {
      redirect($url);
      exit;
    }
  }
}

function show_login($next) {
  header('Location: /acct/login.php?prompt=login_required');
  exit;
}

/**
 * Gets the MCID of the logged-in user.
 *
 * Returns FALSE if not logged in.
 */
function get_login_mcid() {
  global $SECRET;

  if (isset($_COOKIE['mc'])) {
    parse_str(str_replace(',', '&', $_COOKIE['mc']), $values);

    if (isset($values['enc'])) {
      parse_str(decrypt_urlsafe_base64($values['enc'], $SECRET), $enc_values);
      return $enc_values['mcid'];
    }
  }

  return False;
}

/**
 * Tests if the user is logged in.
 *
 * If the user is logged in, returns the MCID.
 *
 * Otherwise, redirects to the login page.
 */
function login_required($page) {
  require "cluster.inc.php";
  $mcid = get_login_mcid();

  if ($mcid) return $mcid;

  /*
   * ELB/Cluster ``Mode' (nVa 010610)
   */
  error_log("ELB:LOI:$acClusterMode");
  if (!$acClusterMode) {
    error_log("ELB:LOI:standalone");
    show_login($page);
  }
  error_log("LOI:ELB");
}

/**
 * Calls the secure server to create an authentication token for the
 * specified user.  The token is returned.
 *
 * @param mcid - account id for which the token should be created, 
 *               may be array of accounts.  Each account id is queried
 *               to determine any groups it is a member of and the token
 *               is automatically enabled for these groups too.
 * @param u  - optional additional information about the user.  If supplied then will be
 *             recorded with the auth token for later reference.  Used when logging in from 
 *             3rd party IDPs (eg. Facebook).
 */
function get_authentication_token($accts, $u = null) {
  global $IDENTITY_PDO, $IDENTITY_USER, $IDENTITY_PASS, $DB_SETTINGS;

  if(!is_array($accts)) {
    $accts = array($accts);
  }

  $json = new Services_JSON();

  $db = new PDO($IDENTITY_PDO, $IDENTITY_USER, $IDENTITY_PASS,
		$DB_SETTINGS);

  $stmt = $db->prepare("SELECT gi.accid ".
		       "FROM groupmembers gm, groupinstances gi ".
		       "WHERE gm.memberaccid = :mcid ".
		       "AND gm.groupinstanceid = gi.groupinstanceid");

  $groups = array();
  foreach($accts as $mcid) {
    if($stmt && $stmt->execute(array("mcid" => $mcid))) {
      // Build array of groups
      $groups[]=$mcid;
      while ($group = $stmt->fetch())
        $groups[]="g:".$group[0];
    }
    else {
      $e = $stmt->errorInfo();
      throw new SystemFailure("Unable to query for groups for user $mcid ".$e[2]);
    }
  }

  $authServiceUrl = gpath('Commons_Url')."/ws/createAuthenticationToken.php?accountIds=".implode(",",$groups);
  if($u != null) {
    $authServiceUrl .= "&fn=".urlencode($u->first_name)."&ln=".urlencode($u->last_name);
    if(isset($u->source_name) && $u->source_name) {
      $authServiceUrl .= "&idp=".urlencode($u->source_name);
      $authServiceUrl .= "&idp_id=".urlencode($u->user_id);
    }
  }
  
  dbg("creating token using url ".$authServiceUrl);
  $jsonResult = get_url($authServiceUrl);
  
  dbg("auth token result: $jsonResult");
  $authResult = $json->decode($jsonResult);
  if(!$authResult) 
    throw new SystemFailure("Authentication system failure.","Unable to parse JSON received from call:  ".$jsonResult);
      
  if($authResult->status != "ok") 
    throw new SystemFailure("Authentication service failure",$authResult->message);
      
  return $authResult->result;
}


$ID_IS_BLANK = '';
$ID_IS_MCID = 'mcid';
$ID_IS_TRACKING_NUMBER = 'tracking number';
$ID_IS_PIN = 'pin';
$ID_IS_EMAIL_ADDRESS = 'email address';
$ID_IS_OPENID_URL = 'openid url';
$ID_IS_PHONE = 'phone';

$MCID_RE = '/^([0-9]{4}[\.,-_ ]?){3}[0-9]{4}$/';
$TRACKING_NUMBER_RE = '/^([0-9]{4}[\.,-_ ]?){2}[0-9]{4}$/';
$PIN_RE = '/^([0-9][\.,-_ ]?){5}$/';
$EMAIL_ADDRESS_RE = '!^[^/]+\@!';
$PHONE_NUMBER_RE = '/^\(?[0-9]{3}\)?[- \.]?[0-9]{3}[- \.]?[0-9]{4}$/';

function id_type($q) {
  global $ID_IS_BLANK, $ID_IS_MCID, $ID_IS_TRACKING_NUMBER, $ID_IS_PIN,
         $ID_IS_EMAIL_ADDRESS, $ID_IS_OPENID_URL, $ID_IS_PHONE;

  if ($q == '') return $ID_IS_BLANK;

  if (ctype_digit($q[0])) {
    if (is_mcid($q)) return $ID_IS_MCID;
    if (is_tracking_number($q)) return $ID_IS_TRACKING_NUMBER;
    if (is_pin($q)) return $ID_IS_PIN;
  }
  if (is_phone_number($q)) return $ID_IS_PHONE;
  if (is_email_address($q)) return $ID_IS_EMAIL_ADDRESS;

  return $ID_IS_OPENID_URL;
}

function is_mcid($q) {
  global $MCID_RE;
  return strlen($q) >= 16 && ctype_digit($q[0]) && preg_match($MCID_RE, $q);
}

function is_tracking_number($q) {
  global $TRACKING_NUMBER_RE;
  return strlen($q) >= 12 && ctype_digit($q[0]) && preg_match($TRACKING_NUMBER_RE, $q);
}

function is_pin($q) {
  global $PIN_RE;
  return strlen($q) >= 5 && ctype_digit($q[0]) && preg_match($PIN_RE, $q);
}

function is_email_address($q) {
  global $EMAIL_ADDRESS_RE;
  return $q != '' && preg_match($EMAIL_ADDRESS_RE, $q);
}

function is_phone_number($q) {
  global $PHONE_NUMBER_RE;
  dbg("Checking value $q against regex $PHONE_NUMBER_RE");

  if(preg_match($PHONE_NUMBER_RE, $q)) {
    dbg("yes match!");
    return true;
  }
  else {
    dbg("no match!");
    return false;
  }
}

function is_openid_url($q) {
  global $ID_IS_OPENID_URL;
  return id_type($q) == $ID_IS_OPENID_URL;
}

// donner -- 8 dec 2009 -- one big table and new code added to freeze out users who repetitively fail input validation
define ('MAX_FAILURES', 3);
define ('FREEZEOUT_TIME',300); // in seconds

/* 
 * login_trakker table was added to mcx
 * 
 * To be in this table a user must either be 'frozen' or on the way to freezing.
 * A properly logged on user is NEVER in this table
 */
function exceeds_deltatime ($now,$last)
{
	// returns boolean
	return( ($now-$last) > FREEZEOUT_TIME ) ;
}

function isfrozen ($anykey)
{
	// returns true if this key is still frozen because the timeout hasn't expired
	$db = DB::get();
	
	$sql = <<<EOF
SELECT logintrakker.lasttime, logintrakker.failurecounter
FROM   logintrakker
WHERE  logintrakker.userinput = :input
EOF;

	$row = $db->first_row($sql, array("input" => $anykey));
	// assert - there is at most one record with this key
	if ($row) {
		$count = $row->failurecounter;
		$last = $row->lasttime;
	     
		if ($count < MAX_FAILURES) return false;
		if (exceeds_deltatime(time(),$last)) return false;
		return true;
	}
	// if not found there's no problemo
	return false;
}

function remove_trakking ($anykey)
{
	// donner -- dec 09 2009 --  delete userfrom trakking table
	$db = DB::get();
	
	$sql = <<<EOF
DELETE FROM logintrakker 
WHERE  logintrakker.userinput = :input
EOF;
	$result = $db->execute($sql, array("input" => $anykey));
}

function track_login_failure($input)
{
	$db = DB::get();

	$sql = <<<EOF
SELECT logintrakker.lasttime, logintrakker.failurecounter
FROM   logintrakker
WHERE  logintrakker.userinput = :input
EOF;

	$count = 0;  // if not found then its no problem
	$last = 0;

	// assert - there is at most one record with this key
	$row = $db->first_row($sql, array("input" => $input));
	if($row) {
		$count = $row->failurecounter;
		$last = $row->lasttime;
	}
	
	$now = time(); // get current unix time

	if (++$count>MAX_FAILURES) // this ensures no counts of 0 in the table
	{
		// if it's been a long time, its ok so reset the count back to 1
		if(exceeds_deltatime ($now,$last))	
		{
			$sql = <<<EOF
UPDATE logintrakker set logintrakker.lasttime = :timenow, logintrakker.failurecounter=:counter
WHERE  logintrakker.userinput = :input
EOF;
			$db->execute($sql, array("input" => $input, "timenow" => $now, "counter"=>1));
		}
	}
	else
	{
        // havent yet hit max failures
		//store the now bumpedup failure counter and reset the lasttime to now
		if ($count==1)
		$sql = <<<EOF
INSERT into logintrakker set logintrakker.lasttime = :timenow, logintrakker.failurecounter=:counter,
                             logintrakker.userinput = :input
EOF;

		else
		$sql = <<<EOF
UPDATE logintrakker set logintrakker.lasttime = :timenow, logintrakker.failurecounter=:counter
WHERE  logintrakker.userinput = :input
EOF;
		$result = $db->execute($sql, array("input" => $input, "timenow" => $now, "counter"=>$count));
	}
}

?>
