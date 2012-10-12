
<div class="secondary_page1_1 secondary_portal">

<!--googleon: index-->

<div class="innerPageGroup">

<div class="innerPageGroup">
  <div class="secondary_header">    
            <div class="sheader_right">         
              <p class="text"><img src="../images/more.gif" alt="" />still looking?</p>
              <p class="link"><a href="psettings.php">Personal Settings</a></p>
            </div>
            <div class="section_title">
      
                            <div class="vCenter">
              <div class="vCenter_inner">     
                <div class="vCenter_body">

                <h1>Group Settings</h1> 
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

   
  <div id='personalAccount'>


<?
global $Secure_Url;
?><script type='text/javascript'>
var user = { accid: '<?=$accid?>' };
</script>

<br/>
  <h4 > http://www.medcommons.net/<?=htmlentities($practice)?>
  </h4> 

  <p>The MedCommons Groupid is: <span><?=($active_group_accid)?></span></p>
  <div id='addGroupMember'>
  <input type='image' id='inviteGroupMemberButton' value='Invite'
         title='Click to invite a new person to the group by Email'
         src='images/invite_button.png' onmousedown='this.style.top="2px";' onmouseup='this.style.top="1px"; this.blur();'/>
  <span id='groupmsg'></span> Invite Others to this Group

  </div>
  <hr/>
  <div id='groupContainer'>
  </div>
  <div style='margin-top: 10px;'>
   <p>Send imaging automatically to your DICOM workstation or PACS 
        <button onclick="location.href='dod_poller.php'">Install Polling DDL</button></p>
   
   <?if(is_feature_enabled("group.uploadPage")):?>
   <p><input type='checkbox' name='allowPublicUpload' style='position: relative; top: 2px;' onclick='enableGroupUploads(this.checked);'
       <?if($user->practice->enable_uploads):?>checked='true'<?endif;?>
       title='Place a link to this page on your web site or send them by email to enable others to send DICOM to your Group'/> 
        Receive health records with a practice dropbox 
        <button onclick="location.href='<?=$Secure_Url?>/<?=$active_group_accid?>/upload'" 
            title='Place a link to this page on your web site or send them by email to enable others to send DICOM to your Group'>
            <?=hsc($user->practice->practicename)?> Dropbox</button>
   </p>
    <?endif;?>
   <?if(is_feature_enabled("group.customLogo")):?>
   <p class='checkboxRow'><input type='checkbox' name='groupLogoCheckBox' style='position: relative; top: 2px;' onclick='enableGroupLogo(this.checked);'
       <?if($user->practice->logo_url):?>checked='true'<?endif;?>
       title='This logo will be displayed on screens associated with your group'/> 
        Show a custom logo in header of your Dropbox and pages associated with this group&nbsp;
   </p>
        <div id='changeLogoFields'
		  <?if(!$user->practice->logo_url):?>
		  class='hidden'
		  <?endif;?>
        >
		&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
		Image URL: <input type='text' id='groupLogo' name='groupLogo' size='80' 
		  value='<?=htmlentities($user->practice->logo_url, ENT_QUOTES)?>'/>
		  &nbsp;
		  <input type='image' id='changeLogoButton' value='Change'  
		         title='Click to update the group logo' 
		         src='images/change_button.png' 
		         onmousedown='this.style.top="2px";' onmouseup='this.style.top="1px"; this.blur();'/>
		         
		  <span id='grouplogoUpdate' class='updatingMsg hidden'>Updating ...</span>
		  
	  </div>
    
    <?endif;?>
    
   <?if(is_feature_enabled("group.apiSigning")):?>
   <p class='checkboxRow'><input type='checkbox' name='apisigning' id='apisigning' style='position: relative; top: 2px;' onclick='enableAPIKeys(this.checked);'
       <?if(isset($apiKeys)):?>checked='true'<?endif;?>
       title=''/> 
        &nbsp;
        Enable access to group functions by external applications using signed API calls 
        <a <?if(!isset($apiKeys)):?>class='hidden'<?endif;?> id='keysLink'
            href='javascript:enableAPIKeys(true)'>Show Keys</a>
   </p>
   <?endif;?>
   
   
   </div>
 </div>
 </div>
<!-- finishes divs in footer -->
  </div> <!-- remainder in footer -->

