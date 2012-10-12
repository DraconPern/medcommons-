<?PHP
require_once "../../dbparams.inc.php";
require_once "tabledump.inc.php";
//
///
////
///// custom table specific code goes below this line
////

class documentdumper extends tabledumper
{

	function table_query($idpre,$limit)
	{return "SELECT * from document
	WHERE (document.id LIKE '$idpre%') OR 
		(document.guid LIKE '$idpre%')  LIMIT $limit";
	}
	function table_header()
	{
		$out = "<tr><th>document id</th><th>guid</th></tr>";
		return $out;
	}
	function table_row ($l)
	{
		// custom code to process one row, building hyperlinks and tooltips
		// $network needs to be passed in or eliminated

		$id = $l['id'];
		$guid = $l['guid'];
		$encrypted_key = $l['encrypted_key'];
		$creation_time = $l['creation_time'];
		$rights_time = $l['rights_time'];
		$encrypted_hash = $l['encrypted_hash'];

		$id = $this->blurb("cd.php?filter=$id&url=hipaadump.php","$creation_time $encrypted_key",$id);

		$guid = $this->blurb("cd.php?filter=$guid&url=hipaadump.php","$rights_time $encrypted_hash",$guid);

		$out = "<tr><td>$id</td><td>$guid</td></tr>";

		return $out;
	}
}

// main starts here
$u = new documentdumper();
echo $u->table_dump('Document');
?>