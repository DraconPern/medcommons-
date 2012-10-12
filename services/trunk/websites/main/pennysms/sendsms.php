<?php

require_once "../hiddencreds.inc.php";

function xpost ($content)
{
	$pennysmsurl = "http://api.pennysms.com/xmlrpc";
	
	$headers  =  array( "Content-type: text/xml" );

	$ch = curl_init();
	curl_setopt($ch, CURLOPT_URL, $pennysmsurl );
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
	curl_setopt($ch, CURLOPT_TIMEOUT, 20);
	curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
	//curl_setopt($ch, CURLOPT_USERPWD, $your_username.':'.$your_password);
	curl_setopt($ch, CURLOPT_POSTFIELDS, $content);

	$data = curl_exec($ch);

	if (curl_errno($ch))print curl_error($ch);
	else curl_close($ch);

	return $data;
}
if (
(!isset($_POST['replyaddr'])) ||
(!isset($_POST['phones']))||
(!isset($_POST['msg160']))
)
server_response_msg(4,"missing http post arguments");

if (strlen($_POST['msg160'])>160)
server_response_msg(5,"max length of sms msg is 160");

list($sig,$sigtime) = verify_client_signature();
$body = <<<MSG
<?xml version="1.0"?>
<methodCall>
<methodName>send</methodName>
<params>
<param>
<value><string>$pennysmskey</string></value>
</param>
<param>
<value><string>{$_POST['replyaddr']}</string></value>
</param>
<param>
<value><string>{$_POST['phones']}</string></value>
</param>
<param>
<value><string>{$_POST['msg160']}</string></value>
        </param>
   </params>
</methodCall>
MSG;

server_response_msg (1,xpost($body)); // just pass back whatever actual happens


?>