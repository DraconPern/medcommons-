

<div class="secondary_page1_1 secondary_portal">

<!--googleon: index-->

<div class="innerPageGroup">


  <div class="secondary_header">    
            <div class="sheader_right">         
              <p class="text"><img src="../images/more.gif" alt="" />still looking?</p>
              <p class="link"><a href="settingsgr.php">Group Settings</a>&nbsp;<a href="settings.php">old</a></p>
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
      			
<div class="left220w">
				<div class="questionText"> 
					<label for="fn">Your Group</label>
				</div>
</div>
<div class="alignSettings600">
    


    <form id='phrs' action='switchgroups.php' method=post >
      <?foreach($practices as $p):?>
             <input type='radio' name='mode' value='<?=htmlentities($p->practicename)?>' onclick='saveActiveGroup("<?=$p->accid?>")'
                        <?if($p->accid == $active_group_accid):?>checked='true'<?endif;?>  />
                        <?=htmlentities($p->practicename)?>
                        &nbsp;  &nbsp;
                      
     <?endforeach;?>
     <input type='submit' label=change value=change >
    </form>
    
    </div>

   </div>
 </div>
 </div>
<!-- finishes divs in footer -->


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

