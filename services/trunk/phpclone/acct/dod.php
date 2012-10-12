<?
/**
 * Displays a form to allow a user to send imaging data to an arbitrary group
 * <p>
 * There are several different ways to invoke this page:
 * 
 * <li>As a consumer with no account / group involved - this just results
 *     in a voucher being issued and nothing else
 * <li>As a group with an order reference - this issues a voucher and adds the 
 *     patient and dicom to the patient list for the group and ALSO creates
 *     a dicom_order associated with the data.  In this case the accid parameter
 *     is expected.  
 * <li>With a default order group, which is configured in settings. In this 
 *     case when an order reference is supplied
 *     the patient CCR and dicom are associated with the default group.
 *     This is referred to in the code as "DODX" or "dodx", and results in a much
 *     more advanced UI being displayed.  This case is used by TIMC.
 */

// Turn off session timeout on this page
$_GLOBALS['no_session_check']=true;

require_once "utils.inc.php";
require_once "template.inc.php";
require_once "login.inc.php";
require_once "JSON.php";
require_once "alib.inc.php";
require_once "DB.inc.php";
require_once "settings.php";

global $Secure_Url;
global $Homepage_Url;

nocache();

$gwUrl = allocate_gateway(null);
$startDDLUrl =  $gwUrl."/ddl/start";

$errors = array();

$orderReference = req('callers_order_reference');
$accid = req('accid');
// $baseUrl = $Homepage_Url;
$baseUrl = $Secure_Url;

force_to_secure();

$db = DB::get();
$group = $db->first_row("select * from groupinstances where accid = ?", array($accid));

// If accid was specified then it must be a known group
if($accid && !$group)
    throw new Exception("The group ".$accid." specified does not exist.  Please check the URL used to invoke this page.");
    
$dodx = false;
if($orderReference) { // DODX using an existing order
    if(!$accid) {
        if(!isset($acDefaultDODXProvider))
            throw new Exception("Bad configuration - a default DOD provider must be configured to use this page.");
            
            
        $accid = $acDefaultDODXProvider;
        $dodx = true;
		$group = $db->first_row("select * from groupinstances where accid = ?", array($accid));
		if(!$group)
            throw new Exception("Bad configuration - default DOD provider $accid is not known on this system");
    }
    
    $order = $db->first_row("select * from dicom_order o
                             left join dicom_order_label dol on dol.dicom_order_id = o.id
                             where o.callers_order_reference = ?
                             and o.group_account_id = ?",array($orderReference,$accid));
    if(!$order)  {
        // Preserve TIMC behavior - they MUST create orders before entering upload page
        if(isset($acDefaultDODXProvider) && ($accid == $acDefaultDODXProvider))
            throw new Exception("Unknown order: ".$orderReference);
        else // No order?  Ok, we will make one using the order reference
            $order = generate_order($accid, $orderReference);
    }
    
    if($dodx) {
        $t = template("dodx.tpl.php");
    }
    else
    if($accid && $group && $group->dod_template)  {
    	$t = template($group->dod_template);
    }
    else
        $t = template("simpledod.tpl.php");
}
else {
    if($accid) { // DODX without an order - create one using default parameters
        $order = generate_order($accid);        
        if($group->enable_uploads != 1)
            throw new Exception("DICOM Uploads are not enabled for group $accid.  Please contact an administrator for the group to enable this option.");
    }
    
    if($accid && $group && $group->dod_template)  {
    	$t = template($group->dod_template);
    }
    else
    	$t = template("simpledod.tpl.php");
}

$hasCustomDisplay = false;
if($accid) {
    
    for($i=0; $i<10; ++$i) {
        $label = "label_0".$i;
        if($order->$label)
	        $hasCustomDisplay = true;
    }
    
    $json = new Services_JSON();
    $t->set("groupAccountId",$accid)
      ->set("group",$group)
      ->set("order",$order)
      ->set("orderJSON",$json->encode($order));
}

$t->set("hasCustomDisplay", $hasCustomDisplay);

if(req("oauth_consumer_key")) {
    $t->set("oauthConsumerKey", req("oauth_consumer_key"));
}

if(req('next')) {
    $t->set("next", req('next'));
}

$t->set("startDDLUrl",$startDDLUrl)
  ->set("baseUrl",$baseUrl)
  ->set("bodyClass","dicomUploadPage")
  ->set("title","DICOM Upload")
  ->set("loggedIn", is_logged_in());
         
if($group) 
    $t->set("title",$group->name." DICOM Upload");
         
echo $t->fetch();         

exit;
         
/**
 * Create a default generated order for the specified group account id with
 * blank information.
 *
 * @param unknown_type $accid
 * @return unknown
 */
function generate_order($accid, $orderReference = false) {
    
    $db = DB::get();
    
    $count = $db->first_row("select count(*) as cnt from dicom_order where group_account_id = ?",array($accid));
    
    if(!$orderReference)
        $orderReference = $accid ."-". $count->cnt;
    
    // Generate an order reference automatically
    $db->execute("insert into dicom_order (id, version, baseline, callers_order_reference, date_created, ddl_status, due_date_time,
                  email, group_account_id, last_updated, modality, patient_id,
                  protocol_id, scan_date_time)
                  values ( NULL, ?, ?, ?, NOW(), ?, NOW(), ?, ?, NOW(), ?, ?, ?, NOW())",
                 array(1, 0, $orderReference, 'DDL_ORDER_ACCEPTED', 0, $accid, '', '', '' ));
                
    return  $order = $db->first_row("select * from dicom_order o
		                             left join dicom_order_label dol on dol.dicom_order_id = o.id
		                             where callers_order_reference = ?",array($orderReference));
}
?>
