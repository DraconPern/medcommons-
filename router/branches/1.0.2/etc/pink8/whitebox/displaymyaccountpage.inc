<?PHP

require_once "../whitebox/wbsubs.inc";
require_once "../whitebox/hipaasubs.inc";

function display_my_account_page($user,$gateway)
{
	$folders = 	wadolist($user,$gateway); //in wbsubs	
 
	$gateways = gatewaylist($user);
			
$recenthipaa =hipaa_log(5,$tothipaa,0,0);// should pick 5 most recent
$months = monthlist();
$years = yearlist();			
$hipaabutton = "<input type='submit' name = 'submit' value = 'HIPAA Log' >";
//butt("HIPAA Log",gotopage("hipaalog.php"));
$gobutton = butt("Go",gotopage("selectionscreen.php?orderul=$ou"));
$viewbutton = butt("View/Edit",gotopage("vrcp.php"));
		
$wbh = wbheader('my account',"My Account on MedCommons");
$x=<<<XXX
$wbh
<br>
<span style="font-weight: bold;">Recent Account Activity (HIPAA) Log</span>
<br><br>
$recenthipaa
<br><br>
<span style="font-weight: bold;">All Documents</span><br>
<form method = 'GET' action = hipaalog.php>
Your documents ($tothipaa total) as registered with MedCommons are listed for month $months year $years in the &nbsp;$hipaabutton</form><hr style="width: 100%; height: 2px;"><br>
<br>
<span style="font-weight: bold;">Lists and Folders<br>
</span>Display your custom worklists and document folders:&nbsp;&nbsp;
$folders
&nbsp;&nbsp; $gobutton 
<hr style="width: 100%; height: 2px;"><span style="font-weight: bold;"><br>
<br>Gateways and Transfer Rules<br>
         <form method="post" action="vrcp.php">
</span>Clinicians can access DICOM devices by installing and
configuring a MedCommons Gateway:&nbsp;$gateways&nbsp;<input type="SUBMIT" name="submit" value="View/Edit"/></form>
<span style="font-weight: bold;"></span>
<hr style="width: 100%; height: 2px;"><span style="font-weight: bold;"><br>
</span><br>
</body>
</html>
XXX;
sqltraceon();
echo $x;
sqltracedump();
}
?>