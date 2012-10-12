<?php
$db = mysql_connect("db95.perfora.net", "dbo100827707", "meddemo");
?>

<php3>
<html>
<head>
<title>MEDCOMMONS</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<meta name="robots" content="none">
<link href="/css/main.css" rel="stylesheet" type="text/css">
<script language="javascript" src="functions/form_functions.js"></script>
</head>

<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<table width="100%" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td bgcolor="DFF2F7"><table width="100%" border="0" cellspacing="5" cellpadding="0">
        <tr>
          <td width="256"><img src="/mc/images/logo_sm.gif" width="256" height="50"></td>
          <td align="right" valign="bottom" width="100%">
<p><a href="/mc/index.php">home</a> || <a href="/mc/register.php">register</a> 
              || <a href="/mc/my_account.php">download</a> || <a href="/mc/contact.php">contact</a></p></td>
        </tr>
      </table></td>
  </tr>
  <tr>
    <td><table width="80%" border="0" align="center" cellpadding="0" cellspacing="0">
        <tr> 
          <td><p>&nbsp;</p>
            <p><strong>BAD USERNAME AND / OR PASSWORD</strong></p></td>
        </tr>
        <tr> 
          <td><p>We were not able to process your login. Please re enter, taking 
              care to check whether your 'Caps Lock' key is pressed.</p>
            </td>
        </tr>
        <tr> 
          <td> <form method="post" action="my_account.php">
              <table width="200" border="0" align="center" cellpadding="0" cellspacing="0">
                <tr> 
                  <td align="right"><p>email: &nbsp;</p></td>
                  <td><input type="text" name="email"></td>
                </tr>
                <tr> 
                  <td align="right"><p>password:&nbsp;</p></td>
                  <td><input type="password" name="password"></td>
                </tr>
                <tr> 
                  <td align="right">&nbsp;</td>
                  <td><input type="Submit" name="submit" value="Login"></td>
                </tr>
              </table>
            </form>
            <p>&nbsp;</p>
            <p>&nbsp;</p></td>
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