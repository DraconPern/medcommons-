<?

require_once "login.inc.php";
require_once "wslibdb.inc.php";
require_once "../../acct/alib.inc.php";
require_once "../../acct/rls.inc.php";
require_once "DB.inc.php";
require_once "utils.inc.php";


class LoginService extends jsonrestws  {
    
    function verify_caller() {
        // No verification needed
    }
    
    function jsonbody() {
        
        $email = req('email'); 
        if(!$email)
            throw new ValidationFailure('email not provided');
        
        if(!is_email_address($email))
            throw new ValidationFailure('Invalid email '.$email.' provided');
            
        $password = req('password');
        if(!$password)
            throw new ValidationFailure('password not provided');
        
        // Resolve user
        $row = User::resolveEmail($email, $password);  //in rls.inc.php????!
        if(!$row) 
            throw new ValidationFailure('No such user / invalid password');
            
            $wasloggedin = (isset($_COOKIE['mc'])?1:0); // send back our previous state
            
        // Got user, log them in
        $user = User::load($row->mcid);
        $user->login();
            
        $info = get_validated_account_info();
        if($row->acctype == 'VOUCHER') {
            $db = DB::get();
            $info->voucher = $db->firstRow("select couponum, auth, expirationdate from modcoupons where mcid = ?",$info->accid);
        }
            
        // Get some rows of patient list
        if($info->practice) 
            $info->practice->patients = query_patient_list($info->practice->practiceid, 5);
            
            
            $info->wasloggedin = $wasloggedin;
        
        return $info;
    }
}

$ws = new LoginService();
$ws->handlews("Login One User");

?>