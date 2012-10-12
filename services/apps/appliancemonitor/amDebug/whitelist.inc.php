<?php

	// DO NOT INSTALL THIS CODE IN THE HTML TREE
	// check for a list of acceptable clients to make a call

	$__whitelist = array('96.239.68.104','96.239.68.104');

	function __validate_addr ()
	{   	
		global $__whitelist;
		$remoteaddr = $_SERVER["REMOTE_ADDR"]; 
		foreach ($__whitelist as $w) if ($w == $remoteaddr) return;
		header ("Content-type: text/xml");
		echo "<error>$remoteaddr is not on the whitelist for this sevice</error>";
		exit;		
	}
		__validate_addr ();// this code will execute and die if not on the white list
	
?>