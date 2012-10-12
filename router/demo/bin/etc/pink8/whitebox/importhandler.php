<?php
require_once("../whitebox/wbsubs.inc");
	// puts up a small message with an OK button
	// when the user hits the OK button this window will force a refresh of the parent window
	// and vaporize itself
$message = "VRCP Table Has Been Imported";
readconfig();
$gateway=cleanreq('gateway');
$user=cleanreq('user');

$wbheader = wbheader('complete',$message,true);
$okbutton = butt('OK',refreshparentanddie());


if (is_uploaded_file($_FILES['upload']['tmp_name'])){
	$message= readfile($_FILES['upload']['tmp_name']);
}
else $message ="Can't import that file";

$x=<<<XXX
$wbheader
      <p>&nbsp;</p>
      <p>&nbsp;</p>
     
      <p>&nbsp;</p>
      <p>$message</p>
      
      <p>&nbsp;</p>
      <p>&nbsp;</p>
       $okbutton
      
</body></html>
XXX;

echo $x;

?>
