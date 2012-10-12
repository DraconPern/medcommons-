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
<html>
<head>
<title>Register With MedCommons</title>
<link href="../main.css" rel="stylesheet" type="text/css">

</head>
<body> 
<table width="100%"  border="0" cellspacing="0" cellpadding="0"> 
  <tr> 
    <td> <div id="TopBody" class="PageContent"> 
        <iframe name="HeaderFrame" width="100%" height="35" src="../hptop_logosm.html" frameborder="0"></iframe> 
      </div></td> 
  </tr> 
  <tr> 
    <td><table width="100%"  border="0" cellspacing="0" cellpadding="0"> 
        <tr> 
          <td align="left" valign="top"><img src="../images/title_register.gif" width="350" height="50" class="Indent"></td> 
        </tr> 
        <tr> 
          <td align="left" valign="top"><p class="TextMain">To register with MedCommons you must obtain a Digital ID from Verisign, Inc and then install it into MedCommons</p>
          <p class="TextMain">&nbsp;</p></td> 
        </tr>
        <tr>
          <td align="left" valign="top"><p class="SubHead">Step 1 - Obtain Your Digital ID from Verisign </p></td>
        </tr>
        <tr>
          <td align="left" valign="top"><p class="TextMain"><br>
            You must <a href = "http://www.verisign.com/products-services/security-services/pki/pki-application/email-digital-id/page_dev004002.html">obtain a Verisign Digital ID</a> </p>
            <p class="TextMain">          You can use a trial Digital ID if you choose, but some of the capabilities of the full ID will be absent</p>
          <p class="TextMain">&nbsp;</p></td>
        </tr>
        <tr>
          <td align="left" valign="top"><p class="SubHead">Step 2 - Verify Your Digital ID </p></td>
        </tr>
        <tr>
          <td align="left" valign="top"><p class="TextMain"><br>
            Locate the Digital ID you want as your MedCommons ID</p>
            <p class="TextMain">&nbsp;</p></td>
        </tr>
        <tr>
          <td align="left" valign="top"><iframe id="EmailLookup" src=lookupbyemail.html name=email width="640" height="125" frameborder="0" class="IndentMore"></iframe> </td>
        </tr>
        <tr>
          <td align="left" valign="top"><p class="SubHead">Step 3 - Install Your Digital ID into MedCommons</p></td>
        </tr>
        <tr>
          <td align="left" valign="top">&nbsp;</td>
        </tr>
        <tr>
          <td align="left" valign="top"><iframe src=installcert.php?mcid=$mcid  name=install width="600" height=250 frameborder="0" class="IndentMore"></iframe></td>
        </tr>
        <tr>
          <td align="left" valign="top"><p class="SubHead">Step 4 - Purchase Desktop #$mcid</p></td>
        </tr>
        <tr>
          <td align="center" valign="top"><p class="TextMain">
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
</form></p></td>
        </tr>
        <tr>
          <td align="left" valign="top">&nbsp;</td>
        </tr> 
        <tr> 
          <td align="left" valign="top"> </td> 
        </tr> 
      </table></td> 
  </tr> 
</table>
 <table width="100%" cellpadding="0" cellspacing="0"> 
   <tr>
    <td align="right"><p class="MenuBottom"><a href="adminregister.php" target="_top">admin accounts</a> || <a href="../faq.html" target="_top">faq</a> || <a href="../termsofuse.html" target="_top">terms of use</a>&nbsp;</p>
    <p class="CopyRight">&copy;2005 MedCommons, Inc. all rights reserved &nbsp;</p></td>
  </tr>
</table>
</body>
</html>
XXX;


echo $x;
?>