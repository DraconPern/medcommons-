<?php
	
	/**
	 * queryServicesList
	 *
	 * Returns JSON representing an arbirary table
	 *
	 */
	
	header("Cache-Control: no-store, no-cache, must-revalidate");
	header("Pragma: no-cache");
	require_once "../acct/alib.inc.php";
	require_once "wslibdb.inc.php";
	require_once "utils.inc.php";
	require_once "mc.inc.php";
	
	function emit ($fh,$s)
	{
		//echo $s."<br/>"; //for initial debugging only
		fwrite($fh, $s);
	}
	class queryRemoteDB extends jsonrestws {
		function verify_caller() {
			
			return;
		}
		function jsonbody() {
			$db = $GLOBALS['DB_Database'];
			
			mysql_connect($GLOBALS['DB_Connection'],
						  $GLOBALS['DB_User'],
						  $GLOBALS['DB_Password']
						  ) or die ("can not connect to mysql");
			mysql_select_db($db) or die ("can not connect to database $db");
			$counter = 0;
			
			$boro = req('boro');
		    if(($boro<0)||($boro>5))
			   throw new Exception("bad boro specified");
			
			// $sql = "select guid, status, date_format(date , '%Y-%m-%d %H:%i:%s') as date, tracking from ccrlog where accid=?  order by date desc";
			$first = true;
			$myFile = "ws/pics/nyc_foodstuff_boro_$boro.plist";
			$fh = fopen($myFile, 'w') or die("can't open file");
		
			$sql = "select * from nyc_last where boro='$boro' order by boro,DBA limit 100";
			$results = mysql_query($sql);
			while ($r=mysql_fetch_object($results))
			{
				// write file incrementally, so it can be as big as we like
	//			"CN":"$r->CamisNumber",
//				"DBA":"$r->DBA","boro":"$r->boro","bui":"$r->building","st":"$r->street","zip":"$r->Zip",
//				"idate":"$r->Inspection_date","action":"$r->Action",
//				"pa":"$r->Pre_Adjudication","vc":"$r->Violation_code","sc":"$r->Score","ga":"$r->GoldenApple",
//				"ph":"$r->Phone","sf":"$r->Score_flag","count":"$r->count",
//				"ds":"$r->description"
//			
//			
				$dba =  str_replace(array("'",'&'), array("*",'+'),$r->DBA);
				$ds =  str_replace(array("'",'&'), array("*",'+'),$r->description);

		
				//hand roll the json
				if (!$first) $onerow = ' 
					
'; 
				else $onerow = <<<XXX
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<array>

XXX;

$onerow .= 
"	<dict>
		<key>CN</key>
		<string>$r->CamisNumber</string>
		<key>DBA</key>
		<string>$dba</string>
		<key>action</key>
		<string>$r->Action</string>
		<key>boro</key>
		<string>$r->boro</string>
		<key>bui</key>
		<string>$r->building</string>
		<key>count</key>
		<string>$r->count</string>
		<key>ds</key>
		<string>$ds</string>
		<key>ga</key>
		<string>$r->GoldenApple</string>
		<key>idate</key>
		<string>$r->Inspection_date</string>
		<key>pa</key>
		<string>$r->Pre_Adjudication</string>
		<key>ph</key>
		<string>$r->Phone</string>
		<key>sc</key>
		<string>$r->Score</string>
		<key>sf</key>
		<string>$r->Score_flag</string>
		<key>st</key>
		<string>$r->street</string>
		<key>vc</key>
		<string>$r->Violation_code</string>
		<key>zip</key>
		<string>$r->Zip</string>
	</dict>";
				$counter++;
	//			$onerow .= <<<XXX
//				{"CamisNumber":$r->CamisNumber,"DBA":$r->DBA,"boro":$r->boro,"building":$r->building,"street":$r->street,"Zip":$r->Zip,
//					"Inspection_date":$r->Inspection_date,"Action":$r->Action,
//					"Pre_Adjudication":$r->Pre_Adjudication,"Violation_code":$r->Violation_code,"Score":$r->Score,"GoldenApple":$r->GoldenApple,
//					"Phone":$r->Phone,"Score_flag":$r->Score_flag,"count":$r->count,
//					"description":$r->description}
//				XXX;
				emit ($fh,$onerow);
				$first =false;
				
				
			}
			emit($fh,'
</array>
</plist>
'); // close off the dict
			fclose($fh);
			$this->result = new stdClass;
			$this->result->status="ok"; // tell the caller it went ok
			$this->result->cachelocation=$myFile;
			$this->result->query=$sql;
			$this->result->counter=$counter;
			return true;
		}
	}
	
	$x = new queryRemoteDB();
	$x->handlews("response_queryRemoteDB");
?>