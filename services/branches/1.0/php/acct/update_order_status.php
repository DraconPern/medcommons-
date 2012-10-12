<?
/**
 * Accepts form describing order details and creates a patient to become the target of the order.
 */
require_once "utils.inc.php";
require_once "JSON.php";
require_once "settings.php";
require_once "template.inc.php";
require_once "email.inc.php";
require_once "DB.inc.php";

nocache();

$json = new Services_JSON();
$result = new stdClass;
$db = DB::get();

try {
    $orderReference = req('callers_order_reference');
    if(!$orderReference) 
      throw new Exception("Expected parameter 'callers_order_reference' not provided");

    $valid_statuses = array("DDL_ORDER_NOMATCH",
                            "DDL_ORDER_XMITING",
                            "DDL_ORDER_UPLOAD_COMPLETE",
                            "DDL_ORDER_DOWNLOAD_COMPLETE",
                            "DDL_ORDER_COMPLETE",
                            "DDL_ORDER_TIMEOUT_WARNING",
                            "DDL_ORDER_CANCELLED",
                            "DDL_ORDER_ERROR");
    
    $status = req('status');
    if(!in_array($status, $valid_statuses)) 
      throw new Exception("Status $status is not an acceptable status value");
      
    dbg("Updating order $orderReference to status $status");
    
    $set = array("ddl_status = ?");
    $params = array($status);
    
    $errorCode = req('errorCode');
    if($errorCode && $errorCode != "") {
	    $set[]= "error_code = ?";
	    $params[]= $errorCode;
    }
    
    $comments = req('comments');
    if($comments) {
	    $set[]= "comments = ?";
	    $params[]= $comments;
    }
    
    $desc = req("desc","");
    
    $user = req("user","");
    
    $params[]=$orderReference;
    
    $order = $db->first_row("select * from dicom_order where callers_order_reference = ?",array($orderReference));
    if(!$order) 
        throw new Exception("Unknown order reference $orderReference");
    
    $db->execute("update dicom_order set ".implode(",",$set)." where callers_order_reference = ?",$params);
    $db->execute("insert into dicom_order_history (ddl_status,description,dicom_order_id,remote_host,remote_ip, remote_user, date_created) 
                 values (?,?,?,?,?,?,?)",
                 array($status,$errorCode?$errorCode:$desc,$order->id, $_SERVER['REMOTE_ADDR'], $_SERVER['REMOTE_ADDR'], $user, date('Y-m-d H:i:s',time())));
    
    /*                 
    // If status == DDL_UPLOAD_COMPLETE then send notification to group members
    if(($status == "DDL_ORDER_UPLOAD_COMPLETE") && $order->group_account_id) {
        
        global $acCommonName, $acApplianceName, $acDomain, $Secure_Url;
        
        $members = $db->query("select u.email, gi.name from users u, groupmembers gm, groupinstances gi 
                               where gm.memberaccid = u.mcid
                               and   gm.groupinstanceid = gi.groupinstanceid
                               and   gi.accid = ?", array($order->group_account_id));
        
        foreach($members as $gm) {
            $t = template("order_upload_notification_email_text.tpl.php")
                ->set("order",$order)
                ->set("acCommonName", $acCommonName)
                ->set("acApplianceName",$acApplianceName)
                ->set("acDomain", $acDomain)
                ->set("applianceUrl", $Secure_Url)
                ->set("groupName",$gm->name);
                
                
    
            dbg("Sending email to $gm->email");
            send_mc_email($gm->email,
                        "$acCommonName - DICOM Upload Received - ".$order->callers_order_reference,
                        $t->fetch(),
                        $t->fetch("order_upload_notification_email_html.tpl.php"),
                        array());
        }
    }
    */
    
    $result->status = "ok";
}
catch(Exception $ex) {
    $result->status = "failed";
    $result->error = $ex->getMessage();
}
echo $json->encode($result);

?>
