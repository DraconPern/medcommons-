<?php
require_once("../whitebox/wbsubs.inc");
session_start();
$wbheader = wbheader('contact',"Contact Adrian at MedCommons");
$x=<<<XXX
$wbheader
  <table border="0" cellpadding="0" cellspacing="0" width="100%">
  <tbody>
	
  <tr>
    <td><table align="center" border="0" cellpadding="0" cellspacing="0" width="80%">
        <tbody><tr> 
          <td>&nbsp;</td>
        </tr>
        <tr> 
          <td> </td>
        </tr>
        <tr> 
          <td><p><strong>CONTACT</strong></p></td>
        </tr>
        <tr> 
          <td><p>For more information, please send email to <a href="mailto:agropper@medcommons.org">agropper@medcommons.org</a>.</p>
            <p>&nbsp;</p></td>
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
      <p>&nbsp;</p>
      <p>&nbsp;</p></td>
  </tr>
</tbody></table><br>
</body></html>
XXX;

echo $x;
?>
