<?php 

	// File Name: pass_auth.php3
	
	if (!isset($PHP_AUTH_USER)) {

		header('WWW-Authenticate: Basic realm="AUTOCYT"');
		header('HTTP/1.0 401 Unauthorized');
		echo 'Authorization Required.';
		exit;

	} else if (isset($PHP_AUTH_USER)) {

		if (($PHP_AUTH_USER != "demo") || ($PHP_AUTH_PW != "32feet")) {

			header('WWW-Authenticate: Basic realm="AUTOCYT"');
			header('HTTP/1.0 401 Unauthorized');
			echo 'Authorization Required.';
			exit;

		}
	} 



?>

