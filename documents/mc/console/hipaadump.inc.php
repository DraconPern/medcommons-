<?PHP
require_once "../../dbparams.inc.php";
require_once "tabledump.inc.php";
//
///
////
///// custom table specific code goes below this line
////

class hipaadumper extends tabledumper
{	
	private $tab;
	
	function set_table ($t)
	{
		$this->tab=$t;
	}
	
	function table_query($idpre,$limit)
	{
		$table = $this->tab;
		
	return "SELECT * from $table LEFT JOIN user ON $table.a2 = user.medcommons_user_id
	WHERE (user.email_address LIKE '$idpre%') OR 
	($table.a2 LIKE '$idpre%') OR 
	($table.tracking_number LIKE '$idpre%')
				ORDER BY $table.creation_time DESC LIMIT $limit";
	}
	function table_header()
	{
		$out = "<tr><th>time</th><th>tracking #</th><th>id</th><th>op</th><th>modality</th><th>obj1</th><th>obj2</th></tr>";
		return $out;
	}
	function table_row ($l)
	{
		// custom code to process one row, building hyperlinks and tooltips
		$t= $l['tracking_number'];
		$gw= $l['s4'];
		$pin= $l['hpin'];
		$e= $l['email_address'];
		$mcid= $l['a2'];
		$u= $l['cert_url'];
		if ($u=="") $u="hnocert.html";
		$s1 = $l['s1'];
		$s3 = $l['s3'];
		$obj1 = $l['a1'];
		$obj2 = $l['a3'];
		
		$ll = $this->blurb("$gw/tracking.jsp?tracking=$t&hpin=$pin","tipa",$t);
		
		$out = "<tr><td>".$l['creation_time']."</td><td>$ll</td>";
		
		if ($e=="") $out.= "<td>$mcid</td>";
		else $out.= "<td><a href=$u target='_NEW'>$e</a></td>";
		
		
		$out.="<td>". $s1. "</td><td>". $s3. "</td><td>".
		"<a href=$gw/dumpobj.jsp?objid=$obj1>$obj1</a>".
		"</td><td>". "<a href=$gw/dumpobj.jsp?objid=$obj2>$obj2</a>". "</td></tr>";

		return $out;
	}
}


?>