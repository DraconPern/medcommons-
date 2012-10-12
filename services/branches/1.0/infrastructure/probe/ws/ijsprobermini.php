<?PHP
// report status of medcommons appliance as JSON
require 'settings.php';
require_once 'utils.inc.php';
require_once "ijswslib.inc.php";


class mcApplianceProbeMini extends jsonws {


	function main_web_service ()
	{

		$diskfree = disk_free_space("/")/(1024*1024*1024);
		$diskfree = number_format($diskfree, 2, '.', '');

		$disktotal = disk_total_space("/")/(1024*1024*1024);
		$disktotal = number_format($disktotal, 2, '.', '');

		$x =  exec('cat /proc/loadavg');
		$loadavg='';
		for ($i=0; $i<strlen($x); $i++)
		{
			$c =  (substr($x,$i,1));
			if (($c=='.')||($c>='0'&&$c<='9')) $loadavg .=$c; else break;
		}
		return array(
		'status'=>'ok',
		"diskfreespace"=>$diskfree,
		"disktotalspace"=>$disktotal,
		'loadavg'=>$loadavg);
	}

}// end outer clas

//main

$jsws = new mcApplianceProbeMini();
$jsws->json_service_handler("measure_load_average");

?>