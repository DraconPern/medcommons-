<?php
require_once "wslibdb.inc.php";
require_once "../alib.inc.php";
require_once "utils.inc.php";
require_once "mc.inc.php";
require_once "AccountSettings.inc.php";

/**
 * querySettingsWs 
 *
 * Returns account settings for the requested account.
 *
 * Inputs:
 *    accid - account id to check
 *
 * @author ssadedin@medcommons.net
 */
class querySettingsWs extends dbrestws {
    
  function verify_caller() {
    try {
        verify_local_call();
    }
    catch(Exception $ex) {
        // If we cannot verify that this is a local call,
        // allow it to come from a remote gateway by 
        // delegating to parent to check node key
        parent::verify_caller();
    }
  }
  
  function xmlbody(){
    $db = DB::get();

    // This was enabled but it unfortunately causes some issues
    // so now disabled until we sort out some better logic for 
    // managing the settings on the gateway side
    // header("Cache-Control: max-age=90");

    // pick up and clean out inputs from the incoming args
    $accid = req('accid');

    if(!is_valid_mcid($accid,true))
      $this->xmlend("Invalid account id format: $accid");

    // get basic account info
    $user = $db->first_row(
      "select u.mcid as accid, u.first_name, u.last_name,
        u.email, u.photoUrl, u.enable_vouchers, u.active_group_accid,
        u.amazon_user_token, u.amazon_product_token, u.amazon_pid, u.tip_state,
        p.practiceRlsUrl, p.providergroupid, p.practicename, p.practiceid,
        gi.createdatetime as group_create_date_time,
        uds.*
      from users u
      left join users_dicom_settings uds on uds.uds_accid = u.mcid
      left join groupinstances gi on gi.accid = u.active_group_accid
      left join practice p on p.providergroupid = gi.groupinstanceid
      where mcid = ?",array($accid));

    $settings = $user ? AccountSettings::load($user) : new AccountSettings();
    if($settings->coupon) {
      $voucher_details = $this->xmfield("voucherId",  $settings->coupon->voucherid). 
                           $this->xmfield("expirationDate",  $settings->coupon->expirationdate). 
                           $this->xmfield("otpHash",  sha1($settings->coupon->otp)). 
                           $this->xmfield("status",  $settings->coupon->status). 
                           $this->xmfield("providerAccId",  $settings->coupon->providerAccId). 
                           $this->xmfield("couponNum",  $settings->coupon->couponum); 
    }

    $logicalDocuments = "";
    foreach($settings->logicalDocuments as $doc) {
      $logicalDocuments.=$this->xmnest("document",$this->xmfield("type",$doc['type']).$this->xmfield("guid",$doc['guid']));
    }

    $todir=gpath('Accounts_Url')."/ws/queryToDir.php?ctx=".$accid;

    if(isset($GLOBALS['Directory_Url'])) {
      $todir=$GLOBALS['Directory_Url']."/ws/queryToDir.php?ctx=".$accid;
    }

    $appsXML = "<applications>";
    foreach($settings->applications as $app) {
      $appsXML .= "<app><code>".xmlentities($app->ea_code)."</code><key>".xmlentities($app->ea_key)."</key><name>".xmlentities($app->ea_name)."</name></app>";
    }
    $appsXML.="</applications>\n";

    $this->xm(
      $this->xmnest("outputs",
        $this->xmfield("status","ok").
        $this->xmfield("groupInstanceId",$settings->groupinstanceid). // For now first group only TODO: change protocol to return all groups
        $this->xmfield("groupAccountId",$settings->accid). // For now first group only TODO: change protocol to return all groups
        $this->xmfield("groupName",$settings->name). 
        $this->xmfield("groupCreateDateTime",isset($settings->createdatetime) ? $settings->createdatetime : ""). 
        $this->xmfield("firstName",$user?$user->first_name:""). 
        $this->xmfield("lastName",$user?$user->last_name:""). 
        $this->xmfield("email",$user?$user->email:""). 
        $this->xmfield("photoUrl",$user?$user->photoUrl:""). 
        $this->xmfield("amazonUserToken",$user?$user->amazon_user_token:""). 
        $this->xmfield("amazonProductToken",$user?$user->amazon_product_token:""). 
        $this->xmfield("amazonPid",$user?$user->amazon_pid:""). 
        $this->xmfield("photoUrl",$user?$user->photoUrl:""). 
        $this->xmfield("vouchersEnabled",($user && $user->enable_vouchers)?"true":"false"). 
        $this->xmfield("practiceId",$user?$user->practiceid:"").
        $this->xmfield("registry",$settings->practiceRlsUrl).
        $this->xmfield("statusValues",$settings->statusValues).
        $this->xmfield("directory","$todir").
        $this->xmfield("tipState",$user?$user->tip_state:"0").
        $this->xmfield("dicomAeTitle",isset($user->uds_aetitle)?$user->uds_aetitle:"").
        $this->xmfield("dicomIpAddress",isset($user->uds_host)?$user->uds_host:"").
        $this->xmfield("dicomPort",isset($user->uds_port)?$user->uds_port:"").
        $this->xmfield("creationRights", $this->xmfield("accountId",$settings->accid)). // send back group account for access rights
        (isset($voucher_details) ?  $this->xmnest("voucher", $voucher_details) : "").
        $this->xmnest("documents",$logicalDocuments).
        $appsXML
      )
    );
	}
}

// main
$x = new querySettingsWs();
$x->handlews("querySettings_Response");
?>
