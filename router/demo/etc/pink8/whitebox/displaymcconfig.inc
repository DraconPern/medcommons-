<?php
require_once("../lib/config.inc");
require_once("../whitebox/wbsubs.inc");
function display_mc_config(
	$rowid,
   $readonly,
   $displayname,
   $user,   
   $gateway,
   $colleagues,
   $destination, 
   $showmenus, 
   $regusers, 
   $sendemail,
   $email,
   $emailtype,
   $worklistonly,
   $templist,
   $generalerror,
   $displaynameerror,
   $emailerror)
{

$insertneeded = ($displayname=="");
$checked = ($showmenus==1)?"checked":"";
if ($readonly==true) $fob = " Display MedCommons Destination"; else
$fob =($insertneeded==true)?" New MedCommons Destination":
					" Modify MedCommons Destination";
//echo $f;
$wbh = wbheader("mcdest",
				"$fob on $gateway for user $user "
				,true);
				
$passinsert = ($insertneeded==true)?"<input type='hidden' name=insertnew value='insert'>":"";
$passreadonly = ($readonly==true)?"<input type='hidden' name=readonly value='yes'>":"";			


$worklistblock = ($readonly==true)?$worklistonly:
"<input type='checkbox'  value ='$worklistonly' name=worklist> ";
$templistblock = ($readonly==true)?$templist:
"<input type='checkbox' value ='$templist' name=templist> ";
$emailblock = ($readonly==true)?$email:
"<input type='text'  value ='$email' name=email> ";
$regusersblock = ($readonly==true)?$regusers:
"<input type='checkbox' value ='$regusers' name=regusers> ";
$destinationblock = ($readonly==true)?$destination:
"<input type='text' value ='$destination' name = destination> ";
$colleaguesblock = ($readonly==true)?$colleagues:
"<input type='checkbox' value ='$colleagues' name =colleagues>"; 

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
$generalerror
$emailerror
<p class=MsoNormal><b>$fob on $gateway  user $user </b></p>

<p class=MsoNormal>&nbsp;</p>

<p class=MsoNormal><i>[This form is a web page linked to from any entry in the
right column of the VRCP Table]</i></p>

<p class=MsoNormal>&nbsp;</p>

<form method=GET action="mchandler.php">
$passinsert
$passreadonly
<input type='hidden' name=gateway value='$gateway'>
<input type='hidden' name=user value='$user'>
<input type='hidden' name=olddisplayname value='$displayname'>
<input type='hidden' value='$rowid' name='rowid'>

<p class=MsoNormal style='margin-bottom:12.0pt'>Display Name 
   $displaynameblock 
</p>

<p class=MsoNormal style='margin-bottom:12.0pt'>MedCommons Destination ID 
	$destinationblock  
</p>
<p class=MsoNormal style='margin-bottom:12.0pt'>Enable Colleagues to search for this Virtual Radiology Folder
	$colleaguesblock 
</p>
<p class=MsoNormal style='margin-bottom:12.0pt'>Enable Registerd MedCommons users to search this folder
	$regusersblock 
</p>
<p class=MsoNormal style='margin-bottom:12.0pt'>Send email to:
	$emailblock 
</p>
<p class=MsoNormal style='margin-bottom:12.0pt'>Worklist Only- No Automatic Transfer
	$worklistblock 
</p>
<p class=MsoNormal style='margin-bottom:12.0pt'>Temporary List - No Automatic Registration
	$templistblock 
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
