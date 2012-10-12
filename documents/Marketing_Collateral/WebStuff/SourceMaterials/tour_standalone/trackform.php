<html>
<head>
<link href="main.css" rel="stylesheet" type="text/css">
<script language="JavaScript" type="text/JavaScript">
<!--
function SubmitTrackForm() {
	var theForm = parent.TrackForm.trackingForm
	if (theForm.trackingNumber.value=="") {
		
		alert("please enter a tracking number");
		theForm.trackingNumber.focus();
		
	}
		else {
		
		theForm.submit();
		
	}
	}
//-->
</script>
</head>
<body>
<form name="trackingForm" method="post" action="logservice/trackinghandler.php" target="_top"><table width="200" height="100" border="0" cellpadding="0" cellspacing="0">

  <tr><td colspan="2" class="BgAcctNum">

          <input type="text" name="trackingNumber" class="AcctNumBox">
</td>
  </tr>  <tr>
    <td width="141"><img src="images/spacer_dkteal.gif" width="141" height="39"></td>
    <td width="59"><a href="javascript:parent.HeaderFrame.TrackForm.trackingForm.submit();" target="_top"><img src="images/tracknumblock_03.gif" width="59" height="39" border="0"></a></td>
  </tr></table></form>
</body></html>