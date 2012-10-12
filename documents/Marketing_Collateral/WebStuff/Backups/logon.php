<!--
PAGE REVISON : 2.3
LAST UPDATED : 7.22.2005
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
.LogBlockTop {
	background-image: url(../images/logblock_01.gif);
	height: 79px;
	width: 350px;
	background-repeat: no-repeat;
	margin: 3px;
}
#tracknum {
	border-style: solid;
	border-width: 1px;
	border-color:#000000;
	margin-top: 5px;
	margin-left: 150px;
	position: relative;
}
#pword {
	border-style: solid;
	border-width: 1px;
	border-color:#000000;
	margin-top: 0px;
	margin-left: 150px;
	position: relative;
}
#helplink {
	color:#FFFFFF;
	font-size: 9px;
	margin-left: 10px;
}
a:hover#helplink {
	color:#33FF00;
}
#copy {
	width: 100%;
	position: absolute;
	margin-bottom: 0px;
	text-align: right;

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
        <td align="left" valign="top"><img src="../images/title_login.gif" width="325" height="50" class="Indent"></td>
      </tr>
      <tr>
        <td align="left" valign="top"><p class="TextMain style1 style2">$err</p>
          </td>
      </tr>
      <tr>
        <td align="left" valign="top"><form name="logonForm" method="post" action="logonhandler.php"> 
      <input type="hidden" name="password" value="">
          <table width="450" border="0" cellpadding="0" cellspacing="0" bgcolor="#527463">
            <tr>
			<td rowspan="2" bgcolor="#ffffff"><img src="../images/spacer.gif" width="80" height="1"></td>
              <td colspan="2" class="LogBlockTop"><p>
                <input id="tracknum" type="text" name="userid" value="$user">
              </p>
                <p>
                  <input id="pword" type="password" name="txtPassword">            
                      </p></td>
              </tr>
            <tr>
              <td width="292"><a href="loginhelp.htm" target="_blank" id="helplink">help</a></td>
              <td><input type="image" src="../images/logblock_03.gif" name="Image4" width="58" height="46" border="0" onclick="document.logonForm.password.value=hex_sha1(txtPassword.value);"></td>
            </tr>
          </table> 
        </form><br><br></td>
      </tr>
	  <tr><td>
	  <table width="450" border="0" cellpadding="0" cellspacing="0">
            <tr>
              <td><p class="SubHead">Register with MedCommons</p>
                <p class="TextMain">Registering with MedCommons will allow you to create, modify, and transmit your own CCR's. <a href="mcregister.php">register now</a></p></td>
              </tr>

          </table>
	  
	  </td></tr>

      <!-- Insert Tracking Number Form Code Here --><tr><td><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br></td></tr>
    </table></td>
  </tr></table><div id="Message1" class="HideMe"> 
              <table width="150" border="0" cellspacing="0" cellpadding="0" class="WhyRegister"> 
                <tr> 
                  <td>Registering with MedCommons will allow you to create, modify, and transmit your own CCR's.</td> 
                </tr> 
              </table> 
            </div> 

    <div id="copy" align="right"><p class="CopyRight">&copy;2005 MedCommons, Inc. all rights reserved &nbsp;</p></td>

</body>
</html>


XXX;
echo $x;
?>
