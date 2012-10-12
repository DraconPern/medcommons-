<?php

function display_new_pw($err)
{
$wbheader = wbheader('newpw',"New Password");

$x = <<<XXX
$wbheader
<div class=Section1>

<p class=MsoNormal><span style='font-family:Verdana'>&nbsp;</span></p>

<p class=MsoNormal><span style='font-family:Verdana'>&nbsp;</span></p>

<p class=MsoNormal><span style='font-family:Verdana'>&nbsp;</span></p>

<p class=MsoNormal><span style='font-family:Verdana'>&nbsp;</span></p>


<p class=MsoNormal><b><span style='font-size:10.0pt;font-family:Arial'>Lost
Password? Enter your userid and email address and we will send you a new
password</span></b></p>

<form action="../whitebox/newpwhandler.php" method=Get
enctype="application/x-www-form-urlencoded">

<p class=MsoNormal><span style='font-size:10.0pt;font-family:Arial'>&nbsp; &nbsp; &nbsp; User
id:&nbsp;  <INPUT TYPE="TEXT" NAME="userid">$err</span></p>

<p class=MsoNormal><span style='font-size:10.0pt;font-family:Arial'>&nbsp;</span></p>

<p class=MsoNormal><span style='font-size:10.0pt;font-family:Arial'>&nbsp; &nbsp; &nbsp; Email:
&nbsp;&nbsp;&nbsp; <INPUT TYPE="TEXT" NAME="email"></span></p>

<p class=MsoNormal><span style='font-size:10.0pt;font-family:Arial'>&nbsp;</span></p>

<p class=MsoNormal><span style='font-size:10.0pt;font-family:Arial'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<INPUT TYPE="SUBMIT" ACTION="../whitebox/newpwhandler.php" VALUE="New Password" NAME="Submit"
ACTION="../whitebox/newpwhandler.php"></span></p>

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