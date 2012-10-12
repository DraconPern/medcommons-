<?php

// Dirk Diamond's page, will be made more generic 
	if (isset($_COOKIE['mc'])) //wld 10 sep 06 strict type checking

{
	// logged in set up variables

$loginout = <<<XXX
 <form name="login2" style='display:inline;' action='/acct/login.php' method="post">		
      	<input type="hidden" id="openid_url" value = 'billdonner+dirkdiamond@gmail.com' name="openid_url"/>
    	<input type="hidden" id="password" value = 'tester' name="password"/>
			<input style='display:inline;' type="submit" value="Demo Sign In as Dirk Diamond" name="loginsubmit" class="mainwide"/>		
</form>
XXX;
}
else 
{
	

$loginout = <<<XXX

<h4>You are already logged on to Hutton Health</h4>
 <form name="login2" style='display:inline;' action='/acct/logout.php&next=/portal/g.php?sports' method="post">
 			<input style='display:inline;' type="submit" value="Sign Out" name="loginsubmit" class="mainwide"/>		
</form>

XXX;
}
$html = <<<XXX
<!DOCTYPE HTML>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Hutton Health Portal - Dirk Diamond, ATC</title>
 <link rel="stylesheet" type="text/css"  href="css/gstyle.css"/>
 <link rel="shortcut icon" href="images/favicon.gif" type="image/gif"/>
 <meta name="viewport" content="minimum-scale=0.6, width=device-width, maximum-scale=1.6, user-scalable=yes">
    <meta name="apple-mobile-web-app-capable" content="YES">
    <link rel="apple-touch-icon" href="mcportal.png">

</head>
<body>
<h2>Dirk Diamond, ATC</h2>
<img  src='http://portal.medcommons.net/portal/images/DirkDiamond.png' />
	
$loginout

<h3>Dirk's Teams</h3>
<p>Select logo to view the team's public upload page</p>
<a href='../g.php?public&amp;h=6'><img  src='http://portal.medcommons.net/portal/images/NYDangers.png' /></a>
<a href='../g.php?public&amp;h=5'><img   src = 'http://portal.medcommons.net/portal/images/BostonBrooms.png' /></a>
<h3>More About Dirk</h3>
<p>ipsum lorem...</p>

</body>
</html>
XXX;


echo $html;

?>