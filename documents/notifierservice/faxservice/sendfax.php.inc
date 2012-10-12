<?PHP
function getvalue($blurb, $s){
	$len = strlen($s);
	$start = strpos($blurb,"<".$s.">");
	$end = strpos ($blurb, "</".$s.">");
	$ret =  substr($blurb, $start+$len+2,$end-$start-$len-2);
//	echo "Getvalue for $s is $ret<br>";
	return $ret;
}
	

function outfaxxml($xmtnum,$recipient,$faxnum,$filecontents,$filetype){
	// careful, this external service is very sensitive to the ordering of the arguments in the xml, must strictly follow manual
	$x=<<<XXX
<?xml version="1.0"?> 

<WebFaxAPIRequest> 
<AccessControl> 
<UserName>bdonner</UserName> 
<Password>medcompass</Password>
</AccessControl> 
<Transmission> 
<TransmissionControl> 
<TransmissionID>$xmtnum</TransmissionID>
<Resolution>STANDARD</Resolution> 
<Priority>NORMAL</Priority>
<SelfBusy>ENABLE</SelfBusy>
<FaxHeader>"@DATE1 @TIME3 @ROUTETO{26} CCR Test Fax @RCVRFAX Pg%P/@TPAGES"</FaxHeader> 
</TransmissionControl>

<DispositionControl> 
<DispositionURL>https://secure.test.medcommons.net/notifierservice/faxstatus.php</DispositionURL> 
<DispositionLevel>BOTH</DispositionLevel>
<DispositionMethod>POST</DispositionMethod>
</DispositionControl> 

<Recipients>
<Recipient> 
<RecipientName>$recipient</RecipientName>
<RecipientCompany>$xmtnum</RecipientCompany> 
<RecipientFax>$faxnum</RecipientFax>
</Recipient>
</Recipients>

<Files> 
<File> 
<FileContents>$filecontents</FileContents>
<FileType>$filetype</FileType> 
</File>  
</Files>
</Transmission>
</WebFaxAPIRequest>
XXX;
/*

<DispositionMethod>EMAIL</DispositionMethod>
<DispositionEmails>
<DispositionEmail> 
<DispositionRecipient>william l donner</DispositionRecipient> 
<DispositionAddress>billdonner@medcommons.net</DispositionAddress>
</DispositionEmail> 
</DispositionEmails>

>https://secure.medcommons.net/notifierservice/faxstatus.php</DispositionURL> 


*/

return $x;
}


function sendfax ($xmtnum,$recipient,$faxnum,$filecontents,$filetype)
{
//$st = strip_tags($filecontents);

//echo "send fax to $faxnum body $st";


$retstr = "";

$data  = outfaxxml($xmtnum, $recipient, $faxnum , $filecontents,$filetype);
$cont = "id=".urlencode("10062")."&respond=".urlencode("XML")."&xml=".urlencode($data); 

$contlen = strlen($cont);
$fp = fsockopen("secureservices.dataoncall.com",  80, $errno, $errstr, 30);
if (!$fp) {
   $retstr =  "$errstr ($errno)<br />\n"; return $retstr;
} ;
   $out = "POST /WebFax.serv HTTP/1.1\r\n";
   $out .= "Host: secureservices.dataoncall.com\r\n";
   $out .= "Content-type: application/x-www-form-urlencoded\r\n";
//    $out .= "Connection: Close\r\n";
   $out .= "Content-length: $contlen";
   $out .= "\r\n\r\n";
   
   fputs($fp, $out);   //header
   fputs($fp, $cont); // body.$cont;
   
   $response = "";

//   fwrite($fp, $out);
   while (!feof($fp)) {
       $response.= fgets($fp, 128);
   }
   fclose($fp);
//  echo $response;
   
   

   
// now parse out the response
$xmtTransmissionID= getvalue($response,"TransmissionID");
$xmtDOCID = getvalue($response,"DOCID");
$xmtStatusCode = getvalue($response,"StatusCode");
$xmtStatusDescription = getvalue($response, "StatusDescription");
$xmtErrorLevel = getvalue($response,"ErrorLevel");
$xmtErrorMessage=getvalue($respone,"ErrorMessage");


$service = "secureservices.dataoncall.com";

	mysql_connect($GLOBALS['DB_Connection'],
			$GLOBALS['DB_User'],
			$GLOBALS['DB_Password']
			) or die ("can not connect to mysql");
	$db = $GLOBALS['DB_Database'];
	mysql_select_db($db) or die ("can not connect to database $db");
	 	 
    // now write an entry in the mysql database

$insert="INSERT INTO faxstatus (xmtTime,xmtService, faxnum,filespec,filetype,xmtTransmissionID,xmtDOCID,xmtStatusCode,xmtStatusDescription,xmtErrorLevel,xmtErrorMessage)".
				" VALUES(NOW(),'$service','$faxnum','$filespec','$filetype','$xmtTransmissionID','$xmtDOCID','$xmtStatusCode','
				$xmtStatusDescription','$xmtErrorLevel','$xmtErrorMessage')";
	mysql_query($insert) or die("can not insert into table faxstatus - ".mysql_error());
mysql_close();

return "OK";
	
}
?>

