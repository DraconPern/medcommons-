<?php

require_once "JSON.php";



abstract class jsonws {

	private $servicetag;
	private $starttime;

function json_service_handler ($servicetag)
	{
		//
		// all JSON web services for iPhone come thru here
		$this->starttime = microtime(true);
		$this->servicetag = $servicetag;
		$results = $this->main_web_service (); // in the main body, returns an array of more JSON pairings
		$now = time();

		$servicetime = 1000*(microtime(true)-$this->starttime);
		$servicetime = number_format($servicetime, 2, '.', '');

		$json = new Services_JSON();

		// Because JSON encode sometimes emits warnings in the middle of output!
		//error_reporting(0);
		$gmt = gmstrftime("%b %d %Y %H:%M:%S");
			
		$front = array(
		'service'=>$this->servicetag,
		'servertime'=>"$gmt GMT",
		'servicetime'=>$servicetime,
		'servername'=>$_SERVER ['SERVER_NAME'],		
		'ipaddr'=>$_SERVER['REMOTE_ADDR'].'<>'.$_SERVER['SERVER_ADDR']);

		$out =  $json->encode(array_merge($front,$results));

		// keep a log file
		//$outsize = strlen($out);
		//dosql("Insert into itestlog set time='$now',service='$this->servicetag', remoteip='$ipaddr',
		//servicetime='$servicetime',responsesize='$outsize',response='$out' ");
			
		header ("Content-type: text/javascript");
		echo $out;

	}
}
?>