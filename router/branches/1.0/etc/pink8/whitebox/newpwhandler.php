<?php
require_once("../whitebox/wbsubs.inc");
require_once("../whitebox/mailsubs.inc");
require_once("../whitebox/displaynewpw.inc");
require_once("../whitebox/displayhomepage.inc");
session_start();
readconfig();
$userid = $_REQUEST['userid'];
$email = $_REQUEST['email'];
$success = validuseridemail($userid,$email);
if ($success == true) 
{// alright send an email and update the database
$password = generate_password();
updateuseridpassword($userid,$password);

$status = send_new_password($email,$userid,$password);
    if ($status ==true ){
			display_home_page(errortext("An email has been sent to ".$email.
						". Thank you for registering with MedCommons",$userid));
    } else display_home_page(errortext("There was a problem sending email - your password is $password - please contact customer support"));
}else 
{//never found this user
echo display_new_pw(errortext("*** Userid and Email are incorrect ***"));
}
?>