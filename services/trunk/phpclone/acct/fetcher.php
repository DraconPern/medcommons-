<?php

function fetcher ($url)
{
	$stuff=@file_get_contents($url);
	if ($stuff===false)
	echo("<br/>Could not open $url<br/>");
	else 
	{
		$len = strlen($stuff);
		if ($len<100)

	echo ("<br/>Small file $url size ".$len);
	else {
		$pos1 = strpos ($stuff,'<title>');
		$pos2 = strpos ($stuff,'</title>');
		$title = substr($stuff, $pos1+7,$pos2-$pos1-7);
		echo ("<br/>Processing $url.... size $len title $title");
		
	
		}
	}
}

$base = $_GET['base'];
$limit = $_GET['limit'];
for ($i=0; $i<$limit; $i++)
fetcher ($base.$i);
?>