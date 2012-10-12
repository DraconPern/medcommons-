<?PHP


/*
put this HTML back in to get the logon fields to show up again

        <form name="logonForm" method="post" action="logonhandler.php">
            <input type="hidden" name="password" value="">
            
              <tr><td>User Id:</td><td><input type="text" name="userid" size="20" value="">  &nbsp;&nbsp;<a href="register.php">register</a>
      </td></tr>
              <tr><td>Password:</td><td><input type="password" name="txtPassword" size="20"/></td></tr>
              <tr><td></td><td><input type="submit" value="Logon" onclick="document.logonForm.password.value=hex_sha1(txtPassword.value);"></td></tr>
              <tr><td>&nbsp;</td><td>&nbsp;</td></tr>
              </form>
              
              
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
if (isset($_COOKIE['MCID']) && isset($_COOKIE['MCGW']))
{
	logon_redirect($_COOKIE['MCGW'],$_COOKIE['MCID']);

	exit;
}
//if we have an error code, show it
$err = $_GET['err'];
if ($err=="") $err="Please enter your logon credentials:";
$x=<<<XXX
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>MedCommons</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<link href="../main.css" rel="stylesheet" type="text/css">
<script language="JavaScript" type="text/JavaScript">
<!--
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
<script language="JavaScript" src="sha1.js"></script>
<script language="javascript">
      function init() {
      
      }
</script>
</head>

<body onLoad="init(); MM_preloadImages('../images/topmenu_over_03.gif','../images/topmenu_over_04.gif','../images/topmenu_over_05.gif','../images/topmenu_over_06.gif','../images/topmenu_over_07.gif')">
<table width="100%"  border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td>
      <div id="TopBody" class="PageContent"><iframe width="100%" height="100" src="../hptop.html" frameborder="0"></iframe></div></td>
  </tr>
  <tr>
    <td><table width="100%"  border="0" cellspacing="0" cellpadding="0">
      <tr>
        <td width="100%" align="left" valign="top"><p>$err</p>
		 <table width="250" cellpadding="0" cellspacing="0" border="0">


              <form name="trackForm" method="post" action="../logservice/trackinghandler.php">
                <input type="hidden" name="hpin" value="">
            <tr><td>Tracking Number:</td><td><input type="text" name="trackingNumber" size="20" value=""></td></tr>
            <tr><td>PIN:</td><td><input type="password" name="pin" size="20" value=""></td></tr>
            <tr><td></td><td><input type="submit" value="Track" onclick="document.trackForm.hpin.value=hex_sha1(pin.value);"></td></tr>
                 </form>
        </table>
		
		</td>
      </tr>
    </table></td>
  </tr></table>
 <table width="100%" cellpadding="0" cellspacing="0"> 
   <tr>
    <td align="right"><p class="MenuBottom"><a href="../about.html" target="_top">about us</a> || <a href="../faq.html" target="_top">faq</a> || <a href="../termsofuse.html" target="_top">terms of use</a></p>
    <p class="style1">&copy;2005 MedCommons, Inc. all rights reserved &nbsp;</p></td>
  </tr>
</table>
</body>
</html>


XXX;
echo $x;
?>