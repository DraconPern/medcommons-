<?php
/**
 * RLS / Worklist / Patient List Query Logic
 *
 * This page is the main driver that performs queries to display the patient list
 * (aka worklist).  It servers both AJAX dynamic updates and also the initial rendering
 * that displays the whole page.  Content is rendered using the template rlstable.tpl.php.
 *
 * Originally written by bdonner, updated and maintained by ssadedin@medcommons.net.
 */
require_once "dbparamsidentity.inc.php";
require_once "template.inc.php";
require_once "utils.inc.php";
require_once "alib.inc.php";
require_once "JSON.php";
require_once "DB.inc.php";
require_once "rls.inc.php";
nocache();

/**
 * Get clean value from request
 */
function strip($x) {
  return isset($_REQUEST[$x]) ?  (get_magic_quotes_gpc() ? stripslashes($_REQUEST[$x]) : $_REQUEST[$x]) : "";
}

function cleanreq ($x) { if (isset($_REQUEST[$x])) return $_REQUEST[$x]; else return false;}

/**
 * Fail with given error message
 */
function fail($msg) {
  echo "<p>Error:  $msg</p>";
  error_log("Query failed:  ".mysql_error());
  exit;
}


$db = DB::get();

// these are the basic query parameter arguments that are passed around
//
//
$pfn = cleanreq('PatientFamilyName');
$pgn = cleanreq('PatientGivenName');
$pid= cleanreq('PatientIdentifier');
$pis= cleanreq('PatientIdentifierSource');
$psx = cleanreq('PatientSex');
$pag = cleanreq('PatientAge');
$spid  = cleanreq('SenderProviderId');
$rpid  = cleanreq('ReceiverProviderId');
$dob  = cleanreq('DOB');
$cc = cleanreq('ConfirmationCode');
// these are not query parameters, but are passed around
$rs = cleanreq('RegistrySecret');
$guid = cleanreq('Guid');
$purp = cleanreq('Purpose');
$cxpserv = cleanreq('CXPServerURL');
$cxpvendor = cleanreq('CXPServerVendor');
$viewerurl = cleanreq('ViewerURL');
$comment = cleanreq('Comment');
// these params control the formatting of output
$int = cleanreq('int'); // if non-zero, ajax'd dynamic updates
$st = cleanreq('st');
$ti = cleanreq('ti');
$limit = cleanreq('limit');
$logo = cleanreq('logo');
$page = cleanreq('page');

$showHidden = false;

if(req('showHidden','false')=='true')
  $showHidden = true;

if($page == '')
  $page = 1;

// this multiplexes the group - wld 072506
//$gid = cleanreq('gid'); // retired in favor of pgid

// this is a hacked version of glib.inc.php because I cant figure out the nesting structure of dbparams, etc.

$GLOBALS['RLS_Name'] = "MedCommons Builtin Registry";
$GLOBALS['RLS_Version'] = "0.2";
$GLOBALS['RLS_DB']="practiceccrevents";

if(!isset($no_login_necessary)) 
	list($accid,$fn,$ln,$email,$idp,$cookie,$auth) = aconfirm_logged_in (); // does not return if not logged on
else
    $accid = "";

// let this entire file be 'required_once' by setting the $__practicegroupid
if (isset($__practicegroupid)) $practicegroupid = $__practicegroupid; else

// otherwise lets try this
$practicegroupid = $_REQUEST['pid']; //specifies the practicegroup

$select = "SELECT p.providergroupid,p.practicename,gi.worklist_limit, gi.accid
           from practice p, groupinstances gi
           where p.practiceid=$practicegroupid
             and gi.groupinstanceid = p.providergroupid";

$result = $db->first_row($select);

$providersid = $result->providergroupid;  // this is what we care about
$groupAccountId = $result->accid;

if($limit === false) {
  if($result->worklist_limit != null) {
    $limit = $result->worklist_limit;
  }
}

if(($limit == false) || ($limit == null)) {
  $limit = 6;
}

$practicename = htmlspecialchars($result->practicename);

if(!isset($no_login_necessary)) 
	aconfirm_member_access($accid,$providersid); // does not return if this user is not a group member
	
$gid = $practicegroupid;

$int = 10; // now integrating the ajax stuff, all calls should come right back here

$lasttime = cleanreq('lt'); // wld if missing, then its the first call, otherwise it is an ajax refresh

// build WHERE clause for select statement based on the arguments
$where = ""; $wc = 1;

aconnect_db();

// Search criteria
$searchPatientName = mysql_real_escape_string(strip('searchPatientName'));
if($searchPatientName!="") {
  $showHidden = true;
  $wc++;
  $names = explode(",",$searchPatientName);
  if(count($names) == 1) 
    $where .= " AND (e.PatientFamilyName like '%$searchPatientName%' OR e.PatientGivenName like '%$searchPatientName%')";
  else
    $where .= " AND (e.PatientFamilyName like '%".trim($names[1])."%' AND e.PatientGivenName like '%".trim($names[0])."%')";
}

$searchLastUpdate = req('searchLastUpdate',"");
if(($searchLastUpdate!="") && ($searchLastUpdate!="all")) {
  $showHidden = true;
  $day = 3600*24;
  $wc++;
  if($searchLastUpdate=="week") {
    $where .= " AND (e.CreationDateTime > ".(time()-$day*7).")";
  }
  else
  if($searchLastUpdate=="month") {
    $where .= " AND (e.CreationDateTime > ".(time()-$day*30).")";
  }
  else 
  if($searchLastUpdate=="year") {
    $where .= " AND (e.CreationDateTime > ".(time()-$day*365).")";
  }
}

$searchPurpose = req('searchPurpose',"");
if($searchPurpose!="") {
  $showHidden = true;
  $wc++;
  $where .= " AND (e.Purpose like '%$searchPurpose%')";
}
$searchStatus = req('searchStatus',"");
if($searchStatus!="") {
  $showHidden = true;
  $wc++;
  $where .= " AND (e.Status like '%$searchStatus%')";
}

$viewStatusClause = " AND e.ViewStatus = 'Visible' ";
if($showHidden) {
  $viewStatusClause = " AND e.ViewStatus in ('Visible','Hidden')";
  // error_log("## show hidden");
}
// else 
//  error_log("## no hidden");

if ($wc!=0) $whereclause = $where; else $whereclause='';


$isajax = ($int!=0);
$mb = $GLOBALS['RLS_Name'];
$start = ($page-1) * $limit;

// error_log($whereclause);

/*
 * Get count of all rows (visible and non-visible)
 */
$allCountSql="SELECT count(*) as cnt FROM practiceccrevents e, users u 
              WHERE e.practiceid = '$gid' $whereclause AND e.ViewStatus in ('Visible','Hidden')
              AND u.mcid = e.PatientIdentifier
              AND u.acctype <> 'EXPIRE_IMMEDIATE'
              AND (u.expiration_date is NULL OR u.expiration_date > NOW())";
$row = $db->first_row($allCountSql);
$allCount = $row->cnt;

/*
 * Get count of Visible rows only
 */
$countSql=$allCountSql . $viewStatusClause;
$result = $db->first_row($countSql);
$count = $result->cnt;

/*
 * Main query - retrieve actual data
 */
$rows = query_patient_list($gid, $limit, $start, $whereclause, $viewStatusClause);
$rowCount = count($rows);
dbg("rowCount = $rowCount");
$pages = ceil($count/$limit);

$pageLinks = "";
if($pages > 1) {
$pageLinks = "Page ";
  for($p=0; $p<$pages; $p++) {
    $pn = $p + 1;
    if($pn == $page)
    $pageLinks.="$pn&nbsp;";
    else
    $pageLinks.="<a href='javascript:page($pn);'>$pn</a>&nbsp;";
  }
}

$displayedCount = $count < $limit ? $count : $limit;

// Query status values
$results = $db->query("select value from mcproperties where property = 'acAccountStatus'");

$statusValues = count($results)>0 ? $results[0]->value : "";

$patientIds = array();

foreach($rows as $l) {
  if($l->PatientIdentifier) {
    $l->gwUrl = allocate_gateway($l->PatientIdentifier);
    $patientIds[]=$l->PatientIdentifier;
  }
}

// Get transfer statuses
if(count($patientIds)>0) {
  $ds =
    pdo_query("select ts.*, tm.tm_id, tm.tm_message, UNIX_TIMESTAMP(ts.ts_create_date_time) as ts_crt
               from transfer_state ts
               left join transfer_message tm on ts.ts_key = tm.tm_transfer_key
               where ts.ts_account_id in (".implode(",",$patientIds).")
               order by ts.ts_create_date_time desc", array());
}
else
  $ds = array();

dbg("got ".count($ds). " transfer state values for ".count($patientIds)." patients");

$dicomStatus = array();
$tsKeys = array();
foreach($ds as $s) {
  if(!isset($dicomStatus[$s->ts_account_id])) {
    $dicomStatus[$s->ts_account_id] = $s;
    $dicomStatus[$s->ts_account_id]->messages = array();
    if($s->tm_message)
        $dicomStatus[$s->ts_account_id]->messages[]=$s->tm_message;
  }
  else
  if(($dicomStatus[$s->ts_account_id]->ts_key == $s->ts_key) && $s->tm_message) {
    $dicomStatus[$s->ts_account_id]->messages[]=$s->tm_message;
  }
}

// Get global messages
$messages = $db->query("select *, UNIX_TIMESTAMP(tm.tm_create_date_time) as timestamp from transfer_message tm
                       where tm.tm_account_id = ? and (NOW() - tm.tm_create_date_time) < 36000
                       order by tm.tm_create_date_time desc",
                      array($groupAccountId));
$messageTimestamp = (count($messages)>0) ? $messages[0]->timestamp : 0;

dbg("got ".count($messages)." global messages");

$json = new Services_JSON();

if(isset($rls_template))
    $tpl = $rls_template;
else
    $tpl = new Template(resolveUp('rlstable.tpl.php'));
    
$tpl->set("accid",$accid)
	->set("auth",$auth)
	->set("lasttime",$lasttime)
	->esc("mb",$mb)
	->set("rows",$rows)
	->set("rowCount",$rowCount)
	->set("visibleCount",$count)
	->set("displayedCount",$displayedCount)
	->set("allCount",$allCount)
	->set("practicename", $practicename)
	->set("pageLinks",$pageLinks)
	->set("statusValues",$statusValues)
	->set("showHidden",$showHidden)
	->set("transferState",$dicomStatus)
	->set("groupAccountId",$groupAccountId)
	->set("messageTimestamp",$messageTimestamp)
	->set("globalMessages",$json->encode($messages));
	
// If the format is atom, stop here and send it back
if(req("fmt") === "atom") {
    echo $tpl->fetch("patient_list_atom.tpl.php");
    exit;
}

if(isset($rls_template)) {
    echo $tpl->fetch();
    return;
}


$content = $tpl->fetch();
$synch = time();
$newUrl = new_ccr_url($accid);
$gwUrl = allocate_gateway($accid);

if(isset($_REQUEST['widget'])) {
  $tpl = template('rlswidget.tpl.php')
      ->set("content", $content)
      ->set("auth",$auth)
      ->set("pid", $practicegroupid)
      ->set("limit", $limit)
      ->set("practicename", $practicename)
      ->set("newUrl", $newUrl)
      ->set("gwUrl", $gwUrl)
      ->set("statusValues",$statusValues)
      ->set("accid",$accid)
      ->set("allCount",$allCount)
      ->set("visibleCount",$count)
      ->set("displayedCount",$displayedCount)
      ->set("rowCount",$rowCount)
      ->set("searchPatientName",$searchPatientName)
      ->set("searchLastUpdate",$searchLastUpdate)
      ->set("searchPurpose",$searchPurpose)
      ->set("searchStatus",$searchStatus)
      ->set("showHidden",$showHidden)
      ->set("dicomStatus",$dicomStatus)
      ->set("messageTimestamp",$messageTimestamp)
      ->set("groupAccountId",$groupAccountId)
      ->set("globalMessages",$json->encode($messages));
      
  $tpl->set("embed", isset($embed) && $embed);
  echo $tpl->fetch();
}
else {
 echo "<table id='rlsUpdate'><tbody>$content</tbody></table>";
}

?>
