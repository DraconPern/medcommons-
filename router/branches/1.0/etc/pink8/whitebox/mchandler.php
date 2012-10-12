<?PHP
require_once("../whitebox/wbsubs.inc");
require_once("../whitebox/displaymcconfig.inc");
require_once("../whitebox/displaydialogcomplete.inc");

function b($s)
{
	return $_REQUEST[$s];
	//	echo "$s is ".$$s."\n\r";
}

// handle completiton of mcdest editing function
//
// there are two key variables that determine the flow here insertnew and rowid
//
// if insertnew is set then add a completely new mcdest element to vrdt
// 		in this case the key variable is the name of the new element to add
//		verify uniquess of the new display name and insert the record into vrdt table
//			ifnot unique then give it up with an error 
// 
// if insertnew is not set then we are going to update an existing mcdest element
//		in this case the key variable is the 'new name' for the element and oldkey is the old
//		if the two are equal then this is an easy update - just change the fields as indicated with a simple update of the table emtry
//		otherwise check to see that this new key is unique
//			ifso then insert a new mcdest element to vrdt as above and set flag to say primary key was changed
//			ifnot give it up with an error
// 
// ---- vrcp table fixup processing
//
// if primary key was changed above then 
//				go thru the vrcp table and fix up all the entries to reflect the keychange
//
// if rowid is non-null then this adjustment to the mcdest column should happen on that row of the vrcp table 
// otherwise, a new row is added to the bottom
//
// if the wado flag hsa changed state then the wado fields in vrcp table are patched up
// 
//

readconfig(); // get reconnected to database
session_start();
sqltraceon(); // make sure we get it all
// get all of the fields from the form

//these are passed in from vrcp and are immutable
$submit = b("submit");
sqt("submit flag $submit");

$gateway = b("gateway");
$user = b("user");
$rowid = b("rowid");
$insertnew = b("insertnew");
$readonly = (b("readonly")=="yes");
$key = b("displayname");
$oldkey = b("olddisplayname");
$showmenus=  (b("showinmenus")!="")?"1":"0";
//echo "in mc handler displayname $key olddisplayname $oldkey";
// these fields are specific to this mcdest handler
//+
$colleagues = b("colleagues");
$regusers = b("regusers");
$sendemail = b("sendemail");
$email = b("email");
$emailtype = b("emailtype");
$worklistonly = b("worklistonly");
$templist = b("templist");

//echo "mcdesthandler showmenus $showmenus";

// clear out all the error codes

$displayname_error="";
$email_error="";
$general_error="";

// check out all of the errors
if ($key =="") $displayname_error = errortext("** please enter a name **");

// if there is a problem with a field then clear it out
if ($displayname_error!="")$displayname="";
if ($email_error!="") $email="";
if ($general_error!="") $displayname ="";
// if any errors put the thing up again
if (($displayname_error!="")
or ($email_error!="")
or ($general_error!=""))
{
echo display_mc_config(   $rowid,   $readonly,   $key,   $user,   
   $gateway,   $colleagues,   $destination,    $showmenus,    $regusers,    $sendemail,   $email,   $emailtype,
   $worklistonly,   $templist,   $generalerror,   $displaynameerror,
   $emailerror);exit;
}

//-

// we only get here on good news from the input form fields
// form was good, adjust database
if ($oldkey=="") $primarykeychanged=false; else
$primarykeychanged = ($key!=$oldkey)?true:false;
$wadochanged = false; // set after reading the record later
if ($submit=='DELETE')
{
//handle delete	

$success = getmcdetails( $key,  $user,   $gateway,   $oldcolleagues,  $olddestination,  $oldshowmenus, $oldregusers, 
    $oldsendemail, $oldemail, $oldemailtype, $oldworklistonly, $oldtemplist);
if ($success==false) exit_with_message("No such medcommons destination");
$success = deletemc($key,$user,$gateway);
if ($success==false) exit_with_message("There was a problem deleting medcommons destination $key");
exit_with_message("medcommons destination $key was deleted");
}
if ($insertnew=='insert')
{
sqt("mchandler case  insert");
	
// insert a new record
//  before we insert, make sure the record is not already there
$success = getmcdetails( $key,  $user,   $gateway,   $oldcolleagues,  $olddestination,  $oldshowmenus, $oldregusers, 
    $oldsendemail, $oldemail, $oldemailtype, $oldworklistonly, $oldtemplist);
// if it's already there, let's suggest that the user try again
if ($success==true) exit_with_message("The destination device $key is already defined".
							" - click on an existing entry to modify");
//otherwise insert this as a new record
sqt("mchandler case fresh insert");
 patchandexit($rowid,$key,$user,$gateway,$colleagues,  $destination, $showmenus, $regusers, 
    $sendemail, $email, $emailtype, $worklistonly, $templist,false,
 	"A new destination record for $key was added");
}
else
{
// alright, this is the code to handle an update
// first, separate out the new vs old case
$success = getmcdetails( $key,  $user,   $gateway,   $oldcolleagues,  $olddestination,  $oldshowmenus, $oldregusers, 
    $oldsendemail, $oldemail, $oldemailtype, $oldworklistonly, $oldtemplist);
if ($primarykeychanged==true) {
// if changing primary key then make sure nothing else is named similarly
		if ($success==true) exit_with_message("the destination $key is already defined".
									"- click on an existing entry to modify");
// alright, we changed to a new key, it's almost like an insert
sqt ("case mchandler changed key");	
$wadochanged=($oldshowmenus!=$showmenus)?true:false;

	 patchandexit($rowid,$key,$user,$gateway,$colleagues,  $destination,  $showmenus, $regusers, 
    $sendemail, $email, $emailtype, $worklistonly, $templist,$wadochanged
	 ,"Destination device $key was added to your configuration");
	 
}
// this is a clean update, with no key change
sqt ("mchandler case straight update");

$success =  updatemcdetails( $key,  $user, $gateway, $colleagues,  $destination, $showmenus,  $regusers,  $sendemail, 
   $email, $emailtype, $worklistonly,  $templist  );
if ($success==false) exit_with_message("could not update vrdt mc entry");
$wadochanged=($oldshowmenus!=$showmenus)?true:false;	
$success = vrcpcleanup($rowid,$key,$user,$gateway,$oldkey,$showmenus,$wadochanged);

if ($success==true) exit_with_message("The destinationcharacteristics for $key were updated");
	 else exit_with_message ("problem in vrcpcleanup for mchandler");
//that's it should never get here	

}//end of code to handle update

// vrcp cleanup processing
function vrcpcleanup ($rowid,$key,$user, $gateway,$oldkey,$showmenus,$wadochanged)
{
	sqt( "vrcpcleanup rowid $rowid showmenus $showmenus wadochange $wadochanged key $key oldkey $oldkey");
	//  patch will either insert or update depending on row id
	 $success =	patchvcrpmcrow($rowid,$key,$user, $gateway);

	// if we changed the primary key then we need to fix the vrcp table
		if (($oldkey!="") and ($key!=$oldkey))
		$success=	updatevrcpmckeychange($oldkey,$key,
		                                          $user, $gateway);
	// if changing the wado flag setting then we must update the vrcp table as well
		if ($success==true)
		{//updating wado flags if needed
		//	if ($wadochanged==true)
			$success =  updatevrcpmcwadoflags($showmenus,$user, $gateway,$key);
		 //echo "updatedvrcpmcwadflags $success oldshow $oldshowmenus newsho $showmenus";
		}        

		return $success;
} 
function patchandexit($rowid,$key,$user,$gateway,$colleagues,  $destination,  $showmenus, $regusers, 
    $sendemail, $email, $emailtype, $worklistonly, $templist)
{
$success =  insertmcdetails( 
   $key,  $user,     $gateway,   $colleagues,   $destination,    $showmenus, 
   $regusers, $sendemail,$email,$emailtype,$worklistonly,$templist);
   
//now fix up the vrcp table
if ($success==true) $success = vrcpcleanup ($rowid,$key,$user, $gateway,"",$showmenus,$wadochanged);
if ($success==true) exit_with_message($mess);
else exit_with_message("internal processing error in destination $key - please contact the call center");
// end of the insert case, all processing stops here
} 
function exit_with_message ($s)
{	sqltracedump();
	echo display_dialog_complete("<p>$s</p>"); exit;}

?> 

