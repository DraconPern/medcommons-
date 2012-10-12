<?php

class tabledumper {
// common code for dumping tables
//
function table_header ()
{ echo "table_header base should be overridden"; }

function table_row ($l)
{ echo "table_row $l base should be overridden"; }
function table_query($idpre,$limit)
{ echo "table_query $idpre $limit base should be overridden"; }
function blurb ($link,$tooltip,$text)
{ return <<<XXX
<a href="$link" onmouseover="this.T_WIDTH=200;this.T_FONTCOLOR='#003399'; return escape('$tooltip')">$text</a>
XXX;
}
function table_dump ($t)
{
	//get standard parameters
	$limit = $_REQUEST['limit'];
	if ($limit=="") $limit=30;
	$filter = $_REQUEST['filter'];

	//connect to database

	mysql_connect($GLOBALS['DB_Connection'],
	$GLOBALS['DB_User'],
	$GLOBALS['DB_Password']
	) or xmlend ("can not connect to mysql");

	$db = $GLOBALS['DB_Database'];
	mysql_select_db($db) or die ("can not connect to database $db");



	//build generic header appending custom part
	$d = gmstrftime("%b %d %Y %H:%M:%S")." GMT";
	if ($filter!="") $f = "Filter $filter"; else $f=''; // set up filter header
	$query = $this->table_query($filter,$limit);

	$header = <<<XXX
<html><head><title>$t on MedCommons $network Network</title></head>
<body>
<table border=0>
<td><img src='../../images/MEDcommons_logo_246x50.gif' width=246 height=50 alt='medcommons, inc.'></td>
<td><large><b>$t on MedCommons $network Network at $d $f</b></large><small><br>$query</small></td></tr>
</table>
<P>
<table border=1>
XXX;
	$header.=$this->table_header();

	//build custom query
	$result = mysql_query ($query) or die ("can not query $t - ".mysql_error());
	$count = 0;
	if ($result!="") {
		echo $header;
		// the while statement is generic
		while ($l = mysql_fetch_array($result,MYSQL_ASSOC)) {
			echo $this->table_row ($l);
			$count++;
		}

	}
	mysql_free_result($result);

	mysql_close();

	$x=<<<xxx
</table><script language="JavaScript" type="text/javascript" src="wz_tooltip.js"></script></body></html>
xxx;
	return $x;
}

}
?>