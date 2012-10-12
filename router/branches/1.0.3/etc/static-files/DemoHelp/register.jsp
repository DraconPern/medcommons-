<%@ taglib uri="http://jakarta.apache.org/struts/tags-tiles" prefix="tiles" %>
<html>
 <head>
   <title>MEDCOMMONS</title><meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
   <link href="main.css" rel="stylesheet" type="text/css">
   <script type="text/javascript" src="cookies.js"></script>
   <script language="JavaScript" src="register.php_files/ContactFunctions.js"></script>
 </head>
<body leftmargin="0" topmargin="0" marginheight="0" marginwidth="0" onload="loggedInAs();">
  <table border="0" cellpadding="0" cellspacing="0" width="100%">
  <tbody><tr>
    <td bgcolor="#dff2f7"><tiles:insert page="header.jsp"/></td>
  </tr>
  <tr>
    <td> <table align="center" border="0" cellpadding="0" cellspacing="0" width="80%">
        <tbody><tr>
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
          <td> <form name="RegisterForm" method="post" action="index.php.htm">
<input name="subject" value="New User Added" type="hidden">
<input name="recipient" value="alex@beuscher.net" type="hidden">
<input name="required" value="recipient,realname" type="hidden">
<input name="sort" value="alphabetic" type="hidden">
<input name="print_config" value="subject, realname, email" type="hidden">
<input name="print_blank_fields" value="0" type="hidden">
<input name="title" value="Viewer Download Signup Form" type="hidden">
<input name="return_link_url" value="index.php3.htm" type="hidden">
<input name="return_link_title" value="Back to Home Page" type="hidden">
<input name="background" value="" type="hidden">

<input name="courtesy_reply" value="yes" type="hidden">
<input name="courtesy_our_email" value="Support &lt;alex@beuscher.net&gt;" type="hidden">
<input name="courtesy_our_url" value="http://www.autocyt.com/mc/" type="hidden">
<input name="courtesy_reply_texta" value="Thank you for your registration" type="hidden">
<input name="courtesy_reply_textb" value="You may use the email address this message was sent to as your username. Your password is : medcommons." type="hidden">
<input name="courtesy_who_we_are" value="MEDCOMMONS" type="hidden">
<input name="courtesy_who_we_are2" value="the world of medical imaging on your desktop" type="hidden">

<!--	Prompt the user for anything else... -->

<blockquote>
        
    <table align="center" bgcolor="#cccccc" border="0" cellpadding="5" cellspacing="0" width="75%">
      <tbody><tr align="left" valign="top"> 
        <td align="right" width="25%"><font face="Arial, Helvetica, sans-serif" size="2">email*:</font></td>
        <td width="75%">
<input name="email" size="25" type="text">
        </td>
      </tr>
      <tr align="left" valign="top"> 
        <td align="right" width="25%"><font face="Arial" size="2">name*: </font></td>
        <td width="75%"><font face="Arial" size="3"> 
          <input size="25" name="realname" type="text">
          </font></td>
      </tr>
      <tr align="left" valign="top"> 
        <td align="right" width="25%">
<p><font face="Arial, Helvetica, sans-serif" size="2">age*:</font></p></td>
        <td width="75%"><font face="Arial" size="3"> 
          <input size="5" name="age" type="text">
          <font face="Arial" size="2">[required]</font> </font></td>
      </tr>
      <tr align="left" valign="top"> 
        <td align="right" width="25%">
<p><font face="Arial, Helvetica, sans-serif" size="2">gender*:</font></p></td>
        <td width="75%">
<table border="0" cellpadding="0" cellspacing="0" width="100">
            <tbody><tr align="center"> 
              <td width="50"><font face="Arial, Helvetica, sans-serif" size="1">male</font></td>
              <td width="50"><font face="Arial, Helvetica, sans-serif" size="1">female</font></td>
            </tr>
            <tr align="center"> 
              <td width="50"> 
                <input name="gender" value="m" checked="checked" type="radio"></td>
              <td width="50"> 
                <input name="gender" value="f" type="radio"></td>
            </tr>
          </tbody></table>
        </td>
      </tr>
      <tr align="left" valign="top"> 
        <td align="right" width="25%">
<p><font face="Arial, Helvetica, sans-serif" size="2">phone number*: </font></p></td>
        <td width="75%"><font face="Arial" size="3"> 
          <input size="25" name="realname2" type="text">
          </font></td>
      </tr>
      <tr align="left" valign="top"> 
        <td align="right" width="25%">
<p><font face="Arial, Helvetica, sans-serif" size="2">are you a healthcare professional*?</font></p></td>
        <td width="75%">
<table border="0" cellpadding="0" cellspacing="0" width="100">
            <tbody><tr align="center"> 
              <td width="50"><font face="Arial, Helvetica, sans-serif" size="1">yes</font></td>
              <td width="50"><font face="Arial, Helvetica, sans-serif" size="1">no</font></td>
            </tr>
            <tr align="center"> 
              <td width="50"> <input name="healthprof" value="1" type="radio"></td>
              <td width="50"> <input name="healthprof" value="0" checked="checked" type="radio"></td>
            </tr>
          </tbody></table></td>
      </tr>
      <tr align="left" valign="top"> 
        <td align="right" width="25%">
<p><font face="Arial, Helvetica, sans-serif" size="2">are you licensed*?</font></p></td>
        <td width="75%">
<table border="0" cellpadding="0" cellspacing="0" width="100">
            <tbody><tr align="center"> 
              <td width="50"><font face="Arial, Helvetica, sans-serif" size="1">yes</font></td>
              <td width="50"><font face="Arial, Helvetica, sans-serif" size="1">no</font></td>
            </tr>
            <tr align="center"> 
              <td width="50"> <input name="licensed" value="1" type="radio"></td>
              <td width="50"> <input name="licensed" value="0" checked="checked" type="radio"></td>
            </tr>
          </tbody></table></td>
      </tr>
      <tr align="left" valign="top"> 
        <td align="right" width="25%">
<p><font face="Arial, Helvetica, sans-serif" size="2">if yes, in what state are you licensed*?</font></p></td>
        <td width="75%">
           <p>
            <select name="select"><option value=""/><option value="AR"> AL</option><option value="AR">AR</option><option value="AZ">AZ</option><option value="CA">CA</option><option value="CO">CO</option><option value="CT">CT</option><option value="DC">DC</option><option value="DE">DE</option><option value="FL">FL</option><option value="GA">GA</option><option value="HI">HI</option><option value="IA">IA</option><option value="ID">ID</option><option value="IL">IL</option><option value="IN">IN</option><option value="KS">KS</option><option value="KY">KY</option><option value="LA">LA</option><option value="MA">MA</option><option value="MD">MD</option><option value="ME">ME</option><option value="MI">MI</option><option value="MN">MN</option><option value="MO">MO</option><option value="MS">MS</option><option value="MT">MT</option><option value="NC">NC</option><option value="ND">ND</option><option value="NE">NE</option><option value="NH">NH</option><option value="NJ">NJ</option><option value="NM">NM</option><option value="NV">NV</option><option value="NY">NY</option><option value="OH">OH</option><option value="OK">OK</option><option value="OR">OR</option><option value="PA">PA</option><option value="RI">RI</option><option value="SC">SC</option><option value="SD">SD</option><option value="TN">TN</option><option value="TX">TX</option><option value="UT">UT</option><option value="VA">VA</option><option value="VT">VT</option><option value="WA">WA</option><option value="WI">WI</option><option value="WV">WV</option><option value="WY">WY</option></select>
          </p>
          </td>
      </tr>
      <tr align="left" valign="top"> 
        <td align="right" width="25%"><font face="Arial" size="2">company: </font></td>
        <td width="75%"><font face="Arial" size="3"> 
          <input size="25" name="company" type="text">
          </font></td>
      </tr>
      <tr align="left" valign="top"> 
        <td align="right" width="25%"><font face="Arial" size="2">comments:</font></td>
        <td width="75%"> 
          <textarea name="comments" cols="35" rows="6"></textarea>
        </td>
      </tr>
      <tr align="left" valign="top"> 
        <td colspan="2"> <p align="center"> 
            <input name="submit" value="Register" type="submit">
          </p></td>
      </tr>
    </tbody></table>
    
  </blockquote>
</form></td>
        </tr>
      </tbody></table>
      <p>&nbsp;</p>
      <p>&nbsp; </p>
      </td>
  </tr>
</tbody></table>


</body></html>
