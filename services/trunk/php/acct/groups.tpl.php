<?
global $Secure_Url;
function getUploadUrl($g,$type) {
    $UploadHandler = 'uh.php';
    $GroupUploadURL = "/acct/".$g->accid.'/upload';
    $arg = base64_encode ($g->practicename.'|'.$g->practicename.'|'.$g->accid.'|'
        .$GroupUploadURL.'|'.$g->logo_url.'|'.$UploadHandler);
    
    $UploadForm = "/acct/putils/uploader$type.php";
    return "$UploadForm?a=$arg";
}
?><script type='text/javascript'>
var user = { accid: '<?=$accid?>' };
</script>
<style type='text/css'>
  #addGroupMember {
    margin: 10px 0px;
  }
  #addGroupMember input#addgroupMemberButton { 
    vertical-align: middle;
    position: relative;
  }
  #addGroupMember *, #groupNameHeader * {
    vertical-align: middle;
  }
  #addGroupMember input#maccid, input#groupName { 
    font-size: 11px;
    width: 12em; 
    margin: 0px 0.5em 0px 0.5em;
    font-weight: normal;
  } 
  #groupContainer div.yui-dt-col-accid {
    text-align: center;
  }
  #groupContainer img.clickable{
    cursor: pointer;
  }
  #groupmsg {
    padding-left: 2em;
    color: orange;
  }
  #groupContainer table {
    background-color: white;
    border-collapse: collapse;
    width: 100%;
  }
  table tbody tr.highlight, table tbody tr.highlight td {
    background-color: #FFD490 !important;
  }
  .yui-dialog .bd table th {
    text-align: right;
  }
  .yui-dialog .bd table td,
  .yui-dialog .bd table th {
    padding: 5px 10px;
    border: solid 1px #444;
  }
  .yui-dialog .bd table {
    margin: 0px 100px;
    width: 300px;
    background-color: white;
    border-collapse: collapse;
  }
  .updatingMsg, #updatedMsg {
    font-weight: normal;
    color: orange;
    font-size: 11px;
  }
  #updatedMsg {
    display: none;
  }
  #groupAcctId h4 {
    display: inline;
  }
  #groupAcctId {
    position: absolute;
    right: 20px;
    font-size: 11px;
  }
  #inviteEmailsDlg ol {
      margin-left: 60px;
  }
  #inviteEmailsDlg .inviteEmail {
    display: inline;
    margin: 0.5em 0px 0px 0px;
    width: 20em;
  }
  .inviteEmail.pending {
      /* background-color: #f2f2f2; */
      color: #ccc;
  }
  table tr.pending {
      color: #aaa;
  }
  .hidden {
    display: none;
  }
  #changeLogoFields * {
    vertical-align: middle;
  }
  #notifyEmailOuter {
      margin-left: 1em;
  }
  
  #emailTip {
      color: gray;
      font-style: italics;
  }
  #notificationEmailMsg, #orderNotificationEmail, #emailTip {
      margin-left: 1.5em;
  }
  #orderNotificationEmail {
      position: relative;
      top: 3px;
      width: 30em;
  }
  #changeEmailsButton {
      position: relative;
      top: 7px;
  }
  
  
</style>
<br/>
<div id='groupsPanel'>
  <div id='groupAcctId'><h4>Group Account ID: </h4> <span><?=pretty_mcid($active_group_accid)?></span></div>
  <h4 id='groupNameHeader'>Group: <input type='text' id='groupName' value='<?=htmlentities($practice)?>'/>
  <input type='image' id='changeButton' value='Change'  
         title='Click to update the group name' 
         src='images/change_button.png' onclick='changeGroupName();' onmousedown='this.style.top="2px";' onmouseup='this.style.top="1px"; this.blur();'/>
         <span id='updatedMsg'>Updated</span>
  </h4> 
  <br/>
  
  <p>This page displays the members in your active Group.
	  <?if(is_feature_enabled("settings.personalAccount")):?>You can switch your active group on the <a href='?page=personalAccount'>My HealthURL</a> tab.<?endif;?>
  </p>
  <p>View your <a href='home.php'>Dashboard</a> to manage patients in your group.
     Log out and sign in again to return to the standard Inbox. 
  </p>
  <div id='addGroupMember'>
  <input type='image' id='inviteGroupMemberButton' value='Invite'
         title='Click to invite a new person to the group by Email'
         src='images/invite_button.png' onmousedown='this.style.top="2px";' onmouseup='this.style.top="1px"; this.blur();'/>
  <span id='groupmsg'></span>

  </div>
  <div id='groupContainer'>
  </div>

</div>
