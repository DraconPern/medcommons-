<?
/**
 * Moves a patient from a group 'inbox' to the group's patient 'members' list
 */
require_once "DB.inc.php";
require_once "mc.inc.php";
require_once "patientlist.inc.php";
require_once "alib.inc.php";

$groupId = req('g');
if(!$groupId || (preg_match("/^[0-9]{1,12}$/", $groupId) !== 1))
    throw new Exception("Invalid or missing value for parameter g");
    
$accid = req('accid');
if(!$groupId || !is_valid_mcid($accid,true))
    throw new Exception("Invalid or missing value for parameter accid");
    
$info = get_validated_account_info();    

if(!$info)
    throw new Exception("Must be logged in to access this function");
    
if(!$info->practice)
    throw new Exception("Must be group member to access this function");

$db = DB::get();    
$practices = q_member_practices($info->accid);

$practice = false;
foreach($practices as $p) {
    if($p->providergroupid == $groupId) {
        $practice = $p;
        break;
    }
}

if(!$practice)
    throw new Exception("Unable to locate practice for group $groupId");

$inbox = new PatientList($practice->practiceid, "inbox");    
$members = new PatientList($practice->practiceid, "members");    

$inbox->remove($accid);
$members->add($accid);

header("Location: index.php");
?>
