<?php
///////
/////////
///////////    THE CODE BELOW THE LINE IS COMMON FOR "COVERS" VS "SHARING" 
////////
//////

require_once "login.inc.php";
require_once "wslibdb.inc.php";
require_once "DB.inc.php";
require_once "utils.inc.php";
// Efax credentials for the account to use for incoming and outgoing fax
$faxoutuser ="bdonner5";
$faxoutpassword ="bdon23";
$faxoutid = "9175919352";
$faxoutpretty = '1-917-591-9352';

$pennysmskey = "d40bdebb-61da-4266-a045-27dfdb3c932f";
$pennysmsurl = "http://api.pennysms.com/xmlrpc";


	function make_tracking_pin ()
	{
		/////////////// this routine can use simon's help
	$trackingnumber = "987612347654";	$pin = '111111';
	
	return array ($trackingnumber, $pin);
	}
	
	function getvars ()
{
global $pickupURL;
	// restart here after taking user inputs
    // make a tracking number and pin and put it into the msg only if requested

	list ($trackingnumber,$pin) = make_tracking_pin();
	if ($_POST['switchA']=='1')
	{
		$youmaybecalled ="<h2>Your counterpart has everything needed to access the health records.</h2>";
		$pickup = "$pickupURL?t=$trackingnumber&pin=$pin";
		$pinstuff = <<<ZZZ
		<div class="row">
		<label>pin</label>
		<input type="number" name="pin" readonly="readonly" value="$pin"/>
	</div>
ZZZ;
	}
	else
	{
		$pickup = "$pickupURL?t=$trackingnumber";
		$pinstuff='';
		$youmaybecalled ="<h2>You may need to separately communicate the pin $pin to your counterpart.</h2>";
	}
return array ($trackingnumber,$pin,$pickup,$pinstuff,$youmaybecalled);

}
function pageheader()
{
	global $mainTitle;
	$html = <<<XXX
	<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
	<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
	<title>$mainTitle</title>
<meta name="viewport" content="width=device-width; initial-scale=1.0; maximum-scale=1.0; user-scalable=0;"/>
<link rel="apple-touch-icon" href="../iui/iui-logo-touch-icon.png" />
<style type="text/css" media="screen">@import "../iui/iui.css";</style>
<script type="application/x-javascript" src="../iui/iui.js"></script>
<style>
body > *:not(.toolbar) {

    top: 0px;
    width: 100%;
    min-height: 417px;

}
</style>
</head>
XXX;

	return $html;
}
function error_exit ($s)
{
	global $msgType;
	$ph = pageheader();
	$msg = <<<XXX
		
	<form id="share" title="send $msgType with sharing info" class="panel" selected="true" >
	<h2>An internal error occurred; Technical support has already been notified. </h2>
	<div style='border:1px solid black; padding:10px'>
	<h2>$s</h2>
</div>
</form>
XXX;

	$html = <<<XXX
	$ph
	$msg
</body></html>
XXX;
	echo $html;
	exit;

}
function warn_exit ($s)
{
		global $msgType;
	$ph = pageheader();
	$msg = <<<XXX
		
	<form id="share" title="send $msgType with sharing info" class="panel" selected="true" >
	<div style='border:1px solid black; padding:10px'>
	<h2>$s</h2>
</div>
</form>
XXX;

	$html = <<<XXX
	$ph
	$msg
</body></html>
XXX;
	echo $html;
	exit;

}
function check_credentials($from,$password,$mcid)
{
	// Resolve user
	$row = User::resolveEmail($from, $password);
	if(!$row)
	return false;
	if($row->mcid!=$mcid)
	return false;
	// should return more data, for now, lets just see
	return true;
}


?>