<?PHP
function out($depth,$s1,$s2='')
{ 
	$pre=''; 
    for ($i=0; $i<$depth; $i++) {$pre.="    ";}
	echo $pre.$s1." ".$s2."\r\n";
	return $s2; //will store value into simple variable
}
function emit($x) {$GLOBALS['emt']=$GLOBALS['emt'].$x."\r\n"; }
//
// main program
//
$GLOBALS['emt']='';
$head = file_get_contents ("templates/head.php.inc",true);
//echo "head ".$head;

$tail = file_get_contents ("templates/tail.php.inc");
$mtoppart = file_get_contents("templates/mhead.php.inc");
$mbottompart = file_get_contents("templates/mtail.php.inc");
	
$cfile = "templates/notifications.xml";
$xml = simplexml_load_file($cfile);


emit($head);

foreach ($xml->notification as $notification) { 
  	echo " Compiling Notification ".$notification['name']." ".$notification->email."<br>"; 
  	$meat = file_get_contents("templates/".$notification->email);
  	emit("function mail".$notification['name']);
  	emit($mtoppart);
  	emit($meat);
    emit($mbottompart);
  
  };
  
emit ($tail);
file_put_contents("generated-code/gencode.php.inc",$GLOBALS['emt']);
?>
  
