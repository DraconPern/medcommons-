<?PHP

//mysql version of dblib.inc
// wld
// 17 Sept 04
// presumably we can rewrite this for whatever database we like
//

function sqt($s)
{	
	if ($GLOBALS['sqltrace']==true)
			 $GLOBALS['sqltracebuffer'].="<br>------Tracing $s:<br>";
			
}

function sqltraceon()
{	
	if ($_SESSION['sqltrace']==1){
		    echo ("SQL TRACE ON");
			$GLOBALS['sqltrace']=1;
	 		$GLOBALS['sqltracebuffer']="";} 
	else $GLOBALS['sqltrace']=0;
}
function sqltracedump()
{
if ($GLOBALS['sqltrace']==1)
	echo $GLOBALS['sqltracebuffer'];
	$GLOBALS['sqltracebuffer']="";
}


function db_num_rows($x){
return mysql_num_rows($x);
}
function db_num_fields($x){
return mysql_num_fields($x);
}
function db_query($x,$l=''){
if ($GLOBALS['sqltrace']==true)	
$GLOBALS['sqltracebuffer'].="<br>".$x;
return mysql_query($x,$GLOBALS['DB_Link']);
}
function db_fetch_array($x){
return mysql_fetch_array($x);
}
function db_fetch_array_assoc($x){
return mysql_fetch_array($x,MYSQL_ASSOC);
}
function db_fetch_array_index($idx){
return mysql_fetch_array($idx,MYSQL_NUM);
}
function db_free_result($x){
return mysql_free_result($x);
}
function db_field_name($x,$i){
return mysql_field_name($x,$i);
}
function db_affected_rows(){
return mysql_affected_rows();
}
function db_error(){
return mysql_error();
}
function db_connect($a,$b,$c){
return mysql_pconnect($a,$b,$c);
}
function db_close($l){
return true;
}
function db_select($d,$l){
return mysql_select_db($d,$l);
}

?>