<?php

function run_test($testname,$count,$appliance)
{
$myserver = $_SERVER ['SERVER_NAME'];
$starttime = time();

$t1 = microtime(true);	
for ($j=0; $j<$count; $j++)
{

	$mcid = one_call($appliance,'','','',"010101","M",'test.jpg');
	
}

$t2 = microtime(true);
$delta2 = round($t2-$t1,3);
$persecond = $delta2/$count;
$etime =round((1/$persecond),3);
$stoptime = time();

echo "<h4>$starttime $stoptime $testname Finished $count test cycles on $appliance from $myserver in $delta2 seconds, rps=$etime, per test=$persecond</h4>";


}
?>