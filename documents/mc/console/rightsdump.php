<?PHP
require_once "../../dbparams.inc.php";
require_once "tabledump.inc.php";
//
///
////
///// custom table specific code goes below this line
////

class rightsdumper extends tabledumper 
{

function table_query($idpre,$limit)
{return "SELECT * from rights
	WHERE (rights_id LIKE '$idpre%') OR 
(user_medcommons_user_id LIKE '$idpre%') OR 
(document_ID LIKE '$idpre%')LIMIT $limit";
}
function table_header()
{
	$out = "<tr><th>rights id</th><th>document id</th><th>mc id</th></tr>";
	return $out;
}
function table_row ($l)
{
// custom code to process one row, building hyperlinks and tooltips
// $network needs to be passed in or eliminated
		
$groups = $l['groups_group_number'];
$userid = $l['user_medcommons_user_id'];
$rights_id = $l['rights_id'];
$rights = $l['rights'];
$document = $l['document_ID'];
$creation_time = $l['creation_time'];
$expiration_time = $l['expiration_time'];
$rights_time = $l['rights_time'];
$accepted_time = $l['accepted_time'];

$rightsblock =  $this->blurb("cd.php?filter=$rights&url=rightsdump.php"," $rights $rights_time",$rights_id);
$documentblock = $this->blurb("cd.php?filter=$document&url=documentdump.php","cr:$creation_time ex:$expiration_time",$document);
$userblock = $this->blurb("cd.php?filter=$userid&url=userdump.php","ac: $accepted_time gr: $groups",$userid);


$out = "<tr><td>$rightsblock</td><td>$documentblock</td><td>$userblock</td></tr>";

return $out;
}
}

// main starts here
$u = new rightsdumper();
echo $u->table_dump('Rights');
?>