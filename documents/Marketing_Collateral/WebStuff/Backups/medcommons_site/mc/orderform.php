<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<title>MedDemo</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<meta name="robots" content="none">
<script language="JavaScript" src="functions/form_functions.js"></script>
<?php
if ($orient=="portrait") {
?>
<style type="text/css">
<!--
.FolderTab {
	border-top-width: 1px;
	border-right-width: 1px;
	border-top-style: solid;
	border-right-style: solid;
	border-top-color: #000000;
	border-right-color: #000000;
	border-bottom-color: #000000;
	border-left-color: #000000;
	height: 25px;
}
.FolderTop {
	border-bottom-width: 1px;
	border-bottom-style: solid;
	border-top-color: #000000;
	border-right-color: #000000;
	border-bottom-color: #000000;
	border-left-color: #000000;
}
.FolderLeft {
	border-left-width: 1px;
	border-left-style: solid;
	border-top-color: #000000;
	border-right-color: #000000;
	border-bottom-color: #000000;
	border-left-color: #000000;
}
.FolderHeader {
	font-family: Arial, Helvetica, sans-serif;
	font-size: 12px;
	font-weight: bold;
	text-indent: 4px;
}

.TrackingField {
	background-color: #FFFFFF;
	border: 1px solid CCCCFF;
	width: 270px;
	position: relative;
	height: 25px;
	left: 12px;
	top: 14px;
}

.AccountFieldLeft {
	background-color: E1E3F0;
	width: 110px;
	left: 5px;
	top: 10px;
	position: relative;
	height: 25px;
}
.AccountFieldLarge {
	background-color: E1E3F0;
	width: 270px;
	left: 5px;
	top: 10px;
	position: relative;
	height: 25px;
}

.AccountFieldSmall {
	background-color: E1E3F0;
	width: 40px;
	left: 0px;
	top: 10px;
	position: relative;
	height: 25px;
}

.AccountFieldRight {
	background-color: E1E3F0;
	width: 96px;
	left: 0px;
	top: 10px;
	position: relative;
	height: 25px;
}
.FieldHeader {
	font-family: Arial, Helvetica, sans-serif;
	font-size: 10px;
	font-weight: bold;
	color: #999999;
	text-indent: 5px;
}

.CreditCardField {
	background-color: E1E3F0;
	width: 150px;
	left: 5px;
	top: 0px;
	position: relative;
	height: 25px;
}
.CreditCardFieldRight {
	background-color: E1E3F0;
	width: 100px;
	left: 5px;
	top: 0px;
	position: relative;
	height: 25px;
}

.HIPAAblock {
	background-color: E1E3F0;
	overflow: hidden;
	height: 40px;
	width: 270px;
	left: 5px;
	top: 5px;
	position: relative;
	font-family: Arial, Helvetica, sans-serif;
	font-size: 9px;
	color: #000000;
}

.RightColHeader {
	font-family: Arial, Helvetica, sans-serif;
	font-size: 16px;
	font-weight: bold;
	color: #000000;
}
.RightFieldHeader {
	font-family: Arial, Helvetica, sans-serif;
	font-size: 12px;
	font-weight: bold;
	color: #000000;
}

.RightColTextField {
	background-image: url(/mc/images/fieldbackg_rightscreen.gif);
	position: relative;
	height: 150px;
	width: 320px;
	top: 5px;
	border: none;
	overflow: hidden;
	font-family: "Courier New", Courier, mono;
	font-size: 12px;
	line-height: 30px;
}
.RightColTextSmall {
	background-color: #FFFFFF;
	border: none;
	position: relative;
	height: 30px;
	width: 320px;
}
body {
	margin: 0px;
	padding: 0px;
	overflow: hidden;
	background-color: DCDFEE;
}
.ThumbLabel {
	font-family: Arial, Helvetica, sans-serif;
	font-size: 12px;
	font-weight: bold;
	color: #FFFFFF;
	padding-left: 5px;
}
.ImageCount {
	font-family: Arial, Helvetica, sans-serif;
	font-size: 12px;
	font-style: normal;
	font-weight: normal;
	color: #CCCCCC;
	text-align: right;
	text-indent: 5px;
}

.FooterCell {
	overflow: hidden;
	position: relative;
	height: 50px;
}
-->
</style>
</head>
<body>
<FORM name=form1 action="/router/OrderProcessorServlet" method=post>
  <INPUT TYPE="HIDDEN" NAME="guid" VALUE="59b5be93b8a1782277b0ffec44cbf168">
  <table width="750" height="1000" border="0" align="left" cellpadding="0" cellspacing="0">
    <tr> 
      <td width="750" height="750"> <table width="750" height="750" border="0" align="left" cellpadding="0" cellspacing="0">
          <tr align="left" valign="top"> 
            <td width="330" bgcolor="BFC4E1"><table width="330" border="0" cellspacing="0" cellpadding="0">
                <tr> 
                  <td><table width="330" border="0" cellpadding="0" cellspacing="0">
                      <tr> 
                        <td width="20"><img src="/mc/images/spacer_blank.gif" width="20" height="18"></td>
                        <td width="72"><img src="/mc/images/spacer_blank.gif" width="72" height="1"></td>
                        <td width="218"><img src="/mc/images/spacer_blank.gif" width="218" height="8"></td>
                        <td width="20"><img src="/mc/images/spacer_blank.gif" width="20" height="1"></td>
                      </tr>
                      <tr> 
                        <td><img src="/mc/images/spacer_blank.gif" width="20" height="1"></td>
                        <td width="72" bgcolor="E1E3F0" class="FolderTab"><p class="FolderHeader">Tracking</p></td>
                        <td width="218" class="FolderTop">&nbsp;</td>
                        <td><img src="/mc/images/spacer_blank.gif" width="20" height="1"></td>
                      </tr>
                      <tr> 
                        <td rowspan="2"><img src="/mc/images/spacer_blank.gif" width="1" height="80"></td>
                        <td colspan="2" bgcolor="E1E3F0" height="40"><INPUT class=TrackingField name=tracking value="771EF"></td>
                        <td rowspan="2" class="FolderLeft">&nbsp;</td>
                      </tr>
                      <tr> 
                        <td colspan="2" bgcolor="E1E3F0" height="40"><img src="/mc/images/medcommons_trackingfooter.gif" width="300" height="40"></td>
                      </tr>
                    </table></td>
                </tr>
                <tr> 
                  <td><table width="330" border="0" cellspacing="0" cellpadding="0">
                      <tr> 
                        <td width="20"><img src="/mc/images/spacer_blank.gif" width="20" height="18"></td>
                        <td width="72"><img src="/mc/images/spacer_blank.gif" width="72" height="1"></td>
                        <td width="218"><img src="/mc/images/spacer_blank.gif" width="218" height="1"></td>
                        <td width="20"><img src="/mc/images/spacer_blank.gif" width="20" height="1"></td>
                      </tr>
                      <tr> 
                        <td><img src="/mc/images/spacer_blank.gif" width="20" height="1"></td>
                        <td width="72" bgcolor="E1E3F0" class="FolderTab"><p class="FolderHeader">Account</p></td>
                        <td width="218" class="FolderTop">&nbsp;</td>
                        <td><img src="/mc/images/spacer_blank.gif" width="20" height="1"></td>
                      </tr>
                      <tr> 
                        <td><img src="/mc/images/spacer_blank.gif" width="1" height="125"></td>
                        <td colspan="2" bgcolor="E1E3F0" valign="top"><table width="300" border="0" cellspacing="0" cellpadding="0">
                            <tr> 
                              <td colspan="3"><INPUT class=AccountFieldLeft name=name value="Joseph B Doe"></td>
                            </tr>
                            <tr> 
                              <td colspan="3"><img src="/mc/images/spacer_blank.gif" width="1" height="10"></td>
                            </tr>
                            <tr> 
                              <td colspan="3"><TEXTAREA class=HIPAAblock  name=address>123 Lucky St.</TEXTAREA></td>
                            </tr>
                            <tr> 
                              <td width="120"><INPUT class=AccountFieldLeft name=city value="Butte"></td>
                              <td width="60" align="center"> <INPUT class=AccountFieldSmall name=state value="MT"> 
                              </td>
                              <td width="120"><INPUT class=AccountFieldRight name=zip value="83132"></td>
                            </tr>
                          </table></td>
                        <td class="FolderLeft">&nbsp;</td>
                      </tr>
                    </table></td>
                </tr>
                <tr> 
                  <td><table width="330" border="0" cellspacing="0" cellpadding="0">
                      <tr> 
                        <td width="20"><img src="/mc/images/spacer_blank.gif" width="20" height="18"></td>
                        <td width="72"><img src="/mc/images/spacer_blank.gif" width="72" height="1"></td>
                        <td width="218"><img src="/mc/images/spacer_blank.gif" width="218" height="1"></td>
                        <td width="20"><img src="/mc/images/spacer_blank.gif" width="20" height="1"></td>
                      </tr>
                      <tr> 
                        <td><img src="/mc/images/spacer_blank.gif" width="20" height="1"></td>
                        <td width="72" bgcolor="E1E3F0" class="FolderTab"><p class="FolderHeader">Charges</p></td>
                        <td width="218" class="FolderTop">&nbsp;</td>
                        <td><img src="/mc/images/spacer_blank.gif" width="20" height="1"></td>
                      </tr>
                      <tr> 
                        <td>&nbsp;</td>
                        <td colspan="2" bgcolor="E1E3F0"> <table width="300" border="0" cellspacing="0" cellpadding="0">
                            <tr> 
                              <td width="170" valign="bottom"> <p class="FieldHeader">card 
                                  number</p></td>
                              <td width="130"><p class="FieldHeader">exp</p></td>
                            </tr>
                            <tr> 
                              <td width="170"><INPUT class=CreditCardField name=cardnumber value="7817574478133225"></td>
                              <td width="130"><INPUT class=CreditCardFieldRight  name=expiration value="12/09"></td>
                            </tr>
                            <tr> 
                              <td colspan="2" class="FieldHeader"><img src="/mc/images/spacer_blank.gif" width="1" height="5"></td>
                            </tr>
                            <tr> 
                              <td width="170" class="FieldHeader"> <p align="right">amount</p></td>
                              <td width="130"><INPUT class=CreditCardFieldRight name=amount value="150.00"></td>
                            </tr>
                            <tr> 
                              <td width="170" class="FieldHeader"> <p align="right">tax</p></td>
                              <td width="130"><INPUT class=CreditCardFieldRight name=tax value="12.00"></td>
                            </tr>
                            <tr> 
                              <td width="170" class="FieldHeader"> <p align="right">charge</p></td>
                              <td width="130"><INPUT class=CreditCardFieldRight name=charge value="162.00"></td>
                            </tr>
                          </table></td>
                        <td class="FolderLeft">&nbsp;</td>
                      </tr>
                    </table></td>
                </tr>
                <tr> 
                  <td><table width="330" border="0" cellspacing="0" cellpadding="0">
                      <tr> 
                        <td width="20"><img src="/mc/images/spacer_blank.gif" width="20" height="18"></td>
                        <td width="72"><img src="/mc/images/spacer_blank.gif" width="72" height="1"></td>
                        <td width="218"><img src="/mc/images/spacer_blank.gif" width="218" height="1"></td>
                        <td width="20"><img src="/mc/images/spacer_blank.gif" width="20" height="1"></td>
                      </tr>
                      <tr> 
                        <td><img src="/mc/images/spacer_blank.gif" width="20" height="1"></td>
                        <td width="72" bgcolor="E1E3F0" class="FolderTab"><p class="FolderHeader">HIPAA</p></td>
                        <td width="218" class="FolderTop">&nbsp;</td>
                        <td><img src="/mc/images/spacer_blank.gif" width="20" height="1"></td>
                      </tr>
                      <tr> 
                        <td><img src="/mc/images/spacer_blank.gif" width="1" height="146"></td>
                        <td colspan="2" bgcolor="E1E3F0" valign="top"><table width="300" border="0" cellspacing="0" cellpadding="0">
                            <tr> 
                              <td align="center" valign="middle"><img src="/mc/images/spacer_blank.gif" width="1" height="15"></td>
                            </tr>
                            <tr> 
                              <td valign="middle"><TEXTAREA class=HIPAAblock name=signature1>MEDCOMMONS</TEXTAREA></td>
                            </tr>
                            <tr> 
                              <td valign="middle"><TEXTAREA class=HIPAAblock name=signature2>Joe's Imaging Centers Inc.</TEXTAREA></td>
                            </tr>
                            <tr> 
                              <td valign="middle"><TEXTAREA class=HIPAAblock name=signature3></TEXTAREA></td>
                            </tr>
                          </table></td>
                        <td class="FolderLeft">&nbsp;</td>
                      </tr>
                    </table></td>
                </tr>
                <tr> 
                  <td><table width="100%" border="0" cellspacing="0" cellpadding="0">
                      <tr align="center">
                        <td><img src="/mc/images/spacer_blank.gif" width="1" height="5"></td>
                        <td><img src="/mc/images/spacer_blank.gif" width="1" height="5"></td>
                      </tr>
                      <tr align="center"> 
                        <td><a href="javascript:window.print();"><img src="/mc/images/button_print.gif" width="70" height="20" border="0"></a></td>
                        <td><a href="javascript:SendOrderForm();"><img src="/mc/images/button_send.gif" width="70" height="20" border="0"></a></td>
                      </tr>
                    </table></td>
                </tr>
              </table></td>
            <td width="420" bgcolor="DCDFEE"> <table width="420" border="0" cellspacing="0" cellpadding="0">
                <tr> 
                  <td width="60"><img src="/mc/images/formspacer_rightscreen.gif" width="60" height="18"></td>
                  <td width="320"><img src="/mc/images/formspacer_rightscreen.gif" width="320" height="1"></td>
                  <td width="40"><img src="/mc/images/formspacer_rightscreen.gif" width="40" height="1"></td>
                </tr>
                <tr> 
                  <td width="60">&nbsp;</td>
                  <td width="320"><p class="RightColHeader">Night Interpretation 
                      Order</p></td>
                  <td width="40">&nbsp;</td>
                </tr>
                <tr> 
                  <td colspan="3"><img src="/mc/images/formspacer_rightscreen.gif" width="1" height="15"></td>
                </tr>
                <tr> 
                  <td width="60">&nbsp;</td>
                  <td width="320"><p class="RightFieldHeader">History (Non Private 
                      Information)</p></td>
                  <td width="40">&nbsp;</td>
                </tr>
                <tr> 
                  <td width="60">&nbsp;</td>
                  <td width="320"><TEXTAREA class=RightColTextField name=history>Unknown</TEXTAREA></td>
                  <td width="40">&nbsp;</td>
                </tr>
                <tr> 
                  <td colspan="3"><img src="/mc/images/formspacer_rightscreen.gif" width="1" height="15"></td>
                </tr>
                <tr> 
                  <td width="60" height="20">&nbsp;</td>
                  <td width="320"><p class="RightFieldHeader">Comments</p></td>
                  <td width="40">&nbsp;</td>
                </tr>
                <tr> 
                  <td width="60">&nbsp;</td>
                  <td width="320"><TEXTAREA class=RightColTextField name=comments>CERVICAL SPINE</TEXTAREA></td>
                  <td width="40">&nbsp;</td>
                </tr>
                <tr> 
                  <td colspan="3"><img src="/mc/images/formspacer_rightscreen.gif" width="1" height="15"></td>
                </tr>
                <tr> 
                  <td>&nbsp;</td>
                  <td><p class="RightFieldHeader">Copy To</p></td>
                  <td>&nbsp;</td>
                </tr>
                <tr> 
                  <td width="60">&nbsp;</td>
                  <td width="320"><INPUT class=RightColTextSmall name=copyto value="agropper@medcommons.org"></td>
                  <td width="40">&nbsp;</td>
                </tr>
                <tr> 
                  <td width="60">&nbsp;</td>
                  <td width="320">&nbsp;</td>
                  <td width="40">&nbsp;</td>
                </tr>
                <tr> 
                  <td width="60">&nbsp;</td>
                  <td width="320">&nbsp;</td>
                  <td width="40">&nbsp;</td>
                </tr>
              </table></td>
          </tr>
        </table></td>
    </tr>
    <tr>
      <td> <table width="750" border="0" cellspacing="0" cellpadding="0">
          <tr> 
            <td bgcolor="003366"> <table width="750" height="200" border="0" cellpadding="0" cellspacing="0">
                <tr> 
                  <td><img src="images/bformspacer.gif" width="5" height="1"></td>
                  <td valign="top"> <table width="140" border="0" cellspacing="0" cellpadding="0">
                      <tr> 
                        <td><img src="/mc/images/thumbnail_order.gif" width="140" height="140"></td>
                      </tr>
                      <tr> 
                        <td> <p class="ThumbLabel"> Interpretation Order</p>
                          <p class="ThumbLabel">1 page</p></td>
                      </tr>
                    </table></td>
                  <td valign="top"> <table width="140" border="0" cellspacing="0" cellpadding="0">
                      <tr> 
                        <td colspan="2">&nbsp;</td>
                      </tr>
                      <tr> 
                        <td width="90"><p class="ThumbLabel">imagelabel1</p></td>
                        <td width="50"><p class="ImageCount">00:00&nbsp;&nbsp;</p></td>
                      </tr>
                      <tr> 
                        <td colspan="2"><img src="images/bformspacer.gif" width="1" height="5"></td>
                      </tr>
                      <tr> 
                        <td colspan="2"><p class="ThumbLabel"> 99 images</p></td>
                      </tr>
                    </table></td>
                  <td valign="top"> <table width="140" border="0" cellspacing="0" cellpadding="0">
                      <tr> 
                        <td colspan="2"><a href="viewer.php?orient=<?php echo($orient) ?>"><img name="thumb1" border="0" src="images/thumbnail1_off.gif" width="140" height="140"></a><a href="viewer.php?orient=<?php echo($orient) ?>"><img name="thumb2" border="0" src="images/thumbnail1_off.gif" width="140" height="140"></a></td>
                      </tr>
                      <tr> 
                        <td width="90"><p class="ThumbLabel">imagelabel1</p></td>
                        <td width="50"><p class="ImageCount">00:00&nbsp;&nbsp;</p></td>
                      </tr>
                      <tr> 
                        <td colspan="2"><img src="images/bformspacer.gif" width="1" height="5"></td>
                      </tr>
                      <tr> 
                        <td colspan="2"><p class="ThumbLabel"> 99 images</p></td>
                      </tr>
                    </table></td>
                  <td valign="top"> <table width="140" border="0" cellspacing="0" cellpadding="0">
                      <tr> 
                        <td colspan="2"><a href="viewer.php?orient=<?php echo($orient) ?>"><img name="thumb3" border="0" src="images/thumbnail1_off.gif" width="140" height="140"></a></td>
                      </tr>
                      <tr> 
                        <td width="90"><p class="ThumbLabel">imagelabel1</p></td>
                        <td width="50"><p class="ImageCount">00:00&nbsp;&nbsp;</p></td>
                      </tr>
                      <tr> 
                        <td colspan="2"><img src="images/bformspacer.gif" width="1" height="5"></td>
                      </tr>
                      <tr> 
                        <td colspan="2"><p class="ThumbLabel"> 99 images</p></td>
                      </tr>
                    </table></td>
                  <td><table width="180" border="0" cellspacing="0" cellpadding="0">
              <tr align="center" valign="middle"> 
                <td height="50"><img src="/mc/images/tool_boxes.jpg" width="40" height="40"></td>
                <td height="50"><img src="/mc/images/tool_r.jpg" width="40" height="40"></td>
                <td height="50"><a href="/mc/hipaa.php"><img src="/mc/images/tool_x.jpg" width="40" height="40" border="0"></a></td>
              </tr>
              <tr align="center" valign="middle"> 
                <td height="50"><img src="/mc/images/tool_compare.jpg" width="40" height="40"></td>
                <td height="50"><img src="/mc/images/tool_whitea.jpg" width="40" height="40"></td>
                <td height="50"><img src="/mc/images/tool_reset.jpg" width="40" height="40"></td>
              </tr>
              <tr align="center" valign="middle"> 
                <td height="50"><img src="/mc/images/tool_dicom.jpg" width="40" height="40"></td>
                <td height="50"><img src="/mc/images/tool_blacka.jpg" width="40" height="40"></td>
                <td height="50"><a href="/mc/hipaa.php"><img src="/mc/images/tool_ibutton.jpg" width="40" height="40" border="0"></a></td>
              </tr>
                      <tr align="center" valign="middle"> 
                        <td colspan="3"><img src="images/bformspacer.gif" width="1" height="15"></td>
                      </tr>
                      <tr align="center" valign="middle"> 
                        <td colspan="3"><img src="/mc/images/medcommons_bottomfooter.gif" width="180" height="35"></td>
                      </tr>
                    </table></td>
                  <td><img src="images/bformspacer.gif" width="5" height="1"></td>
                </tr>
              </table></td>
          </tr>
          <tr> 
            <td height="50" align="center" valign="middle" bgcolor="#000000" class="FooterCell"><p><font color="#999999" size="2" face="Arial, Helvetica, sans-serif"><strong>1 
                Series total - Doe Joseph B</strong></font><br>
                <select name="actionmenu" onchange="javascript:DropDownAction(this);">
                  <option value="" selected>choose option</option>
                  <option value="1">option 1</option>
                  <option value="2">option 2</option>
                  <option value="3">option 3</option>
                  <option value="">--------</option>
                  <option value="4">option 4</option>
                  <option value="5">option 5</option>
                  <option value="6">option 6</option>
                  <option value="">--------</option>
                  <option value="7">option 7</option>
                  <option value="8">option 8</option>
                  <option value="9">option 9</option>
                </select>
              </p></td>
          </tr>
        </table></td>
    </tr>
  </table>
</form>
</body>
<?php
}
else {
?>
<style type="text/css">
<!--
.FolderTab {
	border-top-width: 1px;
	border-right-width: 1px;
	border-top-style: solid;
	border-right-style: solid;
	border-top-color: #000000;
	border-right-color: #000000;
	border-bottom-color: #000000;
	border-left-color: #000000;
	height: 25px;
}
.FolderTop {
	border-bottom-width: 1px;
	border-bottom-style: solid;
	border-top-color: #000000;
	border-right-color: #000000;
	border-bottom-color: #000000;
	border-left-color: #000000;
}
.FolderLeft {
	border-left-width: 1px;
	border-left-style: solid;
	border-top-color: #000000;
	border-right-color: #000000;
	border-bottom-color: #000000;
	border-left-color: #000000;
}
.FolderHeader {
	font-family: Arial, Helvetica, sans-serif;
	font-size: 12px;
	font-weight: bold;
	text-indent: 4px;
}

.TrackingField {
	background-color: #FFFFFF;
	border: 1px solid CCCCFF;
	width: 270px;
	position: relative;
	height: 25px;
	left: 12px;
	top: 14px;
}

.AccountFieldLeft {
	background-color: E1E3F0;
	width: 110px;
	left: 5px;
	top: 10px;
	position: relative;
	height: 25px;
}
.AccountFieldLarge {
	background-color: E1E3F0;
	width: 270px;
	left: 5px;
	top: 10px;
	position: relative;
	height: 25px;
}

.AccountFieldSmall {
	background-color: E1E3F0;
	width: 40px;
	left: 0px;
	top: 10px;
	position: relative;
	height: 25px;
}

.AccountFieldRight {
	background-color: E1E3F0;
	width: 96px;
	left: 0px;
	top: 10px;
	position: relative;
	height: 25px;
}
.FieldHeader {
	font-family: Arial, Helvetica, sans-serif;
	font-size: 10px;
	font-weight: bold;
	color: #999999;
	text-indent: 5px;
}

.CreditCardField {
	background-color: E1E3F0;
	width: 150px;
	left: 5px;
	top: 0px;
	position: relative;
	height: 25px;
}
.CreditCardFieldRight {
	background-color: E1E3F0;
	width: 100px;
	left: 5px;
	top: 0px;
	position: relative;
	height: 25px;
}

.HIPAAblock {
	background-color: E1E3F0;
	overflow: hidden;
	height: 40px;
	width: 270px;
	left: 5px;
	top: 5px;
	position: relative;
	font-family: Arial, Helvetica, sans-serif;
	font-size: 9px;
	color: #000000;
}

.RightColHeader {
	font-family: Arial, Helvetica, sans-serif;
	font-size: 16px;
	font-weight: bold;
	color: #000000;
}
.RightFieldHeader {
	font-family: Arial, Helvetica, sans-serif;
	font-size: 12px;
	font-weight: bold;
	color: #000000;
}

.RightColTextField {
	background-image: url(/mc/images/fieldbackg_rightscreen.gif);
	position: relative;
	height: 150px;
	width: 320px;
	top: 5px;
	border: none;
	overflow: hidden;
	font-family: "Courier New", Courier, mono;
	font-size: 12px;
	line-height: 30px;
}
.RightColTextSmall {
	background-color: #FFFFFF;
	border: none;
	position: relative;
	height: 30px;
	width: 320px;
}
body {
	margin: 0px;
	padding: 0px;
	overflow: hidden;
	background-color: DCDFEE;
}
.ThumbLabel {
	font-family: Arial, Helvetica, sans-serif;
	font-size: 12px;
	font-weight: bold;
	color: #FFFFFF;
	padding-left: 5px;
}
.ImageCount {
	font-family: Arial, Helvetica, sans-serif;
	font-size: 12px;
	font-style: normal;
	font-weight: normal;
	color: #CCCCCC;
	text-align: right;
	text-indent: 5px;
}

.FooterCell {
	overflow: hidden;
	position: relative;
	height: 50px;
}
.LeftColDiv {
	border-top-width: 1px;
	border-right-width: 1px;
	border-bottom-width: 1px;
	border-left-width: 1px;
	border-top-style: none;
	border-right-style: none;
	border-bottom-style: solid;
	border-left-style: solid;
	border-top-color: #FFFFFF;
	border-right-color: #FFFFFF;
	border-bottom-color: #FFFFFF;
	border-left-color: #FFFFFF;
	background-color: 003366;
	overflow: hidden;
	width: 200px;
}
-->
</style>
</head>
<body>
<FORM name=form1 action="/router/OrderProcessorServlet" method=post>
  <INPUT TYPE="HIDDEN" NAME="guid" VALUE="59b5be93b8a1782277b0ffec44cbf168">
  <table width="750" height="1000" border="0" align="left" cellpadding="0" cellspacing="0">
    <tr> 
      <td width="200" valign="top" bgcolor="#000000"><table width="200" border="0" cellspacing="0" cellpadding="0">
          <tr> 
            <td class="LeftColDiv"><table width="200" border="0" cellspacing="0" cellpadding="0">
                <tr> 
                  <td colspan="2"><img src="/mc/images/thumbnail_order.gif" width="140" height="140"> 
                  </td>
                </tr>
                <tr> 
                  <td><p class="ThumbLabel"> Interpretation Order</p></td>
                  <td><p class="ThumbLabel">1 page</p></td>
                </tr>
              </table></td>
          </tr>
          <tr> 
            <td class="LeftColDiv"><table width="200" border="0" cellspacing="0" cellpadding="0">
                <tr> 
                  <td colspan="2" rowspan="2" width="140"><a href="viewer.php?orient=<?php echo($orient) ?>"><img name="thumb1" border="0" src="images/thumbnail1_off.gif" width="140" height="140"></a></td>
                  <td valign="top"><p class="ThumbLabel"> 99 images</p></td>
                </tr>
                <tr> 
                  <td>&nbsp;</td>
                </tr>
                <tr> 
                  <td width="100"> <p class="ThumbLabel">imagelabel1</p></td>
                  <td width="40"> <p class="ImageCount">00:00&nbsp;&nbsp;</p></td>
                  <td>&nbsp;</td>
                </tr>
              </table></td>
          </tr>
          <tr> 
            <td class="LeftColDiv"><table width="200" border="0" cellspacing="0" cellpadding="0">
                <tr> 
                  <td colspan="2" rowspan="2" width="140"><a href="viewer.php?orient=<?php echo($orient) ?>"><img name="thumb2" border="0" src="images/thumbnail1_off.gif" width="140" height="140"></a></td>
                  <td valign="top"><p class="ThumbLabel"> 99 images</p></td>
                </tr>
                <tr> 
                  <td>&nbsp;</td>
                </tr>
                <tr> 
                  <td width="100"> <p class="ThumbLabel">imagelabel1</p></td>
                  <td width="40"> <p class="ImageCount">00:00&nbsp;&nbsp;</p></td>
                  <td>&nbsp;</td>
                </tr>
              </table></td>
          </tr>
          <tr> 
            <td class="LeftColDiv" valign="middle" align="center"> 
            <table width="180" border="0" cellspacing="0" cellpadding="0">
              <tr align="center" valign="middle"> 
                <td height="50"><img src="/mc/images/tool_boxes.jpg" width="40" height="40"></td>
                <td height="50"><img src="/mc/images/tool_r.jpg" width="40" height="40"></td>
                <td height="50"><a href="/mc/hipaa.php"><img src="/mc/images/tool_x.jpg" width="40" height="40" border="0"></a></td>
              </tr>
              <tr align="center" valign="middle"> 
                <td height="50"><img src="/mc/images/tool_compare.jpg" width="40" height="40"></td>
                <td height="50"><img src="/mc/images/tool_whitea.jpg" width="40" height="40"></td>
                <td height="50"><img src="/mc/images/tool_reset.jpg" width="40" height="40"></td>
              </tr>
              <tr align="center" valign="middle"> 
                <td height="50"><img src="/mc/images/tool_dicom.jpg" width="40" height="40"></td>
                <td height="50"><img src="/mc/images/tool_blacka.jpg" width="40" height="40"></td>
                <td height="50"><a href="/mc/hipaa.php"><img src="/mc/images/tool_ibutton.jpg" width="40" height="40" border="0"></a></td>
              </tr>
            </table></td>
          </tr>
          <tr> 
            <td bgcolor="000000"><img src="/mc/images/lformspacer.gif" width="1" height="10"></td>
          </tr>
          <tr> 
            <td align="center" bgcolor="003366"><img src="/mc/images/logo_left.gif" width="200" height="50"></td>
          </tr>
          <tr> 
            <td align="center" bgcolor="#000000"> <p><font color="#999999" size="2" face="Arial, Helvetica, sans-serif"><strong>1 
              Series total - Doe Joseph B</strong></font></p></td>
          </tr>
          <tr> 
            <td align="center" bgcolor="#000000"> <select name="actionmenu" onchange="javascript:DropDownAction(this);">
                <option value="" selected>choose option</option>                
				<option value="1">about</option>
                <option value="2">help</option>
                <option value="3">option 3</option>
                <option value="">--------</option>
                <option value="4">option 4</option>
                <option value="5">option 5</option>
                <option value="6">option 6</option>
                <option value="">--------</option>
                <option value="7">option 7</option>
                <option value="8">option 8</option>
                <option value="9">option 9</option>
              </select></td>
          </tr>
        </table></td>
      <td width="750" height="750" valign="top"> <table width="750" height="750" border="0" align="left" cellpadding="0" cellspacing="0">
          <tr align="left" valign="top"> 
            <td width="330" bgcolor="BFC4E1"><table width="330" border="0" cellspacing="0" cellpadding="0">
                <tr> 
                  <td><table width="330" border="0" cellpadding="0" cellspacing="0">
                      <tr> 
                        <td width="20"><img src="/mc/images/spacer_blank.gif" width="20" height="18"></td>
                        <td width="72"><img src="/mc/images/spacer_blank.gif" width="72" height="1"></td>
                        <td width="218"><img src="/mc/images/spacer_blank.gif" width="218" height="8"></td>
                        <td width="20"><img src="/mc/images/spacer_blank.gif" width="20" height="1"></td>
                      </tr>
                      <tr> 
                        <td><img src="/mc/images/spacer_blank.gif" width="20" height="1"></td>
                        <td width="72" bgcolor="E1E3F0" class="FolderTab"><p class="FolderHeader">Tracking</p></td>
                        <td width="218" class="FolderTop">&nbsp;</td>
                        <td><img src="/mc/images/spacer_blank.gif" width="20" height="1"></td>
                      </tr>
                      <tr> 
                        <td rowspan="2"><img src="/mc/images/spacer_blank.gif" width="1" height="80"></td>
                        <td colspan="2" bgcolor="E1E3F0" height="40"><INPUT class=TrackingField name=tracking value="771EF"></td>
                        <td rowspan="2" class="FolderLeft">&nbsp;</td>
                      </tr>
                      <tr> 
                        <td colspan="2" bgcolor="E1E3F0" height="40"><img src="/mc/images/medcommons_trackingfooter.gif" width="300" height="40"></td>
                      </tr>
                    </table></td>
                </tr>
                <tr> 
                  <td><table width="330" border="0" cellspacing="0" cellpadding="0">
                      <tr> 
                        <td width="20"><img src="/mc/images/spacer_blank.gif" width="20" height="18"></td>
                        <td width="72"><img src="/mc/images/spacer_blank.gif" width="72" height="1"></td>
                        <td width="218"><img src="/mc/images/spacer_blank.gif" width="218" height="1"></td>
                        <td width="20"><img src="/mc/images/spacer_blank.gif" width="20" height="1"></td>
                      </tr>
                      <tr> 
                        <td><img src="/mc/images/spacer_blank.gif" width="20" height="1"></td>
                        <td width="72" bgcolor="E1E3F0" class="FolderTab"><p class="FolderHeader">Account</p></td>
                        <td width="218" class="FolderTop">&nbsp;</td>
                        <td><img src="/mc/images/spacer_blank.gif" width="20" height="1"></td>
                      </tr>
                      <tr> 
                        <td><img src="/mc/images/spacer_blank.gif" width="1" height="125"></td>
                        <td colspan="2" bgcolor="E1E3F0" valign="top"><table width="300" border="0" cellspacing="0" cellpadding="0">
                            <tr> 
                              <td colspan="3"><INPUT class=AccountFieldLeft name=name value="Joseph B Doe"></td>
                            </tr>
                            <tr> 
                              <td colspan="3"><img src="/mc/images/spacer_blank.gif" width="1" height="10"></td>
                            </tr>
                            <tr> 
                              <td colspan="3"><TEXTAREA class=HIPAAblock  name=address>123 Lucky St.</TEXTAREA></td>
                            </tr>
                            <tr> 
                              <td width="120"><INPUT class=AccountFieldLeft name=city value="Butte"></td>
                              <td width="60" align="center"> <INPUT class=AccountFieldSmall name=state value="MT"> 
                              </td>
                              <td width="120"><INPUT class=AccountFieldRight name=zip value="83132"></td>
                            </tr>
                          </table></td>
                        <td class="FolderLeft">&nbsp;</td>
                      </tr>
                    </table></td>
                </tr>
                <tr> 
                  <td><table width="330" border="0" cellspacing="0" cellpadding="0">
                      <tr> 
                        <td width="20"><img src="/mc/images/spacer_blank.gif" width="20" height="18"></td>
                        <td width="72"><img src="/mc/images/spacer_blank.gif" width="72" height="1"></td>
                        <td width="218"><img src="/mc/images/spacer_blank.gif" width="218" height="1"></td>
                        <td width="20"><img src="/mc/images/spacer_blank.gif" width="20" height="1"></td>
                      </tr>
                      <tr> 
                        <td><img src="/mc/images/spacer_blank.gif" width="20" height="1"></td>
                        <td width="72" bgcolor="E1E3F0" class="FolderTab"><p class="FolderHeader">Charges</p></td>
                        <td width="218" class="FolderTop">&nbsp;</td>
                        <td><img src="/mc/images/spacer_blank.gif" width="20" height="1"></td>
                      </tr>
                      <tr> 
                        <td>&nbsp;</td>
                        <td colspan="2" bgcolor="E1E3F0"> <table width="300" border="0" cellspacing="0" cellpadding="0">
                            <tr> 
                              <td width="170" valign="bottom"> <p class="FieldHeader">card 
                                  number</p></td>
                              <td width="130"><p class="FieldHeader">exp</p></td>
                            </tr>
                            <tr> 
                              <td width="170"><INPUT class=CreditCardField name=cardnumber value="7817574478133225"></td>
                              <td width="130"><INPUT class=CreditCardFieldRight  name=expiration value="12/09"></td>
                            </tr>
                            <tr> 
                              <td colspan="2" class="FieldHeader"><img src="/mc/images/spacer_blank.gif" width="1" height="5"></td>
                            </tr>
                            <tr> 
                              <td width="170" class="FieldHeader"> <p align="right">amount</p></td>
                              <td width="130"><INPUT class=CreditCardFieldRight name=amount value="150.00"></td>
                            </tr>
                            <tr> 
                              <td width="170" class="FieldHeader"> <p align="right">tax</p></td>
                              <td width="130"><INPUT class=CreditCardFieldRight name=tax value="12.00"></td>
                            </tr>
                            <tr> 
                              <td width="170" class="FieldHeader"> <p align="right">charge</p></td>
                              <td width="130"><INPUT class=CreditCardFieldRight name=charge value="162.00"></td>
                            </tr>
                          </table></td>
                        <td class="FolderLeft">&nbsp;</td>
                      </tr>
                    </table></td>
                </tr>
                <tr> 
                  <td><table width="330" border="0" cellspacing="0" cellpadding="0">
                      <tr> 
                        <td width="20"><img src="/mc/images/spacer_blank.gif" width="20" height="18"></td>
                        <td width="72"><img src="/mc/images/spacer_blank.gif" width="72" height="1"></td>
                        <td width="218"><img src="/mc/images/spacer_blank.gif" width="218" height="1"></td>
                        <td width="20"><img src="/mc/images/spacer_blank.gif" width="20" height="1"></td>
                      </tr>
                      <tr> 
                        <td><img src="/mc/images/spacer_blank.gif" width="20" height="1"></td>
                        <td width="72" bgcolor="E1E3F0" class="FolderTab"><p class="FolderHeader">HIPAA</p></td>
                        <td width="218" class="FolderTop">&nbsp;</td>
                        <td><img src="/mc/images/spacer_blank.gif" width="20" height="1"></td>
                      </tr>
                      <tr> 
                        <td><img src="/mc/images/spacer_blank.gif" width="1" height="146"></td>
                        <td colspan="2" bgcolor="E1E3F0" valign="top"> <table width="300" border="0" cellspacing="0" cellpadding="0">
                            <tr> 
                              <td align="center" valign="middle"><img src="/mc/images/spacer_blank.gif" width="1" height="15"></td>
                            </tr>
                            <tr> 
                              <td valign="middle"><TEXTAREA class=HIPAAblock name=signature1>MEDCOMMONS</TEXTAREA></td>
                            </tr>
                            <tr> 
                              <td valign="middle"><TEXTAREA class=HIPAAblock name=signature2>Joe's Imaging Centers Inc.</TEXTAREA></td>
                            </tr>
                            <tr> 
                              <td valign="middle"><TEXTAREA class=HIPAAblock name=signature3></TEXTAREA></td>
                            </tr>
                          </table></td>
                        <td class="FolderLeft">&nbsp;</td>
                      </tr>
                    </table></td>
                </tr>
                <tr> 
                  <td><table width="100%" border="0" cellspacing="0" cellpadding="0">
                      <tr align="center">
                        <td><img src="/mc/images/spacer_blank.gif" width="1" height="5"></td>
                        <td><img src="/mc/images/spacer_blank.gif" width="1" height="5"></td>
                      </tr>
                      <tr align="center"> 
                        <td><a href="javascript:window.print();"><img src="/mc/images/button_print.gif" width="70" height="20" border="0"></a></td>
                        <td><a href="javascript:SendOrderForm();"><img src="/mc/images/button_send.gif" width="70" height="20" border="0"></a></td>
                      </tr>
                    </table></td>
                </tr>
              </table></td>
            <td width="420" bgcolor="DCDFEE"> <table width="420" border="0" cellspacing="0" cellpadding="0">
                <tr> 
                  <td width="60"><img src="/mc/images/formspacer_rightscreen.gif" width="60" height="18"></td>
                  <td width="320"><img src="/mc/images/formspacer_rightscreen.gif" width="320" height="1"></td>
                  <td width="40"><img src="/mc/images/formspacer_rightscreen.gif" width="40" height="1"></td>
                </tr>
                <tr> 
                  <td width="60">&nbsp;</td>
                  <td width="320"><p class="RightColHeader">Night Interpretation 
                      Order</p></td>
                  <td width="40">&nbsp;</td>
                </tr>
                <tr> 
                  <td colspan="3"><img src="/mc/images/formspacer_rightscreen.gif" width="1" height="15"></td>
                </tr>
                <tr> 
                  <td width="60">&nbsp;</td>
                  <td width="320"><p class="RightFieldHeader">History (Non Private 
                      Information)</p></td>
                  <td width="40">&nbsp;</td>
                </tr>
                <tr> 
                  <td width="60">&nbsp;</td>
                  <td width="320"><TEXTAREA class=RightColTextField name=history>Unknown</TEXTAREA></td>
                  <td width="40">&nbsp;</td>
                </tr>
                <tr> 
                  <td colspan="3"><img src="/mc/images/formspacer_rightscreen.gif" width="1" height="15"></td>

                </tr>
                <tr> 
                  <td width="60" height="20">&nbsp;</td>
                  <td width="320"><p class="RightFieldHeader">Comments</p></td>
                  <td width="40">&nbsp;</td>
                </tr>
                <tr> 
                  <td width="60">&nbsp;</td>
                  <td width="320"><TEXTAREA class=RightColTextField name=comments>CERVICAL SPINE</TEXTAREA></td>
                  <td width="40">&nbsp;</td>
                </tr>
                <tr> 
                  <td colspan="3"><img src="/mc/images/formspacer_rightscreen.gif" width="1" height="15"></td>
                </tr>
                <tr> 
                  <td>&nbsp;</td>
                  <td><p class="RightFieldHeader">Copy To</p></td>
                  <td>&nbsp;</td>
                </tr>
                <tr> 
                  <td width="60">&nbsp;</td>
                  <td width="320"><INPUT class=RightColTextSmall name=copyto value="agropper@medcommons.org"></td>
                  <td width="40">&nbsp;</td>
                </tr>
                <tr> 
                  <td width="60">&nbsp;</td>
                  <td width="320">&nbsp;</td>
                  <td width="40">&nbsp;</td>
                </tr>
                <tr> 
                  <td width="60">&nbsp;</td>
                  <td width="320">&nbsp;</td>
                  <td width="40">&nbsp;</td>
                </tr>
              </table></td>
          </tr>
        </table></td>
    </tr>
  </table>
</form>
</body>
<?php
}
?>
</html>