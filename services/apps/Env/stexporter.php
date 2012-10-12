<?php

require_once "is.inc.php";
//
//
// make zipped up csv archive from mysql tables
//
// for each table of interest, build a long string which is the csv equivalent of the table



function csv_strip ($v)
{
	// make data safe for csv readers
	// throw out everything even slightly hostile
	// tested against mac numbers 09
	
	$new = '';
	$len = strlen($v);
	for ($j=0; $j<$len; $j++)
	{
		$c = substr($v,$j,1);
		if (ord($c) >= ord(' '))
		if ($c!='"')
		if ($c!=',')
		if ($c!="'")
		if ($c!='\\')
		$new.=$c;
	}
	return $new;
}
function csv_table($table,$filter)
{
	$out = ''; $counter=0;
	$q = "select * from $table";
	$result =dosql($q); $firstrow = true;
	while ($row = mysql_fetch_assoc($result)) {
		$counter++;
		if ($firstrow){  // write data
			$first = true;
			foreach ($row as $key => $value) {
				if ($first) $out .= '"'.$key.'"';		else $out .= ', "'.$key.'"';	;
				$first=false;
			}
			$out .='
';
		};
		$firstrow = false;
		// wrtie out the data row
		$first = true;
		foreach ($row as $key => $value) { $stripped = csv_strip ($value);
		if ($first) $out .= '"'.$stripped.'"';		else $out .= ', "'.$stripped.'"';
		$first = false;
		}

		$out .='
';
		//	$out= "#1 This is a test string added as $table.txt.\n";
	}
	mysql_free_result($result);
	//	echo "Table $table - count: $counter<br/>
	//	<hr/>";
	return $out;
}
// wrap them all up as a zip at the browser
//

	$mappings=array();
	$result = dosql("Select * from pctables");
	while ($r=mysql_fetch_object($result)) $mappings[]=array($r->table,$r->csvspec);
	
$now=time();
$zip = new ZipArchive();
$x = rand(100000,999999)."$now.zip";
$filename = "xport/stexport-$x";

if ($zip->open($filename, ZIPARCHIVE::CREATE)!==TRUE) {
	exit("cannot open <$filename>\n");
}
$filter = '';
foreach ($tables as $table) $zip->addFromString("$table.csv" , csv_table($table[0],$filter));
$zip->close();
Header("Content-type: application/octet-stream");
Header ("Content-disposition: attachment; filename=$filename");;

$stuff  = file_get_contents($filename);
print $stuff;
//unlink ($filename); // gets rid of the temporary file'
//echo "wrote zip archive to $filename";

?>