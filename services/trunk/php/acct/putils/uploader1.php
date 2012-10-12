<?php 
// jans form
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

	<link rel="stylesheet" type="text/css"  href="../pcss/pstyle.css"/>
</head>
<body>
<img onClick="" src='<?= $GroupLogo ?>' />

<div class='hideerror'></div>
<form   id=uploadForm method=post action='<?= $UploadHandler?>'>
<input type=hidden name=UploadURL value='<?= $GroupUploadURL?>' />
<input type=hidden name=GroupID value='<?= $GroupID?>' />
<input type=hidden name=ConsultantID value='' />
<input type=hidden name=ConsultantEmail value='' />
<input type=hidden name=ConsultantName value='' />
<input type=hidden name=PatientID value='' />
<input type=hidden name=next value='/acct/index.php' />
 <input id=VideoURL
		name=VideoURL type=hidden value=''>
<input id=SenderSMS
		name=SenderSMS type=hidden value=''>
<input
		id=SenderPhone name=SenderPhone type=hidden value=''>


	<div class='_errormainclass' ></div> 
	<fieldset>
		<legend>Welcome to <?= $LongName ?> Services Portal</legend>
		<p>Please fill out the following information, insert CD and push CONTINUE button.</p><p> Thank you!</p>
	</fieldset>
	<fieldset>
		<legend>This scan is being sent by:</legend>
		<ol>
		
			<li>
				<label for=SenderName>Physician's Name</label>
				<input id=SenderName name=SenderName type=text >
			</li>
			<li>
				<label for=Facility>Facility</label>
				<input id=Facility name=Facility type=text  >
			</li>
		
			<li>
				<label for=SenderEmail>Email</label>
				<input id=SenderEmail name=SenderEmail type=email >
			</li>
			
			<li>
				<label for=Clinical>Notes (optional) </label>
					<textarea rows=6 cols=40  name="Clinical" id="Clinical" /></textarea>
			</li>
	
	</ol>
	</fieldset>
	

	<fieldset>
		<legend>Please upload this scan to: </legend>
		<ol>
				<li>
				<label for=ConsultantName>Name</label>
				<input id=ConsultantName name=ConsultantName type=text >				
				</li>
				<li>
				<label for=ConsultantEmail>Email</label>
				<input id=ConsultantEmail name=ConsultantEmail type=email >				
				</li>
		</ol>
	</fieldset>

	<fieldset>
		<legend>Please Insert CD and click CONTINUE</legend>
		
	</fieldset>
	

	<fieldset>
		<button type=submit>CONTINUE</button>
	</fieldset>
</form>
</body>
</html>