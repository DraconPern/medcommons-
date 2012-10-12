<?php
 $_GLOBALS['no_session_check']=true;
require_once "utils.inc.php";

// this little program is intended to be invoked as a hyperlink e.g. <a href='remail.php?to=billdonner@gmail.com'>resend now</a>

// it will resend the last email sent to the recipient


require_once "email.inc.php";

nocache();

$recipient = base64_decode($_GET['email']);
$next = req('next');

$status =  resend_last_email($recipient);
if($status)
    header("Location: ".$next);
else 
    throw new SystemFailure("Unable to send Email", "PostMark filed with status ".$status);
?>