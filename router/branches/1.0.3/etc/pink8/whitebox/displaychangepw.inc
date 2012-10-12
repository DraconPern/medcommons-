<?php

function display_change_pw($err)
{
$wbheader = wbheader('changepw',"Change Password");

$x = <<<XXX
$wbheader
<div class=Section1>

<p class=MsoNormal><span style='font-family:Verdana'>&nbsp;</span></p>

<p class=MsoNormal><span style='font-family:Verdana'>&nbsp;</span></p>

<p class=MsoNormal><span style='font-family:Verdana'>&nbsp;</span></p>

<p class=MsoNormal><span style='font-family:Verdana'>&nbsp;</span></p>

<form action="../whitebox/changepwhandler.php" method=Get
enctype="application/x-www-form-urlencoded">

<p class=MsoNormal><span style='font-size:10.0pt;font-family:Arial'>&nbsp; &nbsp; &nbsp;current
password&nbsp;</span><span style='font-family:Verdana'> <INPUT TYPE="PASSWORD" NAME="oldpass">$err</span></p>

<p class=MsoNormal><span style='font-family:Verdana'>&nbsp;</span></p>

<p class=MsoNormal><span style='font-size:10.0pt;font-family:Arial'>&nbsp; &nbsp; &nbsp;new
password&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span><span style='font-family:Verdana'> <INPUT TYPE="PASSWORD" NAME="newpass1"></span></p>

<p class=MsoNormal><span style='font-family:Verdana'>&nbsp;</span></p>

<p class=MsoNormal><span style='font-size:10.0pt;font-family:Arial'>&nbsp; &nbsp; &nbsp;confirm
password&nbsp;&nbsp;</span><span style='font-family:Verdana'><INPUT TYPE="PASSWORD" NAME="newpass2"></span></p>

<p class=MsoNormal><span style='font-family:Verdana'>&nbsp;</span></p>

<p class=MsoNormal><span style='font-family:Verdana'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<INPUT TYPE="SUBMIT" ACTION="../whitebox/changepwhandler.php" VALUE="Change Password" NAME="Submit"></span></p>

</form>

<p class=MsoNormal><span style='font-family:Verdana'>&nbsp;</span></p>

<p class=MsoNormal><span style='font-family:Verdana'>&nbsp;</span></p>

<p class=MsoNormal><span style='font-family:Verdana'>&nbsp;</span></p>

<p class=MsoNormal><span style='font-family:Verdana'>&nbsp;</span></p>

<p class=MsoNormal><span style='font-family:Verdana'>&nbsp;</span></p>

<p class=MsoNormal><span style='font-family:Verdana'>&nbsp;</span></p>

<p class=MsoNormal><span style='font-family:Verdana'>&nbsp;</span></p>

</div>

</body>

</html>
XXX;
return $x;
}
?>