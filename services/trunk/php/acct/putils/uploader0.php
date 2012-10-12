<?php 

require_once "template.inc.php";

template("base.tpl.php")->extend();

// standard upload form
$arg = $_GET['a'];
$args = base64_decode($arg);
list($ShortName,$LongName,$GroupID,$GroupUploadURL,$GroupLogo,$UploadHandler)=explode('|',$args);
?>
<?block("title")?>
Upload to <?= $ShortName ?>
<?end_block()?>

<?block("head")?> 
	<link rel="stylesheet" type="text/css"  href="../pcss/pstyle.css"/>
<?end_block()?>	


<?block("content")?> 

<?if($GroupLogo):?>
<img onClick="" src='<?= $GroupLogo ?>' />
<?endif?>
<h2>Upload to <?= $LongName ?> on MedCommons</h2>
<div class='hideerror'></div>
<form   id=uploadForm method=post action='<?= $UploadHandler?>'>
<input type=hidden name=UploadURL value='<?= $GroupUploadURL?>' />
<input type=hidden name=GroupID value='<?= $GroupID?>' />
<input type=hidden name=ConsultantID value='' />
<input type=hidden name=ConsultantEmail value='' />
<input type=hidden name=ConsultantName value='' />
<input type=hidden name=PatientID value='' />
<input type=hidden name=next value='/acct/index.php' />

<fieldset><legend>Comments</legend>
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

<fieldset>
<button type=submit>Send Radiology to <?= $ShortName?></button>
</fieldset>

</form>
<?end_block()?>