<?
  require_once "alib.inc.php";

  $info = get_validated_account_info();
  $gwUrl = allocate_gateway($info->accid);
  $isDDLRunning = false;
  if($isDDLRunning) {
    $uploadURL = "http://localhost:16092/CommandServlet?command=upload";
  }
  else
    $uploadURL = $gwUrl."/ddl/start?auth=".$info->auth;
?>
<div id="worklist">
    <div id="worklistButtons">
        
        <span id='newPatientWrapper' style='display: none;'>
          <?if(is_feature_enabled("dashboard.newPatient") && !$embed):?>
			  <input type="button" id="newPatientButton" name="newPatient" value="New">
			  <select id="newPatientSelect" name="newPatientSelect">
				    <option value="cd">Upload DICOM CD</option>
				    <option value="upload">Upload CCR</option>
				    <option value="new">Create CCR</option>                
	            <?if($info->enable_vouchers):?>
				    <option value="request">From Voucher</option>                
	            <?endif;?>
				  <option value="blue">From Blue Button File</option>                
			  </select>         
		  <?endif;?>
		  <?if($embed):?>
		      <a href='/acct/home.php' target='_new'>Open Full Size Dashboard</a>
		  <?endif;?>
		</span> 
    </div>
  <?
     $_REQUEST['pid']=$info->practice->practiceid;
     $_REQUEST['widget']=true;

     # Forward to real page
     include "rls.php";
  ?>
</div>
<?if(req('voucherid')):?>
<script type='text/javascript'>
addLoadEvent(function() {
	showVoucher('', '<?=htmlentities(req('voucherid'),ENT_QUOTES)?>');	
});
</script>
<?endif;?>
<script type='text/javascript'>
addLoadEvent(function() {
    addElementClass(document.body,'yui-skin-sam');

    if($('newPatientButton')) {
		new YAHOO.widget.Button("newPatientButton", { 
	        type: "menu", 
	        menu: "newPatientSelect" }
	    ).getMenu().subscribe("click", function(type,args) {
	        var menu = args[1];
	        // alert('You selected ' + menu.value );
	        if(menu.value == "cd") {
	            window.location.href='dod.php?accid=<?=$info->practice->accid?>';
	        }
	        else 
	        if(menu.value == "new") {
	            window.open('<?=new_ccr_url($info->accid,$info->auth)?>','ccr');
	        }
	        else
	        if(menu.value == "upload") {
	            window.open('<?=allocate_gateway($info->accid)?>/tracking.jsp?tracking=import&auth=<?=$info->auth?>','ccr');
	        }
	        else
	        if(menu.value == "blue") {
	            window.location.href='/router/blue?auth=<?=$info->auth?>';
	        }
	        else
	        if(menu.value == "request") {
	            yuiLoader().insert(function() {
	                var dlg = new YAHOO.widget.SimpleDialog('lookupRequestDlg', { 
	                    width: '500px',
	                    fixedcenter:true,
	                    modal:true,
	                    visible:true,
	                    draggable:true,
	                    buttons: [ {text: 'OK', handler: function() { 
										                    showVoucher('', $('roirid').value); this.destroy(); 
										                 }}, 
	                               { text: 'Cancel', handler: function(){this.destroy();}} ]
	                });
	                dlg.setHeader('Lookup Voucher');
	                dlg.setBody('<p>Please enter the Voucher ID below:</p><br/>'
	                           +"Voucher ID:  <input type='text' id='roirid'/>");
	                dlg.render(document.body);
	                $('roirid').focus();
	              });
	        }
	    });
    }
	$('newPatientWrapper').style.display = 'inline';
});
</script>

<style>
    li.yuimenuitem {
      background-position: 5px 3px;
      background-repeat: no-repeat;
      cursor: pointer;
    }
    li#yui-gen1 {
      background-image: url("images/cd.png");
    }
    li#yui-gen2 {
      background-image: url("images/upccr.png");
    }
    li#yui-gen3 {
      background-image: url("images/newccr.png");
    }
    
	<?$index=4; if($info->enable_vouchers):?>
    li#yui-gen<?=$index++?> {
      background-image: url("images/voucher-icon.png");
      background-position: 5px 2px;
    }
    <?endif;?>
    
    li#yui-gen<?=$index?> {
      background-image: url("images/bluebuttonuptiny.png");
      background-position: 2px 0px;
    }
     
    .yui-skin-sam #newPatientButton {
        position: relative;
        border-style: none !important;
	    background-image: url(images/newpatientbutton.png) !important;
	    background-repeat: norepeat;
    }
    
    .yui-skin-sam #newPatientButton * {
        border-style: none !important;
    }
    
    .yui-skin-sam #newPatientButton button {
        color: white !important;
        font-weight: bold !important;
        height: 30px;
        width: 80px;
    }
    
    #newPatientButtonWrapper ul li {
        background-color: #fffceb;
    }
    
    
    
    
    
    .yui-skin-sam .yui-menu-button button     {
	    background-image: none !important;
	    padding:0 17px 0 3px !important;
    }
    
    .yui-button-focus,yui-menu-button-focus {
	  background-position: 0px 0px !important;
        
    }
	.yui-menu-button-hover, .yui-menu-button-active, .yui-menu-button-activeoption {
	  background-position: 0px -29px !important;
	}
    .yui-skin-sam .yui-button-hover button,.yui-skin-sam .yui-button-hover a {
      color:#000;
    }    
    
</style>

<link rel="stylesheet" type="text/css" href="/zip/yui/2.8.0r4/assets/skins/sam/button.css,yui/2.8.0r4/menu/assets/skins/sam/menu.css">
<script type="text/javascript" src="/zip/yui/2.8.0r4/yahoo-dom-event/yahoo-dom-event.js,yui/2.8.0r4/container/container_core-min.js,yui/2.8.0r4/menu/menu-min.js,yui/2.8.0r4/element/element-min.js,yui/2.8.0r4/button/button-min.js"></script>
