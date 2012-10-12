<?PHP
require_once("../whitebox/wbsubs.inc");
require_once("../whitebox/displayregistration.inc");
require_once("../whitebox/displayhomepage.inc");

require_once("../whitebox/mailsubs.inc");

function check_phone($str) 
{ 
    //returns 1 if valid phone number (only numeric string), 0 if not 
     
    if (ereg('^[[:digit:]]+$', $str)) 
           return 1; 
    else 
           return 0; 
}
function b($s)
{
	return $_REQUEST[$s];
//	echo "$s is ".$$s."\n\r";
}

readconfig(); // get reconnected to database

// get all of the fields from the form

$userid = b("userid");
$name= b("name");
$title = b("title");
$addr1 = b("addr1");
$addr2 = b("addr2");
$city = b("city");
$state = b("state");
$zip = b("zip");
$email= b("email");
$dob= b("dob");
$sex= b("sex");
$phone= b("phone");
$company= b("company");
$healthcareSpecialist= b("healthcareSpecialist");
$stateLicense=b("stateLicense");


// clear out all the error codes

   $userid_error="";   
   $name_error="";
   $title_error=""; 
   $addr1_error=""; 
   $addr2_error=""; 
   $city_error=""; 
   $state_error="";
   $zip_error="";
   $email_error="";
   $dob_error=""; 
   $sex_error=""; 
   $phone_error=""; 
   $company_error=""; 
   $healthcareSpecialist_error=""; 
   $stateLicense_error="";

// check out all of the errors

if ($userid =="") $userid_error = errortext("** please enter a user id");

if (strlen($name)<3) $name_error = errortext("** please enter a real name**");

if ($email =="") $email_error = errortext("**you must supply a valid email address**");

if ($phone =="") $phone_error = errortext("**you must supply a phone number**");

if (check_phone($phone)!= false) $phone_error = errortext("**invalid phone number**");

// if there is a problem with a field then clear it out

if ($userid_error!="")$userid="";
if ($name_error!="")$name="";
if ($title_error!="") $title="";
if ($addr1_error!="") $addr1="";
if ($addr2_error!="") $addr2="";
if ($city_error!="") $city = "";
if ($state_error!="") $state="";
if ($zip_error!="") $zip="";
if ($email_error!="") $email="";
if ($dob_error!="") $dob="";
if ($sex_error!="") $sex="";
if ($phone_error!="") $phone="";
if ($company_error!="") $company="";
if ($healthcareSpecialist_error!="") $healthcareSpecialist="";
if ($stateLicense_error!="") $stateLicense="";

// if any errors put the thing up again
if (($userid_error!="") 
or ($name_error!="")
or ($title_error!="") 
or ($addr1_error!="") 
or ($addr2_error!="") 
or ($city_error!="") 
or ($state_error!="")
or ($zip_error!="")
or ($email_error!="")
or ($dob_error!="") 
or ($sex_error!="") 
or ($phone_error!="") 
or ($company_error!="") 
or ($healthcareSpecialist_error!="") 
or ($stateLicense_error!="") )

{
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
		
}

else 
{ // form was good, send the invite,put it in the database

$pw = generate_password();

insertuserdetails(   
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
   $pw);

$status = send_invite_registration($email,$userid,$pw,$tracking);
    if ($status ==true ){
			display_home_page(errortext("An email has been sent to ".$email.
						". Thank you for registering with MedCommons",$userid));
    } else display_home_page(errortext("There was a problem sending email - your password is $pw - please contact customer support"));
}
?> 