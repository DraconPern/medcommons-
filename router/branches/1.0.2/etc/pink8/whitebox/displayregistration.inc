<?php

function display_registration_page(
   $tracking, //comes from URL args
   $userid,   
   $name,
   $title, 
   $addr1, 
   $addr2, 
   $city, 
   $state,
   $zip,
   $email,
   $dob, 
   $sex, 
   $phone, 
   $company, 
   $healthcareSpecialist, 
   $stateLicense,

   $userid_error,   
   $name_error,
   $title_error, 
   $addr1_error, 
   $addr2_error, 
   $city_error, 
   $state_error,
   $zip_error,
   $email_error,
   $dob_error, 
   $sex_error, 
   $phone_error, 
   $company_error, 
   $healthcareSpecialist_error, 
   $stateLicense_error
)		
{
	if ($userid=="") $wbh = wbheader('register',"New User Registration");
	else $wbh = wbheader('register',"Change User Profile Parameters");

$msgenform = <<<XXX
$wbh
<h2>Edit User Profile</h2>
<form action=../whitebox/registrationhandler.php method=Get 
                enctype="application/x-www-form-urlencoded"> 

<input name = "tracking" value = "$tracking" type = "HIDDEN">
<div class=Section1>

<div align=center>

<table class=MsoTableGrid border=1 cellspacing=0 cellpadding=0
 style='border-collapse:collapse;border:none'>
 <tr>
  <td width=179 valign=top style='width:134.6pt;border:solid windowtext 1.0pt;
  padding:0in 5.4pt 0in 5.4pt'>
  <p class=MsoNormal><span style='font-family:Arial'>Userid</span></p>
  </td>
  <td width=512 valign=top style='width:600.3pt;border:solid windowtext 1.0pt;
  border-left:none;padding:0in 5.4pt 0in 5.4pt'>
  <p class=MsoNormal><span style='font-family:Arial'><INPUT TYPE="TEXT" SIZE="71" value = "$userid" NAME="userid">$userid_error</span></p>
  </td>
 </tr>
 <tr>
  <td width=179 valign=top style='width:134.6pt;border:solid windowtext 1.0pt;
  border-top:none;padding:0in 5.4pt 0in 5.4pt'>
  <p class=MsoNormal><span style='font-family:Arial'>Name</span></p>
  </td>
  <td width=512 valign=top style='width:600.3pt;border-top:none;border-left:
  none;border-bottom:solid windowtext 1.0pt;border-right:solid windowtext 1.0pt;
  padding:0in 5.4pt 0in 5.4pt'>
  <p class=MsoNormal><span style='font-family:Arial'><INPUT TYPE="TEXT" SIZE="71" value = "$name" NAME="name">$name_error</span></p>
  </td>
 </tr>
 <tr>
  <td width=179 valign=top style='width:134.6pt;border:solid windowtext 1.0pt;
  border-top:none;padding:0in 5.4pt 0in 5.4pt'>
  <p class=MsoNormal><span style='font-family:Arial'>Title</span></p>
  </td>
  <td width=512 valign=top style='width:600.3pt;border-top:none;border-left:
  none;border-bottom:solid windowtext 1.0pt;border-right:solid windowtext 1.0pt;
  padding:0in 5.4pt 0in 5.4pt'>
  <p class=MsoNormal><span style='font-family:Arial'><INPUT TYPE="TEXT" SIZE="71" value = "$title" NAME="title">$title_error</span></p>
  </td>
 </tr>
 <tr>
  <td width=179 valign=top style='width:134.6pt;border:solid windowtext 1.0pt;
  border-top:none;padding:0in 5.4pt 0in 5.4pt'>
  <p class=MsoNormal><span style='font-family:Arial'>Addr1</span></p>
  </td>
  <td width=512 valign=top style='width:600.3pt;border-top:none;border-left:
  none;border-bottom:solid windowtext 1.0pt;border-right:solid windowtext 1.0pt;
  padding:0in 5.4pt 0in 5.4pt'>
  <p class=MsoNormal><span style='font-family:Arial'><INPUT TYPE="TEXT" SIZE="71" value ="$addr1" "$NAME="addr1">$addr1_error</span></p>
  </td>
 </tr>
 <tr>
  <td width=179 valign=top style='width:134.6pt;border:solid windowtext 1.0pt;
  border-top:none;padding:0in 5.4pt 0in 5.4pt'>
  <p class=MsoNormal><span style='font-family:Arial'>Addr2</span></p>
  </td>
  <td width=512 valign=top style='width:600.3pt;border-top:none;border-left:
  none;border-bottom:solid windowtext 1.0pt;border-right:solid windowtext 1.0pt;
  padding:0in 5.4pt 0in 5.4pt'>
  <p class=MsoNormal><span style='font-family:Arial'><INPUT TYPE="TEXT" SIZE="71" value = "$addr2" NAME="addr2">$addr2_error</span></p>
  </td>
 </tr>
 <tr>
  <td width=179 valign=top style='width:134.6pt;border:solid windowtext 1.0pt;
  border-top:none;padding:0in 5.4pt 0in 5.4pt'>
  <p class=MsoNormal><span style='font-family:Arial'>City</span></p>
  </td>
  <td width=512 valign=top style='width:600.3pt;border-top:none;border-left:
  none;border-bottom:solid windowtext 1.0pt;border-right:solid windowtext 1.0pt;
  padding:0in 5.4pt 0in 5.4pt'>
  <p class=MsoNormal><span style='font-family:Arial'><INPUT TYPE="TEXT" SIZE="71" value = "$city" NAME="city">$city_error</span></p>
  </td>
 </tr>
 <tr>
  <td width=179 valign=top style='width:134.6pt;border:solid windowtext 1.0pt;
  border-top:none;padding:0in 5.4pt 0in 5.4pt'>
  <p class=MsoNormal><span style='font-family:Arial'>State</span></p>
  </td>
  <td width=512 valign=top style='width:600.3pt;border-top:none;border-left:
  none;border-bottom:solid windowtext 1.0pt;border-right:solid windowtext 1.0pt;
  padding:0in 5.4pt 0in 5.4pt'>
  <p class=MsoNormal><span style='font-family:Arial'><INPUT TYPE="TEXT" SIZE="71" value = "$state" NAME="state">$state_error</span></p>
  </td>
 </tr>
 <tr>
  <td width=179 valign=top style='width:134.6pt;border:solid windowtext 1.0pt;
  border-top:none;padding:0in 5.4pt 0in 5.4pt'>
  <p class=MsoNormal><span style='font-family:Arial'>Zip</span></p>
  </td>
  <td width=512 valign=top style='width:600.3pt;border-top:none;border-left:
  none;border-bottom:solid windowtext 1.0pt;border-right:solid windowtext 1.0pt;
  padding:0in 5.4pt 0in 5.4pt'>
  <p class=MsoNormal><span style='font-family:Arial'><INPUT TYPE="TEXT" SIZE="71" value = "$zip" NAME="zip">$zip_error</span></p>
  </td>
 </tr>
 <tr>
  <td width=179 valign=top style='width:134.6pt;border:solid windowtext 1.0pt;
  border-top:none;padding:0in 5.4pt 0in 5.4pt'>
  <p class=MsoNormal><span style='font-family:Arial'>Phone</span></p>
  </td>
  <td width=512 valign=top style='width:600.3pt;border-top:none;border-left:
  none;border-bottom:solid windowtext 1.0pt;border-right:solid windowtext 1.0pt;
  padding:0in 5.4pt 0in 5.4pt'>
  <p class=MsoNormal><span style='font-family:Arial'><INPUT TYPE="TEXT" SIZE="71" value = "$phone" NAME="phone">$phone_error</span></p>
  </td>
 </tr>
 <tr>
  <td width=179 valign=top style='width:134.6pt;border:solid windowtext 1.0pt;
  border-top:none;padding:0in 5.4pt 0in 5.4pt'>
  <p class=MsoNormal><span style='font-family:Arial'>Email</span></p>
  </td>
  <td width=512 valign=top style='width:600.3pt;border-top:none;border-left:
  none;border-bottom:solid windowtext 1.0pt;border-right:solid windowtext 1.0pt;
  padding:0in 5.4pt 0in 5.4pt'>
  <p class=MsoNormal><span style='font-family:Arial'><INPUT TYPE="TEXT" SIZE="71" value = "$email" NAME="email">$email_error</span></p>
  </td>
 </tr>
 <tr>
  <td width=179 valign=top style='width:134.6pt;border:solid windowtext 1.0pt;
  border-top:none;padding:0in 5.4pt 0in 5.4pt'>
  <p class=MsoNormal><span style='font-family:Arial'>DOB</span></p>
  </td>
  <td width=512 valign=top style='width:600.3pt;border-top:none;border-left:
  none;border-bottom:solid windowtext 1.0pt;border-right:solid windowtext 1.0pt;
  padding:0in 5.4pt 0in 5.4pt'>
  <p class=MsoNormal><span style='font-family:Arial'><INPUT TYPE="TEXT" SIZE="71" value = "$dob" NAME="dob">$dob_error</span></p>
  </td>
 </tr>
 <tr>
  <td width=179 valign=top style='width:134.6pt;border:solid windowtext 1.0pt;
  border-top:none;padding:0in 5.4pt 0in 5.4pt'>
  <p class=MsoNormal><span style='font-family:Arial'>Sex</span></p>
  </td>
  <td width=512 valign=top style='width:600.3pt;border-top:none;border-left:
  none;border-bottom:solid windowtext 1.0pt;border-right:solid windowtext 1.0pt;
  padding:0in 5.4pt 0in 5.4pt'>
  <p class=MsoNormal><span style='font-family:Arial'><INPUT TYPE="TEXT" SIZE="71" value = "$sex"NAME="sex">$sex_error</span></p>
  </td>
 </tr>
 <tr>
  <td width=179 valign=top style='width:134.6pt;border:solid windowtext 1.0pt;
  border-top:none;padding:0in 5.4pt 0in 5.4pt'>
  <p class=MsoNormal><span style='font-family:Arial'>Company/affiliation</span></p>
  </td>
  <td width=512 valign=top style='width:600.3pt;border-top:none;border-left:
  none;border-bottom:solid windowtext 1.0pt;border-right:solid windowtext 1.0pt;
  padding:0in 5.4pt 0in 5.4pt'>
  <p class=MsoNormal><span style='font-family:Arial'><INPUT TYPE="TEXT" SIZE="71" value = "$company" NAME="company">$company_error</span></p>
  </td>
 </tr>
 <tr>
  <td width=179 valign=top style='width:134.6pt;border:solid windowtext 1.0pt;
  border-top:none;padding:0in 5.4pt 0in 5.4pt'>
  <p class=MsoNormal><span style='font-family:Arial'>Healthcare pro?</span></p>
  </td>
  <td width=512 valign=top style='width:600.3pt;border-top:none;border-left:
  none;border-bottom:solid windowtext 1.0pt;border-right:solid windowtext 1.0pt;
  padding:0in 5.4pt 0in 5.4pt'>
  <p class=MsoNormal><span style='font-family:Arial'><INPUT TYPE="TEXT" SIZE="71" value = "$healthcareSpecialist" NAME="healthcareSpecialist">$healthcareSpecialist_error</span></p>
  </td>
 </tr>
 <tr>
  <td width=179 valign=top style='width:134.6pt;border:solid windowtext 1.0pt;
  border-top:none;padding:0in 5.4pt 0in 5.4pt'>
  <p class=MsoNormal><span style='font-family:Arial'>State license or none</span></p>
  </td>
  <td width=512 valign=top style='width:600.3pt;border-top:none;border-left:
  none;border-bottom:solid windowtext 1.0pt;border-right:solid windowtext 1.0pt;
  padding:0in 5.4pt 0in 5.4pt'>
  <p class=MsoNormal><span style='font-family:Arial'><INPUT TYPE="TEXT" SIZE="71" value = "$stateLicense" NAME="stateLicense"></span>$stateLicense_error</p>
  </td>
 </tr>
</table>

</div>

<p class=MsoNormal><span style='font-family:Arial'>&nbsp;</span></p>

<p class=MsoNormal align=center style='text-align:center'><span
style='font-family:Arial'><INPUT TYPE="SUBMIT" Value="Register"
NAME="submit"></span></p>

<p class=MsoNormal><span style='font-family:Arial'>Thank you</span></p>

<p class=MsoNormal><span style='font-family:Arial'>&nbsp;</span></p>

</div>
</form>

</body>

</html>
XXX;
echo $msgenform;
}

?>