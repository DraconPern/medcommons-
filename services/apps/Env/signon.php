<?php
//regrettably, until we get rid of all the phantom activations of index.php we are just going to ignore any activations where the cookie is already set
require_once 'is.inc.php';

function indexheader($title){

	//$leagueimg = league_logo($league,'is');

	$page_header = page_header($title);
	$mimg = main_logo('is');
	$header = <<<XXX
	    $page_header
	    <style>
	    body {background-color:white}
	    </style>
		$mimg
XXX;
	return $header;
}

// start here
if (isset($_REQUEST['err']))
$err=$_REQUEST['err'];
else $err='';
if ($err=='plslogontomedcommons') $err = "Please Sign On To MedCommons"; else
if ($err=='notsimtrakuser') $err ="You are not enabled for Simtrak Access";

$header = indexheader("$err");
$markup = <<<XXX

$header
<div id='is_body'>
<div id='is_c_section'  style='text-align:center;width:100%;border:purple;'><p>To use this service, you must register with MedCommons.</p>
<p>Works best with new browsers: FF>3.0, Safari>3.0, Opera>9.1,IE>7.0</p>

<p class='errfield'>
$err

</p>

<form class='p' method='post' action='/acct/login.php' id='login' name='login'>



  <input    type='hidden' name='next' value="index.php" />

<div class='f'>

   <button class=infield_button onclick='document.getElementById("is_signon_box").submit(); return false;'><img src="images/openid.jpg" width='38' height='35' alt='OpenID' />login</button>
			<br />&nbsp;<br />&nbsp;<br />&nbsp;<br />&nbsp;<br />&nbsp;<br />&nbsp;
	
</div>
</form>

</div>
</div>
<div id='is_footer'>
</div>
</div> 
</body>
</html>
XXX;

echo $markup;
?>
