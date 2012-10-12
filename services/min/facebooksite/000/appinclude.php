<?php


$GLOBALS['facebook_config']['debug']=false;

$GLOBALS['DB_Connection'] = "mysql.internal";
$GLOBALS['DB_Database'] = "facebook";
$GLOBALS['DB_User']= "medcommons";

$me = $_SERVER['PHP_SELF'];
$me = substr($me,0,strrpos($me,'/')+1);

// ssadedin: some sloppy code creates urls with // instead of /
// it would be nice to clean up the sloppy code, but for the sake
// of convenience we simply coalesce the doubled slashes together here
$me = str_replace("//", "/", $me);

$GLOBALS['app_url']='http://' . $_SERVER['HTTP_HOST'] . $me;

mysql_connect($GLOBALS['DB_Connection'], $GLOBALS['DB_User']) or die("facebook boostrap: error  connecting to database.");
$db = $GLOBALS['DB_Database'];
mysql_select_db($db) or die("can not connect to database $db");
$result = mysql_query("SELECT * FROM `fbapps` WHERE `key` = '$me' ") or die("$me in $db is not registered with medcommons as a healthbook application".mysql_error());
$app = mysql_fetch_object($result);
if ($app===false)  die("$me is not registered with medcommons as a healthbook application");

// simon should review all of these values given our new structure
$GLOBALS['bigapp']=($app->newfeatures==1);  // will normally be in minimode
$GLOBALS['uber'] = $app->uber_server;
$GLOBALS['uber_lookup'] =$app->uber_server."acct/ws/mcidHost.php";
$GLOBALS['appstatus'] = $app->appstatus;
$oauth_consumer_key = $app->oauth_consumer_key;
$oauth_consumer_secret = $app->oauth_consumer_secret;

require_once 'facebook.php';

// these are the branding parameters for the application
$GLOBALS['healthbook_application_name']=$app->healthbook_application_name;
$GLOBALS['healthbook_application_version']=$app->healthbook_application_version;
$GLOBALS['healthbook_application_image']=$app->healthbook_application_image;
$GLOBALS['healthbook_application_publisher'] =$app->healthbook_application_publisher;
$GLOBALS['facebook_application_url']=$app->facebook_application_url;
// behavioral features
$GLOBALS['newfeatures']=$app->newfeatures;

$GLOBALS['medcommons_images']= $app->imagery;

$GLOBALS['new_account_appliance']=$app->new_account_appliance;  // where new accounts get made
$GLOBALS['extgroupurl'] = $app->extgroupurl; 
$GLOBALS['marqueefbml']=$app->marqueefbml;
// standard facebook variables
// where this app is installed
$GLOBALS['appapikey'] =$appapikey = $app->appapikey ;
$GLOBALS['appsecret'] =$appsecret = $app->appsecret;
$GLOBALS['login_iframe']=$GLOBALS['new_account_appliance']."/acct/hblogin.php"; // does login for lower iframe
$GLOBALS['autoapprovemoderators'] = true;
$GLOBALS['facebook'] =$facebook;
$GLOBALS['base_url'] = $app->appcallbackurl;
$GLOBALS['devpay_redir_url']='https://www.medcommons.net/devpay/devpay_redir.php';

// ssadedin: mcid of group that will act as support group
if($app->new_account_support_group_mcid)
  $GLOBALS['new_account_support_group_mcid'] = $app->new_account_support_group_mcid;
?>
