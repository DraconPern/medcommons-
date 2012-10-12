<?PHP
require_once("../whitebox/wbsubs.inc");
require_once("../whitebox/displaydicomconfig.inc");
readconfig();
 $gateway="";
 $user="";
//+
 $aetitle="";
 $showmenus="";
 $dicomipaddress="";
 $dicomport = "";
 $comment="";
//- 
 
 $displayname = cleanreq('dicom'); // passed in or not, depending on whether we want an insert or not
 $user = cleanreq('user');
 $gateway = cleanreq('gateway');
 $rowid = cleanreq('rowid');
 $readonly = (cleanreq('ro')=="yes"); // if we should run in readonly mode
 // if a device with this displayname exists, then displayit
if ($displayname!="")
{  
   $success = getdicomdetails( 
   $displayname,
   $user,   
   $gateway,
   $aetitle, 
   $showmenus, 
   $dicomipaddress, 
   $dicomport, 
   $comment);
    
  if ($success==false){
display_dicom_config(
   $rowid,
   $readonly,
   $displayname,
   $user,   
   $gateway,
   $aetitle, 
   $showmenus, 
   $dicomipaddress, 
   $dicomport, 
   $comment,
  "** internal error - cant find dicom device record $displayname  **",
   $gatewayerror,
   $aetitleerror,
   $dicomipaddresserror,
   $dicomporterror,
   $commenterror);
   exit;
  }
}

   
echo display_dicom_config(
   $rowid,
   $readonly,
   $displayname,
   $user,   
   $gateway,
   $aetitle, 
   $showmenus, 
   $dicomipaddress, 
   $dicomport, 
   $comment,
   $displaynameerror,
   $gatewayerror,
   $aetitleerror,
   $dicomipaddresserror,
   $dicomporterror,
   $commenterror);

?>