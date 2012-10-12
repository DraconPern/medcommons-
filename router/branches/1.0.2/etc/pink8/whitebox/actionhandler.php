<?php
require_once("../whitebox/wbsubs.inc");
require_once("actions.inc");
	// puts up a small message with an OK button
	// when the user hits the OK button this window will force a refresh of the parent window
	// and vaporize itself
$message = "Action updated";
readconfig();
$gateway=cleanreq('gateway');
$user=cleanreq('user');
$displayname=cleanreq('displayname');
$actioncode=cleanreq('actioncode');
$rowid=cleanreq('rowid');
//echo "updateactioncode displayname $displayname oldaction $oldaction actioncode $actioncod";
patchvcrpactionrow($rowid,$actioncode,$user,$gateway);

$wbheader = wbheader('complete',$message,true);
$okbutton = butt('OK',refreshparentanddie() );
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
