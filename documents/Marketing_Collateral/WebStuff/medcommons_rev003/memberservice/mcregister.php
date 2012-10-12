<!--
PAGE REVISON : 2.3
LAST UPDATED : 7.19.2005
BY : ALEX BEUSCHER
//-->
<?PHP
require '../dbparams.inc.php';
function generate_mcid() {
//set the random id length 
return rand(1000,9999).rand(1000,9999).rand(1000,9999).rand(1000,9999);
}
$returl = $_GLOBALS['CCReturn_Url']; // where to go back to
$mcid = generate_mcid(); // make an mcid
$x=<<<XXX
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>MedCommons</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<link href="../css/smallheader.css" rel="stylesheet" type="text/css">
<link href="../css/main.css" rel="stylesheet" type="text/css">
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
<h1>Register With MedCommons</h1>
<h2>To register with MedCommons you must obtain a Digital ID from Verisign, Inc and then install it into MedCommons</h2>
<h1>Step 1 - Obtain Your Digital ID from Verisign</h1>
<h2>You must <a href = "http://www.verisign.com/products-services/security-services/pki/pki-application/email-digital-id/page_dev004002.html">obtain a Verisign Digital ID</a></h2>
<h2>You can use a trial Digital ID if you choose, but some of the capabilities of the full ID will be absent</h2>
<h1>Step 2 - Verify Your Digital ID</h2>
<h2>Locate the Digital ID you want as your MedCommons ID</p>
<iframe id="EmailLookup" src=lookupbyemail.html name=email width="640" height="125" frameborder="0"></iframe>
<h1>Step 3 - Install Your Digital ID into MedCommons</h1>
<iframe src="installcert.php?mcid=$mcid"  name=install width="640" height="250" frameborder="0"></iframe>
<h1>Step 4 - Purchase Desktop #$mcid </h1>
<form method="POST" action="https://payments.verisign.com/payflowlink">
<input type="hidden" name="LOGIN" value="medcommons">
<input type="hidden" name="PARTNER" value="VeriSign">
<input type="hidden" name="AMOUNT" value="1.01">
<input type="hidden" name="TYPE" value="S">
<input type="hidden" name="DESCRIPTION" value="A New MedCommons Desktop $mcid">
<input type="hidden" name="USER1" size="16" value="$mcid">
<input type="hidden" name="USER2" size="255" value="$returl">
<input type="hidden" name="CUSTID" value="12345678">
<input type="hidden" name = "SHOWCONFIRM" value="False">
<input type="hidden" name = "ECHODATA" value="TTrue">
<input type="image" src="../images/1click-02.gif">
</form></p>
<div id="footer">&copy; 2005 MedCommons all rights reserved.&nbsp;&nbsp;&nbsp;page last modified:&nbsp;<script type="text/javascript"> document.write(document.lastModified); </script></div>

<!-- end page content //-->


</body>
</html>
XXX;


echo $x;
?>