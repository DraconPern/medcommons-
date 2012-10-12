<?PHP
require_once("../whitebox/wbsubs.inc");
require_once("../whitebox/displaygatewayconfig.inc");
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
 
 $displayname = cleanreq('gateway'); // passed in or not, depending on whether we want an insert or not
 $user = cleanreq('user');
 $gateway = cleanreq('gateway');
 $rowid = cleanreq('rowid');
 $readonly = (cleanreq('ro')=="yes"); // if we should run in readonly mode
 // if a device with this displayname exists, then displayit
/*if ($displayname!="")
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
  	*/
echo display_gateway_config(
   $rowid,
   $readonly,
   $displayname,
   $user,   
   $gateway,
   "MEDCOMMONS01:", 
   false, 
  "177.33.2.12", 
   "8081", 
   "MedCommons Purple Box Gateway",
  "",
   $gatewayerror,
   $aetitleerror,
   $dicomipaddresserror,
   $dicomporterror,
   $commenterror);



?>