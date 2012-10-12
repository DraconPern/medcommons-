<?php 
// standard upload form
$arg = $_GET['a'];
$args = base64_decode($arg);
list($ShortName,$LongName,$GroupID,$GroupUploadURL,$GroupLogo,$UploadHandler)=explode('|',$args);
if ($GroupLogo=='') $GroupLogo='http://medcommons.net/images/logoHeader.gif';
?>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Upload to <?= $ShortName ?></title>
	<link rel="stylesheet" type="text/css"  href="css/pstyle.css"/>
</head>
<body>
<img onClick="" src='<?= $GroupLogo ?>' />
<h3>Upload to <?= $LongName ?> on MedCommons</h3>
<div class='hideerror'></div>
<form   id=uploadForm method=post action='<?= $UploadHandler?>'>
<input type=hidden name=UploadURL value='<?= $GroupUploadURL?>' />
<input type=hidden name=GroupID value='<?= $GroupID?>' />
<input type=hidden name=ConsultantID value='' />
<input type=hidden name=ConsultantEmail value='' />
<input type=hidden name=ConsultantName value='' />
<input type=hidden name=PatientID value='' />

<fieldset><legend>Clinical History</legend>
<ol>
	<li><textarea rows=6 cols=40 name="Clinical" id="Clinical" /></textarea></li>
</ol>
</fieldset>
<fieldset><legend>Sender's Info</legend>
<ol>
	<li><label for=Facility>Facility</label> <input id=Facility
		name=Facility type=text value=''></li>
	<li><label for=SenderName>Your Name</label> <input id=SenderName
		value='' name=SenderName type=text value=''></li>
	<li><label for=SenderEmail>Your Email</label> <input
		id=SenderEmail name=SenderEmail type=email value=''></li>

</ol>
</fieldset>

<fieldset><legend>Phone</legend>
<ol>
	<li><label for=SenderPhone>Phone Number</label> <input
		id=SenderPhone name=SenderPhone type=text value=''></li>

</ol>
</fieldset>

<fieldset><legend>SMS</legend>
<ol>
	<li><label for=SenderSMS>SMS Number</label> <input id=SenderSMS
		name=SenderSMS type=text value=''></li>
</ol>
</fieldset>

<fieldset><legend>Video Conference</legend>
<ol>
	<li><label for=VideoURL>Video URL</label> <input id=VideoURL
		name=VideoURL type=text value=''></li>
</ol>
</fieldset>

<fieldset>
<button type=submit>Send Radiology to <?= $ShortName?></button>
</fieldset>

</form>
</body>
</html>