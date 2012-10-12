<?php
//purple box simulator
// (c) 2004 MedCommons, Inc.
// wld 8/28/04
//
// this page does two completely different things depending on whether replay was checked
require_once ("../lib/htmlsubs.inc");




function execute($url)
{
/*	$str = file_get_contents($url);
	return ($str!="");
	*/
//	htmlbody( "execute ".$url."\r\n");
	$line = file($url);
//	echo  $line[0]."\r\n";
	return ($line[0]!=""); // this works in php4
	
}

function replay()
{
	$seconds = 10;/* run for 10 seconds */
	$gw=cleanreq('gateway');
	htmltop ("Replaying saved transactions for ".
								$gw,SIMULATOR_STYLE,0);

	$trancount =0;

	$query = db_prepare_replay_select($gw);

	htmlbody($query.eol());

	$result = db_query($query,$GLOBALS['DB_Link']) ;//or die("Query failed : " . db_error());
	$status = db_error($GLOBALS['DB_Link']);/////
	if ($status!="") die($status);
	$ind = -1;
	while ($line = db_fetch_array($result)) {
		$ind = $ind+1;
		$fullurl[$ind] = "http:".$GLOBALS['RootURL'].$line['RP_WEBSERVICECOMMAND'];}
		htmlbody("There are ".($ind+1)." commands for replay".eol());

		if ($ind!=-1){
			$timestart=time(); // ok, start timers now
			$timenow = $timestart;
			$timeend = $timestart +	$seconds;


			while($timenow <= $timeend){
				$transtart = $trancount;

				for ($i=0; $i<=$ind; $i++)
				{

					execute($fullurl[$i])
					or htmlbody("Can not open $fullurl[$i] url".eol());

					$trancount +=1;
				}
				$timenow = time();
			}

			htmlbody ("...Executed $trancount transactions in $seconds seconds".eol());
		}
		/* Free resultset */
		db_free_result($result);
		echo htmlfooter();
}


function simulate()
{

	function slink($s,$t)
	{ $gw2=$GLOBALS['gw'];
	return  alink($s.$gw2,$t);}
	readconfig();
	$rec= cleanreq('recording');
	$gw = "?gateway=".cleanreq('gateway');
	if ($rec!="") $gw.="&recording=".$rec;
	$GLOBALS['gw'] = $gw;
    $gwurl = getgatewayinfo(cleanreq('gateway'),$description,$lastheard);
    if ($gwurl=="") htmltop("invalid medcommons gateway: ".cleanreq('gateway'),SIMULATOR_STYLE);
	else {
	
	htmltop("Simulate ".cleanreq('gateway'),SIMULATOR_STYLE);

	htmlbody(
	hlevel(2,"Simulate web service calls from gateway ".cleanreq('gateway')." $description").
	slink("../xmlws/wsgetgatewayinfo.php","Get Config").
	" of purple box".eol().
	slink("../simulator/suigetcommandsince.php","Get Commands").
	" to process".eol().
	slink("../utils/testxmlwebservices.php","Test ").
			"&nbsp;XML Web Service Calls Against Pinkbox".eol().eol().

	hlevel(3,"Orders").
	slink("../simulator/suiinsertorder.php","Insert Order").eol().
	slink("../simulator/suiupdateorderstatus.php","Update Order Status").eol().
	slink("../simulator/suigetorderinfo.php","Get Order Information").eol().

	hlevel(3,"Order-Data-Series").
	slink("../simulator/suiinsertorderseries.php",
	"Insert Series into Order").
	" - should confirm order exists before inserting,
                 should also confirm uniqueness of series".eol().
	slink("../simulator/suigetorderserieslinks.php","Get Order Series Links").eol().

	hlevel(3,"Routing-Queues").
	slink("../simulator/suiinsertroutinginfo.php",
	"Insert Element Into Routing Queue").eol().
	slink("../simulator/suiupdateroutinginfo.php",
	"Update Routing Queue Element") .eol().
	slink("../simulator/suigetroutingqueueitems.php",
	"Get Routing Queue Items").
	"for processing".eol().
	
	
	hlevel(3,"XDS-Registry").
	slink("../simulator/suiinsertxdsinfo.php",
	"Insert Element Into XDS Registry").eol().
	slink("../simulator/suiupdatexdsstatus.php",
	"Update XDS Registry Info") .eol().
	slink("../simulator/suigetxdsinfo.php",
	"Get XDS Registry Info").
	"for processing".eol().
	
	
	
	
	//		fmemoask().
	eol());
	}
	echo htmlfooter();
}


(cleanreq('replay')=='replay')?replay():simulate();


?>

