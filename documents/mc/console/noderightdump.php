<?PHP
require_once "../../dbparams.inc.php";
require_once "tabledump.inc.php";
//
///
////
///// custom table specific code goes below this line
////

class noderightdumper extends tabledumper 
{

function table_query($idpre,$limit)
{return "SELECT * from node_right
	WHERE (node_node_id LIKE '$idpre%') OR 
		(groups_group_number LIKE '$idpre%') LIMIT $limit";
}
function table_header()
{
	$out = "<tr><th>node_node_id</th><th>groups_group_number</th><th>rights</th></tr>";
	return $out;
}
function table_row ($l)
{
// custom code to process one row, building hyperlinks and tooltips
// $network needs to be passed in or eliminated
		
$id = $l['node_node_id'];
$gid = $l['groups_group_number'];
$rights = $l['rights'];

$id =  $this->blurb("cd.php?filter=$id&url=nodedump.php","click to view transactions",$id);
$gid = $this->blurb("cd.php?filter=$gid&url=groupsdump.php","click to view transactions",$gid);


$out = "<tr><td>$id</td><td>$gid</td><td>$rights</td></tr>";

return $out;
}
}

// main starts here
$u = new noderightdumper();
echo $u->table_dump('Node Right');
?>