<?PHP
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
if ($err=="<br><br>") $err=""; else $err="<h2 id='err'>".$err."</h2>";
$x=<<<XXX
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>MedCommons</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<link href="../css/smallheader.css" rel="stylesheet" type="text/css">
<link href="../css/main.css" rel="stylesheet" type="text/css">
<link href="../css/login.css" rel="stylesheet" type="text/css">
<script language="javascript" src="sha1.js"></script>
<script language="javascript">
function init() {  
}
</script>
</head>

<body>
<div class="menu">
<ul>
<li id="mmenu1"><a href="../hp.html"><img src="../images/mmtab_off_01.gif" border="0"></a></li>
<li id="mmenu2"><a href="logon.php?err=You%20must%20be%20logged%20in%20to%20sign%20a%20consent%20form."><img src="../images/mmtab_off_02.gif" border="0"></a></li>
<li id="mmenu3"><a href="logon.php?err=You%20must%20be%20logged%20in%20to%20create%20or%20view%20a%20CCR."><img src="../images/mmtab_off_03.gif" border="0"></a></li>
<li id="mmenu4"><a href="logon.php?err=You%20must%20be%20logged%20in%20to%20access%20your%20personalized%20desktop."><img src="../images/mmtab_off_04.gif" border="0"></a></li>
<li id="mmenu5"><a href="logon.php?err=You%20must%20be%20logged%20in%20to%20view%20images."><img src="../images/mmtab_off_05.gif" border="0"></a></li>
<li id="mmenu6"><img src="../images/mmtab_off_06.gif" border="0"></li>
</ul>
</div>
<div id="topframe"></div>
<div class="textmenu"><ul>
  <li><a href="../terms_of_use.htm">Terms of Use</a></li>
  <li><a href="../about.htm">About Us</a>&nbsp;&nbsp;||&nbsp;&nbsp;</li>
  <li><a href="../faq.htm">FAQ</a>&nbsp;&nbsp;||&nbsp;&nbsp;</li>
  <li><a href="../tour.htm">Tour</a>&nbsp;&nbsp;||&nbsp;&nbsp;</li>
</ul></div>


<!-- Begin Page Content //-->


<div id="content">
<br><br>
$err
<h2 style="width:300px;">Please enter a MedCommons Tracking Number and corresponding PIN to access your private medical communication.</h2>
<div id="loginblock">
<form name="logonForm" method="post" action="../logservice/trackinghandler.php">
<input id="tracknum" name="trackingNumber" type="text">
<input id="pword" name="pin" type="password">
<div id="help"><a href="#">help</a></div>
<div id="logbutton"><a href="javascript:document.logonForm.submit();" onclick="document.logonForm.password.value=hex_sha1(pin.value);"><img src="../images/logbutton_off.gif" width="50" height="35" border="0"></a></div>
</div>
<br><br><br><br><br><br><br><br><br><br><br><br>
</div>
<div id="footer" onload="javascript:document.logonForm.tracknum.focus();">&copy; 2005 MedCommons all rights reserved.&nbsp;&nbsp;&nbsp;page last modified:&nbsp;<script type="text/javascript"> document.write(document.lastModified); </script></div>


<!-- end page content //-->


</body>
</html>

XXX;
echo $x;
?>