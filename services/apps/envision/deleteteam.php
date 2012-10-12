<?php


require_once "Envision-DataKit/is.inc.php";
// common processing for both mainline and postback
global $GRID,$PROPS, $ebuf;
$ebuf = '';
$USER = user_record();
if ($USER===false) die("Must be logged in");
$GRID = $USER->grid;
$PROPS = get_properties();
$teamind = mysql_real_escape_string($_REQUEST['teamind']); // from the get or thru the post below
$result = dosql ("Select * from teams where grid='$GRID' and teamind='$teamind' ");
$teamrec = mysql_fetch_object($result);
if ($teamrec===false) die ("Team $teamind  can not be found in grid $GRID");
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
		//
		// check to make sure no players on this team
		//
		$result = mysql_query ("Select count(*) from teamplayers where grid='$GRID' and teamind='$teamind' ");
		if ($result)
		{
			$counta = mysql_fetch_array($result);

			if (($counta)&&($counta[0]>0) )
			{
				$ebuf.= "<h5>Please remove ".$counta[0]." players before removing this team. </h5>";
			}
			else {
				dosql ("Delete from teams where  grid='$GRID' and teamind='$teamind' ");
				dosql ("Delete from leagueteams where  grid='$GRID' and teamind='$teamind' ");
				$ebuf  .="<h5>Deleted team $teamrec->name</h5>";
			}
		}
	}
}
else
{
	// not POSTED, but GET'd

	// otherwise, all get does is put up a form
	$ebuf .= "<h3>Delete Team $teamrec->name? </h3>
	<p>You must remove all players before the delete will work</p>
	<form action=deleteteam.php method=post>
	<input type=hidden value=$teamind name=teamind />
	<input type=hidden value=delete name=op />

	<input type=submit value=delete name=submit />
	<input type=submit value=cancel name=cancel />
	</form>
";
}
echo envision_page_shell ($ebuf);


?>