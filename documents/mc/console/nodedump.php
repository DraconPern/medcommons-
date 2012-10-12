<?PHP
require_once "../../dbparams.inc.php";
require_once "tabledump.inc.php";
//
///
////
///// custom table specific code goes below this line
////

class nodedumper extends tabledumper 
{

function table_query($idpre,$limit)
{return "SELECT * from node
	WHERE (node_id LIKE '$idpre%') OR 
(hostname LIKE '$idpre%') OR 
(node_type LIKE '$idpre%')LIMIT $limit";
}
function table_header()
{
	$out = "<tr><th>node</th><th>host</th><th>admin</th></tr>";
	return $out;
}
function table_row ($l)
{
// custom code to process one row, building hyperlinks and tooltips
// $network needs to be passed in or eliminated
$logging_server = $l['logging_server'];		
$creation_time = $l['creation_time'];
$node_type = $l['node_type'];
$fixed_ip = $l['fixed_ip'];
$hostname = $l['hostname'];
$display_name = $l['display_name'];
$m_key = $l['m_key'];
$e_key = $l['e_key'];
$admin_id = $l['admin_id'];
$node_id = $l['node_id'];



$nodeblock =  $this->blurb("cd.php?filter=$groups&url=nodedump.php","$node_type $creation_time",$node_id);
$hostblock = $this->blurb("cd.php?filter=$id&url=nodedump.php","$fixed_ip $display_name $logging_server ",$hostname);
$adminblock = $this->blurb("cd.php?filter=$type&url=nodedump.php","mkey: $m_key ekey: $e_key",$admin_id);


$out = "<tr><td>$nodeblock</td><td>$hostblock</td><td>$adminblock</td></tr>";

return $out;
}
}

// main starts here
$u = new nodedumper();
echo $u->table_dump('Node');
?>