<?php
/**
 * ************ CONSULTING PHYSICIAN REGISTRATION PAGE *******************
 *
 * (this is optionally present depending on the templagte)
 */


function consultant_registration_page_handler()
{

	//
	// this needs rewriting - it was stolen
	//
	global $trace;

	$h = z('h');
	
	list($hn,$hlogo,$progind,$vn,$servicename,$serviceind) = ctx($h);

	// prepare this so we can repost if we mess up
	$page = "htm/gconsregister.htm";
	$handler = "consreghandler&h=$h";
	$defs = array();
	if (isset($_REQUEST['Password1'])) $defs['Password1']= " value='{$_REQUEST['Password1']}' ";
	if (isset($_REQUEST['Password2'])) $defs['Password2']=" value='{$_REQUEST['Password2']}' ";
	if (isset($_REQUEST['Email'])) $defs['Email']=" value='{$_REQUEST['Email']}' ";
	if (isset($_REQUEST['Department'])) $defs['Department']= " value='{$_REQUEST['Department']}' ";
	if (isset($_REQUEST['Phone'])) $defs['Phone']= " value='{$_REQUEST['Phone']}' ";
	if (isset($_REQUEST['calltype'])) $defs['calltype']= " value='{$_REQUEST['calltype']}' ";
	
	if (isset($_REQUEST['Video'])) $defs['Video']= " value='{$_REQUEST['Video']}' ";
	if (isset($_REQUEST['videotype'])) $defs['videotype']= " value='{$_REQUEST['videotype']}' ";


	$substitutions = array (
'$$$buttonlabel$$$'=>"Sign Up",
'$$$practiceind$$$'=>$h,
'$$$logourl$$$'=>$hlogo,
'$$$practice$$$'=>$hn,
'$$$program$$$'=>$vn,
'$$$crumbs$$$'=>'',
	);

	if ($trace)
	{
		echo "hit practice_registration_page_handler get";
		print_r ($_GET);
		echo "hit practice_registration_page_handler post";
		print_r ($_POST);
	}


	must ('Email') or failwith($substitutions,$defs,$page,$handler,"Please Specify an Email Address");;
	must ('Password') or failwith($substitutions,$defs,$page,$handler,"You must enter a password");
	must ('Password2') or failwith($substitutions,$defs,$page,$handler,"You must enter password twice");
	mustconfirm ('confirm') or failwith($substitutions,$defs,$page,$handler,"Please confirm your acceptance of the terms of agreement");


	// clean everything up on the way into the database

	$Email  = z('Email');
	$Password = z('Password');
	$Password2 = z('Password2');
	$confirm = z('confirm');


	// perform more detailed error checking
	if ($Password != $Password2) failwith($substitutions,$defs,$page,$handler,"Passwords must match");

	/// get medcommons to do create various things
	// make a medcommons account, for now we'll just patch in all 999999s and we'll fix manually
	$mcid = '9999999999999999';
	
	
	$department = z('Department'); // these are the fields input fields
	$videourl = z('Video');
	$phone = z('Phone');
	$calltype = z('calltype');
	$videotype = z('videotype');
//	$emailcontact = z('EmailContact');


	//finally inset gunk into database

	// goto the users brand new dashbaord and set that as his startpage dashboard
	$redir = "?home&h=$h"; // just stay in here for now

	dosql ("Insert into aProviders set role='consultant', email='$Email' , mcid='$mcid', provider='$Email', department='$department', hind='$h',


defaultVideoURL = '$videourl',

defaultPhone = '$phone',

defaultEmailContact = '$emailcontact',

defaultCalltype = '$calltype',

defaultVideotype = '$videotype'

	
	");
//	$providerind = mysql_insert_id();
//	dosql ("Insert into aJoined set hospitalind='$hospitalind',serviceind='$serviceind', providerind='$providerind', programind='$programind'");
//	/// get medcommons to do create various things

	header ("Location:$redir"); // ans



}

function consultant_registration_page($errdecorators=array())
{
	$h = z('h');
	
	list($hn,$hlogo,$progind,$vn,$servicename,$serviceind) = ctx($h);
	$defaultfields = array (
	);

	$crumbs = //"<a href='?home&h=$h'>admin home</a> >".
	"<a href='?handler=adminconspage&h=$h'>Directory</a> > Register";
	$substitutions = array (
'$$$buttonlabel$$$'=>"Register",
'$$$logourl$$$'=>$hlogo,
	'$$$practiceind$$$'=>$h,
	'$$$practice$$$'=>$hn,
'$$$program$$$'=>$vn,
	'$$$crumbs$$$'=>$crumbs,
	);

	$errdecorators = array (
'_errormaintext'=>"",
'_errormainclass'=>'hideerror', 
	); // or class=hideerror to hide error field

	return loadpage("htm/gconsregister.htm", "?handler=consreghandler&h=$h",
	$defaultfields, $substitutions, $errdecorators);

}



/**
 * ************ REFERRING PHYSICIAN REGISTRATION PAGE *******************
 *
 * (this is optionally present depending on the templagte)
 */

function ref_phys_registration_page_handler()
{

	//
	// this needs rewriting - it was stolen
	//
	global $trace;

	$h = z('h');
	
	list($hn,$hlogo,$progind,$vn,$servicename,$serviceind) = ctx($h);

	// prepare this so we can repost if we mess up
	$page = "grefphysregister.htm";
	$handler = "refreghandler&h=$h";
	$defs = array();
	if (isset($_REQUEST['Password1'])) $defs['Password1']= " value='{$_REQUEST['Password1']}' ";
	if (isset($_REQUEST['Password2'])) $defs['Password2']=" value='{$_REQUEST['Password2']}' ";
	if (isset($_REQUEST['Email'])) $defs['Email']=" value='{$_REQUEST['Email']}' ";
	if (isset($_REQUEST['Hospital'])) $defs['Hospital']= " value='{$_REQUEST['Hospital']}' ";
	if (isset($_REQUEST['RefPhys'])) $defs['RefPhys']= " value='{$_REQUEST['RefPhys']}' ";
	
	if (isset($_REQUEST['Phone'])) $defs['Phone']= " value='{$_REQUEST['Phone']}' ";
	if (isset($_REQUEST['calltype'])) $defs['calltype']= " value='{$_REQUEST['calltype']}' ";
	
	if (isset($_REQUEST['Video'])) $defs['Video']= " value='{$_REQUEST['Video']}' ";
	if (isset($_REQUEST['videotype'])) $defs['videotype']= " value='{$_REQUEST['videotype']}' ";


	$substitutions = array (
'$$$buttonlabel$$$'=>"Sign Up",
'$$$practiceind$$$'=>$h,
'$$$logourl$$$'=>$hlogo,
'$$$practice$$$'=>$hn,
'$$$program$$$'=>$vn,
	'$$$crumbs$$$'=>'',
	);

	if ($trace)
	{
		echo "hit practice_registration_page_handler get";
		print_r ($_GET);
		echo "hit practice_registration_page_handler post";
		print_r ($_POST);
	}


	must ('Email') or failwith($substitutions,$defs,$page,$handler,"Please Specify an Email Address");;
	must ('Password') or failwith($substitutions,$defs,$page,$handler,"You must enter a password");
	must ('Password2') or failwith($substitutions,$defs,$page,$handler,"You must enter password twice");
	mustconfirm ('confirm') or failwith($substitutions,$defs,$page,$handler,"Please confirm your acceptance of the terms of agreement");


	// clean everything up on the way into the database

	$Email  = z('Email');
	$Password = z('Password');
	$Password2 = z('Password2');
	$confirm = z('confirm');


	// perform more detailed error checking
	if ($Password != $Password2) failwith($substitutions,$defs,$page,$handler,"Passwords must match");

	/// get medcommons to do create various things
	// make a medcommons account, for now we'll just patch in all 999999s and we'll fix manually
	$mcid = '9999999999999999';
	
	
	$refhospital = z('Hospital'); // these are the fields input fields
	$refphys = z('RefPhys');
	$videourl = z('Video');
	$phone = z('Phone');
	$calltype = z('calltype');
	$videotype = z('videotype');
	$emailcontact = z('EmailContact');


	//finally inset gunk into database

	// goto the users brand new dashbaord and set that as his startpage dashboard
	$redir = "?handler=home&h=$h&h=$h"; // just stay in here for now

	dosql ("Insert into aProviders set role='refphys', email='$Email' , mcid='$mcid', provider='$Email', department='unspecified', hind='$h',
defaultHospital = '$refhospital',

defaultRefPhys = '$refphys',

defaultVideoURL = '$videourl',

defaultPhone = '$phone',

defaultEmailContact = '$emailcontact',

defaultCalltype = '$calltype',

defaultVideotype = '$videotype'

	
	");
//	$providerind = mysql_insert_id();
//	dosql ("Insert into aJoined set hospitalind='$hospitalind',serviceind='$serviceind', providerind='$providerind', programind='$programind'");
	/// get medcommons to do create various things

	header ("Location:$redir"); // ans



}
function ref_phys_registration_page($errdecorators=array())
{
	$h = z('h');
	
	list($hn,$hlogo,$progind,$vn,$servicename,$serviceind) = ctx($h);
	$defaultfields = array (
	);

	$crumbs = //"<a href='?home&h=$h'>admin home</a> >".
	" <a href='?handler=adminrefphyspage&h=$h'>referring physicians directory</a> > enroll";
	$substitutions = array (
'$$$buttonlabel$$$'=>"Register",
'$$$logourl$$$'=>$hlogo,
	'$$$practiceind$$$'=>$h,
	'$$$practice$$$'=>$hn,
'$$$program$$$'=>$vn,
	'$$$crumbs$$$'=>$crumbs,
	);

	$errdecorators = array (
'_errormaintext'=>"",
'_errormainclass'=>'hideerror', 
	); // or class=hideerror to hide error field

	return loadpage("htm/grefphysregister.htm", "?handler=refhandler&h=$h",
	$defaultfields, $substitutions, $errdecorators);

}




/**
 * ********** PRACTICE REGISTRATION PAGE ****************
 *
 */
function practice_registration_page_handler()
{
	global $trace;
	// prepare this so we can repost if we mess up
	$page = "htm/gform.htm";
	$handler = "prphanlder";
	$defs = array();
	if (isset($_REQUEST['First'])) $defs['First']= " value='{$_REQUEST['First']}' ";
	if (isset($_REQUEST['Last'])) $defs['Last']=" value='{$_REQUEST['Last']}' ";
	if (isset($_REQUEST['Email'])) $defs['Email']=" value='{$_REQUEST['Email']}' ";
	if (isset($_REQUEST['name'])) $defs['name']= " value='{$_REQUEST['name']}' ";
	if (isset($_REQUEST['program'])) $defs['program']= " value='{$_REQUEST['program']}' ";
	// these are radio boxes
	//	if (isset($_REQUEST['rpp'])) $defs['rpp']= 'value="checked=checked" ';
	//	if (isset($_REQUEST['rpp2'])) $defs['rpp2']= 'value="checked=checked" ';
	//	if (isset($_REQUEST['amex'])) $defs['amex']= 'value="checked=checked" ';

	//	$h = $_REQUEST['h'];
	//	list($hn,$hlogo,$progind,$vn,$servicename) = ctx($h);


	$substitutions = array (
'$$$buttonlabel$$$'=>"Sign Up",
'$$$logourl$$$'=>$hlogo,
	'$$$practice$$$'=>$hn,
'$$$program$$$'=>$vn,
	'$$$crumbs$$$'=>'',
	);

	if ($trace)
	{
		echo "hit practice_registration_page_handler get";
		print_r ($_GET);
		echo "hit practice_registration_page_handler post";
		print_r ($_POST);
	}

	must ('name') or failwith($substitutions,$defs,$page,$handler,"Please Specify a Name for Your Practice");
	mustbeset ('program') or failwith($substitutions,$defs,$page,$handler,"Please select a template");
	must ('First') or failwith($substitutions,$defs,$page,$handler,"Enter Your First Name");
	must ('Email') or failwith($substitutions,$defs,$page,$handler,"Please Specify an Email Address");;
	must ('Last') or failwith($substitutions,$defs,$page,$handler,"Enter Your Last Name");
	must ('Password') or failwith($substitutions,$defs,$page,$handler,"You must enter a password");
	must ('Password2') or failwith($substitutions,$defs,$page,$handler,"You must enter password twice");
	mustconfirm ('confirm') or failwith($substitutions,$defs,$page,$handler,"Please confirm your acceptance of the terms of agreement");


	// clean everything up on the way into the database
	$hospitalname = z('name');
	$programind = z('program');
	$First = z('First');
	$Last = z('Last');
	$Email  = z('Email');
	$Password = z('Password');
	$Password2 = z('Password2');
	$confirm = z('confirm');


	// perform more detailed error checking
	if ($Password != $Password2) failwith($defs,$page,$handler,"Passwords must match");

	/// get medcommons to do create various things
	// make a medcommons account, for now we'll just use medcommons7+1
	$mcid = '9999999999999999';
	//Make a group, and make it a service
	$groupname = "$hospitalname"; // get an upload form url too



	//finally inset gunk into database


	dosql ("Insert into aHospitals set hospital='$hospitalname', service='$groupname', indprogram='$programind',logourl='http://www.medcommons.net/images/Logo_246x50_Transparent.gif' ");
	$hospitalind = mysql_insert_id();

	dosql ("Insert into aProviders set role='admin', email='$Email' , mcid='$mcid', provider='$First $Last' ");
	$providerind = mysql_insert_id();
//	dosql ("Insert into aJoined set hospitalind='$hospitalind',serviceind='$serviceind', providerind='$providerind', programind='$programind'");
	/// get medcommons to do create various things

	header ("Location:$redir"); // ans



}
function practice_registration_page()
{
	//	$h = $_REQUEST['h'];
	//	list($hn,$hlogo,$progind,$vn,$servicename) = ctx($h);


	$page = "htm/gform.htm";
	$handler = "prphandler";
	$defaultfields = array (
"rpp2" =>"checked='checked'",
"Email"=>"value='billdonner@gmail.com'",
"Cell"=>"checked='checked'",
"Skype"=>"checked='checked'",
	);
	$substitutions = array (
'$$$buttonlabel$$$'=>"Sign Up",
//'$$$logourl$$$'=>$hlogo,
	//'$$$practice$$$'=>$hn,
	//'$$$program$$$'=>$vn,
	);
	$errdecorators = array (
'_errormaintext'=>"",
'_errormainclass'=>'hideerror', 
	); // or class=hideerror to hide error field
	return loadpage($page, "?handler=$handler",
	$defaultfields, $substitutions, $errdecorators);
}
/**
 * ************ PRACTICE OPTIONS PAGE
 *
 */

function practice_options_page_handler()
{
	global $trace;
	if ($trace)
	{
		echo "hit practice_options_page_handler get";
		print_r ($_GET);
		echo "hit practice_options_page_handler post";
		print_r ($_POST);
	}

	$hospitalind = $_REQUEST['h'] or die ("hospital index missing in practice options page handler");


	$h = z('h');
	$logoURL = z('logoURL');

	list($hn,$hlogo,$progind,$vn,$servicename,$serviceind) = ctx($h);
	$hospitalind = $h;

	$page = "htm/goptions.htm";
	$handler = "prohandler";
	$substitutions = array (
'$$$buttonlabel$$$'=>"Adjust Options",
'$$$logourl$$$'=>$hlogo,
	'$$$practice$$$'=>$hn,
'$$$program$$$'=>$vn,
	'$$$crumbs$$$'=>'',
	);
	$errdecorators = array (
'_errormaintext'=>"Main error goes here",
'_errormainclass'=>'class=hide', //class=hideerror to hide error field
	); // or class=hideerror to hide error field


	$defs = array();
	if (isset($_REQUEST['showPracticeName'])) $defs['showPracticeName']= " value='{$_REQUEST['showPracticeName']}' ";
	$showPracticeName = z('showPracticeName');
	if (isset($_REQUEST['showService'])) $defs['showService']= " value='{$_REQUEST['showService']}' ";
	$showService = z('showService');
	if (isset($_REQUEST['showConsultant'])) $defs['showConsultant']= " value='{$_REQUEST['showConsultant']}' ";
	$showConsultant = z('showConsultant');
	if (isset($_REQUEST['showRefHospital'])) $defs['showRefHospital']= " value='{$_REQUEST['showRefHospital']}' ";
	$showRefHospital= z('showRefHospital');
	if (isset($_REQUEST['showRefPhys'])) $defs['showRefPhys']= " value='{$_REQUEST['showRefPhys']}' ";
	$showRefPhys= z('showRefPhys');
	if (isset($_REQUEST['showRefPhone'])) $defs['showRefPhone']= " value='{$_REQUEST['showRefPhone']}' ";
	$showRefPhone= z('showRefPhone');
	if (isset($_REQUEST['showRefEmail'])) $defs['showRefEmail']= " value='{$_REQUEST['showRefEmail']}' ";
	$showRefEmail= z('showRefEmail');
	if (isset($_REQUEST['showRefWebConf'])) $defs['showRefWebConf']= " value='{$_REQUEST['showRefWebConf']}' ";
	$showRefWebConf= z('showRefWebConf');
	if (isset($_REQUEST['showClinicalHistory'])) $defs['showClinicalHistory']= " value='{$_REQUEST['showClinicalHistory']}' ";
	$showClinicalHistory= z('showClinicalHistory');
	if (isset($_REQUEST['showMALFooter'])) $defs['showMALFooter']= " value='{$_REQUEST['showMALFooter']}' ";
	$showMALFooter= z('showMALFooter');


	//finally inset gunk into database


	dosql ("Update aHospitals set
	logoURL = '$logoURL',
	showPracticeName='$showPracticeName',
	showService='$showService',
	showConsultant='$showConsultant',
	showRefHospital='$showRefHospital',
	showRefPhys='$showRefPhys',
	showRefPhone='$showRefPhone',
	showRefEmail='$showRefEmail',
	showRefWebConf='$showRefWebConf',
	showClinicalHistory='$showClinicalHistory',
	showMALFooter='$showMALFooter'  where ind = '$hospitalind' ");

	// goto the users brand new dashbaord and set that as his startpage dashboard
	$redir = "?home&h=$hospitalind"; // just stay in here for now

	header ("Location:$redir"); // ans

	exit;

}
function practice_options_page($errdecorators=array())
{

	$h = z('h');
	list($hn,$hlogo,$progind,$vn,$servicename,$serviceind) = ctx($h);
	$page = "htm/goptions.htm";
	$handler = "?handler=prohandler";
	$defaultfields=array();
	$substitutions = array (
	'$$$practiceind$$$'=>$h,
	'$$$buttonlabel$$$'=>"Adjust Options",
	'$$$logourl$$$'=>$hlogo,
	'$$$practice$$$'=>$hn,
	'$$$program$$$'=>$vn,
	'$$$crumbs$$$'=>"<a href='?home&h=$h'>admin home</a> > options chooser",
	);
	$errdecorators = array (
'_errormaintext'=>"",
'_errormainclass'=>'hideerror', 
	); // or class=hideerror to hide error field
	return  loadpage($page, $handler,
	$defaultfields, $substitutions, $errdecorators);

}

?>