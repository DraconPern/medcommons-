<?php
require_once("../whitebox/wbsubs.inc");
function display_dialog_complete($message)
{
	// puts up a small message with an OK button
	// when the user hits the OK button this window will force a refresh of the parent window
	// and vaporize itself
$wbheader = wbheader('complete',$message,true);
$okbutton = butt('OK',refreshparentanddie());
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

return $x;
}
?>
