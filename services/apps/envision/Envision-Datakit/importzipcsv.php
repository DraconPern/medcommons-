<?php
require_once "setup.inc.php";
require_once "is.inc.php";

// Upload Zipped CSV Table Set

// these dictionaries act as caches to avoid excessive sql reads

global $mappings,$tmappings,$lmappings; $mappings=array();$lmappings=array();$tmappings=array();

// buffer up output

global $ebuf; $ebuf='';

function makeplayer ($simtrakid, $ln,$fn,$dob,$sex,$img, $league,$team,$leagueind, $teamind, $status) // new args, only validated from importzipcsv
{ global $GRID; global $PROPS; global $mcaccounts;
//dbg("making player with healthurl $healthurl");

// if none supplied, then make one
$remoteurl = $PROPS['appliance']."/router/NewPatient.action?familyName=$ln*$GRID&givenName=$fn&dateOfBirth=$dob".
      "&sex=$sex".
      "&enableSimtrak=1".
      "&auth=".$PROPS['token'].
      "&oauth_consumer_key=".$PROPS['token']; 

try {
	// consumer token when creating patient
	$file = get_url($remoteurl);
	$json = new Services_JSON();
	$result = $json->decode($file);
	if(!$result)
	throw new Exception("Unable decode JSON returned from URL ".$remoteurl.": ".$file);

	if($result->status != "ok")
	throw new Exception("Bad status '".$result->status."' error='".$result->error."' returned from JSON call ".$remoteurl);

	$mcid = $result->patientMedCommonsId;
	$auth = $result->auth;
	$secret = $result->secret;
	$healthurl = $PROPS['appliance'].$mcid;

	$mcaccounts[] = array ($fn,$ln,$mcid,$league,$team); // bump count of accounts we created
	// lets be careful and make sure we always make new records
	$sql = "Insert into players set name='$fn $ln', team='$team', league = '$league', leagueind = '$leagueind', teamind='$teamind', imageurl='$img', oauthtoken='$auth,$secret', born='$dob', status='$status',healthurl='$healthurl',
	simtrakid = '$simtrakid' , mcid='$mcid', grid='$GRID'  ";
	$status =mysql_query($sql ) or die("Cant $sql ".mysql_error());
	if ($status == false ) return false;

	$playerind = isdb_insert_id(); // get last
	$sql = ("Insert into teamplayers set teamind='$teamind', playerind='$playerind' , grid='$GRID'");  // dupes don't mattr

	$status =mysql_query($sql );// or die("Cant $sql ".mysql_error());
	return $mcid;
}
catch(Exception $ex) {
	islog("?","Failed to create new patient.", $ex->getMessage());
	die("Unable to create new patient");
}
}
function lookuporcreateleague($league)
{
	global $GRID,$lmappings;
	//echo "lookuporcr $personid <br/>";
	$key = $GRID.'-'.$league;
	if (isset ($lmappings[$key])) return $lmappings[$key];
	//
	$result = dosql ("Select * from leagues where name='$league' and grid='$GRID' ");
	$r = mysql_fetch_object($result);
	if ($r===false)
	{
		// must add
		dosql ("insert into leagues set grid='$GRID',name='$league' ");
		$g = mysql_insert_id() ;
	}
	else $g = $r->ind;
	$lmappings[$key]=$g;
	return $g;
}
function lookuporcreateteam($team,$leagueind)
{
	global $GRID,$tmappings;
	//echo "lookuporcr $personid <br/>";
	$key = $GRID.'-'.$team;
	if (isset ($tmappings[$key])) return $tmappings[$key];
	//
	$result = dosql ("Select * from teams where name='$team' and grid='$GRID' ");
	$r = mysql_fetch_object($result);
	if ($r===false)
	{
		// must add
		dosql ("insert into teams set grid='$GRID',name='$team' , leagueind='$leagueind'");
		$g = mysql_insert_id() ;
		dosql ("insert into leagueteams set grid='$GRID', leagueind='$leagueind', teamind = '$g' ");
	}
	else $g = $r->teamind;
	$tmappings[$key]=$g;
	return $g;
}
function lookuporcreatemcid ($personid,$fn,$ln,$status,$gender,$birthdate,$league,$team,$leagueind,$teamind)
{
	global $GRID,$mappings;
	//echo "lookuporcr $personid <br/>";
	$key = $GRID.'-'.$personid;
	if (isset ($mappings[$key])) return $mappings[$key];
	//
	// ok, not cached, so look it up in player table

	$result = dosql("Select * from players where simtrakid='$personid' and grid='$GRID' ");
	$r = mysql_fetch_object($result);
	if ($r===false)
	{
		// lookup the team name
		$stteamnum = substr($personid,0,2);
		$result = dosql("select * from _g_mappings where grid = '$GRID' and stteamnum='$stteamnum' ");
		$r = mysql_fetch_object($result);
		if ($r)
		{
			$league = $r->league; $team = $r->team;
			$leagueind = lookuporcreateleague($league);
			$teamind = lookuporcreateteam($team,$leagueind);
		}

		//
		// create the player afresh and return his mcid
		$img='';
		$mcid =	 makeplayer ($personid, $ln,$fn,$birthdate,$gender,$img, $league,$team,$leagueind,$teamind, $status);
	} else
	// the player is already in there and thus has an mcid
	$mcid = $r->mcid;
	$mappings [$key] = $mcid;
	return $mappings [$key] ;
}
function onefile($dbname,$fname,$keyloc,$league,$team,$leagueind,$teamind)
{
	global $GRID,$ebuf;
	$row = 0; $dupes = 0;
	$errs = '';	//time some api calls into facebook from here
	$t0 = microtime(true);

	$ebuf .="<br/>Storing to Envision Table: $dbname "
	;
	$handle = fopen($fname, "r");
	if (!$handle) $errs.= ("Can't open $fname hence can't load table $dbname<br/>"); else
	{
		while (($data = fgetcsv($handle, 8000, ",")) !== FALSE) {
			$num = count($data);
			foreach ($data as $d) $d = clean($d);
			if ($row==0) {
				$num1 = $num;

				//	echo "Dbtable:            " . $dbname . "\n";
				$ebuf .= "fields:           " . $num1 . "\n";
				$row = 1; // move along to next index
			}
			else if ($num1==$num)
			{
				$mcid = 0;
				// if the keyloc (personid) field is in there, then get it out and go looking for an mcid
				if (($keyloc!=0)&&($dbname=='PERSON')){
					$personid = $data[$keyloc-1];
					//	echo "keyloc $keyloc personid $personid <br/>";
					$mcid = lookuporcreatemcid ($personid,$data[7],$data [9],$data[10],$data[14],$data[15],$league,$team,$leagueind,$teamind); // risky business
				}
				//$v = '0","'.implode('","',$data).'","0'; // dummy value for row index
				$v = '"'.'0'.'","'.implode('","',$data).'","'.$GRID.'","'.$mcid.'"'; // dummy value for row index at start and gid at end
				$q="Insert into $dbname values($v)"; //echo $q.'<br/>';
				$status = mysql_query($q);
				if (!$status) {
					if (strpos(mysql_error(),'uplicate entry')>0) $dupes++; else // ignore duplicates and just display them
					$errs .= ("insert into $dbname row $row fields $num1  data {$data[0]} failed ".mysql_error()."<br/>");
				}
				$row++;
			}

		}
		fclose($handle);
		//test 1 - smallest possible call asks if I am app user
		$t1 = microtime(true);


		$delta1 = round($t1-$t0,3);
		$ebuf .= "rows:             " . ($row-1) . " duplicates: $dupes Elapsed: $delta1 \n";
	}
	return $errs;
}

//main
global $GRID, $PROPS,$mcaccounts;

$t1 = microtime(true);	$mcaccounts = array();

$r = user_record();
if ($r===false) die("Must be logged in");

$GRID = $r->grid;

$PROPS = get_properties();
$pcmappings=array(); $toprocess=array();
$team = $PROPS['defteam']; // teamnamefromind($teamind);
$league = $PROPS ['defleague'];

$leagueind = lookuporcreateleague ($league);
$teamind = lookuporcreateteam($team,$leagueind);
$result = dosql("Select * from _pctables order by importsort desc");
while ($r=mysql_fetch_object($result)) $pcmappings[]=array($r->table,$r->csvspec,$r->keyloc,$r->importsort); // add one extra slot for temp file name


$incoming = $_FILES['uploaded']['name'];
$ebuf .= "<h4>Importing " . $incoming .' into '. $PROPS['servicename']." (grid: $GRID)</h4>";
$zipfile = $_FILES['uploaded']['tmp_name'];
$ebuf .= "Uploaded Zipfile:            " . $zipfile . "\n<br/>";
$ebuf .=  "Default Team:            " . $PROPS['defteam'] . "\n<br/>";
$ebuf .= "Default League:            " . $PROPS['defleague'] . "\n<br/>";
if ( $_FILES['uploaded']['type']=='application/zip')
{
	$zip = zip_open($zipfile);
	if ($zip) {
		while ($zip_entry = zip_read($zip)) {
			$name=zip_entry_name($zip_entry) ;
			if (substr($name,0,2)!='__') {

				$ebuf .= "Reading:               " . $name . "\n";
					
				$ebuf .= "Actual Filesize:    " . zip_entry_filesize($zip_entry) . "\n";
				$ebuf .= "Compressed Size:    " . zip_entry_compressedsize($zip_entry) . "\n";
				$ebuf .= "Compression Method: " . zip_entry_compressionmethod($zip_entry) . "\n";

				if (zip_entry_open($zip, $zip_entry, "r")) {

					$buf = zip_entry_read($zip_entry, zip_entry_filesize($zip_entry));
					$newname = $zipfile.'-'.$name;
					file_put_contents($newname,$buf);

					foreach ($pcmappings as $mapping) if ($mapping[1]==$name) { //m fix up mappings array with temp file

						$toprocess[$mapping[3]] = array ($mapping[0],$newname,$mapping[2]);
						break;
					}
					zip_entry_close($zip_entry);
				}
				$ebuf .= "\n<br/>";
			}
		}
		zip_close($zip);
	}

	else $ebuf .= "Cant open $zipfile \n<br/>";
}
else $ebuf .=  "Only Envision zip files can be uploaded\n<br/>";


$t2 = microtime(true);


$delta2 = round($t2-$t1,3);
// now play it all out
$count = count($toprocess); $errs='';
$ebuf .=  "Time: $delta2 <h4>Processing</h4>";
for ($j=0; $j<$count; $j++)

$errs .= onefile($toprocess[$j][0],$toprocess[$j][1],$toprocess[$j][2],$league,$team,$leagueind,$teamind);
if (count($mcaccounts) !=0)
{   $count = count($mcaccounts);
	$mca = "<h4>Created $count new MedCommons Accounts on {$PROPS['appliance']}</h4><ul>";
	foreach ($mcaccounts as $ma) $mca .= "<li>{$ma[0]} {$ma[1]} {$ma[2]} {$ma[3]} {$ma[4]} </li>";
	$mca .= '</ul>';
}
else $mca = '';

if ($errs!='') $errs = "<h4>Errors</h4><p>$errs</p>";
echo envision_page_shell($ebuf.$mca.$errs);

//print_r ($mappings);

?>