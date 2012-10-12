<?php


require_once "Envision-DataKit/is.inc.php";
// common processing for both mainline and postback
global $GRID,$PROPS, $ebuf;
$ebuf = '';
$USER = user_record();
if ($USER===false) die("Must be logged in");
$GRID = $USER->grid;
$PROPS = get_properties();

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
		
		$status = mysql_query ("Insert into leagues set grid='$GRID', name='$name' ");
		if ($status)
		{

		if ($status) $ebuf  .="<h5>Added league: $name </h5>";
		}
		
	
	}
}
else
{
	// not POSTED, but GET'd

	// otherwise, all get does is put up a form
	$ebuf .= "<h3>Add League</h3>
	<p>You can call the league almost anything you like for now</p>
	<form action=addleague.php method=post>
	<input type=hidden value=add name=op />
	<input type=text name=name />
	<input type=submit value=add name=submit />
	<input type=submit value=cancel name=cancel />
	</form>
";
}
echo envision_page_shell ($ebuf);


?>