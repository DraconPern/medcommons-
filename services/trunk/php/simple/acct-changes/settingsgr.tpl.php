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
<!-- Sam Skin CSS for TabView -->
<link rel="stylesheet" type="text/css" href="/zip/yui/2.7.0/tabview/assets/skins/sam/tabview.css">

<style type='text/css'>
.yui-skin-sam .yui-navset .yui-content {
  background-color: #f8f8f8 !important;
  padding: 10px;
}
.yui-skin-sam .yui-navset li a em {
  color: #333;
}
.yui-nav {
  border-color: #828076 !important;
}
.yui-skin-sam .yui-navset li.selected a em {
  color: white;
  font-weight: bold;
}
#wrapper .yui-navset a:hover
{
    text-decoration: none;
}

table.creditsTable {
  text-align: center;
  border-collapse: collapse;
  background-color: white;
  margin-top: 20px;
}
table.creditsTable tr th {
  border-bottom-style: solid;
  border-bottom-width: 1px;
  border-bottom-color: #333;
  color: #333;
}

table.creditsTable tr th, 
table.creditsTable tr td {
  text-align: center;
  padding: 5px 25px;
}
#externalUsersTable th,
#externalUsersTable td {
    text-align: left;
    padding: 0px 9px;
}
#externalUsersTable td {
    background-color: white;
}
div.f,form.p .f,form.p p  {      clear: both;      padding-top: 10px;}
div.f span.n,form.p .n,form.p label {      float: left;      margin-left:50px;      width: 150px;      text-align: right;}
div.f span.q,form.p .q {      float: left;      margin-left: 10px;      width: 400px;      text-align: left;}
div.f span.q span.r,form.p .r {      padding: 10px;      font-size: .8em;}
.error { color: red; }
#content {
min-width: 670px;
}

</style>


<div id='content'>
<div class="secondary_page1_1 secondary_portal">

<!--googleon: index-->

<div class="innerPageGroup">
  <div class="secondary_header">    
            <div class="sheader_right">         
              <p class="text"><img src="../images/sys_images/ACAicon_medium.gif" alt="" />still looking?</p>
              <p class="link"><a href="settings.php">Personal Settings</a></p>
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
<?/*
  This page uses a YUI tab control that is built from the existing markup.
  Each tab is represented by a child <div> of class 'yui-content'
 */?>

   

  <!-- p>
  <a href='/version.php'>Version Information</a> about this MedCommons Appliance
  </p -->
<?if(is_feature_enabled("settings.personalAccount")):?>      
  <div id='personalAccount'>
    <form id='phrs'>
            <br/>Group: &nbsp;
            <?foreach($practices as $p):?>
             <input type='radio' name='mode' value='group' onclick='saveActiveGroup("<?=$p->accid?>")'
                        <?if($p->accid == $active_group_accid):?>checked='true'<?endif;?>  />
                        <?=htmlentities($p->practicename)?>
                        &nbsp;
                      
            <?endforeach;?>
         
       
  
    </form>

<?endif;?>


  <div id='groups'>
    <?if($is_group_member && $active_group_accid):?>
      <?= $template->fetch('groups.tpl.php') ?>
    <?else:?>
      <?= $template->fetch('no_group.tpl.php') ?>
    <?endif;?>
  </div>


</div>
</div>


    <script type="text/javascript" src="/zip/yui/2.7.0/utilities/utilities.js,yui/2.7.0/tabview/tabview-min.js,yui/2.7.0/datasource/datasource-min.js,yui/2.7.0/datatable/datatable-min.js,acct/utils.js,acct/sha1.js,acct/settings.js"></script>

    <script type='text/javascript'>

      function tabFromLabel(label) {
          var allTabs = tabs.get('tabs');
          for(var i = 0; i<allTabs.length; ++i) {
	    	  if(allTabs[i].get('label').indexOf(label)>=0)
		    	  return allTabs[i];
          }
      }

      YAHOO.util.Event.onDOMReady(function() { 
	      document.body.className='yui-skin-sam';
	      window.tabs = new YAHOO.widget.TabView("profileTabs");
	      var addressesIndex = tabs.get('tabs')
	      if($('addresses')) {
		      tabFromLabel('Address').addListener('click',function(e) {
		        initAddresses();
		      });
	      }
	      
	      tabFromLabel('Group').addListener('click',function(e) {
	        initGroups();
	      });
	      if(www_init)
	        www_init();
	      <?if($acctype == 'VOUCHER'):?>
	        var allTabs = tabs.get('tabs');
	        for(var i=0; i<allTabs.length; ++i) {
	          if(allTabs[i].get('label') != 'Password') {
	            tabs.removeTab(allTabs[i]);
	            i--;
	          }
	        }
	      <?endif;?>
	      document.getElementById('profileTabs').style.display='block';
	      // Get full yui loading in background
	      // window.setTimeout(yuiLoader(),1500);
    });
    </script>


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
<?if($page == "groups"):?>
      initGroups();
<?elseif($page == "addresses"):?>
      initAddresses();
<?endif;?>
    </script>
    <link rel="stylesheet" type="text/css" href="/zip/yui/2.7.0/datatable/assets/skins/sam/datatable.css">

