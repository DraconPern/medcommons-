<?php
$db = mysql_connect("db95.perfora.net", "dbo100827707", "meddemo");
session_start();
if ($_SESSION['name'] == "") {

	if ($submit) {
	
	    mysql_select_db("db100827707",$db);

  		$sql = "SELECT * from medusers WHERE (email='" . $email . "')";

  		$result = mysql_query($sql);
		
		if ($myrow = mysql_fetch_row($result) and $password=="medcommons") {
			$_SESSION['name'] = $myrow[1];
			}
			else {
			Header ("Location: http://www.autocyt.com/mc/bad_login.php");
			}
		}		
		else {
		Header ("Location: http://www.autocyt.com/mc/bad_login.php");
		}
	}
?>

<php3>
<html>
<head>
<title>MEDCOMMONS</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<meta name="robots" content="none">
<link href="/css/main.css" rel="stylesheet" type="text/css">
<script language="JavaScript" src="functions/form_functions.js"></script>
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
          <td> 
            <p>Welcome back to MEDCOMMONS. You are logged in as <b><?php echo $_SESSION['name']; ?></b></p>
            <table width="400" border="0" align="center" cellpadding="0" cellspacing="0">
              <tr> 
                <td colspan="2" background="/mc/images/spacer_userhome.gif"><img src="/mc/images/spacer_userhome.gif" width="1" height="13"></td>
              </tr>
              <tr valign="top"> 
                <td> 
                  <p>Bring up a list of all studies available for viewing at 
                    <em>YOUR COMPANY NAME</em>.</p></td>
                <td><a href="/mc/hipaa.php"><img src="/mc/images/button_gocentral.gif" width="120" height="20" border="0"></a></td>
              </tr>
              <tr> 
                <td colspan="2" background="/mc/images/spacer_userhome.gif"><img src="/mc/images/spacer_userhome.gif" width="1" height="13"></td>
              </tr>
              <tr valign="top"> 
                <td> 
                  <p>You can download a Router / Gateway tool. Clearly, this 
                    tool has a specific function.</p></td>
                <td><a href="javascript:NewWindow('http://www.autocyt.com/mc/install_1.php', 'Installer', 480, 350, 'no')"><img src="/mc/images/button_gorouter.gif" width="120" height="20" border="0"></a></td>
              </tr>
			  <tr> 
                <td colspan="2" background="/mc/images/spacer_userhome.gif"><img src="/mc/images/spacer_userhome.gif" width="1" height="13"></td>
              </tr>
              <tr valign="top"> 
                <td> 
                  <p>The HIPAA log lists out currently available studies in 
                    your personal list. </p></td>
                <td><a href="/mc/hipaa.php"><img src="/mc/images/button_gohipaalog.gif" width="120" height="20" border="0"></a></td>
              </tr>
            </table>
            <p>&nbsp;</p>
            <p>&nbsp;</p>
            <p>&nbsp;</p>
            <p>&nbsp;</p>
            <p>&nbsp;</p></td>
        </tr>
        <tr> 
          <td>&nbsp;</td>
        </tr>
        <tr> 
          <td>&nbsp;</td>
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