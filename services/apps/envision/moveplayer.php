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
	if ($op == 'move') {
		// alright,
		$teamind = $_POST['teamind']; // new team
		$result = dosql ("Select * from teams where teamind='$teamind' ");
		$teamrec = mysql_fetch_object($result);
		if ($teamrec)
		{
			$leagueind = $teamrec->leagueind; // get the league index
			$result = dosql ("Select * from leagues where ind='$leagueind' ");
			$leaguerec = mysql_fetch_object($result);
			if ($leaguerec)
			{
				dosql ("Update players set  teamind='$teamind', leagueind='$leagueind', team='$teamrec->name', league='$leaguerec->name' where grid='$GRID' and mcid='$accid' ");
				dosql ("Update teamplayers set teamind='$teamind' where grid='$GRID' and playerind='$player->playerind' ");
				$ebuf  .="<h5>Moved $accid $player->name to team: $teamrec->name league: $leaguerec->name</h5>";
			}
		}
		
	}
}
else
{
	// not POSTED, but GET'd

	// otherwise, all get does is put up a form
	$teamchooser = allteamchooser('12345'); // fills in teamind variable
	$ebuf .= "<h3>Move Player $player->name From (team: $player->team) (league: $player->league) To Another Team</h3>
	<p>Deleted Players are kept forever in the database, out of harm's way but available for data analysis purposes</p>
	<form action=moveplayer.php method=post>
	<input type=hidden value=$accid name=id />
	<input type=hidden value=move name=op />
	move to: $teamchooser
	<input type=submit value=move name=submit />
	<input type=submit value=cancel name=cancel />
	</form>
";
}
echo envision_page_shell ($ebuf);


?>