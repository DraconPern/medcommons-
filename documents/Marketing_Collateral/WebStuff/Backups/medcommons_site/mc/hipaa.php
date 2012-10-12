<?php
$db = mysql_connect("db95.perfora.net", "dbo100827707", "meddemo");
session_start();
?>

<php3>
<html>
<head>
<title>MEDCOMMONS</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<meta name="robots" content="none">
<link href="/css/main.css" rel="stylesheet" type="text/css">
<style type="text/css">
<!--
.HeaderRow {
	font-family: Arial, Helvetica, sans-serif;
	font-size: 12px;
	font-weight: bold;
	color: #000000;
}
.SmallData {
	font-family: Arial, Helvetica, sans-serif;
	font-size: 9px;
	color: #000000;
}
.RegData {
	font-family: Arial, Helvetica, sans-serif;
	font-size: 10px;
	font-weight: normal;
	color: #000000;
}
.Status {
	font-family: Arial, Helvetica, sans-serif;
	font-size: 12px;
	font-style: italic;
	font-weight: normal;
	color: #000099;
}
-->
</style>
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
          <td><p>&nbsp;</p>
            <p><strong>HIPAA LOG 4</strong></p></td>
        </tr>
        <tr> 
          <td><table width="750" border="0" cellspacing="0" cellpadding="0">
              <tr> 
                <td colspan="8" background="/mc/images/spacer_userhome.gif"><img src="/mc/images/spacer_userhome.gif" width="1" height="13"></td>
              </tr>
              <tr> 
                <td width="150"> <p class="HeaderRow">Date and Time</p></td>
                <td width="150"> <p class="HeaderRow">Tracking No. </p></td>
                <td width="100"> <p class="HeaderRow">Recipient </p></td>
                <td width="60"> <p class="HeaderRow">Auth.</p></td>
                <td width="60"> <p class="HeaderRow">Mod.</p></td>
                <td width="60"> <p class="HeaderRow">Series </p></td>
                <td width="60"> <p class="HeaderRow">Images</p></td>
                <td width="60"> <p class="HeaderRow">Status</p></td>
              </tr>
              <tr> 
                <td width="150"> <p class="SmallData">May 6, 2004 17:45</p></td>
                <td width="150"> <p class="RegData"><a href="/mc/viewer.php">2376349871</a></p></td>
                <td width="100"> <p class="RegData">Vlad Tepes, MD</p></td>
                <td width="60"> <p class="RegData">A1</p></td>
                <td width="60"> <p class="RegData">CT</p></td>
                <td width="60"> <p class="RegData">8 </p></td>
                <td width="60"> <p class="RegData">458</p></td>
                <td width="60" class="Status"> <p class="Status">OK</p></td>
              </tr>
              <tr> 
                <td width="150"> <p class="SmallData">May 16, 2004 17:45</p></td>
                <td width="150"> <p class="RegData"><a href="/mc/viewer.php">2376349871</a></p></td>
                <td width="100"> <p class="RegData">Vlad Tepes, MD</p></td>
                <td width="60"> <p class="RegData">A1</p></td>
                <td width="60"> <p class="RegData">CT</p></td>
                <td width="60"> <p class="RegData">8 </p></td>
                <td width="60"> <p class="RegData">458</p></td>
                <td width="60" class="Status"> <p class="Status">OK</p></td>
              </tr>
              <tr> 
                <td width="150"> <p class="SmallData">May 20, 2004 17:45</p></td>
                <td width="150"> <p class="RegData"><a href="/mc/viewer.php">2376349871</a></p></td>
                <td width="100"> <p class="RegData">Vlad Tepes, MD</p></td>
                <td width="60"> <p class="RegData">A1</p></td>
                <td width="60"> <p class="RegData">CT</p></td>
                <td width="60"> <p class="RegData">8 </p></td>
                <td width="60"> <p class="RegData">458</p></td>
                <td width="60" class="Status"> <p class="Status">OK</p></td>
              </tr>
              <tr> 
                <td width="150"> <p class="SmallData">May 26, 2004 17:45</p></td>
                <td width="150"> <p class="RegData"><a href="/mc/viewer.php">2376349871</a></p></td>
                <td width="100"> <p class="RegData">Vlad Tepes, MD</p></td>
                <td width="60"> <p class="RegData">A1</p></td>
                <td width="60"> <p class="RegData">CT</p></td>
                <td width="60"> <p class="RegData">8 </p></td>
                <td width="60"> <p class="RegData">458</p></td>
                <td width="60" class="Status"> <p class="Status">OK</p></td>
              </tr>
              <tr> 
                <td width="150">&nbsp;</td>
                <td width="150">&nbsp;</td>
                <td width="100">&nbsp;</td>
                <td width="60">&nbsp;</td>
                <td width="60">&nbsp;</td>
                <td width="60">&nbsp;</td>
                <td width="60">&nbsp;</td>
                <td width="60">&nbsp;</td>
              </tr>
            </table></td>
        </tr>
      </table>
      
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