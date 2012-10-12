<?php
require_once("../whitebox/wbsubs.inc");
	// puts up a small message with an OK button
	// when the user hits the OK button this window will force a refresh of the parent window
	// and vaporize itself
$message = "VRCP Table Export";
readconfig();
$gateway=cleanreq('gateway');
$user=cleanreq('user');

$wbheader = wbheader('table export as xml',$message,true);
$okbutton = butt('OK',refreshparentanddie());

//echo $wbheader;

$query = <<<XXX
select vc_dicom,vc_action,vc_mcdest,vd_displayname,vd_aetitle,vd_dicomipaddr,vd_dicomport,vm_displayname,vm_destination,vm_email from vrcp 
LEFT JOIN vrdt on (vrcp.vc_dicom = vrdt.vd_displayname and
                   vrcp.vc_user = vrdt.vd_user and
                   vrcp.vc_gateway = vrdt.vd_gateway)
LEFT JOIN vrmd on (vrcp.vc_mcdest = vrmd.vm_displayname and
                   vrcp.vc_user = vrmd.vm_user and
                   vrcp.vc_gateway = vrmd.vm_gateway)

WHERE (vrcp.vc_user = '$user' and vrcp.vc_gateway = '$gateway')

XXX;
$result = db_query($query,$GLOBALS['DB_Link']) ;//or die("Query failed : " . db_error());
$status = db_error($GLOBALS['DB_Link']);/////
$filename = "$user-$gateway-vrcpexport";
$type = 'xml';
    // Generate basic dump extension
    if ($type == 'csv') {
        $filename  .= '.csv';
        $mime_type = 'text/x-csv';
    } else if ($type == 'xml') {
        $filename  .= '.xmv';
		$mime_type = 'text/xml';
    }
        // loic1: 'application/octet-stream' is the registered IANA type but
        //        MSIE and Opera seems to prefer 'application/octetstream'
$mime_type = //(PMA_USR_BROWSER_AGENT == 'IE' || PMA_USR_BROWSER_AGENT == 'OPERA')
               //    ? 
               'application/octetstream'
               //    : 'application/octet-stream'
               ;
            header('Content-Type: ' . $mime_type);
			header('Expires: ' . gmdate('D, d M Y H:i:s') . ' GMT');
            header('Content-Disposition: inline; filename="' . $filename . '"');
            header('Cache-Control: must-revalidate, post-check=0, pre-check=0');
            header('Pragma: public');

if ($status!="") die($status);
$number_cols = db_num_fields($result);

$s = '"vrcp_dicom","vrcp_action","vrcp_mcdest","vrcp_displayname","vrcp_aetitle"'.
                 '"vrcp_dicomipaddr","vrcp_dicomport","vrcp_displayname","vrcp_destination","vrcp_email"';

$colheaders = array( "vrcp_dicom","vrcp_action","vrcp_mcdest","vrcp_displayname","vrcp_aetitle",
                                "vrcp_dicomipaddr","vrcp_dicomport","vrcp_displayname","vrcp_destination","vrcp_email");              
                 
if ($type == 'xml') {
	echo "<virtualradiology>\r\n"; }
else echo $s;

//col headers
//for ($i=0; $i<$number_cols;++$i){
//	$colname[i] = db_field_name($result,$i);

while ($line = db_fetch_array_assoc($result)) {
	$i=0; if ($type=='xml') echo "     <vrcp>\r\n";
   foreach ($line as $col_value) {
      if ($type != 'xml'){ 
	   echo '"'.$col_value.'"';
       $i=$i+1;
       if ($i<$number_cols) echo ","; 
        }
       else {
       	echo "          <$colheaders[$i]>".$col_value."</$colheaders[$i]>\r\n";
       	$i=$i+1;
       }
   }
   if ($type=='xml') echo "     </vrcp>\r\n";
  }
if ($type!= 'xml')   echo ("\r\n"); else echo ("</virtualradiology>\r\n");

/* Free resultset */
db_free_result($result);
?>