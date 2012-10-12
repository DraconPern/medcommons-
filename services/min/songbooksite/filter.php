<?php
require_once "songs.inc.php";
// start here
//$GLOBALS['debug']=true;
$perpage=6;
$out = file_get_contents('songheader.htm');
$config = $_REQUEST["f"];
$xmldata = simplexml_load_file($config);
$song = $xmldata->songs;
$desc = $xmldata->description; 

$out = str_replace('$$$title$$$',$desc,$out); // build title from description
$num=1;$last='';
foreach ($song->tune as $tune)
{
	$tune = trim($tune);
	//	echo "Locating $tune<br/>";
	$result = dosql ("select * from songs where tune='$tune'");
	if (false===$result) die ("Cant select $tune error ".mysql_error());
	$r = mysql_fetch_object($result);
	if (false===$r)  echo ("Cant fetch $tune error ".mysql_error()); else
	{
		//echo "Found $r->tune key: $r->key chords: $r->chords <br/>";
		if ($last !='')
		{
			$out .= "
			<div class='next'>next:&nbsp;$tune</div>
			</div>
		";
			$mod = $num -floor($num/$perpage)*$perpage;
			
			if (1== $mod  )  $out .="<div class='break' title = 'num $num mod $mod' ></div>";
		}
		$out .= "
		<div class='song'>
		";
		$out .= "<div class='num'>&nbsp;song&nbsp;#$num&nbsp;</div> <div class='desc'>&nbsp;$desc&nbsp;</div>
		<div class='tune'>$r->tune</div>
		<div class='key'>$r->key</div>
		<div class='chords'>$r->chords</div>
		";
		if ($last!='')$out .= "<div class='last'>last:&nbsp;$last</div>
		";

		$num++;
		$last = $r->tune;

	}
}
if ($last!='') $out .= "	</div>
		";



echo $out;
?>