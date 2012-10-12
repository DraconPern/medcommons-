<?php

require_once "songs.inc.php";
//
//
// make zipped up csv archive from mysql tables
//
// for each table of interest, build a long string which is the csv equivalent of the table




function xml_table($table)
{
	$out = "<$table>
"; $counter=0;
	$q = "select * from $table";
	$result =dosql($q); $firstrow = true;
	while ($row = mysql_fetch_assoc($result)) {
		$counter++;
		//$out .="<record seq='$counter' simtrak-table='$table' id='{$row['id']}' grid='{$row['grid']}' mcid='{$row['mcid']}' >";
		// wrtie out the data row

		foreach ($row as $key => $value) { 
			$stripped = csv_strip   // breaks with poorly formed xml on output to prower if we don't call CSV strip - I think there are \\ in the standard data set
			            ($value); 
			if (($stripped!='') /*
			  &&($key!='id')
			    &&($key!='grid')*/
			     &&($key=='tune')
			  )	
				$out .= "<$key>$stripped</$key>
";
		}

		//$out .='</record>';
		//	$out= "#1 This is a test string added as $table.txt.\n";
	}
	mysql_free_result($result);
	//	echo "Table $table - count: $counter<br/>
	//	<hr/>";

	$out .="</$table>
";
	return $out;
}
// wrap them all up as one big xml document
//



$now=strftime('%D %T');
$buf = '';
$db = $GLOBALS['DB_Database'];
$table = 'songs';

Header("Content-type: text/xml");
//Header ("Content-disposition: attachment; filename=$filename");;
echo <<<XXX
<?xml version="1.0" encoding="utf-8" ?>
<!--
-
- all Songs XML Dump
- version 0.8
- Host: localhost
- Generation Time: $now

-->

<!--
- Database: '$db'
-->

XXX;
echo "<TableExport xporttime='$now' filter='none' >
<description>ALL Songs Master Set List</description>
  ";
echo xml_table ($table);
echo "</TableExport>";
?>