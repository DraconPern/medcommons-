<?PHP
require_once "../../dbparams.inc.php";
require_once "tabledump.inc.php";
//
///
////
///// custom table specific code goes below this line
////

class trackingnumberdumper extends tabledumper 
{

function table_query($idpre,$limit)
{return "SELECT * from tracking_number
	WHERE (tracking_number LIKE '$idpre%')  LIMIT $limit";
}
function table_header()
{
	$out = "<tr><th>tracking_number</th><th>rights</th></tr>";
	return $out;
}
function table_row ($l)
{
// custom code to process one row, building hyperlinks and tooltips
// $network needs to be passed in or eliminated
		
$tracking_number = $l['tracking_number'];
$rights_id = $l['rights_id'];
$epin = $l['encrypted_pin'];


$trackingblock =  $this->blurb("../../memberservice/logon.php?tracking=$tracking_number","click to logon",$tracking_number);
$rightsblock =    $this->blurb("cd.php?filter=$rights_id&url=rightsdump.php","epin: $epin",$rights_id);

$out = "<tr><td>$trackingblock</td><td>$rightsblock</td></tr>";

return $out;
}
}

// main starts here
$u = new trackingnumberdumper();
echo $u->table_dump('Tracking Number');
?>