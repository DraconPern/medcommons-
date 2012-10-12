<?php
require_once("../whitebox/wbsubs.inc");
session_start(); //hack to put start session so user can be wiped out before header goes up

$_SESSION['user']=""; // dirty deeds done dirt cheap
$wbh=wbheader('logout',"You have been logged out");

$x = <<<XXX
$wbh
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
          <td><p><strong>YOU HAVE LOGGED OUT</strong></p></td>
        </tr>
        <tr> 
          <td></td>
        </tr>
        <tr> 
          <td><p>Thank you for using MedCommons!  For maximum security, please close your browser window.</p></td>
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