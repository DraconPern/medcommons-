<?php


require_once "Envision-DataKit/is.inc.php";
// common processing for both mainline and postback
global $GRID,$PROPS, $ebuf;
$ebuf = '';
$USER = user_record();
if ($USER===false) die("Must be logged in");
$GRID = $USER->grid;
$PROPS = get_properties();
$accid = mysql_real_escape_string($_REQUEST['id']); // from the get or thru the post below
$result = dosql ("Select * from players where grid='$GRID' and mcid='$accid' ");
$player = mysql_fetch_object($result);
if ($player===false) die ("Player $accid can not be found in grid $GRID");
if (isset($_POST['cancel']))
{
	header ("Location: envision.php");
	die ("Cancel redirected back to home");
}
else
// end of common processing

if (isset($_POST['op']))
{
	// if opcode present then handle postback
	$op = $_POST['op'];
	if ($op == 'delete') {
			// alright, lets clean him out 
		dosql ("Update players set  teamind=0, leagueind=0, status='Deleted' where grid='$GRID' and mcid='$accid' ");
		dosql ("Delete from teamplayers where grid='$GRID' and playerind='$player->playerind' ");
	
		$ebuf  .="<h5>Deleted $accid $player->name</h5>";
	}
}
else
{
	// not POSTED, but GET'd

	// otherwise, all get does is put up a form
	$ebuf .= "<h3>Delete Player $player->name (team: $player->team) (league: $player->league) ? </h3>
	<p>Deleted Players are kept forever in the database, out of harm's way but available for data analysis purposes</p>
	<form action=deleteplayer.php method=post>
	<input type=hidden value=$accid name=id />
	<input type=hidden value=delete name=op />
	<input type=submit value=delete name=submit />
	<input type=submit value=cancel name=cancel />
	</form>
";
}
echo envision_page_shell ($ebuf);


?>