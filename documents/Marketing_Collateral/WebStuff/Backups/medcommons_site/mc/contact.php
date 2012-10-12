<?php
session_start();
?>
<php3>
<html>
<head>
<title>MEDCOMMONS</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<meta name="robots" content="none">
<link href="/css/main.css" rel="stylesheet" type="text/css">
</head>

<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<table width="100%" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td bgcolor="DFF2F7"><table width="100%" border="0" cellspacing="5" cellpadding="0">
        <tr> 
          <td width="256" rowspan="2"><img src="/mc/images/logo_sm.gif" width="256" height="50"></td>
          <td align="right" valign="bottom" width="100%"> 
            <?php if ($_SESSION['name'] != "") { ?>
            <p>logged in as <strong><?php echo $_SESSION['name']; ?></strong> 
              <a href="/mc/my_account.php">My Account</a></p>
            <?php } ?>
          </td>
        </tr>
        <tr>
          <td align="right" valign="bottom"><p><a href="/mc/index.php">home</a> 
              || <a href="/mc/register.php">register</a> || <a href="/mc/my_account.php">download</a> 
              || <a href="/mc/contact.php">contact</a></p></td>
        </tr>
      </table></td>
  </tr>
  <tr>
    <td><table width="80%" border="0" align="center" cellpadding="0" cellspacing="0">
        <tr> 
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
      </table>
      <p>&nbsp;</p>
      <p>&nbsp;</p>
      <p>&nbsp;</p>
      <p>&nbsp;</p>
      <p>&nbsp;</p>
      <p>&nbsp;</p>
      <p>&nbsp;</p></td>
  </tr>
</table><br>
</body>
</html>
</php3>