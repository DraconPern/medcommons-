<?php

require_once("../whitebox/wbsubs.inc");
require_once("../whitebox/displayhomepage.inc");

//we got here just because the user clicked on a hyperlink to get here
//check that we are logged in
session_start();
$userid = $_SESSION['user'];
if ($userid=="")
{//not logged in, send him back with penalties
display_home_page(errortext(
"You must be logged in to download the MedCommons Gateway"),'','',
			errortext("**please login**"));
}
$wbh = wbheader('download',"Download MedCommons Gateway");

$x = <<<XXX
$wbh
<table border="0" cellpadding="0" cellspacing="0" width="100%">

  <tr>
    <td><table align="center" border="0" cellpadding="0" cellspacing="0" width="60%">
        <tbody><tr> 
          <td>&nbsp;</td>
        </tr>
        <tr> 
          <td>&nbsp;</td>
        </tr>
        <tr> 
          <td> </td>
        </tr>
        <tr> 
          <td><p><strong>MEDCOMOMONS ROUTER DOWNLOAD</strong></p></td>
        </tr>
        <tr> 
          <td></td>
        </tr>
        <tr> 
          <td colspan="2"><p>The MedCommons Router is a software component that you can install to host MedCommons data
                 on your own local network.
              </p></td>
        </tr>
        <tr> 
          <td>&nbsp;</td>
        </tr>
        <tr valign="top"> 
          <td> 
            <p></p></td>
          <td align="right"><a href="javascript:NewWindow('install_1.php',
          'Installer', 480, 382, 'no')"><img src="button_gorouter.gif" border="0" 
                    height="20" width="120"></a></td>
        </tr>
        <tr> 
          <td>&nbsp;</td>
        </tr>
        <tr> 
          <td>&nbsp;</td>
        </tr>
      </tbody></table>
      <p>&nbsp;</p>
      <p>&nbsp;</p>
      <p>&nbsp;</p>
      <p>&nbsp;</p>
      <p>&nbsp;</p>
      <p>&nbsp;</p></td>
  </tr>
</tbody></table><br>
</body></html>
XXX;

echo $x;

?>