<?php
require_once("../lib/config.inc");
require_once("../whitebox/wbsubs.inc");
function display_dicom_config(
   $rowid,
   $readonly,
   $displayname,
   $user,   
   $gateway,
   $aetitle, 
   $showmenus, 
   $dicomipaddress, 
   $dicomport, 
   $comment,
   $displaynameerror,
   $gatewayerror,
   $aetitleerror,
   $dicomipaddresserror,
   $dicomporterror,
   $commenterror)
{

$insertneeded = ($displayname=="");
$checked = ($showmenus==1)?"checked":"";
if ($readonly==true) $fob = " Display DICOM Device"; else
$fob =($insertneeded==true)?" New DICOM Device":" Modify DICOM Device";
//echo $f;
$wbh = wbheader("DICOM",
				"$fob on $gateway for user $user "
				,true);
				
$passinsert = ($insertneeded==true)?"<input type='hidden' name=insertnew value='insert'>":"";
$passreadonly = ($readonly==true)?"<input type='hidden' name=readonly value='yes'>":"";			
$commentblock = ($readonly==true)?$comment:"<input type='text' size=40 value ='$comment' name=comment> ";
$dicomportblock = ($readonly==true)?$dicomport:"<input type='text' value ='$dicomport' name=dicomport> ";
$dicomipblock = ($readonly==true)?$dicomipaddress:"<input type='text' value ='$dicomipaddress' name = dicomipaddress> ";
$aetitleblock = ($readonly==true)?$aetitle:"<input type='text' value ='$aetitle' name = dicomaetitle>"; 
$displaynameblock = ($readonly==true)?$displayname." $checked ":
"<input type='text' value = '$displayname' name= displayname>
	<input type='checkbox' $checked name=showinmenus>Show in Menus";

if($readonly==true) $bottombutt= butt('OK',refreshparentanddie());

else $bottombutt= 
"<input type='submit' value='SAVE' name='submit'>".butt("CANCEL",suicide()).
"<input type='submit' value='DELETE' name='submit'>";

$x=<<<XXX
<div class=Section1>
$wbh
$displaynameerror
$aetitleerror
$dicomipaddresserror
$dicomporterror
$commenterror
<p class=MsoNormal><b>$fob on $gateway  user $user </b></p>

<p class=MsoNormal>&nbsp;</p>

<p class=MsoNormal><i>[This form is a web page linked to from any entry in the
left column of the VRCP Table]</i></p>

<p class=MsoNormal>&nbsp;</p>

<form method=GET action="dicomhandler.php">
$passinsert
$passreadonly
<input type='hidden' name=gateway value='$gateway'>
<input type='hidden' name=user value='$user'>
<input type='hidden' name=olddisplayname value='$displayname'>
<input type='hidden' value='$rowid' name='rowid'>

<p class=MsoNormal style='margin-bottom:12.0pt'>Display Name 
   $displaynameblock 
</p>

<p class=MsoNormal style='margin-bottom:12.0pt'>DICOM AE Title 
	$aetitleblock  
</p>
<p class=MsoNormal style='margin-bottom:12.0pt'>DICOM IP Address
	$dicomipblock 
</p>
<p class=MsoNormal style='margin-bottom:12.0pt'>DICOM Port 
	$dicomportblock 
</p>
<p class=MsoNormal style='margin-bottom:12.0pt'>Comment 
	$commentblock 
</p>


<p class=MsoNormal style='margin-bottom:12.0pt'>
<input type='submit' value='DICOM Echo Test' name='echo'>
</p>
<p class=MsoNormal style='margin-bottom:12.0pt'>&nbsp;</p>

<p class=MsoNormal style='margin-bottom:12.0pt'>
$bottombutt
 </p>

</form>


</div>

</body>

</html>
XXX;

return $x;

}

?>
