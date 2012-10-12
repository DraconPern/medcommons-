<?PHP
require_once("../whitebox/wbsubs.inc");
require_once("../whitebox/displayregistration.inc");
session_start();//need to get this systematized
readconfig();
$tracking = cleanreq('tracking'); // passed in from index.php to pass back into evail invite
 $userid="";
 $name="";
 $title="";
 $addr1="";
 $addr2="";
 $city = "";
 $state="";
 $zip="";
 $email="";
 $dob="";
 $sex="";
 $phone="";
 $company="";
 $healthcareSpecialist="";
 $stateLicense="";
// if already logged in, go fetch the existing values from this users record
if ($_SESSION['user']!="")
{ $userid = $_SESSION['user'];
 $success = getuserdetails(
   $userid,   
   $name,
   $title, 
   $addr1, 
   $addr2, 
   $city, 
   $state,
   $zip,
   $email,
   $dob, 
   $sex, 
   $phone, 
   $company, 
   $healthcareSpecialist, 
   $stateLicense,
   $pin,
   $defaultworklist,
   $defaultgateway,
   $backupgateways);
  if ($success==false){
  		display_home_page("** internal error - cant find this user in 
  					registration **",$userid,'','');
  		exit;
}
}

display_registration_page(
   $tracking,
   $userid,   
   $name,
   $title, 
   $addr1, 
   $addr2, 
   $city, 
   $state,
   $zip,
   $email,
   $dob, 
   $sex, 
   $phone, 
   $company, 
   $healthcareSpecialist, 
   $stateLicense,

   $userid_error,   
   $name_error,
   $title_error, 
   $addr1_error, 
   $addr2_error, 
   $city_error, 
   $state_error,
   $zip_error,
   $email_error,
   $dob_error, 
   $sex_error, 
   $phone_error, 
   $company_error, 
   $healthcareSpecialist_error, 
   $stateLicense_error
);		
?>