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
			$myFile = "ws/pics/nyc_foodstuff_boro_$boro.json.txt";
			$fh = fopen($myFile, 'w') or die("can't open file");
		
			$sql = "select * from nyc_last where boro='$boro' order by boro,DBA limit 100000";
			$results = mysql_query($sql);
			while ($r=mysql_fetch_object($results))
			{
				// write file incrementally, so it can be as big as we like
		
			
			
					
		
				//hand roll the json
				if (!$first) $onerow = ', 
					'; else $onerow = '
						[';
				$onerow .= <<<XXX
				{
					"CN":"$r->CamisNumber",
					"DBA":"$r->DBA","boro":"$r->boro","bui":"$r->building","st":"$r->street","zip":"$r->Zip",
					"idate":"$r->Inspection_date","action":"$r->Action",
					"pa":"$r->Pre_Adjudication","vc":"$r->Violation_code","sc":"$r->Score","ga":"$r->GoldenApple",
					"ph":"$r->Phone","sf":"$r->Score_flag","count":"$r->count",
					"ds":"$r->description"
				}
XXX;
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
				 ]
				 '); // close off the JSON array
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