<html><head><title>MEDCOMMONS</title><meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"><meta name="robots" content="none">
    <link href="main.css" rel="stylesheet" type="text/css">
    <script language="javascript" src="index.php_files/form_functions.js"></script>
    <script type="text/javascript" src="cookies.js"></script>

    <script language="javascript">
      function login() {
        setCookie("loginName",document.loginForm.email.value);
        document.loginForm.submit(); 
      }

      if(getCookie("loginName")) {
        //alert('logged in as ' + getCookie("loginName"));
      }

      function submitSearch() {
        if(document.getElementById('tracknumber').value=='') { 
          alert('Please specify a tracking number.')
        } 
        else {
          if(getCookie('mctrackguid') == null ) {
            setCookie('mctrackguid','19098c6d4b0559a5df2d1ecf752adc1e');
          }
          if(getCookie('mctrack') == null ) {
            setCookie('mctrack','190982634105');
          }
          doWado(getCookie('mctrackguid'));
          document.getElementById('tracknumber').value='';
        }
      }
    </script>
</head>
<body leftmargin="0" topmargin="0" marginheight="0" marginwidth="0" onload="loggedInAs(); trackMessage(); self.focus();">
  <table border="0" cellpadding="0" cellspacing="0" width="100%">
    <tbody>
      <tr>
    <td bgcolor="#dff2f7"><%@ include file="header.jsp" %></td>
  </tr>
  <tr>
    <td><table align="center" border="0" cellpadding="0" cellspacing="0" width="80%">
        <tbody><tr> 
          <td><p>&nbsp;</p>
            <p><strong>WELCOME</strong></p></td>
        </tr>
        <tr> 
          <td><p>Welcome to Medcommons, providing the world of medical imaging 
            on your desktop<br/><br/></p>
            </td>
        </tr>
        <tr>
          <td><p><b>TRACK YOUR STUDY:</b> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;#<input type='text' name='tracknumber' value='' size='12'/> 
            <input type="button" name="trackbutton" onclick="submitSearch();" value="Go"/><br/></p><p/></td>
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
          <td><p>New users <a href="register.jsp">register here</a> for our 
              viewer download</p>
            <p>&nbsp;</p></td>
        </tr>
        <tr> 
          <td><p><strong>REGISTERED USERS</strong></p></td>
        </tr>
        <tr> 
          <td><p>Registered users enter username and password below to download 
              the viewer:</p>
              <form method="post" name="loginForm" action="my_account.jsp">
              <table align="center" border="0" cellpadding="0" cellspacing="0" width="200">
                <tbody><tr> 
                  <td align="right"><p>email: &nbsp;</p></td>
                  <td><input name="email" type="text"></td>
                </tr>
                <tr> 
                  <td align="right"><p>password:&nbsp;</p></td>
                  <td><input name="password" type="password"></td>
                </tr>
                <tr> 
                  <td align="right">&nbsp;</td>
                  <td><input name="submitButton" value="Login" type="button" onclick="login();"/></td>
                </tr>
              </tbody></table>
            </form>
            </td>
        </tr>
      </tbody></table>
      </td>
  </tr>
  <tr>
    <td>
      <table align="center" border="0" cellpadding="0" cellspacing="0" width="80%">
        <tbody><tr><td class="copyrighttext" align="left">Copyright 2004, MedCommons Inc. All Rights Reserved.</td></tr></tbody>
      </table>
    </td>
  </tr>
</tbody></table><br>
</body></html>
