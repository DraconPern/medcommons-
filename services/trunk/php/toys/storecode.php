<?php

// handle post from VxUploadCode.php

$type=$_REQUEST['type'];
$name=$_REQUEST['name'];
$mckey=$_REQUEST['mckey'];
$code=$_REQUEST['code'];
$sha1=false;$accid=false;$email=false; //prepare for the worst
$mckeyparts = explode('|',base64_decode($mckey)); 
if (isset($mckeyparts[0])) $sha1 = $mckeyparts[0];
if (isset($mckeyparts[1])) $accid = $mckeyparts[1];
if (isset($mckeyparts[2])) $email = $mckeyparts[2];
if ($sha1===false || $accid===false || $email===false) die ('This mckey appears invalid, try again');

$time =  gmstrftime("%b %d %Y %H:%M:%S");
;
$checksum = sha1($code);
$pre = "<?php
// module autogenerated by VxUpload on $time 
// uploader: $accid $email
\$checksum = '$checksum'; //sha1 of all lines between //** and //**
//**
";
$post = "
//** end of user supplied function
?>";

file_put_contents('/var/www/php/funcs/'.$accid.'-'.$name.'-vxm.ccr.php',$pre.$code.$post);

echo "Your function was stored and can be viewed <a target='_new' href=Vx.php?t=$name&a=ARGA|ARGB|ARGC|ARGD|ARGE|ARGF|ARGG|ARGH|ARGI|ARGJ&c=code&mckey=$mckey >here</a>";
echo "<p>you can test it with a standard set of arguments and &c=xml
 <a target='_new' href=Vx.php?t=$name&a=ARGA|ARGB|ARGC|ARGD|ARGE|ARGF|ARGG|ARGH|ARGI|ARGJ&c=xml&mckey=$mckey >here</a>";

echo "<p>you can test it with a standard set of arguments and &c=do
 <a target='_new' href=Vx.php?t=$name&a=ARGA|ARGB|ARGC|ARGD|ARGE|ARGF|ARGG|ARGH|ARGI|ARGJ&c=do&mckey=$mckey >here</a>";

echo "<p>you can build a form to easily customize and submit these messages with &c=form
 <a target='_new' href=Vx.php?t=$name&a=ARGA|ARGB|ARGC|ARGD|ARGE|ARGF|ARGG|ARGH|ARGI|ARGJ&c=form&mckey=$mckey >here</a>";
echo "<p>You can also save or bookmark these links in order to serve as templates for other calls to Vx";
exit;
?>