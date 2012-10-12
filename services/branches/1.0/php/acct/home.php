<?
/**
 * $Id: home.php 8329 2010-01-21 21:12:05Z ssadedin $
 *
 * Home Page Initialization Script
 *
 * Checks the user's login status and gathers various information before
 * selecting an appropriate template and rendering it.
 *
 * Supports display of messages that are read from template files. A 
 * message is specified by passing parameter "msg" with the name of the
 * template file.  The actual file loaded will be this value + "_msg.tpl.php".
 *
 * Note: the template to use can be passed in the 'template' parameter.
 *
 * @author ssadedin@medcommons.net
 */
// removed background-color='transparent' from iframe
header("Cache-Control: no-store, no-cache, must-revalidate");
header("Pragma: no-cache");
require_once "template.inc.php";
require_once "utils.inc.php";
require_once "alib.inc.php";
require_once 'settings.php';

try {
  $t = new Template();
  $loggedIn = is_logged_in();
  $info = $loggedIn ? get_validated_account_info() : false;
  $interests = $loggedIn ? get_user_interests() : false;
  $t->set("interests",$interests);
  $sd = isset($GLOBALS['Script_Domain']) ? $GLOBALS['Script_Domain'] : "";
  $t->set("sd",$sd);
  $isPracticeMember = $loggedIn ? is_practice_member($info->accid) : false;

  if($isPracticeMember) {
    $practices = q_member_practices($info->accid);
    // Find appropriate gateway for creating new accounts
    $gwUrl = allocate_gateway($info->accid);
    $gwUrlParts = parse_url($gwUrl);
    if(!isset($gwUrlParts['port'])) { // Avoid problem if gateway url does not have explicit port
      $gwUrlParts['port'] = ($gwUrlParts['scheme']=='https')?'443':'80';
    }
    $t->set("gwUrlParts",$gwUrlParts);
    $t->set("gwUrl",$gwUrl);
  }
  $enableCombinedFiles = (isset($GLOBALS['use_combined_files']) && ($GLOBALS['use_combined_files']==true));
  $t->set("info",$info);
  $cccrGuid = $info ? getCurrentCCRGuid($info->accid) : false;
  $t->set("cccrGuid",$cccrGuid);

  // Is there a currently active gateway ? 
  $activeGateway = get_current_gateway_url();
  $t->set("activeGateway", $activeGateway);

  if($loggedIn && (($info->email == null) || ($info->email == "")))
    $unconfirmed = true;
  else
    $unconfirmed = false;

  dbg("unconfirmed = ".($unconfirmed?"yes":"no"));

  if($unconfirmed && ($info->acctype != 'VOUCHER')) {
    $t->set('msg',template("confirm_email_msg.tpl.php"));
  }

  $msg = req('msg');
  if($msg) {
    $t->set('msg',template($msg."_msg.tpl.php"));
  }

  $t->set("embed",false);
  $homeTemplate = "home.tpl.php";
  if(req('embed')) {
	  $homeTemplate = "widget.tpl.php";
	  if(!$loggedIn && req('openid_url')) {
	      
	      // If they already went to the OpenID provider then don't 
	      // send them on an infinite loop of trying again
	      if(req('openid_return')) {
	          header("Location: /acct/login.php");
	          exit;
	      }
		      
	      $next = $_SERVER['REQUEST_URI'];
	      if(strpos($next, "?")===FALSE)
	        $next .= "?";
	      else 
	        $next .= "&";
	      $next .= "openid_return=true"; 
	      header("Location: login.php?openid_url=".req('openid_url')."&next=".urlencode($next));
	      exit;
	  }
	  $t->set("embed",true);
  }
  

  $t->set("isPracticeMember",$isPracticeMember);

  // Set account type panel to appropriate type for the kind of user logging in
  if($isPracticeMember && $info->practice) {
    $t->set("accountTypePanel", $t->fetch("practice_member_account_panel.tpl.php"));
    $t->set("title",$info->practice->practicename);
    if($info->enable_vouchers) {
      $patientCount = pdo_first_row("select 1 as cnt from practiceccrevents where practiceid = ? limit 1",array($info->practice->practiceid));
      $t->set("patientCount", $patientCount ? 1 : 0);
    }
  }
  else
  if($loggedIn && ($info->acctype == 'VOUCHER')) {

    $cpn = pdo_query("select couponum, auth, expirationdate from modcoupons where mcid = ?",$info->accid);
    if(count($cpn)==0)
      throw new Exception("Unable to locate coupon for account $info->accid");

    $t->set("voucherExpirationDate",$cpn[0]->expirationdate);
    $t->set("voucherAuth",$cpn[0]->auth);
    $t->set("voucherCouponum",$cpn[0]->couponum);
    $t->set("accountTypePanel",$t->fetch("voucher_account_panel.tpl.php"));
  }
  else
    $t->set("accountTypePanel","");
      
    
  // If template provided, use that - note we only support specific pre-configured options
  if($loggedIn && ($info->acctype != 'VOUCHER') && $unconfirmed) {
    $t->set("content", $t->fetch("unconfirmed_account.tpl.php"));
    echo $t->fetch($homeTemplate);
  }
  else
  if(req('template') == 'pdframe') {
    $t->set("content",$t->fetch("patientDetailsFrame.tpl.php"));
    echo $t->fetch("widget.tpl.php");
  }
  elseif($loggedIn && !$cccrGuid && !$info->enable_vouchers && !$info->practice) { // Show intro page
    $t->set("content",$t->fetch("intro.tpl.php"));
    echo $t->fetch($homeTemplate);
  }
  elseif($loggedIn) {
    $templateName = "loggedInHome.tpl.php";
    $t->set("content",$t->fetch($templateName));
    echo $t->fetch($homeTemplate);
  }
  else {
    $t->esc('openid_url', '');
    $t->set('password', False);
    echo $t->fetch("login.tpl.php");
  }
}
catch(Exception $e) {
  error_page("A problem occurred while loading your home page.",$e);
}
?>
