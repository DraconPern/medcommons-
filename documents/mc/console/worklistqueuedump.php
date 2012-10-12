<?PHP
require_once "../../dbparams.inc.php";
require_once "tabledump.inc.php";
//
///
////
///// custom table specific code goes below this line
////

class worklistqueuedumper extends tabledumper 
{

function table_query($idpre,$limit)
{return "SELECT * from worklist_queue
	WHERE (groups_group_number LIKE '$idpre%') OR 
			(description LIKE '$idpre%') OR 
		     (user_medcommons_user_id LIKE '$idpre%') LIMIT $limit";
}
function table_header()
{
	$out = "<tr><th>user</th><th>group</th><th>description</th>";
	return $out;
}
function table_row ($l)
{
// custom code to process one row, building hyperlinks and tooltips
// $network needs to be passed in or eliminated
		
$mcid = $l['user_medcommons_user_id'];
$group = $l['groups_group_number'];
$worklistid = $l['worklist_id'];
$description = $l['description'];


$userblock =  $this->blurb("cd.php?filter=$mcid&url=trackingnumberdump.php","click to examine user",$mcid);
$groupblock  =    $this->blurb("cd.php?filter=$group&url=groupsdump.php","click to examine group",$group);
$workblock =   $this->blurb("cd.php?filter=$description&url=hipaadump.php","worklist id: $worklistid",$description);
$out = "<tr><td>$userblock</td><td>$groupblock</td><td>$workblock</td></tr>";

return $out;
}
}

// main starts here
$u = new worklistqueuedumper();
echo $u->table_dump('Worklist Queue');
?>