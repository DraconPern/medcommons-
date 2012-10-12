<?
  require_once "mc.inc.php";

  global $acAmazonProducts;
  global $acOpenIDMode;
  global $p;
  $p = $page;

  // utility function to select correct page
  function page($x) {
    global $p;
    // error_log("checking page $x == $p");
    if(isset($p) && ($x == $p))
      return " class='selected'";
    else
      return "";
  }
?>


<div id='content'>
<div class="secondary_page1_1 secondary_portal">

<!--googleon: index-->

<div id=wrapper class="innerPageGroup">


  <div class="secondary_header">    
            <div class="sheader_right">         
              <p class="text"><img src="../images/sys_images/ACAicon_medium.gif" alt="" />still looking?</p>
              <p class="link"><a href="settingsgr.php">Group Settings</a></p>
            </div>
            <div class="section_title">
      
                            <div class="vCenter">
              <div class="vCenter_inner">     
                <div class="vCenter_body">

                <h1>Personal Settings</h1> 
                          <p>Keep track of health records and radiology for yourself, your family or your patients.</p>
                          </div> <!-- end vCenter_body -->
              </div> <!-- end vCenter_inner -->
            </div> <!-- vCenter --> 
          </div> <!-- page title -->
  </div> <!-- end secondary_header -->
    
    <div id=wrapper>
    
   <div id="blueTop">&nbsp;
	<div id="blueContainerResults">
  <style>
.alignSettings600 {float:left; font-size:1.5em;}
.alignSettings600 a {font-size:.8em; color:yellow; text-decoration:none;}
</style>
      
      			<div class="styleClear"></div>



<div class="left220w">
				<div class="questionText"> 
					<label for="fn">Your Name</label>
				</div>
</div>
<div class="alignSettings600" style='{float:left; font-size:1.4em; color:white}'>
<span>   
              <?= $first_name ?> <?= $last_name ?> &nbsp;<a href='edituser.php'>change</a> </span>
</div>

      			<div class="styleClear"></div>
      			
<div class="left220w">
				<div class="questionText"> 
					<label for="fn">Your Email</label>
				</div>
</div>
<div class="alignSettings600" style='{float:left; font-size:1.4em; color:white}' >
   <?= $email ?>&nbsp;<a href='setemail.php'>change</a>
</div>	

      			<div class="styleClear"></div>
      			
<div class="left220w">
				<div class="questionText"> 
					<label for="fn">Your Password</label>
				</div>
</div>
<div class="alignSettings600">
       ****** &nbsp;<a href='changepwd.php'>change</a>
</div>


        
      			<div class="styleClear"></div>

			<div class="middleLine"></div>
			


<div class="left220w">
				<div class="questionText"> 
					<label for="fn">Your Documents</label>
				</div>
</div>
<div class="alignSettings600">your health records and documents
      as a ZIP archive.
      <a href='gwredir.php?dest=<?=urlencode("PersonalBackup?storageId=$accid&auth=$auth")?>'>Download</a>
 </div>

      			<div class="styleClear"></div>
<div class="left220w">
				<div class="questionText"> 
					<label for="fn">Delete Account</label>
				</div>
</div>  
<div class="alignSettings600">
    You can delete all data from your HealthURL.  <a href='javascript:deleteAccount();'>Delete My HealthURL Now</a>
</div>



       

    <div id='deleteAccountRequest' style='display: none;'>
      <p>Deleting your account permanently removes all existing data from your account.</p>
      <br/>

      <p><b>This operation cannot be undone.</b></p>
      <br/>
      <p>Your account will not be deleted immediately.  You will receive an email when 
         the account contents have been deleted.</p>
    </div>

   </div>
 </div>
 </div>
 </div>
 </div>


    <script type="text/javascript">
      function saveActiveGroup(value) {
        YAHOO.util.Connect.asyncRequest('POST', 'set_dashboard_mode.php', {
          success: function(req) { 
            window.location.href='?page=personalAccount';
          },
          failure: function(req) { alert('failed: '+req.responseText);  window.location.reload();}
        }, 'accid='+value+'&enc='+hex_sha1(getCookie('mc')));
      }
      function sendDeleteRequest() {
        window.location = 'delete_account_request.php';
      }
      function deleteAccount() {
        yuiLoader().insert(function() {
          var dlg = new YAHOO.widget.SimpleDialog('deleteWarnDlg', { 
              width: '500px',
              fixedcenter:true,
              modal:true,
              visible:true,
              draggable:true,
              buttons: [ {text: 'OK - Send Delete Request', handler: sendDeleteRequest}, { text: 'Cancel', handler: function(){dlg.destroy();}} ]
          });
          dlg.setHeader('Delete Account Request - Confirmation');
          dlg.setBody(document.getElementById('deleteAccountRequest').innerHTML);
          dlg.render(document.body);
        });
      }
      function confirmUnlink(idpName) {
          return confirm('You are about to unlink your MedCommons Account from your ' + idpName + ' identity.\r\n\r\n' +
                         'This may prevent single signon between your accounts, or may stop externally '
                        +'hosted gadgets that use this identity from working.\r\n\r\n'
                        +'Are you sure you want to unlink this identity?');
      }
    </script>

</div>

        </div>
