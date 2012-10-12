<?PHP
require_once "../../dbparams.inc.php";
require_once "tabledump.inc.php";
//
///
////
///// custom table specific code goes below this line
////

class userdumper extends tabledumper 
{

function table_query($idpre,$limit)
{return "SELECT * from user
	WHERE (user.email_address LIKE '$idpre%') OR 
		(user.medcommons_user_id LIKE '$idpre%') OR 
			(user.name LIKE '$idpre%') ORDER BY email_address LIMIT $limit";
}
function table_header()
{
	$out = "<tr><th>email</th><th>ipv</th><th>mcid</th>";
	return $out;
}
function table_row ($l)
{
// custom code to process one row, building hyperlinks and tooltips
// $network needs to be passed in or eliminated
		
$mcid = $l['medcommons_user_id'];
$serial = $l['serial'];
$gateway1 = $l['gateway1'];
$gateway2 = $l['gateway2'];
$ipv = $l['identity_provider'];
$certurl = $l['cert_Url'];
$status = $l['status'];
$name = $l['name'];
$email = $l['email_address'];
$certc = $l['cert_checked'];
$wip = $l['wired_ipaddress'];

if ($wip=="") $wip="unwired";

$email =  $this->blurb("../../memberservice/logon.php?user=$email","$name $wip $status $gateway1",$email);
$ipv =    $this->blurb($certurl,"$serial $name $certc",$ipv);
$mcid =   $this->blurb("cd.php?filter=$mcid&url=hipaadump.php",'click to view hipaa entries',$mcid);
$out = "<tr><td>$email</td><td>$ipv</td><td>$mcid</td>";

return $out;
}
}

// main starts here
$u = new userdumper();
echo $u->table_dump('Users');
?>