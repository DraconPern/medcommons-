<?php


require_once "Envision-DataKit/is.inc.php";
// common processing for both mainline and postback
global $GRID,$PROPS, $ebuf;
$ebuf = '';
$USER = user_record();
if ($USER===false) die("Must be logged in");
$GRID = $USER->grid;
$PROPS = get_properties();
$leagueid = mysql_real_escape_string($_REQUEST['leagueind']); // from the get or thru the post below
$result = dosql ("Select * from leagues where grid='$GRID' and ind='$leagueid' ");
$leaguerec = mysql_fetch_object($result);
if ($leaguerec===false) die ("League $leagueid  can not be found in grid $GRID");
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
	if ($op == 'add') {
			// alright, lets add this
		$name = $_POST['name'];  //
		//dosql ("Update players set  teamind=0, leagueind=0, status='Deleted' where grid='$GRID' and mcid='$accid' ");
		//dosql ("Delete from teamplayers where grid='$GRID' and playerind='$player->playerind' ");
		
		$status = mysql_query ("Insert into teams set grid='$GRID', name='$name' , leagueind='$leaguerec->ind' ");
		if ($status)
		{
		$teamind = mysql_insert_id(); // get last
		$status = mysql_query ("Insert into leagueteams set grid='$GRID', leagueind='$leaguerec->ind', teamind='$teamind' ");
		if ($status) $ebuf  .="<h5>Added team: $name to league: $leaguerec->name</h5>";
		}
		
	
	}
}
else
{
	// not POSTED, but GET'd

	// otherwise, all get does is put up a form
	$ebuf .= "<h3>Add Team To League: $leaguerec->name? </h3>
	<p>You can call the team anything you like for now</p>
	<form action=addteam.php method=post>
	<input type=hidden value=$leagueid name=leagueind />
	<input type=hidden value=add name=op />
	<input type=text name=name />
	<input type=submit value=add name=submit />
	<input type=submit value=cancel name=cancel />
	</form>
";
}
echo envision_page_shell ($ebuf);


?>