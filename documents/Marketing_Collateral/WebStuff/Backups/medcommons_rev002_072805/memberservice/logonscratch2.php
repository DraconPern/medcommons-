<!--
PAGE REVISON : 2.3
LAST UPDATED : 7.19.2005
BY : ALEX BEUSCHER
//-->
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
<!--
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

function MM_swapImgRestore() { //v3.0
  var i,x,a=document.MM_sr; for(i=0;a&&i<a.length&&(x=a[i])&&x.oSrc;i++) x.src=x.oSrc;
}

function MM_preloadImages() { //v3.0
  var d=document; if(d.images){ if(!d.MM_p) d.MM_p=new Array();
    var i,j=d.MM_p.length,a=MM_preloadImages.arguments; for(i=0; i<a.length; i++)
    if (a[i].indexOf("#")!=0){ d.MM_p[j]=new Image; d.MM_p[j++].src=a[i];}}
}

function MM_findObj(n, d) { //v4.01
  var p,i,x;  if(!d) d=document; if((p=n.indexOf("?"))>0&&parent.frames.length) {
    d=parent.frames[n.substring(p+1)].document; n=n.substring(0,p);}
  if(!(x=d[n])&&d.all) x=d.all[n]; for (i=0;!x&&i<d.forms.length;i++) x=d.forms[i][n];
  for(i=0;!x&&d.layers&&i<d.layers.length;i++) x=MM_findObj(n,d.layers[i].document);
  if(!x && d.getElementById) x=d.getElementById(n); return x;
}

function MM_swapImage() { //v3.0
  var i,j=0,x,a=MM_swapImage.arguments; document.MM_sr=new Array; for(i=0;i<(a.length-2);i+=3)
   if ((x=MM_findObj(a[i]))!=null){document.MM_sr[j++]=x; if(!x.oSrc) x.oSrc=x.src; x.src=a[i+2];}
}
//-->
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
	background-color: #A4B4B0;
	position: absolute;
	height: 50px;
	width: 150px;
	left: 480px;
	top: 100px;
	padding: 3px;
	border: thin solid #009900;
	clear: none;
	overflow: hidden;
	z-index: 99;
}
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
-->
</style>
</head>
<body onload="init();MM_preloadImages('../images/logblock_over_03.gif')"> 
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
      <td width="195" bgcolor="#A4B4B0"><input name="userid" type="text" class="FormBox" value="" size="20">
&nbsp;&nbsp;</td>
      <td width="125" align="left" bgcolor="#A4B4B0"><p><a href="mcregister.html">register</a></p></td> 
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
      <tr>
        <td align="left" valign="top"><form name="logonForm" method="post" action="logonhandler.php"> 
      <input type="hidden" name="password" value="">
          <table width="350" border="0" cellpadding="0" cellspacing="0" bgcolor="#527463">
            <tr>
              <td colspan="2" class="LogBlockTop"><p>
                <input id="tracknum" type="text" name="userid">
              </p>
                <p>
                  <input id="pword" type="password" name="txtPassword">            
                      </p></td>
              </tr>
            <tr>
              <td width="292">&nbsp;</td>
              <td><a href="#" onMouseOut="MM_swapImgRestore()" onMouseOver="MM_swapImage('Image4','','../images/logblock_over_03.gif',1)"><img src="../images/logblock_03.gif" name="Image4" width="58" height="46" border="0"></a></td>
            </tr>
          </table> 
        </form>         <p class="SubHead">&nbsp;</p>
          <p class="SubHead">Tracking Number &amp; PIN Login </p>
          <p class="TextMain">If you have been supplied with a tracking number and PIN, enter these below to view a specific CCR.<br>
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
