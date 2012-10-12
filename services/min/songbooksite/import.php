<?php

require_once "songs.inc.php";

// Read CSV File


function onefile($dbname,$fname)
{
	$ebuf = '';
	$row = 0; $dupes = 0;
	$errs = '';	//time some api calls into facebook from here
	$t0 = microtime(true);

	$ebuf .="<br/>Storing to Songbook Table: $dbname "
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
				//$mcid = 0; $GRID=0;

				//$v = '0","'.implode('","',$data).'","0'; // dummy value for row index
				//$v = '"'.'0'.'","'.implode('","',$data).'","'.$GRID.'","'.$mcid.'"'; // dummy value for row index at start and gid at end
				$now='';
				
				$v = '"'.'0'.'","'.implode('","',$data).'","'.$now.'"';
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
	echo $ebuf;
	return $errs;
}

//main

sql_connect();
$errs= onefile('songs','songbook.csv');

if ($errs!='') echo "<h4>Errors</h4><p>$errs</p>";


?>