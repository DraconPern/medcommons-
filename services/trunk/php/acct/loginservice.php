<?

require_once "login.inc.php";
require_once "wslibdb.inc.php";
require_once "../acct/alib.inc.php";
require_once "../acct/rls.inc.php";
require_once "urls.inc.php";
require_once "DB.inc.php";
require_once "utils.inc.php";


/**
 * Login Service
 * 
 * Expects email and password parameters and if correct, 
 * sets login cookie and returns data about logged in user
 * in JSON format.
 */
class LoginService extends jsonrestws  {
    
    function verify_caller() {
        // No verification needed
    }
    
    function jsonbody() {
        
        global $Secure_Url;
        
        $email = req('email'); 
        if(!$email)
            throw new ValidationFailure('email not provided');
        
        if(!is_email_address($email))
            throw new ValidationFailure('Invalid email '.$email.' provided');
            
        $password = req('password');
        if(!$password)
            throw new ValidationFailure('password not provided');
        
        if(isfrozen($email))
            throw new ValidationFailure('Too many login attempts on this account.  Account frozen for 5 minutes.');
        
        $allpatients = req('allpatients',false);
        
        $customFields = req('customFields',false) === "true";
            
        $maxpatients = intval(req('maxpatients', 5));
            
        // Resolve user
        $row = User::resolveEmail($email, $password);
        if(!$row) {
            track_login_failure($email);
            throw new ValidationFailure('No such user / invalid password');
        }
        
        // Got user, log them in
        $user = User::load($row->mcid);
        
        remove_trakking($email);
        
        $user->login(); // Cookie set in here
        $info = get_validated_account_info();

        $db = DB::get();
        if($row->acctype == 'VOUCHER') {
            $info->voucher = $db->first_row("select couponum, auth, expirationdate from modcoupons where mcid = ?",$info->accid);
        }
        
        $patients = array();
            
        // Get some rows of patient list
        if($info->practice && !$allpatients)  {
            $patients = query_patient_list($info->practice->practiceid, $maxpatients, 0, ""," AND e.ViewStatus = 'Visible' ",$customFields);
            $info->practice->patients = $patients;
        }
        
        // What other groups does this user have?
        $info->groups = $db->query("select gi.accid,gi.name, gi.parentid from groupinstances gi, groupmembers gm
                                    where gm.memberaccid = ? and gm.groupinstanceid = gi.groupinstanceid",
                                   array($info->accid));
        $allgroups = array();                                   
        if($allpatients) {
            $practiceIds = array();
            foreach($info->groups as $g) {
                $practiceIds[]=$g->parentid;
	            $g->patients = array();
                $allgroups[$g->parentid] = $g;
            }
            $patients = query_patient_list($practiceIds, $maxpatients,0,"", " AND e.ViewStatus = 'Visible' ", $customFields);
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
        return $info;
    }
}

$ws = new LoginService();
$ws->handlews("LoginService");

?>
