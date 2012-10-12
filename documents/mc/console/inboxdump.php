<?PHP
require_once "../../dbparams.inc.php";
require_once "tabledump.inc.php";
//
///
////
///// custom table specific code goes below this line
////

class inboxdumper extends tabledumper 
{

function table_query($idpre,$limit)
{return "SELECT * from inbox
	WHERE (inbox_name LIKE '$idpre%') OR 
(inbox_location LIKE '$idpre%') OR 
(inbox_type LIKE '$idpre%') ORDER BY inbox_name LIMIT $limit";
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
		
$name = $l['inbox_name'];
$location = $l['inbox_location'];
$type = $l['inbox_type'];
$id = $l['inbox_id'];

$nameblock =  $this->blurb("cd.php?filter=$name&url=hipaadump.php","id: $id",$name);
$locationblock = $this->blurb("cd.php?filter=$location&url=hipaadump.php","click to view ",$location);
$typeblock = $this->blurb("cd.php?filter=$type&url=hipaadump.php","click to view transactions",$type);


$out = "<tr><td>$nameblock</td><td>$locationblock</td><td>$typeblock</td></tr>";

return $out;
}
}

// main starts here
$u = new inboxdumper();
echo $u->table_dump('Inbox');
?>