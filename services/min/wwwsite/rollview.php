<?php

$WEBSITE_PROTOCOL="https"; // to allow for dev boxes without https

require_once 'site_config.php';

$image = urldecode($_GET['a']); // try really hard

global $WEBSITE,$WEBSITE_PROTOCOL,$GLOBALREDIRECTOR;

$host=$_SERVER['HTTP_HOST'];
$time = gmdate ("M d Y H:i:s");

$comments = <<<XXX
<div class=imagebody>
<img width=580px src='$image' alt='$image' />
</div>
XXX;
$markup = "<!-- loading header from /var/www/html/htm/_header.htm -->".file_get_contents("htm/_header.htm").$comments.
	"<!-- loading footer from /var/www/html/htm/_footer.htm -->".file_get_contents("htm/_footer.htm");


$names = array('$$$htmltitle$$$','$$$modcomments$$$');
$values = array('Features Gallery' ,'');

echo str_replace($names,$values,$markup);

?>
