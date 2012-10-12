<?
$GLOBALS['DB_Connection'] = "mysql.internal";
$GLOBALS['DB_Database'] = "facebook";
$GLOBALS['DB_User']= "medcommons";

function page_shell($stuff)
{
	$markup = <<<XXX
	<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
	<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
	<head>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
	<meta name="author" content="MedCommons, Inc." />
	<meta name="description" content="MedCommons Facebook Admin Console" />
	<meta name="keywords" content="medcommons, personal health records,ccr, phr, privacy, patient, health, records, medical records,emergencyccr"/>
	<meta name="robots" content="noindex,nofollow"/>
	<meta name="viewport" content="width=320" />
	<title>MedCommons Facebook</title>
	<link rel="shortcut icon" href="/images/favicon.gif" type="image/gif" />
    <link media="all"	href="fbadminstyle.css" type="text/css" rel="stylesheet" />
</head>
<body >
<div class=fbbody >
<div class=fbtab><h1>MedCommons Facebook Admin Console</h1></div>

$stuff
</div>
</body>
</html>
	
XXX;
return $markup;
}
?>