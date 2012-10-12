<?php
require_once("../lib/config.inc");
require_once("../whitebox/wbsubs.inc");
require_once ("../whitebox/hipaasubs.inc");

session_start();

$wbh=wbheader('hipaalog',"Display HIPAA Log");

$month = cleanreq('month');
if ($month=="") $month=0;
$year = cleanreq('year');
if ($year == "") $year = 0;
 hipaa_title ($month,$year);
$hipaastuff = hipaa_log(999,$count,$month,$year);
if ($count==0) $hipaastuff = "There are no HIPAA Log entries for the selected time period";
$export = zlink("export","../whitebox/hipaaexport.php?year=$year&month=$month");

$x=<<<XXX
$wbh
<br>

<span style="font-weight: bold;">All Account Activity (HIPAA) Log  $export</span> 

<br>

<br>
$hipaastuff

</body>

</html>
XXX;

echo $x;

?>
