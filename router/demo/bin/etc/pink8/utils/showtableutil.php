<?php
// (c) 2004 MedCommons, Inc.
// wld 8/30/04
// suiinsertroutinginfo.php
require_once ("../lib/htmlsubs.inc");

function showtableutil()
{

$table = cleanreq('table');
$filter = cleanreq('filter');//input fields need cleansing
$page_title = "Show Table ".$table;
if ($filter!="")$page_title.=" Filtered by ".$filter;

	htmltop($page_title,
				PINKBOX_STYLE);


$db = $GLOBALS['DB_Database'];//can't get until after header



$query = db_prepare_select_general($table,$filter);

$result = db_query($query,$GLOBALS['DB_Link']) ;//or die("Query failed : " . db_error());
$status = db_error($GLOBALS['DB_Link']);/////

if ($status!="") die($status);

$number_cols = db_num_fields($result);
/* Printing results in HTML */
htmlbody ( "<table class=.tabhead>\n");
//col headers
htmlbody ( "<tr>\n");
for ($i=0; $i<$number_cols;++$i){
	$colname = db_field_name($result,$i);
	//htmlbody ( $colname."\n\r";
	{htmlbody ( '<th >'.$colname.'</th>'); }
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
echo htmlfooter();
}
showtableutil();
?>