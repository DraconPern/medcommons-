<?php
// (c) 2004 MedCommons, Inc.
// wld 8/30/04
// suiinsertroutinginfo.php
require_once ("../lib/htmlsubs.inc");

function showgatewaystatus()
{
	$gw = cleanreq('Gateway');
	$page_title = "Gateway ". $gw. " status:";

	htmltop($page_title,
				PINKBOX_STYLE);
       

$db = $GLOBALS['DB_Database'];//can't get until after header


//htmlbody ( $query;
$query=db_prepare_gateways_select($gw);
$result = db_query($query,$GLOBALS['DB_Link']) ;//or die("Query failed : " . db_error());
$status = db_error($GLOBALS['DB_Link']);/////

if ($status!="") die($status);

$number_cols = db_num_fields($result);
/* Printing results in HTML */
htmlbody ( "<table  >\n");
//col headers
htmlbody ( "<tr>\n");
for ($i=0; $i<$number_cols;++$i){
	$colname = db_field_name($result,$i);
	{htmlbody ( "<th>".$colname."</th>\n"); }
	
}

htmlbody ( "</tr>\n");

while ($line = db_fetch_array($result)) {
   htmlbody ( "\t<tr>\n");
   foreach ($line as $col_value) {
       htmlbody ( "\t\t<td>$col_value</td>\n");
   }
   htmlbody ( "\t</tr>\n");
}
htmlbody ( "</table>\n");

/* Free resultset */
db_free_result($result);

//db_close($link);
htmlbody ( "\n");

// step 2, show information from virtrad table


$query = db_prepare_virtrad_select($gw);
//htmlbody ( $query;
$result = db_query($query,$GLOBALS['DB_Link']) ;//or die("Query failed : " . db_error());
$status = db_error($GLOBALS['DB_Link']);/////

if ($status!="") die($status);

$number_cols = db_num_fields($result);
/* Printing results in HTML */
htmlbody ( "<table  >\n");
//col headers
htmlbody ( "<tr>\n");
for ($i=0; $i<$number_cols;++$i){
	$colname = db_field_name($result,$i);
	{htmlbody ( "<th>".$colname."</th>\n"); }
}

htmlbody ( "</tr>\n");

while ($line = db_fetch_array_assoc($result)) {
   htmlbody ( "\t<tr>\n");
   foreach ($line as $col_value) {
       htmlbody ( "\t\t<td>$col_value</td>\n");
   }
   htmlbody ( "\t</tr>\n");
}
htmlbody ( "</table>\n");

/* Free resultset */
db_free_result($result);

//db_close($link2);

	echo  htmlfooter();
}
showgatewaystatus();
?> 


