<?PHP
require_once "../../dbparams.inc.php";
require_once "tabledump.inc.php";
//
///
////
///// custom table specific code goes below this line
////

class groupsdumper extends tabledumper 
{

function table_query($idpre,$limit)
{return "SELECT * from groups
	WHERE (name LIKE '$idpre%') OR 
(location LIKE '$idpre%') OR 
(group_type LIKE '$idpre%') ORDER BY name LIMIT $limit";
}
function table_header()
{
	$out = "<tr><th>name</th><th>location</th><th>type</th></tr>";
	return $out;
}
function table_row ($l)
{
// custom code to process one row, building hyperlinks and tooltips
// $network needs to be passed in or eliminated
		
$group_number = $l['group_number'];
$name = $l['name'];
$location = $l['location'];
$group_type = $l['group_type'];
$admin_id = $l['admin_id'];
$point_of_contact_id = $l['point_of_contact_id'];

$nameblock =  $this->blurb("cd.php?filter=$id&url=hipaadump.php","group: $group_number admin: $admin_id",$name);
$locationblock = $this->blurb("cd.php?filter=$gid&url=hipaadump.php","contact: $point_of_contact_id",$location);
$typeblock = $this->blurb("cd.php?filter=$gid&url=hipaadump.php","click to view transactions",$group_type);


$out = "<tr><td>$nameblock</td><td>$locationblock</td><td>$typeblock</td></tr>";

return $out;
}
}

// main starts here
$u = new groupsdumper();
echo $u->table_dump('Groups');
?>