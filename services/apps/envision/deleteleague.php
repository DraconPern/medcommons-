<?php


require_once "Envision-DataKit/is.inc.php";
// common processing for both mainline and postback
global $GRID,$PROPS, $ebuf;
$ebuf = '';
$USER = user_record();
if ($USER===false) die("Must be logged in");
$GRID = $USER->grid;
$PROPS = get_properties();
$leagueind = mysql_real_escape_string($_REQUEST['leagueind']); // from the get or thru the post below
$result = dosql ("Select * from leagues where grid='$GRID' and ind='$leagueind' ");
$leaguerec = mysql_fetch_object($result);
if ($leaguerec===false) die ("League $leagueind  can not be found in grid $GRID");
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
		$result = mysql_query ("Select count(*) from leagueteams where grid='$GRID' and leagueind='$leagueind' ");
		if ($result)
		{
			$counta = mysql_fetch_array($result);

			if (($counta)&&($counta[0]>0) )
			{
				$ebuf.= "<h5>Please remove ".$counta[0]." teams before removing this league. </h5>";
			}
			else {
				dosql ("Delete from leagues where  grid='$GRID' and ind='$leagueind' ");
			
				$ebuf  .="<h5>Deleted league $leaguerec->name</h5>";
			}
		}
	}
}
else
{
	// not POSTED, but GET'd

	// otherwise, all get does is put up a form
	$ebuf .= "<h3>Delete League $leaguerec->name? </h3>
	<p>You must remove all players before the delete will work</p>
	<form action=deleteleague.php method=post>
	<input type=hidden value=$leagueind name=leagueind />
	<input type=hidden value=delete name=op />

	<input type=submit value=delete name=submit />
	<input type=submit value=cancel name=cancel />
	</form>
";
}
echo envision_page_shell ($ebuf);


?>