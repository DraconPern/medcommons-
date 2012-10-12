<?php

require_once "JSON.php";

require_once "urls.inc.php";
require_once "DB.inc.php";
require_once "utils.inc.php";


$GLOBALS['DB_Connection'] = "mysql.internal";
$GLOBALS['DB_User']= "medcommons";
$GLOBALS['DB_Database'] = "mcx";
$DB_HOST = 'mysql.internal';
$CENTRAL_HOST = $DB_HOST;
$CENTRAL_DB = 'mcx';
$CENTRAL_USER = 'medcommons';
$CENTRAL_PASS = '';
$CENTRAL_PDO  = 'mysql:host=' . $CENTRAL_HOST  . ';dbname=' . $CENTRAL_DB;
$GLOBALS['DB_Connection'] = "mysql.internal";
$GLOBALS['DB_User']= "medcommons";
$GLOBALS['DB_Database'] = "mcx";
$DB_HOST = 'mysql.internal';
$CENTRAL_HOST = $DB_HOST;
$CENTRAL_DB = 'mcx';
$CENTRAL_USER = 'medcommons';
$CENTRAL_PASS = '';
$CENTRAL_PDO  = 'mysql:host=' . $CENTRAL_HOST  . ';dbname=' . $CENTRAL_DB;

$trace = false;


// Efax credentials for the account to use for incoming and outgoing fax
$faxoutuser ="bdonner5";
$faxoutpassword ="bdon23";
$faxoutid = "9175919352";
$faxoutpretty = '1-917-591-9352';

$pennysmskey = "d40bdebb-61da-4266-a045-27dfdb3c932f";
$pennysmsurl = "http://api.pennysms.com/xmlrpc";


require_once "dbparams.inc.php";


function dbconnect()
{
	mysql_connect($GLOBALS['DB_Connection'],
	$GLOBALS['DB_User'],
	$GLOBALS['DB_Password']
	) or die ("can not connect to mysql");

	$db = $GLOBALS['DB_Database'];
	mysql_select_db($db) or die("can not connect to database $db");
}
function dosql($q)
{
	$status = mysql_query($q);
	if (!$status) die ("dosql failed $q ".mysql_error());
	return $status;
}
function get_hospital($s)
{
	$hospitals = dosql("select hospital,logourl from aHospitals where ind='$s'  ");
	while ($hospital =mysql_fetch_object($hospitals)) return array($hospital -> hospital,$hospital -> logourl);
}
//function get_program($s)
//{
//	$programs = dosql("select program from aPrograms where ind='$s'  ");
//	while ($program =mysql_fetch_object($programs)) return $program  -> program;
//}
//function get_service($s)
//{
////	$services = dosql("select service from aServices where ind='$s'  ");
////	while ($service =mysql_fetch_object($services)) return $service  -> service;
//	return -1;
//}
function get_provider($s)
{
	$providers = dosql("select provider from aProviders where ind='$s'  ");
	while ($provider =mysql_fetch_object($providers)) return $provider  -> provider;
}
function h($a)
{
	if (isset($_GET[$a]))
	$h=mysql_real_escape_string($_GET[$a]);
	else $h=-1;

	return $h;
}
function hh($a)
{
	if (isset($_GET[$a]))
	$h=mysql_real_escape_string($_GET[$a]);
	else $h=$a;

	return $h;
}
function Gtestif_logged_in()
{
	if (!isset($_COOKIE['mc'])) //wld 10 sep 06 strict type checking
	return false;
	$mc = $_COOKIE['mc'];

	$accid=""; $fn=""; $ln = ""; $email = ""; $idp = ""; $auth="";
	if ($mc!='')
	{
		$accid=""; $fn=""; $ln = ""; $email = ""; $idp = ""; $auth="";
		$props = explode(',',$mc);
		for ($i=0; $i<count($props); $i++) {
			list($prop,$val)= explode('=',$props[$i]);
			switch($prop)
			{
				case 'mcid': $accid=$val; break;
				case 'fn': $fn = $val; break;
				case 'ln': $ln = $val; break;
				case 'email'; $email = $val; break;
				case 'from'; $idp = stripslashes($val); break;
				case 'auth'; $auth = $val; break;
			}
		}
	}
	return array($accid,$fn,$ln,$email,$idp,$mc,$auth);
}
function 	ctx($h)
{
	$hospitals = dosql("Select * from aHospitals where ind='$h' ");
	if (!$hospital =mysql_fetch_object($hospitals))  die ("MedCommons Imaging Portal can not locate hospital with $h");
	return array($hospital->hospital,$hospital->logourl,$hospital->indprogram,$hospital->program,$hospital->headers,
	$hospital->labels,$hospital->groupAccountID,$hospital->loginpage,$hospital->homelinklabel);
}
function please_login()
{

	header("Location: login.php");
	exit;
}
function z($a)
{
	if (isset($_REQUEST[$a]))
	$h=mysql_real_escape_string($_REQUEST[$a]);
	else $h=-1;

	return $h;
}

function must ($field)
{
	if (!isset($_REQUEST[$field])) die ("Non-optional field $field is missing");
	$val = trim($_REQUEST[$field]);
	return ($val!='');

}
function mustconfirm ($field)
{
	if (isset($_REQUEST[$field])&&($_REQUEST[$field]=='0')) // this is the test for checkbox set
	return true; else
	return false;

}
function mustbeset ($field)
{
	if (!isset($_REQUEST[$field])) return false;
	return true;

}
function failwith($substitutions,$defs,$page,$handler,$error)
{
	$errdecorators = array (
'_errormaintext'=>$error,
'_errormainclass'=>'show', //class=hideerror to hide error field

	);
	$h=z('h');

	echo loadpage($page, "?handler=$handler&h=$h",
	$defs, $substitutions, $errdecorators);
	exit;
}
function loadpage($url,$handler, $defs,$subs,$errs)
{
	global $trace;
	if ($trace)
	{
		echo "----loadpage $url $handler <br/>";
		echo "defs <br/>"; print_r($defs);
		echo "subs <br/>"; print_r($subs);
		echo "errs <br/>"; print_r($errs);
	}
	// loads the requested page from anywhere
	// locates id=<tags> and adds strings from $defs if matched
	// handles errs the same way
	// afterwards does strings substitution patchups as directed by caller

	$subs ['$$$action$$$']=$handler; //establish handler address

	foreach($errs as $n => $v) {
		$subs[$n]=$v; // copy errs in and handle as defaults
		if ($trace) echo "adding $n $v to subs <br/>";
	}
	// get form template
	$data = file_get_contents($url);
	$dlen = strlen($data);
	// find each name= tag
	$offset = 0; $obuf = ''; $obufpos = 0;
	while ($offset <$dlen)
	{
		$idpos = strpos ($data, 'id=', $offset);
		if (!$idpos) break;
		$blankpos = strpos ($data, ' ',$idpos);
		if (!$blankpos) break;
		$id = substr ($data,$idpos+3,$blankpos-$idpos-3);
		$offset = $blankpos+1;
		if (isset ($defs[$id])) $defaultstring = $defs[$id];
		else $defaultstring='';

		if ($trace) echo "looking up $id found $defaultstring<br/>";

		// build the output buffer up to this point
		$obuf.=substr ($data,$obufpos,$offset-$obufpos);
		$obufpos = $offset; // mark that we are caught up to here
		$obuf .= " $defaultstring " ; // throw in substitution into page
		;

	}

	// last piece - copy out all the rest
	if ($obufpos <$dlen)
	$obuf.=substr ($data,$obufpos,$dlen-$obufpos);
	//
	// do the substitutions at the end
	//

	foreach($subs as $n => $v) {
		$names[]=$n;
		$values[]=$v;
	}
	$obuf = str_replace($names,$values,$obuf);

	return $obuf;
}



function listofproviderlinkswithrole($role,$h,$showlink=false)
{
	//handler=adminlogin
	$providers = dosql("Select * from aProviders p where role='$role' and hind='$h' ");
	$out ="<ul class=listlinks>";
	while ($provider = mysql_fetch_object($providers))
	{
		if ($showlink)

		$out.="<li><a href='?handler=uploadpage&h=$h&p={$provider->providerind}'>$provider->provider</a> $provider->department</li>";

		else

		$out.="<li>$provider->provider $provider->department</li>";
	}
	$out .="</ul>";
	return $out;
}


///////////////////
////////  Actors
//////////////////




function refphys_directory_page($uploadformlink=false)
{

	$data = file_get_contents("htm/grefphysdir.htm");


	$h = z('h');

	list($hn,$hlogo,$progind,$vn,$servicename,$serviceind) = ctx($h);


	$menu =  listofproviderlinkswithrole('refphys',$h, $uploadformlink);
	$crumbs = "<a href='?home&h=$h'>admin home</a> > referring physicians directory";
	$menu = str_replace(array('$$$body$$$','$$$practice$$$','$$$logourl$$$','$$$program$$$','$$$crumbs$$$'),
	array($menu,$hn,$hlogo,$vn,$crumbs),$data); // replace body of template
	return $menu;

}

function consultants_directory($badge,$showlink=false)
{

	$data = file_get_contents("htm/gcondir.htm");

	$h = z('h');

	list($hn,$hlogo,$progind,$vn,$servicename,$serviceind) = ctx($h);

	$role = 'consultant';

	$providers = dosql("Select * from aProviders p  where role='$role' and hind='$h' ");
	$out ="<table><tbody>";
	while ($provider = mysql_fetch_object($providers))
	{
		if ($showlink)

		$out.=<<<XXX
		<tr><td><a href='{$provider->webpageURL}'>$provider->provider</a></td><td>$provider->department</td><td>
		<button style="width:65;height:85" onClick="window.location='?handler=uploadpage&h=$h&p={$provider->ind}'"><b>Request Consultation</b></button></td></tr>
XXX;
		else

		$out.="<tr><td>$provider->provider $provider->department</td></tr>";
	}
	$out .="</tbody></table>";



	$crumbs = //"<a href='?home&h=$h'>admin home</a> > ".
	"Directory";
	$menu = str_replace(array('$$$body$$$','$$$practice$$$','$$$logourl$$$','$$$program$$$','$$$badge$$$','$$$crumbs$$$'),
	array($out,$hn,$hlogo,$vn,$badge,$crumbs),$data); // replace body of template

	$menu .= "<hr/><a href='g.php?login&h=$h'>Consultant Sign In</a>";// start at general sign in point
	//    $menu .= <<<XXX
	//   <button style="width:65;height:65" onClick="history.go(-1)"><b>Back</b></button>
	//<button style="width:65;height:65" onClick="history.go(1)"><b>Forward</b></button>
	//<button style="width:65;height:65" onClick="window.location='http://www.javascriptkit.com'"><b>Home</b></button>
	//<button style="width:65;height:65" onClick="window.location.reload()"><b>Reload</b></button>
	//<button style="width:65;height:65" onClick="window.close()"><b>Close</b></button>
	//XXX;
	return $menu;

}

function admin_cons_page()
{
	$h = z('h'); //$v = $_REQUEST['v'];
	if(!($me=Gtestif_logged_in())) please_login(); else
	list($accid,$fn,$ln,$email,$idp,$mc,$auth) =$me;
	// figure out if we have admin privs
	$providers = dosql("select provider,ind,mcid,role from aProviders p where mcid='$accid' and hind='$h' ");

	if (!($provider=mysql_fetch_object($providers)))  die ("Cant find provider with mcid $accid");
	if  ($provider->role!='admin') die ("You shouldnt be here without admin privs");
	list($hn,$hlogo,$progind,$vn,$servicename,$serviceind) = ctx($h);
	$out = consultants_directory('') . "<p><a href='?handler=conspage&h=$h'>Enroll Consultants</a></p><p>Invite Consultants</p>";

	return $out;
}

function admin_refphys_page()
{$h = z('h'); //$v = $_REQUEST['v'];

if(!($me=Gtestif_logged_in())) please_login(); else
list($accid,$fn,$ln,$email,$idp,$mc,$auth) =$me;
// figure out if we have admin privs
$providers = dosql("select provider,ind,mcid,role from aProviders p where mcid='$accid' and hind='$h'");

if (!($provider=mysql_fetch_object($providers)))  die ("Cant find provider with mcid $accid");
if  ($provider->role!='admin') die ("You shouldnt be here without admin privs");


list($hn,$hlogo,$progind,$vn,$servicename,$serviceind) = ctx($h);


$out = refphys_directory('') . "<p><a href='?handler=refpage&h=$h'>Enroll Referring Physicians</a></p><p>Invite Referring Physicians</p>";

return $out;
}

///// starting pages for specific role-based actors


function consultant_loggedin_page()
{
	$h = z('h');
	if(!($me=Gtestif_logged_in())) please_login(); else
	list($accid,$fn,$ln,$email,$idp,$mc,$auth) =$me;
	$providers = dosql("select provider,ind,mcid,role from aProviders p where mcid='$accid' and hind='$h' ");

	if (!($provider=mysql_fetch_object($providers)))  die ("Cant find provider with mcid $accid");
	if  ($provider->role!='consultant') die ("You shouldnt be here without consultant privs");
	// move this off premises, its already slow
	require "g3dashboard.inc.php"; // fault it in
	list($hn,$hlogo,$progind,$vn,$servicename,$serviceind) = ctx($h);


	return show_consultants_dashboard($h);
}
function atc_loggedin_page()
{
	$h = z('h'); //$v = $_REQUEST['v'];
	if(!($me=Gtestif_logged_in())) please_login(); else
	list($accid,$fn,$ln,$email,$idp,$mc,$auth) =$me;
	// figure out if we have admin privs
	$providers = dosql("select provider,ind,mcid,role from aProviders p where mcid='$accid' and hind='$h' ");

	if (!($provider=mysql_fetch_object($providers)))  die ("Cant find provider with mcid $accid");
	if  ($provider->role!='atc') die ("You shouldnt be here without atc privs");


	list($hn,$hlogo,$progind,$vn,$servicename,$serviceind) = ctx($h);
	$d = $serviceind;// ok, finally

	require "g3dashboard.inc.php"; // fault it in
	if ($progind==4)	$rp="<p>Administer <a href='?handler=adminrefphyspage&h=$h' > Referring Physicians</a></p>"; else $rp=''; // only for restricted portal
	return
	show_consultants_dashboard($h);
	// add enhanced links
}
function admin_loggedin_page()
{
	$h = z('h'); //$v = $_REQUEST['v'];
	if(!($me=Gtestif_logged_in())) please_login(); else
	list($accid,$fn,$ln,$email,$idp,$mc,$auth) =$me;
	// figure out if we have admin privs
	$providers = dosql("select provider,ind,mcid,role from aProviders p where mcid='$accid' and hind='$h' ");

	if (!($provider=mysql_fetch_object($providers)))  die ("Cant find provider with mcid $accid");
	if  ($provider->role!='admin') die ("You shouldnt be here without admin privs");


	list($hn,$hlogo,$progind,$vn,$servicename,$serviceind) = ctx($h);
	$d = $serviceind;// ok, finally
	if ($progind==3)// restricted
	$extrablurb = "<p>Use of this portal requires signon by Referring Physicians. You can <a href='invite' />invite them</a> or you can <a href='?handler=refpage&h=$h' />enroll referring physicians yourself</a>";
	else $extrablurb ="<p>This portal does not require signon by Referring Physicians</p>";

	require "g3dashboard.inc.php"; // fault it in
	if ($progind==3)	$rp="<p>Administer <a href='?handler=adminrefphyspage&h=$h' > Referring Physicians</a></p>"; else $rp=''; // only for restricted portal

	if (isset($_REQUEST['admin'])) $x = "<hr/><p>Administer <a href='?handler=adminconspage&h=$h' > Consultants</a></p>
	$rp
	<p>Administer <a href='?handler=propage&h=$h' > Portal Parameters</a></p> ".

	    "<cite>If you'd like to behave as a consultant or referring physician, please make another account for yourself</cite>";
	else $x='';
	// add enhanced links
	return show_consultants_dashboard($h).$x;
}
function refphys_loggedin_page()
{
	// show the consultants filtered dashboard
	if(!($me=Gtestif_logged_in())) please_login(); else
	list($accid,$fn,$ln,$email,$idp,$mc,$auth) =$me;
	// if we have admin privs then we can see everything, otherwise we just match on $accid
	$h = z('h');

	list($hn,$hlogo,$progind,$vn,$servicename,$serviceind) = ctx($h);



	$badge ="<span id=provider>$email&nbsp;&nbsp;<a id=settings href='settings.php?h=$h' >settings</a>&nbsp;&nbsp;<a id=logout href='glogout.php?h=$h' >logout</a></span>";// intended for inside a span


	return consultants_directory($badge,true);
}

///// public pages
{
	function medcommons_page($page)
	{

		$data = file_get_contents($page);

		$menu = str_replace(array('$$$practice$$$','$$$logourl$$$','$$$program$$$'),
		array('MedCommons Portal Services','http://medcommons.net/images/logoHeader.gif','Catalog'),$data); // replace body of template
		return $menu;

	}
}
function login_page($h)
{

	list($hn,$hlogo,$progind,$vn,$headers,
	$labels,$groupAccountID,$loginpage,$homelinklabel) = ctx($h);

	$providers = dosql("select * from aProviders p where demologin='1' and hind='$h' ");
	$found = 0; $options ='';

	while($provider=mysql_fetch_object($providers))
	{
		$found++;
		$options .=  "<option value ='{$provider->email}'>{$provider->provider}</option>";
	}


	if ($found==0 )  $select = ''; else

	$select =<<<XXX

	<p>
	<span class="bodyLink">Demo Logins</span>
	</p>


	<form name="login2" style='display:inline;' action='/acct/login.php' method="post">

	<select  style='display:inline;' name="openid_url">
	$options
 
</select>	
    <input type="hidden" id="password" value = 'tester' name="password"/>
    		<p>
			<input style='display:inline;' type="submit" value=" Demo Sign In" name="loginsubmit" class="mainwide"/>
			</p>		
		</form>
XXX;

	$data = file_get_contents($loginpage);
	$menu = str_replace(array('$$$practiceind$$$','$$$practice$$$','$$$logourl$$$','$$$program$$$','$$$demoblock$$$','$$$homelinklabel$$$'),
	array($h,$hn,$hlogo,$vn,$select,$homelinklabel),$data); // replace body of template
	return $menu;

}
function plain_page($page)
{

	$data = file_get_contents($page);


	$h = z('h');

	list($hn,$hlogo,$progind,$vn,$servicename,$serviceind) = ctx($h);

	$menu = str_replace(array('$$$practiceind$$$','$$$practice$$$','$$$logourl$$$','$$$program$$$'),
	array($h,$hn,$hlogo,$vn),$data); // replace body of template
	return $menu;

}
function restricted_public_page()
{

	$data = file_get_contents("htm/grestrictedhome.htm");


	$h = z('h');

	list($hn,$hlogo,$progind,$vn,$servicename,$serviceind) = ctx($h);
	$menu = str_replace(array('$$$practiceind$$$','$$$practice$$$','$$$logourl$$$','$$$program$$$'),
	array($h,$hn,$hlogo,$vn),$data); // replace body of template
	return $menu;

}
function consultants_directory_public_page()
{
	return consultants_directory('',true);
}
function sports_public_page()
{
	require "gmulti.inc.php";
	$_REQUEST['h'] =5;
	echo sports_home(5);
}
function general_public_page()
{


	$h = z('h');
	$hospitals = dosql ("select * from aHospitals h  where ind='$h' order by h.ind desc limit 1 ");
	if (!($hospital = mysql_fetch_object($hospitals))) die ("Cant find hospital $h in general public page") ;
	else
	if  ($hospital->publicpage=='general_public') echo general_public_page();
	else 	if  ($hospital->publicpage=='consultants_directory_public')
	echo consultants_directory_public_page();
	else 	if  ($hospital->publicpage=='sports_public')
	echo sports_public_page();
	else    if ($hospital->publicpage=='restricted_public' )
	echo restricted_public_page();

	else    if ($hospital->publicpage=='upload_public' )
	echo plain_page('htm/gpublic.htm');


	else		die ("MedCommons Imaging Portal found facility $hospital->hospital with unknown public page $hospital->publicpage");

}

//function met_hospital_public_page()
//{
//	$_REQUEST['h'] =4;
//	echo consultants_directory_public_page();
//}
//
//function green_state_public_page()
//{
//	$_REQUEST['h'] =3;
//	echo restricted_public_page();
//}
function opine_public_page()
{
	$_REQUEST['h'] =2;
	echo plain_page("htm/gopinion.htm");
}
function public_start()
{
	return medcommons_page("htm/gcatalog.htm");
}
/**
 * Main Program Starts Here
 */
dbconnect();
$db = DB::get();

$accid = 0; // no evidence we are logged on

if (isset($_REQUEST['handler'])) // are we coming back to one of our handlers?
{
	$handler =$_REQUEST['handler'];
	switch ($handler)
	{

		case 'adminconspage': {require 'g2sms.inc.php'; echo admin_cons_page(); break;}
		case 'adminrefphyspage': {require 'g2sms.inc.php'; echo admin_refphys_page(); break;}
		case 'tel_call_msg': {require 'g2sms.inc.php'; echo tel_call_msg(); break;}
		case 'sms_send_msg': {require 'g2sms.inc.php'; echo sms_send_msg(); break;}

		case 'forward_email': { require "g3emailer.inc.php"; echo send_forward_page(); break;}

		case 'forward_chooser': { require "g3dashboard.inc.php"; echo choose_consultant_page(); break;}

		case 'uploadpage': {	require "g2upload.inc.php"; echo contact_and_upload(); break;}
		case 'contact_and_upload_handler': { require "g2upload.inc.php"; echo contact_and_upload_handler(); break;}
		case 'upload_done': { require 'g2upload.inc.php'; upload_done_handler(); die ("should have been redirected");}

		// these functions have an extra &admin needed to keep them out of harms way
		case 'refreghandler': if (isset($_REQUEST['admin'])) { require "gregister.inc.php"; echo ref_phys_registration_page_handler(); break;}
		case 'consreghandler': if (isset($_REQUEST['admin'])) { require "gregister.inc.php"; echo consultant_registration_page_handler(); break;}
		case 'prohandler': if (isset($_REQUEST['admin'])) { require "gregister.inc.php"; practice_options_page_handler(); break;}
		case 'prphandler': if (isset($_REQUEST['admin'])) { require "gregister.inc.php"; practice_registration_page_handler(); break;}
		case 'refpage':  if (isset($_REQUEST['admin'])){ require "gregister.inc.php"; echo ref_phys_registration_page(); break;}
		case 'conspage': if (isset($_REQUEST['admin'])) { require "gregister.inc.php"; echo consultant_registration_page(); break;}
		case 'prppage':  if (isset($_REQUEST['admin'])){ require "gregister.inc.php"; echo practice_registration_page(); break;}
		case 'propage':  if (isset($_REQUEST['admin'])){ require "gregister.inc.php"; echo practice_options_page(); break;}

		default : { die ("Invalid handler specified: $handler");}
	}

	exit; // we exit straight out of the handlers
}

// this puts up the main page
if (isset($_REQUEST['test']))
{
	require "g2main.inc.php";
	echo testmenu();
	exit;
}
if (isset($_REQUEST['sports']))
{
	$_REQUEST['start']='1';
	$_REQUEST['h']=5;
	// fall thru
}
if (isset($_REQUEST['met']))
{
	$_REQUEST['start']='1';
	$_REQUEST['h']=4;
	// fall thru
}
if (isset($_REQUEST['green']))
{
	$_REQUEST['start']='1';
	$_REQUEST['h']=3;
	// fall thru
}
if (isset($_REQUEST['opine']))
{
	$_REQUEST['start']='1';
	$_REQUEST['h']=2;
	// fall thru
}
/*
 if (isset($_REQUEST['public'])&&(isset($_REQUEST['h'])&&(2==$_REQUEST['h'])))
 {
 opine_public_page();
 exit;
 }
 if (isset($_REQUEST['public'])&&(isset($_REQUEST['h'])&&(4==$_REQUEST['h'])))
 {
 met_hospital_public_page();
 exit;
 }
 if (isset($_REQUEST['public'])&&(isset($_REQUEST['h'])&&(3==$_REQUEST['h'])))
 {
 green_state_public_page();
 exit;
 }
 if (isset($_REQUEST['public'])&&(isset($_REQUEST['h'])&&(5==$_REQUEST['h'])))
 {
 sports_public_page();
 exit;
 }
 if (isset($_REQUEST['public'])&&(isset($_REQUEST['h'])&&(6==$_REQUEST['h'])))
 {
 sports_public_page();
 exit;
 }
 */
if ((isset($_REQUEST['public'])&&(isset($_REQUEST['h']))))
{
	general_public_page();
	exit;

}


//
if(($me=Gtestif_logged_in()))
{
	// logged in set up variables

	list($accid,$fn,$ln,$email,$idp,$mc,$auth) =$me;
	if (isset($_REQUEST['rv']))  $_REQUEST['home']=1; // internal redirection based on role
}
else
{
	// not logged in
	$_REQUEST['login']=1;
	$accid=0;
}


// if login page is requested, make sure we are not logged on

if (isset($_REQUEST['login']))
{
	if ($accid!=0)
	//
	// if already logged on lets make it behave like ?start die ("Already logged on");
	{
		$_REQUEST['start']=1;
		// fall thru
	}

	else

	{

		// not logged on
		if (!isset($_REQUEST['h']))
		{
			$_REQUEST['start']=1;

			// fall thru
		}
		else
		{
			$h=z('h');
			echo login_page($h);
			exit;
		}
	}

}

if ((isset($_REQUEST['home']))  //  this is how we deal with our own redirections internally
||
(isset($_REQUEST['start'])) ) // this is how medcommons starts us up
{
	// wants to go home based on role
	if (isset($_REQUEST['h']))
	{
		$h = z('h');
		$hospitals = dosql ("select * from aHospitals h  where ind='$h' order by h.ind desc limit 1 ");
		if (!($hospital=mysql_fetch_object($hospitals)))die ("MedCommons Imaging Portal Provider $accid is not associated with hospital $h");

		// wants home, but not logged on, this is the public starting point
		if ($accid==0) {
			if  ($hospital->publicpage=='general_public') echo general_public_page();
			else 	if  ($hospital->publicpage=='consultants_directory_public') echo consultants_directory_public_page();
			else 	if  ($hospital->publicpage=='sports_public') echo sports_public_page();
			else    if ($hospital->publicpage=='restricted_public' )echo restricted_public_page();

			else    if ($hospital->publicpage=='upload_public' )echo opine_public_page();


			else		die ("MedCommons Imaging Portal found facility $hospital->hospital with unknown public page $hospital->publicpage");
			exit;
		}
	}
	else
	if ($accid ==0)
	{
		// not logged in plain old start
		echo public_start();
		exit;
	}
	else
	{ // logged in but h not specified

		// list them all
		$hospitals = dosql ("select * from aHospitals h, aProviders p where p.mcid = '$accid' and p.hind = h.ind order by h.ind desc limit 10 ");
		$choices = mysql_num_rows($hospitals);
		if ($choices<=1)
		{
			// no ambiguity
			if (!($hospital=mysql_fetch_object($hospitals)))die ("MedCommons Imaging Portal Provider $accid is brand new - please specify h=");
			$h = $hospital->hind;
			$_REQUEST['h'] =  $h; // give it a shot
		}
		else
		{
			// present a list of alternatives
			echo "You are logged in as $email and are recognized as an Actor in $choices separate demos; please choose:<br/><ul>";
			while ($hospital=mysql_fetch_object($hospitals))
			echo "<li><a href='?start&h=$hospital->hind'>$hospital->hospital</a> $hospital->role</li>";
			echo "</ul>";
			exit;
		}
	}
	// go to the home/start page based on role

	$providers = dosql("select provider,ind,mcid,role from aProviders p where mcid='$accid' and hind='$h' ");
	if (!($provider=mysql_fetch_object($providers)))  die ("MedCommons Imaging Portal can not locate user with MedCommons ID $accid in practice $h");
	if  ($provider->role=='refphys') echo refphys_loggedin_page();
	else 	if  ($provider->role=='consultant') echo consultant_loggedin_page();
	else 	if  ($provider->role=='admin') echo admin_loggedin_page();
	else 	if  ($provider->role=='atc') echo atc_loggedin_page();
	else
	die ("MedCommons Imaging Portal found user with MedCommons ID $accid and unknown role $provider->role");
	exit;
}

// ottherwise nothing specified at all
echo public_start();
?>