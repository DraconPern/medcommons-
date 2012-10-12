<?php
require_once("../lib/config.inc");
require_once("../whitebox/wbsubs.inc");
function display_gateway_config(
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
	$readonly=true;
$fob = " Display Gateway Device"; 
$wbh = wbheader("Gateway",
				"$fob on $gateway for user $user "
				,true);

$gatewayurl = getgatewayinfo($gateway,$description,$lastheard);
if ($gatewayurl=="") $gatewayurl = "????";
$bottombutt= butt('OK',refreshparentanddie());

$x=<<<XXX
<div class=Section1>
$wbh

<p class=MsoNormal><b>$fob on $gateway  user $user </b></p>

<p class=MsoNormal>&nbsp;</p>

<p class=MsoNormal><i>[This form is a web page linked to from any entry in the
left column of the VRCP Table]</i></p>

<p class=MsoNormal>&nbsp;</p>

<form method=GET >

<p class=MsoNormal style='margin-bottom:12.0pt'>Gateway URL
   $gatewayurl
</p>

<p class=MsoNormal style='margin-bottom:12.0pt'>Gateway Description
   $description
</p>

<p class=MsoNormal style='margin-bottom:12.0pt'>Last Heard from at 
   $lastheard
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
