<?php

function run_test($appliance,$count)
{
$myserver = $_SERVER ['SERVER_NAME'];
$time = time();
echo "<h4>$time $testname Started test-  Running $count cycles on $appliance from $myserver</h4>";

$t1 = microtime(true);	
for ($j=0; $j<$count; $j++)
{

	$mcid = one_call($appliance,'','','',"010101","M",'test.jpg');
	
}

$t2 = microtime(true);
$delta2 = round($t2-$t1,3);
$persecond = $delta2/$count;
$etime =round((1/$persecond),3);
$time = time();

echo "<h4>$time $testname Finished $count test cycles on $appliance from $myserver in $delta2 seconds</h4>";
echo "<h4>The rate per second is $etime and the time to create each account is $persecond seconds</h4>";

}
?>