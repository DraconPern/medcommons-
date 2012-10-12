<?PHP
// report status of medcommons appliance as JSON
require 'settings.php';
require_once 'utils.inc.php';
require_once "ijswslib.inc.php";


class mcApplianceProbeNil extends jsonws {


	function main_web_service ()
	{


		return array('status'=>"ok");
		//"diskfreespace"=>$diskfree,
		//"disktotalspace"=>$disktotal,
		//'loadavg'=>$loadavg);
	}

}// end outer clas

//main
if (isset($_GET['service'])) $service = $_GET['service']; else $service = "loopback_basic";
$jsws = new mcApplianceProbeNil();
$jsws->json_service_handler($service);

?>