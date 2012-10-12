<?php

require_once "JSON.php";


//require_once "login.inc.php";
//require_once "wslibdb.inc.php";

//require_once "../acct/rls.inc.php";
require_once "urls.inc.php";
require_once "DB.inc.php";
require_once "utils.inc.php";

$trace = false;

// ssadedin: stop these pages causing timeouts
$_GLOBALS['no_session_check']=true;

function dbconnect()
{
	    global $DB_HOST, $CENTRAL_USER, $CENTRAL_PASS, $CENTRAL_DB;
        mysql_connect($DB_HOST,
                $CENTRAL_USER,
                $CENTRAL_PASS
        ) or die ("can not connect to mysql");

        $db = $CENTRAL_DB;
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
function get_program($s)
{
        $programs = dosql("select program from aPrograms where ind='$s'  ");
        while ($program =mysql_fetch_object($programs)) return $program  -> program;
}
function get_service($s)
{
        $services = dosql("select service from aServices where ind='$s'  ");
        while ($service =mysql_fetch_object($services)) return $service  -> service;
}
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
function ctx($h)
{
        $hospitals = dosql("Select * from aHospitals where ind='$h' ");
        if (!$hospital =mysql_fetch_object($hospitals))  die ("MedCommons Imaging Portal can not locate hospital with $h");
        $hn = $hospital->hospital;
        $hlogo = $hospital->logourl;
        $progind = $hospital->indprogram;
        $programs = dosql("Select * from aPrograms where ind='$progind' ");
        if (!$program=mysql_fetch_object($programs)) die("table aPrograms doesnt contain $hn");
        $vn = $program->program;
        // figure out the service from the hospital index
        $services = dosql("Select * from aServices where indhospital=$h limit 1");
        if (!$service=mysql_fetch_object($services)) die ("table aServices doesnt contain $hn");
        $d = $service->ind;// ok, finally
        $servicename = $service->service;
        $serviceind = $service->ind;
        return array($hn,$hlogo,$progind,$vn,$servicename,$serviceind);
}
function please_login()
{
//        $hlogo = "http://ci.myhealthespace.com/images/mc_logo.png";
//        $out ="<img src='$hlogo' alt='missing $hlogo' />";
//        echo "<html><head><title>MedCommons Imaging Portal</title><style>#crumbs{margin-top:50px;}</style></head><body> $out";
//        echo "<h1>Please Login to MedCommons</h1>";
//        echo "<p><a href='/acct/g.php'>here</a></p>";
//        echo "</body></html>";

        header("Location: /acct/login.php");
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


///////////////////
//////// Specific Pages
//////////////////




function newest_instance()
{
        $hospitals =
        dosql ("select * from aHospitals order by ind desc limit 1");
        if (!($hospital=mysql_fetch_object($hospitals))) die ("No hosptials!");

        return array($hospital->ind);
}

function listofproviderlinkswithrole($role,$h,$showlink=false)
{
        //handler=adminlogin
        $providers = dosql("Select * from aProviders p ,aJoined j where role='$role' and  p.ind=j.providerind and j.hospitalind='$h' ");
        $out ="<table>";
        while ($provider = mysql_fetch_object($providers))
        {
            $url = "?handler=uploadpage&h=$h&p={$provider->providerind}";
            if ($showlink) {
                        $out.="<tr><td><a href='$url'>$provider->provider</a></td><td>$provider->department</td><td>
                                        <button class='consultButton' onclick='window.location.href=\"$url\"'>Request Consultation</button></td>
                               </tr>";
            }
            else
                   $out.="<tr><td>$provider->provider<td><td>$provider->department</td></tr>";
        }
        $out .="</table>";
        return $out;
}
function refphys_directory($uploadformlink=false)
{

        $data = file_get_contents("grefphysdir.htm");


        $h = z('h');

        list($hn,$hlogo,$progind,$vn,$servicename,$serviceind) = ctx($h);


        $menu =  listofproviderlinkswithrole('refphys',$h, $uploadformlink);
        $crumbs = "<a href='?handler=adminlogin&h=$h'>admin home</a> > referring physicians directory";
        $menu = str_replace(array('$$$body$$$','$$$practice$$$','$$$logourl$$$','$$$program$$$','$$$crumbs$$$'),
        array($menu,$hn,$hlogo,$vn,$crumbs),$data); // replace body of template
        return $menu;

}

function consultants_directory($badge,$uploadformlink=false)
{

        $data = file_get_contents("gcondir.htm");

        $h = z('h');

        list($hn,$hlogo,$progind,$vn,$servicename,$serviceind) = ctx($h);
        $menu = listofproviderlinkswithrole('consultant',$h,$uploadformlink);

        $crumbs = //"<a href='?handler=adminlogin&h=$h'>admin home</a> > ".
        "Consultants Directory";
        
        $consultantLoginUrl = "/acct/login.php?next=".urlencode("/portal/g.php?handler=adminlogin&h=$h");
        $menu = str_replace(array('$$$body$$$','$$$practice$$$','$$$logourl$$$','$$$program$$$','$$$badge$$$','$$$crumbs$$$','$$$consultantloginurl$$$'),
        array($menu,$hn,$hlogo,$vn,$badge,$crumbs,$consultantLoginUrl),$data); // replace body of template

        return $menu;

}
function consultants_directory_page()
{
        return consultants_directory('');

}

function refphys_directory_page()
{

        return refphys_directory();

}


function refphys_loggedin_page()
{
        // show the consultants filtered dashboard
        if(!($me=Gtestif_logged_in())) please_login(); else
        list($accid,$fn,$ln,$email,$idp,$mc,$auth) =$me;
        // if we have admin privs then we can see everything, otherwise we just match on $accid
        $h = z('h');
        
        list($hn,$hlogo,$progind,$vn,$servicename,$serviceind) = ctx($h);


        $ustart = ''; // if set, will get set to a link
        $badge ="<span id=provider>$fn $ln($accid)&nbsp;&nbsp;&nbsp;&nbsp;<a id=logout href='logout.php?next=/acct/g.php' >logout</a></span>";// intended for inside a span

                
        return consultants_directory($badge,true);
}


function random_user_open_page()
{
        return consultants_directory('',true);

}
function random_user_restricted_page()
{
        // you must be logged in
        return "<h3>Here are the physicians who refer patients to us:</h3>".refphys_directory();

}


function consultant_loggedin_page()
{

        if(!($me=Gtestif_logged_in())) please_login(); else
        list($accid,$fn,$ln,$email,$idp,$mc,$auth) =$me;
        $providers = dosql("select provider,ind,mcid,role from aProviders p where mcid='$accid'");

        if (!($provider=mysql_fetch_object($providers)))  die ("Cant find provider with mcid $accid");
        if  ($provider->role!='consultant') die ("You shouldnt be here without consultant privs");
        // move this off premises, its already slow
        require "g2dashboard.inc.php"; // fault it in

        $h = z('h');


        list($hn,$hlogo,$progind,$vn,$servicename,$serviceind) = ctx($h);


        echo show_consultants_dashboard($h);
}

function admin_loggedin_page()
{

        if(!($me=Gtestif_logged_in())) please_login(); else
        list($accid,$fn,$ln,$email,$idp,$mc,$auth) =$me;
        // figure out if we have admin privs
        $providers = dosql("select provider,ind,mcid,role from aProviders p where mcid='$accid'");

        if (!($provider=mysql_fetch_object($providers)))  die ("Cant find provider with mcid $accid");
        if  ($provider->role!='admin') die ("You shouldnt be here without admin privs");
        $h = z('h'); //$v = $_REQUEST['v'];

        list($hn,$hlogo,$progind,$vn,$servicename,$serviceind) = ctx($h);
//        $services = dosql("Select * from aServices where indhospital=$h limit 1");
//        if (!$service=mysql_fetch_object($services)) die ("table aServices doesnt contain $hn");
        $d = $serviceind;// ok, finally
        if ($progind==3)// restricted
        $extrablurb = "<p>Use of this portal requires signon by Referring Physicians. You can <a href='invite' />invite them</a> or you can <a href='?handler=refpage&h=$h' />enroll referring physicians yourself</a>";
        else $extrablurb ="<p>This portal does not require signon by Referring Physicians</p>";

        require "g2dashboard.inc.php"; // fault it in
        if ($progind==3)        $rp="<p>Administer <a href='?handler=adminrefphyspage&h=$h' > Referring Physicians</a></p>"; else $rp=''; // only for restricted portal
        return
        "
        <html>
          <head>
			    <link rel='stylesheet' type='text/css'  href='/css/medCommonsStyles.css'/>
          </head>
          <body>
        ".
        show_consultants_dashboard($h)."<hr/><p>Administer <a href='?handler=adminconspage&h=$h' > Consultants</a></p>
$rp
        <p>Administer <a href='?handler=propage&h=$h' > Portal Parameters</a></p> ".

            "<cite>If you'd like to behave as a consultant or referring physician, please make another account for yourself</cite>
            </body>
         </html>"
           ;
        // add enhanced links
}

function admin_cons_page()
{

        if(!($me=Gtestif_logged_in())) please_login(); else
        list($accid,$fn,$ln,$email,$idp,$mc,$auth) =$me;
        // figure out if we have admin privs
        $providers = dosql("select provider,ind,mcid,role from aProviders p where mcid='$accid'");

        if (!($provider=mysql_fetch_object($providers)))  die ("Cant find provider with mcid $accid");
        if  ($provider->role!='admin') die ("You shouldnt be here without admin privs");
        $h = z('h'); //$v = $_REQUEST['v'];

        list($hn,$hlogo,$progind,$vn,$servicename,$serviceind) = ctx($h);

        
        $out = consultants_directory('') . "<p><a href='?handler=conspage&h=$h'>Enroll Consultants</a></p><p>Invite Consultants</p>";
        
return $out;
}

function admin_refphys_page()
{

        if(!($me=Gtestif_logged_in())) please_login(); else
        list($accid,$fn,$ln,$email,$idp,$mc,$auth) =$me;
        // figure out if we have admin privs
        $providers = dosql("select provider,ind,mcid,role from aProviders p where mcid='$accid'");

        if (!($provider=mysql_fetch_object($providers)))  die ("Cant find provider with mcid $accid");
        if  ($provider->role!='admin') die ("You shouldnt be here without admin privs");
        $h = z('h'); //$v = $_REQUEST['v'];

        list($hn,$hlogo,$progind,$vn,$servicename,$serviceind) = ctx($h);

        
        $out = refphys_directory('') . "<p><a href='?handler=refpage&h=$h'>Enroll Referring Physicians</a></p><p>Invite Referring Physicians</p>";
        
return $out;
}


function contact_and_upload()
{
        $h = z('h');
        echo contact_and_upload_page($h);
}


/**
 * Main Program Starts Here
 */
dbconnect();
$db = DB::get();
//echo "got pdo stuff";
if (isset($_REQUEST['menu']))
{
        echo random_user_open_page();
        exit;
}


if (isset($_REQUEST['handler'])) // are we coming back to one of our handlers?
{
        $handler =$_REQUEST['handler'];
        switch ($handler)
        {
        
                case 'adminlogin':  { echo admin_loggedin_page(); break;}
                        
                case 'adminconspage': {require 'gsms.inc.php'; echo admin_cons_page(); break;}
        
                case 'adminrefphyspage': {require 'gsms.inc.php'; echo admin_refphys_page(); break;}
                
                case 'tel_call_msg': {require 'gsms.inc.php'; echo tel_call_msg(); break;}
        
                case 'sms_send_msg': {require 'gsms.inc.php'; echo sms_send_msg(); break;}
                case 'forward_email': { require "gemailer.inc.php"; echo send_forward_page(); break;}
                case 'forward_chooser': { require "g2dashboard.inc.php"; echo choose_consultant_page(); break;}
                case 'refreghandler':  { require "gregister.inc.php"; echo ref_phys_registration_page_handler(); break;}
        
                case 'consreghandler':  { require "gregister.inc.php"; echo consultant_registration_page_handler(); break;}
                case 'prohandler':  { practice_options_page_handler(); break;}
                case 'contact_and_upload_handler': { require "g2upload.inc.php"; echo contact_and_upload_handler(); break;}
                case 'prphandler':  { require "gregister.inc.php"; practice_registration_page_handler(); break;}
                case 'directory':  { echo consultants_directory_page(); break;}
                case 'pubpage':  { echo random_user_open_page(); break;}
                case 'restrictedpage':  { echo random_user_restricted_page(); break;}
                case 'directory':  { echo consultants_directory_page(); break;}
                case 'refdirectory':  { echo refphys_directory_page(); break;}

                case 'orderdone':
                case 'refphyslogin':  { echo refphys_loggedin_page(); break;}
                case 'consultantlogin':  { echo consultant_loggedin_page(); break;}
                case 'refpage':  { require "gregister.inc.php"; echo ref_phys_registration_page(); break;}
                case 'conspage':  { require "gregister.inc.php"; echo consultant_registration_page(); break;}
                case 'prppage':  { require "gregister.inc.php"; echo practice_registration_page(); break;}
                case 'propage':  { require "gregister.inc.php"; echo practice_options_page(); break;}
                case 'uploadpage': {        require "g2upload.inc.php"; echo contact_and_upload(); break;}

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
// otherwise if nothing supplied at all see if we are logged in and try to do something sensible
if(($me=Gtestif_logged_in())) 
{
        list($accid,$fn,$ln,$email,$idp,$mc,$auth) =$me;
        // figure out our role
        $providers = dosql("select provider,ind,mcid,role from aProviders p where mcid='$accid'");
        if (!($provider=mysql_fetch_object($providers)))  die ("MedCommons Imaging Portal can not locate user with MedCommons ID $accid");
        // figure out the most recent hospital this provider is registered with
        $hospitals = dosql ("select * from aHospitals h,aJoined j,aProviders p where p.ind=$provider->ind and j.providerind=p.ind and j.hospitalind=h.ind order by h.ind desc limit 1 ");
        if (!($hospital=mysql_fetch_object($hospitals)))die ("MedCommons Imaging Portal Provider $accid is not associated with any hospital");
         $h = $hospital->hospitalind;
    $_REQUEST['h'] =  $h; // give it a shot
    
        if  ($provider->role=='refphys') echo refphys_loggedin_page();
        else         if  ($provider->role=='consultant') echo consultant_loggedin_page();

        else         if  ($provider->role=='admin') echo admin_loggedin_page();
else
        
        die ("MedCommons Imaging Portal found user with MedCommons ID $accid and unknown role $provider->role");
        
        exit;
}
// ottherwise, we are not logged on to MedCommons
//die ("Not logged on to MedCommons - to see a Test Page use ?test option")        ;
header("Location: /acct/login.php");
        
?>