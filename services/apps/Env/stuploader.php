<?php
require_once "setup.inc.php";

// Upload Zipped CSV Table Set

function dosql($q)
{
	if (!isset($GLOBALS['db_connected']) ){
		$GLOBALS['db_connected'] = mysql_connect($GLOBALS['DB_Connection'] ,$GLOBALS['DB_User'] );
		$db = $GLOBALS['DB_Database'];
		mysql_select_db($db) or die ("can not connect to database $db ".mysql_error());
	}
	$status = mysql_query($q);
	//	if (!$status) echo ("dosql failed $q ".mysql_error());
	return $status;
}
function clean($s)
{
	$s=  str_replace('"','',$s);
	return mysql_real_escape_string(trim($s));
}
function onefile($dbname,$fname)
{
	$row = 1;
	$errs = '';

	$handle = fopen($fname, "r");
	if (!$handle) $errs.= ("Can't open $fname hence can't load table $dbname<br/>"); else
	{
		while (($data = fgetcsv($handle, 8000, ",")) !== FALSE) {
			$num = count($data);
			foreach ($data as $d) $d = clean($d);
			if ($row==1) {
				$num1 = $num;

				echo "Dbtable:            " . $dbname . "\n";
				echo "Fields:           " . $num1 . "\n";
			}
			else if ($num1==$num)
			{
				$v = '0","'.implode('","',$data); // dummy value for row index
					
				$status = dosql("Insert into $dbname values(\"$v\")");
				if (!$status) $errs .= ("insert into $dbname row $row fields $num1  data {$data[0]} failed ".mysql_error()."<br/>");
			}
			$row++;
		}
		fclose($handle);
		echo "Rows:             " . $row . "\n";
	}
	if ($errs!='')
	echo "Errs:             <br/>" . $errs . "\n";
	echo "<br/>";
}

//main

if (!isset($_FILES['uploaded']))
{
	$html = <<<XXX
<form enctype="multipart/form-data" action="stuploader.php" method="POST">
Please choose a Simtrak Zipped CSV file: <input name="uploaded" type="file" /><br />
<input type="submit" value="Upload" />
</form>
XXX;
	echo $html;
}
else
{
	$mappings=array();
	$result = dosql("Select * from pctables");
	while ($r=mysql_fetch_object($result)) $mappings[]=array($r->table,$r->csvspec);
	/*
	 $mappings = array(
	 array('WEIGHT',"Simtrak-Weight.csv"),
	 array('FACTRN',"Simtrak-FacilityTransactions.csv"),
	 array('INJURY', "Simtrak-Injury.csv"),
	 array('PHYSIC',"Simtrak-Physical.csv"),
	 array('TRTMNT',"Simtrak-Treatment.csv"),
	 array('PERSON',"Simtrak-Personnel.csv"),
	 array('PROGRS',"Simtrak-Progress.csv"),
	 );
	 */
	$ret ='';
	$incoming = $_FILES['uploaded']['name'];
	echo "Incoming Zipfile:            " . $incoming . "\n<br/>";
	$zipfile = $_FILES['uploaded']['tmp_name'];
	echo "Uploaded Zipfile:            " . $zipfile . "\n<br/>";
	if ( $_FILES['uploaded']['type']=='application/zip')
	{
		$zip = zip_open($zipfile);
		if ($zip) {
			while ($zip_entry = zip_read($zip)) {
				$name=zip_entry_name($zip_entry) ;
				if (substr($name,0,2)!='__') {

					echo "Embedded CSV Name:               " . $name . "\n";
					echo "Actual Filesize:    " . zip_entry_filesize($zip_entry) . "\n";
					echo "Compressed Size:    " . zip_entry_compressedsize($zip_entry) . "\n";
					echo "Compression Method: " . zip_entry_compressionmethod($zip_entry) . "\n";

					if (zip_entry_open($zip, $zip_entry, "r")) {

						$buf = zip_entry_read($zip_entry, zip_entry_filesize($zip_entry));
						$newname = $zipfile.'-'.$name;
						file_put_contents($newname,$buf);
						foreach ($mappings as $mapping) if ($mapping[1]==$name) {
							onefile($mapping[0],$newname); break;
						}
						zip_entry_close($zip_entry);
					}
					echo "\n<br/>";
				}
			}
			zip_close($zip);
		}

		else echo "Cant open $zipfile \n<br/>";
	}
	else echo "Only zip files can be uploaded\n<br/>";

	echo $ret;
}
?>