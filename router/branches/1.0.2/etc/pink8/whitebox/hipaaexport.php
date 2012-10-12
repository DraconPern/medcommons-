<?php
require_once("../whitebox/wbsubs.inc");
require_once("../whitebox/hipaasubs.inc");
	// puts up a small message with an OK button
	// when the user hits the OK button this window will force a refresh of the parent window
	// and vaporize itself
$message = "HIPAA Log Export";
readconfig();
$gateway=cleanreq('gateway');
$user=cleanreq('user');

$month = cleanreq('month');
if ($month=="") $month=0;
$year = cleanreq('year');
if ($year == "") $year = 0;
 hipaa_title ($month,$year);
$wbheader = wbheader('table export as xml',$message,true);
$okbutton = butt('OK',refreshparentanddie());

//echo $wbheader;

$query = <<<XXX

select or_time,or_tracking,or_vr_vaetitleorigin,or_patientname,
or_modality,or_series,or_images from orders

XXX;
//WHERE (vrcp.vc_user = '$user' and vrcp.vc_gateway = '$gateway')

$result = db_query($query,$GLOBALS['DB_Link']) ;//or die("Query failed : " . db_error());
$status = db_error($GLOBALS['DB_Link']);/////
$filename = "$user-$gateway-hipaaexport";
$type = 'xml';
    // Generate basic dump extension
    if ($type == 'csv') {
        $filename  .= '.csv';
        $mime_type = 'text/x-csv';
    } else if ($type == 'xml') {
        $filename  .= '.xmv';
		$mime_type = 'text/xml';  }
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


$colheaders = array( "hipaa_datetime","hipaa_tracking","hipaa_sender","hipaa_patient","hipaa_modality",
                                "hipaa_nseries","hipaa_nimages");              
                 
if ($type == 'xml') {
	echo "<patientdata>\r\n"; }
else echo $s;

//col headers
//for ($i=0; $i<$number_cols;++$i){
//	$colname[i] = db_field_name($result,$i);

while ($line = db_fetch_array_assoc($result)) {
	$i=0; if ($type=='xml') echo "     <hipaalog>\r\n";
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
   if ($type=='xml') echo "     </hipaalog>\r\n";
  }
if ($type!= 'xml')   echo ("\r\n"); else echo ("</patientdata>\r\n");

/* Free resultset */
db_free_result($result);
?>