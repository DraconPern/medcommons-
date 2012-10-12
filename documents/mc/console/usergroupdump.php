<?PHP
require_once "../../dbparams.inc.php";
require_once "tabledump.inc.php";
//
///
////
///// custom table specific code goes below this line
////

class usergroupdumper extends tabledumper 
{

function table_query($idpre,$limit)
{return "SELECT * from user_group
	WHERE (groups_group_number LIKE '$idpre%') OR 
		(user_medcommons_user_id LIKE '$idpre%') LIMIT $limit";
}
function table_header()
{
	$out = "<tr><th>user</th><th>group</th><th>role</th>";
	return $out;
}
function table_row ($l)
{
// custom code to process one row, building hyperlinks and tooltips
// $network needs to be passed in or eliminated
		
$mcid = $l['user_medcommons_user_id'];
$group = $l['groups_group_number'];
$role = $l['user_role_with_group'];
$added = $l['added_by_id'];


$userblock =  $this->blurb("cd.php?filter=$mcid&url=trackingnumberdump.php","click to examine user",$mcid);
$groupblock  =    $this->blurb("cd.php?filter=$group&url=groupsdump.php","click to examine group",$group);
$roleblock =   $this->blurb("cd.php?filter=$role&url=hipaadump.php",'added by: $added',$role);
$out = "<tr><td>$userblock</td><td>$groupblock</td><td>$roleblock</td>";

return $out;
}
}

// main starts here
$u = new usergroupdumper();
echo $u->table_dump('User Group');
?>