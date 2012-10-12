<?PHP
require_once "../../dbparams.inc.php";
require_once "tabledump.inc.php";
//
///
////
///// custom table specific code goes below this line
////

class inboxesdumper extends tabledumper 
{

function table_query($idpre,$limit)
{return "SELECT * from inboxes
	WHERE (inbox_id LIKE '$idpre%') OR 
(user_medcommons_user_id LIKE '$idpre%') OR 
(descriptor LIKE '$idpre%')LIMIT $limit";
}
function table_header()
{
	$out = "<tr><th>group</th><th>inbox</th><th>descriptor</th></tr>";
	return $out;
}
function table_row ($l)
{
// custom code to process one row, building hyperlinks and tooltips
// $network needs to be passed in or eliminated
		
$groups = $l['groups_group_number'];
$userid = $l['user_medcommons_user_id'];
$type = $l['descriptor_type'];
$descriptor = $l['descriptor'];
$authentication = $l['authentication'];
$id = $l['inbox_id'];

$groupblock =  $this->blurb("cd.php?filter=$groups&url=groupsdump.php","mcid: $userid",$groups);
$inboxblock = $this->blurb("cd.php?filter=$id&url=groupsdump.php","click to view ",$id);
$descriptorblock = $this->blurb("cd.php?filter=$type&url=hipaadump.php","type:$type auth:$authentication",$descriptor);


$out = "<tr><td>$groupblock</td><td>$inboxblock</td><td>$descriptorblock</td></tr>";

return $out;
}
}

// main starts here
$u = new inboxesdumper();
echo $u->table_dump('Inboxes');
?>