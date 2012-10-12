<?PHP
require_once "../../dbparams.inc.php";
require_once "tabledump.inc.php";
//
///
////
///// custom table specific code goes below this line
////

class documentlocationdumper extends tabledumper
{

	function table_query($idpre,$limit)
	{return "SELECT * from document_location
	WHERE (document_location.id LIKE '$idpre%') OR 
		(document_location.document_id LIKE '$idpre%')  OR 
		(document_location.node_node_id LIKE '$idpre%') LIMIT $limit";
	}
	function table_header()
	{
		$out = "<tr><th>id</th><th>doc id</th><th>node id</th></tr>";
		return $out;
	}
	function table_row ($l)
	{
		// custom code to process one row, building hyperlinks and tooltips
		// $network needs to be passed in or eliminated

		$id = $l['id'];
		$document_id = $l['document_id'];
		$encrypted_key = $l['encrypted_key'];
		$node_id = $l['node_node_id'];
		$integrity_check = $l['integrity_check'];
		$integrity_status = $l['integrity_status'];
		$copy_number = $l['copynumber'];

		$id = $this->blurb("cd.php?filter=$id&url=hipaadump.php","$encrypted_key",$id);

		$document_id = $this->blurb("cd.php?filter=$document_id&url=hipaadump.php","integrity: $integrity_check $integrity_status",$document_id);
		$node_id = $this->blurb("cd.php?filter=$node_id&url=hipaadump.php","copy:$copy_number",$node_id);
		$out = "<tr><td>$id</td><td>$document_id</td><td>$node_id</td></tr>";

		return $out;
	}
}

// main starts here
$u = new documentlocationdumper();
echo $u->table_dump('Document Locations');
?>