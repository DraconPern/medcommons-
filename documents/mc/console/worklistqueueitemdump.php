<?PHP
require_once "../../dbparams.inc.php";
require_once "tabledump.inc.php";
//
///
////
///// custom table specific code goes below this line
////

class worklistqueueitemdumper extends tabledumper 
{

function table_query($idpre,$limit)
{return "SELECT * from worklist_queue_item
	WHERE (order_number LIKE '$idpre%') OR 
			(worklist_queue_worklist_id LIKE '$idpre%') ORDER BY priority LIMIT $limit";
}
function table_header()
{
	$out = "<tr><th>worklist</th><th>order</th></tr>";
	return $out;
}
function table_row ($l)
{
// custom code to process one row, building hyperlinks and tooltips
// $network needs to be passed in or eliminated
		
$worklistid = $l['worklist_queue_worklist_id'];
$order = $l['order_number'];
$placed = $l['placed_in_queue'];
$description = $l['description'];
$rightsid = $l['rights_id'];

$workblock =   $this->blurb("cd.php?filter=$worklistid&url=worklistqueuedump.php","rightsid: $rightsid",$worklistid);

$orderblock  =    $this->blurb("cd.php?filter=$rightsid&url=rightsdump.php","$description $placed",$order);

$out = "<tr><td>$workblock</td><td>$orderblock</td></tr>";

return $out;
}
}

// main starts here
$u = new worklistqueueitemdumper();
echo $u->table_dump('Worklist Queue Item');
?>