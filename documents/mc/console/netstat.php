<?php
require_once  "../../dbparamsmcback.inc.php";

// show status of the network

$db=$GLOBALS['DB_Database'];
$a = $GLOBALS['DB_Connection'];
$r = $GLOBALS['Default_Repository'];
$srvname = $_SERVER['SERVER_NAME'];

$srva = $_SERVER['SERVER_ADDR'];
$srvp = $_SERVER['SERVER_PORT'];
$gmt = gmstrftime("%b %d %Y %H:%M:%S")." GMT";
$uri = htmlspecialchars($_SERVER ['REQUEST_URI']);

//main
// get a select list of all gatways
$x=<<<xxx
<html><head><title>MedCommons Network Status Display</title><meta http-equiv="refresh" content="60"></head><body>
	<body><img src='../../images/MEDcommons_logo_246x50.gif' width=246 height=50 alt='medcommons, inc.'>
<h4>MedCommons Network Status at $gmt</h4>
<p><small>The polling database engine is $db on $srvname ($srva:$srvp)</small><br>
</p>

xxx;

 
	mysql_connect($GLOBALS['DB_Connection'],
			$GLOBALS['DB_User'],
			$GLOBALS['DB_Password']
			) or die ("can not connect to mysql");
				$db = $GLOBALS['DB_Database'];
	mysql_select_db($db) or die ("can not connect to database $db");
 
	
 	$query = "SELECT * from centralprobes";

 	$result = mysql_query ($query) or die("can not query table centralprobes - ".mysql_error());
 	$count=0;
	if ($result=="") {echo "?no central systems defined?"; exit;}
	
	echo $x;// f here, we have a good database
	echo "<table border=2><tr><th><b>Network<b></th><th><b>Status<b></th><th><b>Version<b></th><th><b>Description<b></th><th><b>#hipaa<b></th><th><b>#users<b></th>
	<th><b>fax<b></th><th><b>email<b></th><th><b>pay<b></th>
	</tr>";
	while ($l = mysql_fetch_array($result,MYSQL_ASSOC)) {
		$nickname = $l['nickname'];
		$ctprot = $l['ctprot'];	
		$cthost = $l['cthost'];
		$ctport= $l['ctport'];
		$ctfile = $l['ctfile'];
		$description = $l['description'];
		$status = $l['summarystatus'];
		$ss=substr($status,0,2);
		
		$hc =$l['hipaacount'];
			$htc =$l['hipaatracecount'];
		$uc = $l['usercount'];
		$opaycount = $l['opaycount'];
		$opayerrs = $l['opayerrs'];
		$ofaxcount = $l['ofaxcount'];
		$ofaxerrs = $l['ofaxerrs'];
		$oemailcount = $l['oemailcount'];
		$oemailerrs = $l['oemailerrs'];
		
		$ipaddr = $l['ipaddr'];
		$dbconnection = $l['dbconnection'];
		$dbdatabase = $l['dbdatabase'];
				$swversion = $l['swversion'];
						$swrevision = $l['swrevision'];
	
		$hp = $ctprot."://".$cthost.":".$ctport;//wld now passed in from database
		$hd = $hp."/mc/console/hipaadump.php?network=$nickname&table=hipaa&limit=30";
		$htd = $hp."/mc/console/hipaadump.php?network=$nickname&table=hipaa_trace&limit=30";

		$ud = $hp."/mc/console/usersdump.php?network=$nickname";
		
		if ($ss=="ER") $nickname="<FONT COLOR=#ff0000>".$nickname."</FONT>";
		if ($ss=="ER") $ss="<FONT COLOR=#ff0000>".$ss."</FONT>";

		$ct = $hp.$ctfile;
		$count++;
		$xx=<<<XXX
<tr>
<td><a href='$hp' onmouseover="this.T_WIDTH=200;this.T_FONTCOLOR='#003399';return escape('$cthost $ipaddr $dbconnection $dbdatabase')" target='_NEW'>$nickname</a></td>
<td><a href='$ct' target='_NEW' onmouseover="this.T_WIDTH=200;this.T_FONTCOLOR='#003399';return escape('$status')">$ss</a></td>
		<td>$swversion%$swrevision</td>
		<td>$description</td>
		<td><a href='$hd' target='_NEW'>$hc</a>/<a href='$htd' target='_NEW'>$htc</a></td>
		<td><a href='$ud' target='_NEW'>$uc</td>
		<td>$ofaxcount/$ofaxerrs</td>
		<td>$oemailcount/$oemailerrs</td>
		<td>$opaycount/$opayerrs</td>
		
		
		</tr>
XXX;
	echo $xx;

        }
	mysql_free_result($result);
	echo "</table>"; 	 
 	$query = "SELECT * from gwprobes";

 	$result = mysql_query ($query) or die("can not query table gateways - ".mysql_error());
 	$count=0;
	if ($result=="") {echo "?no gateways defined?"; exit;}
	
	echo "<table border=2><tr><td><b>Gateway<b></td><td><b>Status<b></td><td><b>Network</b></td><td><b>Version</b></td><td><b>Description<b></td></tr>";
	while ($l = mysql_fetch_array($result,MYSQL_ASSOC)) {
		$gwprot = $l['gwprot'];	
		$gwhost = $l['gwhost'];
		$gwport= $l['gwport'];
		$gwfile = $l['gwfile'];
		$nickname = $l['nickname'];
		$description = $l['description'];
		$central= $l['central'];
		$status=$l['status'];
		$egroup = $l['egroup'];
						$swversion = $l['swversion'];
						$swrevision = $l['swrevision'];
		$tr ="https://$gwhost:8443/router/tracking.jsp?tracking=999999999999";
		$gw = $gwprot."//".$gwhost.":".$gwport.$gwfile;//wld now passed in from database

		$count++;
		$ss=substr($status,0,2);
if ($ss=="ER") $nickname="<FONT COLOR=#ff0000>".$nickname."</FONT>";
		
if ($ss=="ER") $ss="<FONT COLOR=#ff0000>".$ss."</FONT>";

		$xx=<<<XXX
<tr><td><a href='$tr' onmouseover="this.T_WIDTH=200;this.T_FONTCOLOR='#003399';return escape('$gwhost')" target='_NEW'>$nickname</a></td>
<td><a href='$gw' onmouseover="this.T_WIDTH=200;this.T_FONTCOLOR='#003399';return escape('$status')" target='_NEW'>$ss</a></td>
<td>$central</td>
		<td>$swversion%$swrevision</td>

<td>$description</td></tr>
XXX;

	      echo $xx;	
        }
	mysql_free_result($result);
	echo "</table>";
	
	//
	
/*	$query = "SELECT * from alerted";

 	$result = mysql_query ($query) or die("can not query table alerted - ".mysql_error());
 	$count=0;
	if ($result=="") {echo "?no parties to alert"; exit;}
	echo "<br><small>the parties listed below will receive alerts</small>";
	echo "<table border=2><tr><td><b>Email<b></td><td><b>Frequency (mins)<b></td></tr>";
	while ($l = mysql_fetch_array($result,MYSQL_ASSOC)) {
			
		$email = $l['email'];
		$last = $l['last'];
		$frequency = $l['frequency'];

		$count++;
		echo "<tr><td>$email</td><td>$frequency</td></tr>";

        }*/
	//mysql_free_result($result);
	mysql_close();
$x=<<<xxx
</table><script language="JavaScript" type="text/javascript" src="wz_tooltip.js"></script></body></html>
xxx;
echo $x;
exit;

?>