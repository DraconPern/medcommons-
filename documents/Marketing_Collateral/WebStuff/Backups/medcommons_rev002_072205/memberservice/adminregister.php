<?PHP
$error = $_REQUEST['err'];
$x=<<<XXX
<HTML>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!--
 Copyright 2004 MedCommons Inc.   All Rights Reserved.
-->
  <head>
    <title>MedCommons Admin Account Setup</title>
    <link href="main.css" rel="stylesheet" type="text/css">
    <style type="text/css">
      body {
        font-family: arial,helvetica;
      }

      td {
        font-size: 12px;
        text-align: left;
      }
    .style1 {
	font-size: 14px;
	font-weight: bold;
	color: #FFFFFF;
}
.FormBox {
	border: 1px solid #006666;
}
    </style>
    <script language="JavaScript" src="sha1.js"></script>
    <script language="javascript">
      function init() {
      
      }
    </script>
  </head>
<BODY >
<h4>Setup Shared Admin Account - No Verisign, No Credit Cards</h4>
<p>$err</p>
<form name=gw method=POST action=adminregisterhandler.php>
<input type="hidden" name="hpassword1" value="">
<input type="hidden" name="hpassword2" value="">
<TABLE BORDER=0 WIDTH=600 CELLPADDING=4 CELLSPACING=0>

<TR align=left valign="top" bgcolor="#A4B4B0"> 
<TD width="150" align="right" bgcolor="#A4B4B0"> <p align="right" class="style1">Email: </p></TD><TD width="200" bgcolor="#A4B4B0"><input name=email type=text class="FormBox" value="" size=20> </TD>
<TD width="250" bgcolor="#A4B4B0">The email address is the UserId for logon </TD>
</TR>

<TR align=left valign="top" bgcolor="#A4B4B0"> 
<TD width="150" align="right" bgcolor="#A4B4B0"> <p align="right" class="style1">Password: </p></TD><TD width="200" bgcolor="#A4B4B0"><input name=password1 type=password class="FormBox" value="" size=20> </TD>
<TD width="250" bgcolor="#A4B4B0"> You must create a password to sign on to MedCommons </TD>
</TR>
<TR align=left valign="top" bgcolor="#A4B4B0"> 
<TD width="150" align="right" bgcolor="#A4B4B0"> <p align="right" class="style1">Confirm:&nbsp;</p></TD><TD width="200" bgcolor="#A4B4B0"> <input name=password2 type=password class="FormBox" value="" size=20> </TD>
<TD width="250" bgcolor="#A4B4B0"> You must enter the same password twice </TD>
</TR>

<tr valign="top" bgcolor="#A4B4B0">
  <TD align="right" bgcolor="#A4B4B0">&nbsp;</TD>
  <TD bgcolor="#A4B4B0"><input type=submit name=install value="Create Shared Account" 
    onClick="document.gw.hpassword1.value=hex_sha1(password1.value);document.gw.hpassword2.value=hex_sha1(password2.value);"></TD>
  <TD bgcolor="#A4B4B0">&nbsp;</TD>
</TR> 
</TABLE>
<br>
</form>
</body>
XXX;

echo $x;

?>
