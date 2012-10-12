<?php 
  define('PUBLIC_PAGE',1);
  
  require_once "settings.php";
  require_once "urls.inc.php";
  require_once "utils.inc.php";
  require_once "template.inc.php";
  require_once "../../acct/verify.inc.php";
  require_once "DB.inc.php";
  
  $db = DB::get();
  
  if(!isset($acAptusGroupAccountId)) 
      throw new SystemFailure("Incorrect Configuration", "Parameter acAptusGroupAccountId was not set in local_settings.php");
  
  $group = $db->first_row("select * from groupinstances where accid=?", array($acAptusGroupAccountId));
  $GroupID=$group->groupinstanceid;
  $GroupUploadURL=gpath("Secure_Url")."/".$group->accid."/upload";
  $GroupLogo=isset($group->logo_url)?$group->logo_url:'/images/logoHeader.gif';
  
  
  if(isset($_POST['GroupID'])) {
      $t = template("");
      $facility = post("Facility");
      $senderName = post("SenderName");
      $email = post('SenderEmail');
      if($email && !is_valid_email($email))    
        Template::$errors->SenderEmail = 'Email address is not in valid format';
      $comments = post("Clinical");        
      $name = verify("nameAndId", "Name and ID");
      $accessionNumber = verify("accessionNumber","Accession Number");
      $notifyEmail = $group->upload_notification;
      
      if(!Template::has_errors()) {
          
          $reference = sha1(time().$name.$accessionNumber.$group->accid);
          $url = gpath("Orders_Url")."/order?".
               "patient_id=".urlencode($name).
               "&sender_name=".urlencode($senderName).
               "&sender_email=".urlencode($email).
               "&accession_number=".urlencode($accessionNumber).
               "&facility=".urlencode($facility).
               "&callers_order_reference=".$reference.
               "&group_account_id=".$group->accid.
               "&upload_notification_email=".urlencode($group->upload_notification).
               "&order_comments=".$comments;
          
          header("Location: $url"); 
          exit;
      }
  }
?>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Upload to STAPLE International Post Market Registry</title>
    <link rel="stylesheet" type="text/css"  href="/acct/pcss/pstyle.css"/>
    <style type='text/css'>
        span.error { display: block; }
    </style>
</head>
<body>
<img src='<?= $GroupLogo ?>' />
<h3>Upload to STAPLE International Post Market Registry</h3>
<div class='hideerror'></div>
<form   id=uploadForm method=post>
<input type=hidden name=UploadURL value='<?= $GroupUploadURL?>' />
<input type=hidden name=GroupID value='<?= $GroupID?>' />
<input type=hidden name=ConsultantID value='' />
<input type=hidden name=ConsultantEmail value='' />
<input type=hidden name=ConsultantName value='' />
<input type=hidden name=PatientID value='' />
<input type=hidden name=next value='/acct/index.php' />

<fieldset><legend>Clinical History</legend>
<ol>
    <li><textarea rows=6 cols=40 name="Clinical" id="Clinical" /><?=field('comments')?></textarea></li>
</ol>
</fieldset>
<fieldset>
    <legend>Sender's Info</legend>
    <ol>
        <li><label for=Facility>Facility</label> 
            <input id=Facility
                   name=Facility type=text value='<?=field('Facility')?>'>
            <span class='error'><?=error_msg()?></span>
         </li>
            
        <li><label for=SenderName>Your Name</label> 
            <input id=SenderName
                   name=SenderName type=text value='<?=field("SenderName")?>'>
            <span class='error'><?=error_msg()?></span>
        </li>
        
        <li><label for=SenderEmail>Your Email</label> 
            <input id=SenderEmail name=SenderEmail type=email value='<?=field('SenderEmail')?>'>
            <span class='error'><?=error_msg()?></span>
        </li>
    </ol>
</fieldset>


<h3>Anonymization</h3>
<fieldset><legend>Patient</legend>
<ol>
    <li><label for=nameAndId>Patient ID *</label> 
        <input id=nameAndId
               name=nameAndId type=text value='<?=field('nameAndId')?>'>
               <?if(isset(Template::$errors->nameAndId)):?>
                <br>
                <span class='error'><?=error_msg()?></span>
               <?else:?>
                <br>
                <span>Example: 01-R003-ABC</span>
               <?endif?>
    </li>
</ol>
</fieldset>

<fieldset><legend>Accession Number</legend>
<ol>
    <li><label for=accessionNumber>Accession Number *</label> <input id=accessionNumber
        name=accessionNumber type=text value='<?=field('accessionNumber')?>'>
        <br>
        <span class='error'><?=error_msg()?></span>
    </li>
</ol>
</fieldset>

<fieldset>
    <button type=submit>Continue</button>
</fieldset>

</form>
</body>
</html>