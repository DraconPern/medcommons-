<?php
if ($submit) {
		$db = mysql_connect("db95.perfora.net", "dbo100827707", "meddemo");
	    mysql_select_db("db100827707",$db);
		
		
		$sql = "INSERT INTO medusers (name, company, email, comments) VALUES ('$realname', '$company', '$email', '$comments')";
		
		$result = mysql_query($sql);
		
		?>
<php3>
<html>
<head>
<title>MEDCOMMONS</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<meta name="robots" content="none">
<link href="/css/main.css" rel="stylesheet" type="text/css">
</head>

<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<table width="100%" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td bgcolor="DFF2F7"><table width="100%" border="0" cellspacing="5" cellpadding="0">
        <tr>
          <td width="256"><img src="/mc/images/logo_sm.gif" width="256" height="50"></td>
          <td align="right" valign="bottom" width="100%">
<p><a href="/mc/index.php">home</a> || register || <a href="/mc/my_account.php">download</a> 
              || <a href="/mc/contact.php">contact</a></p></td>
        </tr>
      </table></td>
  </tr>
  <tr>
    <td> <table width="80%" border="0" align="center" cellpadding="0" cellspacing="0">
        <tr>
          <td>
            <!-- Standard fields as described in Matt Wright's FormMail v1.6 readme -->
            <p>Thank you for your submission. Please verify the information below 
              and read and agree to the terms of service before clicking NEXT.</p>
			<p><b>USER INFO:</b></p>
      </td>
        </tr>
        <tr>
          <td><p>Name: <?php printf($realname); ?><br>
              Email: <?php printf($email); ?>
			  <?php if($company !="") {
			  ?>
			  <br>
              Company: <?php printf($company); ?>
			  <?php
			  }
			  if($comments !="") {
			  ?>
			  <br>
              Comments: </p>
            <?php printf($comments); ?>
			  <?php
			  }
			  ?><br><br><a href="javascript:history.back();">back up and change info</a><br><br><br><br>
			</td>
        </tr>
        <tr>
          <td><p><b>TERMS OF SERVICE</b></p><p>legalese etc....</p><p><i>by clicking next, i certify that i agree to these terms of service which are clearly very important and official sounding.</i></p> <form method="post" action="functions/BFormMail.pl">
<input type="hidden" name="subject" value="New User Added">
<input type="hidden" name="recipient" value="alex@beuscher.net">
<input type="hidden" name="required" value="recipient,realname">
<input type="hidden" name="sort" value="alphabetic">
<input type="hidden" name="print_config" value="subject, realname, email">
<input type="hidden" name="print_blank_fields" value="0">
<input type="hidden" name="title" value="Registration Complete">
<input type="hidden" name="return_link_url" value="http://www.autocyt.com/mc/download.php3">
<input type="hidden" name="return_link_title" value="Proceed to Download Login">
<input type="hidden" name="background" value="">

<input type="hidden" name="courtesy_reply" value="yes">
<input type="hidden" name="courtesy_our_email"
	value="medcommons &lt;alex@beuscher.net&gt;">
<input type="hidden" name="courtesy_our_url" value="http://www.autocyt.com/mc/">
<input type="hidden" name="courtesy_reply_textb" 
	value="http://www.autocyt.com/mc/download.php3">
<input type="hidden" name="courtesy_reply_texta" 
	value="You may use the email address this message was sent to as your username. Your password is : medcommons.">
<input type="hidden" name="courtesy_who_we_are" value="MEDCOMMONS">
<input type="hidden" name="courtesy_who_we_are2"
	value="the world of medical imaging on your desktop">
<input type="hidden" name="realname" value="<?php printf($realname); ?>">
<input type="hidden" name="email" value="<?php printf($email); ?>">
<input type="hidden" name="company" value="<?php printf($company); ?>">
<input type="hidden" name="comments" value="<?php printf($comments); ?>">
<input type="submit" value="next">
	</td>
        </tr>
      </table>
      <p>&nbsp;</p>
      <p>&nbsp; </p>
      </td>
  </tr>
</table>


</body>
</html>
<?php		
}
		else{
?>

<php3>
<html>
<head>
<title>MEDCOMMONS</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<link href="/css/main.css" rel="stylesheet" type="text/css">
<script language="JavaScript" src="functions/ContactFunctions.js"></script>
</head>

<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<table width="100%" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td bgcolor="DFF2F7"><table width="100%" border="0" cellspacing="5" cellpadding="0">
        <tr>
          <td width="256"><img src="/mc/images/logo_sm.gif" width="256" height="50"></td>
          <td align="right" valign="bottom" width="100%">
<p><a href="/mc/index.php">home</a> || register || <a href="/mc/my_account.php">download</a> 
              || <a href="/mc/contact.php">contact</a></p></td>
        </tr>
      </table></td>
  </tr>
  <tr>
    <td> <table width="80%" border="0" align="center" cellpadding="0" cellspacing="0">
        <tr>
          <td>
            <!-- Standard fields as described in Matt Wright's FormMail v1.6 readme -->
            <p>Please fill in the form below. A valid email address and name are 
              required. An email will be sent to you with your password.</p>
      </td>
        </tr>
        <tr>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td> <form name="RegisterForm" method="post" action="<?php echo $PHP_SELF?>">
<input type="hidden" name="subject" value="New User Added">
<input type="hidden" name="recipient" value="alex@beuscher.net">
<input type="hidden" name="required" value="recipient,realname">
<input type="hidden" name="sort" value="alphabetic">
<input type="hidden" name="print_config" value="subject, realname, email">
<input type="hidden" name="print_blank_fields" value="0">
<input type="hidden" name="title" value="Viewer Download Signup Form">
<input type="hidden" name="return_link_url" value="http://www.autocyt.com/mc/index.php3">
<input type="hidden" name="return_link_title" value="Back to Home Page">
<input type="hidden" name="background" value="">

<input type="hidden" name="courtesy_reply" value="yes">
<input type="hidden" name="courtesy_our_email"
	value="Support &lt;alex@beuscher.net&gt;">
<input type="hidden" name="courtesy_our_url" value="http://www.autocyt.com/mc/">
<input type="hidden" name="courtesy_reply_texta" 
	value="Thank you for your registration">
<input type="hidden" name="courtesy_reply_textb" 
	value="You may use the email address this message was sent to as your username. Your password is : medcommons.">
<input type="hidden" name="courtesy_who_we_are" value="MEDCOMMONS">
<input type="hidden" name="courtesy_who_we_are2"
	value="the world of medical imaging on your desktop">

<!--	Prompt the user for anything else... -->

<blockquote>
        
    <table width="75%" border="0" align="center" cellpadding="5" cellspacing="0" bgcolor="#CCCCCC">
      <tr align="left" valign="top"> 
        <td width="25%" align="right"><font size="2" face="Arial, Helvetica, sans-serif">email*:</font></td>
        <td width="75%">
<input type="text" name="email" size="25">
        </td>
      </tr>
      <tr align="left" valign="top"> 
        <td width="25%" align="right"><font size="2" face="Arial">name*: </font></td>
        <td width="75%"><font size="3" face="Arial"> 
          <input type="text" size="25" name="realname">
          </font></td>
      </tr>
      <tr align="left" valign="top"> 
        <td width="25%" align="right">
<p><font size="2" face="Arial, Helvetica, sans-serif">age*:</font></p></td>
        <td width="75%"><font size="3" face="Arial"> 
          <input type="text" size="5" name="age">
          <font size="2" face="Arial">[required]</font> </font></td>
      </tr>
      <tr align="left" valign="top"> 
        <td width="25%" align="right">
<p><font size="2" face="Arial, Helvetica, sans-serif">gender*:</font></p></td>
        <td width="75%">
<table width="100" border="0" cellspacing="0" cellpadding="0">
            <tr align="center"> 
              <td width="50"><font size="1" face="Arial, Helvetica, sans-serif">male</font></td>
              <td width="50"><font size="1" face="Arial, Helvetica, sans-serif">female</font></td>
            </tr>
            <tr align="center"> 
              <td width="50"> 
                <input type="radio" name="gender" value="m" checked></td>
              <td width="50"> 
                <input type="radio" name="gender" value="f"></td>
            </tr>
          </table>
        </td>
      </tr>
      <tr align="left" valign="top"> 
        <td width="25%" align="right">
<p><font size="2" face="Arial, Helvetica, sans-serif">phone number*: </font></p></td>
        <td width="75%"><font size="3" face="Arial"> 
          <input type="text" size="25" name="realname2">
          </font></td>
      </tr>
      <tr align="left" valign="top"> 
        <td width="25%" align="right">
<p><font size="2" face="Arial, Helvetica, sans-serif">are you a healthcare professional*?</font></p></td>
        <td width="75%">
<table width="100" border="0" cellspacing="0" cellpadding="0">
            <tr align="center"> 
              <td width="50"><font size="1" face="Arial, Helvetica, sans-serif">yes</font></td>
              <td width="50"><font size="1" face="Arial, Helvetica, sans-serif">no</font></td>
            </tr>
            <tr align="center"> 
              <td width="50"> <input type="radio" name="healthprof" value="1"></td>
              <td width="50"> <input type="radio" name="healthprof" value="0" checked></td>
            </tr>
          </table></td>
      </tr>
      <tr align="left" valign="top"> 
        <td width="25%" align="right">
<p><font size="2" face="Arial, Helvetica, sans-serif">are you licensed*?</font></p></td>
        <td width="75%">
<table width="100" border="0" cellspacing="0" cellpadding="0">
            <tr align="center"> 
              <td width="50"><font size="1" face="Arial, Helvetica, sans-serif">yes</font></td>
              <td width="50"><font size="1" face="Arial, Helvetica, sans-serif">no</font></td>
            </tr>
            <tr align="center"> 
              <td width="50"> <input type="radio" name="licensed" value="1"></td>
              <td width="50"> <input type="radio" name="licensed" value="0" checked></td>
            </tr>
          </table></td>
      </tr>
      <tr align="left" valign="top"> 
        <td width="25%" align="right">
<p><font size="2" face="Arial, Helvetica, sans-serif">if yes, in what state are you licensed*?</font></p></td>
        <td width="75%">
<p>
            <select name="select">
              <option value='AL' selected>AL</option>
              <option value='AR'>AR</option>
              <option value='AZ'>AZ</option>
              <option value='CA'>CA</option>
              <option value='CO'>CO</option>
              <option value='CT'>CT</option>
              <option value='DC'>DC</option>
              <option value='DE'>DE</option>
              <option value='FL'>FL</option>
              <option value='GA'>GA</option>
              <option value='HI'>HI</option>
              <option value='IA'>IA</option>
              <option value='ID'>ID</option>
              <option value='IL'>IL</option>
              <option value='IN'>IN</option>
              <option value='KS'>KS</option>
              <option value='KY'>KY</option>
              <option value='LA'>LA</option>
              <option value='MA'>MA</option>
              <option value='MD'>MD</option>
              <option value='ME'>ME</option>
              <option value='MI'>MI</option>
              <option value='MN'>MN</option>
              <option value='MO'>MO</option>
              <option value='MS'>MS</option>
              <option value='MT'>MT</option>
              <option value='NC'>NC</option>
              <option value='ND'>ND</option>
              <option value='NE'>NE</option>
              <option value='NH'>NH</option>
              <option value='NJ'>NJ</option>
              <option value='NM'>NM</option>
              <option value='NV'>NV</option>
              <option value='NY'>NY</option>
              <option value='OH'>OH</option>
              <option value='OK'>OK</option>
              <option value='OR'>OR</option>
              <option value='PA'>PA</option>
              <option value='RI'>RI</option>
              <option value='SC'>SC</option>
              <option value='SD'>SD</option>
              <option value='TN'>TN</option>
              <option value='TX'>TX</option>
              <option value='UT'>UT</option>
              <option value='VA'>VA</option>
              <option value='VT'>VT</option>
              <option value='WA'>WA</option>
              <option value='WI'>WI</option>
              <option value='WV'>WV</option>
              <option value='WY'>WY</option>
            </select>
          </p>
          </td>
      </tr>
      <tr align="left" valign="top"> 
        <td width="25%" align="right"><font size="2" face="Arial">company: </font></td>
        <td width="75%"><font size="3" face="Arial"> 
          <input type="text" size="25" name="company">
          </font></td>
      </tr>
      <tr align="left" valign="top"> 
        <td width="25%" align="right"><font size="2" face="Arial">comments:</font></td>
        <td width="75%"> 
          <textarea name="comments" cols="35" rows="6"></textarea>
        </td>
      </tr>
      <tr align="left" valign="top"> 
        <td colspan="2"> <p align="center"> 
            <input name="submit" type="submit" value="Register">
          </p></td>
      </tr>
    </table>
    
  </blockquote>
</form></td>
        </tr>
      </table>
      <p>&nbsp;</p>
      <p>&nbsp; </p>
      </td>
  </tr>
</table>


</body>
</html>
  <?php



} // end if



?>

</php3>