<?
/**
 * Toggles whether a patient is on the group's roster.  If the patient is 
 * in the inbox, removes them from it when adding to the group roster.
 */
require_once "DB.inc.php";
require_once "mc.inc.php";
require_once "patientlist.inc.php";
require_once "utils.inc.php";
require_once "alib.inc.php";
require_once "JSON.php";

$json = new Services_JSON();
$result = new stdClass;
nocache();
try {
    
    validate_query_string();
    
    $accid = req('accid');
    if($accid && !is_valid_mcid($accid,true))
        throw new Exception("Invalid or missing value for parameter accid");
        
    $accids = req('accids');
    if($accids) {
       $a = array();
       $accids = explode(",", $accids);
       foreach($accids as $accid) {
           if(!is_valid_mcid($accid, true))
               throw new Exception("Invalid value for parameter accids");
           $a[]=$accid;
       }
       $accids = $a;
    }
    
    if(!$accids && $accid) {
        $accids = array($accid);
    }
    
    if(!$accids) 
        throw new Exception("accid or accids must be provided");
        
    $info = get_validated_account_info();    
    
    if(!$info)
        throw new Exception("Must be logged in to access this function");
        
    if(!$info->practice)
        throw new Exception("Must be group member to access this function");
    
    $db = DB::get();    
    $practices = q_member_practices($info->accid);
    
    $practice = $info->practice;
    
    $inbox = new PatientList($practice->practiceid, "inbox");    
    
    $members = new PatientList($practice->practiceid, "members");    
    foreach($accids as $accid) {
        if($members->contains($accid)) {
            $members->remove($accid);
            $inbox->add($accid);
            dbg("toggling member ${accid} to non-member");
        }
        else {
            dbg("toggling member ${accid} to member");
            $members->add($accid);
            $inbox->remove($accid);
        }
    }
    $result->status = "ok";
}
catch(Exception $ex) {
    $result->status = "failed";
    $result->error = $ex->getMessage();
}

echo $json->encode($result);
?>
