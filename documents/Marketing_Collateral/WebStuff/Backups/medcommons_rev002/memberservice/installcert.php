<?PHP
$mcid = $_REQUEST['mcid'];
$x=<<<XXX
<HTML>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!--
 Copyright 2004 MedCommons Inc.   All Rights Reserved.
-->
  <head>
    <title>MedCommons Registration Step 3</title>
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
<form name=gw method=POST action=installcerthandler.php>
<input type="hidden" name="hpassword1" value="">
<input type="hidden" name="hpassword2" value="">
<input type="hidden" name="mcid" value="$mcid">
<TABLE BORDER=0 WIDTH=600 CELLPADDING=4 CELLSPACING=0>
<TR align=left valign="top" bgcolor="#A4B4B0"> 
<TD width="150" align="right" bgcolor="#A4B4B0"> <p align="right" class="style1">Password: </p></TD><TD width="200" bgcolor="#A4B4B0"><input name=password1 type=password class="FormBox" value="" size=20> </TD>
<TD width="250" bgcolor="#A4B4B0"> You must create a password to sign on to MedCommons </TD>
</TR>
<TR align=left valign="top" bgcolor="#A4B4B0"> 
<TD width="150" align="right" bgcolor="#A4B4B0"> <p align="right" class="style1">Confirm:&nbsp;</p></TD><TD width="200" bgcolor="#A4B4B0"> <input name=password2 type=password class="FormBox" value="" size=20> </TD>
<TD width="250" bgcolor="#A4B4B0"> You must enter the same password twice </TD>
</TR>
<tr valign="top" bgcolor="#A4B4B0"><TD width="150" align="right" bgcolor="#A4B4B0"> <p align="right" class="style1">Certificate Link:</p></TD><TD width="200" bgcolor="#A4B4B0"> <input name = certurl type=text class="FormBox" value="" size=30> </TD> 
<TD width="250" bgcolor="#A4B4B0"> <strong>Right click on the link from step 2 - your name followed by (Valid) - and select Copy Link Location(FF) or Copy Shortcut (IE) then paste in at left </strong></TD> 
</TR>
<tr valign="top" bgcolor="#A4B4B0">
  <TD align="right" bgcolor="#A4B4B0">&nbsp;</TD>
  <TD bgcolor="#A4B4B0"><input type=submit name=install value="Install" 
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
