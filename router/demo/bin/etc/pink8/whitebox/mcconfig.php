<?PHP
require_once("../whitebox/wbsubs.inc");
require_once("../whitebox/displaymcconfig.inc");
readconfig();
 $gateway="";
 $user="";
 //+
 $aetitle="";
 $showmenus="";
 $mcdestipaddress="";
 $mcdestport = "";
 $comment="";
 //-
 $displayname = cleanreq('mcdest'); // passed in or not, depending on whether we want an insert or not
 $user = cleanreq('user');
 $gateway = cleanreq('gateway');
  $rowid = cleanreq('rowid');

 $readonly = (cleanreq('ro')=="yes"); // if we should run in readonly mode
 
  // if a device with this displayname exists, then displayit
if ($displayname!="")
{  
   $success = getmcdetails( 
   $displayname,  $user,   $gateway,   $colleagues, $destination, $showmenus, $regusers,
    						$sendemail, $email, $emailtype, $worklistonly, $templist);
      
  if ($success==false){
echo display_mc_config(
   $rowid,
   $readonly,
   $displayname,
   $user,   
   $gateway,
   $colleagues,
   $destination, 
   $showmenus, 
   $regusers, 
   $sendemail,
   $email,
   $emailtype,
   $worklistonly,
   $templist,
   "** Internal error - cant read getmcdetails **",
   $displaynameerror,
   $emailerror);
   exit;
  }
}

   
echo display_mc_config(
   $rowid,
   $readonly,
   $displayname,
   $user,   
   $gateway,
   $colleagues,
   $destination, 
   $showmenus, 
   $regusers, 
   $sendemail,
   $email,
   $emailtype,
   $worklistonly,
   $templist,
   $generalerror,
   $displaynameerror,
   $emailerror);

?>