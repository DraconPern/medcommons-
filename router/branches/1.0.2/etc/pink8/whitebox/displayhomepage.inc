<?php

function display_login_fields($loginerror,$tracking)
{
	$x = <<<XXX
	        <tr> 
          <td><p>Registered users enter username and password below to login 
            <br>$loginerror<br></p>
              <form method="post" name="loginForm" action="loginhandler.php">
              <input type="HIDDEN" value = "$tracking" name="tracking">
            
              <table align="left" border="0" cellpadding="0" cellspacing="0" width="200">
                <tbody><tr> 
                  <td align="right"><p>userid: &nbsp;</p></td>
                  <td><input name="userid" type="text" value = "$userid">  </td>
                </tr>
                <tr> 
                  <td align="right"><p>password:&nbsp;</p></td>
                  <td><input name="password" type="password"></td>
                </tr>
                <tr> 
                  <td align="right">&nbsp;</td>
                  <td><input name="login" value="login" type="SUBMIT" ></td>
                </tr>
              </tbody></table>
            </form>
            </td>
        </tr>
XXX;
        return $x;
}

function display_link($link,$label)
{
	$x = <<<XXX
	 <tr> 
        <td><p>
		<a href="$link">$label</a>
		</p></td>
     </tr>
XXX;
return $x;
}

function display_users_links()
{
	$x = display_link("changepw.php","Change Password");
	$x .= getspeciallinks('display_link',$_SESSION['user']);
	
	//read the special_links table 
  return $x;
}

function display_home_page($banner,$userid='',$tracking='',$loginerror='')
{
$wbh = wbheader('home',"MedCommons Home");
if ($_SESSION['user']=="") $tail = display_login_fields($loginerror,$tracking);
else $tail = display_users_links();
$x = <<<XXX
$wbh
  <table border="0" cellpadding="0" cellspacing="0" width="100%">
    <tbody>
  <tr>
    <td><table align="center" border="0" cellpadding="0" cellspacing="0" width="80%">
        <tbody><tr> 
          <td><p>&nbsp;</p>
            <p><strong>WELCOME</strong></p></td>
        </tr>
        <tr> 
          <td><p>$banner<br/><br/></p>
            </td>
        </tr>
        <tr>
          <td><p>
          <form method="post" name="trackingForm" action="trackinghandler.php">
                
          <b>TRACK YOUR STUDY:</b> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;#  <input type='text' name='tracking' value="$tracking" size='12'/> 
            <input type="SUBMIT" name="trackingbutton" value="Go"/><br/></p><p/>
            </form></td>
        </tr>
        <tr>
          <td><p><strong>USER MANUAL</strong></p></td>
        </tr>
        <tr> 
          <td><p>Our user manual is available. <a href="javascript:NewWindow('manual.php.htm', 'Help', 720, 550, 'yes')">Click 
              here</a> to view.</p>
            <p>&nbsp;</p></td>
        </tr>
        <tr> 
          <td><p><strong>NEW USERS</strong></p></td>
        </tr>
        <tr> 
          <td><p>New users <a href="register.php?tracking=$tracking">register here</a> for our 
              viewer download</p>
            <p>&nbsp;</p></td>
        </tr>
        <tr> 
          <td><p><strong>REGISTERED USERS</strong></p></td>
        </tr>
$tail
      </tbody></table>
      </td>
  </tr>
  <tr>
    <td>
      <table align="center" border="0" cellpadding="0" cellspacing="0" width="80%">
        <tbody><tr><td class="copyrighttext" align="left"><br><br><br><br><br><br><br><br><p>
        Copyright 2004, MedCommons Inc. All Rights Reserved.</p></td></tr></tbody>
      </table>
    </td>
  </tr>
</tbody></table><br>
</body></html>
XXX;

echo $x;
}
?>