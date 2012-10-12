<!--
PAGE REVISON : 2.3
LAST UPDATED : 7.19.2005
BY : ALEX BEUSCHER
//-->
<?PHP


/*
put this HTML back in to get the Tracking Number fields to show up again
      <tr>
        <td align="left" valign="top"><p class="SubHead">&nbsp;</p>
          <p class="SubHead">Tracking Number &amp; PIN Login </p>          <p class="TextMain">If you have been supplied with a tracking number and PIN, enter these below to view a specific CCR.<br>
            <br>
          </p></td>
      </tr>
<tr>
        <td width="100%" align="left" valign="top">
		
<form name="trackForm" method="post" action="../logservice/trackinghandler.php"> 
  <input type="hidden" name="hpin" value=""> 
  <table width="545" cellpadding="2" cellspacing="0"> 
    <tr>
      <td rowspan="3"><img src="../images/spacer.gif" width="100" height="1"></td> 
      <td width="125" align="right" bgcolor="#A4B4B0"><span class="style5">Tracking #:</span></td> 
      <td width="195" bgcolor="#A4B4B0"><input name="trackingNumber" type="text" class="FormBox" value="" size="20"></td>
      <td width="125" bgcolor="#A4B4B0">&nbsp;</td> 
    </tr> 
    <tr>
      <td width="125" align="right" bgcolor="#A4B4B0"><span class="style5">PIN:</span></td> 
      <td width="195" bgcolor="#A4B4B0"><input name="pin" type="password" class="FormBox" value="" size="20"></td>
      <td width="125" bgcolor="#A4B4B0">&nbsp;</td> 
    </tr> 
    <tr>
      <td width="125" align="right" bgcolor="#A4B4B0"></td> 
      <td width="195" bgcolor="#A4B4B0"><input name="submit" type="submit" onClick="document.trackForm.hpin.value=hex_sha1(pin.value);" value="View"></td>
      <td width="125" bgcolor="#A4B4B0">&nbsp;</td> 
    </tr> 
  </table> 
</form></td>
      </tr>
              
              
 */
 
 
	function logon_redirect($g,$m)
{
$url = "$g/logon.jsp?mcid=$m";
$x=<<<XXX
<html><head><title>Redirecting to MedCommons Repository Gateway</title>
<meta http-equiv="REFRESH" content="0;url=$url"></HEAD>
<body >
<p>
Please wait while we gather your records...
</p>
</body>
</html>
XXX;
echo $x;
}
//main
//if a cookie exists, then just go directly to the patient's desktop
//if (isset($_COOKIE['MCID']) && isset($_COOKIE['MCGW']))
//{
//	logon_redirect($_COOKIE['MCGW'],$_COOKIE['MCID']);
//
//	exit;
//}
//if we have an error code, show it
$user = $_GET['user'];
$err = $_GET['err'] . "<br><br>";
if ($err=="<br><br>") $err="&nbsp;";
$x=<<<XXX
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!--
 Copyright 2004 MedCommons Inc.   All Rights Reserved.
-->
<html>
<head>
<title>MedCommons Logon</title>
<link href="../main.css" rel="stylesheet" type="text/css">
<script language="JavaScript" src="sha1.js"></script>
<script language="javascript">
      function init() {
      
      }
	  function ShowCaption(numMessage) {
  var visible = 'block';
  var hidden = 'none';
  var MessageBlock=document.getElementById("Message" + numMessage);
  MessageBlock.className = 'ShowMe';

}

function HideCaption(numMessage) {

  var visible = 'block';
  var hidden = 'none';
  var MessageBlock=document.getElementById("Message" + numMessage);
  MessageBlock.className = 'HideMe';

}
    </script>
<style type="text/css">
<!--
.FormBox {
	width: 125px;
	border: 1px solid #006666;
}
.style1 {
	font-size: 18px;
	font-weight: bold;
	font-style: italic;
}
.style2 {font-size: 14px}
.style5 {
	color: #FFFFFF;
	font-weight: bold;
}
.style6 {font-size: 10px}
.WhyRegister {
	background-color: #527463;
	position: absolute;
	height: 50px;
	width: 150px;
	left: 480px;
	top: 100px;
	padding: 3px;
	border: thin solid #000000;
	font-family: Arial, Helvetica, sans-serif;
	font-size: 12px;
	font-weight: bold;
	color: #FFFFFF;
	clear: none;
	overflow: hidden;
	z-index: 99;
}
-->
</style>
</head>
<body onload="init();"> 
<table width="100%"  border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td>
      <div id="TopBody" class="PageContent"><iframe name="HeaderFrame" width="100%" height="35" src="../hptop_login.html" frameborder="0"></iframe></div></td>
  </tr>
  <tr>
    <td><table width="100%"  border="0" cellspacing="0" cellpadding="0">
      <tr>
        <td align="left" valign="top"><img src="../images/title_login.gif" width="350" height="50" class="Indent"></td>
      </tr>
      <tr>
        <td align="left" valign="top"><p class="TextMain style1 style2">$err</p>
          </td>
      </tr>
      <tr>
        <td align="left" valign="top"><form name="logonForm" method="post" action="logonhandler.php"> 
      <input type="hidden" name="password" value="">
      <p class="SubHead">User Login    </p>
      <p class="TextMain">If you are a registered user, please use the form below to enter MedCommons.</p>
      <table width="545" cellpadding="2" cellspacing="0"> 
    <tr>
      <td rowspan="3"><img src="../images/spacer.gif" width="100" height="1"></td> 
      <td width="125" align="right" bgcolor="#A4B4B0"><span class="style5">User Id:</span></td> 
      <td width="195" bgcolor="#A4B4B0"><input name="userid" type="text" class="FormBox" value="$user" size="20">
&nbsp;&nbsp;</td>
      <td width="125" align="left" bgcolor="#A4B4B0"><p><a href="mcregister.php">register</a></p></td> 
    </tr> 
    <tr>
      <td width="125" align="right" bgcolor="#A4B4B0"><span class="style5">Password:</span></td> 
      <td width="195" bgcolor="#A4B4B0"><input name="txtPassword" type="password" class="FormBox" size="20"></td>
      <td width="125" align="left" bgcolor="#A4B4B0"><p class="style6"><a href="#" onMouseOver="ShowCaption(1);" onMouseOut="HideCaption(1);">why register? </a></p></td> 
    </tr> 
    <tr>
      <td width="125" align="right" bgcolor="#A4B4B0">&nbsp;</td> 
      <td width="195" bgcolor="#A4B4B0"><input type="submit" value="Logon" onclick="document.logonForm.password.value=hex_sha1(txtPassword.value);"></td>
      <td width="125" align="left" bgcolor="#A4B4B0">&nbsp;</td> 
    </tr> 
  </table> 
</form> </td>
      </tr>

      <!-- Insert Tracking Number Form Code Here --><tr><td><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br></td></tr>
    </table></td>
  </tr></table><div id="Message1" class="HideMe"> 
              <table width="150" border="0" cellspacing="0" cellpadding="0" class="WhyRegister"> 
                <tr> 
                  <td>Registering with MedCommons will allow you to create, modify, and transmit your own CCR's.</td> 
                </tr> 
              </table> 
            </div> 
 <table width="100%" cellpadding="0" cellspacing="0"> 
   <tr>
    <td align="right"><p class="MenuBottom"><a href="../about.html" target="_top">about us</a> || <a href="../faq.html" target="_top">faq</a> || <a href="../termsofuse.html" target="_top">terms of use</a>&nbsp;</p>
    <p class="CopyRight">&copy;2005 MedCommons, Inc. all rights reserved &nbsp;</p></td>
  </tr>
</table>
</body>
</html>


XXX;
echo $x;
?>