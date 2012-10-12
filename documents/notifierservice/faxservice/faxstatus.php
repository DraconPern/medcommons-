<?PHP
// catch fax status updates
require "../dbparamsmcextio.inc.php";



function getvalue($blurb, $s){
	
	$len = strlen($s);
	$start = strpos($blurb,$s.'="');
	if ($start==false) return "";
	$end = strpos ($blurb, '"',$start+$len+2);
if ($end==false) return "";
	$ret =  substr($blurb, $start+$len+2,$end-$start-$len-2);
//	echo "Getvalue for $s is $ret start $start end $end len $len<br>";
	return $ret;
}
	
function bytechange($s)
{	$out='';
	for ($i=0; $i<strlen($s); $i++)
{	$c = substr($s,$i,1);
	if ($c!="\\") $out.=$c;
}
return $out;
}

 $response =  bytechange(urldecode(substr($_POST['xml'],0))); // raw XML

// $response = '<WebFaxAPIDisposition UserName="bdonner" Password="medcompass" TransmissionID="9000" DOCID="62560658" FaxNumber="1914459116//8" CompletionDate="2005-06-27 18:41:50" FaxStatus="0" RecipientCSID="19144591168" Duration="0.4" PagesSent="1" NumberOfRetries="1"/>';

$filetype = $response; // stuff it away
//echo "parsing $response <br><br>";
// now parse out the response
$dispTransmissionID= getvalue($response,"TransmissionID");
$dispDOCID = getvalue($response,"DOCID");
$dispFaxNumber = getvalue($response,"FaxNumber");
$dispCompletionDate = getvalue($response, "CompletionDate");
$dispFaxStatus = getvalue($response,"FaxStatus");
$dispRecipientCSID=getvalue($response,"RecipientCSID");
$dispDuration = getvalue($response, "Duration");
$dispPagesSent = getvalue($response, "PagesSent");
$dispNumberOfRetries = getvalue ($response, "NumberOfRetries");

$filespec = "((incoming))";

$service = "secureservices.dataoncall.com";

	mysql_connect($GLOBALS['DB_Connection'],
			$GLOBALS['DB_User'],
			$GLOBALS['DB_Password']
			) or die ("can not connect to mysql");
	$db = $GLOBALS['DB_Database'];
	mysql_select_db($db) or die ("can not connect to database $db");
// see if we can find an existing record via DOCID, and ifso, then update it
$update = <<<VVV
	UPDATE faxstatus 
	
		SET dispCompletionDate = '$dispCompletionDate',
			dispFaxStatus = '$dispFaxStatus',
			dispRecipientCSID = '$dispRecipientCSID',
			dispDuration = '$dispDuration',
			dispPagesSent = '$dispPagesSent',
			dispNumberofRetries = '$dispNumberOfRetries'
		WHERE (xmtDOCID='$dispDOCID');
VVV;
 	mysql_query($update) or die("can't update table faxstatus - ".mysql_error());
 	$count=mysql_affected_rows(); // get number of rows
// 	echo "$count rows affected";
 	if ($count<1) {
   
// now write an entry in the mysql database

$insert="INSERT INTO faxstatus (xmtTime,xmtService, faxnum,filespec,filetype,xmtTransmissionID,xmtDOCID,
dispCompletionDate,dispFaxStatus,dispRecipientCSID,dispDuration,dispPagesSent,dispNumberOfRetries)".
" VALUES(NOW(),'$service','$dispFaxNumber','$filespec','$filetype','$dispTransmissionID','$dispDOCID','$dispCompletionDate'
				,'$dispFaxStatus','$dispRecipientCSID','$dispDuration','$dispPagesSent','$dispNumberOfRetries')";
	mysql_query($insert) or die("can not insert into table faxstatus - ".mysql_error());
 	};
mysql_close();
	
$x = <<<XXX
<html><body>Post Successful</body></html>
XXX;

echo $x;



?> 