<?PHP
require_once "../../dbparams.inc.php";
require_once "tabledump.inc.php";
//
///
////
///// custom table specific code goes below this line
////

class forensiclogdumper extends tabledumper 
{
	
function table_query($idpre,$limit)
{return "SELECT * from forensic_log
	WHERE (id LIKE '$idpre%') OR 
		(event_type LIKE '$idpre%')  ORDER BY creation_time LIMIT $limit";
}
function table_header()
{
	$out = "<tr><th>event</th><th>time</th><th>status</th>";
	return $out;
}
function table_row ($l)
{
// custom code to process one row, building hyperlinks and tooltips
// $network needs to be passed in or eliminated
		
$id = $l['id'];
$creation_time = $l['creation_time'];
$event_type = $l['event_type'];
$event_description = $l['event_description'];
$event_status = $l['event_status'];
$rights_id = $l['rights_id'];
$rights_table = $l['rights_table'];

$event =  $this->blurb("cd.php?filter=$event_type&url=hipaadump.php","$event_description",$event_type);
$time =    $this->blurb("cd.php?filter=$id&url=hipaadump.php","$id",$creation_time);
$status =   $this->blurb("cd.php?filter=$event_status&url=hipaadump.php","rights: $rights_id $rights_status",$event_status);


$out = "<tr><td>$event</td><td>$time</td><td>$status</td>";

return $out;
}
}

// main starts here
$u = new forensiclogdumper();
echo $u->table_dump('Forensic Log');
?>